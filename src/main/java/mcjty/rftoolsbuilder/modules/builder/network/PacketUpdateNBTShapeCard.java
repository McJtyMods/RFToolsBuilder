package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lib.network.TypedMapTools;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * This is a packet that can be used to update the NBT on the held item of a player.
 */
public record PacketUpdateNBTShapeCard(TypedMap args) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsBuilder.MODID, "updatenbtshapecard");

    @Override
    public void write(FriendlyByteBuf buf) {
        TypedMapTools.writeArguments(buf, args);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static PacketUpdateNBTShapeCard create(FriendlyByteBuf buf) {
        return new PacketUpdateNBTShapeCard(TypedMapTools.readArguments(buf));
    }

    public static PacketUpdateNBTShapeCard create(TypedMap arguments) {
        return new PacketUpdateNBTShapeCard(arguments);
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(playerEntity -> {
                ItemStack heldItem = playerEntity.getItemInHand(InteractionHand.MAIN_HAND);
                if (heldItem.isEmpty()) {
                    return;
                }
                CompoundTag tagCompound = heldItem.getTag();
                if (tagCompound == null) {
                    tagCompound = new CompoundTag();
                    heldItem.setTag(tagCompound);
                }
                for (Key<?> akey : args.getKeys()) {
                    String key = akey.name();
                    if (Type.STRING.equals(akey.type())) {
                        tagCompound.putString(key, (String) args.get(akey));
                    } else if (Type.INTEGER.equals(akey.type())) {
                        tagCompound.putInt(key, (Integer) args.get(akey));
                    } else if (Type.DOUBLE.equals(akey.type())) {
                        tagCompound.putDouble(key, (Double) args.get(akey));
                    } else if (Type.BOOLEAN.equals(akey.type())) {
                        tagCompound.putBoolean(key, (Boolean) args.get(akey));
                    } else if (Type.BLOCKPOS.equals(akey.type())) {
                        throw new RuntimeException("BlockPos not supported for PacketUpdateNBTItem!");
                    } else if (Type.ITEMSTACK.equals(akey.type())) {
                        throw new RuntimeException("ItemStack not supported for PacketUpdateNBTItem!");
                    }
                }
            });
        });
    }
}