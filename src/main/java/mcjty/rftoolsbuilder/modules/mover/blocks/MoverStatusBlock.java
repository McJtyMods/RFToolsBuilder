package mcjty.rftoolsbuilder.modules.mover.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import javax.annotation.Nullable;

import static mcjty.lib.builder.TooltipBuilder.*;
import static mcjty.rftoolsbuilder.modules.mover.blocks.MoverControlBlock.HORIZ_FACING;

public class MoverStatusBlock extends BaseBlock {

    public MoverStatusBlock() {
        super(new BlockBuilder()
                .topDriver(RFToolsBuilderTOPDriver.DRIVER)
                .info(key("message.rftoolsbuilder.shiftmessage"))
                .infoShift(header(), gold()));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(HORIZ_FACING, context.getPlayer().getDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HORIZ_FACING);
    }
}