package mcjty.rftoolsbuilder;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.modules.Modules;
import mcjty.rftoolsbase.tools.ClientItemDefinitions;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import mcjty.rftoolsbuilder.setup.*;
import mcjty.rftoolsbuilder.shapes.ScanMeshRenderer;
import mcjty.rftoolsbuilder.shapes.ScanPreviewRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod(RFToolsBuilder.MODID)
public class RFToolsBuilder {

    public static final String MODID = "rftoolsbuilder";

    @SuppressWarnings("PublicField")
    public static final ModSetup setup = new ModSetup();
    private final Modules modules = new Modules();
    public static RFToolsBuilder instance;

    public RFToolsBuilder(ModContainer mod, IEventBus bus, Dist dist) {
        instance = this;
        setupModules(bus, dist);

        Config.register(mod, bus, modules);

        Registration.register(bus);

        bus.addListener(setup::init);
        bus.addListener(modules::init);
        bus.addListener(this::onDataGen);
        bus.addListener(RFToolsBuilderMessages::registerMessages);
        bus.addListener(setup.getBlockCapabilityRegistrar(Registration.RBLOCKS));

        if (dist.isClient()) {
            NeoForge.EVENT_BUS.addListener(ScanMeshRenderer::register);
            bus.addListener(ScanPreviewRenderer::register);
            bus.addListener(ScanPreviewRenderer::registerPipelines);
            bus.addListener(ClientSetup::init);
            bus.addListener(ClientSetup::registerKeyBinds);
            bus.addListener(ClientSetup::registerBlockTints);
            bus.addListener(modules::initClient);
        }
    }

    public static <T extends Item> Supplier<T> tab(Supplier<T> supplier) {
        return instance.setup.tab(supplier);
    }

    public static <T extends Item> Function<Item.Properties, T> tab(Function<Item.Properties, T> factory) {
        return properties -> instance.setup.tab(() -> factory.apply(properties)).get();
    }

    private void onDataGen(GatherDataEvent.Client event) {
        DataGen datagen = new DataGen(MODID, event);
        modules.datagen(datagen, event.getLookupProvider());
        datagen.generate();
        event.getGenerator().addProvider(true, new ClientItemDefinitions(
                event.getGenerator().getPackOutput(), MODID, Map.of()));
    }

    private void setupModules(IEventBus bus, Dist dist) {
        modules.register(new BuilderModule(bus));
        modules.register(new ShieldModule(bus, dist));
        modules.register(new ScannerModule(bus));
        modules.register(new MoverModule(bus, dist));
    }
}
