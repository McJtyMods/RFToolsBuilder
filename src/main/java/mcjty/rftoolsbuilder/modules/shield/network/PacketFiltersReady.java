package mcjty.rftoolsbuilder.modules.shield.network;

import mcjty.lib.McJtyLib;
import mcjty.lib.network.IClientCommandHandler;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbuilder.modules.shield.filters.AbstractShieldFilter;
import mcjty.rftoolsbuilder.modules.shield.filters.ShieldFilter;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketFiltersReady {

    private BlockPos pos;
    private List<ShieldFilter> list;
    private String command;

    public PacketFiltersReady() {
    }

    public PacketFiltersReady(PacketBuffer buf) {
        pos = buf.readBlockPos();
        command = buf.readUtf(32767);
        int size = buf.readInt();
        if (size != -1) {
            list = new ArrayList<>(size);
            for (int i = 0 ; i < size ; i++) {
                ShieldFilter item = AbstractShieldFilter.createFilter(buf);
                list.add(item);
            }
        } else {
            list = null;
        }
    }

    public PacketFiltersReady(BlockPos pos, String command, List<ShieldFilter> list) {
        this.pos = pos;
        this.command = command;
        this.list = new ArrayList<>();
        this.list.addAll(list);
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(command);

        if (list == null) {
            buf.writeInt(-1);
        } else {
            buf.writeInt(list.size());
            for (ShieldFilter item : list) {
                item.toBytes(buf);
            }
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = McJtyLib.proxy.getClientWorld().getBlockEntity(pos);
            if(!(te instanceof IClientCommandHandler)) {
                Logging.log("createInventoryReadyPacket: TileEntity is not a ClientCommandHandler!");
                return;
            }
            IClientCommandHandler clientCommandHandler = (IClientCommandHandler) te;
            if (!clientCommandHandler.receiveListFromServer(command, list, Type.create(ShieldFilter.class))) {
                Logging.log("Command " + command + " was not handled!");
            }
        });
        ctx.setPacketHandled(true);
    }
}
