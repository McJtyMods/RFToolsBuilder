package mcjty.rftoolsbuilder.modules.scanner.network;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lib.varia.RLE;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.shapes.RenderData;
import mcjty.rftoolsbuilder.shapes.ShapeID;
import mcjty.rftoolsbuilder.shapes.ShapeRenderer;
import mcjty.rftoolsbuilder.shapes.StatePalette;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public record PacketReturnShapeData(ShapeID shapeID, RLE positions, StatePalette statePalette, BlockPos dimension,
                                    int count, int offsetY, String msg) implements CustomPacketPayload {

    public static ResourceLocation ID = new ResourceLocation(RFToolsBuilder.MODID, "returnshapedata");

    @Override
    public void write(FriendlyByteBuf buf) {
        shapeID.toBytes(buf);
        buf.writeInt(count);
        buf.writeInt(offsetY);
        NetworkTools.writeStringUTF8(buf, msg);
        buf.writeBlockPos(dimension);

        if (statePalette == null) {
            buf.writeInt(0);
        } else {
            buf.writeInt(statePalette.getPalette().size());
            for (BlockState state : statePalette.getPalette()) {
                BlockState blockState = state;
                if (Tools.getId(blockState) == null) {
                    blockState = Blocks.STONE.defaultBlockState();
                }
                buf.writeUtf(Tools.getId(blockState).toString());
                //                buf.writeInt(state.getBlock().getMetaFromState(state));   // @todo 1.14 persist blockstate here!
            }
        }

        if (positions == null) {
            buf.writeInt(0);
        } else {
            buf.writeInt(positions.getData().length);
            buf.writeBytes(positions.getData());
        }
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static PacketReturnShapeData create(FriendlyByteBuf buf) {
        ShapeID shapeID = new ShapeID(buf);
        int count = buf.readInt();
        int offsetY = buf.readInt();
        String msg = NetworkTools.readStringUTF8(buf);
        BlockPos dimension = buf.readBlockPos();
        StatePalette statePalette;
        RLE positions;

        int size = buf.readInt();
        if (size == 0) {
            statePalette = null;
        } else {
            statePalette = new StatePalette();
            while (size > 0) {
                String r = buf.readUtf(32767);
//                int m = buf.readInt();    // @todo 1.14 no meta!
//                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(r));
                Block block = Tools.getBlock(new ResourceLocation(r));
                statePalette.add(block.defaultBlockState());
                size--;
            }
        }

        size = buf.readInt();
        if (size == 0) {
            positions = null;
        } else {
            positions = new RLE();
            byte[] data = new byte[size];
            buf.readBytes(data);
            positions.setData(data);
        }
        return new PacketReturnShapeData(shapeID, positions, statePalette, dimension, count, offsetY, msg);
    }

    public static PacketReturnShapeData create(ShapeID id, RLE positions, StatePalette statePalette, BlockPos dimension, int count, int offsetY, String msg) {
        return new PacketReturnShapeData(id, positions, statePalette, dimension, count, offsetY, msg);
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
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