package mcjty.rftoolsbuilder;

import net.fabricmc.api.ModInitializer;
import java.util.function.Function;
import java.util.function.Supplier;
import mcjty.lib.modules.Modules;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import mcjty.rftoolsbuilder.setup.*;
import net.minecraft.world.item.Item;

public class RFToolsBuilder implements ModInitializer {

    public static final String MODID = "rftoolsbuilder";

    @SuppressWarnings("PublicField")
    public static final ModSetup setup = new ModSetup();
    public final Modules modules = new Modules();
    public static RFToolsBuilder instance;

    @Override
    public void onInitialize() {
        instance = this;
        modules.register(new BuilderModule());
        modules.register(new ShieldModule());
        modules.register(new ScannerModule());
        modules.register(new MoverModule());
        Config.register(modules);
        Registration.register();
        RFToolsBuilderMessages.registerMessages();
        setup.init();
        modules.init();
        setup.registerBlockCapabilities(Registration.RBLOCKS);
    }

    public static <T extends Item> Supplier<T> tab(Supplier<T> supplier) {
        return instance.setup.tab(supplier);
    }

    public static <T extends Item> Function<Item.Properties, T> tab(Function<Item.Properties, T> factory) {
        return properties -> instance.setup.tab(() -> factory.apply(properties)).get();
    }

}
