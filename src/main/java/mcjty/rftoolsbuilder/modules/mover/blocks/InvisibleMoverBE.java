package mcjty.rftoolsbuilder.modules.mover.blocks;

import mcjty.lib.varia.NBTTools;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class InvisibleMoverBE extends BlockEntity {

    private BlockState originalState;

    public InvisibleMoverBE(BlockPos pos, BlockState state) {
        super(MoverModule.TYPE_INVISIBLE_MOVER.get(), pos, state);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        loadInt(pkt.getTag());
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        CompoundTag tag = getUpdateTag();
        return ClientboundBlockEntityDataPacket.create(this, (BlockEntity entity) -> tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveInt(super.getUpdateTag());
    }

    public BlockState getOriginalState() {
        return originalState;
    }

    public void setOriginalState(BlockState originalState) {
        this.originalState = originalState;
        setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadInt(tag);
    }

    private void loadInt(CompoundTag tag) {
        originalState = NBTTools.readBlockState(level, tag.getCompound("originalState"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        saveInt(tag);
    }

    private CompoundTag saveInt(CompoundTag tag) {
        if (originalState != null) {
            CompoundTag tagState = NbtUtils.writeBlockState(originalState);
            tag.put("originalState", tagState);
        }
        return tag;
    }
}
