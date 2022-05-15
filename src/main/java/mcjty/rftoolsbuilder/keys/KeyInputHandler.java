package mcjty.rftoolsbuilder.keys;

import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbuilder.setup.CommandHandler;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KeyInputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (KeyBindings.unmountVehicle.consumeClick()) {
            RFToolsBuilderMessages.sendToServer(CommandHandler.CMD_UNMOUNT, TypedMap.builder());
        }
    }
}
