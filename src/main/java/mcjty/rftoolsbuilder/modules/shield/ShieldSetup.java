package mcjty.rftoolsbuilder.modules.shield;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.shield.blocks.*;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static mcjty.rftoolsbuilder.RFToolsBuilder.MODID;


public class ShieldSetup {

    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<TileEntityType<?>> TILES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, MODID);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = new DeferredRegister<>(ForgeRegistries.CONTAINERS, MODID);

    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<BaseBlock> SHIELD_BLOCK1 = BLOCKS.register("shield_block1", () -> new ShieldProjectorBlock(ShieldSetup::createProjector1, ShieldConfiguration.maxShieldSize.get()));
    public static final RegistryObject<BaseBlock> SHIELD_BLOCK2 = BLOCKS.register("shield_block2", () -> new ShieldProjectorBlock(ShieldSetup::createProjector2, ShieldConfiguration.maxShieldSize.get() * 4));
    public static final RegistryObject<BaseBlock> SHIELD_BLOCK3 = BLOCKS.register("shield_block3", () -> new ShieldProjectorBlock(ShieldSetup::createProjector3, ShieldConfiguration.maxShieldSize.get() * 16));
    public static final RegistryObject<BaseBlock> SHIELD_BLOCK4 = BLOCKS.register("shield_block4", () -> new ShieldProjectorBlock(ShieldSetup::createProjector4, ShieldConfiguration.maxShieldSize.get() * 128));
    public static final RegistryObject<Item> SHIELD_BLOCK1_ITEM = ITEMS.register("shield_block1", () -> new BlockItem(SHIELD_BLOCK1.get(), RFToolsBuilder.createStandardProperties()));
    public static final RegistryObject<Item> SHIELD_BLOCK2_ITEM = ITEMS.register("shield_block2", () -> new BlockItem(SHIELD_BLOCK2.get(), RFToolsBuilder.createStandardProperties()));
    public static final RegistryObject<Item> SHIELD_BLOCK3_ITEM = ITEMS.register("shield_block3", () -> new BlockItem(SHIELD_BLOCK3.get(), RFToolsBuilder.createStandardProperties()));
    public static final RegistryObject<Item> SHIELD_BLOCK4_ITEM = ITEMS.register("shield_block4", () -> new BlockItem(SHIELD_BLOCK4.get(), RFToolsBuilder.createStandardProperties()));
    public static final RegistryObject<TileEntityType<?>> TYPE_SHIELD_BLOCK1 = TILES.register("shield_block1", () -> TileEntityType.Builder.create(ShieldSetup::createProjector1, SHIELD_BLOCK1.get()).build(null));
    public static final RegistryObject<TileEntityType<?>> TYPE_SHIELD_BLOCK2 = TILES.register("shield_block2", () -> TileEntityType.Builder.create(ShieldSetup::createProjector2, SHIELD_BLOCK2.get()).build(null));
    public static final RegistryObject<TileEntityType<?>> TYPE_SHIELD_BLOCK3 = TILES.register("shield_block3", () -> TileEntityType.Builder.create(ShieldSetup::createProjector3, SHIELD_BLOCK3.get()).build(null));
    public static final RegistryObject<TileEntityType<?>> TYPE_SHIELD_BLOCK4 = TILES.register("shield_block4", () -> TileEntityType.Builder.create(ShieldSetup::createProjector4, SHIELD_BLOCK4.get()).build(null));
    public static final RegistryObject<ContainerType<GenericContainer>> CONTAINER_SHIELD = CONTAINERS.register("shield", GenericContainer::createContainerType);

    public static final RegistryObject<ShieldTemplateBlock> TEMPLATE_BLUE = BLOCKS.register("blue_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.BLUE));
    public static final RegistryObject<ShieldTemplateBlock> TEMPLATE_RED = BLOCKS.register("red_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.RED));
    public static final RegistryObject<ShieldTemplateBlock> TEMPLATE_GREEN = BLOCKS.register("green_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.GREEN));
    public static final RegistryObject<ShieldTemplateBlock> TEMPLATE_YELLOW = BLOCKS.register("yellow_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.YELLOW));

    public static final RegistryObject<Item> TEMPLATE_BLUE_ITEM = ITEMS.register("blue_shield_template_block", () -> new BlockItem(TEMPLATE_BLUE.get(), RFToolsBuilder.createStandardProperties()));
    public static final RegistryObject<Item> TEMPLATE_RED_ITEM = ITEMS.register("red_shield_template_block", () -> new BlockItem(TEMPLATE_RED.get(), RFToolsBuilder.createStandardProperties()));
    public static final RegistryObject<Item> TEMPLATE_GREEN_ITEM = ITEMS.register("green_shield_template_block", () -> new BlockItem(TEMPLATE_GREEN.get(), RFToolsBuilder.createStandardProperties()));
    public static final RegistryObject<Item> TEMPLATE_YELLOW_ITEM = ITEMS.register("yellow_shield_template_block", () -> new BlockItem(TEMPLATE_YELLOW.get(), RFToolsBuilder.createStandardProperties()));

    public static final RegistryObject<ShieldingBlock> SHIELDING_SOLID = BLOCKS.register("shielding_solid", () -> new ShieldingBlock(BlockRenderLayer.SOLID));
    public static final RegistryObject<ShieldingBlock> SHIELDING_TRANSLUCENT = BLOCKS.register("shielding_translucent", () -> new ShieldingBlock(BlockRenderLayer.TRANSLUCENT));
    public static final RegistryObject<ShieldingBlock> SHIELDING_CUTOUT = BLOCKS.register("shielding_cutout", () -> new ShieldingBlock(BlockRenderLayer.CUTOUT));
    public static final RegistryObject<TileEntityType<?>> TYPE_SHIELDING = TILES.register("shielding", () -> TileEntityType.Builder.create(ShieldingTileEntity::new,
            SHIELDING_SOLID.get(), SHIELDING_TRANSLUCENT.get(), SHIELDING_CUTOUT.get()).build(null));

    public static ShieldProjectorTileEntity createProjector1() {
        return new ShieldProjectorTileEntity(TYPE_SHIELD_BLOCK1.get(), ShieldConfiguration.maxShieldSize.get(), ShieldConfiguration.MAXENERGY.get(), ShieldConfiguration.RECEIVEPERTICK.get());
    }

    public static ShieldProjectorTileEntity createProjector2() {
        return new ShieldProjectorTileEntity(TYPE_SHIELD_BLOCK2.get(), ShieldConfiguration.maxShieldSize.get() * 4, ShieldConfiguration.MAXENERGY.get(), ShieldConfiguration.RECEIVEPERTICK.get());
    }

    public static ShieldProjectorTileEntity createProjector3() {
        return new ShieldProjectorTileEntity(TYPE_SHIELD_BLOCK3.get(), ShieldConfiguration.maxShieldSize.get() * 16, ShieldConfiguration.MAXENERGY.get() * 3, ShieldConfiguration.RECEIVEPERTICK.get() * 2)
                .setDamageFactor(4.0f)
                .setCostFactor(2.0f);
    }

    public static ShieldProjectorTileEntity createProjector4() {
        return new ShieldProjectorTileEntity(TYPE_SHIELD_BLOCK4.get(), ShieldConfiguration.maxShieldSize.get() * 128, ShieldConfiguration.MAXENERGY.get() * 6, ShieldConfiguration.RECEIVEPERTICK.get() * 6)
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
}
