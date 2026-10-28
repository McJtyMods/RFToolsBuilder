package mcjty.rftoolsbuilder.modules.mover.blocks;

import com.mojang.serialization.MapCodec;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class InvisibleMoverBE extends BlockEntity {

    private BlockState originalState;

    public InvisibleMoverBE(BlockPos pos, BlockState state) {
        super(MoverModule.TYPE_INVISIBLE_MOVER.get(), pos, state);
    }

    @Override
    public void onDataPacket(Connection net, ValueInput input) {
        loadInt(input.read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).orElseGet(CompoundTag::new), input.lookup());
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        CompoundTag tag = getUpdateTag(level.registryAccess());
        return ClientboundBlockEntityDataPacket.create(this, (BlockEntity entity, RegistryAccess access) -> tag);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveInt(super.getUpdateTag(provider));
    }

    public BlockState getOriginalState() {
        return originalState;
    }

    public void setOriginalState(BlockState originalState) {
        this.originalState = originalState;
        setChanged();
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        CompoundTag tag = input.read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).orElseGet(CompoundTag::new);
        HolderLookup.Provider provider = input.lookup();

        loadInt(tag, provider);
    }

    private void loadInt(CompoundTag tag, HolderLookup.Provider provider) {
        originalState = NbtUtils.readBlockState(provider.lookup(Registries.BLOCK).get(), tag.getCompoundOrEmpty("originalState"));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        CompoundTag tag = new CompoundTag();
        HolderLookup.Provider provider = level != null ? level.registryAccess() : RegistryAccess.EMPTY;

        saveInt(tag);
        output.store(MapCodec.assumeMapUnsafe(CompoundTag.CODEC), tag);
    }

    private CompoundTag saveInt(CompoundTag tag) {
        if (originalState != null) {
            CompoundTag tagState = NbtUtils.writeBlockState(originalState);
            tag.put("originalState", tagState);
        }
        return tag;
    }
}
