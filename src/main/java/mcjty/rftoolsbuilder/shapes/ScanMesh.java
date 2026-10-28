package mcjty.rftoolsbuilder.shapes;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.ArrayList;
import java.util.List;

/** Immutable, bounded geometry in scan coordinates. GPU ownership belongs to the feature renderer. */
public final class ScanMesh {
    public static final int MAX_VERTICES = 65536;
    final byte[] vertices;
    final int vertexCount;
    final RenderType renderType;

    private ScanMesh(byte[] vertices, int vertexCount, RenderType renderType) {
        this.vertices = vertices;
        this.vertexCount = vertexCount;
        this.renderType = renderType;
    }

    private static void capture(BufferBuilder builder, RenderType type, List<ScanMesh> output, boolean opaque) {
        try (MeshData mesh = builder.build()) {
            if (mesh == null) return;
            var data = mesh.vertexBuffer();
            int stride = type.format().getVertexSize();
            for (int first = 0; first < mesh.drawState().vertexCount(); first += MAX_VERTICES) {
                int count = Math.min(MAX_VERTICES, mesh.drawState().vertexCount() - first);
                byte[] bytes = new byte[count * stride];
                data.get(bytes);
                if (opaque) {
                    // POSITION_COLOR is used for the GUI's opaque, depth-tested preview.
                    for (int at = 15; at < bytes.length; at += stride) bytes[at] = (byte) 255;
                }
                output.add(new ScanMesh(bytes, count, type));
            }
        }
    }

    /** Flush between quads, before even unusually complex models can reach BufferBuilder's limit. */
    public static final class Builder implements VertexConsumer, AutoCloseable {
        private final ByteBufferBuilder storage = new ByteBufferBuilder(65536);
        private final RenderType type;
        private final List<ScanMesh> meshes = new ArrayList<>();
        private BufferBuilder builder;
        private int vertices;
        private final boolean opaque;

        public Builder(RenderType type) {
            this(type, false);
        }

        public Builder(RenderType type, boolean opaque) {
            this.type = type;
            this.opaque = opaque;
            builder = new BufferBuilder(storage, PrimitiveTopology.QUADS, type.format());
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            if (vertices == MAX_VERTICES) flush();
            vertices++;
            builder.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) { builder.setColor(color); return this; }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) { builder.setColor(r, g, b, a); return this; }

        @Override
        public VertexConsumer setUv(float u, float v) { builder.setUv(u, v); return this; }

        @Override
        public VertexConsumer setUv1(int u, int v) { builder.setUv1(u, v); return this; }

        @Override
        public VertexConsumer setUv2(int u, int v) { builder.setUv2(u, v); return this; }

        @Override
        public VertexConsumer setUv3(float u, float v) { builder.setUv3(u, v); return this; }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) { builder.setNormal(x, y, z); return this; }

        @Override
        public VertexConsumer setLineWidth(float width) { builder.setLineWidth(width); return this; }

        private void flush() {
            capture(builder, type, meshes, opaque);
            storage.clear();
            builder = new BufferBuilder(storage, PrimitiveTopology.QUADS, type.format());
            vertices = 0;
        }

        public List<ScanMesh> build() {
            flush();
            return List.copyOf(meshes);
        }

        @Override
        public void close() {
            storage.close();
        }
    }
}
