package mcjty.rftoolsbuilder.modules.scanner.network;

import mcjty.rftoolsbuilder.shapes.BeaconType;
import mcjty.rftoolsbuilder.shapes.ScanDataManagerClient;
import mcjty.rftoolsbuilder.shapes.ScanExtraData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PacketReturnExtraData {

    private int scanId;
    private ScanExtraData data;

    public void toBytes(FriendlyByteBuf buf) {
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

    public PacketReturnExtraData() {
    }

    public PacketReturnExtraData(FriendlyByteBuf buf) {
        scanId = buf.readInt();
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
    }

    public PacketReturnExtraData(int scanId, ScanExtraData extraData) {
        this.scanId = scanId;
        this.data = extraData;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ScanDataManagerClient.getScansClient().registerExtraDataFromServer(scanId, data);
        });
        ctx.setPacketHandled(true);
    }
}