package mcjty.rftoolsbuilder.modules.scanner.network;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.shapes.BeaconType;
import mcjty.rftoolsbuilder.shapes.ScanDataManagerClient;
import mcjty.rftoolsbuilder.shapes.ScanExtraData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record PacketReturnExtraData(int scanId, ScanExtraData data) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsBuilder.MODID, "returnextradata");

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(scanId);
        if (data == null) {
            buf.writeInt(-1);
        } else {
            List<ScanExtraData.Beacon> beacons = data.getBeacons();
            buf.writeInt(beacons.size());
            for (ScanExtraData.Beacon beacon : beacons) {
                buf.writeBlockPos(beacon.getPos());
                buf.writeByte(beacon.getType().ordinal());
                buf.writeBoolean(beacon.isDoBeacon());
            }
        }
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static PacketReturnExtraData create(FriendlyByteBuf buf) {
        int scanId = buf.readInt();
        ScanExtraData data;
        int size = buf.readInt();
        if (size == -1) {
            data = null;
        } else {
            data = new ScanExtraData();
            for (int i = 0; i < size; i++) {
                BlockPos pos = buf.readBlockPos();
                BeaconType type = BeaconType.VALUES[buf.readByte()];
                boolean doBeacon = buf.readBoolean();
                data.addBeacon(pos, type, doBeacon);
            }
        }
        return new PacketReturnExtraData(scanId, data);
    }

    public static PacketReturnExtraData create(int scanId, ScanExtraData extraData) {
        return new PacketReturnExtraData(scanId, extraData);
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ScanDataManagerClient.getScansClient().registerExtraDataFromServer(scanId, data);
        });
    }
}