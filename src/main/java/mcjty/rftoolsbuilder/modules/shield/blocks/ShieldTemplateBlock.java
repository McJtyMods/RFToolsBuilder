package mcjty.rftoolsbuilder.modules.shield.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class ShieldTemplateBlock extends Block {

    public enum TemplateColor {
        BLUE("blue"), RED("red"), GREEN("green"), YELLOW("yellow");

        private final String name;

        TemplateColor(String name) {
            this.name = name;
        }
    }

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
}
