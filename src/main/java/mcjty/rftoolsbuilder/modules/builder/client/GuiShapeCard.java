package mcjty.rftoolsbuilder.modules.builder.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.*;
import mcjty.lib.base.StyleConfig;
import mcjty.lib.client.GuiTools;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.gui.BaseScreen;
import mcjty.lib.gui.IKeyReceiver;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.WindowManager;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.ComponentFactory;
import mcjty.rftoolsbuilder.modules.builder.BuilderConfiguration;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardType;
import mcjty.rftoolsbuilder.modules.builder.network.PacketOpenBuilderGui;
import mcjty.rftoolsbuilder.modules.builder.network.PacketUpdateNBTItemInventoryShape;
import mcjty.rftoolsbuilder.modules.builder.network.PacketUpdateNBTShapeCard;
import mcjty.rftoolsbuilder.modules.scanner.ScannerConfiguration;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import mcjty.rftoolsbuilder.shapes.IShapeParentGui;
import mcjty.rftoolsbuilder.shapes.Shape;
import mcjty.rftoolsbuilder.shapes.ShapeID;
import mcjty.rftoolsbuilder.shapes.ShapeRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

import static mcjty.lib.gui.layout.AbstractLayout.DEFAULT_SPACING;
import static mcjty.lib.gui.widgets.Widgets.*;

public class GuiShapeCard extends BaseScreen implements IShapeParentGui, IKeyReceiver {

    /**
     * The X size of the window in pixels.
     */
    protected int xSize = 360;
    /**
     * The Y size of the window in pixels.
     */
    protected int ySize = 160;

    private int guiLeft;
    private int guiTop;

    private boolean isQuarryCard;

    private ChoiceLabel shapeLabel;
    private ChoiceLabel solidLabel;
    private TextField dimX;
    private TextField dimY;
    private TextField dimZ;
    private TextField offsetX;
    private TextField offsetY;
    private TextField offsetZ;
    private Window window = null;
    private Label blocksLabel;

    private Panel voidPanel;
    private ToggleButton stone;
    private ToggleButton cobble;
    private ToggleButton dirt;
    private ToggleButton gravel;
    private ToggleButton sand;
    private ToggleButton netherrack;
    private ToggleButton endstone;
    private ToggleButton tagMatching;

    public final boolean fromTE;

    // For GuiComposer, GuiBuilder, etc.: the current card to edit
    public static BlockPos fromTEPos = null;
    public static int fromTEStackSlot = 0;

    private ShapeID shapeID = null;
    private ShapeRenderer shapeRenderer = null;


    public GuiShapeCard(boolean fromTE) {
        super(ComponentFactory.literal("Shapecard"));
        this.fromTE = fromTE;
    }

    @Override
    public void removed() {
        super.removed();
        if (fromTE) {
            RFToolsBuilderMessages.INSTANCE.sendToServer(new PacketOpenBuilderGui(fromTEPos));
        }
    }

    private ShapeRenderer getShapeRenderer() {
        if (shapeID == null) {
            shapeID = getShapeID();
        } else if (!shapeID.equals(getShapeID())) {
            shapeID = getShapeID();
            shapeRenderer = null;
        }
        if (shapeRenderer == null) {
            shapeRenderer = new ShapeRenderer(shapeID);
            shapeRenderer.initView(getPreviewLeft(), guiTop);
        }
        return shapeRenderer;
    }

    private ShapeID getShapeID() {
        ItemStack stackToEdit = getStackToEdit();
        return new ShapeID(Level.OVERWORLD, null, ShapeCardItem.getScanId(stackToEdit), false, ShapeCardItem.isSolid(stackToEdit));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private ItemStack getStackToEdit() {
        if (fromTE) {
            BlockEntity te = minecraft.level.getBlockEntity(fromTEPos);
            return te.getCapability(ForgeCapabilities.ITEM_HANDLER).map(h -> h.getStackInSlot(fromTEStackSlot)).orElse(ItemStack.EMPTY);
        } else {
            return minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);
        }
    }

    @Override
    public int getPreviewLeft() {
        return guiLeft + 104;
    }

    @Override
    public int getPreviewTop() {
        return guiTop - 5 + (isQuarryCard ? 0 : 10);
    }

    @Override
    public void init() {
        super.init();

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        ItemStack heldItem = getStackToEdit();
        if (heldItem.isEmpty()) {
            // Cannot happen!
            return;
        }

        ShapeCardType type = ShapeCardItem.getType(heldItem);
        isQuarryCard = type.isQuarry();
        if (isQuarryCard) {
            ySize = 160 + 28;
        }

        getShapeRenderer().initView(getPreviewLeft(), guiTop);

        shapeLabel = new ChoiceLabel().desiredWidth(100).desiredHeight(16).choices(
                Shape.SHAPE_BOX.getDescription(),
                Shape.SHAPE_TOPDOME.getDescription(),
                Shape.SHAPE_BOTTOMDOME.getDescription(),
                Shape.SHAPE_SPHERE.getDescription(),
                Shape.SHAPE_CYLINDER.getDescription(),
                Shape.SHAPE_CAPPEDCYLINDER.getDescription(),
                Shape.SHAPE_PRISM.getDescription(),
                Shape.SHAPE_TORUS.getDescription(),
                Shape.SHAPE_CONE.getDescription(),
                Shape.SHAPE_HEART.getDescription(),
                Shape.SHAPE_COMPOSITION.getDescription(),
                Shape.SHAPE_SCAN.getDescription()
        ).event((newChoice) -> updateSettings());

        solidLabel = new ChoiceLabel().desiredWidth(50).desiredHeight(16).choices(
                "Hollow",
                "Solid"
        ).event((newChoice) -> updateSettings());

        Panel shapePanel = horizontal().children(shapeLabel, solidLabel);

        Shape shape = ShapeCardItem.getShape(heldItem);
        shapeLabel.choice(shape.getDescription());
        boolean solid = ShapeCardItem.isSolid(heldItem);
        solidLabel.choice(solid ? "Solid" : "Hollow");

        blocksLabel = label("# ").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
        blocksLabel.desiredWidth(100).desiredHeight(16);

        Panel modePanel = vertical().desiredWidth(170).children(shapePanel, blocksLabel);

        BlockPos dim = ShapeCardItem.getDimension(heldItem);
        BlockPos offset = ShapeCardItem.getOffset(heldItem);

        dimX = new TextField().event((newText) -> {
            if (isTorus()) {
                dimZ.text(newText);
            }
            updateSettings();
        }).text(String.valueOf(dim.getX()));
        dimY = new TextField().event((newText) -> updateSettings()).text(String.valueOf(dim.getY()));
        dimZ = new TextField().event((newText) -> updateSettings()).text(String.valueOf(dim.getZ()));
        Panel dimPanel = horizontal(0, DEFAULT_SPACING).desiredHeight(18)
                .children(
                        label("Dim:").horizontalAlignment(HorizontalAlignment.ALIGN_RIGHT).desiredWidth(40),
                        dimX, dimY, dimZ);
        offsetX = new TextField().event((newText) -> updateSettings()).text(String.valueOf(offset.getX()));
        offsetY = new TextField().event((newText) -> updateSettings()).text(String.valueOf(offset.getY()));
        offsetZ = new TextField().event((newText) -> updateSettings()).text(String.valueOf(offset.getZ()));
        Panel offsetPanel = horizontal(0, DEFAULT_SPACING).desiredHeight(18).children(
                label("Offset:").horizontalAlignment(HorizontalAlignment.ALIGN_RIGHT).desiredWidth(40),
                offsetX, offsetY, offsetZ);

        Panel settingsPanel = new Panel().layout(new VerticalLayout().setSpacing(1).setVerticalMargin(1).setHorizontalMargin(0))
                .children(dimPanel, offsetPanel);

        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        Panel modeSettingsPanel = new Panel().layout(new VerticalLayout().setHorizontalMargin(0))
                .children(modePanel, settingsPanel);
        modeSettingsPanel.hint(0, 0, 180, 160);
        Panel toplevel;
        if (isQuarryCard) {
            setupVoidPanel(heldItem);
            toplevel = Widgets.positional().filledRectThickness(2).children(modeSettingsPanel, voidPanel);

        } else {
            toplevel = Widgets.positional().filledRectThickness(2).children(modeSettingsPanel);
        }

        toplevel.bounds(k, l, xSize, ySize);

        window = new Window(this, toplevel);
    }

    private void setupVoidPanel(ItemStack heldItem) {
        voidPanel = horizontal()
                .desiredHeight(26)
                .filledRectThickness(-2)
                .filledBackground(StyleConfig.colorListBackground);
        voidPanel.hint(5, 155, 350, 26);
        Label label = label("Void:");
        stone = new ToggleButton().desiredWidth(20).desiredHeight(20).tooltips("Void stone").event(this::updateVoidSettings);
        cobble = new ToggleButton().desiredWidth(20).desiredHeight(20).tooltips("Void cobble").event(this::updateVoidSettings);
        dirt = new ToggleButton().desiredWidth(20).desiredHeight(20).tooltips("Void dirt").event(this::updateVoidSettings);
        gravel = new ToggleButton().desiredWidth(20).desiredHeight(20).tooltips("Void gravel").event(this::updateVoidSettings);
        sand = new ToggleButton().desiredWidth(20).desiredHeight(20).tooltips("Void sand").event(this::updateVoidSettings);
        netherrack = new ToggleButton().desiredWidth(20).desiredHeight(20).tooltips("Void netherrack").event(this::updateVoidSettings);
        endstone = new ToggleButton().desiredWidth(20).desiredHeight(20).tooltips("Void end stone").event(this::updateVoidSettings);
        tagMatching = new ToggleButton().desiredWidth(60).desiredHeight(15).tooltips("Enable tag matching")
                .text("Tags")
                .checkMarker(true)
                .event(this::updateVoidSettings);

        stone.pressed(ShapeCardItem.isVoiding(heldItem, "stone"));
        cobble.pressed(ShapeCardItem.isVoiding(heldItem, "cobble"));
        dirt.pressed(ShapeCardItem.isVoiding(heldItem, "dirt"));
        gravel.pressed(ShapeCardItem.isVoiding(heldItem, "gravel"));
        sand.pressed(ShapeCardItem.isVoiding(heldItem, "sand"));
        netherrack.pressed(ShapeCardItem.isVoiding(heldItem, "netherrack"));
        endstone.pressed(ShapeCardItem.isVoiding(heldItem, "endstone"));
        tagMatching.pressed(ShapeCardItem.isTagMatching(heldItem));

        voidPanel.children(label, stone, cobble, dirt, gravel, sand, netherrack, endstone, tagMatching);
    }

    private boolean isTorus() {
        Shape shape = getCurrentShape();
        return Shape.SHAPE_TORUS.equals(shape);
    }

    private Shape getCurrentShape() {
        return Shape.getShape(shapeLabel.getCurrentChoice());
    }

    private boolean isSolid() {
        return "Solid".equals(solidLabel.getCurrentChoice());
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateSettings() {
        int dx = parseInt(dimX.getText());
        int dy = parseInt(dimY.getText());
        int dz = parseInt(dimZ.getText());
        int max = Math.max(ScannerConfiguration.maxScannerDimension.get(), BuilderConfiguration.maxBuilderDimension.get());
        if (dx < 0) {
            dx = 0;
        } else if (dx > max) {
            dx = max;
        }
        dimX.text(Integer.toString(dx));
        if (dz < 0) {
            dz = 0;
        } else if (dz > max) {
            dz = max;
        }
        dimZ.text(Integer.toString(dz));
        if (dy < 0) {
            dy = 0;
        } else if (dy > 256) {
            dy = 256;
        }
        dimY.text(Integer.toString(dy));

        if (isTorus()) {
            dimZ.text(dimX.getText());
        }
        if (fromTE) {
            ItemStack stack = getStackToEdit();
            if (!stack.isEmpty()) {
                CompoundTag tag = stack.getTag();
                if (tag == null) {
                    tag = new CompoundTag();
                }
                ShapeCardItem.setShape(stack, getCurrentShape(), isSolid());
                ShapeCardItem.setDimension(stack, dx, dy, dz);
                ShapeCardItem.setOffset(stack, parseInt(offsetX.getText()), parseInt(offsetY.getText()), parseInt(offsetZ.getText()));
                RFToolsBuilderMessages.INSTANCE.sendToServer(new PacketUpdateNBTItemInventoryShape(
                        fromTEPos, fromTEStackSlot, tag));
            }
        } else {
            RFToolsBuilderMessages.INSTANCE.sendToServer(new PacketUpdateNBTShapeCard(
                    TypedMap.builder()
                            .put(new Key<>("shape", Type.STRING), getCurrentShape().getDescription())
                            .put(new Key<>("solid", Type.BOOLEAN), isSolid())
                            .put(new Key<>("dimX", Type.INTEGER), dx)
                            .put(new Key<>("dimY", Type.INTEGER), dy)
                            .put(new Key<>("dimZ", Type.INTEGER), dz)
                            .put(new Key<>("offsetX", Type.INTEGER), parseInt(offsetX.getText()))
                            .put(new Key<>("offsetY", Type.INTEGER), parseInt(offsetY.getText()))
                            .put(new Key<>("offsetZ", Type.INTEGER), parseInt(offsetZ.getText()))
                            .build()));
        }
    }

    private void updateVoidSettings() {
        if (fromTE) {
            ItemStack stack = getStackToEdit();
            if (!stack.isEmpty()) {
                CompoundTag tag = stack.getTag();
                if (tag == null) {
                    tag = new CompoundTag();
                }
                tag.putBoolean("voidstone", stone.isPressed());
                tag.putBoolean("voidcobble", cobble.isPressed());
                tag.putBoolean("voiddirt", dirt.isPressed());
                tag.putBoolean("voidgravel", gravel.isPressed());
                tag.putBoolean("voidsand", sand.isPressed());
                tag.putBoolean("voidnetherrack", netherrack.isPressed());
                tag.putBoolean("voidendstone", endstone.isPressed());
                tag.putBoolean("tagMatching", tagMatching.isPressed());
                RFToolsBuilderMessages.INSTANCE.sendToServer(new PacketUpdateNBTItemInventoryShape(
                        fromTEPos, fromTEStackSlot, tag));
            }
        } else {
            RFToolsBuilderMessages.INSTANCE.sendToServer(new PacketUpdateNBTShapeCard(
                    TypedMap.builder()
                            .put(new Key<>("voidstone", Type.BOOLEAN), stone.isPressed())
                            .put(new Key<>("voidcobble", Type.BOOLEAN), cobble.isPressed())
                            .put(new Key<>("voiddirt", Type.BOOLEAN), dirt.isPressed())
                            .put(new Key<>("voidgravel", Type.BOOLEAN), gravel.isPressed())
                            .put(new Key<>("voidsand", Type.BOOLEAN), sand.isPressed())
                            .put(new Key<>("voidnetherrack", Type.BOOLEAN), netherrack.isPressed())
                            .put(new Key<>("voidendstone", Type.BOOLEAN), endstone.isPressed())
                            .put(new Key<>("tagMatching", Type.BOOLEAN), tagMatching.isPressed())
                            .build()));
        }
    }

    private final boolean[] buttons = new boolean[10];    // @todo ugly hack to get mouse buttons?

    @Override
    public void mouseMoved(double xx, double yy) {
        // If not initialized yet we do nothing
        if (window == null) {
            return;
        }
        window.mouseDragged(xx, yy, 0); // @todo 1.14 is this right? What button?

        int x = GuiTools.getRelativeX(this);
        int y = GuiTools.getRelativeY(this);
        x -= guiLeft;
        y -= guiTop;

        getShapeRenderer().handleShapeDragging(x, y, buttons);
    }


    @Override
    public boolean mouseClicked(double x, double y, int button) {
        // If not initialized yet we do nothing
        if (window == null) {
            return false;
        }
        if (button < buttons.length) {
            buttons[button] = true;
        }
        boolean rc = super.mouseClicked(x, y, button);
        window.mouseClicked(x, y, button);
        return rc;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // If not initialized yet we do nothing
        if (window == null) {
            return false;
        }
        if (button < buttons.length) {
            buttons[button] = false;
        }
        boolean rc = super.mouseReleased(mouseX, mouseY, button);
        window.mouseReleased(mouseX, mouseY, button);
        return rc;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // If not initialized yet we do nothing
        if (window == null) {
            return false;
        }
        boolean rc = false;
        if (!window.keyTyped(keyCode, scanCode)) {
            rc = super.keyPressed(keyCode, scanCode, modifiers);
        }
        return rc;
    }

    @Override
    public Window getWindow() {
        return window;
    }

    @Override
    public void keyTypedFromEvent(int keyCode, int scanCode) {
        if (window != null) {
            if (window.keyTyped(keyCode, scanCode)) {
                super.keyPressed(keyCode, scanCode, 0); // @todo 1.14: modifiers?
            }
        }
    }

    @Override
    public void charTypedFromEvent(char codePoint) {
        if (window != null) {
            if (window.charTyped(codePoint)) {
                super.charTyped(codePoint, 0); // @todo 1.14: modifiers?
            }
        }
    }

    @Override
    public boolean mouseClickedFromEvent(double x, double y, int button) {
        WindowManager manager = getWindow().getWindowManager();
        manager.mouseClicked(x, y, button);
        return true;
    }

    @Override
    public boolean mouseReleasedFromEvent(double x, double y, int button) {
        WindowManager manager = getWindow().getWindowManager();
        manager.mouseReleased(x, y, button);
        return true;
    }

    @Override
    public boolean mouseScrolledFromEvent(double x, double y, double amount) {
        WindowManager manager = getWindow().getWindowManager();
        manager.mouseScrolled(x, y, amount);
        return true;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double wheel) {
        // If not initialized yet we do nothing
        if (window == null) {
            return false;
        }
        getShapeRenderer().handleMouseWheel(wheel);
        return super.mouseScrolled(x, y, wheel);
    }

    private static int updateCounter = 20;

    @Override
    protected void renderInternal(PoseStack matrixStack, int pMouseX, int pMouseY, float pPartialTick) {
        // If not initialized yet we do nothing
        if (window == null) {
            return;
        }

        dimZ.enabled(!isTorus());

        updateCounter--;
        if (updateCounter <= 0) {
            updateCounter = 10;
            int count = getShapeRenderer().getCount();
            if (count >= ShapeCardItem.MAXIMUM_COUNT) {
                blocksLabel.text("#Blocks: ++" + count);
            } else {
                blocksLabel.text("#Blocks: " + count);
            }
        }

        window.draw(matrixStack);

        if (isQuarryCard) {
            // @@@ Hacky code!
            int x = (int) (window.getToplevel().getBounds().getX() + voidPanel.getBounds().getX()) + 1;
            int y = (int) (window.getToplevel().getBounds().getY() + voidPanel.getBounds().getY() + stone.getBounds().getY()) + 1;

            renderVoidBlock(matrixStack, x, y, stone, Blocks.STONE);
            renderVoidBlock(matrixStack, x, y, cobble, Blocks.COBBLESTONE);
            renderVoidBlock(matrixStack, x, y, dirt, Blocks.DIRT);
            renderVoidBlock(matrixStack, x, y, gravel, Blocks.GRAVEL);
            renderVoidBlock(matrixStack, x, y, sand, Blocks.SAND);
            renderVoidBlock(matrixStack, x, y, netherrack, Blocks.NETHERRACK);
            renderVoidBlock(matrixStack, x, y, endstone, Blocks.END_STONE);
        }

        ItemStack stack = getStackToEdit();
        if (!stack.isEmpty()) {
            // @todo 1.18 DISABLED FOR NOW
//            getShapeRenderer().renderShape(matrixStack, this, stack, guiLeft, guiTop, true, true, true, false);
        }

        List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int guiLeft = (this.width - this.xSize) / 2;
            int guiTop = (this.height - this.ySize) / 2;
            int x = GuiTools.getRelativeX(this);
            int y = GuiTools.getRelativeY(this);
            // @todo check on 1.16
            List<FormattedText> properties = tooltips.stream().map(ComponentFactory::literal).collect(Collectors.toList());
            List<FormattedCharSequence> processors = Language.getInstance().getVisualOrder(properties);
            renderTooltip(matrixStack, processors, x - guiLeft, y - guiTop);
        }
    }

    private void renderVoidBlock(PoseStack matrixStack, int x, int y, ToggleButton button, Block block) {
        x += (int) button.getBounds().getX();
        RenderHelper.renderObject(matrixStack, x, y, new ItemStack(block), button.isPressed());
        if (button.isPressed()) {
            drawLine(x - 1, y - 1, x + 18, y + 18, 0xffff0000);
            drawLine(x + 18, y - 1, x - 1, y + 18, 0xffff0000);
        }
    }

    private static void drawLine(int x1, int y1, int x2, int y2, int color) {
        float f3 = (color >> 24 & 255) / 255.0F;
        float f = (color >> 16 & 255) / 255.0F;
        float f1 = (color >> 8 & 255) / 255.0F;
        float f2 = (color & 255) / 255.0F;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION);
        GlStateManager._enableBlend();
        GlStateManager._disableTexture();
        GlStateManager._disableDepthTest();
        GL11.glLineWidth(2.0f);
        GlStateManager._blendFuncSeparate(770, 771, 1, 0);
//        GlStateManager._color4f(f, f1, f2, f3);// @todo 1.18
        buffer.vertex(x1, y1, 0.0D).endVertex();
        buffer.vertex(x2, y2, 0.0D).endVertex();
        tessellator.end();
        GlStateManager._enableTexture();
        GlStateManager._enableDepthTest();
        GlStateManager._disableBlend();
    }

    public static void open(boolean fromTE) {
        Minecraft.getInstance().setScreen(new GuiShapeCard(fromTE));
    }

}
