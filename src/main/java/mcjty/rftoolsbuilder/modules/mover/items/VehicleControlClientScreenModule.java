package mcjty.rftoolsbuilder.modules.mover.items;

import mcjty.lib.client.RenderHelper;
import mcjty.rftoolsbase.api.screens.*;
import mcjty.rftoolsbase.tools.ScreenTextHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class VehicleControlClientScreenModule implements IClientScreenModule<VehicleControlScreenModule.EmptyData> {

    private String line = "";
    private String button = "";
    private int color = 0xffffff;
    private int buttonColor = 0xffffff;
    private boolean activated = false;
    private String vehicle = "";
    private String mover = "";

    private final ITextRenderHelper labelCache = new ScreenTextHelper();
    private final ITextRenderHelper buttonCache = new ScreenTextHelper();

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 14;
    }

    @Override
    public void render(GuiGraphics graphics, MultiBufferSource buffer, IModuleRenderHelper renderHelper, Font fontRenderer, int currenty, VehicleControlScreenModule.EmptyData screenData, ModuleRenderInfo renderInfo) {
        int xoffset;
        int buttonWidth;
        if (!line.isEmpty()) {
            labelCache.setup(line, 316, renderInfo);
            labelCache.renderText(graphics, buffer, 0, currenty + 2, color, renderInfo);
            xoffset = 7 + 40;
            buttonWidth = 300;
        } else {
            xoffset = 7 + 5;
            buttonWidth = 490;
        }

        boolean act = activated;

        RenderHelper.drawBeveledBox(graphics, buffer, xoffset - 5, currenty, 130 - 7, currenty + 12, act ? 0xff333333 : 0xffeeeeee, act ? 0xffeeeeee : 0xff333333, 0xff666666,
                renderInfo.getLightmapValue());
        buttonCache.setup(button, buttonWidth, renderInfo);
        buttonCache.renderText(graphics, buffer, xoffset -10 + (act ? 1 : 0), currenty + 2, buttonColor, renderInfo);
    }

    @Override
    public void mouseClick(Level world, int x, int y, boolean clicked) {
        int xoffset;
        if (!line.isEmpty()) {
            xoffset = 40;
        } else {
            xoffset = 5;
        }
        activated = false;
        if (x >= xoffset) {
            activated = clicked;
        }
    }

    @Override
    public void setupFromNBT(CompoundTag tagCompound, ResourceKey<Level> dim, BlockPos pos) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            button = tagCompound.getString("button");
            if (tagCompound.contains("color")) {
                color = tagCompound.getInt("color");
            } else {
                color = 0xffffff;
            }
            if (tagCompound.contains("buttonColor")) {
                buttonColor = tagCompound.getInt("buttonColor");
            } else {
                buttonColor = 0xffffff;
            }
            mover = tagCompound.getString("mover");
            vehicle = tagCompound.getString("vehicle");
            if (tagCompound.contains("align")) {
                String alignment = tagCompound.getString("align");
                labelCache.align(TextAlign.get(alignment));
            } else {
                labelCache.align(TextAlign.ALIGN_LEFT);
            }
            buttonCache.setDirty();
        }
    }

    @Override
    public boolean needsServerData() {
        return false;
    }
}
