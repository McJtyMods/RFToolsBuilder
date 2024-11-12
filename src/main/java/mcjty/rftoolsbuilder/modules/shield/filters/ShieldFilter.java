package mcjty.rftoolsbuilder.modules.shield.filters;

import com.mojang.serialization.Codec;
import mcjty.lib.blockcommands.ISerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface ShieldFilter {
    public static final int ACTION_PASS = 0;            // Entities that match this filter can pass
    public static final int ACTION_SOLID = 1;           // Entities that match this filter are blocked
    public static final int ACTION_DAMAGE = 2;          // Entities that match this filter get damage (can be combined with solid)

    public static final Codec<ShieldFilter> SHIELD_FILTER_CODEC = Codec.lazyInitialized(() -> Codec.STRING.dispatch("type",
            e -> e.getType().getID(),
            s -> XNet.xNetApi.findType(s).getConnectorCodec()));

//    public static final Codec<ConnectorInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
//            CONNECTOR_SETTINGS_CODEC.fieldOf("settings").forGetter(ConnectorInfo::getConnectorSettings),
//            SidedConsumer.CODEC.fieldOf("id").forGetter(ConnectorInfo::getId),
//            Codec.BOOL.fieldOf("advanced").forGetter(ConnectorInfo::isAdvanced)
//    ).apply(instance, ConnectorInfo::new));


    public static class Serializer implements ISerializer<ShieldFilter> {
        @Override
        public Function<RegistryFriendlyByteBuf, ShieldFilter> getDeserializer() {
            return AbstractShieldFilter::createFilter;
        }

        @Override
        public BiConsumer<RegistryFriendlyByteBuf, ShieldFilter> getSerializer() {
            return (buf, info) -> info.toBytes(buf);
        }
    }

    /// Return true if this entity matches the filter.
    boolean match(Entity entity);

    int getAction();

    void setAction(int action);

    String getFilterName();

    void readFromNBT(CompoundTag tagCompound);

    void writeToNBT(CompoundTag tagCompound);

    void toBytes(FriendlyByteBuf buf);
}
