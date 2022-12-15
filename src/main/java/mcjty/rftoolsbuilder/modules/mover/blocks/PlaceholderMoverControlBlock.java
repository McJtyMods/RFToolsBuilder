package mcjty.rftoolsbuilder.modules.mover.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;

import static mcjty.lib.builder.TooltipBuilder.*;

public class PlaceholderMoverControlBlock extends BaseBlock {

    public PlaceholderMoverControlBlock() {
        super(new BlockBuilder()
                .topDriver(RFToolsBuilderTOPDriver.DRIVER)
                .info(key("message.rftoolsbuilder.shiftmessage"))
                .infoShift(header(), gold()));
    }
}
