package mcjty.rftoolsbuilder.modules.mover.client;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Collections;
import java.util.List;

import static mcjty.rftoolsbuilder.modules.mover.client.MoverRenderer.BLACK;

public class ClientSetup {

    public static void initClient() {
        NeoForge.EVENT_BUS.addListener(MoverRenderer::onCameraSetup);
        NeoForge.EVENT_BUS.addListener(MoverRenderer::submitMovers);
    }

    public static List<Identifier> onTextureStitch() {
        return Collections.singletonList(BLACK);
    }

}
