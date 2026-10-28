package mcjty.rftoolsbuilder.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Blocks;
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
        ListTag pal = new ListTag();
        for (BlockState state : materialPalette) {
            CompoundTag tc = NbtUtils.writeBlockState(state);
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

    public void readFromNBT(CompoundTag tagCompound) {
        dirtyCounter = tagCompound.getIntOr("dirty", 0);
    }

    public void readFromNBTExternal(CompoundTag tagCompound, HolderLookup.Provider provider) {
        ListTag list = tagCompound.getListOrEmpty("scanpal");
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tc = list.getCompoundOrEmpty(i);
            BlockState state = NbtUtils.readBlockState(provider.lookupOrThrow(Registries.BLOCK), tc);
            if (state == null) {
                state = Blocks.STONE.defaultBlockState();
            }
            materialPalette.add(state);
        }
        rledata = tagCompound.getByteArray("data").orElseGet(() -> new byte[0]);
        dataDim = new BlockPos(tagCompound.getIntOr("scandimx", 0), tagCompound.getIntOr("scandimy", 0), tagCompound.getIntOr("scandimz", 0));
        dataOffset = new BlockPos(tagCompound.getIntOr("scanoffx", 0), tagCompound.getIntOr("scanoffy", 0), tagCompound.getIntOr("scanoffz", 0));
    }
}
