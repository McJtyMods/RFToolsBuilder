package mcjty.rftoolsbuilder.modules.mover;


import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbuilder.modules.mover.blocks.*;
import mcjty.rftoolsbuilder.modules.mover.client.ClientSetup;
import mcjty.rftoolsbuilder.modules.mover.client.GuiMover;
import mcjty.rftoolsbuilder.modules.mover.client.GuiMoverController;
import mcjty.rftoolsbuilder.modules.mover.client.GuiVehicleBuilder;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleCard;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleControlModuleItem;
import mcjty.rftoolsbuilder.setup.Config;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;

import static mcjty.rftoolsbuilder.setup.Registration.*;

public class MoverModule implements IModule {

    public static final RegistryObject<BaseBlock> MOVER = BLOCKS.register("mover", MoverTileEntity::createBlock);
    public static final RegistryObject<Item> MOVER_ITEM = ITEMS.register("mover", () -> new BlockItem(MOVER.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<MoverTileEntity>> TYPE_MOVER = TILES.register("mover", () -> BlockEntityType.Builder.of(MoverTileEntity::new, MOVER.get()).build(null));
    public static final RegistryObject<MenuType<GenericContainer>> CONTAINER_MOVER = CONTAINERS.register("mover", GenericContainer::createContainerType);

    public static final RegistryObject<BaseBlock> MOVER_CONTROLLER = BLOCKS.register("mover_controller", MoverControllerTileEntity::createBlock);
    public static final RegistryObject<Item> MOVER_CONTROLLER_ITEM = ITEMS.register("mover_controller", () -> new BlockItem(MOVER_CONTROLLER.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<MoverControllerTileEntity>> TYPE_MOVER_CONTROLLER = TILES.register("mover_controller", () -> BlockEntityType.Builder.of(MoverControllerTileEntity::new, MOVER_CONTROLLER.get()).build(null));
    public static final RegistryObject<MenuType<GenericContainer>> CONTAINER_MOVER_CONTROLLER = CONTAINERS.register("mover_controller", GenericContainer::createContainerType);

    public static final RegistryObject<BaseBlock> VEHICLE_BUILDER = BLOCKS.register("vehicle_builder", VehicleBuilderTileEntity::createBlock);
    public static final RegistryObject<Item> VEHICLE_BUILDER_ITEM = ITEMS.register("vehicle_builder", () -> new BlockItem(VEHICLE_BUILDER.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<?>> TYPE_VEHICLE_BUILDER = TILES.register("vehicle_builder", () -> BlockEntityType.Builder.of(VehicleBuilderTileEntity::new, VEHICLE_BUILDER.get()).build(null));
    public static final RegistryObject<MenuType<GenericContainer>> CONTAINER_VEHICLE_BUILDER = CONTAINERS.register("vehicle_builder", GenericContainer::createContainerType);

    public static final RegistryObject<InvisibleMoverBlock> INVISIBLE_MOVER_BLOCK = BLOCKS.register("invisible_mover", InvisibleMoverBlock::new);

    public static final RegistryObject<Block> MOVER_CONTROL_BLOCK = BLOCKS.register("mover_control", MoverControlBlock::new);
    public static final RegistryObject<Item> MOVER_CONTROL_ITEM = ITEMS.register("mover_control", () -> new BlockItem(MOVER_CONTROL_BLOCK.get(), createStandardProperties()));
    public static final RegistryObject<Block> MOVER_STATUS_BLOCK = BLOCKS.register("mover_status", MoverStatusBlock::new);
    public static final RegistryObject<Item> MOVER_STATUS_ITEM = ITEMS.register("mover_status", () -> new BlockItem(MOVER_STATUS_BLOCK.get(), createStandardProperties()));

    public static final RegistryObject<VehicleCard> VEHICLE_CARD = ITEMS.register("vehicle_card", VehicleCard::new);
    public static final RegistryObject<VehicleControlModuleItem> VEHICLE_CONTROL_MODULE = ITEMS.register("vehicle_control_module", VehicleControlModuleItem::new);

    public MoverModule() {
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
        modbus.addListener(ClientSetup::onTextureStitch);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {
    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        ClientSetup.initClient();
        event.enqueueWork(() -> {
            GuiMover.register();
            GuiMoverController.register();
            GuiVehicleBuilder.register();
        });
    }

    @Override
    public void initConfig() {
        MoverConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }
}
