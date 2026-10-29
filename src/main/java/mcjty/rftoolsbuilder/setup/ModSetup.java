package mcjty.rftoolsbuilder.setup;

import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import mcjty.rftoolsbuilder.compat.rftoolsutility.RFToolsSupport;

public class ModSetup extends DefaultModSetup {

    @Override
    public void init() {
        super.init();
        BuilderChunkTickets.init();
        CommandHandler.registerCommands();
    }

    @Override
    protected void setupModCompat() {
        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
        RFToolsSupport.register();
    }
}
