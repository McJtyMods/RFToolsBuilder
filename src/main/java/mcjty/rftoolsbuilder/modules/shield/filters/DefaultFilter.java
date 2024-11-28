package mcjty.rftoolsbuilder.modules.shield.filters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

public class DefaultFilter extends AbstractShieldFilter<DefaultFilter> {

    public static final String ID = "default";

    public static final MapCodec<DefaultFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("action").forGetter(DefaultFilter::getAction))
            .apply(instance, DefaultFilter::new));
    public static final StreamCodec<FriendlyByteBuf, DefaultFilter> STREAM_CODEC = StreamCodec.of(
            (buf, settings) -> {
                buf.writeInt(settings.getAction());
            },
            buf -> new DefaultFilter(buf.readInt())
    );

    public DefaultFilter(int action) {
        super(action);
    }

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
