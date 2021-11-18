package mcjty.rftoolsbuilder.modules.shield.network;

import mcjty.lib.network.AbstractPacketSendResultToClient;
import mcjty.rftoolsbuilder.modules.shield.filters.AbstractShieldFilter;
import mcjty.rftoolsbuilder.modules.shield.filters.ShieldFilter;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class PacketFiltersReady extends AbstractPacketSendResultToClient<ShieldFilter> {

    public PacketFiltersReady(PacketBuffer buf) {
        super(buf);
    }

    public PacketFiltersReady(BlockPos pos, String command, List<ShieldFilter> list) {
        super(pos, command, list);
    }

    @Override
    protected ShieldFilter readElement(PacketBuffer buf) {
        return AbstractShieldFilter.createFilter(buf);
    }

    @Override
    protected void writeElement(PacketBuffer buf, ShieldFilter element) {
        element.toBytes(buf);
    }
}
