package mcjty.rftoolsbuilder.modules.shield.filters;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.IMob;

public class AnimalFilter extends AbstractShieldFilter {

    public static final String ANIMAL = "animal";

    @Override
    public boolean match(Entity entity) {
        return entity instanceof MobEntity && !(entity instanceof IMob);
    }

    @Override
    public String getFilterName() {
        return ANIMAL;
    }
}
