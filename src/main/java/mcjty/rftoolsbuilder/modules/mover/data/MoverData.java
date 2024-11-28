package mcjty.rftoolsbuilder.modules.mover.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.CompositeStreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MoverData(String name, boolean down, boolean up, boolean north, boolean south, boolean west, boolean east) {

    public static final MoverData DEFAULT = new MoverData("", true, true, true, true, true, true);

    public static final Codec<MoverData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(MoverData::name),
            Codec.BOOL.fieldOf("down").forGetter(MoverData::down),
            Codec.BOOL.fieldOf("up").forGetter(MoverData::up),
            Codec.BOOL.fieldOf("north").forGetter(MoverData::north),
            Codec.BOOL.fieldOf("south").forGetter(MoverData::south),
            Codec.BOOL.fieldOf("west").forGetter(MoverData::west),
            Codec.BOOL.fieldOf("east").forGetter(MoverData::east)
    ).apply(instance, MoverData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MoverData> STREAM_CODEC = CompositeStreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, MoverData::name,
            ByteBufCodecs.BOOL, MoverData::down,
            ByteBufCodecs.BOOL, MoverData::up,
            ByteBufCodecs.BOOL, MoverData::north,
            ByteBufCodecs.BOOL, MoverData::south,
            ByteBufCodecs.BOOL, MoverData::west,
            ByteBufCodecs.BOOL, MoverData::east,
            MoverData::new);

    public MoverData withName(String name) {
        return new MoverData(name, down, up, north, south, west, east);
    }

    public MoverData withDirections(boolean down, boolean up, boolean north, boolean south, boolean west, boolean east) {
        return new MoverData(name, down, up, north, south, west, east);
    }
}
