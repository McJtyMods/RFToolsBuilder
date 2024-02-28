package mcjty.rftoolsbuilder.setup;

import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import mcjty.rftoolsbuilder.compat.rftoolsutility.RFToolsSupport;
import net.neoforged.neoforge.fml.InterModComms;
import net.neoforged.neoforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModSetup extends DefaultModSetup {

    @Override
    public void init(FMLCommonSetupEvent e) {
        super.init(e);
        e.enqueueWork(() -> {
            CommandHandler.registerCommands();
        });
        RFToolsBuilderMessages.registerMessages();
    }

    @Override
    protected void setupModCompat() {
        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
        InterModComms.sendTo("rftoolsutility", "getScreenModuleRegistry", RFToolsSupport.GetScreenModuleRegistry::new);
    }
}
