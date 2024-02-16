package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public record PacketCloseContainerAndOpenCardGui(BlockPos builderPos) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsBuilder.MODID, "closecontainerandopencardgui");

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(builderPos);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static PacketCloseContainerAndOpenCardGui create(BlockPos pos) {
        return new PacketCloseContainerAndOpenCardGui(pos);
    }

    public static PacketCloseContainerAndOpenCardGui create(FriendlyByteBuf buf) {
        return new PacketCloseContainerAndOpenCardGui(buf.readBlockPos());
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(player -> {
                ((ServerPlayer)player).doCloseContainer();
                RFToolsBuilderMessages.sendToPlayer(new PacketOpenCardGuiFromBuilder(), player);
                BlockEntity te = player.level().getBlockEntity(builderPos);
                if (te instanceof BuilderTileEntity builderTileEntity) {
                    builderTileEntity.setSupportMode(false);
                }
            });
        });
    }
}
