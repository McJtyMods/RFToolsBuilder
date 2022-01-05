package mcjty.rftoolsbuilder.modules.shield.filters;

import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingBlock;
import net.minecraft.world.entity.Entity;

public class HostileFilter extends AbstractShieldFilter {

    public static final String HOSTILE = "hostile";

    @Override
    public boolean match(Entity entity) {
        return ShieldingBlock.isHostile(entity);
    }

    @Override
    public String getFilterName() {
        return HOSTILE;
    }
}
