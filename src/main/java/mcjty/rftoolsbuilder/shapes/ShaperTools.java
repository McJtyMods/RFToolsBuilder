package mcjty.rftoolsbuilder.shapes;

import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbuilder.modules.scanner.network.PacketReturnExtraData;
import mcjty.rftoolsbuilder.setup.ClientCommandHandler;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;

public class ShaperTools {

    public static void requestExtraShapeData(Player player, int scanId) {
        ScanExtraData extraData = ScanDataManager.get(player.getCommandSenderWorld()).getExtraData(scanId);
        RFToolsBuilderMessages.INSTANCE.sendTo(new PacketReturnExtraData(scanId, extraData), ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void requestLocatorEnergyConsumption(Player player, BlockPos pos) {
        Level world = player.getCommandSenderWorld();
        BlockEntity te = world.getBlockEntity(pos);
        // @todo 1.14 locator
//        if (te instanceof LocatorTileEntity) {
//            int energy = ((LocatorTileEntity) te).getEnergyPerScan();
//            RFToolsBuilderMessages.sendToClient(player, ClientCommandHandler.CMD_RETURN_ENERGY_CONSUMPTION,
//                    TypedMap.builder().put(ClientCommandHandler.PARAM_ENERGY, energy));
//        }
    }

    public static void requestScanDirty(Player player, int scanId) {
        int counter = ScanDataManager.get(player.getCommandSenderWorld()).loadScan(player.getCommandSenderWorld(), scanId).getDirtyCounter();
        RFToolsBuilderMessages.sendToClient(player, ClientCommandHandler.CMD_RETURN_SCAN_DIRTY,
                TypedMap.builder().put(ClientCommandHandler.PARAM_SCANID, scanId).put(ClientCommandHandler.PARAM_COUNTER, counter));
    }
}
