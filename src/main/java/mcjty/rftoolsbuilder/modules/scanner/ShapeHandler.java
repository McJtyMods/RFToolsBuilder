package mcjty.rftoolsbuilder.modules.scanner;

import mcjty.rftoolsbuilder.shapes.ShapeDataManagerServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ShapeHandler {

    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.level.dimension().equals(Level.OVERWORLD)) {
            int delay = Math.max(1, ScannerConfiguration.projectorPlaneSendInterval.get());
            if ((event.level.getGameTime() % delay) == 0) {
                ShapeDataManagerServer.handleWork(delay);
            }
        }
    }

}
