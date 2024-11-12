package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketUpdateCardInInventory(BlockPos pos, int slotIndex, ItemStack stack) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsBuilder.MODID, "updatecard_inventory");
    public static final CustomPacketPayload.Type<PacketUpdateCardInInventory> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketUpdateCardInInventory> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketUpdateCardInInventory::pos,
            ByteBufCodecs.INT, PacketUpdateCardInInventory::slotIndex,
            ItemStack.STREAM_CODEC, PacketUpdateCardInInventory::stack,
            PacketUpdateCardInInventory::new);

    public static PacketUpdateCardInInventory create(BlockPos pos, int slotIndex, ItemStack stack) {
        return new PacketUpdateCardInInventory(pos, slotIndex, stack);
    }

    private boolean isValidBlock(Level world, BlockPos blockPos, BlockEntity tileEntity) {
        return /* @todo 1.14 tileEntity instanceof ComposerTileEntity || */tileEntity instanceof BuilderTileEntity;
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
            Level world = player.getCommandSenderWorld();
            BlockEntity te = world.getBlockEntity(pos);
            if (te != null) {
                if (!isValidBlock(world, pos, te)) {
                    return;
                }
                if (!isValidItem(this.stack)) {
                    return;
                }
                IItemHandler h = world.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
                if (h instanceof IItemHandlerModifiable modifiable) {
                    modifiable.setStackInSlot(slotIndex, this.stack);
                }
            }
        });
    }
}
