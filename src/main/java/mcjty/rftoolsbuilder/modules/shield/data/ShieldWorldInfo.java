package mcjty.rftoolsbuilder.modules.shield.data;

import mcjty.lib.worlddata.AbstractLocalWorldData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Keeps track of where shield blocks are so that a shield block can
 * find the shield projector
 */
public class ShieldWorldInfo extends AbstractLocalWorldData<ShieldWorldInfo> {

    private static final String NAME = "XNetBlobData";

    private Map<SubChunkIndex, ShieldChunkInfo> shieldData = new HashMap<>();


    public ShieldWorldInfo(String name) {
        super(name);
    }

    @Nonnull
    public static ShieldWorldInfo get(World world) {
        return getData(world, () -> new ShieldWorldInfo(NAME), NAME);
    }

    // Call this when an individual shielding block is decomposed
    public void decomposeShield(BlockPos shieldPos, BlockPos shieldingPos) {
        SubChunkIndex index = calculateSubChunkIndex(shieldingPos);
        ShieldChunkInfo info = shieldData.get(index);
        if (info != null) {
            info.decomposeShield(shieldPos, shieldingPos);
        }

        cleanUpArea(index);
        markDirty();
    }

    private void cleanUpArea(SubChunkIndex index) {
        // We check an area of subchunks around the shield projector to see if we can get
        // rid of subchunks
        int range = 4;
        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    SubChunkIndex ind = index.offset(dx, dy, dz);
                    ShieldChunkInfo info = shieldData.get(ind);
                    if (info != null && info.isEmpty()) {
                        shieldData.remove(ind);
                    }
                }
            }
        }
    }

    // Call this when a shield is composed for a giving shielding block
    public void composeShield(BlockPos shieldPos, BlockPos shieldingPos) {
        SubChunkIndex index = calculateSubChunkIndex(shieldingPos);
        ShieldChunkInfo info = shieldData.get(index);
        if (info == null) {
            info = new ShieldChunkInfo();
            shieldData.put(index, info);
        }
        info.composeShield(shieldPos, shieldingPos);
        markDirty();
    }

    // Get the position of the shield projector given a shielding block
    @Nullable
    public BlockPos getShieldProjector(BlockPos shieldingPos) {
        SubChunkIndex index = calculateSubChunkIndex(shieldingPos);
        ShieldChunkInfo info = shieldData.get(index);
        if (info == null) {
            return null;
        }
        return info.getShieldProjector(shieldingPos);
    }


    private SubChunkIndex calculateSubChunkIndex(BlockPos pos) {
        return new SubChunkIndex(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
    }

    @Override
    public void read(CompoundNBT tag) {
        shieldData.clear();
        ListNBT list = tag.getList("info", Constants.NBT.TAG_COMPOUND);
        for (INBT inbt : list) {
            CompoundNBT nbt = (CompoundNBT) inbt;
            SubChunkIndex index = new SubChunkIndex(nbt.getInt("sx"), nbt.getInt("sy"), nbt.getInt("sz"));
            ShieldChunkInfo info = new ShieldChunkInfo();
            info.read(nbt.getCompound("info"));
            shieldData.put(index, info);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        ListNBT list = new ListNBT();
        for (Map.Entry<SubChunkIndex, ShieldChunkInfo> entry : shieldData.entrySet()) {
            CompoundNBT nbt = new CompoundNBT();
            SubChunkIndex index = entry.getKey();
            ShieldChunkInfo info = entry.getValue();
            nbt.putInt("sx", index.getSx());
            nbt.putInt("sy", index.getSy());
            nbt.putInt("sz", index.getSz());
            nbt.put("info", info.write());
            list.add(nbt);
        }
        tag.put("info", list);
        return tag;
    }

}
