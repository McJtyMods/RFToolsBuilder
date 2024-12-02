package mcjty.rftoolsbuilder.modules.mover.client;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Button;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.blocks.VehicleBuilderTileEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import javax.annotation.Nonnull;

public class GuiVehicleBuilder extends GenericGuiContainer<VehicleBuilderTileEntity, GenericContainer> {

    private Button createButton;

    public GuiVehicleBuilder(GenericContainer container, Inventory inventory, Component title) {
        super(container, inventory, title, MoverModule.VEHICLE_BUILDER.block().get().getManualEntry());
    }

    public static void register(RegisterMenuScreensEvent event) {
        event.register(MoverModule.CONTAINER_VEHICLE_BUILDER.get(), GuiVehicleBuilder::new);
    }

    @Override
    public void init() {
        window = new Window(this, getBE(), ResourceLocation.fromNamespaceAndPath(RFToolsBuilder.MODID, "gui/vehicle_builder.gui"));
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
        VehicleBuilderTileEntity tileEntity = getBE();
        ItemStack spaceCard = tileEntity.getItems().getStackInSlot(VehicleBuilderTileEntity.SLOT_SPACE_CARD);
        ItemStack vehicleCard = tileEntity.getItems().getStackInSlot(VehicleBuilderTileEntity.SLOT_VEHICLE_CARD);
        createButton.enabled(VehicleBuilderTileEntity.isUsableSpaceCard(spaceCard) && VehicleBuilderTileEntity.isVehicleCard(vehicleCard));
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTicks, int x, int y) {
        updateFields();
        drawWindow(graphics, partialTicks, x, y);
    }
}
