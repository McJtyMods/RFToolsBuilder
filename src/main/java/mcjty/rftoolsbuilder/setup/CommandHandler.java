package mcjty.rftoolsbuilder.setup;

import mcjty.lib.McJtyLib;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderTools;
import mcjty.rftoolsbuilder.shapes.ShaperTools;
import net.minecraft.util.math.BlockPos;

public class CommandHandler {

    public static final String CMD_REQUEST_SHAPE_DATA = "requestShapeData";
    public static final String CMD_REQUEST_SCAN_DIRTY = "requestScanDirty";
    public static final String CMD_REQUEST_LOCATOR_ENERGY = "requestLocatorEnergy";
    public static final String CMD_GET_CHAMBER_INFO = "getChamberInfo";
    public static final Key<BlockPos> PARAM_POS = new Key<>("pos", Type.BLOCKPOS);

    public static final String CMD_GET_SECURITY_INFO = "getSecurityInfo";
    public static final Key<Integer> PARAM_ID = new Key<>("id", Type.INTEGER);

    public static void registerCommands() {
        McJtyLib.registerCommand(RFToolsBuilder.MODID, CMD_REQUEST_SHAPE_DATA, (player, arguments) -> {
            ShaperTools.requestExtraShapeData(player, arguments.get(PARAM_ID));
            return true;
        });
        McJtyLib.registerCommand(RFToolsBuilder.MODID, CMD_REQUEST_SCAN_DIRTY, (player, arguments) -> {
            ShaperTools.requestScanDirty(player, arguments.get(PARAM_ID));
            return true;
        });
        McJtyLib.registerCommand(RFToolsBuilder.MODID, CMD_REQUEST_LOCATOR_ENERGY, (player, arguments) -> {
            ShaperTools.requestLocatorEnergyConsumption(player, arguments.get(PARAM_POS));
            return true;
        });
        McJtyLib.registerCommand(RFToolsBuilder.MODID, CMD_GET_CHAMBER_INFO, (player, arguments) -> {
            BuilderTools.returnChamberInfo(player);
            return true;
        });
    }

}
