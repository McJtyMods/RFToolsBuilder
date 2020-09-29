package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class PacketCloseContainerAndOpenCardGui {

    public void toBytes(PacketBuffer buf) {
    }

    public PacketCloseContainerAndOpenCardGui() {
    }

    public PacketCloseContainerAndOpenCardGui(PacketBuffer buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ctx.getSender().closeContainer();
            RFToolsBuilderMessages.INSTANCE.send(PacketDistributor.PLAYER.with(ctx::getSender), new PacketOpenCardGuiFromBuilder());
        });
        ctx.setPacketHandled(true);
    }
}
