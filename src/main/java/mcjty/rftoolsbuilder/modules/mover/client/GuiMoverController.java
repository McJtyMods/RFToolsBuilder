package mcjty.rftoolsbuilder.modules.mover.client;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.GuiPopupTools;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.SelectionEvent;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.network.PacketGetListFromServer;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverControllerTileEntity;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;

import static mcjty.lib.gui.widgets.Widgets.horizontal;
import static mcjty.lib.gui.widgets.Widgets.label;
import static mcjty.rftoolsbuilder.modules.mover.blocks.MoverControllerTileEntity.CMD_GETNODES;
import static mcjty.rftoolsbuilder.modules.mover.blocks.MoverControllerTileEntity.CMD_GETVEHICLES;

public class GuiMoverController extends GenericGuiContainer<MoverControllerTileEntity, GenericContainer> {

    private EnergyBar energyBar;

    private SyncedList<String> vehicleList;
    private SyncedList<Pair<BlockPos, String>> nodeList;

    public GuiMoverController(MoverControllerTileEntity builderTileEntity, GenericContainer container, Inventory inventory) {
        super(builderTileEntity, container, inventory, MoverModule.MOVER_CONTROLLER.get().getManualEntry());
    }

    public static void register() {
        register(MoverModule.CONTAINER_MOVER_CONTROLLER.get(), GuiMoverController::new);
    }

    @Override
    public void init() {
        window = new Window(this, tileEntity, RFToolsBuilderMessages.INSTANCE, new ResourceLocation(RFToolsBuilder.MODID, "gui/mover_controller.gui"));
        super.init();

        initializeFields();
        setupEvents();

        vehicleList.refresh();
        nodeList.refresh();
    }

    private void setupEvents() {
        window.<Button>findChild("scan").event(this::doScan);
        window.<Button>findChild("move").event(this::doMove);
        nodeList.getList().event(new SelectionEvent() {
            @Override
            public void select(int index) {
            }

            @Override
            public void doubleClick(int index) {
                selectNode();
            }
        });
    }

    private void doMove() {
        if (nodeList.getSelected() == null) {
            GuiPopupTools.showMessage(minecraft, this, getWindowManager(), 100, 100, "Please select a node!");
            return;
        }
        if (vehicleList.getSelected() == null) {
            GuiPopupTools.showMessage(minecraft, this, getWindowManager(), 100, 100, "Please select a vehicle!");
            return;
        }
        sendServerCommandTyped(RFToolsBuilderMessages.INSTANCE, MoverControllerTileEntity.CMD_MOVE, TypedMap.builder()
                .put(MoverControllerTileEntity.SELECTED_NODE, nodeList.getSelected().getLeft())
                .put(MoverControllerTileEntity.SELECTED_VEHICLE, vehicleList.getSelected())
                .put(MoverControllerTileEntity.SELECTED_DESTINATION, nodeList.getSelected().getRight())
                .build());
        vehicleList.refresh();
        nodeList.refresh();
    }

    private void doScan() {
        sendServerCommandTyped(RFToolsBuilderMessages.INSTANCE, MoverControllerTileEntity.CMD_SCAN, TypedMap.EMPTY);
        vehicleList.refresh();
        nodeList.refresh();
    }

    private void selectNode() {
        Pair<BlockPos, String> selected = nodeList.getSelected();
        if (selected != null) {
            sendServerCommandTyped(RFToolsBuilderMessages.INSTANCE, MoverControllerTileEntity.CMD_SELECTNODE, TypedMap.builder()
                    .put(MoverControllerTileEntity.SELECTED_NODE, selected.getLeft())
                    .build());
        }
    }

    private void initializeFields() {
        energyBar = window.findChild("energybar");
        vehicleList = new SyncedList<>(window.findChild("vehicles"), this::requestVehicles, this::makeVehicleLine, 10);
        nodeList = new SyncedList<>(window.findChild("nodes"), this::requestNodes, this::makeNodeLine, 20);

        updateFields();
    }

    private void updateFields() {
        updateEnergyBar(energyBar);
        vehicleList.populateLists();
        nodeList.populateLists();
    }

    public static void setVehiclesFromServer(List<String> vehicles) {
        if (Minecraft.getInstance().screen instanceof GuiMoverController gui) {
            gui.vehicleList.setFromServerList(vehicles);
        } else {
            RFToolsBuilder.setup.getLogger().warn("This is not a gui for the mover controller!");
        }
    }

    public static void setNodesFromServer(List<Pair<BlockPos, String>> nodes) {
        if (Minecraft.getInstance().screen instanceof GuiMoverController gui) {
            gui.nodeList.setFromServerList(nodes);
        } else {
            RFToolsBuilder.setup.getLogger().warn("This is not a gui for the mover controller!");
        }
    }

    public static void setSelectedVehicle(String vehicle) {
        if (Minecraft.getInstance().screen instanceof GuiMoverController gui) {
            gui.vehicleList.select(vehicle);
        } else {
            RFToolsBuilder.setup.getLogger().warn("This is not a gui for the mover controller!");
        }
    }


    private void requestVehicles() {
        RFToolsBuilderMessages.sendToServer(PacketGetListFromServer.create(tileEntity.getBlockPos(), CMD_GETVEHICLES.name()));
    }

    private void requestNodes() {
        RFToolsBuilderMessages.sendToServer(PacketGetListFromServer.create(tileEntity.getBlockPos(), CMD_GETNODES.name()));
    }

    private Panel makeVehicleLine(String vehicle) {
        Panel panel = horizontal(0, 0).hint(0, 0, 130, 14);
        panel.children(label(vehicle));
        return panel;
    }

    private Panel makeNodeLine(Pair<BlockPos, String> node) {
        Panel panel = horizontal(0, 0).hint(0, 0, 100, 14);
        panel.children(label(node.getRight()));
        return panel;
    }


    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        updateFields();

        drawWindow(graphics);
    }
}
