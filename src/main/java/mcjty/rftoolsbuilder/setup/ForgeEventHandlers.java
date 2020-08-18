package mcjty.rftoolsbuilder.setup;

import mcjty.lib.varia.DimensionId;
import mcjty.rftoolsbuilder.shapes.ShapeDataManagerServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START && DimensionId.fromWorld(event.world).isOverworld()) {
            ShapeDataManagerServer.handleWork();
        }
    }

}
