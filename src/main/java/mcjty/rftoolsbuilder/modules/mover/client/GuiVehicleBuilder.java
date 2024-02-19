package mcjty.rftoolsbuilder.modules.mover.client;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Button;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.blocks.VehicleBuilderTileEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class GuiVehicleBuilder extends GenericGuiContainer<VehicleBuilderTileEntity, GenericContainer> {

    private Button createButton;

    public GuiVehicleBuilder(VehicleBuilderTileEntity te, GenericContainer container, Inventory inventory) {
        super(te, container, inventory, MoverModule.VEHICLE_BUILDER.get().getManualEntry());
    }

    public static void register() {
        register(MoverModule.CONTAINER_VEHICLE_BUILDER.get(), GuiVehicleBuilder::new);
    }

    @Override
    public void init() {
        window = new Window(this, tileEntity, new ResourceLocation(RFToolsBuilder.MODID, "gui/vehicle_builder.gui"));
        super.init();
        initializeFields();
    }

    private void initializeFields() {
        createButton = window.findChild("create");
    }

    private void updateFields() {
        if (window == null) {
            return;
        }
        ItemStack spaceCard = tileEntity.getItems().getStackInSlot(VehicleBuilderTileEntity.SLOT_SPACE_CARD);
        ItemStack vehicleCard = tileEntity.getItems().getStackInSlot(VehicleBuilderTileEntity.SLOT_VEHICLE_CARD);
        createButton.enabled(VehicleBuilderTileEntity.isUsableSpaceCard(spaceCard) && VehicleBuilderTileEntity.isVehicleCard(vehicleCard));
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTicks, int x, int y) {
        updateFields();
        drawWindow(graphics);
    }
}
