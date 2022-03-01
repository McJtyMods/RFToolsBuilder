package mcjty.rftoolsbuilder.modules.mover.network;

import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class PacketGrabbedEntitiesToClient {

    private final BlockPos pos;
    private final Set<Integer> grabbedEntities;

    public PacketGrabbedEntitiesToClient(BlockPos pos, Set<Integer> grabbedEntities) {
        this.pos = pos;
        this.grabbedEntities = new HashSet<>(grabbedEntities);
    }

    public PacketGrabbedEntitiesToClient(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.grabbedEntities = new HashSet<>(size);
        for (int i = 0 ; i < size ; i++) {
            this.grabbedEntities.add(buf.readInt());
        }
        this.pos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(grabbedEntities.size());
        for (Integer entity : grabbedEntities) {
            buf.writeInt(entity);
        }
        buf.writeBlockPos(pos);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (SafeClientTools.getClientWorld().getBlockEntity(pos) instanceof MoverTileEntity mover) {
                mover.getLogic().setGrabbedEntitiesClient(grabbedEntities);
            }
        });
        ctx.setPacketHandled(true);
    }
}
