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
    public static ShieldBlock shieldBlock1;
    @ObjectHolder("rftoolsbuilder:shield_block2")
    public static ShieldBlock shieldBlock2;
    @ObjectHolder("rftoolsbuilder:shield_block3")
    public static ShieldBlock shieldBlock3;
    @ObjectHolder("rftoolsbuilder:shield_block4")
    public static ShieldBlock shieldBlock4;

    @ObjectHolder("rftoolsbuilder:invisible_shield_block")
    public static InvisibleShieldBlock invisibleShieldBlock;
    @ObjectHolder("rftoolsbuilder:notick_invisible_shield_block")
    public static NoTickInvisibleShieldBlock noTickInvisibleShieldBlock;
    @ObjectHolder("rftoolsbuilder:invisible_shield_block_opaque")
    public static InvisibleShieldBlock invisibleShieldBlockOpaque;
    @ObjectHolder("rftoolsbuilder:notick_invisible_shield_block_opaque")
    public static NoTickInvisibleShieldBlock noTickInvisibleShieldBlockOpaque;

    @ObjectHolder("rftoolsbuilder:solid_shield_block")
    public static SolidShieldBlock solidShieldBlock;
    @ObjectHolder("rftoolsbuilder:notick_solid_shield_block")
    public static NoTickSolidShieldBlock noTickSolidShieldBlock;
    @ObjectHolder("rftoolsbuilder:camo_shield_block")
    public static CamoShieldBlock camoShieldBlock;
    @ObjectHolder("rftoolsbuilder:notick_camo_shield_block")
    public static NoTickCamoShieldBlock noTickCamoShieldBlock;

    @ObjectHolder("rftoolsbuilder:solid_shield_block_opaque")
    public static SolidShieldBlock solidShieldBlockOpaque;
    @ObjectHolder("rftoolsbuilder:notick_solid_shield_block_opaque")
    public static NoTickSolidShieldBlock noTickSolidShieldBlockOpaque;
    @ObjectHolder("rftoolsbuilder:camo_shield_block_opaque")
    public static CamoShieldBlock camoShieldBlockOpaque;
    @ObjectHolder("rftoolsbuilder:notick_camo_shield_block_opaque")
    public static NoTickCamoShieldBlock noTickCamoShieldBlockOpaque;

    @ObjectHolder("rftoolsbuilder:blue_shield_template_block")
    public static ShieldTemplateBlock blueShieldTemplateBlock;
    @ObjectHolder("rftoolsbuilder:red_shield_template_block")
    public static ShieldTemplateBlock redShieldTemplateBlock;
    @ObjectHolder("rftoolsbuilder:green_shield_template_block")
    public static ShieldTemplateBlock greenShieldTemplateBlock;
    @ObjectHolder("rftoolsbuilder:yellow_shield_template_block")
    public static ShieldTemplateBlock yellowShieldTemplateBlock;

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
        event.getRegistry().register(new BaseBlockItem(shieldBlock1, properties));
        event.getRegistry().register(new BaseBlockItem(shieldBlock2, properties));
        event.getRegistry().register(new BaseBlockItem(shieldBlock3, properties));
        event.getRegistry().register(new BaseBlockItem(shieldBlock4, properties));
        event.getRegistry().register(new BaseBlockItem(blueShieldTemplateBlock, properties));
        event.getRegistry().register(new BaseBlockItem(redShieldTemplateBlock, properties));
        event.getRegistry().register(new BaseBlockItem(greenShieldTemplateBlock, properties));
        event.getRegistry().register(new BaseBlockItem(yellowShieldTemplateBlock, properties));
    }

    public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(TileEntityType.Builder.create(ShieldTileEntity::new, shieldBlock1).build(null).setRegistryName("shield_block1"));
        event.getRegistry().register(TileEntityType.Builder.create(ShieldTileEntity2::new, shieldBlock2).build(null).setRegistryName("shield_block2"));
        event.getRegistry().register(TileEntityType.Builder.create(ShieldTileEntity3::new, shieldBlock3).build(null).setRegistryName("shield_block3"));
        event.getRegistry().register(TileEntityType.Builder.create(ShieldTileEntity4::new, shieldBlock4).build(null).setRegistryName("shield_block4"));

        event.getRegistry().register(TileEntityType.Builder.create(TickShieldBlockTileEntity::new, invisibleShieldBlock).build(null).setRegistryName("shield_inv_block"));
        event.getRegistry().register(TileEntityType.Builder.create(NoTickShieldBlockTileEntity::new, noTickInvisibleShieldBlock).build(null).setRegistryName("shield_inv_no_tickblock"));
        event.getRegistry().register(TileEntityType.Builder.create(TickShieldSolidBlockTileEntity::new, solidShieldBlock).build(null).setRegistryName("solid_shield_block"));
        event.getRegistry().register(TileEntityType.Builder.create(NoTickShieldSolidBlockTileEntity::new, noTickSolidShieldBlock).build(null).setRegistryName("notick_solid_shield_block"));

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
