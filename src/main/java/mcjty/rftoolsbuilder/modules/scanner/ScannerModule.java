package mcjty.rftoolsbuilder.modules.scanner;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.lib.setup.DeferredBlock;
import mcjty.lib.setup.DeferredItem;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ComposerBlock;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ComposerTileEntity;
import mcjty.rftoolsbuilder.modules.scanner.client.GuiComposer;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ScannerBlock;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ScannerTileEntity;
import mcjty.rftoolsbuilder.modules.scanner.client.GuiScanner;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ProjectorBlock;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ProjectorTileEntity;
import mcjty.rftoolsbuilder.modules.scanner.client.GuiProjector;
import mcjty.rftoolsbuilder.modules.scanner.client.ProjectorRenderer;
import mcjty.rftoolsbuilder.setup.Config;
import mcjty.rftoolsbuilder.setup.Registration;
import mcjty.rftoolsbuilder.shapes.ShapeDataManagerClient;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.function.Supplier;

import static mcjty.lib.datagen.DataGen.has;
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

    @Override
    public void init(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ShapeHandler());
    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiComposer.register();
            GuiProjector.register();
            GuiScanner.register();
        });
        MinecraftForge.EVENT_BUS.addListener(ShapeDataManagerClient::cleanupOldRenderers);
        MinecraftForge.EVENT_BUS.addListener(ShapeDataManagerClient::processPendingRenderPlanes);
        ProjectorRenderer.register();
    }

    @Override
    public void initConfig(IEventBus bus) {
        ScannerConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }

    @Override
    public void initDatagen(DataGen dataGen) {
        dataGen.add(
                Dob.blockBuilder(COMPOSER)
                        .ironPickaxeTags()
                        .standardLoot(TYPE_COMPOSER)
                        .parentedItem("block/composer")
                        .blockState(p -> p.horizontalOrientedBlock(COMPOSER.get(), p.models().getExistingFile(p.modLoc("block/composer"))))
                        .shaped(builder -> builder
                                        .define('F', VariousModule.MACHINE_FRAME.get())
                                        .define('P', Items.PAPER)
                                        .define('B', Items.BRICK)
                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                                "PBP", "BFB", "PBP"),
                Dob.blockBuilder(PROJECTOR)
                        .ironPickaxeTags()
                        .standardLoot(TYPE_PROJECTOR)
                        .parentedItem("block/projector")
                        .blockState(p -> p.horizontalOrientedBlock(PROJECTOR.get(), p.models().getExistingFile(p.modLoc("block/projector"))))
                        .shaped(builder -> builder
                                        .define('F', VariousModule.MACHINE_FRAME.get())
                                        .define('X', VariousModule.INFUSED_DIAMOND.get())
                                        .define('E', Items.GLOWSTONE_DUST)
                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                                "XoX", "EFE", "XrX")
        );
    }
}
