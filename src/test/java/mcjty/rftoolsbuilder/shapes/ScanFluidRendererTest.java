package mcjty.rftoolsbuilder.shapes;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ScanFluidRendererTest {
    @Test
    void restoresSectionLocalFluidVerticesWithoutChangingTheirAttributes() {
        var type = RenderTypes.translucentMovingBlock();
        for (int x : new int[]{-33, -17, -1, 0, 15, 16, 33}) {
            var pos = new BlockPos(x, 17, -18);
            try (var actual = new ScanMesh.Builder(type); var expected = new ScanMesh.Builder(type)) {
                var output = ScanFluidRenderer.inScanCoordinates(actual, pos);
                for (int i = 0; i < 4; i++) {
                    float dx = i / 2f, dy = .875f, dz = i % 2;
                    // Match FluidRenderer's complete vertex call, including UVs, tint and light.
                    output.addVertex((pos.getX() & 15) + dx, (pos.getY() & 15) + dy, (pos.getZ() & 15) + dz,
                            0x80123456, .25f, .75f, 0, 0xf000f0, 0, 1, 0);
                    expected.addVertex(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz,
                            0x80123456, .25f, .75f, 0, 0xf000f0, 0, 1, 0);
                }
                assertArrayEquals(expected.build().getFirst().vertices, actual.build().getFirst().vertices,
                        "Fluid vertices must retain scan coordinates and all attributes at " + pos);
            }
        }
    }
}
