package mcjty.rftoolsbuilder.modules.scanner.network;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lib.varia.RLE;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.scanner.ScannerConfiguration;
import mcjty.rftoolsbuilder.modules.scanner.ScannerConfiguration.ProjectorCompressionCodec;
import mcjty.rftoolsbuilder.shapes.RenderData;
import mcjty.rftoolsbuilder.shapes.ShapeDataManagerClient;
import mcjty.rftoolsbuilder.shapes.ShapeID;
import mcjty.rftoolsbuilder.shapes.StatePalette;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public record PacketReturnShapeData(ShapeID shapeID, int checksum, @Nullable byte[] positionData, PositionCodec positionCodec,
                                    int positionDataLength, StatePalette statePalette, BlockPos dimension, int count,
                                    int offsetY, String msg) implements CustomPacketPayload {

    public static ResourceLocation ID = new ResourceLocation(RFToolsBuilder.MODID, "returnshapedata");
    private static final int COMPRESSION_MIN_BYTES = 256;
    private static final int COMPRESSION_MIN_GAIN = 32;
    private static final int COMPRESSION_LEVEL = Deflater.BEST_COMPRESSION;

    private enum PositionCodec {
        RLE(0, false),
        RLE_DEFLATE(1, true),
        PACKED_BITS(2, false),
        PACKED_BITS_DEFLATE(3, true);

        private final int id;
        private final boolean compressed;

        PositionCodec(int id, boolean compressed) {
            this.id = id;
            this.compressed = compressed;
        }

        public int getId() {
            return id;
        }

        public boolean isCompressed() {
            return compressed;
        }

        public static PositionCodec byId(int id) {
            for (PositionCodec codec : values()) {
                if (codec.id == id) {
                    return codec;
                }
            }
            throw new IllegalArgumentException("Unknown position codec id: " + id);
        }
    }

    private record EncodedPayload(PositionCodec codec, byte[] payload, int decodedLength) {
        private int wireSize() {
            int size = 1 + FriendlyByteBuf.getVarIntSize(payload.length) + payload.length;
            if (codec.isCompressed()) {
                size += FriendlyByteBuf.getVarIntSize(decodedLength);
            }
            return size;
        }
    }

    private record CodecFamilyStats(EncodedPayload best, int encodedBytes, int deflatedBytes, long prepNanos, int bitsPerValue) {
    }

    private static class CompressionLog {
        private static long planeCount = 0;
        private static long totalRawBytes = 0;
        private static long totalLegacyBytes = 0;
        private static long totalLegacyDeflatedBytes = 0;
        private static long totalPackedBytes = 0;
        private static long totalPackedDeflatedBytes = 0;
        private static long totalWireBytes = 0;
        private static long legacyDeflatedCount = 0;
        private static long packedCount = 0;
        private static long packedDeflatedCount = 0;
        private static long totalLegacyPrepNanos = 0;
        private static long totalPackedPrepNanos = 0;
        private static long totalPaletteSize = 0;
        private static long totalPackedBits = 0;
        private static final long[] wins = new long[PositionCodec.values().length];

        private static synchronized void record(int rawBytes, int paletteSize, CodecFamilyStats legacy, CodecFamilyStats packed, EncodedPayload best) {
            if (!ScannerConfiguration.projectorCompressionLogging.get()) {
                return;
            }
            planeCount++;
            totalRawBytes += rawBytes;
            totalWireBytes += best.wireSize();
            if (legacy != null) {
                totalLegacyBytes += legacy.encodedBytes();
                totalLegacyPrepNanos += legacy.prepNanos();
                if (legacy.deflatedBytes() >= 0) {
                    totalLegacyDeflatedBytes += legacy.deflatedBytes();
                    legacyDeflatedCount++;
                }
            }
            if (packed != null) {
                totalPackedBytes += packed.encodedBytes();
                totalPackedPrepNanos += packed.prepNanos();
                totalPaletteSize += paletteSize;
                totalPackedBits += packed.bitsPerValue();
                packedCount++;
                if (packed.deflatedBytes() >= 0) {
                    totalPackedDeflatedBytes += packed.deflatedBytes();
                    packedDeflatedCount++;
                }
            }
            wins[best.codec().ordinal()]++;

            int interval = Math.max(1, ScannerConfiguration.projectorCompressionLogInterval.get());
            if ((planeCount % interval) != 0) {
                return;
            }

            long avgRaw = totalRawBytes / planeCount;
            long avgLegacy = totalLegacyBytes / planeCount;
            long avgLegacyDeflate = legacyDeflatedCount == 0 ? -1 : totalLegacyDeflatedBytes / legacyDeflatedCount;
            long avgPacked = packedCount == 0 ? -1 : totalPackedBytes / packedCount;
            long avgPackedDeflate = packedDeflatedCount == 0 ? -1 : totalPackedDeflatedBytes / packedDeflatedCount;
            long avgWire = totalWireBytes / planeCount;
            long avgLegacyPrep = totalLegacyPrepNanos / planeCount / 1000L;
            long avgPackedPrep = packedCount == 0 ? -1 : totalPackedPrepNanos / packedCount / 1000L;
            long avgPalette = packedCount == 0 ? -1 : totalPaletteSize / packedCount;
            long avgBits = packedCount == 0 ? -1 : totalPackedBits / packedCount;
            double savingsVsRaw = totalRawBytes == 0 ? 0.0 : (100.0 * (totalRawBytes - totalWireBytes)) / totalRawBytes;

            RFToolsBuilder.setup.getLogger().info(
                    "Projector compression over {} planes using {}: avg raw={}B, avg legacy={}B, avg legacy+deflate={}B, avg packed={}B, avg packed+deflate={}B, avg chosen wire={}B, avg palette={}, avg bits={}, savings vs raw={}%, avg prep[legacy={}us, packed={}us], wins[legacy={}, legacy+deflate={}, packed={}, packed+deflate={}]",
                    planeCount,
                    ScannerConfiguration.projectorCompressionCodec.get(),
                    avgRaw,
                    avgLegacy,
                    avgLegacyDeflate < 0 ? "n/a" : Long.toString(avgLegacyDeflate),
                    avgPacked < 0 ? "n/a" : Long.toString(avgPacked),
                    avgPackedDeflate < 0 ? "n/a" : Long.toString(avgPackedDeflate),
                    avgWire,
                    avgPalette < 0 ? "n/a" : Long.toString(avgPalette),
                    avgBits < 0 ? "n/a" : Long.toString(avgBits),
                    String.format("%.1f", savingsVsRaw),
                    avgLegacyPrep,
                    avgPackedPrep < 0 ? "n/a" : Long.toString(avgPackedPrep),
                    wins[PositionCodec.RLE.ordinal()],
                    wins[PositionCodec.RLE_DEFLATE.ordinal()],
                    wins[PositionCodec.PACKED_BITS.ordinal()],
                    wins[PositionCodec.PACKED_BITS_DEFLATE.ordinal()]
            );

            planeCount = 0;
            totalRawBytes = 0;
            totalLegacyBytes = 0;
            totalLegacyDeflatedBytes = 0;
            totalPackedBytes = 0;
            totalPackedDeflatedBytes = 0;
            totalWireBytes = 0;
            legacyDeflatedCount = 0;
            packedCount = 0;
            packedDeflatedCount = 0;
            totalLegacyPrepNanos = 0;
            totalPackedPrepNanos = 0;
            totalPaletteSize = 0;
            totalPackedBits = 0;
            for (int i = 0; i < wins.length; i++) {
                wins[i] = 0;
            }
        }
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        shapeID.toBytes(buf);
        buf.writeVarInt(checksum);
        buf.writeVarInt(count);
        buf.writeVarInt(offsetY);
        buf.writeUtf(msg);
        buf.writeBlockPos(dimension);

        if (statePalette == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            statePalette.writeToBuf(buf);
        }

        if (positionData == null) {
            buf.writeBoolean(false);
        } else {
            if (positionData.length == 0) {
                buf.writeBoolean(false);
                return;
            }
            buf.writeBoolean(true);
            buf.writeByte(positionCodec.getId());
            if (positionCodec.isCompressed()) {
                buf.writeVarInt(positionDataLength);
            }
            buf.writeVarInt(positionData.length);
            buf.writeBytes(positionData);
        }
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static PacketReturnShapeData create(FriendlyByteBuf buf) {
        ShapeID shapeID = new ShapeID(buf);
        int checksum = buf.readVarInt();
        int count = buf.readVarInt();
        int offsetY = buf.readVarInt();
        String msg = buf.readUtf();
        BlockPos dimension = buf.readBlockPos();
        StatePalette statePalette;
        byte[] positionData;
        PositionCodec positionCodec;
        int positionDataLength;

        if (!buf.readBoolean()) {
            statePalette = null;
        } else {
            statePalette = StatePalette.readFromBuf(buf);
        }

        if (!buf.readBoolean()) {
            positionData = null;
            positionCodec = PositionCodec.RLE;
            positionDataLength = 0;
        } else {
            positionCodec = PositionCodec.byId(buf.readByte());
            positionDataLength = positionCodec.isCompressed() ? buf.readVarInt() : -1;
            int size = buf.readVarInt();
            positionData = new byte[size];
            buf.readBytes(positionData);
        }
        return new PacketReturnShapeData(shapeID, checksum, positionData, positionCodec, positionDataLength, statePalette, dimension, count, offsetY, msg);
    }

    public static PacketReturnShapeData create(ShapeID id, int checksum, RLE positions, StatePalette statePalette, BlockPos dimension, int count, int offsetY, String msg) {
        EncodedPayload payload = encodePositions(positions, statePalette, dimension);
        return new PacketReturnShapeData(id, checksum, payload.payload(), payload.codec(), payload.decodedLength(), statePalette, dimension, count, offsetY, msg);
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            RenderData.RenderPlane plane = decodePlane();
            ShapeDataManagerClient.queueRenderPlane(shapeID, checksum, plane, offsetY, dimension.getY(), msg);
        });
    }

    @Nullable
    private RenderData.RenderPlane decodePlane() {
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();

        RenderData.RenderPlane plane = null;

        if (positionData != null) {
            BlockState dummy = BuilderModule.SUPPORT.get().defaultBlockState();
            List<BlockState> palette = statePalette == null ? List.of() : statePalette.getPalette();
            PositionReader reader = createPositionReader(dx * dz);

            int oy = offsetY;
            int y = oy - dy / 2;

            RenderData.RenderStrip[] strips = new RenderData.RenderStrip[dx];
            for (int ox = 0; ox < dx; ox++) {
                int x = ox - dx / 2;

                RenderData.RenderStrip strip = new RenderData.RenderStrip(x);
                strips[ox] = strip;

                for (int oz = 0; oz < dz; oz++) {
                    int data = reader.read();
                    if (data < 255) {
                        if (data == 0) {
                            strip.add(dummy);
                        } else {
                            int index = data - 1;
                            if (index >= 0 && index < palette.size()) {
                                strip.add(palette.get(index));
                            } else {
                                strip.add(dummy);
                            }
                        }
                    } else {
                        strip.add(null);
                    }
                }

                strip.close();
            }
            plane = new RenderData.RenderPlane(strips, y, oy, -dz / 2, count);
        }
        return plane;
    }

    private PositionReader createPositionReader(int expectedCount) {
        byte[] data = positionCodec.isCompressed() ? decompress(positionData, positionDataLength) : positionData;
        return switch (positionCodec) {
            case RLE, RLE_DEFLATE -> new RleReader(data, expectedCount);
            case PACKED_BITS, PACKED_BITS_DEFLATE -> new PackedBitsReader(data, expectedCount);
        };
    }

    private interface PositionReader {
        int read();
    }

    private static class RleReader implements PositionReader {
        private final byte[] data;
        private final int expectedCount;
        private int index = 0;
        private int count = 0;
        private int value = 0;
        private int produced = 0;

        private RleReader(byte[] data, int expectedCount) {
            this.data = data;
            this.expectedCount = expectedCount;
        }

        public int read() {
            if (produced >= expectedCount) {
                return 0;
            }
            if (count == 0) {
                if (index + 1 >= data.length) {
                    throw new IllegalStateException("Corrupt RLE plane payload");
                }
                count = data[index++] & 0xff;
                value = data[index++] & 0xff;
            }
            count--;
            produced++;
            return value;
        }
    }

    private static class PackedBitsReader implements PositionReader {
        private final byte[] data;
        private final int expectedCount;
        private final int bitsPerValue;
        private final int mask;
        private int index = 1;
        private long buffer = 0L;
        private int bufferedBits = 0;
        private int produced = 0;

        private PackedBitsReader(byte[] data, int expectedCount) {
            if (data.length == 0) {
                throw new IllegalStateException("Corrupt packed plane payload");
            }
            this.data = data;
            this.expectedCount = expectedCount;
            this.bitsPerValue = data[0] & 0xff;
            if (bitsPerValue <= 0 || bitsPerValue > 30) {
                throw new IllegalStateException("Invalid packed plane bit width: " + bitsPerValue);
            }
            this.mask = (1 << bitsPerValue) - 1;
        }

        @Override
        public int read() {
            if (produced >= expectedCount) {
                return 0;
            }
            while (bufferedBits < bitsPerValue) {
                if (index >= data.length) {
                    throw new IllegalStateException("Corrupt packed plane payload");
                }
                buffer |= (long) (data[index++] & 0xff) << bufferedBits;
                bufferedBits += 8;
            }
            int symbol = (int) (buffer & mask);
            buffer >>>= bitsPerValue;
            bufferedBits -= bitsPerValue;
            produced++;
            return unpackSymbol(symbol);
        }
    }

    private static class PackedBitsWriter {
        private final byte[] data;
        private int index;
        private long buffer = 0L;
        private int bufferedBits = 0;

        private PackedBitsWriter(byte[] data, int index) {
            this.data = data;
            this.index = index;
        }

        private void writeRepeated(int value, int count, int bitsPerValue) {
            for (int i = 0; i < count; i++) {
                write(value, bitsPerValue);
            }
        }

        private void write(int value, int bitsPerValue) {
            buffer |= (long) value << bufferedBits;
            bufferedBits += bitsPerValue;
            while (bufferedBits >= 8) {
                data[index++] = (byte) (buffer & 0xff);
                buffer >>>= 8;
                bufferedBits -= 8;
            }
        }

        private void finish() {
            if (bufferedBits > 0) {
                data[index++] = (byte) (buffer & 0xff);
            }
        }
    }

    private static EncodedPayload encodePositions(RLE positions, StatePalette statePalette, BlockPos dimension) {
        ProjectorCompressionCodec selectedCodec = ScannerConfiguration.projectorCompressionCodec.get();
        boolean compareCodecs = ScannerConfiguration.projectorCompressionLogging.get();
        byte[] rle = positions.getData();
        if (rle.length == 0) {
            return new EncodedPayload(PositionCodec.RLE, rle, 0);
        }

        int rawLength = dimension.getX() * dimension.getZ();
        int paletteSize = statePalette == null ? 0 : statePalette.getPalette().size();

        CodecFamilyStats legacy = encodeLegacyFamily(rle);
        CodecFamilyStats packed = null;
        if (compareCodecs || selectedCodec == ProjectorCompressionCodec.PACKED_BITS) {
            packed = encodePackedFamily(rle, rawLength, paletteSize);
        }

        EncodedPayload best = switch (selectedCodec) {
            case LEGACY_RLE -> legacy.best();
            case PACKED_BITS -> packed == null ? legacy.best() : packed.best();
        };
        CompressionLog.record(rawLength, paletteSize, legacy, packed, best);
        return best;
    }

    private static CodecFamilyStats encodeLegacyFamily(byte[] rle) {
        long start = System.nanoTime();
        EncodedPayload best = new EncodedPayload(PositionCodec.RLE, rle, rle.length);

        int deflatedBytes = -1;
        if (rle.length >= COMPRESSION_MIN_BYTES) {
            byte[] packed = compress(rle);
            deflatedBytes = packed.length;
            if (packed.length + COMPRESSION_MIN_GAIN < rle.length) {
                best = pickBest(best, new EncodedPayload(PositionCodec.RLE_DEFLATE, packed, rle.length));
            }
        }
        return new CodecFamilyStats(best, rle.length, deflatedBytes, System.nanoTime() - start, 8);
    }

    private static CodecFamilyStats encodePackedFamily(byte[] rle, int rawLength, int paletteSize) {
        long start = System.nanoTime();
        byte[] packed = packPositions(rle, rawLength, paletteSize);
        EncodedPayload best = new EncodedPayload(PositionCodec.PACKED_BITS, packed, packed.length);

        int deflatedBytes = -1;
        if (packed.length >= COMPRESSION_MIN_BYTES) {
            byte[] deflated = compress(packed);
            deflatedBytes = deflated.length;
            if (deflated.length + COMPRESSION_MIN_GAIN < packed.length) {
                best = pickBest(best, new EncodedPayload(PositionCodec.PACKED_BITS_DEFLATE, deflated, packed.length));
            }
        }
        return new CodecFamilyStats(best, packed.length, deflatedBytes, System.nanoTime() - start, packed[0] & 0xff);
    }

    private static byte[] packPositions(byte[] rle, int rawLength, int paletteSize) {
        int bitsPerValue = bitsRequired(paletteSize + 2);
        byte[] packed = new byte[1 + ((rawLength * bitsPerValue + 7) >> 3)];
        packed[0] = (byte) bitsPerValue;

        PackedBitsWriter writer = new PackedBitsWriter(packed, 1);
        int produced = 0;
        for (int i = 0; i < rle.length; i += 2) {
            if (i + 1 >= rle.length) {
                throw new IllegalStateException("Corrupt RLE plane payload");
            }
            int count = rle[i] & 0xff;
            int symbol = packSymbol(rle[i + 1] & 0xff);
            writer.writeRepeated(symbol, count, bitsPerValue);
            produced += count;
        }
        writer.finish();
        if (produced != rawLength) {
            throw new IllegalStateException("Unexpected packed plane length: got " + produced + ", expected " + rawLength);
        }
        return packed;
    }

    private static int bitsRequired(int symbolCount) {
        if (symbolCount <= 1) {
            return 1;
        }
        return Integer.SIZE - Integer.numberOfLeadingZeros(symbolCount - 1);
    }

    private static int packSymbol(int value) {
        if (value == 255) {
            return 0;
        }
        if (value == 0) {
            return 1;
        }
        return value + 1;
    }

    private static int unpackSymbol(int packedValue) {
        if (packedValue == 0) {
            return 255;
        }
        if (packedValue == 1) {
            return 0;
        }
        return packedValue - 1;
    }

    private static EncodedPayload pickBest(EncodedPayload current, EncodedPayload candidate) {
        return candidate.wireSize() < current.wireSize() ? candidate : current;
    }

    private static byte[] compress(byte[] data) {
        Deflater deflater = new Deflater(COMPRESSION_LEVEL);
        deflater.setInput(data);
        deflater.finish();
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length);
        while (!deflater.finished()) {
            int len = deflater.deflate(buffer);
            out.write(buffer, 0, len);
        }
        deflater.end();
        return out.toByteArray();
    }

    private static byte[] decompress(byte[] payload, int expectedLength) {
        if (expectedLength <= 0) {
            throw new IllegalStateException("Invalid expected decompressed length for shape packet: " + expectedLength);
        }
        Inflater inflater = new Inflater();
        inflater.setInput(payload);
        byte[] buffer = new byte[Math.max(1024, Math.min(65536, expectedLength))];
        ByteArrayOutputStream out = new ByteArrayOutputStream(expectedLength);
        try {
            while (!inflater.finished()) {
                int len = inflater.inflate(buffer);
                if (len == 0) {
                    if (inflater.needsInput()) {
                        break;
                    }
                    if (inflater.needsDictionary()) {
                        throw new IllegalStateException("Unable to decompress shape packet (dictionary required)");
                    }
                } else {
                    out.write(buffer, 0, len);
                }
            }
        } catch (DataFormatException e) {
            throw new IllegalStateException("Unable to decompress shape packet", e);
        } finally {
            inflater.end();
        }
        byte[] data = out.toByteArray();
        if (data.length != expectedLength) {
            throw new IllegalStateException("Unexpected decompressed length for shape packet: got " + data.length + ", expected " + expectedLength);
        }
        return data;
    }
}
