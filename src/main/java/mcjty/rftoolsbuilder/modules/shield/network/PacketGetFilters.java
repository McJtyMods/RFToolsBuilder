package mcjty.rftoolsbuilder.modules.shield.network;

import mcjty.lib.network.ICommandHandler;
import mcjty.lib.network.TypedMapTools;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldTEBase;
import mcjty.rftoolsbuilder.modules.shield.filters.ShieldFilter;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PacketGetFilters {

    protected BlockPos pos;
    protected TypedMap params;

    public PacketGetFilters() {
    }

    public PacketGetFilters(PacketBuffer buf) {
        pos = buf.readBlockPos();
        params = TypedMapTools.readArguments(buf);
    }

    public PacketGetFilters(BlockPos pos) {
        this.pos = pos;
        this.params = TypedMap.EMPTY;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        TypedMapTools.writeArguments(buf, params);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = ctx.getSender().getEntityWorld().getTileEntity(pos);
            if(!(te instanceof ICommandHandler)) {
                Logging.log("createStartScanPacket: TileEntity is not a CommandHandler!");
                return;
            }
            ICommandHandler commandHandler = (ICommandHandler) te;
            List<ShieldFilter> list = commandHandler.executeWithResultList(ShieldTEBase.CMD_GETFILTERS, params, Type.create(ShieldFilter.class));
            RFToolsBuilderMessages.INSTANCE.sendTo(new PacketFiltersReady(pos, ShieldTEBase.CLIENTCMD_GETFILTERS, list),
                    ctx.getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.setPacketHandled(true);
    }
}
