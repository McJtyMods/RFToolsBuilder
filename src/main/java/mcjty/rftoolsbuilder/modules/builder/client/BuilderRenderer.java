package mcjty.rftoolsbuilder.modules.builder.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import mcjty.lib.client.HudRenderHelper;
import mcjty.lib.network.Networking;
import mcjty.lib.network.PacketGetListFromServer;
import mcjty.rftoolsbase.modules.hud.Hud;
import mcjty.rftoolsbuilder.modules.builder.BuilderConfiguration;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.*;

public class BuilderRenderer implements BlockEntityRenderer<BuilderTileEntity, BuilderRenderer.State> {
    public BuilderRenderer(BlockEntityRendererProvider.Context context) { }

    public static class State extends BlockEntityRenderState {
        List<String> log = List.of();
        Direction facing;
        boolean show, above;
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(BuilderTileEntity tile, State state, float partialTicks, Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, camera, overlay);
        state.show = BuilderConfiguration.showProgressHud.get();
        state.log = List.copyOf(tile.getClientLog());
        state.facing = tile.getBlockOrientation();
        state.above = tile.isBlockAboveAir();
        if (state.show && System.currentTimeMillis() - tile.getLastUpdateTime() > 250) {
            Networking.sendToServer(PacketGetListFromServer.create(tile.getHudPos(), Hud.COMMAND_GETHUDLOG));
            tile.setLastUpdateTime(System.currentTimeMillis());
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.show) HudRenderHelper.renderHud(pose, collector, state.log,
                state.above ? HudRenderHelper.HudPlacement.HUD_ABOVE : HudRenderHelper.HudPlacement.HUD_ABOVE_FRONT,
                state.facing == null ? HudRenderHelper.HudOrientation.HUD_TOPLAYER_HORIZ : HudRenderHelper.HudOrientation.HUD_SOUTH,
                state.facing, 0, 0, 0, 1);
    }

    public static void register() { BlockEntityRenderers.register(BuilderModule.BUILDER.be().get(), BuilderRenderer::new); }

    @Override
    public AABB getRenderBoundingBox(BuilderTileEntity tile) {
        return new AABB(tile.getBlockPos().getCenter(), tile.getBlockPos().offset(1, 2, 1).getCenter());
    }
}
