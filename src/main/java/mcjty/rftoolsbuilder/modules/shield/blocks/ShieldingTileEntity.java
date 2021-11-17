package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import mcjty.rftoolsbuilder.modules.shield.ShieldTexture;
import mcjty.rftoolsbuilder.modules.shield.client.ShieldRenderData;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShieldingTileEntity extends TileEntity {

    private BlockPos shieldProjector;
    private BlockState mimic;

    public static final ModelProperty<Integer> ICON_TOPDOWN = new ModelProperty<>();
    public static final ModelProperty<Integer> ICON_SIDE = new ModelProperty<>();
    public static final ModelProperty<BlockState> MIMIC = new ModelProperty<>();
    public static final ModelProperty<ShieldRenderData> RENDER_DATA = new ModelProperty<>();

    public ShieldingTileEntity() {
        super(ShieldModule.TYPE_SHIELDING.get());
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbtTag = new CompoundNBT();
        this.save(nbtTag);
        return new SUpdateTileEntityPacket(worldPosition, 1, nbtTag);
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        load(getBlockState(), pkt.getTag());
        ModelDataManager.requestModelDataRefresh(this);
        BlockState state = level.getBlockState(worldPosition);
        level.sendBlockUpdated(worldPosition, state, state, Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
    }

    public BlockPos getShieldProjector() {
        return shieldProjector;
    }

    public void setShieldProjector(BlockPos shieldProjector) {
        this.shieldProjector = shieldProjector;
        setChanged();

    }

    public BlockState getMimic() {
        return mimic;
    }

    public void setMimic(BlockState mimic) {
        this.mimic = mimic;
        setChanged();
    }

    private static final ShieldRenderData DEFAULT_RENDER_DATA = new ShieldRenderData(1.0f, 1.0f, 1.0f, 1.0f, ShieldTexture.SHIELD);

    @Nonnull
    @Override
    public IModelData getModelData() {
        int x = worldPosition.getX();
        int y = worldPosition.getY();
        int z = worldPosition.getZ();
        int topdown = (z & 0x1) * 2 + (x & 0x1);
        int side = (y & 0x1) * 2 + ((x + z) & 0x1);
        ShieldRenderData renderData = DEFAULT_RENDER_DATA;
        if (shieldProjector != null) {
            TileEntity te = level.getBlockEntity(shieldProjector);
            if (te instanceof ShieldProjectorTileEntity) {
                renderData = ((ShieldProjectorTileEntity) te).getRenderData();
            }
        }
        return new ModelDataMap.Builder()
                .withInitial(ICON_SIDE, side)
                .withInitial(ICON_TOPDOWN, topdown)
                .withInitial(MIMIC, mimic)
                .withInitial(RENDER_DATA, renderData)
                .build();
    }

    @Override
    public void load(@Nonnull BlockState state, @Nonnull CompoundNBT tag) {
        super.load(state, tag);
        shieldProjector = new BlockPos(tag.getInt("sx"), tag.getInt("sy"), tag.getInt("sz"));
        if (tag.contains("mimic")) {
            mimic = NBTUtil.readBlockState(tag.getCompound("mimic"));
        } else {
            mimic = null;
        }
    }

    @Nonnull
    @Override
    public CompoundNBT save(CompoundNBT tag) {
        tag.putInt("sx", shieldProjector.getX());
        tag.putInt("sy", shieldProjector.getY());
        tag.putInt("sz", shieldProjector.getZ());
        if (mimic != null) {
            CompoundNBT camoNbt = NBTUtil.writeBlockState(mimic);
            tag.put("mimic", camoNbt);
        }
        return super.save(tag);
    }
}
