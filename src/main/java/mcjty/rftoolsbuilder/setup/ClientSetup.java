package mcjty.rftoolsbuilder.setup;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.shapes.ScanMeshRenderer;
import mcjty.rftoolsbuilder.shapes.ScanPreviewRenderer;
import mcjty.rftoolsbuilder.keys.KeyBindings;
import mcjty.rftoolsbuilder.keys.KeyInputHandler;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.SupportBlock;
import java.util.List;

public class ClientSetup implements ClientModInitializer {

    private static final float PREVIEW_OPACITY = 0.25f;

    @Override
    public void onInitializeClient() {
        ClientCommandHandler.registerCommands();
        ClientNetworking.register();
        KeyBindings.init();
        ClientTickEvents.START_CLIENT_TICK.register(client -> KeyInputHandler.onKeyInput());
        ScanMeshRenderer.register();
        ScanPreviewRenderer.register();
        ScanPreviewRenderer.registerPipelines();
        registerBlockTints();
        RFToolsBuilder.instance.modules.initClient();
    }


    public static void registerBlockTints() {
        // Compensate for the original texture alpha so all three status colors have the same opacity.
        BlockColorRegistry.register(List.of(state -> {
            int textureAlpha = switch (state.getValue(SupportBlock.STATUS)) {
                case STATUS_OK -> 129;
                case STATUS_WARN -> 126;
                case STATUS_ERROR -> 92;
            };
            int tintAlpha = Math.round(PREVIEW_OPACITY * 255 * 255 / textureAlpha);
            return (tintAlpha << 24) | 0xffffff;
        }), BuilderModule.SUPPORT.get());
    }
}
