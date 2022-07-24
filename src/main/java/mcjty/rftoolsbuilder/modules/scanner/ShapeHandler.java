package mcjty.rftoolsbuilder.modules.scanner;

import mcjty.rftoolsbuilder.shapes.ShapeDataManagerServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ShapeHandler {

    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.world.dimension().equals(Level.OVERWORLD)) {
            ShapeDataManagerServer.handleWork();
        }
    }

}
