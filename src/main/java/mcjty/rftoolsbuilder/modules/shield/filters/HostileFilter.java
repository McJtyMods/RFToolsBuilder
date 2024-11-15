package mcjty.rftoolsbuilder.modules.shield.filters;

import com.mojang.serialization.MapCodec;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingBlock;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

public class HostileFilter extends AbstractShieldFilter<HostileFilter> {

    public static final String ID = "hostile";

    public static final MapCodec<HostileFilter> CODEC = MapCodec.unit(new HostileFilter());
    public static final StreamCodec<FriendlyByteBuf, HostileFilter> STREAM_CODEC = StreamCodec.of(
            (buf, settings) -> {},
            buf -> new HostileFilter()
    );

    @Override
    public MapCodec getCodec() {
        return CODEC;
    }

    @Override
    public StreamCodec<FriendlyByteBuf, HostileFilter> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public boolean match(Entity entity) {
        return ShieldingBlock.isHostile(entity);
    }

    @Override
    public String getFilterName() {
        return ID;
    }
}
