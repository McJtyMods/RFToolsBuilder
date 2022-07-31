package mcjty.rftoolsbuilder.setup;


import mcjty.rftoolsbuilder.keys.KeyBindings;
import mcjty.rftoolsbuilder.keys.KeyInputHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

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
