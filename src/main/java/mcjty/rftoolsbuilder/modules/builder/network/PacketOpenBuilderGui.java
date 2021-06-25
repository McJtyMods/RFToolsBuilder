package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.lib.api.container.CapabilityContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.function.Supplier;

public class PacketOpenBuilderGui {

    private BlockPos pos;

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
    }

    public PacketOpenBuilderGui(BlockPos pos) {
        this.pos = pos;
    }

    public PacketOpenBuilderGui(PacketBuffer buf) {
        pos = buf.readBlockPos();
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            World world = ctx.getSender().getCommandSenderWorld();
            TileEntity te = world.getBlockEntity(pos);
            if (te == null) {
                return;
            }
            te.getCapability(CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY).ifPresent(h -> {
                NetworkHooks.openGui(ctx.getSender(), h, pos);
            });
        });
        ctx.setPacketHandled(true);
    }
}
