package mcjty.rftoolsbuilder.modules.builder.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.rftoolsbase.modules.hud.client.HudRenderer;
import mcjty.rftoolsbuilder.modules.builder.BuilderConfiguration;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

import javax.annotation.Nonnull;


public class BuilderRenderer implements BlockEntityRenderer<BuilderTileEntity> {

    public BuilderRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@Nonnull BuilderTileEntity te, float v, @Nonnull PoseStack matrixStack, @Nonnull MultiBufferSource buffer, int i, int i1) {
        if (BuilderConfiguration.showProgressHud.get()) {
            HudRenderer.renderHud(matrixStack, buffer, te);
        }
    }

    public static void register() {
        BlockEntityRenderers.register(BuilderModule.TYPE_BUILDER.get(), BuilderRenderer::new);
    }
}
