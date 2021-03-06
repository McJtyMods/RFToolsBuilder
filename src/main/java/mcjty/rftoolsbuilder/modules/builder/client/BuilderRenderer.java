package mcjty.rftoolsbuilder.modules.builder.client;

import mcjty.rftoolsbase.modules.hud.client.HudRenderer;
import mcjty.rftoolsbuilder.modules.builder.BuilderConfiguration;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraftforge.fml.client.registry.ClientRegistry;


public class BuilderRenderer extends TileEntityRenderer<BuilderTileEntity> {

    @Override
    public void render(BuilderTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
        super.render(te, x, y, z, partialTicks, destroyStage);
        if (BuilderConfiguration.showProgressHud.get()) {
            HudRenderer.renderHud(te, x, y, z);
        }
    }

    public static void register() {
        ClientRegistry.bindTileEntitySpecialRenderer(BuilderTileEntity.class, new BuilderRenderer());
    }
}
