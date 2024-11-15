package mcjty.rftoolsbuilder.modules.shield.filters;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

public class DefaultFilter extends AbstractShieldFilter<DefaultFilter> {

    public static final String ID = "default";

    public static final MapCodec<DefaultFilter> CODEC = MapCodec.unit(new DefaultFilter());
    public static final StreamCodec<FriendlyByteBuf, DefaultFilter> STREAM_CODEC = StreamCodec.of(
            (buf, settings) -> {},
            buf -> new DefaultFilter()
    );

    @Override
    public MapCodec<DefaultFilter> getCodec() {
        return CODEC;
    }

    @Override
    public StreamCodec<FriendlyByteBuf, DefaultFilter> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public boolean match(Entity entity) {
        return true;
    }

    @Override
    public String getFilterName() {
        return ID;
    }
}
