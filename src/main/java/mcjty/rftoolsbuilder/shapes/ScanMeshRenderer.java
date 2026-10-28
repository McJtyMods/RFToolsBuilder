package mcjty.rftoolsbuilder.shapes;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/** 26.1.2 has no custom feature renderer registry. Draw cached buffers during its geometry callbacks. */
public final class ScanMeshRenderer {
    private static final Map<ScanMesh, Uploaded> BUFFERS = new IdentityHashMap<>();

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

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ScanMeshRenderer::endFrame);
        NeoForge.EVENT_BUS.addListener(ScanMeshRenderer::stop);
    }

    public static void submit(List<ScanMesh> meshes, PoseStack pose, SubmitNodeCollector collector, boolean gui) {
        Map<RenderType, List<ScanMesh>> groups = new IdentityHashMap<>();
        for (ScanMesh mesh : meshes) groups.computeIfAbsent(mesh.renderType, key -> new ArrayList<>()).add(mesh);
        groups.forEach((type, group) -> {
            var captured = List.copyOf(group);
            // No vertices enter the shared BufferSource: each cached mesh remains a separate GPU draw.
            collector.submitCustomGeometry(pose, type, (transform, unused) -> draw(captured, transform.pose()));
        });
    }

    public static void draw(List<ScanMesh> meshes, Matrix4f pose) {
        if (meshes.isEmpty()) return;
        var transform = RenderSystem.getDynamicUniforms().writeTransform(
                new Matrix4f(RenderSystem.getModelViewMatrix()).mul(pose), new Vector4f(1, 1, 1, 1),
                new Vector3f(), new Matrix4f());
        var sequential = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        var indices = sequential.getBuffer(ScanMesh.MAX_VERTICES / 4 * 6);
        Map<RenderType, Map<String, RenderSetup.TextureAndSampler>> textures = new IdentityHashMap<>();
        long now = System.nanoTime();
        for (ScanMesh mesh : meshes) {
            Uploaded uploaded = BUFFERS.computeIfAbsent(mesh, Uploaded::new);
            uploaded.lastUsed = now;
            var type = mesh.renderType;
            var bindings = textures.computeIfAbsent(type, key -> {
                var setup = RenderSetup.builder(key.pipeline());
                if (key.format() != DefaultVertexFormat.POSITION_COLOR) {
                    setup.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS).useLightmap();
                }
                return setup.createRenderSetup().getTextures();
            });
            var target = type.outputTarget().getRenderTarget();
            var color = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : target.getColorTextureView();
            var depth = RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : target.getDepthTextureView();
            try (var pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    () -> "RFTools scan", color, OptionalInt.empty(), depth, OptionalDouble.empty())) {
                pass.setPipeline(type.pipeline());
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("DynamicTransforms", transform);
                pass.setVertexBuffer(0, uploaded.buffer);
                for (var binding : bindings.entrySet()) {
                    pass.bindTexture(binding.getKey(), binding.getValue().textureView(), binding.getValue().sampler());
                }
                pass.setIndexBuffer(indices, sequential.type());
                pass.drawIndexed(0, 0, mesh.vertexCount / 4 * 6, 1);
            }
        }
    }

    private static void endFrame(RenderFrameEvent.Post event) {
        long oldest = System.nanoTime() - 2_000_000_000L;
        BUFFERS.values().removeIf(buffer -> {
            if (buffer.lastUsed >= oldest) return false;
            buffer.close();
            return true;
        });
    }

    private static void stop(ClientStoppingEvent event) {
        BUFFERS.values().forEach(Uploaded::close);
        BUFFERS.clear();
    }
}
