package mcjty.rftoolsbuilder.shapes;

import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbuilder.modules.scanner.network.PacketReturnExtraData;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import mcjty.rftoolsbuilder.setup.ClientCommandHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;

public class ShaperTools {

    public static void requestExtraShapeData(PlayerEntity player, int scanId) {
        ScanExtraData extraData = ScanDataManager.get(player.getEntityWorld()).getExtraData(scanId);
        RFToolsBuilderMessages.INSTANCE.sendTo(new PacketReturnExtraData(scanId, extraData), ((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void requestLocatorEnergyConsumption(PlayerEntity player, BlockPos pos) {
        World world = player.getEntityWorld();
        TileEntity te = world.getTileEntity(pos);
        // @todo 1.14 locator
//        if (te instanceof LocatorTileEntity) {
//            int energy = ((LocatorTileEntity) te).getEnergyPerScan();
//            RFToolsBuilderMessages.sendToClient(player, ClientCommandHandler.CMD_RETURN_ENERGY_CONSUMPTION,
//                    TypedMap.builder().put(ClientCommandHandler.PARAM_ENERGY, energy));
//        }
    }

    public static void requestScanDirty(PlayerEntity player, int scanId) {
        int counter = ScanDataManager.get(player.getEntityWorld()).loadScan(scanId).getDirtyCounter();
        RFToolsBuilderMessages.sendToClient(player, ClientCommandHandler.CMD_RETURN_SCAN_DIRTY,
                TypedMap.builder().put(ClientCommandHandler.PARAM_SCANID, scanId).put(ClientCommandHandler.PARAM_COUNTER, counter));
    }
}
