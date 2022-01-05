package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.lib.api.container.CapabilityContainerProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class PacketOpenBuilderGui {

    private BlockPos pos;

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public PacketOpenBuilderGui(BlockPos pos) {
        this.pos = pos;
    }

    public PacketOpenBuilderGui(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            Level world = ctx.getSender().getCommandSenderWorld();
            BlockEntity te = world.getBlockEntity(pos);
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
