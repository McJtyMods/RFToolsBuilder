package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class PacketCloseContainerAndOpenCardGui {

    public void toBytes(FriendlyByteBuf buf) {
    }

    public PacketCloseContainerAndOpenCardGui() {
    }

    public PacketCloseContainerAndOpenCardGui(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ctx.getSender().doCloseContainer();
            RFToolsBuilderMessages.INSTANCE.send(PacketDistributor.PLAYER.with(ctx::getSender), new PacketOpenCardGuiFromBuilder());
        });
        ctx.setPacketHandled(true);
    }
}
