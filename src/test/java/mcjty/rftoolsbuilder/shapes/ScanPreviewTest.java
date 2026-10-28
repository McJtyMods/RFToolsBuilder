package mcjty.rftoolsbuilder.shapes;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScanPreviewTest {
    @Test
    void previewMatchesGuiHelpersAtDifferentScalesAndPanPositions() {
        for (int guiScale : new int[]{1, 2, 4}) {
            var view = new Matrix4f().translate(214, 137, 0).rotateX(.5f).rotateY(.7f).scale(-3, 3, 3);
            var state = new ScanPreviewRenderer.State(List.of(), view, 134, 72, 295, 202,
                    new Matrix3x2f(), new ScreenRectangle(134, 72, 161, 130));
            var pose = new PoseStack();
            // The PiP base class positions the origin at the horizontal centre and flips Z.
            pose.translate(161 * guiScale / 2f, 0, 0);
            pose.scale(guiScale, guiScale, -guiScale);
            ScanPreviewRenderer.applyView(state, pose);
            var point = new Vector3f(7, -4, 9);
            var gui = view.transformPosition(new Vector3f(point));
            var preview = pose.last().pose().transformPosition(point);
            assertEquals((gui.x - state.x0()) * guiScale, preview.x, .001f);
            assertEquals((gui.y - state.y0()) * guiScale, preview.y, .001f);
            assertEquals(gui.z * guiScale, preview.z, .001f);
        }
    }

    @Test
    void previewWritesDepthAndUsesAnOpaqueRenderPass() {
        assertTrue(ScanPreviewRenderer.RENDER_TYPE.pipeline().getDepthStencilState().writeDepth());
        assertFalse(ScanPreviewRenderer.RENDER_TYPE.hasBlending());
    }
}
