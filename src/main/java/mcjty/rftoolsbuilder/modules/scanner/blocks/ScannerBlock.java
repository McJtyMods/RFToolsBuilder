package mcjty.rftoolsbuilder.modules.scanner.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import static mcjty.lib.builder.TooltipBuilder.gold;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class ScannerBlock extends BaseBlock {

    public ScannerBlock() {
        super(new BlockBuilder()
                .tileEntitySupplier(ScannerTileEntity::new)
                .topDriver(RFToolsBuilderTOPDriver.DRIVER)
                .infusable()
                .manualEntry(ManualHelper.create("rftoolsbuilder:projector/scanner"))
                .info(key("message.rftoolsbuilder.shiftmessage"))
                .infoShift(header(), gold()));
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.HORIZROTATION;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ScannerTileEntity) {
                blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
                    for (int i = 0; i < h.getSlots(); i++) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), h.getStackInSlot(i));
                    }
                });
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
