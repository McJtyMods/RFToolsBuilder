package mcjty.rftoolsbuilder.modules.shield.filters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import mcjty.lib.blockcommands.ISerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface ShieldFilter<T extends ShieldFilter<?>> {
    int ACTION_PASS = 0;            // Entities that match this filter can pass
    int ACTION_SOLID = 1;           // Entities that match this filter are blocked
    int ACTION_DAMAGE = 2;          // Entities that match this filter get damage (can be combined with solid)

    Map<String, MapCodec<? extends ShieldFilter<?>>> CODECS = Map.of(
            AnimalFilter.ID, AnimalFilter.CODEC,
            DefaultFilter.ID, DefaultFilter.CODEC,
            HostileFilter.ID, HostileFilter.CODEC,
            ItemFilter.ID, ItemFilter.CODEC,
            PlayerFilter.ID, PlayerFilter.CODEC
    );
    Map<String, StreamCodec> STREAM_CODECS = Map.of(
            AnimalFilter.ID, AnimalFilter.STREAM_CODEC,
            DefaultFilter.ID, DefaultFilter.STREAM_CODEC,
            HostileFilter.ID, HostileFilter.STREAM_CODEC,
            ItemFilter.ID, ItemFilter.STREAM_CODEC,
            PlayerFilter.ID, PlayerFilter.STREAM_CODEC
    );

    Codec<ShieldFilter<?>> CODEC = Codec.lazyInitialized(() -> Codec.STRING.dispatch("type",
            ShieldFilter::getFilterName,
            s -> CODECS.get(s)));

    StreamCodec<FriendlyByteBuf, ShieldFilter<?>> STREAM_CODEC = StreamCodec.of(
            (buf, shieldFilter) -> {
                buf.writeUtf(shieldFilter.getFilterName());
                StreamCodec streamCodec = shieldFilter.getStreamCodec();
                streamCodec.encode(buf, shieldFilter);
            },
            buf -> {
                String id = buf.readUtf();
                StreamCodec streamCodec = STREAM_CODECS.get(id);
                return (ShieldFilter<?>) streamCodec.decode(buf);
            });

    class Serializer implements ISerializer<ShieldFilter<?>> {
        @Override
        public Function<RegistryFriendlyByteBuf, ShieldFilter<?>> getDeserializer() {
            return buf -> {
                if (buf.readBoolean()) {
                    return STREAM_CODEC.decode(buf);
                } else {
                    return null;
                }
            };
        }

        @Override
        public BiConsumer<RegistryFriendlyByteBuf, ShieldFilter<?>> getSerializer() {
            return (buf, info) -> {
                if (info == null) {
                    buf.writeBoolean(false);
                } else {
                    buf.writeBoolean(true);
                    STREAM_CODEC.encode(buf, info);
                }
            };
        }
    }

    MapCodec<T> getCodec();

    StreamCodec<FriendlyByteBuf, T> getStreamCodec();

    /// Return true if this entity matches the filter.
    boolean match(Entity entity);

    int getAction();

    void setAction(int action);

    String getFilterName();
}
