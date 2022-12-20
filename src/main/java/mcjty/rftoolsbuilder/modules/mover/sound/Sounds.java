package mcjty.rftoolsbuilder.modules.mover.sound;

import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

public class Sounds {

    public static final RegistryObject<SoundEvent> ELEVATOR_START = Registration.SOUNDS.register("elevator_start", () -> new SoundEvent(new ResourceLocation(RFToolsBuilder.MODID, "elevator_start")));
    public static final RegistryObject<SoundEvent> ELEVATOR_LOOP = Registration.SOUNDS.register("elevator_loop", () -> new SoundEvent(new ResourceLocation(RFToolsBuilder.MODID, "elevator_loop")));
    public static final RegistryObject<SoundEvent> ELEVATOR_STOP = Registration.SOUNDS.register("elevator_stop", () -> new SoundEvent(new ResourceLocation(RFToolsBuilder.MODID, "elevator_stop")));

    public static void init() {
    }

}
