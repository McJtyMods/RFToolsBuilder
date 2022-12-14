package mcjty.rftoolsbuilder.modules.mover.items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mcjty.lib.client.CustomRenderTypes;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.varia.ItemStackList;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class VehicleControlClientScreenModule implements IClientScreenModule<VehicleControlScreenModule.ModuleDataStacks> {

    public static final int LARGESIZE = 22;
    public static final int SMALLSIZE = 16;

    private int buttonColor = 0xffffff;
    private int currentLevelButtonColor = 0xffff00;
    private boolean vertical = false;
    private boolean large = false;

    private final ItemStackList stacks = ItemStackList.create(9);

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.ITEM;
    }

    @Override
    public int getHeight() {
        return 114;
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, IModuleRenderHelper renderHelper, Font fontRenderer, int currenty, VehicleControlScreenModule.ModuleDataStacks screenData, ModuleRenderInfo renderInfo) {
        if (screenData == null) {
            return;
        }

        if (renderInfo.hitx >= 0) {
            matrixStack.pushPose();
            matrixStack.translate(-0.5F, 0.5F, 0.07F);
            float f3 = 0.0105F;
            matrixStack.scale(f3 * renderInfo.factor, -f3 * renderInfo.factor, f3);

            int y = currenty;
            int i = 0;

            for (int yy = 0 ; yy < 3 ; yy++) {
                for (int xx = 0 ; xx < 3 ; xx++) {
                    if (!stacks.get(i).isEmpty()) {
                        int x = xx * 40;
                        boolean hilighted = renderInfo.hitx >= x+8 && renderInfo.hitx <= x + 38 && renderInfo.hity >= y-7 && renderInfo.hity <= y + 22;
                        if (hilighted) {
                            RenderHelper.drawFlatButtonBox(matrixStack, buffer, (int) (5 + xx * 30.5f), 10 + yy * 24 - 4, (int) (29 + xx * 30.5f), 10 + yy * 24 + 20, 0xffffffff, 0xff333333, 0xffffffff,
                                    renderInfo.getLightmapValue());
                        }
                    }
                    i++;
                }
                y += 35;
            }
            matrixStack.popPose();
        }

        matrixStack.pushPose();
        float f3 = 0.0105F;
        matrixStack.translate(-0.5F, 0.5F, 0.06F);
        float factor = renderInfo.factor;
        matrixStack.scale(f3 * factor, f3 * factor, 0.0001f);

        int y = currenty;
        int i = 0;

        for (int yy = 0 ; yy < 3 ; yy++) {
            for (int xx = 0 ; xx < 3 ; xx++) {
                if (!stacks.get(i).isEmpty()) {
                    int x = 7 + xx * 30;
                    renderSlot(matrixStack, buffer, -16-y, stacks.get(i), x, renderInfo.getLightmapValue());
                }
                i++;
            }
            y += 24;
        }

        matrixStack.popPose();

        matrixStack.pushPose();
        matrixStack.translate(-0.5F, 0.5F, 0.08F);
        f3 = 0.0050F;
        matrixStack.scale(f3 * factor, -f3 * factor, 0.0001f);

        y = currenty + 30;
        i = 0;

        for (int yy = 0 ; yy < 3 ; yy++) {
            for (int xx = 0 ; xx < 3 ; xx++) {
                if (!stacks.get(i).isEmpty()) {
                    renderSlotOverlay(matrixStack, buffer, fontRenderer, y, stacks.get(i), screenData.getAmount(i), 32 + xx * 64,
                            renderInfo.getLightmapValue());
                }
                i++;
            }
            y += 52;
        }

        boolean insertStackActive = renderInfo.hitx >= 0 && renderInfo.hitx < 60 && renderInfo.hity > 98 && renderInfo.hity <= 120;
        fontRenderer.drawInBatch("Insert Stack", 20, y - 20, insertStackActive ? 0xffffff : 0x666666, false, matrixStack.last().pose(), buffer, false, 0, renderInfo.getLightmapValue());
        boolean insertAllActive = renderInfo.hitx >= 60 && renderInfo.hitx <= 120 && renderInfo.hity > 98 && renderInfo.hity <= 120;
        fontRenderer.drawInBatch("Insert All", 120, y - 20, insertAllActive ? 0xffffff : 0x666666, false, matrixStack.last().pose(), buffer, false, 0, renderInfo.getLightmapValue());

        matrixStack.popPose();
    }

    @Override
    public void mouseClick(Level world, int x, int y, boolean clicked) {

    }

    private void renderSlot(PoseStack matrixStack, MultiBufferSource buffer, int currenty, ItemStack stack, int x, int lightmapValue) {
        matrixStack.pushPose();
        matrixStack.translate(x +8f, currenty +8f, 5);
        matrixStack.scale(16, 16, 16);

        ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
        BakedModel ibakedmodel = itemRender.getModel(stack, Minecraft.getInstance().level, null, 0);    // @todo 1.18 last parameter?
        itemRender.render(stack, ItemTransforms.TransformType.GUI, false, matrixStack, buffer, lightmapValue, OverlayTexture.NO_OVERLAY, ibakedmodel);
        matrixStack.popPose();
    }

    private void renderSlotOverlay(PoseStack matrixStack, MultiBufferSource buffer, Font fontRenderer, int currenty, ItemStack stack, int amount, int x, int lightmapValue) {
        if (!stack.isEmpty()) {
            String s1;
            if (amount < 10000) {
                s1 = String.valueOf(amount);
            } else if (amount < 1000000) {
                s1 = String.valueOf(amount / 1000) + "k";
            } else if (amount < 1000000000) {
                s1 = String.valueOf(amount / 1000000) + "m";
            } else {
                s1 = String.valueOf(amount / 1000000000) + "g";
            }
            fontRenderer.drawInBatch(s1, x + 19 - 2 - fontRenderer.width(s1), currenty + 6 + 3, 16777215, false, matrixStack.last().pose(), buffer, false, 0, lightmapValue);

            if (stack.getItem().isBarVisible(stack)) {
                double health = stack.getItem().getBarWidth(stack);
                int j1 = (int) Math.round(13.0D - health * 13.0D);
                int k = (int) Math.round(255.0D - health * 255.0D);
                int l = 255 - k << 16 | k << 8;
                int i1 = (255 - k) / 4 << 16 | 16128;
                VertexConsumer builder = buffer.getBuffer(CustomRenderTypes.QUADS_NOTEXTURE);
                renderQuad(builder, x + 2, currenty + 13, 13, 2, 0, 0.0D, lightmapValue);
                renderQuad(builder, x + 2, currenty + 13, 12, 1, i1, 0.02D, lightmapValue);
                renderQuad(builder, x + 2, currenty + 13, j1, 1, l, 0.04D, lightmapValue);
            }
        }
    }

    private static void renderQuad(VertexConsumer builder, int x, int y, int width, int height, int color, double offset, int lightmapValue) {
        builder.vertex(x, y, offset).color(1.0f, 1.0f, 1.0f, 1.0f).uv2(lightmapValue).endVertex();
        builder.vertex(x, (y + height), offset).color(1.0f, 1.0f, 1.0f, 1.0f).uv2(lightmapValue).endVertex();
        builder.vertex((x + width), (y + height), offset).color(1.0f, 1.0f, 1.0f, 1.0f).uv2(lightmapValue).endVertex();
        builder.vertex((x + width), y, offset).color(1.0f, 1.0f, 1.0f, 1.0f).uv2(lightmapValue).endVertex();
    }


    @Override
    public void setupFromNBT(CompoundTag tagCompound, ResourceKey<Level> dim, BlockPos pos) {
        if (tagCompound != null) {
            for (int i = 0 ; i < stacks.size() ; i++) {
                if (tagCompound.contains("stack"+i)) {
                    stacks.set(i, ItemStack.of(tagCompound.getCompound("stack" + i)));
                }
            }
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
