package mcjty.rftoolsbuilder.keys;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class KeyBindings {

    public static KeyMapping unmountVehicle;

    public static void init() {
        var category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("rftoolsbuilder", "keys"));
        unmountVehicle = new KeyMapping("key.unmountVehicle", InputConstants.Type.KEYBOARD, InputConstants.KEY_BACKSLASH, category);
        KeyMappingHelper.registerKeyMapping(unmountVehicle);
    }
}
