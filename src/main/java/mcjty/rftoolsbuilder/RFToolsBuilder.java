package mcjty.rftoolsbuilder;

import mcjty.lib.base.ModBase;
import mcjty.rftoolsbuilder.setup.Config;
import mcjty.rftoolsbuilder.setup.ModSetup;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RFToolsBuilder.MODID)
public class RFToolsBuilder implements ModBase {

    public static final String MODID = "rftoolsbuilder";

    @SuppressWarnings("PublicField")
    public static ModSetup setup = new ModSetup();

    public static RFToolsBuilder instance;

    public RFToolsBuilder() {
        instance = this;

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);

        Registration.register();

        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent event) -> setup.init(event));
    }

    @Override
    public String getModId() {
        return MODID;
    }
}
