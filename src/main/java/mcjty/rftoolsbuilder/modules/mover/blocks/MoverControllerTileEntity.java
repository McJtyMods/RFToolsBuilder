package mcjty.rftoolsbuilder.modules.mover.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ListCommand;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.OrientationTools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import mcjty.rftoolsbuilder.modules.mover.MoverConfiguration;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.client.GuiMoverController;
import mcjty.rftoolsbuilder.modules.mover.logic.MoverGraphNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public static final int MAXSCAN = 512;  //@todo configurable
    private final MoverGraphNode graph = new MoverGraphNode();

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

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        graph.load(tagCompound.getCompound("graph"));
    }

    @Override
    public void loadInfo(CompoundTag tagCompound) {
        super.loadInfo(tagCompound);
        CompoundTag info = tagCompound.getCompound("Info");
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tagCompound) {
        super.saveAdditional(tagCompound);
        tagCompound.put("graph", graph.save());
    }

    @Override
    public void saveInfo(CompoundTag tagCompound) {
        super.saveInfo(tagCompound);
        CompoundTag info = getOrCreateInfo(tagCompound);
    }


    private void doScan() {
        setChanged();
        graph.clear();
        Set<BlockPos> alreadyDone = new HashSet<>();
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            BlockPos moverPos = worldPosition.relative(direction);
            if (level.getBlockEntity(moverPos) instanceof MoverTileEntity mover) {
                // Find the first mover
                doScan(moverPos, graph, alreadyDone);
                return;
            }
        }
    }

    private void doScan(BlockPos moverPos, MoverGraphNode moverNode, Set<BlockPos> alreadyDone) {
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            for (int distance = 1 ; distance <= MAXSCAN ; distance++) {
                BlockPos newPos = moverPos.relative(direction, distance);
                // If we have already handled this position we can stop for this direction
                if (!alreadyDone.contains(newPos)) {
                    if (level.getBlockEntity(newPos) instanceof MoverTileEntity mover) {
                        alreadyDone.add(newPos);
                        MoverGraphNode child = new MoverGraphNode();
                        moverNode.add(direction, child);
                        child.add(direction.getOpposite(), moverNode);
                        doScan(newPos, child, alreadyDone);
                    }
                }
            }
        }
    }

    private List<String> getVehicles() {
        ArrayList<String> vehicles = new ArrayList<>();
        vehicles.add("Vroem");
        vehicles.add("Tuut");
        return vehicles;
    }

    private List<String> getNodes() {
        ArrayList<String> nodes = new ArrayList<>();
        nodes.add("Home");
        nodes.add("Leuven");
        nodes.add("London");
        return nodes;
    }

    @ServerCommand
    public static final Command<?> CMD_SCAN = Command.<MoverControllerTileEntity>create("scan", (te, player, params) -> te.doScan());

    @ServerCommand(type = String.class)
    public static final ListCommand<?, ?> CMD_GETVEHICLES = ListCommand.<MoverControllerTileEntity, String>create("rftoolsbuilder.movercontroller.getVehicles",
            (te, player, params) -> te.getVehicles(),
            (te, player, params, list) -> GuiMoverController.setVehiclesFromServer(list));

    @ServerCommand(type = String.class)
    public static final ListCommand<?, ?> CMD_GETNODES = ListCommand.<MoverControllerTileEntity, String>create("rftoolsbuilder.movercontroller.getNodes",
            (te, player, params) -> te.getNodes(),
            (te, player, params, list) -> GuiMoverController.setNodesFromServer(list));
}
