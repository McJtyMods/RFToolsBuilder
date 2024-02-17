package mcjty.rftoolsbuilder.modules.builder;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.lib.setup.DeferredBlock;
import mcjty.lib.setup.DeferredItem;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.blocks.SpaceChamberControllerBlock;
import mcjty.rftoolsbuilder.modules.builder.blocks.SpaceChamberControllerTileEntity;
import mcjty.rftoolsbuilder.modules.builder.blocks.SupportBlock;
import mcjty.rftoolsbuilder.modules.builder.client.BuilderRenderer;
import mcjty.rftoolsbuilder.modules.builder.client.GuiBuilder;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardType;
import mcjty.rftoolsbuilder.modules.builder.items.SpaceChamberCardItem;
import mcjty.rftoolsbuilder.modules.builder.items.SuperHarvestingTool;
import mcjty.rftoolsbuilder.setup.Config;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.ArrayList;
import java.util.function.Supplier;

import static mcjty.lib.datagen.DataGen.has;
import static mcjty.rftoolsbuilder.RFToolsBuilder.tab;
import static mcjty.rftoolsbuilder.setup.Registration.*;

public class BuilderModule implements IModule {

    public static final DeferredBlock<SupportBlock> SUPPORT = BLOCKS.register("support_block", SupportBlock::new);

    public static final DeferredBlock<BaseBlock> SPACE_CHAMBER = BLOCKS.register("space_chamber", () -> new BaseBlock(new BlockBuilder()
        .properties(BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.METAL)
                .noOcclusion())));
    public static final DeferredItem<Item> SPACE_CHAMBER_ITEM = ITEMS.register("space_chamber", tab(() -> new BlockItem(SPACE_CHAMBER.get(), Registration.createStandardProperties())));

    public static final DeferredBlock<SpaceChamberControllerBlock> SPACE_CHAMBER_CONTROLLER = BLOCKS.register("space_chamber_controller", SpaceChamberControllerBlock::new);
    public static final DeferredItem<Item> SPACE_CHAMBER_CONTROLLER_ITEM = ITEMS.register("space_chamber_controller", tab(() -> new BlockItem(SPACE_CHAMBER_CONTROLLER.get(), Registration.createStandardProperties())));
    public static final Supplier<BlockEntityType<SpaceChamberControllerTileEntity>> TYPE_SPACE_CHAMBER_CONTROLLER = TILES.register("space_chamber_controller", () -> BlockEntityType.Builder.of(SpaceChamberControllerTileEntity::new, SPACE_CHAMBER_CONTROLLER.get()).build(null));

    public static final DeferredItem<Item> SPACE_CHAMBER_CARD = ITEMS.register("space_chamber_card", tab(SpaceChamberCardItem::new));

    public static final DeferredBlock<BaseBlock> BUILDER = BLOCKS.register("builder", BuilderTileEntity::createBlock);
    public static final DeferredItem<Item> BUILDER_ITEM = ITEMS.register("builder", tab(() -> new BlockItem(BUILDER.get(), Registration.createStandardProperties())));
    public static final Supplier<BlockEntityType<BuilderTileEntity>> TYPE_BUILDER = TILES.register("builder", () -> BlockEntityType.Builder.of(BuilderTileEntity::new, BUILDER.get()).build(null));
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_BUILDER = CONTAINERS.register("builder", GenericContainer::createContainerType);

    public static final DeferredItem<Item> SUPER_HARVESTING_TOOL = ITEMS.register("superharvestingtool", SuperHarvestingTool::new);

    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_DEF = ITEMS.register("shape_card_def", tab(() -> new ShapeCardItem(ShapeCardType.CARD_SHAPE)));
    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_LIQUID = ITEMS.register("shape_card_liquid", tab(() -> new ShapeCardItem(ShapeCardType.CARD_PUMP_LIQUID)));
    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_PUMP = ITEMS.register("shape_card_pump", tab(() -> new ShapeCardItem(ShapeCardType.CARD_PUMP)));
    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_PUMP_CLEAR = ITEMS.register("shape_card_pump_clear", tab(() -> new ShapeCardItem(ShapeCardType.CARD_PUMP_CLEAR)));
    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_QUARRY = ITEMS.register("shape_card_quarry", tab(() -> new ShapeCardItem(ShapeCardType.CARD_QUARRY)));
    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_QUARRY_CLEAR = ITEMS.register("shape_card_quarry_clear", tab(() -> new ShapeCardItem(ShapeCardType.CARD_QUARRY_CLEAR)));
    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_QUARRY_CLEAR_FORTUNE = ITEMS.register("shape_card_quarry_clear_fortune", tab(() -> new ShapeCardItem(ShapeCardType.CARD_QUARRY_CLEAR_FORTUNE)));
    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_QUARRY_CLEAR_SILK = ITEMS.register("shape_card_quarry_clear_silk", tab(() -> new ShapeCardItem(ShapeCardType.CARD_QUARRY_CLEAR_SILK)));
    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_QUARRY_FORTUNE = ITEMS.register("shape_card_quarry_fortune", tab(() -> new ShapeCardItem(ShapeCardType.CARD_QUARRY_FORTUNE)));
    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_QUARRY_SILK = ITEMS.register("shape_card_quarry_silk", tab(() -> new ShapeCardItem(ShapeCardType.CARD_QUARRY_SILK)));
    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_VOID = ITEMS.register("shape_card_void", tab(() -> new ShapeCardItem(ShapeCardType.CARD_VOID)));

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiBuilder.register();
        });
        BuilderRenderer.register();
    }

    @Override
    public void initConfig(IEventBus bus) {
        BuilderConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }

    public static void x(Integer... b) {

    }

    @Override
    public void initDatagen(DataGen dataGen) {

        ArrayList<Integer> a = new ArrayList<>();
        a.add(3);
        a.add(5);
        x(a.toArray(new Integer[0]));

        dataGen.add(
                Dob.blockBuilder(BUILDER)
                        .ironPickaxeTags()
                        .standardLoot(TYPE_BUILDER)
                        .parentedItem("block/builder")
                        .blockState(p -> p.horizontalOrientedBlock(BUILDER.get(), p.frontBasedModel("builder", p.modLoc("block/machinebuilder"))))
                        .shaped(builder -> builder
                                        .define('F', VariousModule.MACHINE_FRAME.get())
                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                                "BoB", "rFr", "BrB"),
                Dob.blockBuilder(SPACE_CHAMBER)
                        .ironPickaxeTags()
                        .simpleLoot()
                        .parentedItem("block/space_chamber")
                        .blockState(p -> p.singleTextureBlock(SPACE_CHAMBER.get(), "space_chamber", "block/machinespacechamber"))
                        .shaped(builder -> builder
                                        .define('F', VariousModule.MACHINE_FRAME.get())
                                        .define('x', Tags.Items.DYES_BLUE)
                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                                "xGx", "GFG", "xGx"),
                Dob.blockBuilder(SPACE_CHAMBER_CONTROLLER)
                        .ironPickaxeTags()
                        .standardLoot(TYPE_SPACE_CHAMBER_CONTROLLER)
                        .parentedItem("block/space_chamber_controller")
                        .blockState(p -> p.singleTextureBlock(SPACE_CHAMBER_CONTROLLER.get(), "space_chamber_controller", "block/machinespacechambercontroller"))
                        .shaped(builder -> builder
                                        .define('X', BuilderModule.SPACE_CHAMBER.get())
                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                                " o ", "TXT", " o "),
                Dob.blockBuilder(SUPPORT)
                        .ironPickaxeTags()
                        .blockState(DataGenHelper::generateSupportModel),
                Dob.itemBuilder(SPACE_CHAMBER_CARD)
                        .generatedItem("item/spacechambercarditem")
                        .shaped(builder -> builder
                                        .unlockedBy("glass", has(Items.GLASS)),
                                " B ", "rir", " B "),
                Dob.itemBuilder(SHAPE_CARD_DEF)
                        .generatedItem("item/shapecarditem")
                        .shaped(builder -> builder
                                        .unlockedBy("iron_ingot", has(Items.IRON_INGOT)),
                                "pBp", "rir", "pBp"),
                Dob.itemBuilder(SHAPE_CARD_LIQUID)
                        .generatedItem("item/shapecardliquiditem")
                        .shapedNBT(builder -> builder
                                        .define('M', SHAPE_CARD_DEF.get())
                                        .unlockedBy("shape_card", has(SHAPE_CARD_DEF.get())),
                                "rWr", "iMi", "rLr"),
                Dob.itemBuilder(SHAPE_CARD_PUMP)
                        .generatedItem("item/shapecardpumpitem")
                        .shapedNBT(builder -> builder
                                        .define('M', SHAPE_CARD_DEF.get())
                                        .unlockedBy("shape_card", has(SHAPE_CARD_DEF.get())),
                                "rWr", "bMb", "rLr")
                        .shapedNBT("shape_card_pump_dirt", builder -> builder
                                        .define('M', SHAPE_CARD_PUMP_CLEAR.get())
                                        .unlockedBy("shape_card", has(SHAPE_CARD_DEF.get())),
                                "DDD", "DMD", "DDD"),
                Dob.itemBuilder(SHAPE_CARD_PUMP_CLEAR)
                        .generatedItem("item/shapecardpumpclearitem")
                        .shapedNBT(builder -> builder
                                        .define('M', SHAPE_CARD_PUMP.get())
                                        .unlockedBy("shape_card", has(SHAPE_CARD_DEF.get())),
                                "GGG", "GMG", "GGG"),
                Dob.itemBuilder(SHAPE_CARD_QUARRY)
                        .generatedItem("item/shapecardquarryitem")
                        .shapedNBT(builder -> builder
                                        .define('P', Items.DIAMOND_PICKAXE)
                                        .define('S', Items.DIAMOND_SHOVEL)
                                        .define('M', SHAPE_CARD_DEF.get())
                                        .unlockedBy("shape_card", has(SHAPE_CARD_DEF.get())),
                                "rPr", "iMi", "rSr")
                        .shapedNBT("shape_card_quarry_dirt", builder -> builder
                                        .define('M', SHAPE_CARD_QUARRY_CLEAR.get())
                                        .unlockedBy("shape_card", has(SHAPE_CARD_DEF.get())),
                                "DDD", "DMD", "DDD"),
                Dob.itemBuilder(SHAPE_CARD_QUARRY_CLEAR)
                        .generatedItem("item/shapecardcquarryitem")
                        .shapedNBT(builder -> builder
                                        .define('M', SHAPE_CARD_QUARRY.get())
                                        .unlockedBy("shape_card", has(SHAPE_CARD_DEF.get())),
                                "GGG", "GMG", "GGG"),
                Dob.itemBuilder(SHAPE_CARD_QUARRY_CLEAR_FORTUNE)
                        .generatedItem("item/shapecardcfortuneitem")
                        .shapedNBT(builder -> builder
                                        .define('M', SHAPE_CARD_QUARRY_FORTUNE.get())
                                        .unlockedBy("shape_card", has(SHAPE_CARD_DEF.get())),
                                "GGG", "GMG", "GGG"),
                Dob.itemBuilder(SHAPE_CARD_QUARRY_CLEAR_SILK)
                        .generatedItem("item/shapecardcsilkitem")
                        .shapedNBT(builder -> builder
                                        .define('M', SHAPE_CARD_QUARRY_SILK.get())
                                        .unlockedBy("shape_card", has(SHAPE_CARD_DEF.get())),
                                "GGG", "GMG", "GGG"),
                Dob.itemBuilder(SHAPE_CARD_QUARRY_FORTUNE)
                        .generatedItem("item/shapecardfortuneitem")
                        .shapedNBT(builder -> builder
                                        .define('s', VariousModule.DIMENSIONALSHARD.get())
                                        .define('n', Items.GHAST_TEAR)
                                        .define('M', SHAPE_CARD_QUARRY.get())
                                        .unlockedBy("shape_card", has(SHAPE_CARD_DEF.get())),
                                "sns", "eMd", "srs")
                        .shapedNBT("shape_card_quarry_fortune_dirt", builder -> builder
                                        .define('M', SHAPE_CARD_QUARRY_CLEAR_FORTUNE.get())
                                        .unlockedBy("shape_card", has(SHAPE_CARD_DEF.get())),
                                "DDD", "DMD", "DDD"),
                Dob.itemBuilder(SHAPE_CARD_QUARRY_SILK)
                        .generatedItem("item/shapecardsilkitem")
                        .shapedNBT(builder -> builder
                                        .define('s', VariousModule.DIMENSIONALSHARD.get())
                                        .define('n', Items.NETHER_STAR)
                                        .define('M', SHAPE_CARD_QUARRY.get())
                                        .unlockedBy("shape_card", has(SHAPE_CARD_DEF.get())),
                                "sns", "dMd", "sds")
                        .shapedNBT("shape_card_quarry_silk_dirt", builder -> builder
                                        .define('M', SHAPE_CARD_QUARRY_CLEAR_SILK.get())
                                        .unlockedBy("shape_card", has(SHAPE_CARD_DEF.get())),
                                "DDD", "DMD", "DDD"),
                Dob.itemBuilder(SHAPE_CARD_VOID)
                        .generatedItem("item/shapecardvoiditem")
                        .shapedNBT(builder -> builder
                                        .define('x', Tags.Items.DYES_BLACK)
                                        .define('M', SHAPE_CARD_DEF.get())
                                        .unlockedBy("shape_card", has(SHAPE_CARD_DEF.get())),
                                "xOx", "OMO", "xOx")
        );
    }
}
