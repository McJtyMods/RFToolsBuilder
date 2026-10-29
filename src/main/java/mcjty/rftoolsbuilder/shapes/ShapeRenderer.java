package mcjty.rftoolsbuilder.shapes;

import mcjty.rftoolsbuilder.shapes.ScanModels;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import mcjty.lib.client.CustomRenderTypes;
import mcjty.lib.varia.Check32;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.scanner.client.DummyBlockGetter;
import mcjty.rftoolsbuilder.modules.scanner.network.PacketRequestShapeData;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Shape geometry is captured before submission; render callbacks never access live block entities. */
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
        int sx = (dx + 84) * mainWindow.getWidth() / xScale;
        int sy = (mainWindow.getHeight()) - (dy + 136) * mainWindow.getHeight() / yScale;
        int sw = 161 * mainWindow.getWidth() / xScale;
        int sh = 130 * mainWindow.getHeight() / yScale;
        int vx = sx + sw/2;
        int vy = sy + sh/2;

        this.dx = (float) (vx/mainWindow.getGuiScale());
        this.dy = (float) (vy/mainWindow.getGuiScale());
    }

    public void handleShapeDragging(int x, int y, boolean[] buttons) {
        MouseHandler mouse = Minecraft.getInstance().mouseHandler;
        if (x >= 100 && y <= 120) {
            if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isShiftKeyDown()) {
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

    public static int mouseButtonIndex(int button) {
        return switch (button) {
            case InputConstants.MOUSE_BUTTON_LEFT -> 0;
            case InputConstants.MOUSE_BUTTON_RIGHT -> 1;
            case InputConstants.MOUSE_BUTTON_MIDDLE -> 2;
            default -> -1;
        };
    }

    public record Vertex(float x, float y, float z, int color) { }

    public record Quad(List<Vertex> vertices) { public Quad { vertices = List.copyOf(vertices); } }
    public record Frame(List<ScanMesh> meshes, boolean scanning) {
        public static final Frame EMPTY = new Frame(List.of(), false);

        public Frame { meshes = List.copyOf(meshes); }
    }

    // Strip arrays are replaced on every update, unlike millisecond timestamps which can collide.
    private record PlaneSource(RenderData.RenderStrip[] strips, int y, int startz) {
        PlaneSource(RenderData.RenderPlane plane) { this(plane.getStrips(), plane.getY(), plane.getStartz()); }
    }

    private record CachedPlane(PlaneSource source, boolean grayscale, boolean gui, List<ScanMesh> meshes) { }

    private final Map<Integer, CachedPlane> planeCache = new HashMap<>();
    private final Map<Integer, List<ScanMesh>> modelCache = new HashMap<>();
    private final Map<Integer, PlaneSource> modelSources = new HashMap<>();
    private final Long2ObjectMap<BlockState> modelBlocks = new Long2ObjectOpenHashMap<>();
    private Object cachedModelSet;
    private DummyBlockGetter modelWorld;

    public Frame capture(ItemStack stack, boolean scan, boolean blockModels) {
        return capture(stack, scan, blockModels, false);
    }

    private Frame capture(ItemStack stack, boolean scan, boolean blockModels, boolean gui) {
        RenderData data = requestRenderData(stack);
        data.touch();
        var planes = data.getPlanes();
        if (planes == null) {
            planeCache.clear();
            modelCache.clear();
            modelSources.clear();
            modelBlocks.clear();
            return Frame.EMPTY;
        }
        var mc = Minecraft.getInstance();
        Object currentModels = mc.getModelManager().getBlockStateModelSet();
        if (cachedModelSet != currentModels) {
            cachedModelSet = currentModels;
            planeCache.clear();
            modelCache.clear();
            modelSources.clear();
            modelBlocks.clear();
            modelWorld = null;
        }
        List<ScanMesh> meshes = new ArrayList<>();
        boolean scanning = false;
        planeCache.keySet().removeIf(index -> index >= planes.length || planes[index] == null);
        for (int index = 0; index < planes.length; index++) {
            var plane = planes[index];
            if (plane == null) continue;
            if (scan) plane.markFlashRendered();
            boolean flash = scan && plane.isFlashing(System.currentTimeMillis());
            scanning |= flash;
            if (!blockModels || flash) {
                var source = new PlaneSource(plane);
                var cached = planeCache.get(index);
                if (cached == null || !cached.source().equals(source) || cached.grayscale() != shapeID.isGrayscale()
                        || cached.gui() != gui) {
                    cached = new CachedPlane(source, shapeID.isGrayscale(), gui, buildPlane(plane, shapeID.isGrayscale(), gui));
                    planeCache.put(index, cached);
                }
                meshes.addAll(cached.meshes());
            }
        }
        if (blockModels && mc.level != null) {
            if (syncModelSources(planes)) modelWorld = null;
            if (modelWorld == null) {
                // Only extraction accesses this view. Submitted meshes contain no live world references.
                modelWorld = new DummyBlockGetter(mc.level.registryAccess(), Map.of()) {
                    @Override
                    public BlockState getBlockState(BlockPos pos) {
                        return modelBlocks.getOrDefault(pos.asLong(), Blocks.AIR.defaultBlockState());
                    }
                };
            }
            // Build one changed plane per frame; arrivals invalidate only that plane and its neighbours.
            for (int i = 0; i < planes.length; i++) {
                if (planes[i] != null && !modelCache.containsKey(i)) {
                    modelCache.put(i, buildModels(planes[i]));
                    break;
                }
            }
            for (int i = 0; i < planes.length; i++) {
                var cached = modelCache.get(i);
                if (cached != null) meshes.addAll(cached);
            }
        }
        int scanId = ShapeCardItem.getScanIdRecursive(stack);
        if (scanId > 0) {
            if (--extraDataCounter <= 0) {
                extraDataCounter = 10;
                ScanDataManagerClient.getScansClient().requestExtraDataClient(scanId);
            }
            for (var beacon : ScanDataManagerClient.getScansClient().getExtraDataClient(scanId).getBeacons()) {
                var type = beacon.getType();
                var pos = beacon.getPos();
                try (var builder = new ScanMesh.Builder(gui ? ScanPreviewRenderer.RENDER_TYPE : CustomRenderTypes.QUADS_NOTEXTURE, gui)) {
                    var vertices = translated(builder, pos.getX(), pos.getY() + 1, pos.getZ());
                    float height = beacon.isDoBeacon() ? 10 : 1;
                    addSideN(vertices, type.getR(), type.getG(), type.getB(), .2f, height);
                    addSideS(vertices, type.getR(), type.getG(), type.getB(), .2f, height);
                    addSideE(vertices, type.getR(), type.getG(), type.getB(), .2f, height);
                    addSideW(vertices, type.getR(), type.getG(), type.getB(), .2f, height);
                    meshes.addAll(builder.build());
                }
            }
        }
        return new Frame(meshes, scanning);
    }

    private boolean syncModelSources(RenderData.RenderPlane[] planes) {
        boolean changed = false;
        Set<Integer> indices = new HashSet<>(modelSources.keySet());
        for (int i = 0; i < planes.length; i++) indices.add(i);
        for (int index : indices) {
            var source = index < planes.length && planes[index] != null ? new PlaneSource(planes[index]) : null;
            var previous = modelSources.get(index);
            if (Objects.equals(source, previous)) continue;
            changed = true;
            if (previous != null) visitBlocks(previous, (pos, state) -> modelBlocks.remove(pos.asLong()));
            if (source == null) modelSources.remove(index);
            else {
                modelSources.put(index, source);
                visitBlocks(source, (pos, state) -> modelBlocks.put(pos.asLong(), state));
            }
            modelCache.remove(index - 1);
            modelCache.remove(index);
            modelCache.remove(index + 1);
        }
        return changed;
    }

    private static void visitBlocks(PlaneSource plane, BiConsumer<BlockPos, BlockState> action) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (var strip : plane.strips()) {
            int z = plane.startz();
            for (var run : strip.getData()) {
                if (run.getValue() != null && !run.getValue().isAir()) {
                    for (int n = 0; n < run.getKey(); n++) action.accept(pos.set(strip.getX(), plane.y(), z + n), run.getValue());
                }
                z += run.getKey();
            }
        }
    }

    private List<ScanMesh> buildModels(RenderData.RenderPlane plane) {
        var mc = Minecraft.getInstance();
        var random = RandomSource.create(42);
        var pose = new PoseStack();
        var instance = new QuadInstance();
        var parts = new ArrayList<BlockStateModelPart>();
        var directions = Direction.values();
        var builders = new LinkedHashMap<RenderType, ScanMesh.Builder>();
        var fluids = new FluidRenderer(mc.getModelManager().getFluidStateModelSet());
        try {
            visitBlocks(new PlaneSource(plane), (pos, state) -> {
                ScanFluidRenderer.render(fluids, modelWorld, pos, state, builders);
                parts.clear();
                random.setSeed(state.getSeed(pos));
                ScanModels.collectParts(modelWorld, pos, state, random, parts);
                int[] tints = mc.getBlockColors().getTintSources(state).stream()
                        .mapToInt(tint -> tint.colorInWorld(state, modelWorld, pos)).toArray();
                pose.pushPose();
                pose.translate(pos.getX(), pos.getY(), pos.getZ());
                for (var part : parts) {
                    for (var direction : directions) {
                        if (Block.shouldRenderFace(state,
                                modelWorld.getBlockState(pos.relative(direction)), direction)) {
                            writeModelQuads(part.getQuads(direction), tints, pose, instance, builders);
                        }
                    }
                    writeModelQuads(part.getQuads(null), tints, pose, instance, builders);
                }
                pose.popPose();
            });
            List<ScanMesh> meshes = new ArrayList<>();
            for (var builder : builders.values()) meshes.addAll(builder.build());
            return List.copyOf(meshes);
        } finally {
            builders.values().forEach(ScanMesh.Builder::close);
        }
    }

    private static void writeModelQuads(List<BakedQuad> quads,
            int[] tints, PoseStack pose, QuadInstance instance,
            Map<RenderType, ScanMesh.Builder> builders) {
        for (var quad : quads) {
            int tint = quad.materialInfo().tintIndex();
            instance.setColor(tint >= 0 && tint < tints.length ? tints[tint] : -1);
            var type = switch (quad.materialInfo().layer()) {
                case SOLID -> RenderTypes.solidMovingBlock();
                case CUTOUT -> RenderTypes.cutoutMovingBlock();
                case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
            };
            builders.computeIfAbsent(type, ScanMesh.Builder::new).putBakedQuad(pose.last(), quad, instance);
        }
    }

    public static void submit(Frame frame, PoseStack pose, SubmitNodeCollector collector) {
        ScanMeshRenderer.submit(frame.meshes(), pose, collector, false);
    }

    public void renderShape(GuiGraphicsExtractor graphics, IShapeParentGui gui, ItemStack stack, int x, int y,
            boolean showAxis, boolean showOuter, boolean showScan, boolean showGuidelines) {
        Frame frame = capture(stack, showScan, false, true);
        Matrix4f transform = new Matrix4f().translate(dx, dy, 0)
                .rotateX((float) Math.toRadians(180 - xangle)).rotateY((float) Math.toRadians(yangle))
                .rotateZ((float) Math.toRadians(zangle)).scale(-scale, scale, scale);
        int left = gui.getPreviewLeft() + 84, top = gui.getPreviewTop() + 6;
        graphics.enableScissor(left, top, left + 161, top + 130);
        graphics.guiRenderState.addPicturesInPictureState(new ScanPreviewRenderer.State(frame.meshes(), transform,
                left, top, left + 161, top + 130, new Matrix3x2f(graphics.pose()), graphics.scissorStack.peek()));
        BlockPos dim = ShapeCardItem.getDimension(stack);
        if (showAxis) {
            line(graphics, transform, 0, 0, 0, Math.max(1, dim.getX() / 2f), 0, 0, 0xffff0000);
            line(graphics, transform, 0, 0, 0, 0, Math.max(1, dim.getY() / 2f), 0, 0xff00cc00);
            line(graphics, transform, 0, 0, 0, 0, 0, Math.max(1, dim.getZ() / 2f), 0xff0000ff);
        }
        if (showOuter) {
            float hx = dim.getX() / 2f, hy = dim.getY() / 2f, hz = dim.getZ() / 2f;
            for (int a : new int[]{-1, 1}) for (int b : new int[]{-1, 1}) {
                line(graphics, transform, -hx, a * hy, b * hz, hx, a * hy, b * hz, 0xffaaaaaa);
                line(graphics, transform, a * hx, -hy, b * hz, a * hx, hy, b * hz, 0xffaaaaaa);
                line(graphics, transform, a * hx, b * hy, -hz, a * hx, b * hy, hz, 0xffaaaaaa);
            }
        }
        graphics.disableScissor();
        if (showGuidelines) {
            graphics.fill(x - 62, y + 180, x - 39, y + 182, 0xffff0000);
            graphics.fill(x - 62, y + 195, x - 39, y + 197, 0xff00cc00);
            graphics.fill(x - 62, y + 210, x - 39, y + 212, 0xff0000ff);
        }
        RenderData data = ShapeDataManagerClient.getRenderData(shapeID);
        if (data != null && !data.previewMessage.isEmpty()) {
            graphics.text(Minecraft.getInstance().font, data.previewMessage, left, top + 44, 0xffff0000, false);
        }
    }

    private record Preview(List<Quad> quads, Matrix3x2f pose, ScreenRectangle scissorArea, ScreenRectangle bounds) implements GuiElementRenderState {
        @Override
        public RenderPipeline pipeline() { return RenderPipelines.GUI; }

        @Override
        public TextureSetup textureSetup() { return TextureSetup.noTexture(); }

        @Override
        public void buildVertices(VertexConsumer consumer) {
            for (Quad quad : quads) for (Vertex vertex : quad.vertices()) {
                consumer.addVertexWith2DPose(pose, vertex.x(), vertex.y()).setColor(vertex.color());
            }
        }
    }

    private static void line(GuiGraphicsExtractor graphics, Matrix4f matrix, float x1, float y1, float z1,
            float x2, float y2, float z2, int color) {
        var a = matrix.transformPosition(new Vector3f(x1, y1, z1));
        var b = matrix.transformPosition(new Vector3f(x2, y2, z2));
        float length = Math.max(1, (float) Math.hypot(b.x - a.x, b.y - a.y));
        float nx = (b.y - a.y) / length * .5f, ny = (a.x - b.x) / length * .5f;
        var quad = new Quad(List.of(new Vertex(a.x + nx, a.y + ny, 0, color), new Vertex(b.x + nx, b.y + ny, 0, color),
                new Vertex(b.x - nx, b.y - ny, 0, color), new Vertex(a.x - nx, a.y - ny, 0, color)));
        var bounds = graphics.scissorStack.peek();
        graphics.guiRenderState.addGuiElement(new Preview(List.of(quad), new Matrix3x2f(graphics.pose()), bounds, bounds));
    }

    private static Vec3 offset = Vec3.ZERO;

    private static Vec3 setOffset(double x, double y, double z) { Vec3 previous = offset; offset = new Vec3(x, y, z); return previous; }

    private static void restoreOffset(Vec3 previous) { offset = previous; }

    private static void add(VertexConsumer buffer, double x, double y, double z, float r, float g, float b, float a) {
        buffer.addVertex((float) (x + offset.x), (float) (y + offset.y), (float) (z + offset.z)).setColor(r, g, b, a);
    }

    private List<ScanMesh> buildPlane(RenderData.RenderPlane plane, boolean grayscale, boolean opaque) {
        Matrix4f matrix = new Matrix4f();
        boolean gui = false;
        Map<BlockState, ShapeBlockInfo> palette = new HashMap<>();

        int y = plane.getY();
        try (var buffer = new ScanMesh.Builder(opaque ? ScanPreviewRenderer.RENDER_TYPE : CustomRenderTypes.QUADS_NOTEXTURE, opaque)) {
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
                            var translated = translated(buffer, x, y, z);
                            for (int c = 0; c < cnt; c++) {
                                bd.render(translated, c, r, g, b);
                            }
                        }
                        restoreOffset(origOffset);
                    }
                    z += cnt;
                }
            }
            return buffer.build();
        }
    }

    static VertexConsumer translated(VertexConsumer buffer, float x, float y, float z) {
        return new VertexConsumer() {
            @Override
            public VertexConsumer addVertex(float vx, float vy, float vz) {
                buffer.addVertex(vx + x, vy + y, vz + z);
                return this;
            }

            @Override
            public VertexConsumer setColor(int r, int g, int b, int a) { buffer.setColor(r, g, b, a); return this; }

            @Override
            public VertexConsumer setColor(int color) { buffer.setColor(color); return this; }

            @Override
            public VertexConsumer setUv(float u, float v) { buffer.setUv(u, v); return this; }

            @Override
            public VertexConsumer setUv1(int u, int v) { buffer.setUv1(u, v); return this; }

            @Override
            public VertexConsumer setUv2(int u, int v) { buffer.setUv2(u, v); return this; }

            @Override
            public VertexConsumer setNormal(float nx, float ny, float nz) { buffer.setNormal(nx, ny, nz); return this; }

            @Override
            public VertexConsumer setLineWidth(float width) { buffer.setLineWidth(width); return this; }
        };
    }

    public static void addSideFullTextureD(VertexConsumer buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.addVertex(0, 0, 0).setColor(r, g, b, a);
        buffer.addVertex(1, 0, 0).setColor(r, g, b, a);
        buffer.addVertex(1, 0, cnt).setColor(r, g, b, a);
        buffer.addVertex(0, 0, cnt).setColor(r, g, b, a);
    }

    public static void addSideFullTextureD(Matrix4f matrix, VertexConsumer buffer, int cnt, float r, float g, float b, boolean gui) {
        float a = gui ? 0.9f : 0.5f;
        buffer.addVertex(matrix, (float) offset.x, (float) offset.y, (float) offset.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) offset.y, (float) offset.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) offset.y, (float) (cnt + offset.z)).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) offset.x, (float) offset.y, (float) (cnt + offset.z)).setColor(r, g, b, a);
    }

    public static void addSideFullTextureU(VertexConsumer buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.addVertex(0, 1, cnt).setColor(r, g, b, a);
        buffer.addVertex(1, 1, cnt).setColor(r, g, b, a);
        buffer.addVertex(1, 1, 0).setColor(r, g, b, a);
        buffer.addVertex(0, 1, 0).setColor(r, g, b, a);
    }

    public static void addSideFullTextureU(Matrix4f matrix, VertexConsumer buffer, int cnt, float r, float g, float b, boolean gui) {
        float a = gui ? 0.9f : 0.5f;
        buffer.addVertex(matrix, (float) offset.x, (float) (1 + offset.y), (float) (cnt + offset.z)).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) (1 + offset.y), (float) (cnt + offset.z)).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) (1 + offset.y), (float) offset.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) offset.x, (float) (1 + offset.y), (float) offset.z).setColor(r, g, b, a);
    }

    public static void addSideFullTextureE(VertexConsumer buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.addVertex(1, 0, 0).setColor(r, g, b, a);
        buffer.addVertex(1, 1, 0).setColor(r, g, b, a);
        buffer.addVertex(1, 1, cnt).setColor(r, g, b, a);
        buffer.addVertex(1, 0, cnt).setColor(r, g, b, a);
    }

    public static void addSideFullTextureE(Matrix4f matrix, VertexConsumer buffer, int cnt, float r, float g, float b, boolean gui) {
        float a = gui ? 0.9f : 0.5f;
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) offset.y, (float) offset.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) (1 + offset.y), (float) offset.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) (1 + offset.y), (float) (cnt + offset.z)).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) offset.y, (float) (cnt + offset.z)).setColor(r, g, b, a);
    }

    public static void addSideFullTextureW(VertexConsumer buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.addVertex(0, 0, cnt).setColor(r, g, b, a);
        buffer.addVertex(0, 1, cnt).setColor(r, g, b, a);
        buffer.addVertex(0, 1, 0).setColor(r, g, b, a);
        buffer.addVertex(0, 0, 0).setColor(r, g, b, a);
    }

    public static void addSideFullTextureW(Matrix4f matrix, VertexConsumer buffer, int cnt, float r, float g, float b, boolean gui) {
        float a = gui ? 0.9f : 0.5f;
        buffer.addVertex(matrix, (float) offset.x, (float) offset.y, (float) (cnt + offset.z)).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) offset.x, (float) (1 + offset.y), (float) (cnt + offset.z)).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) offset.x, (float) (1 + offset.y), (float) offset.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) offset.x, (float) offset.y, (float) offset.z).setColor(r, g, b, a);
    }

    public static void addSideFullTextureN(VertexConsumer buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.addVertex(1, 1, 0).setColor(r, g, b, a);
        buffer.addVertex(1, 0, 0).setColor(r, g, b, a);
        buffer.addVertex(0, 0, 0).setColor(r, g, b, a);
        buffer.addVertex(0, 1, 0).setColor(r, g, b, a);
    }

    public static void addSideFullTextureN(Matrix4f matrix, VertexConsumer buffer, int cnt, float r, float g, float b, boolean gui) {
        float a = gui ? 0.9f : 0.5f;
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) (1 + offset.y), (float) offset.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) offset.y, (float) offset.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) offset.x, (float) offset.y, (float) offset.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) offset.x, (float) (1 + offset.y), (float) offset.z).setColor(r, g, b, a);
    }

    public static void addSideFullTextureS(VertexConsumer buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.addVertex(1, 0, cnt).setColor(r, g, b, a);
        buffer.addVertex(1, 1, cnt).setColor(r, g, b, a);
        buffer.addVertex(0, 1, cnt).setColor(r, g, b, a);
        buffer.addVertex(0, 0, cnt).setColor(r, g, b, a);
    }

    public static void addSideFullTextureS(Matrix4f matrix, VertexConsumer buffer, int cnt, float r, float g, float b, boolean gui) {
        float a = gui ? 0.9f : 0.5f;
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) offset.y, (float) (cnt + offset.z)).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) (1 + offset.x), (float) (1 + offset.y), (float) (cnt + offset.z)).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) offset.x, (float) (1 + offset.y), (float) (cnt + offset.z)).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) offset.x, (float) offset.y, (float) (cnt + offset.z)).setColor(r, g, b, a);
    }

    public static void addSideD(VertexConsumer buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.addVertex(l, l, l).setColor(r, g, b, a);
        buffer.addVertex(h, l, l).setColor(r, g, b, a);
        buffer.addVertex(h, l, h).setColor(r, g, b, a);
        buffer.addVertex(l, l, h).setColor(r, g, b, a);
    }

    public static void addSideU(VertexConsumer buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.addVertex(l, h, h).setColor(r, g, b, a);
        buffer.addVertex(h, h, h).setColor(r, g, b, a);
        buffer.addVertex(h, h, l).setColor(r, g, b, a);
        buffer.addVertex(l, h, l).setColor(r, g, b, a);
    }

    public static void addSideE(VertexConsumer buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.addVertex(h, l, l).setColor(r, g, b, a);
        buffer.addVertex(h, h, l).setColor(r, g, b, a);
        buffer.addVertex(h, h, h).setColor(r, g, b, a);
        buffer.addVertex(h, l, h).setColor(r, g, b, a);
    }

    public static void addSideW(VertexConsumer buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.addVertex(l, l, h).setColor(r, g, b, a);
        buffer.addVertex(l, h, h).setColor(r, g, b, a);
        buffer.addVertex(l, h, l).setColor(r, g, b, a);
        buffer.addVertex(l, l, l).setColor(r, g, b, a);
    }

    public static void addSideN(VertexConsumer buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.addVertex(h, h, l).setColor(r, g, b, a);
        buffer.addVertex(h, l, l).setColor(r, g, b, a);
        buffer.addVertex(l, l, l).setColor(r, g, b, a);
        buffer.addVertex(l, h, l).setColor(r, g, b, a);
    }

    public static void addSideS(VertexConsumer buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.addVertex(h, l, h).setColor(r, g, b, a);
        buffer.addVertex(h, h, h).setColor(r, g, b, a);
        buffer.addVertex(l, h, h).setColor(r, g, b, a);
        buffer.addVertex(l, l, h).setColor(r, g, b, a);
    }

    public static void addSideE(VertexConsumer buffer, float r, float g, float b, float size, float height) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.addVertex(h, 0, l).setColor(r, g, b, a);
        buffer.addVertex(h, height, l).setColor(r, g, b, a);
        buffer.addVertex(h, height, h).setColor(r, g, b, a);
        buffer.addVertex(h, 0, h).setColor(r, g, b, a);
    }

    public static void addSideW(VertexConsumer buffer, float r, float g, float b, float size, float height) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.addVertex(l, 0, h).setColor(r, g, b, a);
        buffer.addVertex(l, height, h).setColor(r, g, b, a);
        buffer.addVertex(l, height, l).setColor(r, g, b, a);
        buffer.addVertex(l, 0, l).setColor(r, g, b, a);
    }

    public static void addSideN(VertexConsumer buffer, float r, float g, float b, float size, float height) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.addVertex(h, height, l).setColor(r, g, b, a);
        buffer.addVertex(h, 0, l).setColor(r, g, b, a);
        buffer.addVertex(l, 0, l).setColor(r, g, b, a);
        buffer.addVertex(l, height, l).setColor(r, g, b, a);
    }

    public static void addSideS(VertexConsumer buffer, float r, float g, float b, float size, float height) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.addVertex(h, 0, h).setColor(r, g, b, a);
        buffer.addVertex(h, height, h).setColor(r, g, b, a);
        buffer.addVertex(l, height, h).setColor(r, g, b, a);
        buffer.addVertex(l, 0, h).setColor(r, g, b, a);
    }

}
