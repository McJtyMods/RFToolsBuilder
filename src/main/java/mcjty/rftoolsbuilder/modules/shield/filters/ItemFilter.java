package mcjty.rftoolsbuilder.modules.shield.filters;

import com.mojang.serialization.MapCodec;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingBlock;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

public class ItemFilter extends AbstractShieldFilter<ItemFilter> {

    public static final String ID = "item";

    public static final MapCodec<ItemFilter> CODEC = MapCodec.unit(new ItemFilter());
    public static final StreamCodec<FriendlyByteBuf, ItemFilter> STREAM_CODEC = StreamCodec.of(
            (buf, settings) -> {},
            buf -> new ItemFilter()
    );

    @Override
    public MapCodec getCodec() {
        return CODEC;
    }

    @Override
    public StreamCodec<FriendlyByteBuf, ItemFilter> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public boolean match(Entity entity) {
        return ShieldingBlock.isItem(entity);
    }

    @Override
    public String getFilterName() {
        return ID;
    }
}
