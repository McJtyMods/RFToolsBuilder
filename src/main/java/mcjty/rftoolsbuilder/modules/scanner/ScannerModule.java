package mcjty.rftoolsbuilder.modules.scanner;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ComposerBlock;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ComposerTileEntity;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ProjectorBlock;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ProjectorTileEntity;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ScannerBlock;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ScannerTileEntity;
import mcjty.rftoolsbuilder.modules.scanner.client.GuiComposer;
import mcjty.rftoolsbuilder.modules.scanner.client.GuiProjector;
import mcjty.rftoolsbuilder.modules.scanner.client.GuiScanner;
import mcjty.rftoolsbuilder.modules.scanner.client.ProjectorRenderer;
import mcjty.rftoolsbuilder.setup.Config;
import mcjty.rftoolsbuilder.setup.Registration;
import mcjty.rftoolsbuilder.shapes.ShapeDataManagerClient;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;

import static mcjty.lib.datagen.Dob.has;
import static mcjty.rftoolsbuilder.RFToolsBuilder.tab;
import static mcjty.rftoolsbuilder.setup.Registration.BLOCKS;
import static mcjty.rftoolsbuilder.setup.Registration.CONTAINERS;
import static mcjty.rftoolsbuilder.setup.Registration.ITEMS;
import static mcjty.rftoolsbuilder.setup.Registration.TILES;

public class ScannerModule implements IModule {

    public static final DeferredBlock<BaseBlock> SCANNER = BLOCKS.register("scanner", ScannerBlock::new);
    public static final DeferredItem<Item> SCANNER_ITEM = ITEMS.register("scanner", tab(() -> new BlockItem(SCANNER.get(), Registration.createStandardProperties())));
    public static final Supplier<BlockEntityType<ScannerTileEntity>> TYPE_SCANNER = TILES.register("scanner", () -> BlockEntityType.Builder.of(ScannerTileEntity::new, SCANNER.get()).build(null));
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_SCANNER = CONTAINERS.register("scanner", GenericContainer::createContainerType);

    public static final DeferredBlock<BaseBlock> COMPOSER = BLOCKS.register("composer", ComposerBlock::new);
    public static final DeferredItem<Item> COMPOSER_ITEM = ITEMS.register("composer", tab(() -> new BlockItem(COMPOSER.get(), Registration.createStandardProperties())));
    public static final Supplier<BlockEntityType<ComposerTileEntity>> TYPE_COMPOSER = TILES.register("composer", () -> BlockEntityType.Builder.of(ComposerTileEntity::new, COMPOSER.get()).build(null));
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_COMPOSER = CONTAINERS.register("composer", GenericContainer::createContainerType);

    public static final DeferredBlock<BaseBlock> PROJECTOR = BLOCKS.register("projector", ProjectorBlock::new);
    public static final DeferredItem<Item> PROJECTOR_ITEM = ITEMS.register("projector", tab(() -> new BlockItem(PROJECTOR.get(), Registration.createStandardProperties())));
    public static final Supplier<BlockEntityType<ProjectorTileEntity>> TYPE_PROJECTOR = TILES.register("projector", () -> BlockEntityType.Builder.of(ProjectorTileEntity::new, PROJECTOR.get()).build(null));
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_PROJECTOR = CONTAINERS.register("projector", GenericContainer::createContainerType);

    public ScannerModule(IEventBus bus) {
        bus.addListener(this::registerMenuScreens);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.register(new ShapeHandler());
    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(ShapeDataManagerClient::cleanupOldRenderers);
        ProjectorRenderer.register();
    }

    public void registerMenuScreens(RegisterMenuScreensEvent event) {
        GuiComposer.register(event);
        GuiProjector.register(event);
        GuiScanner.register(event);
    }

    @Override
    public void initConfig(IEventBus bus) {
        ScannerConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }

    @Override
    public void initDatagen(DataGen dataGen, HolderLookup.Provider provider) {
        dataGen.add(
                Dob.blockBuilder(SCANNER)
                        .ironPickaxeTags()
                        .standardLoot()
                        .parentedItem("block/scanner")
                        .blockState(p -> p.horizontalOrientedBlock(SCANNER.get(), p.frontBasedModel("scanner", p.modLoc("block/machinescanner"))))
                        .shaped(builder -> builder
                                        .define('M', VariousModule.MACHINE_FRAME.get())
                                        .define('q', Items.QUARTZ)
                                        .define('X', VariousModule.INFUSED_DIAMOND.get())
                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                                "XoX", "qMq", "XrX"),
                Dob.blockBuilder(COMPOSER)
                        .ironPickaxeTags()
                        .standardLoot()
                        .parentedItem("block/composer")
                        .blockState(p -> p.horizontalOrientedBlock(COMPOSER.get(), p.frontBasedModel("composer", p.modLoc("block/machinecomposer"))))
                        .shaped(builder -> builder
                                        .define('F', VariousModule.MACHINE_FRAME.get())
                                        .define('P', Items.PAPER)
                                        .define('Q', Items.BRICK)
                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                                "PQP", "QFQ", "PQP"),
                Dob.blockBuilder(PROJECTOR)
                        .ironPickaxeTags()
                        .standardLoot()
                        .parentedItem("block/projector")
                        .blockState(p -> p.horizontalOrientedBlock(PROJECTOR.get(), p.frontBasedModel("projector", p.modLoc("block/machineprojector"))))
                        .shaped(builder -> builder
                                        .define('F', VariousModule.MACHINE_FRAME.get())
                                        .define('X', VariousModule.INFUSED_DIAMOND.get())
                                        .define('E', Items.GLOWSTONE_DUST)
                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                                "XoX", "EFE", "XrX")
        );
    }
}
