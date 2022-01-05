package mcjty.rftoolsbuilder.modules.shield.filters;

import net.minecraft.world.entity.Entity;

public class DefaultFilter extends AbstractShieldFilter {

    public static final String DEFAULT = "default";

    @Override
    public boolean match(Entity entity) {
        return true;
    }

    @Override
    public String getFilterName() {
        return DEFAULT;
    }
}
