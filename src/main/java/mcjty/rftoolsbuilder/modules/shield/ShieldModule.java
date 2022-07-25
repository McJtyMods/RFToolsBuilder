package mcjty.rftoolsbuilder.modules.shield;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbuilder.modules.shield.blocks.*;
import mcjty.rftoolsbuilder.modules.shield.client.ClientSetup;
import mcjty.rftoolsbuilder.modules.shield.client.GuiShield;
import mcjty.rftoolsbuilder.modules.shield.client.ShieldModelLoader;
import mcjty.rftoolsbuilder.setup.Config;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nonnull;

import static mcjty.rftoolsbuilder.setup.Registration.*;


public class ShieldModule implements IModule {

    public static final RegistryObject<BaseBlock> SHIELD_BLOCK1 = BLOCKS.register("shield_block1", () -> new ShieldProjectorBlock(ShieldModule::createProjector1, ShieldConfiguration.maxShieldSize));
    public static final RegistryObject<BaseBlock> SHIELD_BLOCK2 = BLOCKS.register("shield_block2", () -> new ShieldProjectorBlock(ShieldModule::createProjector2, () -> ShieldConfiguration.maxShieldSize.get() * 4));
    public static final RegistryObject<BaseBlock> SHIELD_BLOCK3 = BLOCKS.register("shield_block3", () -> new ShieldProjectorBlock(ShieldModule::createProjector3, () -> ShieldConfiguration.maxShieldSize.get() * 16));
    public static final RegistryObject<BaseBlock> SHIELD_BLOCK4 = BLOCKS.register("shield_block4", () -> new ShieldProjectorBlock(ShieldModule::createProjector4, () -> ShieldConfiguration.maxShieldSize.get() * 128));
    public static final RegistryObject<Item> SHIELD_BLOCK1_ITEM = ITEMS.register("shield_block1", () -> new BlockItem(SHIELD_BLOCK1.get(), Registration.createStandardProperties()));
    public static final RegistryObject<Item> SHIELD_BLOCK2_ITEM = ITEMS.register("shield_block2", () -> new BlockItem(SHIELD_BLOCK2.get(), Registration.createStandardProperties()));
    public static final RegistryObject<Item> SHIELD_BLOCK3_ITEM = ITEMS.register("shield_block3", () -> new BlockItem(SHIELD_BLOCK3.get(), Registration.createStandardProperties()));
    public static final RegistryObject<Item> SHIELD_BLOCK4_ITEM = ITEMS.register("shield_block4", () -> new BlockItem(SHIELD_BLOCK4.get(), Registration.createStandardProperties()));
    public static final RegistryObject<BlockEntityType<?>> TYPE_SHIELD_BLOCK1 = TILES.register("shield_block1", () -> BlockEntityType.Builder.of(ShieldModule::createProjector1, SHIELD_BLOCK1.get()).build(null));
    public static final RegistryObject<BlockEntityType<?>> TYPE_SHIELD_BLOCK2 = TILES.register("shield_block2", () -> BlockEntityType.Builder.of(ShieldModule::createProjector2, SHIELD_BLOCK2.get()).build(null));
    public static final RegistryObject<BlockEntityType<?>> TYPE_SHIELD_BLOCK3 = TILES.register("shield_block3", () -> BlockEntityType.Builder.of(ShieldModule::createProjector3, SHIELD_BLOCK3.get()).build(null));
    public static final RegistryObject<BlockEntityType<?>> TYPE_SHIELD_BLOCK4 = TILES.register("shield_block4", () -> BlockEntityType.Builder.of(ShieldModule::createProjector4, SHIELD_BLOCK4.get()).build(null));
    public static final RegistryObject<MenuType<GenericContainer>> CONTAINER_SHIELD = CONTAINERS.register("shield", GenericContainer::createContainerType);

    public static final RegistryObject<ShieldTemplateBlock> TEMPLATE_BLUE = BLOCKS.register("blue_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.BLUE));
    public static final RegistryObject<ShieldTemplateBlock> TEMPLATE_RED = BLOCKS.register("red_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.RED));
    public static final RegistryObject<ShieldTemplateBlock> TEMPLATE_GREEN = BLOCKS.register("green_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.GREEN));
    public static final RegistryObject<ShieldTemplateBlock> TEMPLATE_YELLOW = BLOCKS.register("yellow_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.YELLOW));

    public static final RegistryObject<Item> TEMPLATE_BLUE_ITEM = ITEMS.register("blue_shield_template_block", () -> new BlockItem(TEMPLATE_BLUE.get(), Registration.createStandardProperties()));
    public static final RegistryObject<Item> TEMPLATE_RED_ITEM = ITEMS.register("red_shield_template_block", () -> new BlockItem(TEMPLATE_RED.get(), Registration.createStandardProperties()));
    public static final RegistryObject<Item> TEMPLATE_GREEN_ITEM = ITEMS.register("green_shield_template_block", () -> new BlockItem(TEMPLATE_GREEN.get(), Registration.createStandardProperties()));
    public static final RegistryObject<Item> TEMPLATE_YELLOW_ITEM = ITEMS.register("yellow_shield_template_block", () -> new BlockItem(TEMPLATE_YELLOW.get(), Registration.createStandardProperties()));

    public static final RegistryObject<ShieldingBlock> SHIELDING_SOLID = BLOCKS.register("shielding_solid", ShieldingBlock::new);
    public static final RegistryObject<ShieldingBlock> SHIELDING_TRANSLUCENT = BLOCKS.register("shielding_translucent", ShieldingBlock::new);
    public static final RegistryObject<ShieldingBlock> SHIELDING_CUTOUT = BLOCKS.register("shielding_cutout", ShieldingBlock::new);
    public static final RegistryObject<BlockEntityType<?>> TYPE_SHIELDING = TILES.register("shielding", () -> BlockEntityType.Builder.of(ShieldingTileEntity::new,
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


    public ShieldModule() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ShieldModelLoader::register);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiShield.register();
        });

        ClientSetup.initClient();
    }

    @Override
    public void initConfig() {
        ShieldConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }

}
