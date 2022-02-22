package mcjty.rftoolsbuilder;

import mcjty.lib.modules.Modules;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import mcjty.rftoolsbuilder.setup.ClientSetup;
import mcjty.rftoolsbuilder.setup.Config;
import mcjty.rftoolsbuilder.setup.ModSetup;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RFToolsBuilder.MODID)
public class RFToolsBuilder {

    public static final String MODID = "rftoolsbuilder";

    @SuppressWarnings("PublicField")
    public static final ModSetup setup = new ModSetup();
    private final Modules modules = new Modules();
    public static RFToolsBuilder instance;

    public RFToolsBuilder() {
        instance = this;
        setupModules();

        Config.register(modules);

        Registration.register();

        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
        modbus.addListener(setup::init);
        modbus.addListener(modules::init);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modbus.addListener(ClientSetup::init);
            modbus.addListener(modules::initClient);
        });
    }

    private void setupModules() {
        modules.register(new BuilderModule());
        modules.register(new ShieldModule());
        modules.register(new ScannerModule());
        modules.register(new MoverModule());
    }
}
