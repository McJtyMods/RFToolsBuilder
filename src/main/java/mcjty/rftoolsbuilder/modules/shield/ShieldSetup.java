package mcjty.rftoolsbuilder.modules.shield;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.shield.blocks.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

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

    public static final RegistryObject<BaseBlock> SHIELD_BLOCK1 = BLOCKS.register("shield_block1", () -> new ShieldBlock(ShieldTileEntity.class, ShieldConfiguration.maxShieldSize.get()));
    public static final RegistryObject<BaseBlock> SHIELD_BLOCK2 = BLOCKS.register("shield_block2", () -> new ShieldBlock(ShieldTileEntity.class, ShieldConfiguration.maxShieldSize.get() * 4));
    public static final RegistryObject<BaseBlock> SHIELD_BLOCK3 = BLOCKS.register("shield_block3", () -> new ShieldBlock(ShieldTileEntity.class, ShieldConfiguration.maxShieldSize.get() * 16));
    public static final RegistryObject<BaseBlock> SHIELD_BLOCK4 = BLOCKS.register("shield_block4", () -> new ShieldBlock(ShieldTileEntity.class, ShieldConfiguration.maxShieldSize.get() * 128));
    public static final RegistryObject<Item> SHIELD_BLOCK1_ITEM = ITEMS.register("shield_block1", () -> new BlockItem(SHIELD_BLOCK1.get(), RFToolsBuilder.createStandardProperties()));
    public static final RegistryObject<Item> SHIELD_BLOCK2_ITEM = ITEMS.register("shield_block2", () -> new BlockItem(SHIELD_BLOCK2.get(), RFToolsBuilder.createStandardProperties()));
    public static final RegistryObject<Item> SHIELD_BLOCK3_ITEM = ITEMS.register("shield_block3", () -> new BlockItem(SHIELD_BLOCK3.get(), RFToolsBuilder.createStandardProperties()));
    public static final RegistryObject<Item> SHIELD_BLOCK4_ITEM = ITEMS.register("shield_block4", () -> new BlockItem(SHIELD_BLOCK4.get(), RFToolsBuilder.createStandardProperties()));
    public static final RegistryObject<TileEntityType<?>> TYPE_SHIELD_BLOCK1 = TILES.register("shield_block1", () -> TileEntityType.Builder.create(ShieldTileEntity::new, SHIELD_BLOCK1.get()).build(null));
    public static final RegistryObject<TileEntityType<?>> TYPE_SHIELD_BLOCK2 = TILES.register("shield_block2", () -> TileEntityType.Builder.create(ShieldTileEntity2::new, SHIELD_BLOCK2.get()).build(null));
    public static final RegistryObject<TileEntityType<?>> TYPE_SHIELD_BLOCK3 = TILES.register("shield_block3", () -> TileEntityType.Builder.create(ShieldTileEntity3::new, SHIELD_BLOCK3.get()).build(null));
    public static final RegistryObject<TileEntityType<?>> TYPE_SHIELD_BLOCK4 = TILES.register("shield_block4", () -> TileEntityType.Builder.create(ShieldTileEntity4::new, SHIELD_BLOCK4.get()).build(null));
    public static final RegistryObject<ContainerType<GenericContainer>> CONTAINER_SHIELD = CONTAINERS.register("shield", GenericContainer::createContainerType);

    public static final RegistryObject<ShieldTemplateBlock> TEMPLATE_BLUE = BLOCKS.register("blue_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.BLUE));
    public static final RegistryObject<ShieldTemplateBlock> TEMPLATE_RED = BLOCKS.register("red_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.RED));
    public static final RegistryObject<ShieldTemplateBlock> TEMPLATE_GREEN = BLOCKS.register("green_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.GREEN));
    public static final RegistryObject<ShieldTemplateBlock> TEMPLATE_YELLOW = BLOCKS.register("yellow_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.YELLOW));
    public static final RegistryObject<Item> TEMPLATE_BLUE_ITEM = ITEMS.register("blue_shield_template_block", () -> new BlockItem(TEMPLATE_BLUE.get(), RFToolsBuilder.createStandardProperties()));
    public static final RegistryObject<Item> TEMPLATE_RED_ITEM = ITEMS.register("red_shield_template_block", () -> new BlockItem(TEMPLATE_RED.get(), RFToolsBuilder.createStandardProperties()));
    public static final RegistryObject<Item> TEMPLATE_GREEN_ITEM = ITEMS.register("green_shield_template_block", () -> new BlockItem(TEMPLATE_GREEN.get(), RFToolsBuilder.createStandardProperties()));
    public static final RegistryObject<Item> TEMPLATE_YELLOW_ITEM = ITEMS.register("yellow_shield_template_block", () -> new BlockItem(TEMPLATE_YELLOW.get(), RFToolsBuilder.createStandardProperties()));

    public static final RegistryObject<InvisibleShieldBlock> SHIELD_INVISIBLE = BLOCKS.register("invisible_shield_block", () -> new InvisibleShieldBlock(false));
    public static final RegistryObject<NoTickInvisibleShieldBlock> SHIELD_INVISIBLE_NOTICK = BLOCKS.register("notick_invisible_shield_block", () -> new NoTickInvisibleShieldBlock(false));
    public static final RegistryObject<InvisibleShieldBlock> SHIELD_INVISIBLE_OPAQUE = BLOCKS.register("invisible_shield_block_opaque", () -> new InvisibleShieldBlock(true));
    public static final RegistryObject<NoTickInvisibleShieldBlock> SHIELD_INVISIBLE_OPAQUE_NOTICK = BLOCKS.register("notick_invisible_shield_block_opaque", () -> new NoTickInvisibleShieldBlock(true));

    public static final RegistryObject<CamoShieldBlock> SHIELD_CAMO = BLOCKS.register("camo_shield_block", () -> new CamoShieldBlock(false));
    public static final RegistryObject<NoTickCamoShieldBlock> SHIELD_CAMO_NOTICK = BLOCKS.register("notick_camo_shield_block", () -> new NoTickCamoShieldBlock(false));
    public static final RegistryObject<CamoShieldBlock> SHIELD_CAMO_OPAQUE = BLOCKS.register("camo_shield_block_opaque", () -> new CamoShieldBlock(true));
    public static final RegistryObject<NoTickCamoShieldBlock> SHIELD_CAMO_OPAQUE_NOTICK = BLOCKS.register("notick_camo_shield_block_opaque", () -> new NoTickCamoShieldBlock(true));

    public static final RegistryObject<SolidShieldBlock> SHIELD_SOLID = BLOCKS.register("solid_shield_block", () -> new SolidShieldBlock(false));
    public static final RegistryObject<NoTickSolidShieldBlock> SHIELD_SOLID_NOTICK = BLOCKS.register("notick_solid_shield_block", () -> new NoTickSolidShieldBlock(false));
    public static final RegistryObject<SolidShieldBlock> SHIELD_SOLID_OPAQUE = BLOCKS.register("solid_shield_block_opaque", () -> new SolidShieldBlock(true));
    public static final RegistryObject<NoTickSolidShieldBlock> SHIELD_SOLID_OPAQUE_NOTICK = BLOCKS.register("notick_solid_shield_block_opaque", () -> new NoTickSolidShieldBlock(true));

    public static final RegistryObject<TileEntityType<?>> TYPE_SHIELD_INV_BLOCK = TILES.register("shield_inv_block", () -> TileEntityType.Builder.create(TickShieldBlockTileEntity::new, SHIELD_INVISIBLE.get()).build(null));
    public static final RegistryObject<TileEntityType<?>> TYPE_SHIELD_INV_NO_TICK_BLOCK = TILES.register("shield_inv_no_tickblock", () -> TileEntityType.Builder.create(NoTickShieldBlockTileEntity::new, SHIELD_INVISIBLE_NOTICK.get()).build(null));
    public static final RegistryObject<TileEntityType<?>> TYPE_SHIELD_SOLID_NO_TICK_BLOCK = TILES.register("shield_solid_no_tickblock", () -> TileEntityType.Builder.create(NoTickShieldSolidBlockTileEntity::new, SHIELD_SOLID_NOTICK.get()).build(null));
    public static final RegistryObject<TileEntityType<?>> TYPE_SHIELD_CAMO_BLOCK = TILES.register("shield_camo_block", () -> TileEntityType.Builder.create(NoTickShieldSolidBlockTileEntity::new, SHIELD_CAMO.get()).build(null));

    public static final RegistryObject<TileEntityType<?>> TYPE_SHIELD_BASE;
    public static final List<RegistryObject<BaseShieldBlock>> SHIELD_BASE_BLOCKS = new ArrayList<>();

    static {
        Block.Properties properties = Block.Properties.create(Material.GLASS)
                .hardnessAndResistance(-1.0F, 3600000.0F)
                .noDrops();

        for (int i = 0; i < 256; i++) {
            int finalI = i;
            SHIELD_BASE_BLOCKS.add(BLOCKS.register("shield_base" + i, () -> new BaseShieldBlock(properties, finalI)));
        }
        TYPE_SHIELD_BASE = TILES.register("shield_base", () -> {
            Block[] blocksWithTE = new Block[128];
            int idx = 0;
            for (int i = 0; i < 256; i++) {
                if ((i % BaseShieldBlock.FILTERS_NEEDED) != 0) {
                    blocksWithTE[idx++] = (SHIELD_BASE_BLOCKS.get(i).get());
                }
            }
            return TileEntityType.Builder.create(BaseShieldTileEntity::new, blocksWithTE).build(null);
        });
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
