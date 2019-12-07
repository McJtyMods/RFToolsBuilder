package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.lib.varia.BlockPosTools;
import mcjty.rftoolsbuilder.modules.shield.ShieldSetup;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class BaseShieldTileEntity extends TileEntity {

    // Coordinate of the shield block.
    private BlockPos shieldBlock;

    public BaseShieldTileEntity() {
        super(ShieldSetup.TYPE_SHIELD_BASE.get());
    }

    public BlockPos getShieldBlock() {
        return shieldBlock;
    }

    public void setShieldBlock(BlockPos shieldBlock) {
        this.shieldBlock = shieldBlock;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        shieldBlock = new BlockPos(compound.getInt("sx"), compound.getInt("sy"), compound.getInt("sz"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt("sx", shieldBlock.getX());
        compound.putInt("sy", shieldBlock.getY());
        compound.putInt("sz", shieldBlock.getZ());
        return super.write(compound);
    }
}
