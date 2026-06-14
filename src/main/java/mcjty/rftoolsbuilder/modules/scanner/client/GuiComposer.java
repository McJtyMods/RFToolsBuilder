package mcjty.rftoolsbuilder.modules.scanner.client;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.client.GuiShapeCard;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.builder.network.PacketCloseContainerAndOpenCardGui;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ComposerTileEntity;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import mcjty.rftoolsbuilder.shapes.IShapeParentGui;
import mcjty.rftoolsbuilder.shapes.ShapeGuiTools;
import mcjty.rftoolsbuilder.shapes.ShapeID;
import mcjty.rftoolsbuilder.shapes.ShapeModifier;
import mcjty.rftoolsbuilder.shapes.ShapeOperation;
import mcjty.rftoolsbuilder.shapes.ShapeRenderer;
import mcjty.rftoolsbuilder.shapes.ShapeRotation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

import static mcjty.lib.gui.widgets.Widgets.label;

public class GuiComposer extends GenericGuiContainer<ComposerTileEntity, GenericContainer> implements IShapeParentGui {

    private static final int SIDEWIDTH = 80;
    private static final int SHAPER_WIDTH = 256;
    private static final int SHAPER_HEIGHT = 238;

    private static final ResourceLocation SIDE_BACKGROUND = new ResourceLocation(RFToolsBuilder.MODID, "textures/gui/sidegui_projector.png");
    private static final ResourceLocation MAIN_BACKGROUND = new ResourceLocation(RFToolsBuilder.MODID, "textures/gui/composer.png");

    private final ChoiceLabel[] operationLabels = new ChoiceLabel[ComposerTileEntity.SLOT_COUNT];
    private final ChoiceLabel[] rotationLabels = new ChoiceLabel[ComposerTileEntity.SLOT_COUNT];
    private final ToggleButton[] flipButtons = new ToggleButton[ComposerTileEntity.SLOT_COUNT];

    private ShapeRenderer shapeRenderer;
    private ToggleButton showAxis;
    private ToggleButton showOuter;
    private ToggleButton showScan;

    public GuiComposer(ComposerTileEntity tileEntity, GenericContainer container, Inventory inventory) {
        super(tileEntity, container, inventory, ScannerModule.COMPOSER.get().getManualEntry());
        imageWidth = SIDEWIDTH + SHAPER_WIDTH;
        imageHeight = SHAPER_HEIGHT;
    }

    public static void register() {
        register(ScannerModule.CONTAINER_COMPOSER.get(), GuiComposer::new);
    }

    @Override
    public void init() {
        super.init();

        Panel toplevel = new Panel().layout(new PositionalLayout());

        Panel sidePanel = new Panel().layout(new PositionalLayout()).background(SIDE_BACKGROUND);
        sidePanel.bounds(0, 0, SIDEWIDTH, imageHeight);
        initSidePanel(sidePanel);

        Panel mainPanel = new Panel().layout(new PositionalLayout()).background(MAIN_BACKGROUND);
        mainPanel.bounds(SIDEWIDTH, 0, SHAPER_WIDTH, imageHeight);
        initMainPanel(mainPanel);

        toplevel.children(sidePanel, mainPanel);
        toplevel.bounds(leftPos, topPos, imageWidth, imageHeight);
        window = new Window(this, toplevel);
    }

    private void initMainPanel(Panel panel) {
        ShapeModifier[] modifiers = tileEntity.getModifiers();
        for (int i = 0; i < ComposerTileEntity.SLOT_COUNT; i++) {
            int y = 7 + i * 18;
            int index = i;
            Button config = new Button().text("?").tooltips("Open the shape card editor").hint(3, y + 2, 13, 12).event(() -> openCardGui(index));
            ChoiceLabel op = new ChoiceLabel().hint(55, y, 26, 14)
                    .choices(ShapeOperation.UNION.getCode(), ShapeOperation.SUBTRACT.getCode(), ShapeOperation.INTERSECT.getCode())
                    .event(choice -> updateSettings());
            for (ShapeOperation operation : ShapeOperation.values()) {
                op.choiceTooltip(operation.getCode(), operation.getDescription());
            }
            op.choice(modifiers[i].getOperation().getCode());
            operationLabels[i] = op;
            panel.children(config, op);
        }
        operationLabels[0].enabled(false);

        Button outConfig = new Button().text("?").tooltips("Open the shape card editor").hint(3, 202, 13, 12).event(() -> openCardGui(-1));
        showAxis = ShapeGuiTools.createAxisButton(this, panel, 5, 176);
        showOuter = ShapeGuiTools.createBoxButton(this, panel, 31, 176);
        showScan = ShapeGuiTools.createScanButton(this, panel, 57, 176);

        panel.children(outConfig, showAxis, showOuter, showScan);
    }

    private void initSidePanel(Panel panel) {
        String[] help = {
                "Drag left mouse button to rotate",
                "Shift drag left mouse to pan",
                "Use mouse wheel to zoom in/out",
                "Use middle click to reset rotation"
        };
        panel.children(
                label("E").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).color(0xffff0000).tooltips(help).hint(10, 170, 15, 15),
                label("W").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).color(0xffff0000).tooltips(help).hint(50, 170, 15, 15),
                label("U").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).color(0xff00bb00).tooltips(help).hint(10, 185, 15, 15),
                label("D").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).color(0xff00bb00).tooltips(help).hint(50, 185, 15, 15),
                label("N").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).color(0xff0000ff).tooltips(help).hint(10, 200, 15, 15),
                label("S").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).color(0xff0000ff).tooltips(help).hint(50, 200, 15, 15)
        );

        ShapeModifier[] modifiers = tileEntity.getModifiers();
        for (int i = 0; i < ComposerTileEntity.SLOT_COUNT; i++) {
            int y = 9 + i * 17 + (i / 3) * 2;
            ToggleButton flip = new ToggleButton().checkMarker(true).text("Flip").hint(9, y, 35, 14).event(this::updateSettings);
            flip.pressed(modifiers[i].isFlipY());
            flipButtons[i] = flip;

            ChoiceLabel rotation = new ChoiceLabel().hint(46, y, 28, 14)
                    .choices(ShapeRotation.NONE.getCode(), ShapeRotation.X.getCode(), ShapeRotation.Y.getCode(), ShapeRotation.Z.getCode())
                    .event(choice -> updateSettings());
            rotation.choice(modifiers[i].getRotation().getCode());
            rotationLabels[i] = rotation;

            panel.children(flip, rotation);
        }
    }

    private ShapeRenderer getShapeRenderer() {
        ItemStack stack = menu.getSlot(ComposerTileEntity.SLOT_OUT).getItem();
        ShapeID shapeID = new ShapeID(Level.OVERWORLD, null, ShapeCardItem.getScanIdRecursive(stack), false, ShapeCardItem.isSolid(stack));
        if (shapeRenderer == null || !shapeID.equals(shapeRenderer.getShapeID())) {
            shapeRenderer = new ShapeRenderer(shapeID);
            shapeRenderer.initView(getPreviewLeft(), topPos + 100);
        } else {
            shapeRenderer.setShapeID(shapeID);
        }
        return shapeRenderer;
    }

    private void openCardGui(int index) {
        int slot = index == -1 ? ComposerTileEntity.SLOT_OUT : ComposerTileEntity.SLOT_TABS + index;
        ItemStack cardStack = menu.getSlot(slot).getItem();
        if (!cardStack.isEmpty()) {
            GuiShapeCard.fromTEPos = tileEntity.getBlockPos();
            GuiShapeCard.fromTEStackSlot = slot;
            RFToolsBuilderMessages.sendToServer(PacketCloseContainerAndOpenCardGui.create(tileEntity.getBlockPos()));
        }
    }

    private void updateSettings() {
        TypedMap.Builder builder = TypedMap.builder();
        for (int i = 0; i < ComposerTileEntity.SLOT_COUNT; i++) {
            builder.put(ComposerTileEntity.PARAM_OPS.get(i), operationLabels[i].getCurrentChoice());
            builder.put(ComposerTileEntity.PARAM_FLIPS.get(i), flipButtons[i].isPressed());
            builder.put(ComposerTileEntity.PARAM_ROTS.get(i), rotationLabels[i].getCurrentChoice());
        }
        sendServerCommandTyped(ComposerTileEntity.CMD_SETTINGS_ID, builder.build());
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        boolean result = super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        boolean[] buttons = new boolean[3];
        if (button >= 0 && button < buttons.length) {
            buttons[button] = true;
        }
        getShapeRenderer().handleShapeDragging((int) mouseX - getPreviewLeft(), (int) mouseY - topPos, buttons);
        return result;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        boolean result = super.mouseScrolled(mouseX, mouseY, amount);
        getShapeRenderer().handleMouseWheel(amount);
        return result;
    }

    @Override
    public int getPreviewLeft() {
        return leftPos + SIDEWIDTH;
    }

    @Override
    public int getPreviewTop() {
        return topPos;
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        drawWindow(graphics);

        ItemStack stack = menu.getSlot(ComposerTileEntity.SLOT_OUT).getItem();
        if (!stack.isEmpty()) {
            getShapeRenderer().renderShape(graphics, this, stack, getPreviewLeft(), getPreviewTop(), showAxis.isPressed(), showOuter.isPressed(), showScan.isPressed(), true);
        }
    }
}
