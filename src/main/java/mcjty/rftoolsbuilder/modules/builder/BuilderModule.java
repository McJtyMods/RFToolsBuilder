package mcjty.rftoolsbuilder.modules.builder;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.blocks.SpaceChamberControllerBlock;
import mcjty.rftoolsbuilder.modules.builder.blocks.SpaceChamberControllerTileEntity;
import mcjty.rftoolsbuilder.modules.builder.blocks.SupportBlock;
import mcjty.rftoolsbuilder.modules.builder.client.BuilderRenderer;
import mcjty.rftoolsbuilder.modules.builder.client.ClientSetup;
import mcjty.rftoolsbuilder.modules.builder.client.GuiBuilder;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardType;
import mcjty.rftoolsbuilder.modules.builder.items.SpaceChamberCardItem;
import mcjty.rftoolsbuilder.modules.builder.items.SuperHarvestingTool;
import mcjty.rftoolsbuilder.setup.Config;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static mcjty.rftoolsbuilder.setup.Registration.*;

public class BuilderModule implements IModule {

    public static final RegistryObject<SupportBlock> SUPPORT = BLOCKS.register("support_block", SupportBlock::new);

    public static final RegistryObject<BaseBlock> SPACE_CHAMBER = BLOCKS.register("space_chamber", () -> new BaseBlock(new BlockBuilder()
        .properties(BlockBehaviour.Properties.of(Material.METAL)
                .strength(2.0f)
                .sound(SoundType.METAL)
                .noOcclusion())));
    public static final RegistryObject<Item> SPACE_CHAMBER_ITEM = ITEMS.register("space_chamber", () -> new BlockItem(SPACE_CHAMBER.get(), Registration.createStandardProperties()));

    public static final RegistryObject<SpaceChamberControllerBlock> SPACE_CHAMBER_CONTROLLER = BLOCKS.register("space_chamber_controller", SpaceChamberControllerBlock::new);
    public static final RegistryObject<Item> SPACE_CHAMBER_CONTROLLER_ITEM = ITEMS.register("space_chamber_controller", () -> new BlockItem(SPACE_CHAMBER_CONTROLLER.get(), Registration.createStandardProperties()));
    public static final RegistryObject<BlockEntityType<SpaceChamberControllerTileEntity>> TYPE_SPACE_CHAMBER_CONTROLLER = TILES.register("space_chamber_controller", () -> BlockEntityType.Builder.of(SpaceChamberControllerTileEntity::new, SPACE_CHAMBER_CONTROLLER.get()).build(null));

    public static final RegistryObject<Item> SPACE_CHAMBER_CARD = ITEMS.register("space_chamber_card", SpaceChamberCardItem::new);

    public static final RegistryObject<BaseBlock> BUILDER = BLOCKS.register("builder", BuilderTileEntity::createBlock);
    public static final RegistryObject<Item> BUILDER_ITEM = ITEMS.register("builder", () -> new BlockItem(BUILDER.get(), Registration.createStandardProperties()));
    public static final RegistryObject<BlockEntityType<BuilderTileEntity>> TYPE_BUILDER = TILES.register("builder", () -> BlockEntityType.Builder.of(BuilderTileEntity::new, BUILDER.get()).build(null));
    public static final RegistryObject<MenuType<GenericContainer>> CONTAINER_BUILDER = CONTAINERS.register("builder", GenericContainer::createContainerType);

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


    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiBuilder.register();
        });
        BuilderRenderer.register();
        ClientSetup.initClient();
    }

    @Override
    public void initConfig() {
        BuilderConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }
}
