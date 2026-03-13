package mcjty.rftoolsbuilder.shapes;

import mcjty.lib.varia.NBTTools;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class Scan {

    private byte[] rledata;
    private List<BlockState> materialPalette = new ArrayList<>();
    private BlockPos dataDim;
    private BlockPos dataOffset = new BlockPos(0, 0, 0);
    private int dirtyCounter = 0;

    public int dirtyRequestTimeout = 0;   // Client side only

    public static final byte[] EMPTY = new byte[0];

    public byte[] getRledata() {
        if (rledata == null) {
            return EMPTY;
        }
        return rledata;
    }

    public byte[] getDataInt() {
        return rledata;
    }

    public void setData(byte[] data, List<BlockState> materialPalette, BlockPos dim, BlockPos offset) {
        this.rledata = data;
        this.materialPalette = materialPalette;
        this.dataDim = dim;
        this.dataOffset = offset;
        dirtyCounter++;
    }

    public void setDirtyCounter(int dirtyCounter) {
        this.dirtyCounter = dirtyCounter;
    }

    public int getDirtyCounter() {
        return dirtyCounter;
    }

    public List<BlockState> getMaterialPalette() {
        return materialPalette;
    }

    public BlockPos getDataDim() {
        return dataDim;
    }

    public BlockPos getDataOffset() {
        return dataOffset;
    }

    public void writeToNBT(CompoundTag tagCompound) {
        tagCompound.putInt("dirty", dirtyCounter);
    }

    public void writeToNBTExternal(CompoundTag tagCompound) {
        tagCompound.putByteArray("data", rledata == null ? new byte[0] : rledata);
        StatePalette palette = new StatePalette();
        for (BlockState state : materialPalette) {
            palette.add(state);
        }
        tagCompound.put("scanpal", palette.writeToNBT());
        if (dataDim != null) {
            tagCompound.putInt("scandimx", dataDim.getX());
            tagCompound.putInt("scandimy", dataDim.getY());
            tagCompound.putInt("scandimz", dataDim.getZ());
        }
        if (dataOffset != null) {
            tagCompound.putInt("scanoffx", dataOffset.getX());
            tagCompound.putInt("scanoffy", dataOffset.getY());
            tagCompound.putInt("scanoffz", dataOffset.getZ());
        }
    }

    public void readFromNBT(CompoundTag tagCompound) {
        dirtyCounter = tagCompound.getInt("dirty");
    }

    public void readFromNBTExternal(CompoundTag tagCompound) {
        materialPalette.clear();
        ListTag list = tagCompound.getList("scanpal", Tag.TAG_COMPOUND);
        materialPalette.addAll(StatePalette.readFromNBT(list).getPalette());
        rledata = tagCompound.getByteArray("data");
        dataDim = new BlockPos(tagCompound.getInt("scandimx"), tagCompound.getInt("scandimy"), tagCompound.getInt("scandimz"));
        dataOffset = new BlockPos(tagCompound.getInt("scanoffx"), tagCompound.getInt("scanoffy"), tagCompound.getInt("scanoffz"));
    }
}
