package mcjty.rftoolsbuilder.shapes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.List;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;

/** A depth-tested preview removes the need to project and sort every face on the CPU. */
public final class ScanPreviewRenderer extends PictureInPictureRenderer<ScanPreviewRenderer.State> {
    private static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("rftoolsbuilder", "pipeline/scan_preview"))
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withDepthStencilState(DepthStencilState.DEFAULT).build();
    static final RenderType RENDER_TYPE = RenderType.create("rftoolsbuilder_scan_preview",
            RenderSetup.builder(PIPELINE).createRenderSetup());

    public record State(List<ScanMesh> meshes, Matrix4f transform, int x0, int y0, int x1, int y1,
            Matrix3x2f pose, ScreenRectangle scissorArea) implements PictureInPictureRenderState {
        @Override
        public float scale() { return 1; }

        @Override
        public ScreenRectangle bounds() {
            return PictureInPictureRenderState.getBounds(x0, y0, x1, y1, pose, scissorArea);
        }
    }

    public static void register(RegisterPictureInPictureRenderersEvent event) {
        event.register(State.class, ScanPreviewRenderer::new);
    }

    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(PIPELINE);
    }

    @Override
    public Class<State> getRenderStateClass() { return State.class; }

    @Override
    protected String getTextureLabel() { return "RFTools scan preview"; }

    @Override
    protected void renderToTexture(State state, PoseStack pose, SubmitNodeCollector collector) {
        applyView(state, pose);
        ScanMeshRenderer.submit(state.meshes(), pose, collector, true);
    }

    @Override
    protected float getTranslateY(int height, int guiScale) { return 0; }

    static void applyView(State state, PoseStack pose) {
        // PiP uses GUI pixel coordinates but flips Z. Restore the old preview's screen-space depth.
        pose.translate(-(state.x1() - state.x0()) / 2f, 0, 0);
        pose.scale(1, 1, -1);
        pose.translate(-state.x0(), -state.y0(), 0);
        pose.mulPose(state.transform());
    }
}
