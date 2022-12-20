package mcjty.rftoolsbuilder.modules.mover.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import mcjty.lib.client.RenderHelper;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverControlBlock;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverStatusBlock;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class MoverRenderer {

    // Global pre renderers
    private static final Map<BlockPos, Runnable> DELAYED_PRE_RENDERS = new HashMap<>();
    private static final Map<BlockPos, BiFunction<Level, BlockPos, Boolean>> PRERENDER_VALIDATIONS = new HashMap<>();

    public static final ResourceLocation BLACK = new ResourceLocation(RFToolsBuilder.MODID, "effects/black");

    // Number of lines supported per page on the renderer
    public static final int LINES_SUPPORTED = 7;

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
        MoverModule.INVISIBLE_MOVER_BLOCK.get().removeData(mover.getBlockPos());
        AtomicInteger totalPagers = new AtomicInteger();
        boolean[] pagers = new boolean[] { false, false, false, false };
        blocks.forEach((state, positions) -> {
            if (state.getBlock() instanceof MoverControlBlock moverControl) {
                if (!pagers[moverControl.getPage()]) {
                    pagers[moverControl.getPage()] = true;
                    totalPagers.incrementAndGet();
                }
            }
        });
        mover.setHighlightedMover("");
        blocks.forEach((state, positions) -> {
            positions.forEach(pos -> {
                matrixStack.pushPose();
                matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
                BlockPos realPos = new BlockPos(current.x, current.y, current.z).offset(pos.getX(), pos.getY(), pos.getZ());
                int lightColor = LevelRenderer.getLightColor(level, realPos);

                blockRenderer.renderSingleBlock(state, matrixStack, buffer, lightColor, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
                matrixStack.popPose();
            });
            if (state.getBlock() instanceof MoverControlBlock || state.getBlock() instanceof MoverStatusBlock) {
                positions.forEach(pos -> {
                    matrixStack.pushPose();
                    matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
                    BlockPos realPos = new BlockPos(current.x, current.y, current.z).offset(pos.getX(), pos.getY(), pos.getZ());
                    MoverModule.INVISIBLE_MOVER_BLOCK.get().registerData(mover.getBlockPos(), realPos,
                            state.getValue(MoverControlBlock.HORIZ_FACING), state.getValue(BlockStateProperties.FACING));
                    int lightColor = LevelRenderer.getLightColor(level, realPos);
                    setupTransform(matrixStack, state);
                    if (state.getBlock() instanceof MoverControlBlock moverControl) {
                        renderMoverControl(matrixStack, buffer, mover, lightColor, Objects.equals(realPos, mover.getCursorBlock()), getCorrectedPage(moverControl.getPage(), pagers), totalPagers.get());
                    } else {
                        renderMoverStatus(matrixStack, buffer, mover, lightColor);
                    }
                    matrixStack.popPose();
                });
            }
        });
        matrixStack.popPose();
    }

    private static int getCorrectedPage(int page, boolean[] pagers) {
        for (int i = 0 ; i < pagers.length ; i++) {
            if (pagers[i]) {
                if (page == 0) {
                    return i;
                }
                page--;
            }
        }
        return 0;
    }

    private static void renderMoverStatus(@NotNull PoseStack matrixStack, @NotNull MultiBufferSource buffer,
                                          MoverTileEntity mover, int lightColor) {
        renderScreenBoard(matrixStack, buffer, 1.0f, lightColor);
        renderStatus(matrixStack, buffer, Minecraft.getInstance().font, lightColor, mover);
    }

    private static void renderMoverControl(@NotNull PoseStack matrixStack, @NotNull MultiBufferSource buffer,
                                           MoverTileEntity mover, int lightColor, boolean doCursor, int page, int totalPagers) {
        renderScreenBoard(matrixStack, buffer, 1.0f, lightColor);
        renderMovers(matrixStack, buffer, Minecraft.getInstance().font, lightColor, mover, doCursor, page, totalPagers);
    }

    private static void setupTransform(@NotNull PoseStack matrixStack, BlockState state) {
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
    }

    private static void renderScreenBoard(PoseStack matrixStack, @Nullable MultiBufferSource buffer,
                                          float renderOffset, int packedLight) {
        matrixStack.pushPose();
        matrixStack.scale(1, -1, -1);
        Matrix4f matrix = matrixStack.last().pose();

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(BLACK);

        VertexConsumer builder = buffer.getBuffer(RenderType.solid());
        renderQuad(matrix, sprite, packedLight, builder, -renderOffset+.05f, .5f);

        matrixStack.popPose();
    }

    private static void renderQuad(Matrix4f matrix, TextureAtlasSprite sprite, int packedLight, VertexConsumer builder, float zfront, float ss) {
        float u0 = sprite.getU0();
        float v0 = sprite.getV0();
        float u1 = sprite.getU1();
        float v1 = sprite.getV1();

        vt(builder, matrix, -ss, ss, zfront, u0, v0, packedLight);
        vt(builder, matrix, ss, ss, zfront, u1, v0, packedLight);
        vt(builder, matrix, ss, -ss, zfront, u1, v1, packedLight);
        vt(builder, matrix, -ss, -ss, zfront, u0, v1, packedLight);
    }

    public static void vt(VertexConsumer renderer, Matrix4f matrix, float x, float y, float z, float u, float v, int packedLight) {
        renderer.vertex(matrix, x, y, z).color(1f, 1f, 1f, 1f).uv(u, v).uv2(packedLight).normal(1.0F, 0.0F, 0.0F).endVertex();
    }

    private static void renderMovers(PoseStack matrixStack, MultiBufferSource buffer, Font fontrenderer, int lightmapValue,
                                     MoverTileEntity mover, boolean doCursor, int page, int totalPagers) {
        boolean bright = true;  // @todo
        int textcolor = 0x999999;
        int currentcolor = 0x00ff00;

        float factor = 2.0f;
        int currenty = 9;

        float f = 0.005F;

        matrixStack.pushPose();
        matrixStack.translate(-0.5F, 0.5F, 1 -.03);  // @todo tileEntity.getRenderOffset());
        matrixStack.scale(f * factor, -1.0f * f * factor, f);
        int light = bright ? LightTexture.FULL_BRIGHT : lightmapValue;
        String currentPlatform = mover.getCurrentPlatform();

        if (!mover.isMoverValid()) {
            fontrenderer.drawInBatch("Not Connected!", 10, currenty, 0xffff0000, false, matrixStack.last().pose(), buffer, false, 0, light);
            fontrenderer.drawInBatch("Press 'Scan'", 10, currenty + 20, 0xffffffff, false, matrixStack.last().pose(), buffer, false, 0, light);
            fontrenderer.drawInBatch("at controller", 10, currenty + 30, 0xffffffff, false, matrixStack.last().pose(), buffer, false, 0, light);
        } else if (!mover.hasEnoughPower()) {
            fontrenderer.drawInBatch("No Power!", 10, currenty, 0xffff0000, false, matrixStack.last().pose(), buffer, false, 0, light);
        } else {
            double cursorX = mover.getCursorX();
            double cursorY = mover.getCursorY();

            List<String> platforms = mover.getPlatformsFromServer();
            int l = 0;
            int linesSupported = LINES_SUPPORTED;
            boolean showNavigator = page >= totalPagers - 1;
            if (!showNavigator) {
                linesSupported++;
            }
            int totalSupportedMovers = (linesSupported + 1) * (totalPagers - 1) + linesSupported;
            int start = mover.getCurrentPage() * totalSupportedMovers;
            if (page > 0) {
                start += (linesSupported + 1) * page;
            }
            for (int i = start ; i < platforms.size() ; i++) {
                String line = platforms.get(i);
                int color = line.equals(currentPlatform) ? currentcolor : textcolor;

                if (doCursor) {
                    renderCursor(matrixStack, buffer, mover, currenty, light, cursorY, currenty, currenty + 10, 5, 95, line);
                }
                fontrenderer.drawInBatch(line, 10, currenty, 0xff000000 | color, false, matrixStack.last().pose(), buffer, false, 0, light);
                currenty += 10;
                l++;
                if (l >= linesSupported) {
                    break;
                }
            }

            if (platforms.size() > totalSupportedMovers && showNavigator) {
                currenty = 12 + 10 * LINES_SUPPORTED;
                if (doCursor && currenty / 100.0 <= cursorY && cursorY <= (currenty + 10) / 100.0) {
                    renderCursor(matrixStack, buffer, mover, currenty, light, cursorX, 68, 78, 68, 78, "___<___");
                    renderCursor(matrixStack, buffer, mover, currenty, light, cursorX, 78, 88, 78, 88, "");
                    renderCursor(matrixStack, buffer, mover, currenty, light, cursorX, 88, 98, 88, 98, "___>___");
                }
                fontrenderer.drawInBatch("<", 70, currenty, 0xff0033dd, false, matrixStack.last().pose(), buffer, false, 0, light);
                fontrenderer.drawInBatch("" + (mover.getCurrentPage() + 1), 80, currenty, 0xff0033dd, false, matrixStack.last().pose(), buffer, false, 0, light);
                fontrenderer.drawInBatch(">", 90, currenty, 0xff0033dd, false, matrixStack.last().pose(), buffer, false, 0, light);
            }
        }
        matrixStack.popPose();
    }

    private static void renderCursor(PoseStack matrixStack, MultiBufferSource buffer, MoverTileEntity mover, int currenty, int light, double cursor, int cursor1, int cursor2, int x1, int x2, String s) {
        if (cursor1 / 100.0 <= cursor && cursor <= cursor2 / 100.0) {
            matrixStack.translate(0, 0, -0.01);
            RenderHelper.drawHorizontalGradientRect(matrixStack, buffer, x1, currenty - 1, x2, currenty + 9, 0xff333333, 0xff333333, light);
            matrixStack.translate(0, 0, 0.01);
            mover.setHighlightedMover(s);
        }
    }

    private static void renderStatus(PoseStack matrixStack, MultiBufferSource buffer, Font fontrenderer, int lightmapValue,
                                     MoverTileEntity mover) {
        boolean bright = true;  // @todo
        int textcolor = 0xff999999;
        int currentcolor = 0x00ff00;

        float factor = 2.0f;
        int currenty = 9;

        float f = 0.005F;

        matrixStack.pushPose();
        matrixStack.translate(-0.5F, 0.5F, 1 -.03);  // @todo tileEntity.getRenderOffset());
        matrixStack.scale(f * factor, -1.0f * f * factor, f);
        int l = 0;
        int light = bright ? LightTexture.FULL_BRIGHT : lightmapValue;
        String currentPlatform = mover.getCurrentPlatform();

        if (!mover.isMoverValid()) {
            fontrenderer.drawInBatch("Not Connected!", 10, currenty, 0xffff0000, false, matrixStack.last().pose(), buffer, false, 0, light);
            fontrenderer.drawInBatch("Press 'Scan'", 10, currenty+20, 0xffffffff, false, matrixStack.last().pose(), buffer, false, 0, light);
            fontrenderer.drawInBatch("at controller", 10, currenty+30, 0xffffffff, false, matrixStack.last().pose(), buffer, false, 0, light);
        } else {
            fontrenderer.drawInBatch("At:", 10, currenty, textcolor, false, matrixStack.last().pose(), buffer, false, 0, light);
            fontrenderer.drawInBatch(currentPlatform, 30, currenty, currentcolor, false, matrixStack.last().pose(), buffer, false, 0, light);
            currenty += 10;
            String destinationName = VehicleCard.getDesiredDestinationName(mover.getCard());
            if (destinationName != null && !destinationName.isEmpty()) {
                fontrenderer.drawInBatch("To:", 10, currenty, textcolor, false, matrixStack.last().pose(), buffer, false, 0, light);
                fontrenderer.drawInBatch(destinationName, 30, currenty, currentcolor, false, matrixStack.last().pose(), buffer, false, 0, light);
            }
        }
        matrixStack.popPose();
    }


    /**
     * Add code that is called very early in rendering
     */
    public static void addPreRender(BlockPos pos, Runnable renderer, BiFunction<Level, BlockPos, Boolean> validator) {
        DELAYED_PRE_RENDERS.put(pos, renderer);
        PRERENDER_VALIDATIONS.put(pos, validator);
    }

    public static void preRender() {
        Set<BlockPos> todelete = new HashSet<>();
        DELAYED_PRE_RENDERS.forEach((pos, consumer) -> {
            if (PRERENDER_VALIDATIONS.getOrDefault(pos, (level, blockPos) -> false).apply(Minecraft.getInstance().level, pos)) {
                consumer.run();
            } else {
                todelete.add(pos);
            }
        });
        for (BlockPos pos : todelete) {
            DELAYED_PRE_RENDERS.remove(pos);
            PRERENDER_VALIDATIONS.remove(pos);
        }
    }

    /**
     * Hook to allow us to move the entities very early in rendering (before entities are rendered)
     */
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        preRender();
    }
}
