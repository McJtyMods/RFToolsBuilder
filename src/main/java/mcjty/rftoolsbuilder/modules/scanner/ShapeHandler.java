package mcjty.rftoolsbuilder.modules.scanner;

import mcjty.rftoolsbuilder.shapes.ShapeDataManagerServer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class ShapeHandler {

    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.level.dimension().equals(Level.OVERWORLD)) {
            ShapeDataManagerServer.handleWork();
        }
    }

}
