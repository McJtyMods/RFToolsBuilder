package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.network.PayloadContext;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.data.ShapeCardData;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ComposerTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(PayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            Level world = player.level();
            if (!world.hasChunkAt(pos)
                    || !(world.getBlockEntity(pos) instanceof GenericTileEntity tile)
                    || !tile.canPlayerAccess(player)) {
                return;
            }
            GenericItemHandler items = getCardInventory(tile);
            if (items == null || !(stack.getItem() instanceof ShapeCardItem)) {
                return;
            }

            ItemStack current = items.getStackInSlot(slotIndex);
            if (!current.is(stack.getItem())) {
                return;
            }
            var component = BuilderModule.ITEM_SHAPECARD_DATA.get();
            ShapeCardData edited = stack.getOrDefault(component, ShapeCardData.DEFAULT);
            ShapeCardData data = current.getOrDefault(component, ShapeCardData.DEFAULT);
            ItemStack updated = current.copy();
            updated.set(component, data.withShape(edited.shape())
                    .withSolid(edited.solid())
                    .withDimension(edited.dimensions().dimension())
                    .withOffset(edited.dimensions().offset())
                    .withVoiding(edited.voiding())
                    .withTagMatching(edited.tagMatching()));
            if (tile instanceof BuilderTileEntity builder) {
                builder.applyShapeCardEdit(updated);
            } else {
                items.setStackInSlot(slotIndex, updated);
            }
        });
    }
}
