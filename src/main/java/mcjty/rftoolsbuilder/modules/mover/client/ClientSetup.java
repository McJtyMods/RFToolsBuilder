package mcjty.rftoolsbuilder.modules.mover.client;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

import static mcjty.rftoolsbuilder.modules.mover.client.MoverRenderer.BLACK;

public class ClientSetup {

    public static void initClient() {
        MinecraftForge.EVENT_BUS.addListener(MoverRenderer::onCameraSetup);
    }

    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (!event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            return;
        }
        event.addSprite(BLACK);
    }

}
