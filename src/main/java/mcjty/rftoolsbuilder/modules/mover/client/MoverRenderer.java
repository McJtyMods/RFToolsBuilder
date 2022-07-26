package mcjty.rftoolsbuilder.modules.mover.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;

public class MoverRenderer {

    // Global pre renderers
    private static final Map<BlockPos, Runnable> delayedPreRenders = new HashMap<>();
    private static final Map<BlockPos, BiFunction<Level, BlockPos, Boolean>> prerenderValidations = new HashMap<>();

    public static float getPartialTicks() {
        return Minecraft.getInstance().getFrameTime();
    }

    public static void actualRender(MoverTileEntity mover, @NotNull PoseStack matrixStack, Vec3 cameraPos, ItemStack card, float partialTicks, Vec3 offset,
                                    RenderType renderType) {
        matrixStack.pushPose();
        Level level = mover.getLevel();
        Vec3 current = mover.getLogic().getMovingPosition(partialTicks, level.getGameTime());
        matrixStack.translate(current.x - cameraPos.x - offset.x, current.y - cameraPos.y - offset.y, current.z - cameraPos.z - offset.z);

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        Map<BlockState, List<BlockPos>> blocks = VehicleCard.getBlocks(card, new BlockPos(1, 1, 1));
        blocks.forEach((state, positions) -> {
            positions.forEach(pos -> {
                matrixStack.pushPose();
                matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
                BlockPos realPos = new BlockPos(current.x, current.y, current.z).offset(pos.getX(), pos.getY(), pos.getZ());
                int lightColor = LevelRenderer.getLightColor(level, realPos);

                blockRenderer.renderSingleBlock(state, matrixStack, buffer, lightColor, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
                matrixStack.popPose();
            });
        });
        matrixStack.popPose();
    }

    /**
     * Add code that is called very early in rendering
     */

    public static void addPreRender(BlockPos pos, Runnable renderer, BiFunction<Level, BlockPos, Boolean> validator) {
        delayedPreRenders.put(pos, renderer);
        prerenderValidations.put(pos, validator);
    }

    public static void preRender() {
        Set<BlockPos> todelete = new HashSet<>();
        delayedPreRenders.forEach((pos, consumer) -> {
            if (prerenderValidations.getOrDefault(pos, (level, blockPos) -> false).apply(Minecraft.getInstance().level, pos)) {
                consumer.run();
            } else {
                todelete.add(pos);
            }
        });
        for (BlockPos pos : todelete) {
            delayedPreRenders.remove(pos);
            prerenderValidations.remove(pos);
        }
    }

    /**
     * Hook to allow us to move the entities very early in rendering (before entities are rendered)
     */
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        preRender();
    }
}