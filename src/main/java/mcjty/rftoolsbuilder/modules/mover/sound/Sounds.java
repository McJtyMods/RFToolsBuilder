package mcjty.rftoolsbuilder.modules.mover.sound;

import mcjty.lib.varia.SoundTools;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

public class Sounds {

    public static final RegistryObject<SoundEvent> MOVER_LOOP = Registration.SOUNDS.register("mover_loop", () -> SoundTools.createSoundEvent(new ResourceLocation(RFToolsBuilder.MODID, "mover_loop")));

    public static void init() {
    }

}
