package mcjty.rftoolsbuilder.modules.scanner.network;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lib.varia.RLE;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
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

public record PacketReturnShapeData(ShapeID shapeID, int checksum, RLE positions, StatePalette statePalette, BlockPos dimension,
                                    int count, int offsetY, String msg) implements CustomPacketPayload {

    public static ResourceLocation ID = new ResourceLocation(RFToolsBuilder.MODID, "returnshapedata");
    private static final int COMPRESSION_MIN_BYTES = 256;
    private static final int COMPRESSION_MIN_GAIN = 32;
    private static final int COMPRESSION_LEVEL = Deflater.BEST_COMPRESSION;

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

        if (positions == null) {
            buf.writeBoolean(false);
        } else {
            byte[] raw = positions.getData();
            if (raw.length == 0) {
                buf.writeBoolean(false);
                return;
            }
            buf.writeBoolean(true);
            byte[] payload = raw;
            boolean compressed = false;
            if (raw.length >= COMPRESSION_MIN_BYTES) {
                byte[] packed = compress(raw);
                if (packed.length + COMPRESSION_MIN_GAIN < raw.length) {
                    payload = packed;
                    compressed = true;
                }
            }
            buf.writeBoolean(compressed);
            if (compressed) {
                buf.writeVarInt(raw.length);
            }
            buf.writeVarInt(payload.length);
            buf.writeBytes(payload);
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
        RLE positions;

        if (!buf.readBoolean()) {
            statePalette = null;
        } else {
            statePalette = StatePalette.readFromBuf(buf);
        }

        if (!buf.readBoolean()) {
            positions = null;
        } else {
            boolean compressed = buf.readBoolean();
            int rawLength = compressed ? buf.readVarInt() : -1;
            int size = buf.readVarInt();
            positions = new RLE();
            byte[] payload = new byte[size];
            buf.readBytes(payload);
            positions.setData(compressed ? decompress(payload, rawLength) : payload);
        }
        return new PacketReturnShapeData(shapeID, checksum, positions, statePalette, dimension, count, offsetY, msg);
    }

    public static PacketReturnShapeData create(ShapeID id, int checksum, RLE positions, StatePalette statePalette, BlockPos dimension, int count, int offsetY, String msg) {
        return new PacketReturnShapeData(id, checksum, positions, statePalette, dimension, count, offsetY, msg);
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

        RLE rle = positions;
        RenderData.RenderPlane plane = null;

        if (rle != null) {
            BlockState dummy = BuilderModule.SUPPORT.get().defaultBlockState();
            List<BlockState> palette = statePalette == null ? List.of() : statePalette.getPalette();

            rle.reset();
            int oy = offsetY;
            int y = oy - dy / 2;

            RenderData.RenderStrip[] strips = new RenderData.RenderStrip[dx];
            for (int ox = 0; ox < dx; ox++) {
                int x = ox - dx / 2;

                RenderData.RenderStrip strip = new RenderData.RenderStrip(x);
                strips[ox] = strip;

                for (int oz = 0; oz < dz; oz++) {
                    int data = rle.read();
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
