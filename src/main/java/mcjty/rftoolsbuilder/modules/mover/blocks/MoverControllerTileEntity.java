package mcjty.rftoolsbuilder.modules.mover.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ISerializer;
import mcjty.lib.blockcommands.ListCommand;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.OrientationTools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import mcjty.rftoolsbuilder.modules.mover.MoverConfiguration;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.client.GuiMoverController;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleCard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static mcjty.lib.api.container.DefaultContainerProvider.empty;
import static mcjty.lib.builder.TooltipBuilder.*;

public class MoverControllerTileEntity extends GenericTileEntity {

    @Cap(type = CapType.ENERGY)
    private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(this, true, MoverConfiguration.MAXENERGY.get(), MoverConfiguration.RECEIVEPERTICK.get());

    @Cap(type = CapType.CONTAINER)
    private final LazyOptional<MenuProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Mover")
            .containerSupplier(empty(MoverModule.CONTAINER_MOVER_CONTROLLER, this))
            .energyHandler(() -> energyStorage)
            .setupSync(this));


    @Cap(type = CapType.INFUSABLE)
    private final IInfusable infusable = new DefaultInfusable(MoverControllerTileEntity.this);

    public static final int MAXSCAN = 128;  //@todo configurable

    // For the gui: the selected vehicle
    @GuiValue
    public static final Value<?, String> VALUE_SELECTED_VEHICLE = Value.create("selectedVehicle", Type.STRING, MoverControllerTileEntity::getSelectedVehicle, MoverControllerTileEntity::setSelectedVehicle);
    private String selectedVehicle;

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .tileEntitySupplier(MoverControllerTileEntity::new)
                .topDriver(RFToolsBuilderTOPDriver.DRIVER)
                .infusable()
                .manualEntry(ManualHelper.create("rftoolsbuilder:todo"))
                .info(key("message.rftoolsbuilder.shiftmessage"))
                .infoShift(header(), gold()));
    }


    public MoverControllerTileEntity(BlockPos pos, BlockState state) {
        super(MoverModule.TYPE_MOVER_CONTROLLER.get(), pos, state);
    }

    private void selectNode(BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof MoverTileEntity mover) {
            ItemStack card = mover.getCard();
            if (card.isEmpty()) {
                selectedVehicle = null;
            } else {
                selectedVehicle = VehicleCard.getVehicleName(card);
            }
        } else {
            selectedVehicle = null;
        }
    }

    public String getSelectedVehicle() {
        return selectedVehicle;
    }

    public void setSelectedVehicle(String vehicle) {
        this.selectedVehicle = vehicle;
        if (level.isClientSide) {
            GuiMoverController.setSelectedVehicle(vehicle);
        }
    }

    // @todo make other version of traverse too

    @Nullable
    public <T> T traverseBreadthFirst(BiFunction<BlockPos, MoverTileEntity, T> function) {
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            BlockPos moverPos = worldPosition.relative(direction);
            if (level.getBlockEntity(moverPos) instanceof MoverTileEntity mover) {
                return mover.traverseBreadthFirst(function);
            }
        }
        return null;
    }

    @Nullable
    private <T> T traverseDepthFirst(BiFunction<BlockPos, MoverTileEntity, T> function) {
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            BlockPos moverPos = worldPosition.relative(direction);
            if (level.getBlockEntity(moverPos) instanceof MoverTileEntity mover) {
                return mover.traverseDepthFirst(function);
            }
        }
        return null;
    }

    // Find a mover and an optional vehicle and setup movement
    // The vehicle can be null or empty string in which case we take the
    // vehicle nearest to the mover
    public void setupMovement(String moverName, String vehicle) {
        MoverTileEntity destinationMover = findMover(moverName);
        if (destinationMover != null) {
            if (vehicle == null || vehicle.trim().isEmpty()) {
                // Find the vehicle closest to this mover
                vehicle = traverseBreadthFirst((p, mover) -> {
                    ItemStack card = mover.getCard();
                    if (!card.isEmpty()) {
                        return VehicleCard.getVehicleName(card);
                    } else {
                        return null;
                    }
                });
            }
            if (vehicle != null) {
                startMove(destinationMover.getBlockPos(), vehicle, destinationMover.getName());
            }
        }
    }

    // Find a vehicle and setup movement to a certain node
    private void startMove(BlockPos destination, String vehicle, String destinationName) {
        MoverTileEntity moverContainingVehicle = findVehicle(vehicle);

        if (moverContainingVehicle != null) {
            ItemStack card = moverContainingVehicle.getCard();
            VehicleCard.setDesiredDestination(card, destination, destinationName);
        }
    }

    @Nullable
    private MoverTileEntity findMover(String moverName) {
        return traverseDepthFirst((p, mover) -> {
            if (Objects.equals(moverName, mover.getName())) {
                return mover;
            } else {
                return null;
            }
        });
    }

    @Nullable
    private MoverTileEntity findVehicle(String vehicle) {
        return traverseDepthFirst((p, mover) -> {
            ItemStack card = mover.getCard();
            String name = VehicleCard.getVehicleName(card);
            if (Objects.equals(name, vehicle)) {
                return mover;
            } else {
                return null;
            }
        });
    }

    private void doScan() {
        setChanged();
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            BlockPos moverPos = worldPosition.relative(direction);
            if (level.getBlockEntity(moverPos) instanceof MoverTileEntity mover) {
                Set<BlockPos> alreadyHandled = new HashSet<>();
                alreadyHandled.add(moverPos);
                doScan(moverPos, mover, alreadyHandled);
                return;
            }
        }
    }

    private void doScan(BlockPos moverPos, MoverTileEntity mover, Set<BlockPos> alreadyHandled) {
        mover.clearNetwork();
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            for (int distance = 1; distance <= MAXSCAN; distance++) {
                BlockPos destPos = moverPos.relative(direction, distance);
                if (level.getBlockEntity(destPos) instanceof MoverTileEntity destMover) {
                    mover.addConnection(direction, destPos);
                    if (!alreadyHandled.contains(destPos)) {
                        alreadyHandled.add(destPos);
                        doScan(destPos, destMover, alreadyHandled);
                    }
                    break;  // Stop at the first mover we find
                }
            }
        }
    }

    public List<String> getMovers() {
        List<String> movers = new ArrayList<>();
        traverseDepthFirst((p, mover) -> {
            movers.add(mover.getName());
            return null;
        });
        return movers;
    }


    private List<String> getVehicles() {
        List<String> vehicles = new ArrayList<>();
        traverseDepthFirst((p, mover) -> {
            ItemStack card = mover.getCard();
            if (!card.isEmpty()) {
                String name = VehicleCard.getVehicleName(card);
                BlockPos destination = VehicleCard.getDesiredDestination(card);
                String destinationName = VehicleCard.getDesiredDestinationName(card);
                if (destination != null) {
                    name += " -> " + destinationName;
                }
                vehicles.add(name);
            }
            return null;
        });
        return vehicles;
    }

    private List<Pair<BlockPos, String>> getNodes() {
        List<Pair<BlockPos, String>> nodeNames = new ArrayList<>();
        traverseDepthFirst((p, mover) -> {
            String name = mover.getName();
            if (name == null || name.trim().isEmpty()) {
                name = p.getX() + "," + p.getY() + "," + p.getZ();
            }
            nodeNames.add(Pair.of(p, name));
            return null;
        });
        return nodeNames;
    }

    public static final Key<BlockPos> SELECTED_NODE = new Key<>("node", Type.BLOCKPOS);
    public static final Key<String> SELECTED_VEHICLE = new Key<>("vehicle", Type.STRING);
    public static final Key<String> SELECTED_DESTINATION = new Key<>("destination", Type.STRING);

    @ServerCommand
    public static final Command<?> CMD_SCAN = Command.<MoverControllerTileEntity>create("scan", (te, player, params) -> te.doScan());
    @ServerCommand
    public static final Command<?> CMD_MOVE = Command.<MoverControllerTileEntity>create("move", (te, player, params) ->
            te.startMove(params.get(SELECTED_NODE), params.get(SELECTED_VEHICLE), params.get(SELECTED_DESTINATION)));

    @ServerCommand
    public static final Command<?> CMD_SELECTNODE = Command.<MoverControllerTileEntity>create("selectNode", (te, player, params) -> te.selectNode(params.get(SELECTED_NODE)));

    @ServerCommand(type = String.class)
    public static final ListCommand<?, ?> CMD_GETVEHICLES = ListCommand.<MoverControllerTileEntity, String>create("rftoolsbuilder.movercontroller.getVehicles",
            (te, player, params) -> te.getVehicles(),
            (te, player, params, list) -> GuiMoverController.setVehiclesFromServer(list));

    @ServerCommand(type = Pair.class, serializer = NodePairSerializer.class)
    public static final ListCommand<?, ?> CMD_GETNODES = ListCommand.<MoverControllerTileEntity, Pair<BlockPos, String>>create("rftoolsbuilder.movercontroller.getNodes",
            (te, player, params) -> te.getNodes(),
            (te, player, params, list) -> GuiMoverController.setNodesFromServer(list));

public static class NodePairSerializer implements ISerializer<Pair<BlockPos, String>> {
    @Override
    public Function<FriendlyByteBuf, Pair<BlockPos, String>> getDeserializer() {
        return buf -> Pair.of(buf.readBlockPos(), buf.readUtf(32767));
    }

    @Override
    public BiConsumer<FriendlyByteBuf, Pair<BlockPos, String>> getSerializer() {
        return (buf, pair) -> {
            buf.writeBlockPos(pair.getLeft());
            buf.writeUtf(pair.getRight());
        };
    }
}
}
