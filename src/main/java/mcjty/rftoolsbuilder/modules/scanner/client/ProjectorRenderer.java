package mcjty.rftoolsbuilder.modules.scanner.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.lib.client.RenderHelper;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ProjectorTileEntity;
import mcjty.rftoolsbuilder.shapes.ShapeRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

public class ProjectorRenderer implements BlockEntityRenderer<ProjectorTileEntity, ProjectorRenderer.State> {
    public ProjectorRenderer(BlockEntityRendererProvider.Context context) { }

    public static class State extends BlockEntityRenderState {
        ShapeRenderer.Frame frame = ShapeRenderer.Frame.EMPTY;
        float offset, scale, angle;
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(ProjectorTileEntity tile, State state, float partialTicks, Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, camera, overlay);
        state.frame = tile.isProjecting() && !tile.getRenderStack().isEmpty()
                ? tile.getShapeRenderer().capture(tile.getRenderStack(), tile.isScanline(), tile.isRenderBlockModels()) : ShapeRenderer.Frame.EMPTY;
        state.offset = tile.getVerticalOffset(); state.scale = tile.getScale(); state.angle = tile.getAngle();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(.5f, 1 + state.offset, .5f);
        pose.scale(state.scale, state.scale, state.scale);
        RenderHelper.rotateYP(pose, state.angle);
        ShapeRenderer.submit(state.frame, pose, collector);
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() { return true; }

    public static void register() { BlockEntityRenderers.register(ScannerModule.TYPE_PROJECTOR.get(), ProjectorRenderer::new); }
}
