package mcjty.rftoolsbuilder.shapes;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

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

    public void writeToNBT(CompoundNBT tagCompound) {
        tagCompound.putInt("dirty", dirtyCounter);
    }

    public void writeToNBTExternal(CompoundNBT tagCompound) {
        tagCompound.putByteArray("data", rledata == null ? new byte[0] : rledata);
        ListNBT pal = new ListNBT();
        for (BlockState state : materialPalette) {
            CompoundNBT tc = NBTUtil.writeBlockState(state);
            pal.add(tc);
        }
        tagCompound.put("scanpal", pal);
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

    public void readFromNBT(CompoundNBT tagCompound) {
        dirtyCounter = tagCompound.getInt("dirty");
    }

    public void readFromNBTExternal(CompoundNBT tagCompound) {
        ListNBT list = tagCompound.getList("scanpal", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT tc = list.getCompound(i);
            BlockState state = NBTUtil.readBlockState(tc);
            materialPalette.add(state);
        }
        rledata = tagCompound.getByteArray("data");
        dataDim = new BlockPos(tagCompound.getInt("scandimx"), tagCompound.getInt("scandimy"), tagCompound.getInt("scandimz"));
        dataOffset = new BlockPos(tagCompound.getInt("scanoffx"), tagCompound.getInt("scanoffy"), tagCompound.getInt("scanoffz"));
    }
}
