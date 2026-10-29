package mcjty.rftoolsbuilder.shapes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.PrimitiveTopology;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRenderer;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.fabricmc.fabric.api.client.rendering.v1.FeatureRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhases;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Draw cached buffers directly: never merge a whole scan into Minecraft's per-frame vertex builder. */
public final class ScanMeshRenderer implements FeatureRenderer<ScanMeshRenderer.Submit> {
    private static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("RFTools scan");
    private final Map<ScanMesh, Uploaded> buffers = new IdentityHashMap<>();
    private final List<List<Draw>> groups = new ArrayList<>();

    public record Submit(List<ScanMesh> meshes, Matrix4f pose) implements SubmitNode {
        @Override
        public FeatureRendererType<Submit> featureType() { return TYPE; }
    }

    private static class Uploaded implements AutoCloseable {
        final GpuBuffer buffer;
        long lastUsed;

        Uploaded(ScanMesh mesh) {
            var bytes = MemoryUtil.memAlloc(mesh.vertices.length);
            try {
                bytes.put(mesh.vertices).flip();
                buffer = RenderSystem.getDevice().createBuffer(() -> "RFTools scan", GpuBuffer.USAGE_VERTEX, bytes);
            } finally {
                MemoryUtil.memFree(bytes);
            }
        }

        @Override
        public void close() { buffer.close(); }
    }

    private record Draw(PreparedRenderType type, GpuBuffer vertices, int indexCount) { }

    public static void register() {
        FeatureRendererRegistry.register(TYPE, ScanMeshRenderer::new);
    }

    public static void submit(List<ScanMesh> meshes, PoseStack pose, SubmitNodeCollector collector, boolean gui) {
        List<ScanMesh> solid = new ArrayList<>();
        List<ScanMesh> translucent = new ArrayList<>();
        for (ScanMesh mesh : meshes) {
            (gui || !mesh.renderType.hasBlending() ? solid : translucent).add(mesh);
        }
        if (!solid.isEmpty()) collector.order(0).submitCustom(SubmitRenderPhases.SOLID,
                new Submit(List.copyOf(solid), new Matrix4f(pose.last().pose())));
        if (!translucent.isEmpty()) collector.order(0).submitCustom(SubmitRenderPhases.TRANSLUCENT_CUSTOM_GEOMETRY,
                new Submit(List.copyOf(translucent), new Matrix4f(pose.last().pose())));
    }

    @Override
    public void prepareGroup(FeatureFrameContext context, List<Submit> submits, boolean strictlyOrdered) {
        List<Draw> draws = new ArrayList<>();
        long now = System.nanoTime();
        var sequential = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
        // Reserve the largest batch up front so all draws use the final index element width.
        sequential.getBuffer(ScanMesh.MAX_VERTICES / 4 * 6);
        for (Submit submit : submits) {
            var transform = RenderSystem.getDynamicUniforms().writeTransform(
                    RenderSystem.getModelViewMatrixCopy().mul(submit.pose()));
            Map<RenderType, PreparedRenderType> types = new IdentityHashMap<>();
            for (ScanMesh mesh : submit.meshes()) {
                Uploaded uploaded = buffers.computeIfAbsent(mesh, Uploaded::new);
                uploaded.lastUsed = now;
                PreparedRenderType type = types.computeIfAbsent(mesh.renderType, key -> {
                    var base = key.prepare();
                    return new PreparedRenderType(base.pipeline(), base.outputTarget(), transform,
                            base.scissorState(), base.textures());
                });
                int indices = mesh.vertexCount / 4 * 6;
                draws.add(new Draw(type, uploaded.buffer, indices));
            }
        }
        groups.add(draws);
    }

    @Override
    public void executeGroup(FeatureFrameContext context, int groupIndex, List<Submit> submits, boolean strictlyOrdered) {
        var sequential = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
        for (Draw draw : groups.get(groupIndex)) {
            // Other features can grow the shared index buffer during preparation. Resolve it at draw time.
            var indices = sequential.getBuffer(draw.indexCount());
            draw.type().drawFromBuffer(draw.vertices(), indices, sequential.type(), 0, 0, draw.indexCount());
        }
    }

    @Override
    public void finishExecute(FeatureFrameContext context) {
        groups.clear();
        long oldest = System.nanoTime() - 2_000_000_000L;
        buffers.values().removeIf(buffer -> {
            if (buffer.lastUsed >= oldest) return false;
            buffer.close();
            return true;
        });
    }

    @Override
    public void close() {
        buffers.values().forEach(Uploaded::close);
        buffers.clear();
        groups.clear();
    }
}
