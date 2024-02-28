package mcjty.rftoolsbuilder.modules.mover;


import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.lib.setup.DeferredBlock;
import mcjty.lib.setup.DeferredItem;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.rftoolsbuilder.modules.mover.blocks.*;
import mcjty.rftoolsbuilder.modules.mover.client.ClientSetup;
import mcjty.rftoolsbuilder.modules.mover.client.GuiMover;
import mcjty.rftoolsbuilder.modules.mover.client.GuiMoverController;
import mcjty.rftoolsbuilder.modules.mover.client.GuiVehicleBuilder;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleCard;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleControlModuleItem;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleStatusModuleItem;
import mcjty.rftoolsbuilder.modules.mover.sound.Sounds;
import mcjty.rftoolsbuilder.setup.Config;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.api.distmarker.Dist;
import net.neoforged.neoforge.eventbus.api.IEventBus;
import net.neoforged.neoforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.function.Supplier;

import static mcjty.lib.datagen.BaseBlockStateProvider.RFTOOLSBASE_BOTTOM;
import static mcjty.lib.datagen.BaseBlockStateProvider.RFTOOLSBASE_TOP;
import static mcjty.lib.datagen.DataGen.has;
import static mcjty.rftoolsbuilder.RFToolsBuilder.tab;
import static mcjty.rftoolsbuilder.setup.Registration.*;

public class MoverModule implements IModule {

    public static final DeferredBlock<BaseBlock> MOVER = BLOCKS.register("mover", MoverTileEntity::createBlock);
    public static final DeferredItem<Item> MOVER_ITEM = ITEMS.register("mover", tab(() -> new BlockItem(MOVER.get(), createStandardProperties())));
    public static final Supplier<BlockEntityType<MoverTileEntity>> TYPE_MOVER = TILES.register("mover", () -> BlockEntityType.Builder.of(MoverTileEntity::new, MOVER.get()).build(null));
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_MOVER = CONTAINERS.register("mover", GenericContainer::createContainerType);

    public static final DeferredBlock<BaseBlock> MOVER_CONTROLLER = BLOCKS.register("mover_controller", MoverControllerTileEntity::createBlock);
    public static final DeferredItem<Item> MOVER_CONTROLLER_ITEM = ITEMS.register("mover_controller", tab(() -> new BlockItem(MOVER_CONTROLLER.get(), createStandardProperties())));
    public static final Supplier<BlockEntityType<MoverControllerTileEntity>> TYPE_MOVER_CONTROLLER = TILES.register("mover_controller", () -> BlockEntityType.Builder.of(MoverControllerTileEntity::new, MOVER_CONTROLLER.get()).build(null));
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_MOVER_CONTROLLER = CONTAINERS.register("mover_controller", GenericContainer::createContainerType);

    public static final DeferredBlock<BaseBlock> VEHICLE_BUILDER = BLOCKS.register("vehicle_builder", VehicleBuilderTileEntity::createBlock);
    public static final DeferredItem<Item> VEHICLE_BUILDER_ITEM = ITEMS.register("vehicle_builder", tab(() -> new BlockItem(VEHICLE_BUILDER.get(), createStandardProperties())));
    public static final Supplier<BlockEntityType<?>> TYPE_VEHICLE_BUILDER = TILES.register("vehicle_builder", () -> BlockEntityType.Builder.of(VehicleBuilderTileEntity::new, VEHICLE_BUILDER.get()).build(null));
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_VEHICLE_BUILDER = CONTAINERS.register("vehicle_builder", GenericContainer::createContainerType);

    public static final DeferredBlock<InvisibleMoverBlock> INVISIBLE_MOVER_BLOCK = BLOCKS.register("invisible_mover", InvisibleMoverBlock::new);
    public static final Supplier<BlockEntityType<?>> TYPE_INVISIBLE_MOVER = TILES.register("invisible_mover", () -> BlockEntityType.Builder.of(InvisibleMoverBE::new, INVISIBLE_MOVER_BLOCK.get()).build(null));

    public static final DeferredBlock<Block> MOVER_CONTROL_BLOCK = BLOCKS.register("mover_control", () -> new MoverControlBlock(0));
    public static final DeferredItem<Item> MOVER_CONTROL_ITEM = ITEMS.register("mover_control", tab(() -> new BlockItem(MOVER_CONTROL_BLOCK.get(), createStandardProperties())));
    public static final DeferredBlock<Block> MOVER_CONTROL2_BLOCK = BLOCKS.register("mover_control2", () -> new MoverControlBlock(1));
    public static final DeferredItem<Item> MOVER_CONTROL2_ITEM = ITEMS.register("mover_control2", tab(() -> new BlockItem(MOVER_CONTROL2_BLOCK.get(), createStandardProperties())));
    public static final DeferredBlock<Block> MOVER_CONTROL3_BLOCK = BLOCKS.register("mover_control3", () -> new MoverControlBlock(2));
    public static final DeferredItem<Item> MOVER_CONTROL3_ITEM = ITEMS.register("mover_control3", tab(() -> new BlockItem(MOVER_CONTROL3_BLOCK.get(), createStandardProperties())));
    public static final DeferredBlock<Block> MOVER_CONTROL4_BLOCK = BLOCKS.register("mover_control4", () -> new MoverControlBlock(3));
    public static final DeferredItem<Item> MOVER_CONTROL4_ITEM = ITEMS.register("mover_control4", tab(() -> new BlockItem(MOVER_CONTROL4_BLOCK.get(), createStandardProperties())));

    public static final DeferredBlock<Block> MOVER_STATUS_BLOCK = BLOCKS.register("mover_status", MoverStatusBlock::new);
    public static final DeferredItem<Item> MOVER_STATUS_ITEM = ITEMS.register("mover_status", tab(() -> new BlockItem(MOVER_STATUS_BLOCK.get(), createStandardProperties())));

    public static final DeferredItem<VehicleCard> VEHICLE_CARD = ITEMS.register("vehicle_card", tab(VehicleCard::new));
    public static final DeferredItem<VehicleControlModuleItem> VEHICLE_CONTROL_MODULE = ITEMS.register("vehicle_control_module", tab(VehicleControlModuleItem::new));
    public static final DeferredItem<VehicleStatusModuleItem> VEHICLE_STATUS_MODULE = ITEMS.register("vehicle_status_module", tab(VehicleStatusModuleItem::new));

    public MoverModule(IEventBus bus, Dist dist) {
        Sounds.init();
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
    public void initConfig(IEventBus bus) {
        MoverConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }

    @Override
    public void initDatagen(DataGen dataGen) {
        dataGen.add(
                Dob.blockBuilder(MOVER)
                        .ironPickaxeTags()
                        .parentedItem("block/mover")
                        .standardLoot(TYPE_MOVER)
                        .blockState(p -> p.simpleBlock(MOVER.get(), p.frontBasedModel("mover", p.modLoc("block/machinemover"), p.modLoc("block/machinemover"), RFTOOLSBASE_TOP, RFTOOLSBASE_BOTTOM)))
                        .shaped(builder -> builder
                                        .define('F', VariousModule.MACHINE_FRAME.get())
                                        .define('C', Blocks.RAIL)
                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                                "iTi", "CFC", "iTi"),
                Dob.blockBuilder(MOVER_CONTROLLER)
                        .ironPickaxeTags()
                        .parentedItem("block/mover_controller")
                        .standardLoot(TYPE_MOVER_CONTROLLER)
                        .blockState(p -> p.orientedBlock(MOVER_CONTROLLER.get(), p.frontBasedModel("mover_controller", p.modLoc("block/machinemovercontroller"))))
                        .shaped(builder -> builder
                                        .define('F', VariousModule.MACHINE_FRAME.get())
                                        .define('C', Blocks.ACTIVATOR_RAIL)
                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                                "iTo", "CFC", "oTi"),
                Dob.blockBuilder(VEHICLE_BUILDER)
                        .ironPickaxeTags()
                        .parentedItem("block/vehicle_builder")
                        .standardLoot(TYPE_VEHICLE_BUILDER)
                        .blockState(p -> p.orientedBlock(VEHICLE_BUILDER.get(), p.frontBasedModel("vehicle_builder", p.modLoc("block/machinevehiclebuilder"))))
                        .shaped(builder -> builder
                                        .define('F', VariousModule.MACHINE_FRAME.get())
                                        .define('C', Items.MINECART)
                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                                "iCi", "rFr", "iTi"),
                Dob.blockBuilder(MOVER_CONTROL_BLOCK)
                        .ironPickaxeTags()
                        .simpleLoot()
                        .parentedItem("block/mover_control_0")
                        .blockState(p -> DataGenHelper.create24Model(p, MOVER_CONTROL_BLOCK.get(), "mover_control_", "block/movercontrol"))
                        .shaped(builder -> builder
                                        .define('F', VariousModule.MACHINE_FRAME.get())
                                        .define('C', Blocks.REPEATER)
                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                                "rTr", "CFC", "iTi")
                        .shapeless("mover_control_back", builder -> builder
                                .requires(MOVER_CONTROL4_BLOCK.get())
                                .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get()))),
                Dob.blockBuilder(MOVER_CONTROL2_BLOCK)
                        .ironPickaxeTags()
                        .simpleLoot()
                        .parentedItem("block/mover_control2_0")
                        .blockState(p -> DataGenHelper.create24Model(p, MOVER_CONTROL2_BLOCK.get(), "mover_control2_", "block/movercontrol2"))
                        .shapeless(builder -> builder
                                .requires(MOVER_CONTROL_BLOCK.get())
                                .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get()))),
                Dob.blockBuilder(MOVER_CONTROL3_BLOCK)
                        .ironPickaxeTags()
                        .simpleLoot()
                        .parentedItem("block/mover_control3_0")
                        .blockState(p -> DataGenHelper.create24Model(p, MOVER_CONTROL3_BLOCK.get(), "mover_control3_", "block/movercontrol3"))
                        .shapeless(builder -> builder
                                .requires(MOVER_CONTROL2_BLOCK.get())
                                .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get()))),
                Dob.blockBuilder(MOVER_CONTROL4_BLOCK)
                        .ironPickaxeTags()
                        .simpleLoot()
                        .parentedItem("block/mover_control4_0")
                        .blockState(p -> DataGenHelper.create24Model(p, MOVER_CONTROL4_BLOCK.get(), "mover_control4_", "block/movercontrol4"))
                        .shapeless(builder -> builder
                                .requires(MOVER_CONTROL3_BLOCK.get())
                                .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get()))),
                Dob.blockBuilder(MOVER_STATUS_BLOCK)
                        .ironPickaxeTags()
                        .simpleLoot()
                        .parentedItem("block/mover_status_0")
                        .blockState(p -> DataGenHelper.create24Model(p, MOVER_STATUS_BLOCK.get(), "mover_status_", "block/moverstatus"))
                        .shaped(builder -> builder
                                        .define('F', VariousModule.MACHINE_FRAME.get())
                                        .define('C', Blocks.COMPARATOR)
                                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                                "rTr", "CFC", "iTi"),
                Dob.itemBuilder(VEHICLE_CARD)
                        .generatedItem("item/vehiclecard")
                        .shaped(builder -> builder
                                        .define('C', Items.MINECART)
                                        .unlockedBy("paper", has(Items.PAPER)),
                                " C ", "rpr", " r "),
                Dob.itemBuilder(VEHICLE_CONTROL_MODULE)
                        .generatedItem("item/vehiclecontrolmoduleitem")
                        .shaped(builder -> builder
                                        .define('C', VEHICLE_CARD.get())
                                        .unlockedBy("paper", has(Items.PAPER)),
                                " C ", "rpr", " r "),
                Dob.itemBuilder(VEHICLE_STATUS_MODULE)
                        .generatedItem("item/vehiclestatusmoduleitem")
                        .shaped(builder -> builder
                                        .define('C', VEHICLE_CARD.get())
                                        .define('g', Items.COMPARATOR)
                                        .unlockedBy("paper", has(Items.PAPER)),
                                " C ", "rpr", " g ")
        );
    }
}
