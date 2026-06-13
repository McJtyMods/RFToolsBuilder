package mcjty.rftoolsbuilder.shapes;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.varia.Check32;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.scanner.ScannerConfiguration;
import mcjty.rftoolsbuilder.modules.scanner.network.PacketRequestShapeData;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapeRenderer {

    private int prevX = -1;
    private int prevY = -1;

    private float scale = 3.0f;
    private float dx = 230.0f;
    private float dy = 100.0f;
    private float xangle = 25.0f;
    private float yangle = 25.0f;
    private float zangle = 0.0f;

    private ShapeID shapeID;

    private int waitForNewRequest = 0;


    public ShapeRenderer(ShapeID shapeID) {
        this.shapeID = shapeID;
    }

    public void setShapeID(ShapeID shapeID) {
        this.shapeID = shapeID;
    }

    public ShapeID getShapeID() {
        return shapeID;
    }

    public int getCount() {
        RenderData data = ShapeDataManagerClient.getRenderData(shapeID);
        if (data != null) {
            return data.getBlockCount();
        }
        return 0;
    }

    public static RenderData getRenderDataAndCreate(ShapeID shapeID) {
        RenderData data = ShapeDataManagerClient.getRenderDataAndCreate(shapeID);
        data.touch();
        return data;
    }

    public static void setRenderData(ShapeID id, int checksum, @Nullable RenderData.RenderPlane plane, int offsetY, int dy, String msg) {
        RenderData data = getRenderDataAndCreate(id);
        if (data.getChecksum() != checksum) {
            return;
        }
        data.setPlaneData(plane, offsetY, dy);
        data.previewMessage = msg;
        data.markRequestProgress();
        if (offsetY >= dy - 1) {
            data.clearRequest();
        }
    }

    public void initView(int dx, int dy) {
        Minecraft mc = Minecraft.getInstance();

        Window mainWindow = mc.getWindow();
        int xScale = mainWindow.getGuiScaledWidth();
        int yScale = mainWindow.getGuiScaledHeight();
        int sx = (dx + 84) * mainWindow.getScreenWidth() / xScale;
        int sy = (mainWindow.getScreenHeight()) - (dy + 136) * mainWindow.getScreenHeight() / yScale;
        int sw = 161 * mainWindow.getScreenWidth() / xScale;
        int sh = 130 * mainWindow.getScreenHeight() / yScale;
        int vx = sx + sw/2;
        int vy = sy + sh/2;

        this.dx = (float) (vx/mainWindow.getGuiScale());
        this.dy = (float) (vy/mainWindow.getGuiScale());
    }

    public void handleShapeDragging(int x, int y, boolean[] buttons) {
        MouseHandler mouse = Minecraft.getInstance().mouseHandler;
        if (x >= 100 && y <= 120) {
            if (SafeClientTools.isSneaking()) {
                if (prevX != -1 && buttons[0]) {
                    dx += (x - prevX);
                    dy += (y - prevY);
                }
            } else {
                if (prevX != -1 && buttons[0]) {
                    yangle -= (x - prevX);
                    xangle += (y - prevY);
                }
            }
            prevX = x;
            prevY = y;
        }

        if (buttons[2]) {
            xangle = 0.0f;
            yangle = 0.0f;
        }
    }

    public void handleMouseWheel(double dwheelX, double dwheelY) {
        float dwheel = (float) dwheelY;
        if (dwheel < 0) {
            scale *= .6;
            if (scale <= 0.1) {
                scale = .1f;
            }
        } else if (dwheel > 0) {
            scale *= 1.4;
        }
    }

    public boolean renderShapeInWorld(PoseStack poseStack, ItemStack stack, float offset, float scale, float angle,
                                      boolean scan, ShapeID shape, boolean renderBlockModels) {
        poseStack.pushPose();
        poseStack.translate(.5f, 1.0f + offset, .5f);
        poseStack.scale(scale, scale, scale);
        RenderHelper.rotateYP(poseStack, angle);

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        boolean doSound = renderFacesInWorld(poseStack, Tesselator.getInstance(), stack, scan, shape.isGrayscale(), shape.getScanId());

        RenderSystem.disableBlend();
        poseStack.popPose();
        return doSound;
    }

    public void renderShape(GuiGraphics graphics, IShapeParentGui gui, ItemStack stack, int x, int y, boolean showAxis, boolean showOuter, boolean showScan, boolean showGuidelines) {
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();

        matrixStack.translate(dx, dy, 200);
        RenderHelper.rotateXP(matrixStack, 180-xangle);
        RenderHelper.rotateYP(matrixStack, yangle);
        RenderHelper.rotateZP(matrixStack, zangle);
        matrixStack.scale(-scale, scale, scale);

        RenderSystem.disableBlend();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
//        RenderSystem.disableLighting();

        Tesselator tessellator = Tesselator.getInstance();

        setupScissor(gui);

        renderFacesForGui(matrixStack, tessellator, stack, showScan, false, -1);
        BlockPos dimension = ShapeCardItem.getDimension(stack);
        renderHelpers(matrixStack, tessellator, dimension.getX(), dimension.getY(), dimension.getZ(), showAxis, showOuter);

        RenderSystem.disableScissor();

        matrixStack.popPose();

        if (showGuidelines) {
            RenderSystem.lineWidth(3);
            BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
            buffer.addVertex(x - 62, y + 180, 0).setColor(1f, 0f, 0f, 1f);
            buffer.addVertex(x - 39, y + 180, 0).setColor(1f, 0f, 0f, 1f);
            buffer.addVertex(x - 62, y + 195, 0).setColor(0f, 0.8f, 0f, 1f);
            buffer.addVertex(x - 39, y + 195, 0).setColor(0f, 0.8f, 0f, 1f);
            buffer.addVertex(x - 62, y + 210, 0).setColor(0f, 0f, 1f, 1f);
            buffer.addVertex(x - 39, y + 210, 0).setColor(0f, 0f, 1f, 1f);
            drawIfNotEmpty(buffer);
        }

        RenderSystem.disableBlend();
//        RenderHelper.turnBackOn();    // @todo 1.18

        RenderData data = ShapeDataManagerClient.getRenderData(shapeID);
        if (data != null && !data.previewMessage.isEmpty()) {
            graphics.drawString(Minecraft.getInstance().font, data.previewMessage, gui.getPreviewLeft()+84, gui.getPreviewTop()+50, 0xffff0000, false);
        }

    }

    private void renderHelpers(PoseStack poseStack, Tesselator tessellator, int xlen, int ylen, int zlen, boolean showAxis, boolean showOuter) {
        // X, Y, Z axis
        if (showAxis) {
            ShapeRenderer.renderAxis(poseStack, tessellator, xlen/2, ylen/2, zlen/2);
        }

        if (showOuter) {
            ShapeRenderer.renderOuterBox(poseStack, tessellator, xlen, ylen, zlen);
        }
    }


    private void renderHelpersInGui(Tesselator tessellator, int xlen, int ylen, int zlen, boolean showAxis, boolean showOuter) {
        // X, Y, Z axis
        if (showAxis) {
            ShapeRenderer.renderAxisInGui(tessellator, xlen/2, ylen/2, zlen/2);
        }

        if (showOuter) {
            ShapeRenderer.renderOuterBoxInGui(tessellator, xlen, ylen, zlen);
        }
    }

    private static Vec3 offset = new Vec3(0, 0, 0);

    private static Vec3 setOffset(double x, double y, double z) {
        Vec3 old = offset;
        offset = new Vec3(x, y, z);
        return old;
    }

    private static void restoreOffset(Vec3 prev) {
        offset = prev;
    }

    private static void drawIfNotEmpty(BufferBuilder buffer) {
        MeshData meshData = buffer.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }
    }

    private static void add(Matrix4f matrix, BufferBuilder buffer, double x, double y, double z) {
        buffer.addVertex(matrix, (float) (x + offset.x), (float) (y + offset.y), (float) (z + offset.z)).setColor(1f, 1f, 1f, 1f);
    }

    private static void add(Matrix4f matrix, BufferBuilder buffer, double x, double y, double z, float r, float g, float b, float a) {
        buffer.addVertex(matrix, (float) (x + offset.x), (float) (y + offset.y), (float) (z + offset.z)).setColor(r, g, b, a);
    }

    private static void add(BufferBuilder buffer, double x, double y, double z) {
        buffer.addVertex((float) (x + offset.x), (float) (y + offset.y), (float) (z + offset.z)).setColor(1f, 1f, 1f, 1f);
    }

    private static void add(BufferBuilder buffer, double x, double y, double z, float r, float g, float b, float a) {
        buffer.addVertex((float) (x + offset.x), (float) (y + offset.y), (float) (z + offset.z)).setColor(r, g, b, a);
    }

    static void renderOuterBox(PoseStack poseStack, Tesselator tessellator, int xlen, int ylen, int zlen) {
        RenderSystem.lineWidth(1.0f);
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();
        Vec3 origOffset = setOffset(.5, .5, .5);
        int xleft = -xlen / 2;
        int xright = xlen / 2 + (xlen & 1);
        int ybot = -ylen / 2;
        int ytop = ylen / 2 + (ylen & 1);
        int zsouth = -zlen / 2;
        int znorth = zlen / 2 + (zlen & 1);

        add(matrix, buffer, xleft, ybot, zsouth);
        add(matrix, buffer, xright, ybot, zsouth);
        add(matrix, buffer, xleft, ybot, zsouth);
        add(matrix, buffer, xleft, ytop, zsouth);
        add(matrix, buffer, xleft, ybot, zsouth);
        add(matrix, buffer, xleft, ybot, znorth);
        add(matrix, buffer, xright, ytop, znorth);
        add(matrix, buffer, xleft, ytop, znorth);
        add(matrix, buffer, xright, ytop, znorth);
        add(matrix, buffer, xright, ybot, znorth);
        add(matrix, buffer, xright, ytop, znorth);
        add(matrix, buffer, xright, ytop, zsouth);
        add(matrix, buffer, xright, ybot, zsouth);
        add(matrix, buffer, xright, ybot, znorth);
        add(matrix, buffer, xright, ybot, zsouth);
        add(matrix, buffer, xright, ytop, zsouth);
        add(matrix, buffer, xleft, ytop, zsouth);
        add(matrix, buffer, xright, ytop, zsouth);
        add(matrix, buffer, xleft, ytop, zsouth);
        add(matrix, buffer, xleft, ytop, znorth);
        add(matrix, buffer, xleft, ytop, znorth);
        add(matrix, buffer, xleft, ybot, znorth);
        add(matrix, buffer, xleft, ybot, znorth);
        add(matrix, buffer, xright, ybot, znorth);

        restoreOffset(origOffset);
        drawIfNotEmpty(buffer);
    }

    static void renderOuterBoxInGui(Tesselator tessellator, int xlen, int ylen, int zlen) {
        RenderSystem.lineWidth(1.0f);
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        Vec3 origOffset = setOffset(.5, .5, .5);
        int xleft = -xlen / 2;
        int xright = xlen / 2 + (xlen & 1);
        int ybot = -ylen / 2;
        int ytop = ylen / 2 + (ylen & 1);
        int zsouth = -zlen / 2;
        int znorth = zlen / 2 + (zlen & 1);

        add(buffer, xleft, ybot, zsouth);
        add(buffer, xright, ybot, zsouth);
        add(buffer, xleft, ybot, zsouth);
        add(buffer, xleft, ytop, zsouth);
        add(buffer, xleft, ybot, zsouth);
        add(buffer, xleft, ybot, znorth);
        add(buffer, xright, ytop, znorth);
        add(buffer, xleft, ytop, znorth);
        add(buffer, xright, ytop, znorth);
        add(buffer, xright, ybot, znorth);
        add(buffer, xright, ytop, znorth);
        add(buffer, xright, ytop, zsouth);
        add(buffer, xright, ybot, zsouth);
        add(buffer, xright, ybot, znorth);
        add(buffer, xright, ybot, zsouth);
        add(buffer, xright, ytop, zsouth);
        add(buffer, xleft, ytop, zsouth);
        add(buffer, xright, ytop, zsouth);
        add(buffer, xleft, ytop, zsouth);
        add(buffer, xleft, ytop, znorth);
        add(buffer, xleft, ytop, znorth);
        add(buffer, xleft, ybot, znorth);
        add(buffer, xleft, ybot, znorth);
        add(buffer, xright, ybot, znorth);

        restoreOffset(origOffset);
        drawIfNotEmpty(buffer);
    }

    static void renderAxisInGui(Tesselator tessellator, int xlen, int ylen, int zlen) {
        RenderSystem.lineWidth(2.5f);
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        Vec3 origOffset = setOffset(.5, .5, .5);
        add(buffer, 0, 0, 0, 1f, 0f, 0f, 1f);
        add(buffer, xlen, 0, 0, 1f, 0f, 0f, 1f);
        add(buffer, 0, 0, 0, 0f, 1f, 0f, 1f);
        add(buffer, 0, ylen, 0, 0f, 1f, 0f, 1f);
        add(buffer, 0, 0, 0, 0f, 0f, 1f, 1f);
        add(buffer, 0, 0, zlen, 0f, 0f, 1f, 1f);
        restoreOffset(origOffset);
        drawIfNotEmpty(buffer);
    }

    static void renderAxis(PoseStack poseStack, Tesselator tessellator, int xlen, int ylen, int zlen) {
        RenderSystem.lineWidth(2.5f);
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();
        Vec3 origOffset = setOffset(.5, .5, .5);
        add(matrix, buffer, 0, 0, 0, 1f, 0f, 0f, 1f);
        add(matrix, buffer, xlen, 0, 0, 1f, 0f, 0f, 1f);
        add(matrix, buffer, 0, 0, 0, 0f, 1f, 0f, 1f);
        add(matrix, buffer, 0, ylen, 0, 0f, 1f, 0f, 1f);
        add(matrix, buffer, 0, 0, 0, 0f, 0f, 1f, 1f);
        add(matrix, buffer, 0, 0, zlen, 0f, 0f, 1f, 1f);
        restoreOffset(origOffset);
        drawIfNotEmpty(buffer);
    }

    private int calculateChecksum(ItemStack stack) {
        Check32 crc = new Check32();
        if (!stack.isEmpty()) {
            ShapeCardItem.getFormulaCheckClient(stack, crc);
        }
        return crc.get();
    }

    private int extraDataCounter = 0;

    private RenderData requestRenderData(ItemStack stack) {
        RenderData data = getRenderDataAndCreate(shapeID);
        int check = calculateChecksum(stack);
        if (check != data.getChecksum()) {
            data.clearRequest();
            data.clearData();
            data.setChecksum(check);
            data.setWantData(true);
        } else if (data.isRequestTimedOut()) {
            data.clearRequest();
            data.setWantData(true);
        }

        if ((data.isWantData() || !data.hasData()) && !data.isRequestInFlight()) {
            if (waitForNewRequest <= 0) {
                RFToolsBuilderMessages.sendToServer(PacketRequestShapeData.create(stack, shapeID, check));
                waitForNewRequest = 20;
                data.setWantData(false);
                data.markRequestSent();
            } else {
                waitForNewRequest--;
            }
        }
        return data;
    }

    // @todo 1.15 in world version
    private boolean renderFacesInWorld(PoseStack poseStack, final Tesselator tesselator,
                                       ItemStack stack, boolean showScan, boolean grayscale, int scanId) {

        RenderData data = requestRenderData(stack);

        boolean needScanSound = false;
        if (data.getPlanes() != null) {
            long time = System.currentTimeMillis();
            for (RenderData.RenderPlane plane : data.getPlanes()) {
                if (plane != null) {
                    renderPlaneImmediate(poseStack, tesselator, plane, grayscale, false);
                    boolean flash = false;
                    if (showScan) {
                        plane.markFlashRendered();
                        flash = plane.isFlashing(time);
                    }
                    if (flash) {
                        needScanSound = true;
                        GlStateManager._enableBlend();
                        GlStateManager._blendFunc(GL11.GL_ONE, GL11.GL_ONE);
//                        GlStateManager.colorMask(false, false, true, true);
                    }
                    if (flash) {
                        renderPlaneImmediate(poseStack, tesselator, plane, grayscale, false);
                    }
                    if (flash) {
                        GlStateManager._disableBlend();
                        GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//                        GlStateManager.colorMask(true, true, true, true);
                    }
                }
            }
        }

        // Possibly request extra data for the scan
        int recursiveScanId = ShapeCardItem.getScanIdRecursive(stack);
        if (recursiveScanId > 0) {
            extraDataCounter--;
            if (extraDataCounter <= 0) {
                extraDataCounter = 10;
                ScanDataManagerClient.getScansClient().requestExtraDataClient(recursiveScanId);
            }
            ScanExtraData extraData = ScanDataManagerClient.getScansClient().getExtraDataClient(recursiveScanId);
            for (ScanExtraData.Beacon beacon : extraData.getBeacons()) {
                int x = beacon.getPos().getX();
                int y = beacon.getPos().getY()+1;
                int z = beacon.getPos().getZ();
                BeaconType type = beacon.getType();
//                GlStateManager._translatef(x, y, z); // @todo 1.18
                RenderData.RenderElement element = getBeaconElement(tesselator, type, beacon.isDoBeacon());
                element.render(new PoseStack());
//                GlStateManager._translatef(-x, -y, -z);
            }
        }

        return needScanSound;
    }

    private boolean renderFacesForGui(PoseStack poseStack, Tesselator tessellator,
                                      ItemStack stack, boolean showScan, boolean grayscale, int scanId) {

        RenderData data = requestRenderData(stack);

        boolean needScanSound = false;
        if (data.getPlanes() != null) {
            long time = System.currentTimeMillis();
            for (RenderData.RenderPlane plane : data.getPlanes()) {
                if (plane != null) {
                    renderPlaneImmediate(poseStack, tessellator, plane, grayscale, true);
                    boolean flash = false;
                    if (showScan) {
                        plane.markFlashRendered();
                        flash = plane.isFlashing(time);
                    }
                    if (flash) {
                        needScanSound = true;
                        RenderSystem.enableBlend();
                        RenderSystem.blendFunc(GL11.GL_ONE, GL11.GL_ONE);
                    }
                    if (flash) {
                        renderPlaneImmediate(poseStack, tessellator, plane, grayscale, true);
                    }
                    if (flash) {
                        RenderSystem.disableBlend();
                        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    }
                }
            }
        }

        // Possibly request extra data for the scan
        int recursiveScanId = ShapeCardItem.getScanIdRecursive(stack);
        if (recursiveScanId > 0) {
            extraDataCounter--;
            if (extraDataCounter <= 0) {
                extraDataCounter = 10;
                ScanDataManagerClient.getScansClient().requestExtraDataClient(recursiveScanId);
            }
            ScanExtraData extraData = ScanDataManagerClient.getScansClient().getExtraDataClient(recursiveScanId);
            for (ScanExtraData.Beacon beacon : extraData.getBeacons()) {
                int x = beacon.getPos().getX();
                int y = beacon.getPos().getY()+1;
                int z = beacon.getPos().getZ();
                BeaconType type = beacon.getType();
//                RenderSystem.translatef(x, y, z); // @todo 1.18
                RenderData.RenderElement element = getBeaconElement(tessellator, type, beacon.isDoBeacon());
                element.render(poseStack);
//                RenderSystem.translatef(-x, -y, -z);
            }
        }

        return needScanSound;
    }

    private void renderPlaneImmediate(PoseStack poseStack, Tesselator tessellator, RenderData.RenderPlane plane, boolean grayscale, boolean gui) {
        Matrix4f matrix = poseStack.last().pose();
        Map<BlockState, ShapeBlockInfo> palette = new HashMap<>();

        int y = plane.getY();
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (RenderData.RenderStrip strip : plane.getStrips()) {
            int z = plane.getStartz();
            int x = strip.getX();
            List<Pair<Integer, BlockState>> columnData = strip.getData();
            for (int i = 0; i < columnData.size(); i++) {
                Pair<Integer, BlockState> pair = columnData.get(i);
                int cnt = pair.getKey();
                BlockState state = pair.getValue();
                if (state != null) {
                    Vec3 origOffset = setOffset(x, y, z);
                    ShapeBlockInfo info = ShapeBlockInfo.getBlockInfo(palette, state);
                    ShapeBlockInfo.Col col = info.getCol();
                    float r = col.getR();
                    float g = col.getG();
                    float b = col.getB();
                    if (grayscale) {
                        float a = 0.21f * r + 0.72f * g + 0.07f * b;
                        r = g = b = a;
                    }
                    ShapeBlockInfo.IBlockRender bd = info.getRender();
                    if (bd == null) {
                        addSideFullTextureU(matrix, buffer, cnt, r * .8f, g * .8f, b * .8f, gui);
                        addSideFullTextureD(matrix, buffer, cnt, r * .8f, g * .8f, b * .8f, gui);
                        if (strip.isEmptyAt(i - 1, palette)) {
                            addSideFullTextureN(matrix, buffer, cnt, r * 1.2f, g * 1.2f, b * 1.2f, gui);
                        }
                        if (strip.isEmptyAt(i + 1, palette)) {
                            addSideFullTextureS(matrix, buffer, cnt, r * 1.2f, g * 1.2f, b * 1.2f, gui);
                        }
                        addSideFullTextureW(matrix, buffer, cnt, r, g, b, gui);
                        addSideFullTextureE(matrix, buffer, cnt, r, g, b, gui);
                    } else {
                        for (int c = 0; c < cnt; c++) {
                            bd.render(buffer, c, r, g, b);
                        }
                    }
                    restoreOffset(origOffset);
                }
                z += cnt;
            }
        }
        drawIfNotEmpty(buffer);
    }

    private void createRenderData(RenderData.RenderPlane plane, RenderData data, boolean grayscale) {
        Map<BlockState, ShapeBlockInfo> palette = new HashMap<>();

        int avgcnt = 0;
        int total = 0;
        int y = plane.getY();
        int offsety = plane.getOffsety();

        data.createRenderList(offsety);
        BufferBuilder buffer = RenderData.vboBuffer;
        if (buffer == null) {
            return;
        }

        for (RenderData.RenderStrip strip : plane.getStrips()) {
            int z = plane.getStartz();
            int x = strip.getX();
            List<Pair<Integer, BlockState>> columnData = strip.getData();
            for (int i = 0; i < columnData.size(); i++) {
                Pair<Integer, BlockState> pair = columnData.get(i);
                int cnt = pair.getKey();
                BlockState state = pair.getValue();
                if (state != null) {
                    Vec3 origOffset = setOffset(x, y, z);
                    avgcnt += cnt;
                    total++;
                    ShapeBlockInfo info = ShapeBlockInfo.getBlockInfo(palette, state);
                    ShapeBlockInfo.Col col = info.getCol();
                    float r = col.getR();
                    float g = col.getG();
                    float b = col.getB();
                    if (grayscale) {
//                        float a = (r+g+b)/3.0f;
                        float a = 0.21f*r+0.72f*g+0.07f*b;
                        r = g = b = a;
                    }
                    ShapeBlockInfo.IBlockRender bd = info.getRender();
                    if (bd == null) {
                        addSideFullTextureU(buffer, cnt, r * .8f, g * .8f, b * .8f);
                        addSideFullTextureD(buffer, cnt, r * .8f, g * .8f, b * .8f);
                        if (strip.isEmptyAt(i - 1, palette)) {
                            addSideFullTextureN(buffer, cnt, r * 1.2f, g * 1.2f, b * 1.2f);
                        }
                        if (strip.isEmptyAt(i + 1, palette)) {
                            addSideFullTextureS(buffer, cnt, r * 1.2f, g * 1.2f, b * 1.2f);
                        }
                        addSideFullTextureW(buffer, cnt, r, g, b);
                        addSideFullTextureE(buffer, cnt, r, g, b);
                    } else {
                        for (int c = 0 ; c < cnt ; c++) {
                            bd.render(buffer, c, r, g, b);
                        }
                    }

                    restoreOffset(origOffset);
                }
                z += cnt;
            }
        }

        data.performRenderToList(offsety);

//        float avg = avgcnt / (float) total;
//        System.out.println("y = " + offsety + ", avg = " + avg + ", quads = " + quadcnt);
    }

    private static RenderData.RenderElement beaconElement[] = null;
    private static RenderData.RenderElement beaconElementBeacon[] = null;

    private static RenderData.RenderElement getBeaconElement(Tesselator tesselator, BeaconType type, boolean doBeacon) {
        if (beaconElement == null) {
            beaconElement = new RenderData.RenderElement[BeaconType.VALUES.length];
            beaconElementBeacon = new RenderData.RenderElement[BeaconType.VALUES.length];
            for (int i = 0 ; i < BeaconType.VALUES.length ; i++) {
                beaconElement[i] = null;
                beaconElementBeacon[i] = null;
            }
        }

        RenderData.RenderElement[] elements;
        if (doBeacon) {
            elements = ShapeRenderer.beaconElementBeacon;
        } else {
            elements = ShapeRenderer.beaconElement;
        }
        if (elements[type.ordinal()] == null) {
            elements[type.ordinal()] = new RenderData.RenderElement();
            elements[type.ordinal()].createRenderList();
            RenderSystem.lineWidth(3);
            BufferBuilder buffer = RenderData.vboBuffer;
            if (buffer == null) {
                return elements[type.ordinal()];
            }
            float r = type.getR();
            float g = type.getG();
            float b = type.getB();

            Vec3 origOffset = setOffset(0, -.7f, 0);
            addSideN(buffer, r, g, b, .3f);
            addSideS(buffer, r, g, b, .3f);
            addSideW(buffer, r, g, b, .3f);
            addSideE(buffer, r, g, b, .3f);
            addSideU(buffer, r, g, b, .3f);
            addSideD(buffer, r, g, b, .3f);
            restoreOffset(origOffset);
            origOffset = setOffset(0, -.2f, 0);
            addSideN(buffer, r, g, b, .2f);
            addSideS(buffer, r, g, b, .2f);
            addSideW(buffer, r, g, b, .2f);
            addSideE(buffer, r, g, b, .2f);
            addSideU(buffer, r, g, b, .2f);
            addSideD(buffer, r, g, b, .2f);
            restoreOffset(origOffset);

            if (doBeacon) {
                origOffset = setOffset(0, .2f, 0);
                addSideN(buffer, r, g, b, .1f, ScannerConfiguration.locatorBeaconHeight.get());
                addSideS(buffer, r, g, b, .1f, ScannerConfiguration.locatorBeaconHeight.get());
                addSideW(buffer, r, g, b, .1f, ScannerConfiguration.locatorBeaconHeight.get());
                addSideE(buffer, r, g, b, .1f, ScannerConfiguration.locatorBeaconHeight.get());
                restoreOffset(origOffset);
            }
            elements[type.ordinal()].performRenderToList();
        }
        return elements[type.ordinal()];
    }

    private static void setupScissor(IShapeParentGui gui) {
        Minecraft mc = Minecraft.getInstance();

        int xScale = mc.getWindow().getGuiScaledWidth();
        int yScale = mc.getWindow().getGuiScaledHeight();
        int sx = (gui.getPreviewLeft() + 84) * mc.getWindow().getScreenWidth() / xScale;
        int sy = (mc.getWindow().getScreenHeight()) - (gui.getPreviewTop() + 136) * mc.getWindow().getScreenHeight() / yScale;
        int sw = 161 * mc.getWindow().getScreenWidth() / xScale;
        int sh = 130 * mc.getWindow().getScreenHeight() / yScale;

        RenderSystem.enableScissor(sx, sy, sw, sh);
    }

    public static void addSideFullTextureD(BufferBuilder buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.addVertex(0, 0, 0).setColor(r, g, b, a);
        buffer.addVertex(1, 0, 0).setColor(r, g, b, a);
        buffer.addVertex(1, 0, cnt).setColor(r, g, b, a);
        buffer.addVertex(0, 0, cnt).setColor(r, g, b, a);
    }

    public static void addSideFullTextureD(Matrix4f matrix, BufferBuilder buffer, int cnt, float r, float g, float b, boolean gui) {
        float a = gui ? 0.9f : 0.5f;
        buffer.addVertex(matrix, (float) offset.x, (float) offset.y, (float) offset.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) offset.y, (float) offset.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) offset.y, (float) (cnt + offset.z)).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) offset.x, (float) offset.y, (float) (cnt + offset.z)).setColor(r, g, b, a);
    }

    public static void addSideFullTextureU(BufferBuilder buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.addVertex(0, 1, cnt).setColor(r, g, b, a);
        buffer.addVertex(1, 1, cnt).setColor(r, g, b, a);
        buffer.addVertex(1, 1, 0).setColor(r, g, b, a);
        buffer.addVertex(0, 1, 0).setColor(r, g, b, a);
    }

    public static void addSideFullTextureU(Matrix4f matrix, BufferBuilder buffer, int cnt, float r, float g, float b, boolean gui) {
        float a = gui ? 0.9f : 0.5f;
        buffer.addVertex(matrix, (float) offset.x, (float) (1 + offset.y), (float) (cnt + offset.z)).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) (1 + offset.y), (float) (cnt + offset.z)).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) (1 + offset.y), (float) offset.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) offset.x, (float) (1 + offset.y), (float) offset.z).setColor(r, g, b, a);
    }

    public static void addSideFullTextureE(BufferBuilder buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.addVertex(1, 0, 0).setColor(r, g, b, a);
        buffer.addVertex(1, 1, 0).setColor(r, g, b, a);
        buffer.addVertex(1, 1, cnt).setColor(r, g, b, a);
        buffer.addVertex(1, 0, cnt).setColor(r, g, b, a);
    }

    public static void addSideFullTextureE(Matrix4f matrix, BufferBuilder buffer, int cnt, float r, float g, float b, boolean gui) {
        float a = gui ? 0.9f : 0.5f;
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) offset.y, (float) offset.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) (1 + offset.y), (float) offset.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) (1 + offset.y), (float) (cnt + offset.z)).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) offset.y, (float) (cnt + offset.z)).setColor(r, g, b, a);
    }

    public static void addSideFullTextureW(BufferBuilder buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.addVertex(0, 0, cnt).setColor(r, g, b, a);
        buffer.addVertex(0, 1, cnt).setColor(r, g, b, a);
        buffer.addVertex(0, 1, 0).setColor(r, g, b, a);
        buffer.addVertex(0, 0, 0).setColor(r, g, b, a);
    }

    public static void addSideFullTextureW(Matrix4f matrix, BufferBuilder buffer, int cnt, float r, float g, float b, boolean gui) {
        float a = gui ? 0.9f : 0.5f;
        buffer.addVertex(matrix, (float) offset.x, (float) offset.y, (float) (cnt + offset.z)).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) offset.x, (float) (1 + offset.y), (float) (cnt + offset.z)).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) offset.x, (float) (1 + offset.y), (float) offset.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) offset.x, (float) offset.y, (float) offset.z).setColor(r, g, b, a);
    }

    public static void addSideFullTextureN(BufferBuilder buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.addVertex(1, 1, 0).setColor(r, g, b, a);
        buffer.addVertex(1, 0, 0).setColor(r, g, b, a);
        buffer.addVertex(0, 0, 0).setColor(r, g, b, a);
        buffer.addVertex(0, 1, 0).setColor(r, g, b, a);
    }

    public static void addSideFullTextureN(Matrix4f matrix, BufferBuilder buffer, int cnt, float r, float g, float b, boolean gui) {
        float a = gui ? 0.9f : 0.5f;
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) (1 + offset.y), (float) offset.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) offset.y, (float) offset.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) offset.x, (float) offset.y, (float) offset.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) offset.x, (float) (1 + offset.y), (float) offset.z).setColor(r, g, b, a);
    }

    public static void addSideFullTextureS(BufferBuilder buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.addVertex(1, 0, cnt).setColor(r, g, b, a);
        buffer.addVertex(1, 1, cnt).setColor(r, g, b, a);
        buffer.addVertex(0, 1, cnt).setColor(r, g, b, a);
        buffer.addVertex(0, 0, cnt).setColor(r, g, b, a);
    }

    public static void addSideFullTextureS(Matrix4f matrix, BufferBuilder buffer, int cnt, float r, float g, float b, boolean gui) {
        float a = gui ? 0.9f : 0.5f;
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) offset.y, (float) (cnt + offset.z)).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) (1 + offset.y), (float) (cnt + offset.z)).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) offset.x, (float) (1 + offset.y), (float) (cnt + offset.z)).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) offset.x, (float) offset.y, (float) (cnt + offset.z)).setColor(r, g, b, a);
    }




    public static void addSideD(BufferBuilder buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.addVertex(l, l, l).setColor(r, g, b, a);
        buffer.addVertex(h, l, l).setColor(r, g, b, a);
        buffer.addVertex(h, l, h).setColor(r, g, b, a);
        buffer.addVertex(l, l, h).setColor(r, g, b, a);
    }

    public static void addSideU(BufferBuilder buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.addVertex(l, h, h).setColor(r, g, b, a);
        buffer.addVertex(h, h, h).setColor(r, g, b, a);
        buffer.addVertex(h, h, l).setColor(r, g, b, a);
        buffer.addVertex(l, h, l).setColor(r, g, b, a);
    }

    public static void addSideE(BufferBuilder buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.addVertex(h, l, l).setColor(r, g, b, a);
        buffer.addVertex(h, h, l).setColor(r, g, b, a);
        buffer.addVertex(h, h, h).setColor(r, g, b, a);
        buffer.addVertex(h, l, h).setColor(r, g, b, a);
    }

    public static void addSideW(BufferBuilder buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.addVertex(l, l, h).setColor(r, g, b, a);
        buffer.addVertex(l, h, h).setColor(r, g, b, a);
        buffer.addVertex(l, h, l).setColor(r, g, b, a);
        buffer.addVertex(l, l, l).setColor(r, g, b, a);
    }

    public static void addSideN(BufferBuilder buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.addVertex(h, h, l).setColor(r, g, b, a);
        buffer.addVertex(h, l, l).setColor(r, g, b, a);
        buffer.addVertex(l, l, l).setColor(r, g, b, a);
        buffer.addVertex(l, h, l).setColor(r, g, b, a);
    }

    public static void addSideS(BufferBuilder buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.addVertex(h, l, h).setColor(r, g, b, a);
        buffer.addVertex(h, h, h).setColor(r, g, b, a);
        buffer.addVertex(l, h, h).setColor(r, g, b, a);
        buffer.addVertex(l, l, h).setColor(r, g, b, a);
    }





    public static void addSideE(BufferBuilder buffer, float r, float g, float b, float size, float height) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.addVertex(h, 0, l).setColor(r, g, b, a);
        buffer.addVertex(h, height, l).setColor(r, g, b, a);
        buffer.addVertex(h, height, h).setColor(r, g, b, a);
        buffer.addVertex(h, 0, h).setColor(r, g, b, a);
    }

    public static void addSideW(BufferBuilder buffer, float r, float g, float b, float size, float height) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.addVertex(l, 0, h).setColor(r, g, b, a);
        buffer.addVertex(l, height, h).setColor(r, g, b, a);
        buffer.addVertex(l, height, l).setColor(r, g, b, a);
        buffer.addVertex(l, 0, l).setColor(r, g, b, a);
    }

    public static void addSideN(BufferBuilder buffer, float r, float g, float b, float size, float height) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.addVertex(h, height, l).setColor(r, g, b, a);
        buffer.addVertex(h, 0, l).setColor(r, g, b, a);
        buffer.addVertex(l, 0, l).setColor(r, g, b, a);
        buffer.addVertex(l, height, l).setColor(r, g, b, a);
    }

    public static void addSideS(BufferBuilder buffer, float r, float g, float b, float size, float height) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.addVertex(h, 0, h).setColor(r, g, b, a);
        buffer.addVertex(h, height, h).setColor(r, g, b, a);
        buffer.addVertex(l, height, h).setColor(r, g, b, a);
        buffer.addVertex(l, 0, h).setColor(r, g, b, a);
    }

}
