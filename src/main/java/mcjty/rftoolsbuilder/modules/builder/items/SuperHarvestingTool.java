package mcjty.rftoolsbuilder.modules.builder.items;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

/** Internal tool used to calculate quarry drops and enchantments. */
public class SuperHarvestingTool extends Item {
    public SuperHarvestingTool(Item.Properties properties) {
        super(ToolMaterial.NETHERITE.applyToolProperties(properties, BlockTags.MINEABLE_WITH_PICKAXE, 1.0f, -2.8f, 0));
    }
}
