package mcjty.rftoolsbuilder.modules.builder.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolsbuilder.modules.builder.blocks.AnchorMode;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record BuilderData(String lastError, BuilderMode mode, AnchorMode anchor) {

    public static final BuilderData DEFAULT = new BuilderData(null, BuilderMode.MODE_COPY, AnchorMode.ANCHOR_SW);

    public static final Codec<BuilderData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("lasterror").forGetter(data -> Optional.ofNullable(data.lastError())),
            BuilderMode.CODEC.fieldOf("mode").forGetter(data -> data.mode()),
            AnchorMode.CODEC.fieldOf("anchor").forGetter(data -> data.anchor())
    ).apply(instance, (lasterror, mode, anchor) -> new BuilderData(lasterror.orElse(null), mode, anchor)));

    public static final StreamCodec<RegistryFriendlyByteBuf, BuilderData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), data -> Optional.ofNullable(data.lastError()),
            BuilderMode.STREAM_CODEC, data -> data.mode(),
            AnchorMode.STREAM_CODEC, data -> data.anchor(),
            (lastError, mode, anchor) -> new BuilderData(lastError.orElse(null), mode, anchor)
    );

    public BuilderData withLastError(String lastError) {
        return new BuilderData(lastError, mode, anchor);
    }

    public BuilderData withMode(BuilderMode mode) {
        return new BuilderData(lastError, mode, anchor);
    }

    public BuilderData withAnchor(AnchorMode anchor) {
        return new BuilderData(lastError, mode, anchor);
    }
}
