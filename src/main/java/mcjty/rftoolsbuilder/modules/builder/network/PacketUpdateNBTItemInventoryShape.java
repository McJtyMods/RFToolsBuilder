package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public record PacketUpdateNBTItemInventoryShape(BlockPos pos, int slotIndex, CompoundTag tagCompound) implements CustomPacketPayload {

    public static ResourceLocation ID = new ResourceLocation(RFToolsBuilder.MODID, "updatenbtiteminventoryshape");

    public static PacketUpdateNBTItemInventoryShape create(FriendlyByteBuf buf) {
        return new PacketUpdateNBTItemInventoryShape(buf.readBlockPos(), buf.readInt(), buf.readNbt());
    }

    public static PacketUpdateNBTItemInventoryShape create(BlockPos pos, int slotIndex, CompoundTag tagCompound) {
        return new PacketUpdateNBTItemInventoryShape(pos, slotIndex, tagCompound);
    }

    protected boolean isValidBlock(Level world, BlockPos blockPos, BlockEntity tileEntity) {
        return /* @todo 1.14 tileEntity instanceof ComposerTileEntity || */tileEntity instanceof BuilderTileEntity;
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
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(player -> {
                Level world = player.getCommandSenderWorld();
                BlockEntity te = world.getBlockEntity(pos);
                if (te != null) {
                    if (!isValidBlock(world, pos, te)) {
                        return;
                    }
                    te.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
                        ItemStack stack = h.getStackInSlot(slotIndex);
                        if (!stack.isEmpty()) {
                            stack.setTag(tagCompound);
                        }
                    });
                }
            });
        });
    }
}
