package mcjty.rftoolsbuilder.modules.mover.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.blocks.VehicleBuilderTileEntity;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class GuiVehicleBuilder extends GenericGuiContainer<VehicleBuilderTileEntity, GenericContainer> {

    public GuiVehicleBuilder(VehicleBuilderTileEntity te, GenericContainer container, Inventory inventory) {
        super(te, container, inventory, MoverModule.VEHICLE_BUILDER.get().getManualEntry());
    }

    public static void register() {
        register(MoverModule.CONTAINER_VEHICLE_BUILDER.get(), GuiVehicleBuilder::new);
    }

    @Override
    public void init() {
        window = new Window(this, tileEntity, RFToolsBuilderMessages.INSTANCE, new ResourceLocation(RFToolsBuilder.MODID, "gui/vehicle_builder.gui"));
        super.init();
        initializeFields();
    }

    private void initializeFields() {
    }

    private void updateFields() {
        if (window == null) {
            return;
        }
    }

    @Override
    protected void renderBg(@Nonnull PoseStack matrixStack, float partialTicks, int x, int y) {
        updateFields();
        drawWindow(matrixStack);
    }
}
