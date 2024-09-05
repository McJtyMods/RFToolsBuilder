package mcjty.rftoolsbuilder.modules.scanner;

import mcjty.lib.datagen.DataGen;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbuilder.modules.scanner.client.ProjectorRenderer;
import mcjty.rftoolsbuilder.setup.Config;
import mcjty.rftoolsbuilder.shapes.ShapeDataManagerClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ScannerModule implements IModule {

//    public static final DeferredBlock<BaseBlock> PROJECTOR = BLOCKS.register("projector", ProjectorTileEntity::createBlock);
//    public static final DeferredItem<Item> PROJECTOR_ITEM = ITEMS.register("projector", tab(() -> new BlockItem(PROJECTOR.get(), Registration.createStandardProperties())));
//    public static final Supplier<BlockEntityType<ProjectorTileEntity>> TYPE_PROJECTOR = TILES.register("projector", () -> BlockEntityType.Builder.of(ProjectorTileEntity::new, PROJECTOR.get()).build(null));
//
//    public static final DeferredBlock<BaseBlock> SCANNER = BLOCKS.register("scanner", ScannerTileEntity::createBlock);
//    public static final DeferredItem<Item> SCANNER_ITEM = ITEMS.register("scanner", tab(() -> new BlockItem(SCANNER.get(), Registration.createStandardProperties())));
//    public static final Supplier<BlockEntityType<ScannerTileEntity>> TYPE_SCANNER = TILES.register("scanner", () -> BlockEntityType.Builder.of(ScannerTileEntity::new, SCANNER.get()).build(null));

    @Override
    public void init(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ShapeHandler());
    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(ShapeDataManagerClient::cleanupOldRenderers);
        ProjectorRenderer.register();
    }

    @Override
    public void initConfig(IEventBus bus) {
        ScannerConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }

    @Override
    public void initDatagen(DataGen dataGen) {
//        dataGen.add(
//                Dob.blockBuilder(PROJECTOR)
//                        .ironPickaxeTags()
//                        .standardLoot(TYPE_PROJECTOR)
//                        .parentedItem("block/projector")
//                        .blockState(p -> p.horizontalOrientedBlock(PROJECTOR.get(), p.frontBasedModel("projector", p.modLoc("block/machineprojector"))))
//                        .shaped(builder -> builder
//                                        .define('F', VariousModule.MACHINE_FRAME.get())
//                                        .define('X', VariousModule.INFUSED_DIAMOND.get())
//                                        .define('E', Items.GLOWSTONE_DUST)
//                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
//                                "XoX", "EFE", "XrX"),
//                Dob.blockBuilder(SCANNER)
//                        .ironPickaxeTags()
//                        .standardLoot(TYPE_SCANNER)
//                        .parentedItem("block/scanner")
//                        .blockState(p -> p.horizontalOrientedBlock(SCANNER.get(), p.frontBasedModel("scanner", p.modLoc("block/machinescanner"))))
//                        .shaped(builder -> builder
//                                        .define('F', VariousModule.MACHINE_FRAME.get())
//                                        .define('X', VariousModule.INFUSED_DIAMOND.get())
//                                        .define('E', Items.QUARTZ)
//                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
//                                "XoX", "EFE", "XrX")
//        );
    }
}
