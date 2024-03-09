package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.client.GuiShapeCard;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record PacketOpenCardGuiFromBuilder() implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsBuilder.MODID, "opencardguifrombuilder");

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static PacketOpenCardGuiFromBuilder create(FriendlyByteBuf buf) {
        return new PacketOpenCardGuiFromBuilder();
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            GuiShapeCard.open(true);
        });
    }
}
