package mcjty.rftoolsbuilder.modules.builder.items;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.DiggerItem;

import javax.annotation.Nonnull;
import java.util.Collections;

public class SuperHarvestingTool extends DiggerItem {

    public SuperHarvestingTool() {
        // @todo 1.18 / 1.21
        super(Tiers.NETHERITE, BlockTags.MINEABLE_WITH_PICKAXE, new Item.Properties());
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return super.isCorrectToolForDrops(stack, state);
    }
}
