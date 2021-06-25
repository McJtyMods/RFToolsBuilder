package mcjty.rftoolsbuilder.modules.shield.client;

import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;

public class ClientSetup {
    public static void initClient() {
        RenderTypeLookup.setRenderLayer(ShieldModule.SHIELDING_TRANSLUCENT.get(), RenderType.translucent());
        RenderTypeLookup.setRenderLayer(ShieldModule.SHIELDING_SOLID.get(), RenderType.solid());
        RenderTypeLookup.setRenderLayer(ShieldModule.SHIELDING_CUTOUT.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ShieldModule.TEMPLATE_GREEN.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ShieldModule.TEMPLATE_BLUE.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ShieldModule.TEMPLATE_RED.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ShieldModule.TEMPLATE_YELLOW.get(), RenderType.cutout());
    }
}
