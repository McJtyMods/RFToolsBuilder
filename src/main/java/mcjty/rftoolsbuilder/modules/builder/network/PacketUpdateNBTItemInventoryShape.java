package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketUpdateNBTItemInventoryShape(BlockPos pos, int slotIndex, CompoundTag tagCompound) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsBuilder.MODID, "updatenbtiteminventoryshape");
    public static final CustomPacketPayload.Type<PacketUpdateNBTItemInventoryShape> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacketUpdateNBTItemInventoryShape> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketUpdateNBTItemInventoryShape::pos,
            ByteBufCodecs.INT, PacketUpdateNBTItemInventoryShape::slotIndex,
            ByteBufCodecs.COMPOUND_TAG, PacketUpdateNBTItemInventoryShape::tagCompound,
            PacketUpdateNBTItemInventoryShape::new);

    public static PacketUpdateNBTItemInventoryShape create(BlockPos pos, int slotIndex, CompoundTag tagCompound) {
        return new PacketUpdateNBTItemInventoryShape(pos, slotIndex, tagCompound);
    }

    protected boolean isValidBlock(Level world, BlockPos blockPos, BlockEntity tileEntity) {
        return /* @todo 1.14 tileEntity instanceof ComposerTileEntity || */tileEntity instanceof BuilderTileEntity;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            Level world = player.getCommandSenderWorld();
            BlockEntity te = world.getBlockEntity(pos);
            if (te != null) {
                if (!isValidBlock(world, pos, te)) {
                    return;
                }
                IItemHandler h = world.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
                if (h != null) {
                    ItemStack stack = h.getStackInSlot(slotIndex);
                    if (!stack.isEmpty()) {
                        // @todo 1.21 NBT
//                        stack.setTag(tagCompound);
                    }
                }
            }
        });
    }
}
