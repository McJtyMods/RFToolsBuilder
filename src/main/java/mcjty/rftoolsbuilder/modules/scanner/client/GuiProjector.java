package mcjty.rftoolsbuilder.modules.scanner.client;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.scanner.ProjectorOpcode;
import mcjty.rftoolsbuilder.modules.scanner.ProjectorOperation;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ProjectorTileEntity;
import mcjty.rftoolsbuilder.shapes.IShapeParentGui;
import mcjty.rftoolsbuilder.shapes.ShapeGuiTools;
import mcjty.rftoolsbuilder.shapes.ShapeRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

import static mcjty.lib.gui.widgets.Widgets.label;
import static mcjty.rftoolsbuilder.modules.scanner.blocks.ProjectorTileEntity.*;

public class GuiProjector extends GenericGuiContainer<ProjectorTileEntity, GenericContainer> implements IShapeParentGui {

    private static final int SIDEWIDTH = 80;
    private static final int PROJECTOR_WIDTH = 256;
    private static final int PROJECTOR_HEIGHT = 238;
    private static final int PREVIEW_BUTTON_WIDTH = 39;

    private static final ResourceLocation SIDE_BACKGROUND = new ResourceLocation(RFToolsBuilder.MODID, "textures/gui/sidegui_projector.png");
    private static final ResourceLocation MAIN_BACKGROUND = new ResourceLocation(RFToolsBuilder.MODID, "textures/gui/projector.png");

    private final ChoiceLabel[] rsLabelOn = new ChoiceLabel[4];
    private final ChoiceLabel[] rsLabelOff = new ChoiceLabel[4];
    private final TextField[] valOn = new TextField[4];
    private final TextField[] valOff = new TextField[4];

    private ShapeRenderer shapeRenderer;
    private ToggleButton showAxis;
    private ToggleButton showOuter;
    private ToggleButton showScan;
    private TextField angle;
    private TextField offset;
    private TextField scale;
    private ToggleButton autoRotate;
    private ToggleButton scanline;
    private ToggleButton sound;
    private ToggleButton grayScale;
    private ToggleButton renderModels;

    public GuiProjector(ProjectorTileEntity tileEntity, GenericContainer container, Inventory inventory) {
        super(tileEntity, container, inventory, ScannerModule.PROJECTOR.get().getManualEntry());
        imageWidth = SIDEWIDTH + PROJECTOR_WIDTH;
        imageHeight = PROJECTOR_HEIGHT;
    }

    public static void register() {
        register(ScannerModule.CONTAINER_PROJECTOR.get(), GuiProjector::new);
    }

    private ShapeRenderer getShapeRenderer() {
        if (shapeRenderer == null || !tileEntity.getShapeID().equals(shapeRenderer.getShapeID())) {
            shapeRenderer = new ShapeRenderer(tileEntity.getShapeID());
            shapeRenderer.initView(getPreviewLeft(), topPos + 100);
        } else {
            shapeRenderer.setShapeID(tileEntity.getShapeID());
        }
        shapeRenderer.setRefreshCounter(tileEntity.getCounter());
        return shapeRenderer;
    }

    @Override
    public void init() {
        super.init();

        Panel toplevel = new Panel().layout(new PositionalLayout());

        Panel sidePanel = new Panel().layout(new PositionalLayout()).background(SIDE_BACKGROUND);
        sidePanel.bounds(0, 0, SIDEWIDTH, imageHeight);
        initSidePanel(sidePanel);

        Panel mainPanel = new Panel().layout(new PositionalLayout()).background(MAIN_BACKGROUND);
        mainPanel.bounds(SIDEWIDTH, 0, PROJECTOR_WIDTH, imageHeight);
        initMainPanel(mainPanel);

        toplevel.children(sidePanel, mainPanel);
        toplevel.bounds(leftPos, topPos, imageWidth, imageHeight);
        window = new Window(this, toplevel);
    }

    private void initMainPanel(Panel panel) {
        angle = createField(8, 30, 22, String.valueOf(tileEntity.getAngleInt()), this::updateSettings);
        offset = createField(8, 62, 22, String.valueOf(tileEntity.getOffsetInt()), this::updateSettings);
        scale = createField(8, 94, 22, String.valueOf(tileEntity.getScaleInt()), this::updateSettings);

        autoRotate = new ToggleButton().checkMarker(true).text("Auto").tooltips("Automatic client-side rotation").hint(2, 128, 39, 16).event(this::updateSettings);
        autoRotate.pressed(tileEntity.isAutoRotate());
        scanline = new ToggleButton().checkMarker(true).text("SL").tooltips("Enable visual scanlines when the scan refreshes").hint(42, 128, 39, 16).event(this::updateSettings);
        scanline.pressed(tileEntity.isScanline());
        sound = new ToggleButton().checkMarker(true).text("Snd").tooltips("Enable sound during visual scan").hint(2, 146, 39, 16).event(this::updateSettings);
        sound.pressed(tileEntity.isSound());
        grayScale = new ToggleButton().checkMarker(true).text("Gray").tooltips("Enable grayscale mode").hint(42, 146, 39, 16).event(this::updateSettings);
        grayScale.pressed(tileEntity.isGrayscale());
        renderModels = new ToggleButton().checkMarker(true).text("M").tooltips("Render baked block models in the world", "instead of solid projected cubes").hint(42, 182, PREVIEW_BUTTON_WIDTH, 16).event(this::updateSettings);
        renderModels.pressed(tileEntity.isRenderBlockModels());

        panel.children(
                label("Angle").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).hint(34, 30, 32, 14),
                label("Offset").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).hint(34, 62, 32, 14),
                label("Scale").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).hint(34, 94, 32, 14),
                angle, offset, scale,
                autoRotate, scanline, sound, grayScale, renderModels
        );

        showAxis = ShapeGuiTools.createAxisButton(this, panel, 2, 164, PREVIEW_BUTTON_WIDTH);
        showOuter = ShapeGuiTools.createBoxButton(this, panel, 42, 164, PREVIEW_BUTTON_WIDTH);
        showScan = ShapeGuiTools.createScanButton(this, panel, 2, 182, PREVIEW_BUTTON_WIDTH);
    }

    private void initSidePanel(Panel panel) {
        initRsPanel(panel, 0, "S");
        initRsPanel(panel, 1, "N");
        initRsPanel(panel, 2, "E");
        initRsPanel(panel, 3, "W");
    }

    private void initRsPanel(Panel root, int index, String labelText) {
        int dy = index * 53;
        root.children(label(labelText).horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).hint(8, dy + 8, 12, 13));

        rsLabelOn[index] = new ChoiceLabel().hint(8, dy + 26, 32, 14).choices(ProjectorOpcode.getChoices()).event(choice -> updateRs());
        rsLabelOff[index] = new ChoiceLabel().hint(42, dy + 26, 32, 14).choices(ProjectorOpcode.getChoices()).event(choice -> updateRs());
        for (ProjectorOpcode opcode : ProjectorOpcode.values()) {
            rsLabelOn[index].choiceTooltip(opcode.getCode(), opcode.getDescription());
            rsLabelOff[index].choiceTooltip(opcode.getCode(), opcode.getDescription());
        }

        valOn[index] = createField(8, dy + 41, 32, "", this::updateRs);
        valOff[index] = createField(42, dy + 41, 32, "", this::updateRs);

        ProjectorOperation op = tileEntity.getOperations()[index];
        rsLabelOn[index].choice(op.getOpcodeOn().getCode());
        rsLabelOff[index].choice(op.getOpcodeOff().getCode());
        valOn[index].text(op.getValueOn() == null ? "" : op.getValueOn().toString());
        valOff[index].text(op.getValueOff() == null ? "" : op.getValueOff().toString());

        root.children(rsLabelOn[index], rsLabelOff[index], valOn[index], valOff[index]);
    }

    private TextField createField(int x, int y, int w, String value, Runnable runnable) {
        return new TextField().hint(x, y, w, 14).text(value).event(newText -> runnable.run());
    }

    private int parse(TextField field, int defaultValue) {
        try {
            return Integer.parseInt(field.getText());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Double parseDouble(TextField field) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void updateSettings() {
        sendServerCommandTyped(ProjectorTileEntity.CMD_SETTINGS_ID,
                TypedMap.builder()
                        .put(PARAM_SCALE, parse(scale, tileEntity.getScaleInt()))
                        .put(PARAM_OFFSET, parse(offset, tileEntity.getOffsetInt()))
                        .put(PARAM_ANGLE, parse(angle, tileEntity.getAngleInt()))
                        .put(PARAM_AUTO, autoRotate.isPressed())
                        .put(PARAM_SCAN, scanline.isPressed())
                        .put(PARAM_SOUND, sound.isPressed())
                        .put(PARAM_GRAY, grayScale.isPressed())
                        .put(PARAM_RENDERMODELS, renderModels.isPressed())
                        .build());
    }

    private void updateRs() {
        TypedMap.Builder builder = TypedMap.builder();
        for (int i = 0; i < 4; i++) {
            builder.put(PARAM_OPON.get(i), rsLabelOn[i].getCurrentChoice());
            builder.put(PARAM_OPOFF.get(i), rsLabelOff[i].getCurrentChoice());
            Double on = parseDouble(valOn[i]);
            Double off = parseDouble(valOff[i]);
            if (on != null) {
                builder.put(PARAM_VALON.get(i), on);
            }
            if (off != null) {
                builder.put(PARAM_VALOFF.get(i), off);
            }
        }
        sendServerCommandTyped(ProjectorTileEntity.CMD_RSSETTINGS_ID, builder.build());
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
        for (int i = 0; i < 4; i++) {
            ProjectorOperation op = tileEntity.getOperations()[i];
            valOn[i].enabled(op.getOpcodeOn().isNeedsValue());
            valOff[i].enabled(op.getOpcodeOff().isNeedsValue());
        }
        sound.enabled(scanline.isPressed());

        drawWindow(graphics);

        ItemStack stack = tileEntity.getRenderStack();
        if (!stack.isEmpty()) {
            getShapeRenderer().renderShape(graphics, this, stack, getPreviewLeft(), getPreviewTop(), showAxis.isPressed(), showOuter.isPressed(), showScan.isPressed(), false);
        }
    }
}
