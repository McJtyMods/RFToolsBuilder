package mcjty.rftoolsbuilder.keys;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;


public class KeyBindings {

    public static KeyMapping unmountVehicle;

    public static void init(RegisterKeyMappingsEvent event) {
        unmountVehicle = new KeyMapping("key.unmountVehicle", KeyConflictContext.IN_GAME, InputConstants.getKey("key.keyboard.backslash"), "key.categories.rftools");
        event.register(unmountVehicle);
    }
}
