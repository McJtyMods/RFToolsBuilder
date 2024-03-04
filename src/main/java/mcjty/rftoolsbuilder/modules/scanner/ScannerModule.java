package mcjty.rftoolsbuilder.modules.scanner;

import mcjty.lib.modules.IModule;
import mcjty.rftoolsbuilder.setup.Config;
import mcjty.rftoolsbuilder.shapes.ShapeDataManagerClient;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public class ScannerModule implements IModule {

    @Override
    public void init(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.register(new ShapeHandler());
    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(ShapeDataManagerClient::cleanupOldRenderers);
    }

    @Override
    public void initConfig(IEventBus bus) {
        ScannerConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }
}
