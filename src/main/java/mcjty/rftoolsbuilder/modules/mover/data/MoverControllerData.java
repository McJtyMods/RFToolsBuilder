package mcjty.rftoolsbuilder.modules.mover.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MoverControllerData(int offsetX, int offsetY, int offsetZ) {

    public static final MoverControllerData DEFAULT = new MoverControllerData(1, 1, 1);

    public static final Codec<MoverControllerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(MoverControllerData::offsetX),
            Codec.INT.fieldOf("y").forGetter(MoverControllerData::offsetY),
            Codec.INT.fieldOf("z").forGetter(MoverControllerData::offsetZ)
    ).apply(instance, MoverControllerData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MoverControllerData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, MoverControllerData::offsetX,
            ByteBufCodecs.INT, MoverControllerData::offsetY,
            ByteBufCodecs.INT, MoverControllerData::offsetZ,
            MoverControllerData::new);

    public MoverControllerData withOffset(int x, int y, int z) {
        return new MoverControllerData(x, y, z);
    }
}
