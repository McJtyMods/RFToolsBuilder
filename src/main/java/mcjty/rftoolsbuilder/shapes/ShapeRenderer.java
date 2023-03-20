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
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
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

    public static void setRenderData(ShapeID id, @Nullable RenderData.RenderPlane plane, int offsetY, int dy, String msg) {
        RenderData data = getRenderDataAndCreate(id);
        data.setPlaneData(plane, offsetY, dy);
        data.previewMessage = msg;
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

    public void handleMouseWheel(double dwheel) {
        if (dwheel < 0) {
            scale *= .6;
            if (scale <= 0.1) {
                scale = .1f;
            }
        } else if (dwheel > 0) {
            scale *= 1.4;
        }
    }

    // @todo 1.15: this needs rewriting
    public boolean renderShapeInWorld(ItemStack stack, double x, double y, double z, float offset, float scale, float angle,
                                      boolean scan, ShapeID shape) {
//        GlStateManager._pushMatrix();
//        GlStateManager._translatef((float) x + 0.5F, (float) y + 1F + offset, (float) z + 0.5F);
//        GlStateManager._scalef(scale, scale, scale);
//        GlStateManager._rotatef(angle, 0, 1, 0);
//
////        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
//        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
//        GlStateManager._disableBlend();
//        GlStateManager._enableCull();
//        GlStateManager._disableLighting();
//        GlStateManager._disableTexture();
//
//        Tesselator tessellator = Tesselator.getInstance();
//        BufferBuilder buffer = tessellator.getBuilder();
//        boolean doSound = renderFacesInWorld(buffer, stack, scan, shape.isGrayscale(), shape.getScanId());
//
//        GlStateManager._enableTexture();
//        GlStateManager._disableBlend();
//        GlStateManager._enableLighting();
////        RenderHelper.enableStandardItemLighting();
//        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
//
//        GlStateManager._popMatrix();
//        return doSound;
        return false;
    }

    public void renderShape(PoseStack matrixStack, IShapeParentGui gui, ItemStack stack, int x, int y, boolean showAxis, boolean showOuter, boolean showScan, boolean showGuidelines) {
        setupScissor(gui);

        matrixStack.pushPose();

        matrixStack.translate(dx, dy, 200);
        RenderHelper.rotateXP(matrixStack, 180-xangle);
        RenderHelper.rotateYP(matrixStack, yangle);
        RenderHelper.rotateZP(matrixStack, zangle);
        matrixStack.scale(-scale, scale, scale);

        RenderSystem.disableBlend();
        RenderSystem.disableCull();
//        RenderSystem.disableLighting();

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        renderFacesForGui(tessellator, buffer, stack, showScan, false, -1);
        BlockPos dimension = ShapeCardItem.getDimension(stack);
        renderHelpers(tessellator, buffer, dimension.getX(), dimension.getY(), dimension.getZ(), showAxis, showOuter);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        matrixStack.popPose();

        if (showGuidelines) {
            RenderSystem.lineWidth(3);
            buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
            buffer.vertex(x - 62, y + 180, 0).color(1f, 0f, 0f, 1f).endVertex();
            buffer.vertex(x - 39, y + 180, 0).color(1f, 0f, 0f, 1f).endVertex();
            buffer.vertex(x - 62, y + 195, 0).color(0f, 0.8f, 0f, 1f).endVertex();
            buffer.vertex(x - 39, y + 195, 0).color(0f, 0.8f, 0f, 1f).endVertex();
            buffer.vertex(x - 62, y + 210, 0).color(0f, 0f, 1f, 1f).endVertex();
            buffer.vertex(x - 39, y + 210, 0).color(0f, 0f, 1f, 1f).endVertex();
            tessellator.end();
        }

        RenderSystem.disableBlend();
//        RenderHelper.turnBackOn();    // @todo 1.18

        RenderData data = ShapeDataManagerClient.getRenderData(shapeID);
        if (data != null && !data.previewMessage.isEmpty()) {
            Minecraft.getInstance().font.draw(matrixStack, data.previewMessage, gui.getPreviewLeft()+84, gui.getPreviewTop()+50, 0xffff0000);
        }

    }

    private void renderHelpers(Tesselator tessellator, BufferBuilder buffer, int xlen, int ylen, int zlen, boolean showAxis, boolean showOuter) {
        // X, Y, Z axis
        if (showAxis) {
            ShapeRenderer.renderAxis(tessellator, buffer, xlen/2, ylen/2, zlen/2);
        }

        if (showOuter) {
            ShapeRenderer.renderOuterBox(tessellator, buffer, xlen, ylen, zlen);
        }
    }


    private void renderHelpersInGui(Tesselator tessellator, BufferBuilder buffer, int xlen, int ylen, int zlen, boolean showAxis, boolean showOuter) {
        // X, Y, Z axis
        if (showAxis) {
            ShapeRenderer.renderAxisInGui(tessellator, buffer, xlen/2, ylen/2, zlen/2);
        }

        if (showOuter) {
            ShapeRenderer.renderOuterBoxInGui(tessellator, buffer, xlen, ylen, zlen);
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

    private static void add(BufferBuilder buffer, double x, double y, double z) {
        buffer.vertex(x + offset.x, y + offset.y, z + offset.z).color(1f, 1f, 1f, 1f).endVertex();
    }

    private static void add(BufferBuilder buffer, double x, double y, double z, float r, float g, float b, float a) {
        buffer.vertex(x + offset.x, y + offset.y, z + offset.z).color(r, g, b, a).endVertex();
    }

    static void renderOuterBox(Tesselator tessellator, BufferBuilder buffer, int xlen, int ylen, int zlen) {
        RenderSystem.lineWidth(1.0f);
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
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
        tessellator.end();
    }

    static void renderOuterBoxInGui(Tesselator tessellator, BufferBuilder buffer, int xlen, int ylen, int zlen) {
        RenderSystem.lineWidth(1.0f);
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
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
        tessellator.end();
    }

    static void renderAxisInGui(Tesselator tessellator, BufferBuilder buffer, int xlen, int ylen, int zlen) {
        RenderSystem.lineWidth(2.5f);
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        Vec3 origOffset = setOffset(.5, .5, .5);
        add(buffer, 0, 0, 0, 1f, 0f, 0f, 1f);
        add(buffer, xlen, 0, 0, 1f, 0f, 0f, 1f);
        add(buffer, 0, 0, 0, 0f, 1f, 0f, 1f);
        add(buffer, 0, ylen, 0, 0f, 1f, 0f, 1f);
        add(buffer, 0, 0, 0, 0f, 0f, 1f, 1f);
        add(buffer, 0, 0, zlen, 0f, 0f, 1f, 1f);
        restoreOffset(origOffset);
        tessellator.end();
    }

    static void renderAxis(Tesselator tessellator, BufferBuilder buffer, int xlen, int ylen, int zlen) {
        RenderSystem.lineWidth(2.5f);
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        Vec3 origOffset = setOffset(.5, .5, .5);
        add(buffer, 0, 0, 0, 1f, 0f, 0f, 1f);
        add(buffer, xlen, 0, 0, 1f, 0f, 0f, 1f);
        add(buffer, 0, 0, 0, 0f, 1f, 0f, 1f);
        add(buffer, 0, ylen, 0, 0f, 1f, 0f, 1f);
        add(buffer, 0, 0, 0, 0f, 0f, 1f, 1f);
        add(buffer, 0, 0, zlen, 0f, 0f, 1f, 1f);
        restoreOffset(origOffset);
        tessellator.end();
    }

    private int calculateChecksum(ItemStack stack) {
        Check32 crc = new Check32();
        if (!stack.isEmpty()) {
            ShapeCardItem.getFormulaCheckClient(stack, crc);
        }
        return crc.get();
    }

    private int extraDataCounter = 0;

    // @todo 1.15 in world version
    private boolean renderFacesInWorld(final BufferBuilder buffer,
                                       ItemStack stack, boolean showScan, boolean grayscale, int scanId) {

        RenderData data = getRenderDataAndCreate(shapeID);

        if (data.isWantData() || waitForNewRequest > 0) {
            if (waitForNewRequest <= 0) {
                // No positions, send a new request
                RFToolsBuilderMessages.INSTANCE.sendToServer(new PacketRequestShapeData(stack, shapeID));
                waitForNewRequest = 20;
                data.setWantData(false);
            } else {
                waitForNewRequest--;
            }
        } else {
            long check = calculateChecksum(stack);
            if (!data.hasData() || check != data.getChecksum()) {
                // Checksum failed, we want new data
                data.setChecksum(check);
                data.setWantData(true);
            }
        }

        boolean needScanSound = false;
        if (data.getPlanes() != null) {
            long time = System.currentTimeMillis();
            for (RenderData.RenderPlane plane : data.getPlanes()) {
                if (plane != null) {
                    if (plane.isDirty()) {
                        createRenderData(plane, data, grayscale);
                        plane.markClean();
                    }
                    boolean flash = showScan && (plane.getBirthtime() > time- ScannerConfiguration.projectorFlashTimeout.get());
                    if (flash) {
                        needScanSound = true;
                        GlStateManager._enableBlend();
                        GlStateManager._blendFunc(GL11.GL_ONE, GL11.GL_ONE);
//                        GlStateManager.colorMask(false, false, true, true);
                    }
                    plane.render();
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
                RenderData.RenderElement element = getBeaconElement(buffer, type, beacon.isDoBeacon());
                element.render();
//                GlStateManager._translatef(-x, -y, -z);
            }
        }

        return needScanSound;
    }

    private boolean renderFacesForGui(Tesselator tessellator, final BufferBuilder buffer,
                                      ItemStack stack, boolean showScan, boolean grayscale, int scanId) {

        RenderData data = getRenderDataAndCreate(shapeID);

        if (data.isWantData() || waitForNewRequest > 0) {
            if (waitForNewRequest <= 0) {
                // No positions, send a new request
                RFToolsBuilderMessages.INSTANCE.sendToServer(new PacketRequestShapeData(stack, shapeID));
                waitForNewRequest = 20;
                data.setWantData(false);
            } else {
                waitForNewRequest--;
            }
        } else {
            long check = calculateChecksum(stack);
            if (!data.hasData() || check != data.getChecksum()) {
                // Checksum failed, we want new data
                data.setChecksum(check);
                data.setWantData(true);
            }
        }

        boolean needScanSound = false;
        if (data.getPlanes() != null) {
            long time = System.currentTimeMillis();
            for (RenderData.RenderPlane plane : data.getPlanes()) {
                if (plane != null) {
                    if (plane.isDirty()) {
                        createRenderData(plane, data, grayscale);
                        plane.markClean();
                    }
                    boolean flash = showScan && (plane.getBirthtime() > time- ScannerConfiguration.projectorFlashTimeout.get());
                    if (flash) {
                        needScanSound = true;
                        RenderSystem.enableBlend();
                        RenderSystem.blendFunc(GL11.GL_ONE, GL11.GL_ONE);
                    }
                    plane.render();
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
                RenderData.RenderElement element = getBeaconElement(buffer, type, beacon.isDoBeacon());
                element.render();
//                RenderSystem.translatef(-x, -y, -z);
            }
        }

        return needScanSound;
    }

    private void createRenderData(RenderData.RenderPlane plane, RenderData data, boolean grayscale) {
        Map<BlockState, ShapeBlockInfo> palette = new HashMap<>();

        int avgcnt = 0;
        int total = 0;
        int y = plane.getY();
        int offsety = plane.getOffsety();

        data.createRenderList(offsety);
        RenderData.vboBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

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
                        addSideFullTextureU(RenderData.vboBuffer, cnt, r * .8f, g * .8f, b * .8f);
                        addSideFullTextureD(RenderData.vboBuffer, cnt, r * .8f, g * .8f, b * .8f);
                        if (strip.isEmptyAt(i - 1, palette)) {
                            addSideFullTextureN(RenderData.vboBuffer, cnt, r * 1.2f, g * 1.2f, b * 1.2f);
                        }
                        if (strip.isEmptyAt(i + 1, palette)) {
                            addSideFullTextureS(RenderData.vboBuffer, cnt, r * 1.2f, g * 1.2f, b * 1.2f);
                        }
                        addSideFullTextureW(RenderData.vboBuffer, cnt, r, g, b);
                        addSideFullTextureE(RenderData.vboBuffer, cnt, r, g, b);
                    } else {
                        for (int c = 0 ; c < cnt ; c++) {
                            bd.render(RenderData.vboBuffer, c, r, g, b);
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

    private static RenderData.RenderElement getBeaconElement(BufferBuilder buffer, BeaconType type, boolean doBeacon) {
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
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
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

        GL11.glScissor(sx, sy, sw, sh);
    }

    public static void addSideFullTextureD(BufferBuilder buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.vertex(0, 0, 0).color(r, g, b, a).endVertex();
        buffer.vertex(1, 0, 0).color(r, g, b, a).endVertex();
        buffer.vertex(1, 0, cnt).color(r, g, b, a).endVertex();
        buffer.vertex(0, 0, cnt).color(r, g, b, a).endVertex();
    }

    public static void addSideFullTextureU(BufferBuilder buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.vertex(0, 1, cnt).color(r, g, b, a).endVertex();
        buffer.vertex(1, 1, cnt).color(r, g, b, a).endVertex();
        buffer.vertex(1, 1, 0).color(r, g, b, a).endVertex();
        buffer.vertex(0, 1, 0).color(r, g, b, a).endVertex();
    }

    public static void addSideFullTextureE(BufferBuilder buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.vertex(1, 0, 0).color(r, g, b, a).endVertex();
        buffer.vertex(1, 1, 0).color(r, g, b, a).endVertex();
        buffer.vertex(1, 1, cnt).color(r, g, b, a).endVertex();
        buffer.vertex(1, 0, cnt).color(r, g, b, a).endVertex();
    }

    public static void addSideFullTextureW(BufferBuilder buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.vertex(0, 0, cnt).color(r, g, b, a).endVertex();
        buffer.vertex(0, 1, cnt).color(r, g, b, a).endVertex();
        buffer.vertex(0, 1, 0).color(r, g, b, a).endVertex();
        buffer.vertex(0, 0, 0).color(r, g, b, a).endVertex();
    }

    public static void addSideFullTextureN(BufferBuilder buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.vertex(1, 1, 0).color(r, g, b, a).endVertex();
        buffer.vertex(1, 0, 0).color(r, g, b, a).endVertex();
        buffer.vertex(0, 0, 0).color(r, g, b, a).endVertex();
        buffer.vertex(0, 1, 0).color(r, g, b, a).endVertex();
    }

    public static void addSideFullTextureS(BufferBuilder buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.vertex(1, 0, cnt).color(r, g, b, a).endVertex();
        buffer.vertex(1, 1, cnt).color(r, g, b, a).endVertex();
        buffer.vertex(0, 1, cnt).color(r, g, b, a).endVertex();
        buffer.vertex(0, 0, cnt).color(r, g, b, a).endVertex();
    }




    public static void addSideD(BufferBuilder buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.vertex(l, l, l).color(r, g, b, a).endVertex();
        buffer.vertex(h, l, l).color(r, g, b, a).endVertex();
        buffer.vertex(h, l, h).color(r, g, b, a).endVertex();
        buffer.vertex(l, l, h).color(r, g, b, a).endVertex();
    }

    public static void addSideU(BufferBuilder buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.vertex(l, h, h).color(r, g, b, a).endVertex();
        buffer.vertex(h, h, h).color(r, g, b, a).endVertex();
        buffer.vertex(h, h, l).color(r, g, b, a).endVertex();
        buffer.vertex(l, h, l).color(r, g, b, a).endVertex();
    }

    public static void addSideE(BufferBuilder buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.vertex(h, l, l).color(r, g, b, a).endVertex();
        buffer.vertex(h, h, l).color(r, g, b, a).endVertex();
        buffer.vertex(h, h, h).color(r, g, b, a).endVertex();
        buffer.vertex(h, l, h).color(r, g, b, a).endVertex();
    }

    public static void addSideW(BufferBuilder buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.vertex(l, l, h).color(r, g, b, a).endVertex();
        buffer.vertex(l, h, h).color(r, g, b, a).endVertex();
        buffer.vertex(l, h, l).color(r, g, b, a).endVertex();
        buffer.vertex(l, l, l).color(r, g, b, a).endVertex();
    }

    public static void addSideN(BufferBuilder buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.vertex(h, h, l).color(r, g, b, a).endVertex();
        buffer.vertex(h, l, l).color(r, g, b, a).endVertex();
        buffer.vertex(l, l, l).color(r, g, b, a).endVertex();
        buffer.vertex(l, h, l).color(r, g, b, a).endVertex();
    }

    public static void addSideS(BufferBuilder buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.vertex(h, l, h).color(r, g, b, a).endVertex();
        buffer.vertex(h, h, h).color(r, g, b, a).endVertex();
        buffer.vertex(l, h, h).color(r, g, b, a).endVertex();
        buffer.vertex(l, l, h).color(r, g, b, a).endVertex();
    }





    public static void addSideE(BufferBuilder buffer, float r, float g, float b, float size, float height) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.vertex(h, 0, l).color(r, g, b, a).endVertex();
        buffer.vertex(h, height, l).color(r, g, b, a).endVertex();
        buffer.vertex(h, height, h).color(r, g, b, a).endVertex();
        buffer.vertex(h, 0, h).color(r, g, b, a).endVertex();
    }

    public static void addSideW(BufferBuilder buffer, float r, float g, float b, float size, float height) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.vertex(l, 0, h).color(r, g, b, a).endVertex();
        buffer.vertex(l, height, h).color(r, g, b, a).endVertex();
        buffer.vertex(l, height, l).color(r, g, b, a).endVertex();
        buffer.vertex(l, 0, l).color(r, g, b, a).endVertex();
    }

    public static void addSideN(BufferBuilder buffer, float r, float g, float b, float size, float height) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.vertex(h, height, l).color(r, g, b, a).endVertex();
        buffer.vertex(h, 0, l).color(r, g, b, a).endVertex();
        buffer.vertex(l, 0, l).color(r, g, b, a).endVertex();
        buffer.vertex(l, height, l).color(r, g, b, a).endVertex();
    }

    public static void addSideS(BufferBuilder buffer, float r, float g, float b, float size, float height) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.vertex(h, 0, h).color(r, g, b, a).endVertex();
        buffer.vertex(h, height, h).color(r, g, b, a).endVertex();
        buffer.vertex(l, height, h).color(r, g, b, a).endVertex();
        buffer.vertex(l, 0, h).color(r, g, b, a).endVertex();
    }

}
