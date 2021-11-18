package mcjty.rftoolsbuilder.modules.shield.filters;

import mcjty.lib.blockcommands.ISerializer;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface ShieldFilter {
    public static final int ACTION_PASS = 0;            // Entities that match this filter can pass
    public static final int ACTION_SOLID = 1;           // Entities that match this filter are blocked
    public static final int ACTION_DAMAGE = 2;          // Entities that match this filter get damage (can be combined with solid)

    public static class Serializer implements ISerializer<ShieldFilter> {
        @Override
        public Function<PacketBuffer, ShieldFilter> getDeserializer() {
            return AbstractShieldFilter::createFilter;
        }

        @Override
        public BiConsumer<PacketBuffer, ShieldFilter> getSerializer() {
            return (buf, info) -> info.toBytes(buf);
        }
    }

    /// Return true if this entity matches the filter.
    boolean match(Entity entity);

    int getAction();

    void setAction(int action);

    String getFilterName();

    void readFromNBT(CompoundNBT tagCompound);

    void writeToNBT(CompoundNBT tagCompound);

    void toBytes(PacketBuffer buf);
}
