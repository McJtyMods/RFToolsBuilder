package mcjty.rftoolsbuilder.setup;


import mcjty.rftoolsbuilder.keys.KeyBindings;
import mcjty.rftoolsbuilder.keys.KeyInputHandler;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.SupportBlock;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.List;

public class ClientSetup {

    private static final float PREVIEW_OPACITY = 0.25f;

    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientCommandHandler.registerCommands();
        });
        NeoForge.EVENT_BUS.register(new KeyInputHandler());
    }
    
    public static void registerKeyBinds(RegisterKeyMappingsEvent event) {
        KeyBindings.init(event);
    }

    public static void registerBlockTints(RegisterColorHandlersEvent.BlockTintSources event) {
        // Compensate for the original texture alpha so all three status colors have the same opacity.
        event.register(List.of(state -> {
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
