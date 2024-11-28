package mcjty.rftoolsbuilder.modules.shield.filters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class PlayerFilter extends AbstractShieldFilter<PlayerFilter> {

    public static final String ID = "player";
    private String name = null;

    public static final MapCodec<PlayerFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(PlayerFilter::getName),
            Codec.INT.fieldOf("action").forGetter(PlayerFilter::getAction))
            .apply(instance, PlayerFilter::new));
    public static final StreamCodec<FriendlyByteBuf, PlayerFilter> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PlayerFilter::getName,
            ByteBufCodecs.INT, PlayerFilter::getAction,
            PlayerFilter::new
    );

    public PlayerFilter(String name, int action) {
        super(action);
        this.name = name;
    }

    @Override
    public MapCodec getCodec() {
        return CODEC;
    }

    @Override
    public StreamCodec<FriendlyByteBuf, PlayerFilter> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public String getFilterName() {
        return ID;
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
}
