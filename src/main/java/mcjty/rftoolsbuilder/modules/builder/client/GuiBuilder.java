package mcjty.rftoolsbuilder.modules.builder.client;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.CapabilityItemHandler;

import static mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity.*;

public class GuiBuilder extends GenericGuiContainer<BuilderTileEntity, GenericContainer> {

    private EnergyBar energyBar;
    private Button currentLevel;
    private ImageChoiceLabel anchor[] = new ImageChoiceLabel[4];

    public GuiBuilder(BuilderTileEntity builderTileEntity, GenericContainer container, PlayerInventory inventory) {
        super(builderTileEntity, container, inventory, ManualHelper.create("rftoolsbuilder:builder/builder_intro"));
    }

    @Override
    public void init() {
        window = new Window(this, tileEntity, RFToolsBuilderMessages.INSTANCE, new ResourceLocation(RFToolsBuilder.MODID, "gui/builder.gui"));
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

        ((ChoiceLabel) window.findChild("mode")).choice(MODES[tileEntity.getMode()]);
        ChoiceLabel rotateButton = window.findChild("rotate");
        rotateButton.choice(String.valueOf(tileEntity.getRotate() * 90));
        if (!isShapeCard()) {
            anchor[tileEntity.getAnchor()].setCurrentChoice(1);
        }
    }

    private void openCardGui() {
        ItemStack cardStack = container.getSlot(SLOT_TAB).getStack();
        if (!cardStack.isEmpty()) {
            GuiShapeCard.fromTEPos = tileEntity.getPos();
            GuiShapeCard.fromTEStackSlot = SLOT_TAB;
            GuiShapeCard.returnGui = this;
            GuiShapeCard.open();
        }
    }

    private void selectAnchor(String name) {
        int index = name.charAt(name.length()-1)-48;
        updateAnchorSettings(index);
        sendServerCommandTyped(RFToolsBuilderMessages.INSTANCE, CMD_SETANCHOR, TypedMap.builder().put(PARAM_ANCHOR_INDEX, index).build());
    }

    private void updateAnchorSettings(int index) {
        for (int i = 0 ; i < anchor.length ; i++) {
            if (isShapeCard()) {
                anchor[i].setCurrentChoice(0);
            } else {
                anchor[i].setCurrentChoice(i == index ? 1 : 0);
            }
        }
    }

    private boolean isShapeCard() {
        ItemStack card = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(h -> h.getStackInSlot(SLOT_TAB)).orElse(ItemStack.EMPTY);
        return !card.isEmpty() && card.getItem() instanceof ShapeCardItem;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        int cury = getCurrentLevelClientSide();
        currentLevel.text("Y: " + (cury == -1 ? "stop" : cury));

        ItemStack card = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(h -> h.getStackInSlot(SLOT_TAB)).orElse(ItemStack.EMPTY);
        if (card.isEmpty()) {
            window.setFlag("!validcard");
        } else if (card.getItem() instanceof ShapeCardItem) {
            window.setFlag("!validcard");
        } else {
            window.setFlag("validcard");
        }
        updateAnchorSettings(tileEntity.getAnchor());

        drawWindow();
        updateEnergyBar(energyBar);
    }
}
