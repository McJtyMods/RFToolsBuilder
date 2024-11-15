package mcjty.rftoolsbuilder.modules.builder.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ChamberControllerData(int channel) {

    public static final ChamberControllerData DEFAULT = new ChamberControllerData(-1);

    public static final Codec<ChamberControllerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("channel").forGetter(ChamberControllerData::channel)
    ).apply(instance, ChamberControllerData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChamberControllerData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ChamberControllerData::channel,
            ChamberControllerData::new);

    public ChamberControllerData withChannel(int channel) {
        return new ChamberControllerData(channel);
    }
}
