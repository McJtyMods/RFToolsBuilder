package mcjty.rftoolsbuilder.modules.mover.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import mcjty.rftoolsbuilder.modules.mover.blocks.VehicleBuilderTileEntity;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class MoverRenderer implements BlockEntityRenderer<MoverTileEntity> {

    public MoverRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MoverTileEntity tileEntity, float v, @Nonnull PoseStack matrixStack, @Nonnull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        ItemStack card = tileEntity.getCard();
        if (VehicleBuilderTileEntity.isVehicleCard(card)) {
            BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
            Map<BlockState, List<BlockPos>> blocks = VehicleCard.getBlocks(card, new BlockPos(1, 1, 1));
            blocks.forEach((state, positions) -> {
                positions.forEach(pos -> {
                    matrixStack.pushPose();
                    matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
                    Level level = tileEntity.getLevel();
                    BlockPos realPos = pos.offset(pos.getX(), pos.getY(), pos.getZ());
                    int lightColor = LevelRenderer.getLightColor(level, realPos);

                    blockRenderer.renderSingleBlock(state, matrixStack, buffer, lightColor, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
                    matrixStack.popPose();
                });
            });
        }
    }

    public static void register() {
        BlockEntityRenderers.register(MoverModule.TYPE_MOVER.get(), MoverRenderer::new);
    }
}
