package mcjty.rftoolsbuilder.modules.scanner.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ProjectorTileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;


public class ProjectorRenderer implements BlockEntityRenderer<ProjectorTileEntity> {

    private static Renderer renderer;

    public ProjectorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@Nonnull ProjectorTileEntity te, float partialTicks, @Nonnull PoseStack matrixStack, @Nonnull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (renderer == null) {
            renderer = new Renderer();
            renderer.buildBuffer(te.getBlockPos());
        }
        renderer.render(matrixStack);
    }

    public static void register() {
//        BlockEntityRenderers.register(ScannerModule.TYPE_PROJECTOR.get(), ProjectorRenderer::new);
    }
}
