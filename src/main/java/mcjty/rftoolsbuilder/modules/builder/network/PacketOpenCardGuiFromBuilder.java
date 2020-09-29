package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.rftoolsbuilder.modules.builder.client.GuiShapeCard;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketOpenCardGuiFromBuilder {

    public void toBytes(PacketBuffer buf) {
    }

    public PacketOpenCardGuiFromBuilder() {
    }

    public PacketOpenCardGuiFromBuilder(PacketBuffer buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            GuiShapeCard.open(true);
        });
        ctx.setPacketHandled(true);
    }
}
