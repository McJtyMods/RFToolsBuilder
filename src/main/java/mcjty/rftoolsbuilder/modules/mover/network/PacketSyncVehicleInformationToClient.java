package mcjty.rftoolsbuilder.modules.mover.network;

import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketSyncVehicleInformationToClient {

    private final BlockPos pos;
    private final List<String> platforms;
    private final String currentPlatform;
    private final boolean valid;
    private final boolean enoughPower;

    public PacketSyncVehicleInformationToClient(BlockPos pos, List<String> platforms, String currentPlatform, boolean valid, boolean enoughPower) {
        this.pos = pos;
        this.platforms = platforms;
        this.currentPlatform = currentPlatform;
        this.valid = valid;
        this.enoughPower = enoughPower;
    }

    public PacketSyncVehicleInformationToClient(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        int size = buf.readInt();
        platforms = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            platforms.add(buf.readUtf(32767));
        }
        if (buf.readBoolean()) {
            currentPlatform = buf.readUtf(32767);
        } else {
            currentPlatform = null;
        }
        valid = buf.readBoolean();
        enoughPower = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(platforms.size());
        for (String s : platforms) {
            buf.writeUtf(s);
        }
        if (currentPlatform != null) {
            buf.writeBoolean(true);
            buf.writeUtf(currentPlatform);
        } else {
            buf.writeBoolean(false);
        }
        buf.writeBoolean(valid);
        buf.writeBoolean(enoughPower);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (SafeClientTools.getClientWorld().getBlockEntity(pos) instanceof MoverTileEntity mover) {
                mover.setClientRenderInfo(platforms, currentPlatform, valid, enoughPower);
            }
        });
        ctx.setPacketHandled(true);
    }
}
