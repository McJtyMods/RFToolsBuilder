package mcjty.rftoolsbuilder.modules.mover.network;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Packet from client to sent to indicate that the player clicked on a mover button inside the platform
 */
public record PacketClickMover(BlockPos pos, String mover) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsBuilder.MODID, "click_mover");

    public static PacketClickMover create(FriendlyByteBuf buf) {
        return new PacketClickMover(buf.readBlockPos(), buf.readUtf());
    }

    public static PacketClickMover create(BlockPos worldPosition, String highlightedMover) {
        return new PacketClickMover(worldPosition, highlightedMover);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(mover);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(player -> {
                if (player.level().getBlockEntity(pos) instanceof MoverTileEntity mover) {
                    mover.startMove(this.mover);
                }
            });
        });
    }
}
