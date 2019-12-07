package mcjty.rftoolsbuilder.modules.shield.network;

import mcjty.lib.varia.RLE;
import mcjty.rftoolsbuilder.modules.shield.data.ShieldChunkInfo;
import mcjty.rftoolsbuilder.modules.shield.data.ShieldWorldInfo;
import mcjty.rftoolsbuilder.modules.shield.data.SubChunkIndex;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PacketShieldDataToClient {

    private static class SubChunkClientData {
        private RLE shieldData;
        private List<BlockPos> shields;
    }

    private Map<SubChunkIndex, SubChunkClientData> shieldData;

    public PacketShieldDataToClient(PacketBuffer buf) {
        shieldData = new HashMap<>();
        int subchunks = buf.readInt();
        for (int i = 0 ; i < subchunks ; i++) {
            SubChunkClientData clientData = new SubChunkClientData();
            SubChunkIndex index = new SubChunkIndex(buf.readInt(), buf.readInt(), buf.readInt());
            int size = buf.readInt();
            byte[] data = new byte[size];
            buf.readBytes(data);
            clientData.shieldData = new RLE();
            clientData.shieldData.setData(data);

            size = buf.readInt();
            clientData.shields = new ArrayList<>(size);
            for (int j = 0 ; j < size ; j++) {
                clientData.shields.add(buf.readBlockPos());
            }
            shieldData.put(index, clientData);
        }
    }

    public PacketShieldDataToClient(World world) {
        Map<SubChunkIndex, ShieldChunkInfo> data = ShieldWorldInfo.get(world).getShieldData();
        shieldData = new HashMap<>(data.size());
        for (Map.Entry<SubChunkIndex, ShieldChunkInfo> entry : data.entrySet()) {
            ShieldChunkInfo chunkInfo = entry.getValue();
            SubChunkClientData clientData = new SubChunkClientData();
            clientData.shieldData = chunkInfo.createDataRLE();
    // @todo
            shieldData.put(entry.getKey(), clientData);
        }
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(shieldData.size());
        for (Map.Entry<SubChunkIndex, SubChunkClientData> entry : shieldData.entrySet()) {
            SubChunkIndex index = entry.getKey();
            SubChunkClientData clientData = entry.getValue();
            buf.writeInt(index.getSx());
            buf.writeInt(index.getSy());
            buf.writeInt(index.getSz());

            buf.writeInt(clientData.shieldData.getData().length);
            buf.writeBytes(clientData.shieldData.getData());

            buf.writeInt(clientData.shields.size());
            for (BlockPos pos : clientData.shields) {
                buf.writeBlockPos(pos);
            }
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // @todo
        });
        ctx.setPacketHandled(true);
    }
}
