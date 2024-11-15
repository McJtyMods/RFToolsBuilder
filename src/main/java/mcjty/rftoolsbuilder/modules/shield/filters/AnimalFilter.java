package mcjty.rftoolsbuilder.modules.shield.filters;

import com.mojang.serialization.MapCodec;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingBlock;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

public class AnimalFilter extends AbstractShieldFilter<AnimalFilter> {

    public static final String ID = "animal";

    public static final MapCodec<AnimalFilter> CODEC = MapCodec.unit(new AnimalFilter());
    public static final StreamCodec<FriendlyByteBuf, AnimalFilter> STREAM_CODEC = StreamCodec.of(
            (buf, settings) -> {},
            buf -> new AnimalFilter()
    );

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
