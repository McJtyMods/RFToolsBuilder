package mcjty.rftoolsbuilder.modules.shield.data;

import mcjty.lib.varia.RLE;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

// A 16x16x16 chunk
public class ShieldChunkInfo {
    // Every index in chunkMask is an index in a table of shield projectors.
    // So if you have a shielding block at a certain position of this sub-chunk
    // you can find the position of the corresponding shield projector using that index
    private byte[] chunkMask = new byte[16*16*16];

    // Shield projectors that have an effect on shielding blocks in this
    // sub-chunk. These projectors can themselves be in another sub-chunk
    private List<BlockPos> shieldProjectors = new ArrayList<>();

    public ShieldChunkInfo() {
        for (int i = 0 ; i < chunkMask.length ; i++) {
            chunkMask[i] = -1;
        }
    }

    // Return the data of this subchunk as an RLE
    public RLE createDataRLE() {
        RLE data = new RLE();
        for (byte b : chunkMask) {
            data.add(b);
        }
        return data;
    }

    public List<BlockPos> getShieldProjectors() {
        return shieldProjectors;
    }

    // Return true if the entire mask is empty (everything set to -1)
    public boolean isEmpty() {
        for (byte b : chunkMask) {
            if (b != -1) {
                return false;
            }
        }
        return true;
    }

    // Call this when an individual shielding block is decomposed
    public void decomposeShield(BlockPos shieldPos, BlockPos shieldingPos) {
        int index = findShieldProjector(shieldPos);
        if (index != -1) {
            int idx = getShieldingBlockIndex(shieldingPos);
            chunkMask[idx] = -1;
        }
    }

    // Call this when a shield is composed for a giving shielding block
    public void composeShield(BlockPos shieldPos, BlockPos shieldingPos) {
        // Find a free spot for our shield projector
        int index = findShieldProjector(shieldPos);
        if (index == -1) {
            index = allocateShieldProjector(shieldPos);
        }
        int idx = getShieldingBlockIndex(shieldingPos);
        chunkMask[idx] = (byte) index;
    }

    // Get the position of the shield projector given a shielding block
    @Nullable
    public BlockPos getShieldProjector(BlockPos shieldingPos) {
        int idx = getShieldingBlockIndex(shieldingPos);
        byte index = chunkMask[idx];
        if (index == -1) {
            return null;
        }
        return shieldProjectors.get(index & 0xff);
    }

    public void read(CompoundNBT tag) {
        chunkMask = tag.getByteArray("mask");
        shieldProjectors.clear();
        ListNBT list = tag.getList("shield", Constants.NBT.TAG_INT_ARRAY);
        for (INBT nbt : list) {
            IntArrayNBT posArray = (IntArrayNBT) nbt;
            shieldProjectors.add(new BlockPos(posArray.get(0).getInt(), posArray.get(1).getInt(), posArray.get(2).getInt()));
        }
    }

    public CompoundNBT write() {
        CompoundNBT tag = new CompoundNBT();
        tag.putByteArray("mask", chunkMask);
        ListNBT list = new ListNBT();
        for (BlockPos pos : shieldProjectors) {
            IntArrayNBT posArray = new IntArrayNBT(new int[] { pos.getX(), pos.getY(), pos.getZ() });
            list.add(posArray);
        }
        tag.put("shields", list);
        return tag;
    }


    private void removeShieldFromMask(int index) {
        for (int i = 0 ; i < 16*16*16 ; i++) {
            if ((chunkMask[i] & 0xff) == index) {
                chunkMask[i] = -1;
            }
        }
    }

    // Return the index of the shielding block
    private int getShieldingBlockIndex(BlockPos pos) {
        return ((pos.getX() & 0xf) << 8) + ((pos.getY() & 0xf) << 4) + (pos.getZ() & 0xf);
    }

    private int allocateShieldProjector(BlockPos shieldPos) {
        for (int i = 0 ; i < shieldProjectors.size() ; i++) {
            BlockPos pos = shieldProjectors.get(i);
            if (pos == null) {
                shieldProjectors.set(i, shieldPos);
                return i;
            }
        }
        // We need to grow our array
        shieldProjectors.add(shieldPos);
        return shieldProjectors.size();
    }

    // Returns -1 if the shield projector could not be found yet
    private int findShieldProjector(BlockPos shieldPos) {
        for (int i = 0 ; i < shieldProjectors.size() ; i++) {
            BlockPos pos = shieldProjectors.get(i);
            if (pos != null) {
                if (shieldPos.equals(pos)) {
                    return i;
                }
            }
        }
        return -1;
    }
}
