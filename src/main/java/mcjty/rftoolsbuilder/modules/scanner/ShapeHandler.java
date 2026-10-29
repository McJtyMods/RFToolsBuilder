package mcjty.rftoolsbuilder.modules.scanner;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import mcjty.rftoolsbuilder.shapes.ShapeDataManagerServer;

public class ShapeHandler {

    public static void register() {
        ServerTickEvents.START_SERVER_TICK.register(server -> ShapeDataManagerServer.handleWork());
    }

}
