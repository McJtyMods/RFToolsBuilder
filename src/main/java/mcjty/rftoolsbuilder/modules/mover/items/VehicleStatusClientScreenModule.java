package mcjty.rftoolsbuilder.modules.mover.items;

import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ITextRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import mcjty.rftoolsbase.api.screens.data.IModuleDataString;
import mcjty.rftoolsbase.tools.ScreenTextHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class VehicleStatusClientScreenModule implements IClientScreenModule<IModuleDataString> {

    private final ITextRenderHelper labelCache = new ScreenTextHelper();
    private final ITextRenderHelper cache = new ScreenTextHelper();

    @Override
    public TransformMode getTransformMode(ItemStack moduleItem) {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight(ItemStack moduleItem) {
        return 14;
    }

    @Override
    public void render(GuiGraphics graphics, MultiBufferSource buffer, IModuleRenderHelper renderHelper, Font fontRenderer, int currenty, IModuleDataString screenData, ModuleRenderInfo renderInfo) {
        int xoffset;
        int buttonWidth;
        VehicleStatusScreenModule data = VehicleStatusModuleItem.data(renderInfo.moduleStack);
        if (!data.getLabel().isEmpty()) {
            labelCache.setup(data.getLabel(), 316, renderInfo);
            labelCache.align(data.getAlign());
            labelCache.renderText(graphics, buffer, 0, currenty + 2, data.getLabelColor(), renderInfo);
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
            cache.renderText(graphics, buffer, xoffset -10, currenty + 2, data.getColor(), renderInfo);
        }
    }

    @Override
    public void mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked) {
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
