package mcjty.rftoolsbuilder.modules.shield.network;

import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldProjectorTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PackertNotifyServerClientReady {

    private final BlockPos pos;

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
    }

    public PackertNotifyServerClientReady(BlockPos pos) {
        this.pos = pos;
    }

    public PackertNotifyServerClientReady(PacketBuffer buf) {
        pos = buf.readBlockPos();
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = ctx.getSender().level.getBlockEntity(pos);
            if (te instanceof ShieldProjectorTileEntity) {
                ((ShieldProjectorTileEntity)te).clientIsReady();
            }
        });
        ctx.setPacketHandled(true);
    }
}
