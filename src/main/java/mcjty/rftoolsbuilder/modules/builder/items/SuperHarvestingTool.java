package mcjty.rftoolsbuilder.modules.builder.items;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTier;
import net.minecraft.item.ToolItem;

import java.util.Collections;

public class SuperHarvestingTool extends ToolItem {

    public SuperHarvestingTool() {
        super(1000.0f, 1000.0f, ItemTier.DIAMOND, Collections.emptySet(), new Item.Properties());
        setRegistryName("superharvestingtool");
    }

    @Override
    public boolean canHarvestBlock(BlockState state) {
        return true;
    }
}
