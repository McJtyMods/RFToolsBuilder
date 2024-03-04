package mcjty.rftoolsbuilder.modules.shield;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.lib.setup.DeferredBlock;
import mcjty.lib.setup.DeferredItem;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.shield.blocks.*;
import mcjty.rftoolsbuilder.modules.shield.client.GuiShield;
import mcjty.rftoolsbuilder.modules.shield.client.ShieldModelLoader;
import mcjty.rftoolsbuilder.setup.Config;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.api.distmarker.Dist;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static mcjty.lib.datagen.DataGen.has;
import static mcjty.rftoolsbuilder.RFToolsBuilder.tab;
import static mcjty.rftoolsbuilder.setup.Registration.*;


public class ShieldModule implements IModule {

    public static final DeferredBlock<BaseBlock> SHIELD_BLOCK1 = BLOCKS.register("shield_block1", () -> new ShieldProjectorBlock(ShieldModule::createProjector1, ShieldConfiguration.maxShieldSize));
    public static final DeferredBlock<BaseBlock> SHIELD_BLOCK2 = BLOCKS.register("shield_block2", () -> new ShieldProjectorBlock(ShieldModule::createProjector2, () -> ShieldConfiguration.maxShieldSize.get() * 4));
    public static final DeferredBlock<BaseBlock> SHIELD_BLOCK3 = BLOCKS.register("shield_block3", () -> new ShieldProjectorBlock(ShieldModule::createProjector3, () -> ShieldConfiguration.maxShieldSize.get() * 16));
    public static final DeferredBlock<BaseBlock> SHIELD_BLOCK4 = BLOCKS.register("shield_block4", () -> new ShieldProjectorBlock(ShieldModule::createProjector4, () -> ShieldConfiguration.maxShieldSize.get() * 128));
    public static final DeferredItem<Item> SHIELD_BLOCK1_ITEM = ITEMS.register("shield_block1", tab(() -> new BlockItem(SHIELD_BLOCK1.get(), Registration.createStandardProperties())));
    public static final DeferredItem<Item> SHIELD_BLOCK2_ITEM = ITEMS.register("shield_block2", tab(() -> new BlockItem(SHIELD_BLOCK2.get(), Registration.createStandardProperties())));
    public static final DeferredItem<Item> SHIELD_BLOCK3_ITEM = ITEMS.register("shield_block3", tab(() -> new BlockItem(SHIELD_BLOCK3.get(), Registration.createStandardProperties())));
    public static final DeferredItem<Item> SHIELD_BLOCK4_ITEM = ITEMS.register("shield_block4", tab(() -> new BlockItem(SHIELD_BLOCK4.get(), Registration.createStandardProperties())));
    public static final Supplier<BlockEntityType<?>> TYPE_SHIELD_BLOCK1 = TILES.register("shield_block1", () -> BlockEntityType.Builder.of(ShieldModule::createProjector1, SHIELD_BLOCK1.get()).build(null));
    public static final Supplier<BlockEntityType<?>> TYPE_SHIELD_BLOCK2 = TILES.register("shield_block2", () -> BlockEntityType.Builder.of(ShieldModule::createProjector2, SHIELD_BLOCK2.get()).build(null));
    public static final Supplier<BlockEntityType<?>> TYPE_SHIELD_BLOCK3 = TILES.register("shield_block3", () -> BlockEntityType.Builder.of(ShieldModule::createProjector3, SHIELD_BLOCK3.get()).build(null));
    public static final Supplier<BlockEntityType<?>> TYPE_SHIELD_BLOCK4 = TILES.register("shield_block4", () -> BlockEntityType.Builder.of(ShieldModule::createProjector4, SHIELD_BLOCK4.get()).build(null));
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_SHIELD = CONTAINERS.register("shield", GenericContainer::createContainerType);

    public static final DeferredBlock<ShieldTemplateBlock> TEMPLATE_BLUE = BLOCKS.register("blue_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.BLUE));
    public static final DeferredBlock<ShieldTemplateBlock> TEMPLATE_RED = BLOCKS.register("red_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.RED));
    public static final DeferredBlock<ShieldTemplateBlock> TEMPLATE_GREEN = BLOCKS.register("green_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.GREEN));
    public static final DeferredBlock<ShieldTemplateBlock> TEMPLATE_YELLOW = BLOCKS.register("yellow_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.YELLOW));

    public static final DeferredItem<Item> TEMPLATE_BLUE_ITEM = ITEMS.register("blue_shield_template_block", tab(() -> new BlockItem(TEMPLATE_BLUE.get(), Registration.createStandardProperties())));
    public static final DeferredItem<Item> TEMPLATE_RED_ITEM = ITEMS.register("red_shield_template_block", tab(() -> new BlockItem(TEMPLATE_RED.get(), Registration.createStandardProperties())));
    public static final DeferredItem<Item> TEMPLATE_GREEN_ITEM = ITEMS.register("green_shield_template_block", tab(() -> new BlockItem(TEMPLATE_GREEN.get(), Registration.createStandardProperties())));
    public static final DeferredItem<Item> TEMPLATE_YELLOW_ITEM = ITEMS.register("yellow_shield_template_block", tab(() -> new BlockItem(TEMPLATE_YELLOW.get(), Registration.createStandardProperties())));

    public static final DeferredBlock<ShieldingBlock> SHIELDING_SOLID = BLOCKS.register("shielding_solid", ShieldingBlock::new);
    public static final DeferredBlock<ShieldingBlock> SHIELDING_TRANSLUCENT = BLOCKS.register("shielding_translucent", ShieldingBlock::new);
    public static final DeferredBlock<ShieldingBlock> SHIELDING_CUTOUT = BLOCKS.register("shielding_cutout", ShieldingBlock::new);
    public static final Supplier<BlockEntityType<?>> TYPE_SHIELDING = TILES.register("shielding", () -> BlockEntityType.Builder.of(ShieldingTileEntity::new,
            SHIELDING_SOLID.get(), SHIELDING_TRANSLUCENT.get(), SHIELDING_CUTOUT.get()).build(null));

    @Nonnull
    public static ShieldProjectorTileEntity createProjector1(BlockPos pos, BlockState state) {
        return new ShieldProjectorTileEntity(TYPE_SHIELD_BLOCK1.get(), pos, state, ShieldConfiguration.maxShieldSize.get(), ShieldConfiguration.MAXENERGY.get(), ShieldConfiguration.RECEIVEPERTICK.get());
    }

    @Nonnull
    public static ShieldProjectorTileEntity createProjector2(BlockPos pos, BlockState state) {
        return new ShieldProjectorTileEntity(TYPE_SHIELD_BLOCK2.get(), pos, state, ShieldConfiguration.maxShieldSize.get() * 4, ShieldConfiguration.MAXENERGY.get(), ShieldConfiguration.RECEIVEPERTICK.get());
    }

    @Nonnull
    public static ShieldProjectorTileEntity createProjector3(BlockPos pos, BlockState state) {
        return new ShieldProjectorTileEntity(TYPE_SHIELD_BLOCK3.get(), pos, state, ShieldConfiguration.maxShieldSize.get() * 16, ShieldConfiguration.MAXENERGY.get() * 3, ShieldConfiguration.RECEIVEPERTICK.get() * 2)
                .setDamageFactor(4.0f)
                .setCostFactor(2.0f);
    }

    @Nonnull
    public static ShieldProjectorTileEntity createProjector4(BlockPos pos, BlockState state) {
        return new ShieldProjectorTileEntity(TYPE_SHIELD_BLOCK4.get(), pos, state, ShieldConfiguration.maxShieldSize.get() * 128, ShieldConfiguration.MAXENERGY.get() * 6, ShieldConfiguration.RECEIVEPERTICK.get() * 6)
                .setDamageFactor(4.0f)
                .setCostFactor(2.0f);
    }

//
//    @SideOnly(Side.CLIENT)
//    public static void initClientPost() {
//        if (!ShieldConfiguration.disableShieldBlocksToUncorruptWorld.get()) {
//            solidShieldBlock.initBlockColors();
//            noTickSolidShieldBlock.initBlockColors();
//            solidShieldBlockOpaque.initBlockColors();
//            noTickSolidShieldBlockOpaque.initBlockColors();
//        }
//    }
//
//    @SideOnly(Side.CLIENT)
//    public static void initColorHandlers(BlockColors blockColors) {
//        if (!ShieldConfiguration.disableShieldBlocksToUncorruptWorld.get()) {
//            camoShieldBlock.initColorHandler(blockColors);
//            noTickCamoShieldBlock.initColorHandler(blockColors);
//            camoShieldBlockOpaque.initColorHandler(blockColors);
//            noTickCamoShieldBlockOpaque.initColorHandler(blockColors);
//        }
//    }


    public ShieldModule(IEventBus bus, Dist dist) {
        bus.addListener(ShieldModelLoader::register);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiShield.register();
        });
    }

    @Override
    public void initConfig(IEventBus bus) {
        ShieldConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }

    @Override
    public void initDatagen(DataGen dataGen) {
        dataGen.add(
                Dob.blockBuilder(TEMPLATE_BLUE)
                        .parentedItem("block/blue_shield_template")
                        .simpleLoot()
                        .blockState(p -> p.singleTextureBlockC(TEMPLATE_BLUE.get(), "blue_shield_template", "block/shieldtemplate", builder -> builder.renderType("cutout")))
                        .shaped(builder -> builder
                                        .define('w', ItemTags.WOOL)
                                        .define('l', Tags.Items.DYES_BLUE)
                                        .unlockedBy("glass", has(Items.GLASS)),
                                8,
                                "www", "lGl", "www"),
                Dob.blockBuilder(TEMPLATE_RED)
                        .parentedItem("block/red_shield_template")
                        .simpleLoot()
                        .blockState(p -> p.singleTextureBlockC(TEMPLATE_RED.get(), "red_shield_template", "block/shieldtemplate1", builder -> builder.renderType("cutout")))
                        .shaped(builder -> builder
                                        .define('w', ItemTags.WOOL)
                                        .define('l', Tags.Items.DYES_RED)
                                        .unlockedBy("glass", has(Items.GLASS)),
                                8,
                                "www", "lGl", "www"),
                Dob.blockBuilder(TEMPLATE_GREEN)
                        .parentedItem("block/green_shield_template")
                        .simpleLoot()
                        .blockState(p -> p.singleTextureBlockC(TEMPLATE_GREEN.get(), "green_shield_template", "block/shieldtemplate2", builder -> builder.renderType("cutout")))
                        .shaped(builder -> builder
                                        .define('w', ItemTags.WOOL)
                                        .define('l', Tags.Items.DYES_GREEN)
                                        .unlockedBy("glass", has(Items.GLASS)),
                                8,
                                "www", "lGl", "www"),
                Dob.blockBuilder(TEMPLATE_YELLOW)
                        .parentedItem("block/yellow_shield_template")
                        .simpleLoot()
                        .blockState(p -> p.singleTextureBlockC(TEMPLATE_YELLOW.get(), "yellow_shield_template", "block/shieldtemplate3", builder -> builder.renderType("cutout")))
                        .shaped(builder -> builder
                                        .define('w', ItemTags.WOOL)
                                        .define('l', Tags.Items.DYES_YELLOW)
                                        .unlockedBy("glass", has(Items.GLASS)),
                                8,
                                "www", "lGl", "www"),
                Dob.blockBuilder(SHIELD_BLOCK1)
                        .ironPickaxeTags()
                        .parentedItem("block/shield_block")
                        .standardLoot(TYPE_SHIELD_BLOCK1)
                        .blockState(p -> p.simpleBlock(SHIELD_BLOCK1.get(), p.models().cubeAll("shield_block", p.modLoc("block/machineshieldprojector"))))
                        .shaped(builder -> builder
                                        .define('F', VariousModule.MACHINE_FRAME.get())
                                        .define('g', Tags.Items.INGOTS_GOLD)
                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                                "gTg", "rFr", "OOO"),
                Dob.blockBuilder(SHIELD_BLOCK2)
                        .ironPickaxeTags()
                        .parentedItem("block/shield_block")
                        .standardLoot(TYPE_SHIELD_BLOCK2)
                        .blockState(p -> p.simpleBlock(SHIELD_BLOCK2.get(), p.models().cubeAll("shield_block", p.modLoc("block/machineshieldprojector"))))
                        .shapedNBT(builder -> builder
                                        .define('M', SHIELD_BLOCK1.get())
                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                                "ROR", "OMO", "ROR"),
                Dob.blockBuilder(SHIELD_BLOCK3)
                        .ironPickaxeTags()
                        .parentedItem("block/shield_block")
                        .standardLoot(TYPE_SHIELD_BLOCK3)
                        .blockState(p -> p.simpleBlock(SHIELD_BLOCK3.get(), p.models().cubeAll("shield_block", p.modLoc("block/machineshieldprojector"))))
                        .shapedNBT(builder -> builder
                                        .define('s', VariousModule.DIMENSIONALSHARD.get())
                                        .define('M', SHIELD_BLOCK2.get())
                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                                "sOs", "OMO", "sOs"),
                Dob.blockBuilder(SHIELD_BLOCK4)
                        .ironPickaxeTags()
                        .parentedItem("block/shield_block")
                        .standardLoot(TYPE_SHIELD_BLOCK4)
                        .blockState(p -> p.simpleBlock(SHIELD_BLOCK4.get(), p.models().cubeAll("shield_block", p.modLoc("block/machineshieldprojector"))))
                        .shapedNBT(builder -> builder
                                        .define('s', VariousModule.DIMENSIONALSHARD.get())
                                        .define('M', SHIELD_BLOCK3.get())
                                        .define('n', Items.NETHER_STAR)
                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                                "nOs", "OMO", "sOn"),
                Dob.blockBuilder(SHIELDING_SOLID)
                        .ironPickaxeTags()
                        .blockState(p -> p.simpleBlock(SHIELDING_SOLID.get(), new ModelFile.UncheckedModelFile(new ResourceLocation(RFToolsBuilder.MODID, "block/shielding")))),
                Dob.blockBuilder(SHIELDING_TRANSLUCENT)
                        .ironPickaxeTags()
                        .blockState(p -> p.simpleBlock(SHIELDING_TRANSLUCENT.get(), new ModelFile.UncheckedModelFile(new ResourceLocation(RFToolsBuilder.MODID, "block/shielding")))),
                Dob.blockBuilder(SHIELDING_CUTOUT)
                        .ironPickaxeTags()
                        .blockState(p -> p.simpleBlock(SHIELDING_CUTOUT.get(), new ModelFile.UncheckedModelFile(new ResourceLocation(RFToolsBuilder.MODID, "block/shielding"))))
        );
    }
}
