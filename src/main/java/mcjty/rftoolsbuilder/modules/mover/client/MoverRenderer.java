package mcjty.rftoolsbuilder.modules.mover.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import mcjty.lib.client.AbstractDynamicBakedModel;
import mcjty.lib.client.CustomRenderTypes;
import mcjty.lib.client.RenderHelper;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverControlBlock;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverStatusBlock;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MoverRenderer {

    // Global pre renderers
    private static final Map<BlockPos, Runnable> DELAYED_PRE_RENDERS = new HashMap<>();
    private static final Map<BlockPos, BiFunction<Level, BlockPos, Boolean>> PRERENDER_VALIDATIONS = new HashMap<>();

    public static final Identifier BLACK = Identifier.fromNamespaceAndPath(RFToolsBuilder.MODID, "block/effects/black");

    // Number of lines supported per page on the renderer
    public static final int LINES_SUPPORTED = 7;

    public static float getPartialTicks() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }

    public static void actualRender(MoverTileEntity mover, @NotNull PoseStack graphics, Vec3 cameraPos, ItemStack card, Vec3 current, Vec3 offset,
                                    SubmitNodeCollector buffer) {
        PoseStack matrixStack = graphics;
        matrixStack.pushPose();
        Level level = mover.getLevel();
        matrixStack.translate(current.x - cameraPos.x - offset.x, current.y - cameraPos.y - offset.y, current.z - cameraPos.z - offset.z);

        Map<BlockState, List<BlockPos>> blocks = VehicleCard.getBlocks(card, mover.getOffset());
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
                BlockPos realPos = new BlockPos((int) current.x, (int) current.y, (int) current.z).offset(pos.getX(), pos.getY(), pos.getZ());
                int lightColor = LightCoordsUtil.getLightCoords((ClientLevel) level, realPos);

                submitBlock(state, level, realPos, matrixStack, buffer, lightColor);
                matrixStack.popPose();
            });
            if (state.getBlock() instanceof MoverControlBlock || state.getBlock() instanceof MoverStatusBlock) {
                positions.forEach(pos -> {
                    matrixStack.pushPose();
                    matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
                    BlockPos realPos = new BlockPos((int) current.x, (int) current.y, (int) current.z).offset(pos.getX(), pos.getY(), pos.getZ());
                    MoverModule.INVISIBLE_MOVER_BLOCK.get().registerData(mover.getBlockPos(), realPos,
                            state.getValue(MoverControlBlock.HORIZ_FACING), state.getValue(BlockStateProperties.FACING));
                    int lightColor = LightCoordsUtil.getLightCoords((ClientLevel) level, realPos);
                    setupTransform(graphics, state);
                    if (state.getBlock() instanceof MoverControlBlock moverControl) {
                        renderMoverControl(graphics, buffer, mover, lightColor, Objects.equals(realPos, mover.getCursorBlock()), getCorrectedPage(moverControl.getPage(), pagers), totalPagers.get());
                    } else {
                        renderMoverStatus(graphics, buffer, mover, lightColor);
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

    private static void renderMoverStatus(@NotNull PoseStack graphics, @NotNull SubmitNodeCollector buffer,
                                          MoverTileEntity mover, int lightColor) {
        renderScreenBoard(graphics, buffer, 1.0f, lightColor);
        renderStatus(graphics, buffer, Minecraft.getInstance().font, lightColor, mover);
    }

    private static void renderMoverControl(@NotNull PoseStack graphics, @NotNull SubmitNodeCollector buffer,
                                           MoverTileEntity mover, int lightColor, boolean doCursor, int page, int totalPagers) {
        renderScreenBoard(graphics, buffer, 1.0f, lightColor);
        renderMovers(graphics, buffer, Minecraft.getInstance().font, lightColor, mover, doCursor, page, totalPagers);
    }

    private static void setupTransform(@NotNull PoseStack graphics, BlockState state) {
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

        PoseStack matrixStack = graphics;
        matrixStack.translate(0.5F, 0.5F, 0.5F);
        RenderHelper.rotateYP(matrixStack, yRotation);
        RenderHelper.rotateXP(matrixStack, xRotation);
        matrixStack.translate(0.0F, 0.0F, -0.4375F);
    }

    private static void renderScreenBoard(PoseStack graphics, @Nullable SubmitNodeCollector buffer,
                                          float renderOffset, int packedLight) {
        PoseStack matrixStack = graphics;
        matrixStack.pushPose();
        matrixStack.scale(1, -1, -1);

        TextureAtlasSprite sprite = AbstractDynamicBakedModel.getTexture(BLACK);

        buffer.submitCustomGeometry(matrixStack, CustomRenderTypes.translucent(), (captured, vertices) -> {
            PoseStack pose = new PoseStack(); pose.last().set(captured);
            RenderHelper.renderQuadGui(pose, sprite, packedLight, vertices, -renderOffset + .05f, .5f);
        });

        matrixStack.popPose();
    }

    private static void renderMovers(PoseStack graphics, SubmitNodeCollector buffer, Font fontrenderer, int lightmapValue,
                                     MoverTileEntity mover, boolean doCursor, int page, int totalPagers) {
        boolean bright = true;  // @todo
        int textcolor = 0x999999;
        int currentcolor = 0x00ff00;

        float factor = 2.0f;
        int currenty = 9;

        float f = 0.005F;

        PoseStack matrixStack = graphics;
        matrixStack.pushPose();
        matrixStack.translate(-0.5F, 0.5F, 1 -.03);  // @todo tileEntity.getRenderOffset());
        matrixStack.scale(f * factor, -1.0f * f * factor, f);
        int light = bright ? LightCoordsUtil.FULL_BRIGHT : lightmapValue;
        String currentPlatform = mover.getCurrentPlatform();

        if (!mover.isMoverValid()) {
            RenderHelper.renderText(fontrenderer, "Not Connected!", 10, currenty, 0xffff0000, matrixStack, buffer, light);
            RenderHelper.renderText(fontrenderer, "Press 'Scan'", 10, currenty + 20, 0xffffffff, matrixStack, buffer, light);
            RenderHelper.renderText(fontrenderer, "at controller", 10, currenty + 30, 0xffffffff, matrixStack, buffer, light);
        } else if (!mover.hasEnoughPower()) {
            RenderHelper.renderText(fontrenderer, "No Power!", 10, currenty, 0xffff0000, matrixStack, buffer, light);
        } else {
            double cursorX = mover.getCursorX();
            double cursorY = mover.getCursorY();

            List<String> platforms = mover.getPlatformsFromServer();
            int l = 0;
            int linesSupported = LINES_SUPPORTED;
            boolean showNavigator = page >= totalPagers - 1;
            if (!showNavigator) {
                linesSupported += 2;
            }
            int totalSupportedMovers = (linesSupported + 2) * (totalPagers - 1) + linesSupported;
            int start = mover.getCurrentPage() * totalSupportedMovers;
            if (page > 0) {
                start += (linesSupported + 2) * page;
                currenty = 0;
            }
            for (int i = start ; i < platforms.size() ; i++) {
                String line = platforms.get(i);
                int color = line.equals(currentPlatform) ? currentcolor : textcolor;

                if (doCursor) {
                    renderCursor(graphics, buffer, mover, currenty, light, cursorY, currenty, currenty + 10, 5, 95, line);
                }
                RenderHelper.renderText(fontrenderer, line, 10, currenty, 0xff000000 | color, matrixStack, buffer, light);
                currenty += 10;
                l++;
                if (l >= linesSupported) {
                    break;
                }
            }

            if (platforms.size() > totalSupportedMovers && showNavigator) {
                currenty = 12 + 10 * LINES_SUPPORTED;
                if (doCursor && currenty / 100.0 <= cursorY && cursorY <= (currenty + 10) / 100.0) {
                    renderCursor(graphics, buffer, mover, currenty, light, cursorX, 68, 78, 68, 78, "___<___");
                    renderCursor(graphics, buffer, mover, currenty, light, cursorX, 78, 88, 78, 88, "");
                    renderCursor(graphics, buffer, mover, currenty, light, cursorX, 88, 98, 88, 98, "___>___");
                }
                RenderHelper.renderText(fontrenderer, "<", 70, currenty, 0xff0033dd, matrixStack, buffer, light);
                RenderHelper.renderText(fontrenderer, "" + (mover.getCurrentPage() + 1), 80, currenty, 0xff0033dd, matrixStack, buffer, light);
                RenderHelper.renderText(fontrenderer, ">", 90, currenty, 0xff0033dd, matrixStack, buffer, light);
            }
        }
        matrixStack.popPose();
    }

    private static void renderCursor(PoseStack graphics, SubmitNodeCollector buffer, MoverTileEntity mover, int currenty, int light, double cursor, int cursor1, int cursor2, int x1, int x2, String s) {
        if (cursor1 / 100.0 <= cursor && cursor <= cursor2 / 100.0) {
            PoseStack matrixStack = graphics;
            matrixStack.translate(0, 0, -0.01);
            buffer.submitCustomGeometry(matrixStack, CustomRenderTypes.QUADS_NOTEXTURE, (captured, vertices) -> {
                vertices.addVertex(captured.pose(), x1, currenty - 1, 0).setColor(0xff333333);
                vertices.addVertex(captured.pose(), x1, currenty + 9, 0).setColor(0xff333333);
                vertices.addVertex(captured.pose(), x2, currenty + 9, 0).setColor(0xff333333);
                vertices.addVertex(captured.pose(), x2, currenty - 1, 0).setColor(0xff333333);
            });
            matrixStack.translate(0, 0, 0.01);
            mover.setHighlightedMover(s);
        }
    }

    private static void renderStatus(PoseStack graphics, SubmitNodeCollector buffer, Font fontrenderer, int lightmapValue,
                                     MoverTileEntity mover) {
        boolean bright = true;  // @todo
        int textcolor = 0xff999999;
        int currentcolor = 0x00ff00;

        float factor = 2.0f;
        int currenty = 9;

        float f = 0.005F;

        PoseStack matrixStack = graphics;
        matrixStack.pushPose();
        matrixStack.translate(-0.5F, 0.5F, 1 -.03);  // @todo tileEntity.getRenderOffset());
        matrixStack.scale(f * factor, -1.0f * f * factor, f);
        int l = 0;
        int light = bright ? LightCoordsUtil.FULL_BRIGHT : lightmapValue;
        String currentPlatform = mover.getCurrentPlatform();

        if (!mover.isMoverValid()) {
            RenderHelper.renderText(fontrenderer, "Not Connected!", 10, currenty, 0xffff0000, matrixStack, buffer, light);
            RenderHelper.renderText(fontrenderer, "Press 'Scan'", 10, currenty+20, 0xffffffff, matrixStack, buffer, light);
            RenderHelper.renderText(fontrenderer, "at controller", 10, currenty+30, 0xffffffff, matrixStack, buffer, light);
        } else {
            RenderHelper.renderText(fontrenderer, "At:", 10, currenty, textcolor, matrixStack, buffer, light);
            RenderHelper.renderText(fontrenderer, currentPlatform, 30, currenty, currentcolor, matrixStack, buffer, light);
            currenty += 10;
            String destinationName = VehicleCard.getDesiredDestinationName(mover.getCard());
            if (destinationName != null && !destinationName.isEmpty()) {
                RenderHelper.renderText(fontrenderer, "To:", 10, currenty, textcolor, matrixStack, buffer, light);
                RenderHelper.renderText(fontrenderer, destinationName, 30, currenty, currentcolor, matrixStack, buffer, light);
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

    private record ActiveMover(MoverTileEntity mover, ItemStack vehicle) { }
    private static final Map<BlockPos, ActiveMover> ACTIVE = new HashMap<>();

    public static void addMover(MoverTileEntity mover, ItemStack vehicle) {
        ACTIVE.put(mover.getBlockPos(), new ActiveMover(mover, vehicle.copy()));
    }

    public static void submitMovers(SubmitCustomGeometryEvent event) {
        var level = Minecraft.getInstance().level;
        ACTIVE.entrySet().removeIf(entry -> level == null || entry.getValue().mover().getLevel() != level
                || !entry.getValue().mover().isMoverThere(level, entry.getKey()) || entry.getValue().mover().isRemoved());
        for (var active : ACTIVE.values()) {
            var mover = active.mover();
            float partialTicks = getPartialTicks();
            Vec3 offset = mover.getLogic().tryMoveVehicleThisPlayer(partialTicks);
            Vec3 current;
            if (mover.getCard().isEmpty()) {
                if (mover.getLastDestination() == null) continue;
                current = Vec3.atLowerCornerOf(mover.getLastDestination());
            } else {
                current = mover.getLogic().getMovingPosition(partialTicks, level.getGameTime());
                mover.setLastDestination(mover.getLogic().getDestination());
            }
            actualRender(mover, event.getPoseStack(), event.getLevelRenderState().cameraRenderState.pos,
                    active.vehicle(), current, offset, event.getSubmitNodeCollector());
        }
    }

    private static void submitBlock(BlockState state, Level level, BlockPos pos, PoseStack pose, SubmitNodeCollector collector, int light) {
        List<BlockStateModelPart> parts = new ArrayList<>();
        var minecraft = Minecraft.getInstance();
        minecraft.getModelManager().getBlockStateModelSet().get(state).collectParts((ClientLevel) level, pos, state, RandomSource.create(42), parts);
        int[] tints = minecraft.getBlockColors().getTintSources(state).stream().mapToInt(tint -> tint.colorInWorld(state, (ClientLevel) level, pos)).toArray();
        collector.submitBlockModel(pose, RenderTypes.translucentMovingBlock(), List.copyOf(parts), tints, light, OverlayTexture.NO_OVERLAY, 0);
    }
}
