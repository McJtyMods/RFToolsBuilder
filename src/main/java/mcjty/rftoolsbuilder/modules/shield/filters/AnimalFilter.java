package mcjty.rftoolsbuilder.modules.shield.filters;

import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingBlock;
import net.minecraft.world.entity.Entity;

public class AnimalFilter extends AbstractShieldFilter {

    public static final String ANIMAL = "animal";

    @Override
    public boolean match(Entity entity) {
        return ShieldingBlock.isPassive(entity);
    }

    @Override
    public String getFilterName() {
        return ANIMAL;
    }
}
