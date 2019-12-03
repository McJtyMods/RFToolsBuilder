package mcjty.rftoolsbuilder.modules.shield;

import mcjty.lib.blocks.BaseBlockItem;
import mcjty.lib.container.GenericContainer;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.shield.blocks.*;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;


public class ShieldSetup {

    @ObjectHolder("rftoolsbuilder:shield_block1")
    public static ShieldBlock SHIELD_BLOCK1;
    @ObjectHolder("rftoolsbuilder:shield_block2")
    public static ShieldBlock SHIELD_BLOCK2;
    @ObjectHolder("rftoolsbuilder:shield_block3")
    public static ShieldBlock SHIELD_BLOCK3;
    @ObjectHolder("rftoolsbuilder:shield_block4")
    public static ShieldBlock SHIELD_BLOCK4;

    @ObjectHolder("rftoolsbuilder:invisible_shield_block")
    public static InvisibleShieldBlock SHIELD_INVISIBLE;
    @ObjectHolder("rftoolsbuilder:notick_invisible_shield_block")
    public static NoTickInvisibleShieldBlock SHIELD_INVISIBLE_NOTICK;
    @ObjectHolder("rftoolsbuilder:invisible_shield_block_opaque")
    public static InvisibleShieldBlock SHIELD_INVISIBLE_OPAQUE;
    @ObjectHolder("rftoolsbuilder:notick_invisible_shield_block_opaque")
    public static NoTickInvisibleShieldBlock SHIELD_INVISIBLE_OPAQUE_NOTICK;

    @ObjectHolder("rftoolsbuilder:solid_shield_block")
    public static SolidShieldBlock SHIELD_SOLID;
    @ObjectHolder("rftoolsbuilder:notick_solid_shield_block")
    public static NoTickSolidShieldBlock SHIELD_SOLID_NOTICK;
    @ObjectHolder("rftoolsbuilder:camo_shield_block")
    public static CamoShieldBlock SHIELD_CAMO;
    @ObjectHolder("rftoolsbuilder:notick_camo_shield_block")
    public static NoTickCamoShieldBlock SHIELD_CAMO_NOTICK;

    @ObjectHolder("rftoolsbuilder:solid_shield_block_opaque")
    public static SolidShieldBlock SHIELD_SOLID_OPAQUE;
    @ObjectHolder("rftoolsbuilder:notick_solid_shield_block_opaque")
    public static NoTickSolidShieldBlock SHIELD_SOLID_OPAQUE_NOTICK;
    @ObjectHolder("rftoolsbuilder:camo_shield_block_opaque")
    public static CamoShieldBlock SHIELD_CAMO_OPAQUE;
    @ObjectHolder("rftoolsbuilder:notick_camo_shield_block_opaque")
    public static NoTickCamoShieldBlock SHIELD_CAMO_OPAQUE_NOTICK;

    @ObjectHolder("rftoolsbuilder:blue_shield_template_block")
    public static ShieldTemplateBlock TEMPLATE_BLUE;
    @ObjectHolder("rftoolsbuilder:red_shield_template_block")
    public static ShieldTemplateBlock TEMPLATE_RED;
    @ObjectHolder("rftoolsbuilder:green_shield_template_block")
    public static ShieldTemplateBlock TEMPLATE_GREEN;
    @ObjectHolder("rftoolsbuilder:yellow_shield_template_block")
    public static ShieldTemplateBlock TEMPLATE_YELLOW;

    @ObjectHolder("rftoolsbuilder:shield_inv_block")
    public static TileEntityType<?> TYPE_SHIELD_INV_BLOCK;
    @ObjectHolder("rftoolsbuilder:shield_inv_no_tickblock")
    public static TileEntityType<?> TYPE_SHIELD_INV_NO_TICK_BLOCK;
    @ObjectHolder("rftoolsbuilder:shield_solid_no_tickblock")
    public static TileEntityType<?> TYPE_SHIELD_SOLID_NO_TICK_BLOCK;
    @ObjectHolder("rftoolsbuilder:shield_camo_block")
    public static TileEntityType<?> TYPE_SHIELD_CAMO_BLOCK;

//    @ObjectHolder("rftoolsbuilder:shield_solid")
//    public static TileEntityTyp  e<?> TYPE_SHIELD_SOLID;
    @ObjectHolder("rftoolsbuilder:shield_block1")
    public static TileEntityType<?> TYPE_SHIELD_BLOCK1;
    @ObjectHolder("rftoolsbuilder:shield_block2")
    public static TileEntityType<?> TYPE_SHIELD_BLOCK2;
    @ObjectHolder("rftoolsbuilder:shield_block3")
    public static TileEntityType<?> TYPE_SHIELD_BLOCK3;
    @ObjectHolder("rftoolsbuilder:shield_block4")
    public static TileEntityType<?> TYPE_SHIELD_BLOCK4;

    @ObjectHolder("rftoolsbuilder:shield")
    public static ContainerType<GenericContainer> CONTAINER_SHIELD;

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new ShieldBlock("shield_block1", ShieldTileEntity.class, ShieldConfiguration.maxShieldSize.get()));
        event.getRegistry().register(new ShieldBlock("shield_block2", ShieldTileEntity2.class, ShieldConfiguration.maxShieldSize.get() * 4));
        event.getRegistry().register(new ShieldBlock("shield_block3", ShieldTileEntity3.class, ShieldConfiguration.maxShieldSize.get() * 16));
        event.getRegistry().register(new ShieldBlock("shield_block4", ShieldTileEntity4.class, ShieldConfiguration.maxShieldSize.get() * 128));

        event.getRegistry().register(new ShieldTemplateBlock("blue_shield_template_block", ShieldTemplateBlock.TemplateColor.BLUE));
        event.getRegistry().register(new ShieldTemplateBlock("red_shield_template_block", ShieldTemplateBlock.TemplateColor.RED));
        event.getRegistry().register(new ShieldTemplateBlock("green_shield_template_block", ShieldTemplateBlock.TemplateColor.GREEN));
        event.getRegistry().register(new ShieldTemplateBlock("yellow_shield_template_block", ShieldTemplateBlock.TemplateColor.BLUE));

        event.getRegistry().register(new InvisibleShieldBlock("invisible_shield_block", "rftoolsbuilder.invisible_shield_block", false));
        event.getRegistry().register(new NoTickInvisibleShieldBlock("notick_invisible_shield_block", "rftoolsbuilder.notick_invisible_shield_block", false));
        event.getRegistry().register(new InvisibleShieldBlock("invisible_shield_block_opaque", "rftoolsbuilder.invisible_shield_block", true));
        event.getRegistry().register(new NoTickInvisibleShieldBlock("notick_invisible_shield_block_opaque", "rftoolsbuilder.notick_invisible_shield_block", true));

        event.getRegistry().register(new SolidShieldBlock("solid_shield_block", "rftoolsbuilder.solid_shield_block", false));
        event.getRegistry().register(new NoTickSolidShieldBlock("notick_solid_shield_block", "rftoolsbuilder.notick_solid_shield_block", false));
        event.getRegistry().register(new CamoShieldBlock("camo_shield_block", "rftoolsbuilder.camo_shield_block", false));
        event.getRegistry().register(new NoTickCamoShieldBlock("notick_camo_shield_block", "rftoolsbuilder.notick_camo_shield_block", false));

        event.getRegistry().register(new SolidShieldBlock("solid_shield_block_opaque", "rftoolsbuilder.solid_shield_block", true));
        event.getRegistry().register(new NoTickSolidShieldBlock("notick_solid_shield_block_opaque", "rftoolsbuilder.notick_solid_shield_block", true));
        event.getRegistry().register(new CamoShieldBlock("camo_shield_block_opaque", "rftoolsbuilder.camo_shield_block", true));
        event.getRegistry().register(new NoTickCamoShieldBlock("notick_camo_shield_block_opaque", "rftoolsbuilder.notick_camo_shield_block", true));
    }

    public static void registerItems(RegistryEvent.Register<Item> event) {
        Item.Properties properties = new Item.Properties().group(RFToolsBuilder.setup.getTab());
        event.getRegistry().register(new BaseBlockItem(SHIELD_BLOCK1, properties));
        event.getRegistry().register(new BaseBlockItem(SHIELD_BLOCK2, properties));
        event.getRegistry().register(new BaseBlockItem(SHIELD_BLOCK3, properties));
        event.getRegistry().register(new BaseBlockItem(SHIELD_BLOCK4, properties));
        event.getRegistry().register(new BaseBlockItem(TEMPLATE_BLUE, properties));
        event.getRegistry().register(new BaseBlockItem(TEMPLATE_RED, properties));
        event.getRegistry().register(new BaseBlockItem(TEMPLATE_GREEN, properties));
        event.getRegistry().register(new BaseBlockItem(TEMPLATE_YELLOW, properties));
    }

    public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(TileEntityType.Builder.create(ShieldTileEntity::new, SHIELD_BLOCK1).build(null).setRegistryName("shield_block1"));
        event.getRegistry().register(TileEntityType.Builder.create(ShieldTileEntity2::new, SHIELD_BLOCK2).build(null).setRegistryName("shield_block2"));
        event.getRegistry().register(TileEntityType.Builder.create(ShieldTileEntity3::new, SHIELD_BLOCK3).build(null).setRegistryName("shield_block3"));
        event.getRegistry().register(TileEntityType.Builder.create(ShieldTileEntity4::new, SHIELD_BLOCK4).build(null).setRegistryName("shield_block4"));

        event.getRegistry().register(TileEntityType.Builder.create(TickShieldBlockTileEntity::new, SHIELD_INVISIBLE).build(null).setRegistryName("shield_inv_block"));
        event.getRegistry().register(TileEntityType.Builder.create(NoTickShieldBlockTileEntity::new, SHIELD_INVISIBLE_NOTICK).build(null).setRegistryName("shield_inv_no_tickblock"));
        event.getRegistry().register(TileEntityType.Builder.create(TickShieldSolidBlockTileEntity::new, SHIELD_SOLID).build(null).setRegistryName("solid_shield_block"));
        event.getRegistry().register(TileEntityType.Builder.create(NoTickShieldSolidBlockTileEntity::new, SHIELD_SOLID_NOTICK).build(null).setRegistryName("notick_solid_shield_block"));

/*
    public static TileEntityType<?> TYPE_SHIELD_INV_BLOCK;
    public static TileEntityType<?> TYPE_SHIELD_INV_NO_TICK_BLOCK;
    public static TileEntityType<?> TYPE_SHIELD_SOLID_NO_TICK_BLOCK;
    public static TileEntityType<?> TYPE_SHIELD_CAMO_BLOCK;
    public static TileEntityType<?> TYPE_SHIELD_SOLID;
    public static TileEntityType<?> TYPE_SHIELD_BLOCK1;
    public static TileEntityType<?> TYPE_SHIELD_BLOCK2;
    public static TileEntityType<?> TYPE_SHIELD_BLOCK3;
    public static TileEntityType<?> TYPE_SHIELD_BLOCK4;

 */
    }

    public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().register(GenericContainer.createContainerType("shield"));
    }

    // @todo 1.14
//    @SideOnly(Side.CLIENT)
//    public static void initClient() {
//        shieldBlock1.initModel();
//        shieldBlock2.initModel();
//        shieldBlock3.initModel();
//        shieldBlock4.initModel();
//        shieldTemplateBlock.initModel();
//        invisibleShieldBlock.initModel();
//        noTickInvisibleShieldBlock.initModel();
//        invisibleShieldBlockOpaque.initModel();
//        noTickInvisibleShieldBlockOpaque.initModel();
//        if (!ShieldConfiguration.disableShieldBlocksToUncorruptWorld.get()) {
//            solidShieldBlock.initModel();
//            noTickSolidShieldBlock.initModel();
//            camoShieldBlock.initModel();
//            noTickCamoShieldBlock.initModel();
//            solidShieldBlockOpaque.initModel();
//            noTickSolidShieldBlockOpaque.initModel();
//            camoShieldBlockOpaque.initModel();
//            noTickCamoShieldBlockOpaque.initModel();
//        }
//    }
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
