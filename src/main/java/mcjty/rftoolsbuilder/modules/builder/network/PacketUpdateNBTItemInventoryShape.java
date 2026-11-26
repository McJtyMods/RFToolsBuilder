package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ComposerTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public record PacketUpdateNBTItemInventoryShape(BlockPos pos, int slotIndex, CompoundTag tagCompound) implements CustomPacketPayload {

    private static final List<String> EDITABLE_TAGS = List.of(
            "shape", "solid", "dimX", "dimY", "dimZ", "offsetX", "offsetY", "offsetZ",
            "voidstone", "voidcobble", "voiddirt", "voidgravel", "voidsand", "voidnetherrack", "voidendstone", "tagMatching");

    public static ResourceLocation ID = new ResourceLocation(RFToolsBuilder.MODID, "updatenbtiteminventoryshape");

    public static PacketUpdateNBTItemInventoryShape create(FriendlyByteBuf buf) {
        return new PacketUpdateNBTItemInventoryShape(buf.readBlockPos(), buf.readInt(), buf.readNbt());
    }

    public static PacketUpdateNBTItemInventoryShape create(BlockPos pos, int slotIndex, CompoundTag tagCompound) {
        return new PacketUpdateNBTItemInventoryShape(pos, slotIndex, tagCompound);
    }

    private GenericItemHandler getCardInventory(GenericTileEntity tile) {
        if (tile instanceof BuilderTileEntity builder && slotIndex == BuilderTileEntity.SLOT_TAB) {
            return builder.getItems();
        }
        if (tile instanceof ComposerTileEntity composer && (slotIndex == ComposerTileEntity.SLOT_OUT
                || slotIndex >= ComposerTileEntity.SLOT_TABS && slotIndex < ComposerTileEntity.SLOT_GHOSTS)) {
            return composer.getItems();
        }
        return null;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(slotIndex);
        buf.writeNbt(tagCompound);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> ctx.player().ifPresent(player -> {
            Level world = player.getCommandSenderWorld();
            if (!world.hasChunkAt(pos)
                    || !(world.getBlockEntity(pos) instanceof GenericTileEntity tile)
                    || !tile.canPlayerAccess(player)) {
                return;
            }
            GenericItemHandler items = getCardInventory(tile);
            if (items == null || tagCompound == null) {
                return;
            }

            ItemStack current = items.getStackInSlot(slotIndex);
            if (!(current.getItem() instanceof ShapeCardItem)) {
                return;
            }
            ItemStack updated = current.copy();
            CompoundTag tag = updated.getOrCreateTag();
            for (String key : EDITABLE_TAGS) {
                Tag value = tagCompound.get(key);
                if (value != null) {
                    tag.put(key, value.copy());
                }
            }
            if (tile instanceof BuilderTileEntity builder) {
                builder.applyShapeCardEdit(updated);
            } else {
                items.setStackInSlot(slotIndex, updated);
            }
        }));
    }
}
