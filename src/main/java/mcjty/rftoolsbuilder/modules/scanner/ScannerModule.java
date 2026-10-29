package mcjty.rftoolsbuilder.modules.scanner;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import java.util.function.Supplier;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbase.modules.various.VariousModule;
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
import mcjty.rftoolsbuilder.shapes.ShapeDataManagerClient;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import static mcjty.lib.datagen.Dob.has;
import static mcjty.lib.setup.Registration.ITEM_INVENTORY;
import static mcjty.rftoolsbuilder.setup.Registration.CONTAINERS;
import static mcjty.rftoolsbuilder.setup.Registration.RBLOCKS;

public class ScannerModule implements IModule {

    public static final RBlock<ScannerBlock, BlockItem, ScannerTileEntity> SCANNER = RBLOCKS.registerBlock("scanner",
            ScannerTileEntity.class,
            ScannerBlock::new,
            (block, properties) -> new BaseBlockItem(block.get(), properties.useBlockDescriptionPrefix()),
            ScannerTileEntity::new);
    public static final Supplier<BlockEntityType<ScannerTileEntity>> TYPE_SCANNER = SCANNER.be();
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_SCANNER = CONTAINERS.register("scanner", GenericContainer::createContainerType);

    public static final RBlock<ComposerBlock, BlockItem, ComposerTileEntity> COMPOSER = RBLOCKS.registerBlock("composer",
            ComposerTileEntity.class,
            ComposerBlock::new,
            (block, properties) -> new BaseBlockItem(block.get(), properties.useBlockDescriptionPrefix()),
            ComposerTileEntity::new);
    public static final Supplier<BlockEntityType<ComposerTileEntity>> TYPE_COMPOSER = COMPOSER.be();
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_COMPOSER = CONTAINERS.register("composer", GenericContainer::createContainerType);

    public static final RBlock<ProjectorBlock, BlockItem, ProjectorTileEntity> PROJECTOR = RBLOCKS.registerBlock("projector",
            ProjectorTileEntity.class,
            ProjectorBlock::new,
            (block, properties) -> new BaseBlockItem(block.get(), properties.useBlockDescriptionPrefix()),
            ProjectorTileEntity::new);
    public static final Supplier<BlockEntityType<ProjectorTileEntity>> TYPE_PROJECTOR = PROJECTOR.be();
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_PROJECTOR = CONTAINERS.register("projector", GenericContainer::createContainerType);

    @Override
    public void init() {
        ShapeHandler.register();
    }

    @Override
    public void initClient() {
        registerMenuScreens();
        ClientTickEvents.END_CLIENT_TICK.register(client -> ShapeDataManagerClient.cleanupOldRenderers());
        ProjectorRenderer.register();
    }

    public void registerMenuScreens() {
        GuiComposer.register();
        GuiProjector.register();
        GuiScanner.register();
    }

    @Override
    public void initConfig() {
        ScannerConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }

    @Override
    public void initDatagen(DataGen dataGen, HolderLookup.Provider provider) {
        dataGen.add(
                Dob.blockBuilder(SCANNER)
                        .ironPickaxeTags()
                        .standardLoot(ITEM_INVENTORY.get())
                        .parentedItem("block/scanner")
                        .blockState(p -> p.horizontalOrientedBlock(SCANNER.block().get(), p.topBasedModel("scanner", p.modLoc("block/machinescanner"))))
                        .shaped(builder -> builder
                                        .define('M', VariousModule.MACHINE_FRAME.get())
                                        .define('q', Items.QUARTZ)
                                        .define('X', VariousModule.INFUSED_DIAMOND.get())
                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                                "XoX", "qMq", "XrX"),
                Dob.blockBuilder(COMPOSER)
                        .ironPickaxeTags()
                        .standardLoot(ITEM_INVENTORY.get())
                        .parentedItem("block/composer")
                        .blockState(p -> p.horizontalOrientedBlock(COMPOSER.block().get(), p.topBasedModel("composer", p.modLoc("block/machinecomposer"))))
                        .shaped(builder -> builder
                                        .define('F', VariousModule.MACHINE_FRAME.get())
                                        .define('P', Items.PAPER)
                                        .define('Q', Items.BRICK)
                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                                "PQP", "QFQ", "PQP"),
                Dob.blockBuilder(PROJECTOR)
                        .ironPickaxeTags()
                        .standardLoot(ITEM_INVENTORY.get())
                        .parentedItem("block/projector")
                        .blockState(p -> p.horizontalOrientedBlock(PROJECTOR.block().get(), p.topBasedModel("projector", p.modLoc("block/machineprojector"))))
                        .shaped(builder -> builder
                                        .define('F', VariousModule.MACHINE_FRAME.get())
                                        .define('X', VariousModule.INFUSED_DIAMOND.get())
                                        .define('E', Items.GLOWSTONE_DUST)
                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                                "XoX", "EFE", "XrX")
        );
    }
}
