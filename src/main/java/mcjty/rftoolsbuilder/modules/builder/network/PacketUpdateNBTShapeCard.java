package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * This is a packet that can be used to update the NBT on the held item of a player.
 */
public record PacketUpdateNBTShapeCard(TypedMap args) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsBuilder.MODID, "updatenbtshapecard");
    public static final CustomPacketPayload.Type<PacketUpdateNBTShapeCard> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketUpdateNBTShapeCard> CODEC = StreamCodec.composite(
            TypedMap.STREAM_CODEC, PacketUpdateNBTShapeCard::args,
            PacketUpdateNBTShapeCard::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static PacketUpdateNBTShapeCard create(TypedMap arguments) {
        return new PacketUpdateNBTShapeCard(arguments);
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player playerEntity = ctx.player();
            ItemStack heldItem = playerEntity.getItemInHand(InteractionHand.MAIN_HAND);
            if (heldItem.isEmpty()) {
                return;
            }
            // @todo 1.21 NBT
//            CompoundTag tagCompound = heldItem.getTag();
//            if (tagCompound == null) {
//                tagCompound = new CompoundTag();
//                heldItem.setTag(tagCompound);
//            }
//            for (Key<?> akey : args.getKeys()) {
//                String key = akey.name();
//                if (Type.STRING.equals(akey.type())) {
//                    tagCompound.putString(key, (String) args.get(akey));
//                } else if (Type.INTEGER.equals(akey.type())) {
//                    tagCompound.putInt(key, (Integer) args.get(akey));
//                } else if (Type.DOUBLE.equals(akey.type())) {
//                    tagCompound.putDouble(key, (Double) args.get(akey));
//                } else if (Type.BOOLEAN.equals(akey.type())) {
//                    tagCompound.putBoolean(key, (Boolean) args.get(akey));
//                } else if (Type.BLOCKPOS.equals(akey.type())) {
//                    throw new RuntimeException("BlockPos not supported for PacketUpdateNBTItem!");
//                } else if (Type.ITEMSTACK.equals(akey.type())) {
//                    throw new RuntimeException("ItemStack not supported for PacketUpdateNBTItem!");
//                }
//            }
        });
    }
}