package mcjty.rftoolsbuilder.modules.scanner.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static mcjty.lib.builder.TooltipBuilder.*;

public class ProjectorTileEntity extends TickingTileEntity {

    public ProjectorTileEntity(BlockEntityType type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setRSMode(RedstoneMode.REDSTONE_ONREQUIRED);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
//                .tileEntitySupplier(ProjectorTileEntity::new)
                .topDriver(RFToolsBuilderTOPDriver.DRIVER)
                .infusable()
                .manualEntry(ManualHelper.create("rftoolsbuilder:projector/projector_intro"))
                .info(key("message.rftoolsbuilder.shiftmessage"))
                .infoShift(header(), gold())) {
            @Override
            public RotationType getRotationType() {
                return RotationType.HORIZROTATION;
            }
        };
    }
}
