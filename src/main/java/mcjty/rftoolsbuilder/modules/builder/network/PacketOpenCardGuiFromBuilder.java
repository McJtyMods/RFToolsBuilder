package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.client.GuiShapeCard;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketOpenCardGuiFromBuilder() implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsBuilder.MODID, "opencardguifrombuilder");
    public static final CustomPacketPayload.Type<PacketOpenCardGuiFromBuilder> TYPE = new Type<>(ID);

    public static final PacketOpenCardGuiFromBuilder INSTANCE = new PacketOpenCardGuiFromBuilder();

    public static final StreamCodec<FriendlyByteBuf, PacketOpenCardGuiFromBuilder> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            GuiShapeCard.open(true);
        });
    }
}
