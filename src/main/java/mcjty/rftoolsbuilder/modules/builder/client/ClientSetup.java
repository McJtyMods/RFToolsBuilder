package mcjty.rftoolsbuilder.modules.builder.client;

import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;

public class ClientSetup {
    public static void initClient() {
        RenderTypeLookup.setRenderLayer(BuilderModule.SUPPORT.get(), RenderType.translucent());
        RenderTypeLookup.setRenderLayer(BuilderModule.SPACE_CHAMBER.get(), RenderType.translucent());
        RenderTypeLookup.setRenderLayer(BuilderModule.SPACE_CHAMBER_CONTROLLER.get(), RenderType.translucent());
    }
}
