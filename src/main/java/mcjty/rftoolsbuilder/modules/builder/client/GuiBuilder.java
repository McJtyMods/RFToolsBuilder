package mcjty.rftoolsbuilder.modules.builder.client;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.network.PacketAttachmentData;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.AnchorMode;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.builder.network.PacketCloseContainerAndOpenCardGui;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;

import static mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity.*;

public class GuiBuilder extends GenericGuiContainer<BuilderTileEntity, GenericContainer> {

    private EnergyBar energyBar;
    private Button currentLevel;
    private final ImageChoiceLabel[] anchor = new ImageChoiceLabel[4];

    public GuiBuilder(GenericContainer container, Inventory inventory, Component title) {
        super(container, inventory, title, BuilderModule.BUILDER.block().get().getManualEntry());
    }

    public static void register(RegisterMenuScreensEvent event) {
        event.register(BuilderModule.CONTAINER_BUILDER.get(), GuiBuilder::new);
    }

    @Override
    public void init() {
        window = new Window(this, getBE(), ResourceLocation.fromNamespaceAndPath(RFToolsBuilder.MODID, "gui/builder.gui"));
        super.init();

        initializeFields();
        setupEvents();
    }

    private void setupEvents() {
        window.event("cardgui", (source, params) -> openCardGui());
        window.event("anchor", (source, params) -> selectAnchor(source.getName()));
    }

    private void initializeFields() {
        energyBar = window.findChild("energybar");
        currentLevel = window.findChild("level");
        anchor[0] = window.findChild("anchor0");
        anchor[1] = window.findChild("anchor1");
        anchor[2] = window.findChild("anchor2");
        anchor[3] = window.findChild("anchor3");

        updateFields();
    }

    private void updateFields() {
        if (window == null) {
            return;
        }

        int cury = getCurrentLevelClientSide();
        currentLevel.text("Y: " + (cury == -1 ? "stop" : cury));

        BuilderTileEntity tileEntity = getBE();
        IItemHandler capability = this.minecraft.level.getCapability(Capabilities.ItemHandler.BLOCK, tileEntity.getBlockPos(), null);
        ItemStack card = capability == null ? ItemStack.EMPTY : capability.getStackInSlot(SLOT_TAB);
        if (card.isEmpty()) {
            window.setFlag("!validcard");
        } else if (card.getItem() instanceof ShapeCardItem) {
            window.setFlag("!validcard");
        } else {
            window.setFlag("validcard");
        }
        updateAnchorSettings(tileEntity.getAnchor());
        updateEnergyBar(energyBar);
    }

    private void openCardGui() {
        ItemStack cardStack = menu.getSlot(SLOT_TAB).getItem();
        if (!cardStack.isEmpty()) {
            BuilderTileEntity tileEntity = getBE();
            GuiShapeCard.fromTEPos = tileEntity.getBlockPos();
            GuiShapeCard.fromTEStackSlot = SLOT_TAB;
            RFToolsBuilderMessages.sendToServer(PacketCloseContainerAndOpenCardGui.create(tileEntity.getBlockPos()));
        }
    }

    private void selectAnchor(String name) {
        int index = name.charAt(name.length()-1)-48;
        updateAnchorSettings(AnchorMode.values()[index]);
        window.syncDataToServer(BuilderModule.BUILDER_DATA.get(), getBE());
    }

    private void updateAnchorSettings(AnchorMode index) {
        for (int i = 0 ; i < anchor.length ; i++) {
            if (isShapeCard()) {
                anchor[i].setCurrentChoice(0);
            } else {
                anchor[i].setCurrentChoice(i == index.ordinal() ? 1 : 0);
            }
        }
    }

    private boolean isShapeCard() {
        BuilderTileEntity tileEntity = getBE();
        IItemHandler capability = tileEntity.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, tileEntity.getBlockPos(), null);
        ItemStack card = capability == null ? ItemStack.EMPTY : capability.getStackInSlot(SLOT_TAB);
        return !card.isEmpty() && card.getItem() instanceof ShapeCardItem;
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        updateFields();
        drawWindow(graphics, partialTicks, mouseX, mouseY);
    }
}
