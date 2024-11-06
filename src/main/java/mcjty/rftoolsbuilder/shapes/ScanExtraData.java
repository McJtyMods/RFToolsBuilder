package mcjty.rftoolsbuilder.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Extra transient data that can be added to a scan (by the Locator for example)
 */
public class ScanExtraData {

    private final List<Beacon> beacons = new ArrayList<>();
    private long birthTime;

    public static final StreamCodec<FriendlyByteBuf, ScanExtraData> OPTIONAL_STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                if (data != null) {
                    buf.writeBoolean(true);
                    buf.writeInt(data.beacons.size());
                    for (Beacon beacon : data.beacons) {
                        buf.writeBlockPos(beacon.getPos());
                        buf.writeByte(beacon.getType().ordinal());
                        buf.writeBoolean(beacon.isDoBeacon());
                    }
                    buf.writeLong(data.birthTime);
                } else {
                    buf.writeBoolean(false);
                }
            },
            buf -> {
                if (!buf.readBoolean()) {
                    return null;
                }
                ScanExtraData data = new ScanExtraData();
                int size = buf.readInt();
                for (int i = 0; i < size; i++) {
                    BlockPos pos = buf.readBlockPos();
                    BeaconType type = BeaconType.values()[buf.readByte()];
                    boolean doBeacon = buf.readBoolean();
                    data.addBeacon(pos, type, doBeacon);
                }
                data.birthTime = buf.readLong();
                return data;
            });


    public ScanExtraData() {
        this.birthTime = System.currentTimeMillis();
    }

    public void clear() {
        beacons.clear();
    }

    public void addBeacon(BlockPos beacon, BeaconType type, boolean doBeacon) {
        beacons.add(new Beacon(beacon, type, doBeacon));
    }

    public void touch() {
        birthTime = System.currentTimeMillis();
    }

    public long getBirthTime() {
        return birthTime;
    }

    public List<Beacon> getBeacons() {
        return beacons;
    }

    public static class Beacon {
        private final BlockPos pos;
        private final BeaconType type;
        private final boolean doBeacon;

        public Beacon(BlockPos pos, BeaconType type, boolean doBeacon) {
            this.pos = pos;
            this.type = type;
            this.doBeacon = doBeacon;
        }

        public BlockPos getPos() {
            return pos;
        }

        public BeaconType getType() {
            return type;
        }

        public boolean isDoBeacon() {
            return doBeacon;
        }
    }
}
