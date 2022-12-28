package mcjty.rftoolsbuilder.modules.mover.items;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.rftoolsbase.api.screens.*;
import mcjty.rftoolsbase.api.screens.data.IModuleDataString;
import mcjty.rftoolsbase.tools.ScreenTextHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class VehicleStatusClientScreenModule implements IClientScreenModule<IModuleDataString> {

    private int labelColor = 0xffffff;
    private int color = 0xffffff;
    private String label = "";
    private String vehicle = "";

    private final ITextRenderHelper labelCache = new ScreenTextHelper();
    private final ITextRenderHelper cache = new ScreenTextHelper();

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 14;
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, IModuleRenderHelper renderHelper, Font fontRenderer, int currenty, IModuleDataString screenData, ModuleRenderInfo renderInfo) {
        int xoffset;
        int buttonWidth;
        if (!label.isEmpty()) {
            labelCache.setup(label, 316, renderInfo);
            labelCache.renderText(matrixStack, buffer, 0, currenty + 2, labelColor, renderInfo);
            xoffset = 7 + 40;
            buttonWidth = 300;
        } else {
            xoffset = 7 + 5;
            buttonWidth = 490;
        }

        String line = screenData == null ? null : screenData.get();
        if (line != null) {
            cache.setup(line, buttonWidth, renderInfo);
            cache.setDirty();
            cache.renderText(matrixStack, buffer, xoffset -10, currenty + 2, color, renderInfo);
        }
    }

    @Override
    public void mouseClick(Level world, int x, int y, boolean clicked) {
    }

    @Override
    public void setupFromNBT(CompoundTag tagCompound, ResourceKey<Level> dim, BlockPos pos) {
        if (tagCompound != null) {
            if (tagCompound.contains("color")) {
                color = tagCompound.getInt("color");
            } else {
                color = 0xffffff;
            }
            if (tagCompound.contains("labelColor")) {
                labelColor = tagCompound.getInt("labelColor");
            } else {
                labelColor = 0xffffff;
            }
            label = tagCompound.getString("label");
            vehicle = tagCompound.getString("vehicle");
            if (tagCompound.contains("align")) {
                String alignment = tagCompound.getString("align");
                labelCache.align(TextAlign.get(alignment));
            } else {
                labelCache.align(TextAlign.ALIGN_LEFT);
            }
            cache.setDirty();
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
