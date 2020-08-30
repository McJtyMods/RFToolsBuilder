package mcjty.rftoolsbuilder.modules.builder;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.blocks.SupportBlock;
import mcjty.rftoolsbuilder.modules.builder.client.BuilderRenderer;
import mcjty.rftoolsbuilder.modules.builder.client.GuiBuilder;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardType;
import mcjty.rftoolsbuilder.modules.builder.items.SuperHarvestingTool;
import mcjty.rftoolsbuilder.setup.Config;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ObjectHolder;

import static mcjty.rftoolsbuilder.setup.Registration.*;

public class BuilderModule implements IModule {
//    public static SpaceChamberBlock spaceChamberBlock;
//    public static SpaceChamberControllerBlock spaceChamberControllerBlock;
//    public static BaseBlock composerBlock;
//    public static BaseBlock scannerBlock;
//    public static BaseBlock remoteScannerBlock;
//    public static BaseBlock projectorBlock;
//    public static BaseBlock locatorBlock;

    public static final RegistryObject<SupportBlock> SUPPORT = BLOCKS.register("support_block", SupportBlock::new);

    public static final RegistryObject<BaseBlock> BUILDER = BLOCKS.register("builder", BuilderTileEntity::createBlock);
    public static final RegistryObject<Item> BUILDER_ITEM = ITEMS.register("builder", () -> new BlockItem(BUILDER.get(), Registration.createStandardProperties()));
    public static final RegistryObject<TileEntityType<BuilderTileEntity>> TYPE_BUILDER = TILES.register("builder", () -> TileEntityType.Builder.create(BuilderTileEntity::new, BUILDER.get()).build(null));
    public static final RegistryObject<ContainerType<GenericContainer>> CONTAINER_BUILDER = CONTAINERS.register("builder", GenericContainer::createContainerType);

    public static final RegistryObject<Item> SUPER_HARVESTING_TOOL = ITEMS.register("superharvestingtool", SuperHarvestingTool::new);

    public static final RegistryObject<ShapeCardItem> SHAPE_CARD_DEF = ITEMS.register("shape_card_def", () -> new ShapeCardItem(ShapeCardType.CARD_SHAPE));
    public static final RegistryObject<ShapeCardItem> SHAPE_CARD_LIQUID = ITEMS.register("shape_card_liquid", () -> new ShapeCardItem(ShapeCardType.CARD_PUMP_LIQUID));
    public static final RegistryObject<ShapeCardItem> SHAPE_CARD_PUMP = ITEMS.register("shape_card_pump", () -> new ShapeCardItem(ShapeCardType.CARD_PUMP));
    public static final RegistryObject<ShapeCardItem> SHAPE_CARD_PUMP_CLEAR = ITEMS.register("shape_card_pump_clear", () -> new ShapeCardItem(ShapeCardType.CARD_PUMP_CLEAR));
    public static final RegistryObject<ShapeCardItem> SHAPE_CARD_QUARRY = ITEMS.register("shape_card_quarry", () -> new ShapeCardItem(ShapeCardType.CARD_QUARRY));
    public static final RegistryObject<ShapeCardItem> SHAPE_CARD_QUARRY_CLEAR = ITEMS.register("shape_card_quarry_clear", () -> new ShapeCardItem(ShapeCardType.CARD_QUARRY_CLEAR));
    public static final RegistryObject<ShapeCardItem> SHAPE_CARD_QUARRY_CLEAR_FORTUNE = ITEMS.register("shape_card_quarry_clear_fortune", () -> new ShapeCardItem(ShapeCardType.CARD_QUARRY_CLEAR_FORTUNE));
    public static final RegistryObject<ShapeCardItem> SHAPE_CARD_QUARRY_CLEAR_SILK = ITEMS.register("shape_card_quarry_clear_silk", () -> new ShapeCardItem(ShapeCardType.CARD_QUARRY_CLEAR_SILK));
    public static final RegistryObject<ShapeCardItem> SHAPE_CARD_QUARRY_FORTUNE = ITEMS.register("shape_card_quarry_fortune", () -> new ShapeCardItem(ShapeCardType.CARD_QUARRY_FORTUNE));
    public static final RegistryObject<ShapeCardItem> SHAPE_CARD_QUARRY_SILK = ITEMS.register("shape_card_quarry_silk", () -> new ShapeCardItem(ShapeCardType.CARD_QUARRY_SILK));
    public static final RegistryObject<ShapeCardItem> SHAPE_CARD_VOID = ITEMS.register("shape_card_void", () -> new ShapeCardItem(ShapeCardType.CARD_VOID));

    //    @ObjectHolder("rftools:composer")
//    public static TileEntityType<?> TYPE_COMPOSER;

//    @ObjectHolder("rftools:scanner")
//    public static TileEntityType<?> TYPE_SCANNER;

//    @ObjectHolder("rftools:locator")
//    public static TileEntityType<?> TYPE_LOCATOR;

//    @ObjectHolder("rftools:projector")
//    public static TileEntityType<?> TYPE_PROJECTOR;

//    @ObjectHolder("rftools:space_chamber")
//    public static TileEntityType<?> TYPE_SPACE_CHAMBER;

//    @ObjectHolder("rftools:modifier")
//    public static ContainerType<GenericContainer> CONTAINER_MODIFIER;

    //    public static SpaceChamberCardItem spaceChamberCardItem;
    @ObjectHolder("rftoolsbuilder:space_chamber_card")
    public static Item SPACE_CHAMBER_CARD;    // @todo 1.14


    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        DeferredWorkQueue.runLater(() -> {
            GenericGuiContainer.register(CONTAINER_BUILDER.get(), GuiBuilder::new);
        });
        BuilderRenderer.register();
        RenderTypeLookup.setRenderLayer(SUPPORT.get(), RenderType.getTranslucent());
    }

    @Override
    public void initConfig() {
        BuilderConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }
}
