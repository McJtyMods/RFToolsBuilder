package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class ShieldTemplateBlock extends Block implements ITooltipSettings {

    public enum TemplateColor {
        BLUE("blue"), RED("red"), GREEN("green"), YELLOW("yellow");

        private final String name;

        TemplateColor(String name) {
            this.name = name;
        }
    }

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(key("message.rftoolsbuilder.shiftmessage"))
            .infoShift(header());

    private final TemplateColor color;

    public ShieldTemplateBlock(TemplateColor color) {
        super(Properties.create(Material.GLASS));
        this.color = color;
    }


    public static final VoxelShape SMALLER_SHAPE = VoxelShapes.create(0.01, 0.01F, 0.01F, .99F, .99F, .99F);

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader reader, BlockPos pos) {
        return SMALLER_SHAPE;
    }

    public TemplateColor getColor() {
        return color;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltipBuilder.makeTooltip(getRegistryName(), stack, tooltip, flagIn);
    }
}
