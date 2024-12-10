package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketCloseContainerAndOpenCardGui(BlockPos builderPos) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsBuilder.MODID, "closecontainerandopencardgui");
    public static final CustomPacketPayload.Type<PacketCloseContainerAndOpenCardGui> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacketCloseContainerAndOpenCardGui> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketCloseContainerAndOpenCardGui::builderPos,
            PacketCloseContainerAndOpenCardGui::create);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static PacketCloseContainerAndOpenCardGui create(BlockPos pos) {
        return new PacketCloseContainerAndOpenCardGui(pos);
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            ((ServerPlayer) player).doCloseContainer();
            RFToolsBuilderMessages.sendToPlayer(PacketOpenCardGuiFromBuilder.INSTANCE, player);
            BlockEntity te = player.level().getBlockEntity(builderPos);
            if (te instanceof BuilderTileEntity builderTileEntity) {
                builderTileEntity.setSupportMode(false);
            }
        });
    }
}
