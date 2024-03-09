package mcjty.rftoolsbuilder.modules.shield.network;

import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldProjectorTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record PacketNotifyServerClientReady(BlockPos pos) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsBuilder.MODID, "notify_server_client_ready");

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static PacketNotifyServerClientReady create(FriendlyByteBuf buf) {
        return new PacketNotifyServerClientReady(buf.readBlockPos());
    }

    public static PacketNotifyServerClientReady create(BlockPos worldPosition) {
        return new PacketNotifyServerClientReady(worldPosition);
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(player -> {
                if (player.level().getBlockEntity(pos) instanceof ShieldProjectorTileEntity projector) {
                    projector.clientIsReady();
                }
            });
        });
    }
}
