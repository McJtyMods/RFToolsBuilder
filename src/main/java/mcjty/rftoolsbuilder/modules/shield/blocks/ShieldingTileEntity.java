package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import mcjty.rftoolsbuilder.modules.shield.ShieldTexture;
import mcjty.rftoolsbuilder.modules.shield.client.ShieldRenderData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShieldingTileEntity extends BlockEntity {

    private BlockPos shieldProjector;
    private BlockState mimic;

    public static final ModelProperty<Integer> ICON_TOPDOWN = new ModelProperty<>();
    public static final ModelProperty<Integer> ICON_SIDE = new ModelProperty<>();
    public static final ModelProperty<BlockState> MIMIC = new ModelProperty<>();
    public static final ModelProperty<ShieldRenderData> RENDER_DATA = new ModelProperty<>();

    public ShieldingTileEntity(BlockPos pos, BlockState state) {
        super(ShieldModule.TYPE_SHIELDING.get(), pos, state);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        CompoundTag nbtTag = new CompoundTag();
        this.saveAdditional(nbtTag);
        return ClientboundBlockEntityDataPacket.create(this, blockEntity -> nbtTag);
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
        requestModelDataUpdate();
        BlockState state = level.getBlockState(worldPosition);
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
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
    public ModelData getModelData() {
        int x = worldPosition.getX();
        int y = worldPosition.getY();
        int z = worldPosition.getZ();
        int topdown = (z & 0x1) * 2 + (x & 0x1);
        int side = (y & 0x1) * 2 + ((x + z) & 0x1);
        ShieldRenderData renderData = DEFAULT_RENDER_DATA;
        if (shieldProjector != null) {
            BlockEntity te = level.getBlockEntity(shieldProjector);
            if (te instanceof ShieldProjectorTileEntity shield) {
                renderData = shield.getRenderData();
            }
        }
        return ModelData.builder()
                .with(ICON_SIDE, side)
                .with(ICON_TOPDOWN, topdown)
                .with(MIMIC, mimic)
                .with(RENDER_DATA, renderData)
                .build();
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        shieldProjector = new BlockPos(tag.getInt("sx"), tag.getInt("sy"), tag.getInt("sz"));
        if (tag.contains("mimic")) {
            mimic = NbtUtils.readBlockState(tag.getCompound("mimic"));
        } else {
            mimic = null;
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.putInt("sx", shieldProjector.getX());
        tag.putInt("sy", shieldProjector.getY());
        tag.putInt("sz", shieldProjector.getZ());
        if (mimic != null) {
            CompoundTag camoNbt = NbtUtils.writeBlockState(mimic);
            tag.put("mimic", camoNbt);
        }
    }
}
