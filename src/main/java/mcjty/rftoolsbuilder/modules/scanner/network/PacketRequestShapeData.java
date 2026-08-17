package mcjty.rftoolsbuilder.modules.scanner.network;

import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.shapes.IFormula;
import mcjty.rftoolsbuilder.shapes.Shape;
import mcjty.rftoolsbuilder.shapes.ShapeDataManagerServer;
import mcjty.rftoolsbuilder.shapes.ShapeID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketRequestShapeData(ItemStack card, ShapeID shapeID, int checksum) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsBuilder.MODID, "requestshapedata");
    public static final CustomPacketPayload.Type<PacketRequestShapeData> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketRequestShapeData> CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, PacketRequestShapeData::card,
            ShapeID.STREAM_CODEC, PacketRequestShapeData::shapeID,
            ByteBufCodecs.INT, PacketRequestShapeData::checksum,
            PacketRequestShapeData::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static PacketRequestShapeData create(ItemStack card, ShapeID id, int checksum) {
        return new PacketRequestShapeData(card, id, checksum);
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            Shape shape = ShapeCardItem.getShape(card);
            boolean shapeSolid = ShapeCardItem.isSolid(card);
            boolean optimizeRenderShell = shapeID.isSolid();
            BlockPos dimension = ShapeCardItem.getDimension(card);

            BlockPos clamped = new BlockPos(Math.min(dimension.getX(), 512), Math.min(dimension.getY(), 4096), Math.min(dimension.getZ(), 512));
            int dy = clamped.getY();
            ItemStack copy = card.copy();

            IFormula formula = shape.getFormulaFactory().get();
            formula = formula.correctFormula(shapeSolid);
            formula.setup(player.level(), new BlockPos(0, 0, 0), clamped, new BlockPos(0, 0, 0), copy);

            for (int y = 0; y < dy; y++) {
                ShapeDataManagerServer.pushWork(shapeID, copy, y, formula, optimizeRenderShell, checksum, (ServerPlayer) player);
            }
        });
    }
}
