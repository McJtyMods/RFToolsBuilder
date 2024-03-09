package mcjty.rftoolsbuilder.modules.scanner.network;

import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.shapes.IFormula;
import mcjty.rftoolsbuilder.shapes.Shape;
import mcjty.rftoolsbuilder.shapes.ShapeDataManagerServer;
import mcjty.rftoolsbuilder.shapes.ShapeID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record PacketRequestShapeData(ItemStack card, ShapeID shapeID) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsBuilder.MODID, "requestshapedata");

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeItem(card);
        shapeID.toBytes(buf);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static PacketRequestShapeData create(FriendlyByteBuf buf) {
        return new PacketRequestShapeData(buf.readItem(), new ShapeID(buf));
    }

    public static PacketRequestShapeData create(ItemStack card, ShapeID id) {
        return new PacketRequestShapeData(card, id);
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(player -> {
                Shape shape = ShapeCardItem.getShape(card);
                boolean solid = ShapeCardItem.isSolid(card);
                BlockPos dimension = ShapeCardItem.getDimension(card);

                BlockPos clamped = new BlockPos(Math.min(dimension.getX(), 512), Math.min(dimension.getY(), 256), Math.min(dimension.getZ(), 512));
                int dy = clamped.getY();
                ItemStack copy = card.copy();

                IFormula formula = shape.getFormulaFactory().get();
                formula = formula.correctFormula(solid);
                formula.setup(player.level(), new BlockPos(0, 0, 0), clamped, new BlockPos(0, 0, 0), copy.getTag());

                for (int y = 0; y < dy; y++) {
                    ShapeDataManagerServer.pushWork(shapeID, copy, y, formula, (ServerPlayer) player);
                }
            });
        });
    }
}
