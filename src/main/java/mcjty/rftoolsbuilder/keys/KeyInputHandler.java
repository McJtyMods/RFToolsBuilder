package mcjty.rftoolsbuilder.keys;

import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbuilder.setup.CommandHandler;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class KeyInputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        if (KeyBindings.unmountVehicle.consumeClick()) {
            RFToolsBuilderMessages.sendToServer(CommandHandler.CMD_UNMOUNT, TypedMap.builder());
        }
    }
}
