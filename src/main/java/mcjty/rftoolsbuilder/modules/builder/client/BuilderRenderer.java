package mcjty.rftoolsbuilder.modules.builder.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcjty.rftoolsbase.modules.hud.client.HudRenderer;
import mcjty.rftoolsbuilder.modules.builder.BuilderConfiguration;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.fml.client.registry.ClientRegistry;


public class BuilderRenderer extends TileEntityRenderer<BuilderTileEntity> {

    public BuilderRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(BuilderTileEntity te, float v, MatrixStack matrixStack, IRenderTypeBuffer buffer, int i, int i1) {
        if (BuilderConfiguration.showProgressHud.get()) {
            HudRenderer.renderHud(matrixStack, buffer, te);
        }
    }

    public static void register() {
        ClientRegistry.bindTileEntityRenderer(BuilderModule.TYPE_BUILDER.get(), BuilderRenderer::new);
    }
}
