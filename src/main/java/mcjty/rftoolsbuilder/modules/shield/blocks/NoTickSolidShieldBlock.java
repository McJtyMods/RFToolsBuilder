package mcjty.rftoolsbuilder.modules.shield.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class NoTickSolidShieldBlock extends SolidShieldBlock {

    public NoTickSolidShieldBlock(boolean opaque) {
        super(opaque);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new NoTickShieldSolidBlockTileEntity();
    }
}
