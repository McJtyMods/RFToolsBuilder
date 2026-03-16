package mcjty.rftoolsbuilder.modules.scanner.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ProjectorTileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ProjectorRenderer implements BlockEntityRenderer<ProjectorTileEntity> {

    public ProjectorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@Nonnull ProjectorTileEntity te, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        ItemStack renderStack = te.getRenderStack();
        if (te.isProjecting() && !renderStack.isEmpty()) {
            te.getShapeRenderer().renderShapeInWorld(poseStack, renderStack, te.getVerticalOffset(), te.getScale(), te.getAngle(),
                    te.isScanline(), te.getShapeID(), te.isRenderBlockModels());
        }
    }

    @Override
    public boolean shouldRenderOffScreen(ProjectorTileEntity blockEntity) {
        return true;
    }

    public static void register() {
        BlockEntityRenderers.register(ScannerModule.TYPE_PROJECTOR.get(), ProjectorRenderer::new);
    }
}
