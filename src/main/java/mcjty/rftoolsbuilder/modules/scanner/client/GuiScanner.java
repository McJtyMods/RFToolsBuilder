package mcjty.rftoolsbuilder.modules.scanner.client;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.BlockPosTools;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ScannerTileEntity;
import mcjty.rftoolsbuilder.shapes.IShapeParentGui;
import mcjty.rftoolsbuilder.shapes.ShapeGuiTools;
import mcjty.rftoolsbuilder.shapes.ShapeID;
import mcjty.rftoolsbuilder.shapes.ShapeRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class GuiScanner extends GenericGuiContainer<ScannerTileEntity, GenericContainer> implements IShapeParentGui {

    private static final int SCANNER_WIDTH = 256;
    private static final int SCANNER_HEIGHT = 238;
    private static final ResourceLocation BACKGROUND = new ResourceLocation(RFToolsBuilder.MODID, "textures/gui/scanner.png");

    private ShapeRenderer shapeRenderer;
    private ToggleButton showAxis;
    private ToggleButton showOuter;
    private ToggleButton showScan;
    private Label offsetLabel;
    private Label dimensionLabel;
    private Label progressLabel;

    public GuiScanner(ScannerTileEntity tileEntity, GenericContainer container, Inventory inventory) {
        super(tileEntity, container, inventory, ScannerModule.SCANNER.get().getManualEntry());
        imageWidth = SCANNER_WIDTH;
        imageHeight = SCANNER_HEIGHT;
    }

    public static void register() {
        register(ScannerModule.CONTAINER_SCANNER.get(), GuiScanner::new);
    }

    @Override
    public void init() {
        super.init();

        Panel root = new Panel().layout(new PositionalLayout()).background(BACKGROUND);
        root.bounds(leftPos, topPos, imageWidth, imageHeight);

        showAxis = ShapeGuiTools.createAxisButton(this, root, 5, 176);
        showOuter = ShapeGuiTools.createBoxButton(this, root, 31, 176);
        showScan = ShapeGuiTools.createScanButton(this, root, 57, 176);

        Button scanButton = new Button().text("Scan").hint(5, 156, 40, 16).event(() -> sendServerCommandTyped(ScannerTileEntity.CMD_SCAN_ID, TypedMap.EMPTY));

        root.children(
                scanButton,
                new Button().text("W").hint(4, 30, 16, 15).event(() -> move(-16, 0, 0)),
                new Button().text("w").hint(20, 30, 16, 15).event(() -> move(-1, 0, 0)),
                new Button().text("e").hint(45, 30, 16, 15).event(() -> move(1, 0, 0)),
                new Button().text("E").hint(61, 30, 16, 15).event(() -> move(16, 0, 0)),
                new Button().text("S").hint(4, 50, 16, 15).event(() -> move(0, 0, -16)),
                new Button().text("s").hint(20, 50, 16, 15).event(() -> move(0, 0, -1)),
                new Button().text("n").hint(45, 50, 16, 15).event(() -> move(0, 0, 1)),
                new Button().text("N").hint(61, 50, 16, 15).event(() -> move(0, 0, 16)),
                new Button().text("D").hint(4, 70, 16, 15).event(() -> move(0, -16, 0)),
                new Button().text("d").hint(20, 70, 16, 15).event(() -> move(0, -1, 0)),
                new Button().text("u").hint(45, 70, 16, 15).event(() -> move(0, 1, 0)),
                new Button().text("U").hint(61, 70, 16, 15).event(() -> move(0, 16, 0))
        );

        offsetLabel = Widgets.label("").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).hint(4, 90, 80, 14);
        dimensionLabel = Widgets.label("").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).hint(4, 105, 80, 14);
        progressLabel = Widgets.label("").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).hint(4, 135, 80, 14);
        root.children(offsetLabel, dimensionLabel, progressLabel);

        window = new Window(this, root);
    }

    private void move(int x, int y, int z) {
        sendServerCommandTyped(ScannerTileEntity.CMD_OFFSET_ID,
                TypedMap.builder().put(ScannerTileEntity.PARAM_OFFSET, tileEntity.getDataOffset().offset(x, y, z)).build());
    }

    private ShapeRenderer getShapeRenderer() {
        ShapeID id = new ShapeID(net.minecraft.world.level.Level.OVERWORLD, null, tileEntity.getScanId(), false, true);
        if (shapeRenderer == null || !id.equals(shapeRenderer.getShapeID())) {
            shapeRenderer = new ShapeRenderer(id);
            shapeRenderer.initView(getPreviewLeft(), topPos + 100);
        }
        return shapeRenderer;
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
        return leftPos;
    }

    @Override
    public int getPreviewTop() {
        return topPos;
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        offsetLabel.text("Off: " + BlockPosTools.toString(tileEntity.getDataOffset()));
        dimensionLabel.text("Dim: " + BlockPosTools.toString(tileEntity.getDataDim()));
        int progress = tileEntity.getScanProgress();
        progressLabel.text(progress >= 0 ? progress + "%" : "");

        drawWindow(graphics);

        ItemStack stack = tileEntity.getRenderStack();
        if (!stack.isEmpty()) {
            getShapeRenderer().renderShape(graphics, this, stack, getPreviewLeft(), getPreviewTop(), showAxis.isPressed(), showOuter.isPressed(), showScan.isPressed(), false);
        }
    }
}
