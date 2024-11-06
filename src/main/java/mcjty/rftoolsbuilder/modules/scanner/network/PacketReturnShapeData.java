package mcjty.rftoolsbuilder.modules.scanner.network;

import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.lib.varia.RLE;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.shapes.RenderData;
import mcjty.rftoolsbuilder.shapes.ShapeID;
import mcjty.rftoolsbuilder.shapes.ShapeRenderer;
import mcjty.rftoolsbuilder.shapes.StatePalette;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketReturnShapeData(ShapeID shapeID, RLE positions, StatePalette statePalette, BlockPos dimension,
                                    int count, int offsetY, String msg) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsBuilder.MODID, "returnshapedata");
    public static final CustomPacketPayload.Type<PacketReturnShapeData> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketReturnShapeData> CODEC = CompositeStreamCodec.composite(
            ShapeID.STREAM_CODEC, PacketReturnShapeData::shapeID,
            RLE.OPTIONAL_STREAM_CODEC, PacketReturnShapeData::positions,
            StatePalette.OPTIONAL_STREAM_CODEC, PacketReturnShapeData::statePalette,
            BlockPos.STREAM_CODEC, PacketReturnShapeData::dimension,
            ByteBufCodecs.INT, PacketReturnShapeData::count,
            ByteBufCodecs.INT, PacketReturnShapeData::offsetY,
            ByteBufCodecs.STRING_UTF8, PacketReturnShapeData::msg,
            PacketReturnShapeData::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static PacketReturnShapeData create(ShapeID id, RLE positions, StatePalette statePalette, BlockPos dimension, int count, int offsetY, String msg) {
        return new PacketReturnShapeData(id, positions, statePalette, dimension, count, offsetY, msg);
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            int dx = dimension.getX();
            int dy = dimension.getY();
            int dz = dimension.getZ();

            RLE rle = positions;
            RenderData.RenderPlane plane = null;

            if (rle != null) {
                BlockState dummy = BuilderModule.SUPPORT.get().defaultBlockState();

                rle.reset();
//                for (int oy = 0; oy < dy; oy++) {
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
                                data--;
                                strip.add(statePalette.getPalette().get(data));
                            }
                        } else {
                            strip.add(null);
                        }
                    }

                    strip.close();
                    plane = new RenderData.RenderPlane(strips, y, oy, -dz / 2, count);
                }
            }
            ShapeRenderer.setRenderData(shapeID, plane, offsetY, dy, msg);
        });
    }
}