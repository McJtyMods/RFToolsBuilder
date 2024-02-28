package mcjty.rftoolsbuilder.modules.mover.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.MinecraftForge;

import java.util.Collections;
import java.util.List;

import static mcjty.rftoolsbuilder.modules.mover.client.MoverRenderer.BLACK;

public class ClientSetup {

    public static void initClient() {
        MinecraftForge.EVENT_BUS.addListener(MoverRenderer::onCameraSetup);
    }

    public static List<ResourceLocation> onTextureStitch() {
        return Collections.singletonList(BLACK);
    }

}
