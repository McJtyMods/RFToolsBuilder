package mcjty.rftoolsbuilder.setup;


import mcjty.rftoolsbuilder.keys.KeyBindings;
import mcjty.rftoolsbuilder.keys.KeyInputHandler;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.MinecraftForge;
import net.neoforged.neoforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {

    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientCommandHandler.registerCommands();
        });
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
    }
    
    public static void registerKeyBinds(RegisterKeyMappingsEvent event) {
        KeyBindings.init(event);
    }
}
