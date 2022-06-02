package mcjty.rftoolsbuilder.modules.mover.client;

import net.minecraftforge.common.MinecraftForge;

public class ClientSetup {
    public static void initClient() {
        MinecraftForge.EVENT_BUS.addListener(MoverRenderer::onCameraSetup);
    }
}
