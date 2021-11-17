package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

import net.minecraft.block.AbstractBlock.Properties;

public class ShieldTemplateBlock extends Block implements ITooltipSettings {

    public enum TemplateColor {
        BLUE, RED, GREEN, YELLOW
    }

    private final Lazy<TooltipBuilder> tooltipBuilder = () -> new TooltipBuilder()
            .info(key("message.rftoolsbuilder.shiftmessage"))
            .infoShift(header());

    private final TemplateColor color;

    public ShieldTemplateBlock(TemplateColor color) {
        super(Properties.of(Material.GLASS).noOcclusion());
        this.color = color;
    }

    public TemplateColor getColor() {
        return color;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable IBlockReader worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltipBuilder.get().makeTooltip(getRegistryName(), stack, tooltip, flagIn);
    }
}
