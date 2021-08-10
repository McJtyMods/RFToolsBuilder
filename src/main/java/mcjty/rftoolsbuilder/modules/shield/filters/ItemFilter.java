package mcjty.rftoolsbuilder.modules.shield.filters;

import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingBlock;
import net.minecraft.entity.Entity;

public class ItemFilter extends AbstractShieldFilter {

    public static final String ITEM = "item";

    @Override
    public boolean match(Entity entity) {
        return ShieldingBlock.isItem(entity);
    }

    @Override
    public String getFilterName() {
        return ITEM;
    }
}
