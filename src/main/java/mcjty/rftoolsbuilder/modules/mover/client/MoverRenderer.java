package mcjty.rftoolsbuilder.modules.mover.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class MoverRenderer {

    public static void actualRender(MoverTileEntity tileEntity, @NotNull PoseStack matrixStack, Vec3 cameraPos, ItemStack card) {
        matrixStack.pushPose();
        BlockPos blockPos = tileEntity.getBlockPos();
        matrixStack.translate(blockPos.getX() - cameraPos.x, blockPos.getY() - cameraPos.y, blockPos.getZ() - cameraPos.z);
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        Map<BlockState, List<BlockPos>> blocks = VehicleCard.getBlocks(card, new BlockPos(1, 1, 1));
        Level level = tileEntity.getLevel();
        blocks.forEach((state, positions) -> {
            positions.forEach(pos -> {
                matrixStack.pushPose();
                matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
                BlockPos realPos = blockPos.offset(pos.getX(), pos.getY(), pos.getZ());
                int lightColor = LevelRenderer.getLightColor(level, realPos);

                blockRenderer.renderSingleBlock(state, matrixStack, buffer, lightColor, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
                matrixStack.popPose();
            });
        });
        matrixStack.popPose();
    }
}
