package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.lib.varia.CapabilityTools;
import mcjty.lib.varia.InventoryTools;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.StacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public record PacketUpdateCardInInventory(BlockPos pos, int slotIndex, ItemStack stack) implements CustomPacketPayload {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(RFToolsBuilder.MODID, "updatecard_inventory");
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
            Level world = player.level();
            BlockEntity te = world.getBlockEntity(pos);
            if (te != null) {
                if (!isValidBlock(world, pos, te)) {
                    return;
                }
                if (!isValidItem(this.stack)) {
                    return;
                }
                ResourceHandler<ItemResource> h = CapabilityTools.itemHandler(world.getCapability(Capabilities.Item.BLOCK, pos, null));
                if (h instanceof IndexModifier<?> || h instanceof StacksResourceHandler<?, ?>) {
                    var modifiable = InventoryTools.modifier(h);
                    modifiable.set(slotIndex, ItemResource.of(this.stack), this.stack.getCount());
                }
            }
        });
    }
}
