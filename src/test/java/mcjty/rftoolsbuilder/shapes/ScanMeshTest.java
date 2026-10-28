package mcjty.rftoolsbuilder.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScanMeshTest {
    @Test
    void splitsBeyondMinecraftVertexLimitWithoutDroppingGeometry() {
        int vertices = 16_777_216 + 4;
        try (var builder = new ScanMesh.Builder(RenderTypes.debugQuads())) {
            for (int i = 0; i < vertices; i++) builder.addVertex(i, 2, 3).setColor(0x80402010);
            var meshes = builder.build();
            assertEquals(vertices, meshes.stream().mapToInt(mesh -> mesh.vertexCount).sum());
            int first = 0;
            for (var mesh : meshes) {
                assertTrue(mesh.vertexCount > 0 && mesh.vertexCount <= ScanMesh.MAX_VERTICES);
                assertEquals(0, mesh.vertexCount % 4);
                assertEquals(mesh.vertexCount * 16, mesh.vertices.length);
                var data = ByteBuffer.wrap(mesh.vertices).order(ByteOrder.nativeOrder());
                assertEquals((float) first, data.getFloat(0));
                assertEquals((float) (first + mesh.vertexCount - 1), data.getFloat((mesh.vertexCount - 1) * 16));
                assertEquals(0x80, mesh.vertices[15] & 255);
                first += mesh.vertexCount;
            }
        }
    }

    @Test
    void preservesEveryAttributeAcrossBlockModelBatchBoundary() {
        var type = RenderTypes.translucentMovingBlock();
        try (var builder = new ScanMesh.Builder(type)) {
            for (int i = 0; i < ScanMesh.MAX_VERTICES + 4; i++) {
                builder.addVertex(i % 4, 2, 3).setColor(0xff123456).setUv(.25f, .75f)
                        .setOverlay(0).setLight(0xf000f0).setNormal(0, 1, 0);
            }
            var meshes = builder.build();
            assertEquals(2, meshes.size());
            int stride = type.format().getVertexSize();
            assertArrayEquals(Arrays.copyOf(meshes.getFirst().vertices, 4 * stride), meshes.getLast().vertices);
        }
    }

    @Test
    void previewIsOpaqueAndEmptyGeometryHasNoBatches() {
        try (var builder = new ScanMesh.Builder(ScanPreviewRenderer.RENDER_TYPE, true)) {
            for (int i = 0; i < 4; i++) builder.addVertex(i, 0, 0).setColor(0x80402010);
            var mesh = builder.build().getFirst();
            assertEquals(255, mesh.vertices[15] & 255);
            assertEquals(0x40, mesh.vertices[12] & 255);
        }
        try (var builder = new ScanMesh.Builder(RenderTypes.debugQuads())) {
            assertTrue(builder.build().isEmpty());
        }
    }
}
