package mcjty.rftoolsbuilder.modules.builder.items;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.DiggerItem;

import javax.annotation.Nonnull;
import java.util.Collections;

public class SuperHarvestingTool extends DiggerItem {

    public SuperHarvestingTool() {
        // @todo 1.18
        super(1000.0f, 1000.0f, Tiers.DIAMOND, BlockTags.MINEABLE_WITH_PICKAXE, new Item.Properties());
    }

    @Override
    public boolean isCorrectToolForDrops(@Nonnull BlockState state) {
        return true;
    }
}
