package mcjty.rftoolsbuilder.modules.mover.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.rftoolsbuilder.modules.builder.blocks.RotateMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record VehicleBuilderData(RotateMode rotate) {

    public static final VehicleBuilderData DEFAULT = new VehicleBuilderData(RotateMode.ROTATE_0);

    public static final Codec<VehicleBuilderData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RotateMode.CODEC.fieldOf("rotate").forGetter(VehicleBuilderData::rotate)
    ).apply(instance, VehicleBuilderData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, VehicleBuilderData> STREAM_CODEC = StreamCodec.composite(
            RotateMode.STREAM_CODEC, VehicleBuilderData::rotate,
            VehicleBuilderData::new);

    public VehicleBuilderData withRotate(RotateMode rotate) {
        return new VehicleBuilderData(rotate);
    }
}
