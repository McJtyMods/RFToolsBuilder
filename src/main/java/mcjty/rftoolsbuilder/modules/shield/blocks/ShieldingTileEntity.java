package mcjty.rftoolsbuilder.modules.shield.blocks;

import com.mojang.serialization.MapCodec;
import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import mcjty.rftoolsbuilder.modules.shield.ShieldTexture;
import mcjty.rftoolsbuilder.modules.shield.client.ShieldRenderData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShieldingTileEntity extends BlockEntity {

    private BlockPos shieldProjector;
    private BlockState mimic;

    public ShieldingTileEntity(BlockPos pos, BlockState state) {
        super(ShieldModule.TYPE_SHIELDING.get(), pos, state);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        CompoundTag nbtTag = new CompoundTag();
        this.saveClient(nbtTag);
        return ClientboundBlockEntityDataPacket.create(this, (blockEntity, provider) -> nbtTag);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        this.saveClient(tag);
        return tag;
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

    public static final ShieldRenderData DEFAULT_RENDER_DATA = new ShieldRenderData(1.0f, 1.0f, 1.0f, 1.0f, ShieldTexture.SHIELD);

    public ShieldRenderData getRenderData() {
        if (shieldProjector != null && level != null && level.getBlockEntity(shieldProjector) instanceof ShieldProjectorTileEntity shield) {
            return shield.getRenderData();
        }
        return DEFAULT_RENDER_DATA;
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        CompoundTag tag = input.read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).orElseGet(CompoundTag::new);
        HolderLookup.Provider provider = input.lookup();

        shieldProjector = new BlockPos(tag.getIntOr("sx", 0), tag.getIntOr("sy", 0), tag.getIntOr("sz", 0));
        if (tag.contains("mimic")) {
            mimic = NbtUtils.readBlockState(provider.lookupOrThrow(Registries.BLOCK), tag.getCompoundOrEmpty("mimic"));
        } else {
            mimic = null;
        }
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        CompoundTag tag = new CompoundTag();
        HolderLookup.Provider provider = level != null ? level.registryAccess() : RegistryAccess.EMPTY;

        saveClient(tag);
        output.store(MapCodec.assumeMapUnsafe(CompoundTag.CODEC), tag);
    }

    private void saveClient(CompoundTag tag) {
        if (shieldProjector != null) {
            tag.putInt("sx", shieldProjector.getX());
            tag.putInt("sy", shieldProjector.getY());
            tag.putInt("sz", shieldProjector.getZ());
        }
        if (mimic != null) {
            CompoundTag camoNbt = NbtUtils.writeBlockState(mimic);
            tag.put("mimic", camoNbt);
        }
    }

    private void loadClient(CompoundTag tag, HolderLookup.Provider provider) {
        shieldProjector = new BlockPos(tag.getIntOr("sx", 0), tag.getIntOr("sy", 0), tag.getIntOr("sz", 0));
        if (tag.contains("mimic")) {
            mimic = NbtUtils.readBlockState(provider.lookup(Registries.BLOCK).get(), tag.getCompoundOrEmpty("mimic"));
        } else {
            mimic = null;
        }
    }
}
