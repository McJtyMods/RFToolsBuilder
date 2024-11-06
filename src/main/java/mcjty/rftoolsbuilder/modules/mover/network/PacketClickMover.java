package mcjty.rftoolsbuilder.modules.mover.network;

import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet from client to sent to indicate that the player clicked on a mover button inside the platform
 */
public record PacketClickMover(BlockPos pos, String mover) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsBuilder.MODID, "click_mover");
    public static final CustomPacketPayload.Type<PacketClickMover> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacketClickMover> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketClickMover::pos,
            ByteBufCodecs.STRING_UTF8, PacketClickMover::mover,
            PacketClickMover::create);

    public static PacketClickMover create(BlockPos worldPosition, String highlightedMover) {
        return new PacketClickMover(worldPosition, highlightedMover);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (player.level().getBlockEntity(pos) instanceof MoverTileEntity mover) {
                mover.startMove(this.mover);
            }
        });
    }
}
