package mcjty.rftoolsbuilder;

import mcjty.rftoolsbuilder.setup.ClientSetup;
import mcjty.rftoolsbuilder.setup.Config;
import mcjty.rftoolsbuilder.setup.ModSetup;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RFToolsBuilder.MODID)
public class RFToolsBuilder {

    public static final String MODID = "rftoolsbuilder";

    @SuppressWarnings("PublicField")
    public static ModSetup setup = new ModSetup();

    public static RFToolsBuilder instance;

    public RFToolsBuilder() {
        instance = this;

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);

        Registration.register();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(setup::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::modelInit);
    }
}
