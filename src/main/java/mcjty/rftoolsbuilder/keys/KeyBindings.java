package mcjty.rftoolsbuilder.keys;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;


public class KeyBindings {

    public static KeyMapping unmountVehicle;

    public static void init() {
        unmountVehicle = new KeyMapping("key.unmountVehicle", KeyConflictContext.IN_GAME, InputConstants.getKey("key.keyboard.backslash"), "key.categories.rftools");
        ClientRegistry.registerKeyBinding(unmountVehicle);
    }
}
