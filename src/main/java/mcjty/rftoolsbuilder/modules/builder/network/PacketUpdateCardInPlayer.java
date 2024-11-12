package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketUpdateCardInPlayer(ItemStack stack) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsBuilder.MODID, "updatecard_player");
    public static final Type<PacketUpdateCardInPlayer> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketUpdateCardInPlayer> CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, PacketUpdateCardInPlayer::stack,
            PacketUpdateCardInPlayer::new);

    public static PacketUpdateCardInPlayer create(ItemStack stack) {
        return new PacketUpdateCardInPlayer(stack);
    }

    private boolean isValidItem(ItemStack stack) {
        return stack.getItem() instanceof ShapeCardItem;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (heldItem.isEmpty()) {
                return;
            }
            // To avoid people messing with packets
            if (isValidItem(heldItem) && isValidItem(this.stack)) {
                player.setItemInHand(InteractionHand.MAIN_HAND, this.stack);
            }
        });
    }
}
