package mcjty.rftoolsbuilder.modules.mover.blocks;

import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class InvisibleMoverControlBE extends BlockEntity {

    public InvisibleMoverControlBE(BlockPos pos, BlockState state) {
        super(MoverModule.TYPE_INVISIBLE_MOVER_CONTROL.get(), pos, state);
    }
}
