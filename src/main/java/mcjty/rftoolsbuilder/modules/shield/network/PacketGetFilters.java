package mcjty.rftoolsbuilder.modules.shield.network;

import mcjty.lib.network.AbstractPacketGetListFromServer;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldProjectorTileEntity;
import mcjty.rftoolsbuilder.modules.shield.filters.ShieldFilter;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.List;

public class PacketGetFilters extends AbstractPacketGetListFromServer<ShieldFilter> {

    public PacketGetFilters(PacketBuffer buf) {
        super(buf);
    }

    public PacketGetFilters(BlockPos pos) {
        super(pos, ShieldProjectorTileEntity.CMD_GETFILTERS.getName(), TypedMap.EMPTY);
    }

    @Override
    protected SimpleChannel getChannel() {
        return RFToolsBuilderMessages.INSTANCE;
    }

    @Override
    protected Class<ShieldFilter> getType() {
        return ShieldFilter.class;
    }

    @Override
    protected Object createReturnPacket(List<ShieldFilter> list) {
        return new PacketFiltersReady(pos, command, list);
    }

}
