package mcjty.rftoolsbuilder.modules.mover.items;

import mcjty.lib.client.RenderHelper;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ITextRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import mcjty.rftoolsbase.tools.ScreenTextHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class VehicleControlClientScreenModule implements IClientScreenModule<VehicleControlScreenModule.EmptyData> {

    private boolean activated = false;

    private final ITextRenderHelper labelCache = new ScreenTextHelper();
    private final ITextRenderHelper buttonCache = new ScreenTextHelper();

    @Override
    public TransformMode getTransformMode(ItemStack moduleItem) {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight(ItemStack moduleItem) {
        return 14;
    }

    @Override
    public void render(GuiGraphics graphics, MultiBufferSource buffer, IModuleRenderHelper renderHelper, Font fontRenderer, int currenty, VehicleControlScreenModule.EmptyData screenData, ModuleRenderInfo renderInfo) {
        VehicleControlScreenModule data = VehicleControlModuleItem.data(renderInfo.moduleStack);
        int xoffset;
        int buttonWidth;
        if (!data.getLine().isEmpty()) {
            labelCache.setup(data.getLine(), 316, renderInfo);
            labelCache.align(data.getAlign());
            labelCache.renderText(graphics, buffer, 0, currenty + 2, data.getColor(), renderInfo);
            xoffset = 7 + 40;
            buttonWidth = 300;
        } else {
            xoffset = 7 + 5;
            buttonWidth = 490;
        }

        boolean act = activated;

        RenderHelper.drawBeveledBox(graphics, buffer, xoffset - 5, currenty, 130 - 7, currenty + 12, act ? 0xff333333 : 0xffeeeeee, act ? 0xffeeeeee : 0xff333333, 0xff666666,
                renderInfo.getLightmapValue());
        buttonCache.setup(data.getButton(), buttonWidth, renderInfo);
        buttonCache.renderText(graphics, buffer, xoffset -10 + (act ? 1 : 0), currenty + 2, data.getButtonColor(), renderInfo);
    }

    @Override
    public void mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked) {
        int xoffset;
        VehicleControlScreenModule data = VehicleControlModuleItem.data(moduleStack);
        if (!data.getLine().isEmpty()) {
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
    public boolean needsServerData() {
        return false;
    }
}
