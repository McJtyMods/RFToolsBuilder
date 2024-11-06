package mcjty.rftoolsbuilder.modules.mover.client;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import javax.annotation.Nonnull;

public class GuiMover extends GenericGuiContainer<MoverTileEntity, GenericContainer> {

    public GuiMover(GenericContainer container, Inventory inventory, Component title) {
        super(container, inventory, title, MoverModule.MOVER.get().getManualEntry());
    }

    public static void register(RegisterMenuScreensEvent event) {
        event.register(MoverModule.CONTAINER_MOVER.get(), GuiMover::new);
    }

    @Override
    public void init() {
        window = new Window(this, getBE(), ResourceLocation.fromNamespaceAndPath(RFToolsBuilder.MODID, "gui/mover.gui"));
        super.init();

        initializeFields();
        setupEvents();
    }

    private void setupEvents() {
    }

    private void initializeFields() {
        updateFields();
    }

    private void updateFields() {
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        updateFields();
        drawWindow(graphics, partialTicks, mouseX, mouseY);
    }
}
