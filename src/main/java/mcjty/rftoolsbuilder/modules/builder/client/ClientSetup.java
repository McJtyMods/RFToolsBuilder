package mcjty.rftoolsbuilder.modules.builder.client;

import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;

public class ClientSetup {
    public static void initClient() {
        ItemBlockRenderTypes.setRenderLayer(BuilderModule.SUPPORT.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(BuilderModule.SPACE_CHAMBER.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(BuilderModule.SPACE_CHAMBER_CONTROLLER.get(), RenderType.translucent());
    }
}
