package mcjty.rftoolsbuilder.keys;

import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbuilder.setup.CommandHandler;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;

public class KeyInputHandler {

    public static void onKeyInput() {
        if (KeyBindings.unmountVehicle.consumeClick()) {
            RFToolsBuilderMessages.sendToServer(CommandHandler.CMD_UNMOUNT, TypedMap.builder());
        }
    }
}
