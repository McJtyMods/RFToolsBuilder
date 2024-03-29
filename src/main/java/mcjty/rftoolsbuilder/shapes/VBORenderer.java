package mcjty.rftoolsbuilder.shapes;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;

import java.io.Closeable;
import java.util.function.BiConsumer;

public class VBORenderer implements Closeable {
    private static final int BUFFER_SIZE = 2 * 1024 * 1024;
    private static final BufferBuilder BUILDER = new BufferBuilder(BUFFER_SIZE);

    public static VBORenderer of(int glMode, VertexFormat fmt, BiConsumer<BufferBuilder, VertexFormat> vertexProducer) {
        VertexBuffer vbo = new VertexBuffer(fmt);

        BUILDER.begin(glMode, fmt);
        vertexProducer.accept(BUILDER, fmt);
        BUILDER.clear();
        // 1.14: vbo.bufferData(BUILDER.getByteBuffer());
        vbo.upload(BUILDER);
        return new VBORenderer(vbo, glMode);
    }

    private final VertexBuffer vbo;
    private final int glMode;

    public VBORenderer(VertexBuffer vbo, int glMode) {
        this.vbo = vbo;
        this.glMode = glMode;
    }

    public void render(Matrix4f matrix) {
        // 1.14: vbo.drawArrays(glMode);
        vbo.draw(matrix, glMode);
    }

    @Override
    public void close() {
        //1.14: vbo.deleteGlBuffers();
        vbo.close();
    }
}
