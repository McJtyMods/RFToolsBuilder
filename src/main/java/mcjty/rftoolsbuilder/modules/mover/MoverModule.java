package mcjty.rftoolsbuilder.modules.mover;


import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbuilder.modules.mover.blocks.InvisibleMoverBlock;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import mcjty.rftoolsbuilder.modules.mover.blocks.VehicleBuilderTileEntity;
import mcjty.rftoolsbuilder.modules.mover.client.GuiMover;
import mcjty.rftoolsbuilder.modules.mover.client.GuiVehicleBuilder;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleCard;
import mcjty.rftoolsbuilder.setup.Config;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import static mcjty.rftoolsbuilder.setup.Registration.*;

public class MoverModule implements IModule {

    public static final RegistryObject<BaseBlock> MOVER = BLOCKS.register("mover", MoverTileEntity::createBlock);
    public static final RegistryObject<Item> MOVER_ITEM = ITEMS.register("mover", () -> new BlockItem(MOVER.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<MoverTileEntity>> TYPE_MOVER = TILES.register("mover", () -> BlockEntityType.Builder.of(MoverTileEntity::new, MOVER.get()).build(null));
    public static final RegistryObject<MenuType<GenericContainer>> CONTAINER_MOVER = CONTAINERS.register("mover", GenericContainer::createContainerType);

    public static final RegistryObject<BaseBlock> VEHICLE_BUILDER = BLOCKS.register("vehicle_builder", VehicleBuilderTileEntity::createBlock);
    public static final RegistryObject<Item> VEHICLE_BUILDER_ITEM = ITEMS.register("vehicle_builder", () -> new BlockItem(VEHICLE_BUILDER.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<?>> TYPE_VEHICLE_BUILDER = TILES.register("vehicle_builder", () -> BlockEntityType.Builder.of(VehicleBuilderTileEntity::new, VEHICLE_BUILDER.get()).build(null));
    public static final RegistryObject<MenuType<GenericContainer>> CONTAINER_VEHICLE_BUILDER = CONTAINERS.register("vehicle_builder", GenericContainer::createContainerType);

    public static final RegistryObject<Block> INVISIBLE_MOVER_BLOCK = BLOCKS.register("invisible_mover_block", InvisibleMoverBlock::new);

    public static final RegistryObject<VehicleCard> VEHICLE_CARD = ITEMS.register("vehicle_card", VehicleCard::new);

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiMover.register();
            GuiVehicleBuilder.register();
        });
    }

    @Override
    public void initConfig() {
        MoverConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }
}
