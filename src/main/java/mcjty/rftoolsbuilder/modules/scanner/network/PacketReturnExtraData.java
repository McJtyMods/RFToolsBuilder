package mcjty.rftoolsbuilder.modules.scanner.network;

import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.shapes.ScanDataManagerClient;
import mcjty.rftoolsbuilder.shapes.ScanExtraData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketReturnExtraData(int scanId, ScanExtraData data) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsBuilder.MODID, "returnextradata");
    public static final CustomPacketPayload.Type<PacketReturnExtraData> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacketReturnExtraData> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PacketReturnExtraData::scanId,
            ScanExtraData.OPTIONAL_STREAM_CODEC, PacketReturnExtraData::data,
            PacketReturnExtraData::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static PacketReturnExtraData create(int scanId, ScanExtraData extraData) {
        return new PacketReturnExtraData(scanId, extraData);
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ScanDataManagerClient.getScansClient().registerExtraDataFromServer(scanId, data);
        });
    }
}