package mcjty.rftoolsbuilder.modules.shield.filters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingBlock;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

public class AnimalFilter extends AbstractShieldFilter<AnimalFilter> {

    public static final String ID = "animal";

    public static final MapCodec<AnimalFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("action").forGetter(AnimalFilter::getAction))
            .apply(instance, AnimalFilter::new));
    public static final StreamCodec<FriendlyByteBuf, AnimalFilter> STREAM_CODEC = StreamCodec.of(
            (buf, settings) -> {
                buf.writeInt(settings.getAction());
            },
            buf -> new AnimalFilter(buf.readInt())
    );

    public AnimalFilter(int action) {
        super(action);
    }

    @Override
    public MapCodec<AnimalFilter> getCodec() {
        return CODEC;
    }

    @Override
    public StreamCodec<FriendlyByteBuf, AnimalFilter> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public boolean match(Entity entity) {
        return ShieldingBlock.isPassive(entity);
    }

    @Override
    public String getFilterName() {
        return ID;
    }
}
