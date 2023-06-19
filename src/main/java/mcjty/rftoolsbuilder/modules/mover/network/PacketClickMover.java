package mcjty.rftoolsbuilder.modules.mover.network;

import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet from client to sent to indicate that the player clicked on a mover button inside the platform
 */
public class PacketClickMover {

    private final BlockPos pos;
    private final String mover;

    public PacketClickMover(BlockPos pos, String mover) {
        this.pos = pos;
        this.mover = mover;
    }

    public PacketClickMover(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        mover = buf.readUtf(32767);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(mover);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getSender().level().getBlockEntity(pos) instanceof MoverTileEntity mover) {
                mover.startMove(this.mover);
            }
        });
        ctx.setPacketHandled(true);
    }
}
