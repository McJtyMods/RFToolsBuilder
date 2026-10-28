package mcjty.rftoolsbuilder.modules.shield.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.common.util.Lazy;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class ShieldTemplateBlock extends Block implements ITooltipSettings {

    public enum TemplateColor {
        BLUE, RED, GREEN, YELLOW
    }

    private final Lazy<TooltipBuilder> tooltipBuilder = Lazy.of(() -> new TooltipBuilder()
            .info(key("message.rftoolsbuilder.shiftmessage"))
            .infoShift(header()));

    private final TemplateColor color;

    public ShieldTemplateBlock(Properties properties, TemplateColor color) {
        super(properties.noOcclusion().sound(SoundType.GLASS));
        this.color = color;
    }

    public BlockItem createItem(Item.Properties properties) {
        return new BlockItem(this, properties.useBlockDescriptionPrefix()) {
            @Override
            public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                    Consumer<Component> tooltip, TooltipFlag flags) {
                super.appendHoverText(stack, context, display, tooltip, flags);
                List<Component> lines = new ArrayList<>();
                tooltipBuilder.get().makeTooltip(Tools.getId(ShieldTemplateBlock.this), stack, lines, flags);
                lines.forEach(tooltip);
            }
        };
    }

    public TemplateColor getColor() {
        return color;
    }


}
