package mcjty.rftoolsbuilder.modules.shield.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockRenderLayer;

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

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    public TemplateColor getColor() {
        return color;
    }
}
