package mcjty.rftoolsbuilder.modules.mover.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverControlBlock;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;

public class MoverRenderer {

    // Global pre renderers
    private static final Map<BlockPos, Runnable> delayedPreRenders = new HashMap<>();
    private static final Map<BlockPos, BiFunction<Level, BlockPos, Boolean>> prerenderValidations = new HashMap<>();

    public static final ResourceLocation BLACK = new ResourceLocation(RFToolsBuilder.MODID, "effects/black");

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
            if (state.getBlock() instanceof MoverControlBlock) {
                positions.forEach(pos -> {
                    matrixStack.pushPose();
                    matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
                    BlockPos realPos = new BlockPos(current.x, current.y, current.z).offset(pos.getX(), pos.getY(), pos.getZ());
                    int lightColor = LevelRenderer.getLightColor(level, realPos);
                    renderMoverControls(matrixStack, buffer, mover, lightColor, state);
                    matrixStack.popPose();
                });
            }
        });
        matrixStack.popPose();
    }

    private static void renderMoverControls(@NotNull PoseStack matrixStack, @NotNull MultiBufferSource buffer,
                                            MoverTileEntity mover,
                                            int lightColor, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.FACING);
        Direction horizontalFacing = state.getValue(MoverControlBlock.HORIZ_FACING);
        float yRotation = switch (horizontalFacing) {
            case NORTH -> -180.0F;
            case WEST -> -90.0F;
            case EAST -> 90.0F;
            default -> 0;
        };
        float xRotation = switch (facing) {
            case DOWN -> 90.0F;
            case UP -> -90.0F;
            default -> 0;
        };

        matrixStack.translate(0.5F, 0.5F, 0.5F);
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(yRotation));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(xRotation));
        matrixStack.translate(0.0F, 0.0F, -0.4375F);

        renderScreenBoard(matrixStack, buffer, 1.0f, lightColor);
        renderMovers(matrixStack, buffer, Minecraft.getInstance().font, lightColor, mover);
    }

    private static void renderScreenBoard(PoseStack matrixStack, @Nullable MultiBufferSource buffer,
                                          float renderOffset, int packedLight) {
        matrixStack.pushPose();
        matrixStack.scale(1, -1, -1);

        Matrix4f matrix = matrixStack.last().pose();

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(BLACK);

        VertexConsumer builder = buffer.getBuffer(RenderType.solid());

        float zfront = -renderOffset+.05f;
        float ss = .5f;

        float u0 = sprite.getU0();
        float v0 = sprite.getV0();
        float u1 = sprite.getU1();
        float v1 = sprite.getV1();

        vt(builder, matrix, -ss, ss, zfront, u0, v0, packedLight);
        vt(builder, matrix, ss, ss, zfront, u1, v0, packedLight);
        vt(builder, matrix, ss, -ss, zfront, u1, v1, packedLight);
        vt(builder, matrix, -ss, -ss, zfront, u0, v1, packedLight);

        matrixStack.popPose();
    }

    public static void vt(VertexConsumer renderer, Matrix4f matrix, float x, float y, float z, float u, float v,
                          int packedLight) {
        renderer.vertex(matrix, x, y, z).color(1f, 1f, 1f, 1f).uv(u, v).uv2(packedLight).normal(1.0F, 0.0F, 0.0F).endVertex();
    }

    private static void renderMovers(PoseStack matrixStack, MultiBufferSource buffer, Font fontrenderer, int lightmapValue,
                                     MoverTileEntity mover) {
        boolean large = false;  // @todo
        boolean bright = true;  // @todo
        int textcolor = 0x999999;
        int currentcolor = 0x00ff00;

        float factor = 2.0f + (large ? 2 : 0);
        int currenty = 9 - (large ? 4 : 0);

        float f = 0.005F;

        matrixStack.pushPose();
        matrixStack.translate(-0.5F, 0.5F, 1 -.03);  // @todo tileEntity.getRenderOffset());
        matrixStack.scale(f * factor, -1.0f * f * factor, f);
        int l = 0;
        int linesSupported = 4;//@todo tileEntity.getLinesSupported();
        if (large) {
            linesSupported /= 2;
        }
        String currentPlatform = mover.getCurrentPlatform();
        for (String line : mover.getPlatformsFromServer()) {
            int color = line.equals(currentPlatform) ? currentcolor : textcolor;
            fontrenderer.drawInBatch(line, 10, currenty, 0xff000000 | color, false, matrixStack.last().pose(), buffer, false, 0,
                    bright ? LightTexture.FULL_BRIGHT : lightmapValue);
            currenty += 10;
            l++;
            if (l >= linesSupported) {
                break;
            }
        }
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
