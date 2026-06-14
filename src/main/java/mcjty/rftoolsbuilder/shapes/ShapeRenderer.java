package mcjty.rftoolsbuilder.shapes;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.varia.Check32;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.scanner.ScannerConfiguration;
import mcjty.rftoolsbuilder.modules.scanner.client.DummyBlockGetter;
import mcjty.rftoolsbuilder.modules.scanner.network.PacketRequestShapeData;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.*;

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
    private int refreshCounter = 0;

    private int waitForNewRequest = 0;
    private final ModelRenderCache modelRenderCache = new ModelRenderCache();


    public ShapeRenderer(ShapeID shapeID) {
        this.shapeID = shapeID;
    }

    public void setShapeID(ShapeID shapeID) {
        this.shapeID = shapeID;
    }

    public ShapeID getShapeID() {
        return shapeID;
    }

    public void setRefreshCounter(int refreshCounter) {
        this.refreshCounter = refreshCounter;
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
        data.markRequestProgress();
        data.setPlaneData(plane, offsetY, dy);
        if (offsetY >= dy - 1) {
            data.clearRequest();
        }
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

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();
        boolean doSound = renderBlockModels
                ? renderBlockModelsInWorld(poseStack, builder, stack, scan)
                : renderFacesInWorld(poseStack, builder, stack, scan, shape.isGrayscale());

        RenderSystem.disableBlend();
        poseStack.popPose();
        return doSound;
    }

    public void renderShape(GuiGraphics graphics, IShapeParentGui gui, ItemStack stack, int x, int y, boolean showAxis, boolean showOuter, boolean showScan, boolean showGuidelines) {
        PoseStack matrixStack = graphics.pose();
        setupScissor(gui);

        matrixStack.pushPose();

        matrixStack.translate(dx, dy, 200);
        RenderHelper.rotateXP(matrixStack, 180-xangle);
        RenderHelper.rotateYP(matrixStack, yangle);
        RenderHelper.rotateZP(matrixStack, zangle);
        matrixStack.scale(-scale, scale, scale);

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        renderFacesForGui(matrixStack, buffer, stack, showScan, false);
        BlockPos dimension = ShapeCardItem.getDimension(stack);
        renderHelpers(matrixStack, buffer, dimension.getX(), dimension.getY(), dimension.getZ(), showAxis, showOuter);

        RenderSystem.disableScissor();

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
            BufferUploader.drawWithShader(buffer.end());
        }

        RenderSystem.disableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
//        RenderHelper.turnBackOn();    // @todo 1.18

        RenderData data = ShapeDataManagerClient.getRenderData(shapeID);
        if (data != null && !data.previewMessage.isEmpty()) {
            graphics.drawString(Minecraft.getInstance().font, data.previewMessage, gui.getPreviewLeft()+84, gui.getPreviewTop()+50, 0xffff0000, false);
        }

    }

    private void renderHelpers(PoseStack poseStack, BufferBuilder buffer, int xlen, int ylen, int zlen, boolean showAxis, boolean showOuter) {
        if (!showAxis && !showOuter) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();

        // X, Y, Z axis
        if (showAxis) {
            ShapeRenderer.renderAxis(poseStack, buffer, Math.max(1, xlen/2), Math.max(1, ylen/2), Math.max(1, zlen/2));
        }

        if (showOuter) {
            ShapeRenderer.renderOuterBox(poseStack, buffer, xlen, ylen, zlen);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
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

    private static void add(Matrix4f matrix, BufferBuilder buffer, double x, double y, double z) {
        buffer.vertex(matrix, (float) (x + offset.x), (float) (y + offset.y), (float) (z + offset.z)).color(1f, 1f, 1f, 1f).endVertex();
    }

    private static void add(Matrix4f matrix, BufferBuilder buffer, double x, double y, double z, float r, float g, float b, float a) {
        buffer.vertex(matrix, (float) (x + offset.x), (float) (y + offset.y), (float) (z + offset.z)).color(r, g, b, a).endVertex();
    }

    static void renderOuterBox(PoseStack poseStack, BufferBuilder buffer, int xlen, int ylen, int zlen) {
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();
        double thickness = .08;
        int xleft = -xlen / 2;
        int xright = xlen / 2 + (xlen & 1);
        int ybot = -ylen / 2;
        int ytop = ylen / 2 + (ylen & 1);
        int zsouth = -zlen / 2;
        int znorth = zlen / 2 + (zlen & 1);

        addBox(matrix, buffer, xleft, ybot, zsouth, xright, ybot + thickness, zsouth + thickness, 1f, 1f, 1f, 1f);
        addBox(matrix, buffer, xleft, ytop - thickness, zsouth, xright, ytop, zsouth + thickness, 1f, 1f, 1f, 1f);
        addBox(matrix, buffer, xleft, ybot, znorth - thickness, xright, ybot + thickness, znorth, 1f, 1f, 1f, 1f);
        addBox(matrix, buffer, xleft, ytop - thickness, znorth - thickness, xright, ytop, znorth, 1f, 1f, 1f, 1f);

        addBox(matrix, buffer, xleft, ybot, zsouth, xleft + thickness, ytop, zsouth + thickness, 1f, 1f, 1f, 1f);
        addBox(matrix, buffer, xright - thickness, ybot, zsouth, xright, ytop, zsouth + thickness, 1f, 1f, 1f, 1f);
        addBox(matrix, buffer, xleft, ybot, znorth - thickness, xleft + thickness, ytop, znorth, 1f, 1f, 1f, 1f);
        addBox(matrix, buffer, xright - thickness, ybot, znorth - thickness, xright, ytop, znorth, 1f, 1f, 1f, 1f);

        addBox(matrix, buffer, xleft, ybot, zsouth, xleft + thickness, ybot + thickness, znorth, 1f, 1f, 1f, 1f);
        addBox(matrix, buffer, xright - thickness, ybot, zsouth, xright, ybot + thickness, znorth, 1f, 1f, 1f, 1f);
        addBox(matrix, buffer, xleft, ytop - thickness, zsouth, xleft + thickness, ytop, znorth, 1f, 1f, 1f, 1f);
        addBox(matrix, buffer, xright - thickness, ytop - thickness, zsouth, xright, ytop, znorth, 1f, 1f, 1f, 1f);

        BufferUploader.drawWithShader(buffer.end());
    }

    static void renderAxis(PoseStack poseStack, BufferBuilder buffer, int xlen, int ylen, int zlen) {
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();
        double thickness = .12;
        addBox(matrix, buffer, -xlen, -thickness, -thickness, xlen, thickness, thickness, 1f, 0f, 0f, 1f);
        addBox(matrix, buffer, -thickness, -ylen, -thickness, thickness, ylen, thickness, 0f, 1f, 0f, 1f);
        addBox(matrix, buffer, -thickness, -thickness, -zlen, thickness, thickness, zlen, 0f, 0f, 1f, 1f);
        BufferUploader.drawWithShader(buffer.end());
    }

    private static void addBox(Matrix4f matrix, BufferBuilder buffer,
                               double x1, double y1, double z1,
                               double x2, double y2, double z2,
                               float r, float g, float b, float a) {
        addQuad(matrix, buffer, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, r, g, b, a);
        addQuad(matrix, buffer, x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1, r, g, b, a);
        addQuad(matrix, buffer, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, r, g, b, a);
        addQuad(matrix, buffer, x1, y1, z2, x1, y2, z2, x1, y2, z1, x1, y1, z1, r, g, b, a);
        addQuad(matrix, buffer, x2, y2, z1, x2, y1, z1, x1, y1, z1, x1, y2, z1, r, g, b, a);
        addQuad(matrix, buffer, x2, y1, z2, x2, y2, z2, x1, y2, z2, x1, y1, z2, r, g, b, a);
    }

    private static void addQuad(Matrix4f matrix, BufferBuilder buffer,
                                double x1, double y1, double z1,
                                double x2, double y2, double z2,
                                double x3, double y3, double z3,
                                double x4, double y4, double z4,
                                float r, float g, float b, float a) {
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) x3, (float) y3, (float) z3).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) x4, (float) y4, (float) z4).color(r, g, b, a).endVertex();
    }

    private int calculateChecksum(ItemStack stack) {
        Check32 crc = new Check32();
        if (!stack.isEmpty()) {
            ShapeCardItem.getFormulaCheckClient(stack, crc);
        }
        crc.add(refreshCounter);
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

    private boolean renderFacesInWorld(PoseStack poseStack, final BufferBuilder buffer,
                                       ItemStack stack, boolean showScan, boolean grayscale) {

        RenderData data = requestRenderData(stack);

        boolean needScanSound = false;
        if (data.getPlanes() != null) {
            long time = System.currentTimeMillis();
            for (RenderData.RenderPlane plane : data.getPlanes()) {
                if (plane != null) {
                    renderPlaneImmediate(poseStack, buffer, plane, grayscale, false);
                    if (showScan) {
                        needScanSound |= renderFlashOverlay(poseStack, buffer, plane, grayscale, false, time);
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
                RenderData.RenderElement element = getBeaconElement(type, beacon.isDoBeacon());
                PoseStack beaconStack = new PoseStack();
                beaconStack.mulPoseMatrix(poseStack.last().pose());
                beaconStack.translate(x, y, z);
                element.render(beaconStack);
//                GlStateManager._translatef(-x, -y, -z);
            }
        }

        return needScanSound;
    }

    private boolean renderBlockModelsInWorld(PoseStack poseStack, BufferBuilder overlayBuffer, ItemStack stack, boolean showScan) {
        RenderData data = requestRenderData(stack);
        boolean needScanSound = false;
        if (data.getPlanes() == null) {
            return false;
        }

        Level level = Minecraft.getInstance().level;
        modelRenderCache.buildIfNeeded(data, level);
        modelRenderCache.render(poseStack);
        long time = System.currentTimeMillis();

        RenderData.RenderPlane[] planes = data.getPlanes();
        for (int i = 0; i < planes.length; i++) {
            RenderData.RenderPlane plane = planes[i];
            if (plane == null) {
                continue;
            }
            if (showScan && modelRenderCache.hasPlaneBuffers(i)) {
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                needScanSound |= renderFlashOverlay(poseStack, overlayBuffer, plane, false, false, time);
            }
        }
        return needScanSound;
    }

    private boolean renderFlashOverlay(PoseStack poseStack, BufferBuilder buffer, RenderData.RenderPlane plane, boolean grayscale, boolean gui, long time) {
        plane.markFlashRendered();
        if (!plane.isFlashing(time)) {
            return false;
        }
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_ONE, GL11.GL_ONE);
        renderPlaneImmediate(poseStack, buffer, plane, grayscale, gui);
        RenderSystem.disableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        return true;
    }

    private static class ModelRenderCache {
        private static final int PLANES_TO_BUILD_PER_FRAME = 1;

        private static class PlaneBuffers {
            private final Map<RenderType, VertexBuffer> layerBuffers = new HashMap<>();
            @Nullable
            private VertexBuffer fluidBuffer = null;

            private void close() {
                for (VertexBuffer buffer : layerBuffers.values()) {
                    buffer.close();
                }
                layerBuffers.clear();
                if (fluidBuffer != null) {
                    fluidBuffer.close();
                    fluidBuffer = null;
                }
            }
        }

        private final ChunkBufferBuilderPack fixedBuffers = new ChunkBufferBuilderPack();
        private final Set<RenderType> supportedLayers = new HashSet<>(RenderType.chunkBufferLayers());
        private final Map<Integer, PlaneBuffers> planeBuffers = new HashMap<>();
        private final Map<Integer, Long> knownBirthtimes = new HashMap<>();
        private final Map<Integer, Map<BlockPos, BlockState>> planeBlockMaps = new HashMap<>();
        private final Map<BlockPos, BlockState> blockMap = new HashMap<>();
        private final Queue<Integer> pendingBuilds = new ArrayDeque<>();
        private final Set<Integer> pendingBuildSet = new HashSet<>();
        private int planeCount = -1;

        private void buildIfNeeded(RenderData data, @Nullable Level level) {
            if (level == null) {
                return;
            }
            RenderData.RenderPlane[] planes = data.getPlanes();
            if (planes == null) {
                clearAll();
                return;
            }
            syncPlaneCount(planes.length);

            for (int i = 0; i < planes.length; i++) {
                RenderData.RenderPlane plane = planes[i];
                if (plane == null) {
                    removePlane(i);
                    continue;
                }
                long birthtime = plane.getBirthtime();
                Long known = knownBirthtimes.get(i);
                if (known == null || known != birthtime) {
                    knownBirthtimes.put(i, birthtime);
                    markPlaneDirty(i);
                    markPlaneDirty(i - 1);
                    markPlaneDirty(i + 1);
                }
            }

            int built = 0;
            while (built < PLANES_TO_BUILD_PER_FRAME && !pendingBuilds.isEmpty()) {
                Integer index = pendingBuilds.poll();
                if (index == null || !pendingBuildSet.remove(index)) {
                    continue;
                }
                if (index < 0 || index >= planes.length) {
                    continue;
                }
                RenderData.RenderPlane plane = planes[index];
                if (plane == null) {
                    removePlane(index);
                    continue;
                }
                rebuildPlaneBuffers(level, index, plane);
                built++;
            }
        }

        private void render(PoseStack poseStack) {
            if (planeBuffers.isEmpty()) {
                return;
            }
            List<Integer> indices = new ArrayList<>(planeBuffers.keySet());
            indices.sort(Integer::compareTo);

            for (RenderType renderType : RenderType.chunkBufferLayers()) {
                renderType.setupRenderState();
                ShaderInstance shader = RenderSystem.getShader();
                for (int i = 0; i < 12; i++) {
                    int texId = RenderSystem.getShaderTexture(i);
                    shader.setSampler("Sampler" + i, texId);
                }
                if (shader.MODEL_VIEW_MATRIX != null) {
                    shader.MODEL_VIEW_MATRIX.set(poseStack.last().pose());
                }
                if (shader.PROJECTION_MATRIX != null) {
                    shader.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
                }
                if (shader.COLOR_MODULATOR != null) {
                    shader.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
                }
                if (shader.GLINT_ALPHA != null) {
                    shader.GLINT_ALPHA.set(RenderSystem.getShaderGlintAlpha());
                }
                if (shader.FOG_START != null) {
                    shader.FOG_START.set(RenderSystem.getShaderFogStart());
                }
                if (shader.FOG_END != null) {
                    shader.FOG_END.set(RenderSystem.getShaderFogEnd());
                }
                if (shader.FOG_COLOR != null) {
                    shader.FOG_COLOR.set(RenderSystem.getShaderFogColor());
                }
                if (shader.FOG_SHAPE != null) {
                    shader.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
                }
                if (shader.TEXTURE_MATRIX != null) {
                    shader.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
                }
                if (shader.GAME_TIME != null) {
                    shader.GAME_TIME.set(RenderSystem.getShaderGameTime());
                }
                RenderSystem.setupShaderLights(shader);
                shader.apply();

                for (Integer index : indices) {
                    PlaneBuffers buffers = planeBuffers.get(index);
                    if (buffers == null) {
                        continue;
                    }
                    VertexBuffer buffer = buffers.layerBuffers.get(renderType);
                    if (buffer != null) {
                        buffer.bind();
                        buffer.draw();
                    }
                }
                renderType.clearRenderState();
                shader.clear();
            }

            boolean hasFluids = false;
            for (Integer index : indices) {
                PlaneBuffers buffers = planeBuffers.get(index);
                if (buffers != null && buffers.fluidBuffer != null) {
                    hasFluids = true;
                    break;
                }
            }
            if (hasFluids) {
                RenderType.translucent().setupRenderState();
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.disableCull();
                for (Integer index : indices) {
                    PlaneBuffers buffers = planeBuffers.get(index);
                    if (buffers != null && buffers.fluidBuffer != null) {
                        buffers.fluidBuffer.bind();
                        buffers.fluidBuffer.drawWithShader(poseStack.last().pose(), RenderSystem.getProjectionMatrix(), GameRenderer.getPositionColorShader());
                    }
                }
                RenderSystem.depthMask(true);
                RenderSystem.enableCull();
                RenderType.translucent().clearRenderState();
            }

            VertexBuffer.unbind();
        }

        private boolean hasPlaneBuffers(int index) {
            PlaneBuffers buffers = planeBuffers.get(index);
            return buffers != null && (!buffers.layerBuffers.isEmpty() || buffers.fluidBuffer != null);
        }

        private void syncPlaneCount(int count) {
            if (planeCount != count) {
                clearAll();
                planeCount = count;
            }
        }

        private void markPlaneDirty(int index) {
            if (index < 0 || index >= planeCount) {
                return;
            }
            if (pendingBuildSet.add(index)) {
                pendingBuilds.add(index);
            }
        }

        private void removePlane(int index) {
            knownBirthtimes.remove(index);
            pendingBuildSet.remove(index);

            Map<BlockPos, BlockState> oldPlaneMap = planeBlockMaps.remove(index);
            if (oldPlaneMap != null) {
                for (BlockPos pos : oldPlaneMap.keySet()) {
                    blockMap.remove(pos);
                }
            }

            PlaneBuffers oldBuffers = planeBuffers.remove(index);
            if (oldBuffers != null) {
                oldBuffers.close();
            }
        }

        private void clearAll() {
            for (PlaneBuffers buffers : planeBuffers.values()) {
                buffers.close();
            }
            planeBuffers.clear();
            knownBirthtimes.clear();
            planeBlockMaps.clear();
            blockMap.clear();
            pendingBuilds.clear();
            pendingBuildSet.clear();
            planeCount = -1;
        }

        private void rebuildPlaneBuffers(Level level, int index, RenderData.RenderPlane plane) {
            Map<BlockPos, BlockState> newPlaneMap = extractPlaneBlocks(plane);
            Map<BlockPos, BlockState> oldPlaneMap = planeBlockMaps.put(index, newPlaneMap);
            if (oldPlaneMap != null) {
                for (BlockPos pos : oldPlaneMap.keySet()) {
                    blockMap.remove(pos);
                }
            }
            blockMap.putAll(newPlaneMap);

            PlaneBuffers oldBuffers = planeBuffers.remove(index);
            if (oldBuffers != null) {
                oldBuffers.close();
            }

            PlaneBuffers buffersForPlane = new PlaneBuffers();
            DummyBlockGetter blockGetter = new DummyBlockGetter(level.registryAccess(), blockMap);
            BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
            RandomSource random = RandomSource.create(42L);
            PoseStack buildingPoseStack = new PoseStack();

            for (RenderType renderType : RenderType.chunkBufferLayers()) {
                fixedBuffers.builder(renderType).begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            }

            for (Map.Entry<BlockPos, BlockState> entry : newPlaneMap.entrySet()) {
                BlockPos pos = entry.getKey();
                BlockState state = entry.getValue();
                if (!state.getFluidState().isEmpty()) {
                    continue;
                }
                RenderType stateRenderType = ItemBlockRenderTypes.getChunkRenderType(state);
                if (!supportedLayers.contains(stateRenderType)) {
                    continue;
                }
                BufferBuilder builder = fixedBuffers.builder(stateRenderType);
                buildingPoseStack.pushPose();
                buildingPoseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                random.setSeed(state.getSeed(pos));
                blockRenderer.renderBatched(state, pos, blockGetter, buildingPoseStack, builder, false, random, ModelData.EMPTY, stateRenderType);
                buildingPoseStack.popPose();
            }

            for (RenderType renderType : RenderType.chunkBufferLayers()) {
                BufferBuilder builder = fixedBuffers.builder(renderType);
                BufferBuilder.RenderedBuffer renderedBuffer = builder.endOrDiscardIfEmpty();
                if (renderedBuffer != null) {
                    VertexBuffer layerBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
                    layerBuffer.bind();
                    layerBuffer.upload(renderedBuffer);
                    VertexBuffer.unbind();
                    buffersForPlane.layerBuffers.put(renderType, layerBuffer);
                }
            }

            buildFluidBuffer(buffersForPlane, newPlaneMap);
            planeBuffers.put(index, buffersForPlane);
        }

        private static Map<BlockPos, BlockState> extractPlaneBlocks(RenderData.RenderPlane plane) {
            Map<BlockPos, BlockState> states = new HashMap<>();
            int y = plane.getY();
            for (RenderData.RenderStrip strip : plane.getStrips()) {
                int z = plane.getStartz();
                int x = strip.getX();
                for (Pair<Integer, BlockState> pair : strip.getData()) {
                    int cnt = pair.getKey();
                    BlockState state = pair.getValue();
                    if (state != null) {
                        for (int c = 0; c < cnt; c++) {
                            states.put(new BlockPos(x, y, z + c), state);
                        }
                    }
                    z += cnt;
                }
            }
            return states;
        }

        private void buildFluidBuffer(PlaneBuffers buffersForPlane, Map<BlockPos, BlockState> planeMap) {
            BufferBuilder builder = new BufferBuilder(262144);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            boolean hasFluids = false;

            for (Map.Entry<BlockPos, BlockState> entry : planeMap.entrySet()) {
                BlockPos pos = entry.getKey();
                BlockState state = entry.getValue();
                if (state.getFluidState().isEmpty()) {
                    continue;
                }
                hasFluids = true;
                addFluidBlock(builder, blockMap, pos, state);
            }

            BufferBuilder.RenderedBuffer renderedBuffer = builder.endOrDiscardIfEmpty();
            if (hasFluids && renderedBuffer != null) {
                VertexBuffer fluidBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
                fluidBuffer.bind();
                fluidBuffer.upload(renderedBuffer);
                VertexBuffer.unbind();
                buffersForPlane.fluidBuffer = fluidBuffer;
            }
        }

        private void addFluidBlock(BufferBuilder builder, Map<BlockPos, BlockState> blockMap, BlockPos pos, BlockState state) {
            float height = state.getFluidState().isSource() ? 1.0f : 0.875f;
            float r = 0.25f;
            float g = 0.45f;
            float b = 1.0f;
            float a = 0.7f;

            if (isFluidFaceVisible(blockMap, pos, net.minecraft.core.Direction.UP)) {
                builder.vertex(pos.getX(), pos.getY() + height, pos.getZ() + 1).color(r, g, b, a).endVertex();
                builder.vertex(pos.getX() + 1, pos.getY() + height, pos.getZ() + 1).color(r, g, b, a).endVertex();
                builder.vertex(pos.getX() + 1, pos.getY() + height, pos.getZ()).color(r, g, b, a).endVertex();
                builder.vertex(pos.getX(), pos.getY() + height, pos.getZ()).color(r, g, b, a).endVertex();
            }
            if (isFluidFaceVisible(blockMap, pos, net.minecraft.core.Direction.DOWN)) {
                builder.vertex(pos.getX(), pos.getY(), pos.getZ()).color(r, g, b, a).endVertex();
                builder.vertex(pos.getX() + 1, pos.getY(), pos.getZ()).color(r, g, b, a).endVertex();
                builder.vertex(pos.getX() + 1, pos.getY(), pos.getZ() + 1).color(r, g, b, a).endVertex();
                builder.vertex(pos.getX(), pos.getY(), pos.getZ() + 1).color(r, g, b, a).endVertex();
            }
            if (isFluidFaceVisible(blockMap, pos, net.minecraft.core.Direction.NORTH)) {
                builder.vertex(pos.getX() + 1, pos.getY() + height, pos.getZ()).color(r, g, b, a).endVertex();
                builder.vertex(pos.getX() + 1, pos.getY(), pos.getZ()).color(r, g, b, a).endVertex();
                builder.vertex(pos.getX(), pos.getY(), pos.getZ()).color(r, g, b, a).endVertex();
                builder.vertex(pos.getX(), pos.getY() + height, pos.getZ()).color(r, g, b, a).endVertex();
            }
            if (isFluidFaceVisible(blockMap, pos, net.minecraft.core.Direction.SOUTH)) {
                builder.vertex(pos.getX() + 1, pos.getY(), pos.getZ() + 1).color(r, g, b, a).endVertex();
                builder.vertex(pos.getX() + 1, pos.getY() + height, pos.getZ() + 1).color(r, g, b, a).endVertex();
                builder.vertex(pos.getX(), pos.getY() + height, pos.getZ() + 1).color(r, g, b, a).endVertex();
                builder.vertex(pos.getX(), pos.getY(), pos.getZ() + 1).color(r, g, b, a).endVertex();
            }
            if (isFluidFaceVisible(blockMap, pos, net.minecraft.core.Direction.WEST)) {
                builder.vertex(pos.getX(), pos.getY(), pos.getZ() + 1).color(r, g, b, a).endVertex();
                builder.vertex(pos.getX(), pos.getY() + height, pos.getZ() + 1).color(r, g, b, a).endVertex();
                builder.vertex(pos.getX(), pos.getY() + height, pos.getZ()).color(r, g, b, a).endVertex();
                builder.vertex(pos.getX(), pos.getY(), pos.getZ()).color(r, g, b, a).endVertex();
            }
            if (isFluidFaceVisible(blockMap, pos, net.minecraft.core.Direction.EAST)) {
                builder.vertex(pos.getX() + 1, pos.getY(), pos.getZ()).color(r, g, b, a).endVertex();
                builder.vertex(pos.getX() + 1, pos.getY() + height, pos.getZ()).color(r, g, b, a).endVertex();
                builder.vertex(pos.getX() + 1, pos.getY() + height, pos.getZ() + 1).color(r, g, b, a).endVertex();
                builder.vertex(pos.getX() + 1, pos.getY(), pos.getZ() + 1).color(r, g, b, a).endVertex();
            }
        }

        private boolean isFluidFaceVisible(Map<BlockPos, BlockState> blockMap, BlockPos pos, net.minecraft.core.Direction direction) {
            BlockState neighbor = blockMap.get(pos.relative(direction));
            if (neighbor == null) {
                return true;
            }
            if (!neighbor.getFluidState().isEmpty()) {
                return false;
            }
            return !neighbor.canOcclude();
        }
    }

    private boolean renderFacesForGui(PoseStack poseStack, final BufferBuilder buffer,
                                      ItemStack stack, boolean showScan, boolean grayscale) {

        RenderData data = requestRenderData(stack);

        boolean needScanSound = false;
        if (data.getPlanes() != null) {
            long time = System.currentTimeMillis();
            for (RenderData.RenderPlane plane : data.getPlanes()) {
                if (plane != null) {
                    renderPlaneImmediate(poseStack, buffer, plane, grayscale, true);
                    if (showScan) {
                        needScanSound |= renderFlashOverlay(poseStack, buffer, plane, grayscale, true, time);
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
                RenderData.RenderElement element = getBeaconElement(type, beacon.isDoBeacon());
                PoseStack beaconStack = new PoseStack();
                beaconStack.mulPoseMatrix(poseStack.last().pose());
                beaconStack.translate(x, y, z);
                element.render(beaconStack);
//                RenderSystem.translatef(-x, -y, -z);
            }
        }

        return needScanSound;
    }

    private void renderPlaneImmediate(PoseStack poseStack, BufferBuilder buffer, RenderData.RenderPlane plane, boolean grayscale, boolean gui) {
        Matrix4f matrix = poseStack.last().pose();
        Map<BlockState, ShapeBlockInfo> palette = new HashMap<>();

        int y = plane.getY();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

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
        BufferUploader.drawWithShader(buffer.end());
    }

    private static RenderData.RenderElement beaconElement[] = null;
    private static RenderData.RenderElement beaconElementBeacon[] = null;

    private static RenderData.RenderElement getBeaconElement(BeaconType type, boolean doBeacon) {
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
            RenderData.vboBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            float r = type.getR();
            float g = type.getG();
            float b = type.getB();

            Vec3 origOffset = setOffset(0, -.7f, 0);
            addSideN(RenderData.vboBuffer, r, g, b, .3f);
            addSideS(RenderData.vboBuffer, r, g, b, .3f);
            addSideW(RenderData.vboBuffer, r, g, b, .3f);
            addSideE(RenderData.vboBuffer, r, g, b, .3f);
            addSideU(RenderData.vboBuffer, r, g, b, .3f);
            addSideD(RenderData.vboBuffer, r, g, b, .3f);
            restoreOffset(origOffset);
            origOffset = setOffset(0, -.2f, 0);
            addSideN(RenderData.vboBuffer, r, g, b, .2f);
            addSideS(RenderData.vboBuffer, r, g, b, .2f);
            addSideW(RenderData.vboBuffer, r, g, b, .2f);
            addSideE(RenderData.vboBuffer, r, g, b, .2f);
            addSideU(RenderData.vboBuffer, r, g, b, .2f);
            addSideD(RenderData.vboBuffer, r, g, b, .2f);
            restoreOffset(origOffset);

            if (doBeacon) {
                origOffset = setOffset(0, .2f, 0);
                addSideN(RenderData.vboBuffer, r, g, b, .1f, ScannerConfiguration.locatorBeaconHeight.get());
                addSideS(RenderData.vboBuffer, r, g, b, .1f, ScannerConfiguration.locatorBeaconHeight.get());
                addSideW(RenderData.vboBuffer, r, g, b, .1f, ScannerConfiguration.locatorBeaconHeight.get());
                addSideE(RenderData.vboBuffer, r, g, b, .1f, ScannerConfiguration.locatorBeaconHeight.get());
                restoreOffset(origOffset);
            }
            elements[type.ordinal()].performRenderToList();
        }
        return elements[type.ordinal()];
    }

    private static int scissorX;
    private static int scissorY;
    private static int scissorW;
    private static int scissorH;

    private static void setupScissor(IShapeParentGui gui) {
        Minecraft mc = Minecraft.getInstance();

        int xScale = mc.getWindow().getGuiScaledWidth();
        int yScale = mc.getWindow().getGuiScaledHeight();
        scissorX = (gui.getPreviewLeft() + 84) * mc.getWindow().getScreenWidth() / xScale;
        scissorY = (mc.getWindow().getScreenHeight()) - (gui.getPreviewTop() + 136) * mc.getWindow().getScreenHeight() / yScale;
        scissorW = 161 * mc.getWindow().getScreenWidth() / xScale;
        scissorH = 130 * mc.getWindow().getScreenHeight() / yScale;
    }

    public static void addSideFullTextureD(Matrix4f matrix, BufferBuilder buffer, int cnt, float r, float g, float b, boolean gui) {
        float a = gui ? 0.9f : 0.5f;
        buffer.vertex(matrix, (float) offset.x, (float) offset.y, (float) offset.z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) (1 + offset.x), (float) offset.y, (float) offset.z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) (1 + offset.x), (float) offset.y, (float) (cnt + offset.z)).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) offset.x, (float) offset.y, (float) (cnt + offset.z)).color(r, g, b, a).endVertex();
    }

    public static void addSideFullTextureU(Matrix4f matrix, BufferBuilder buffer, int cnt, float r, float g, float b, boolean gui) {
        float a = gui ? 0.9f : 0.5f;
        buffer.vertex(matrix, (float) offset.x, (float) (1 + offset.y), (float) (cnt + offset.z)).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) (1 + offset.x), (float) (1 + offset.y), (float) (cnt + offset.z)).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) (1 + offset.x), (float) (1 + offset.y), (float) offset.z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) offset.x, (float) (1 + offset.y), (float) offset.z).color(r, g, b, a).endVertex();
    }

    public static void addSideFullTextureE(Matrix4f matrix, BufferBuilder buffer, int cnt, float r, float g, float b, boolean gui) {
        float a = gui ? 0.9f : 0.5f;
        buffer.vertex(matrix, (float) (1 + offset.x), (float) offset.y, (float) offset.z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) (1 + offset.x), (float) (1 + offset.y), (float) offset.z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) (1 + offset.x), (float) (1 + offset.y), (float) (cnt + offset.z)).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) (1 + offset.x), (float) offset.y, (float) (cnt + offset.z)).color(r, g, b, a).endVertex();
    }

    public static void addSideFullTextureW(Matrix4f matrix, BufferBuilder buffer, int cnt, float r, float g, float b, boolean gui) {
        float a = gui ? 0.9f : 0.5f;
        buffer.vertex(matrix, (float) offset.x, (float) offset.y, (float) (cnt + offset.z)).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) offset.x, (float) (1 + offset.y), (float) (cnt + offset.z)).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) offset.x, (float) (1 + offset.y), (float) offset.z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) offset.x, (float) offset.y, (float) offset.z).color(r, g, b, a).endVertex();
    }

    public static void addSideFullTextureN(Matrix4f matrix, BufferBuilder buffer, int cnt, float r, float g, float b, boolean gui) {
        float a = gui ? 0.9f : 0.5f;
        buffer.vertex(matrix, (float) (1 + offset.x), (float) (1 + offset.y), (float) offset.z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) (1 + offset.x), (float) offset.y, (float) offset.z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) offset.x, (float) offset.y, (float) offset.z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) offset.x, (float) (1 + offset.y), (float) offset.z).color(r, g, b, a).endVertex();
    }

    public static void addSideFullTextureS(Matrix4f matrix, BufferBuilder buffer, int cnt, float r, float g, float b, boolean gui) {
        float a = gui ? 0.9f : 0.5f;
        buffer.vertex(matrix, (float) (1 + offset.x), (float) offset.y, (float) (cnt + offset.z)).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) (1 + offset.x), (float) (1 + offset.y), (float) (cnt + offset.z)).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) offset.x, (float) (1 + offset.y), (float) (cnt + offset.z)).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) offset.x, (float) offset.y, (float) (cnt + offset.z)).color(r, g, b, a).endVertex();
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
