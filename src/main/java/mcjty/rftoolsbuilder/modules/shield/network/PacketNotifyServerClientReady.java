package mcjty.rftoolsbuilder.modules.shield.network;

import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldProjectorTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketNotifyServerClientReady {

    private final BlockPos pos;

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public PacketNotifyServerClientReady(BlockPos pos) {
        this.pos = pos;
    }

    public PacketNotifyServerClientReady(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getSender().level.getBlockEntity(pos) instanceof ShieldProjectorTileEntity projector) {
                projector.clientIsReady();
            }
        });
        ctx.setPacketHandled(true);
    }
}
