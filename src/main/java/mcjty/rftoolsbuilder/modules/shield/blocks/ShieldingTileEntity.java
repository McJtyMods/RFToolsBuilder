package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.rftoolsbuilder.modules.shield.ShieldSetup;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class ShieldingTileEntity extends TileEntity {

    private BlockPos shieldProjector;
    private BlockState camo;

    public ShieldingTileEntity() {
        super(ShieldSetup.TYPE_SHIELDING.get());
    }

    public BlockPos getShieldProjector() {
        return shieldProjector;
    }

    public void setShieldProjector(BlockPos shieldProjector) {
        this.shieldProjector = shieldProjector;
        markDirty();
    }

    public BlockState getCamo() {
        return camo;
    }

    public void setCamo(BlockState camo) {
        this.camo = camo;
        markDirty();
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        shieldProjector = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        if (tag.contains("camo")) {
            camo = NBTUtil.readBlockState(tag.getCompound("camo"));
        } else {
            camo = null;
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag.putInt("x", shieldProjector.getX());
        tag.putInt("y", shieldProjector.getY());
        tag.putInt("z", shieldProjector.getZ());
        if (camo != null) {
            CompoundNBT camoNbt = NBTUtil.writeBlockState(camo);
            tag.put("camo", camoNbt);
        }
        return super.write(tag);
    }
}
