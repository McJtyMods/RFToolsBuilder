package mcjty.rftoolsbuilder.modules.shield.filters;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;

public class PlayerFilter extends AbstractShieldFilter {
    public static final String PLAYER = "player";
    private String name = null;

    public PlayerFilter() {
    }

    public PlayerFilter(String name) {
        this.name = name;
    }

    @Override
    public String getFilterName() {
        return PLAYER;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean match(Entity entity) {
        if (!(entity instanceof Player)) {
            return false;
        }

        if (name == null) {
            return true;
        }

        Player PlayerEntity = (Player) entity;
        return name.equals(PlayerEntity.getName().getString());
    }

    @Override
    public void readFromNBT(CompoundTag tagCompound) {
        super.readFromNBT(tagCompound);
        name = tagCompound.getString("name");
    }

    @Override
    public void writeToNBT(CompoundTag tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.putString("name", name);
    }
}
