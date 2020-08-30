package mcjty.rftoolsbuilder.setup;


import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {

    public static void init(FMLClientSetupEvent event) {
        DeferredWorkQueue.runLater(() -> {
            ClientCommandHandler.registerCommands();
        });
    }
}
