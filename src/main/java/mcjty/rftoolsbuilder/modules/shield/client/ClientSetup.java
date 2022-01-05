package mcjty.rftoolsbuilder.modules.shield.client;

import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;

public class ClientSetup {
    public static void initClient() {
        ItemBlockRenderTypes.setRenderLayer(ShieldModule.SHIELDING_TRANSLUCENT.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ShieldModule.SHIELDING_SOLID.get(), RenderType.solid());
        ItemBlockRenderTypes.setRenderLayer(ShieldModule.SHIELDING_CUTOUT.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ShieldModule.TEMPLATE_GREEN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ShieldModule.TEMPLATE_BLUE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ShieldModule.TEMPLATE_RED.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ShieldModule.TEMPLATE_YELLOW.get(), RenderType.cutout());
    }
}
