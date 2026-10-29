package mcjty.rftoolsbuilder.modules.mover.client;

import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import java.util.Collections;
import java.util.List;
import static mcjty.rftoolsbuilder.modules.mover.client.MoverRenderer.BLACK;

public class ClientSetup {

    public static void initClient() {
        LevelRenderEvents.END_EXTRACTION.register(context -> MoverRenderer.onCameraSetup());
        LevelRenderEvents.COLLECT_SUBMITS.register(MoverRenderer::submitMovers);
    }

    public static List<Identifier> onTextureStitch() {
        return Collections.singletonList(BLACK);
    }

}
