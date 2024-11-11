package mcjty.rftoolsbuilder.modules.builder.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.rftoolsbuilder.modules.builder.blocks.AnchorMode;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderMode;
import mcjty.rftoolsbuilder.modules.builder.blocks.RotateMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record BuilderData(String lastError, BuilderMode mode, AnchorMode anchor, RotateMode rotate, Flags flags,
                          BlockPos scan, BlockPos minBox, BlockPos maxBox) {

    public record Flags(boolean silent, boolean supportMode, boolean entityMode, boolean loopMode, boolean waitMode, boolean hilightMode) {}

    public static final Flags DEFAULT_FLAGS = new Flags(false, false, false, false, true, false);

    public static final Codec<Flags> FLAGS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("silent").forGetter(Flags::silent),
            Codec.BOOL.fieldOf("supportMode").forGetter(Flags::supportMode),
            Codec.BOOL.fieldOf("entityMode").forGetter(Flags::entityMode),
            Codec.BOOL.fieldOf("loopMode").forGetter(Flags::loopMode),
            Codec.BOOL.fieldOf("waitMode").forGetter(Flags::waitMode),
            Codec.BOOL.fieldOf("hilightMode").forGetter(Flags::hilightMode)
    ).apply(instance, Flags::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Flags> FLAGS_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, Flags::silent,
            ByteBufCodecs.BOOL, Flags::supportMode,
            ByteBufCodecs.BOOL, Flags::entityMode,
            ByteBufCodecs.BOOL, Flags::loopMode,
            ByteBufCodecs.BOOL, Flags::waitMode,
            ByteBufCodecs.BOOL, Flags::hilightMode,
            Flags::new
    );

    public static final BuilderData DEFAULT = new BuilderData(null, BuilderMode.MODE_COPY, AnchorMode.ANCHOR_SW, RotateMode.ROTATE_0,
            DEFAULT_FLAGS, null, null, null);

    public static final Codec<BuilderData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("lasterror").forGetter(data -> Optional.ofNullable(data.lastError())),
            BuilderMode.CODEC.fieldOf("mode").forGetter(BuilderData::mode),
            AnchorMode.CODEC.fieldOf("anchor").forGetter(BuilderData::anchor),
            RotateMode.CODEC.fieldOf("rotate").forGetter(BuilderData::rotate),
            FLAGS_CODEC.fieldOf("flags").forGetter(BuilderData::flags),
            BlockPos.CODEC.optionalFieldOf("scan").forGetter(data -> Optional.ofNullable(data.scan())),
            BlockPos.CODEC.optionalFieldOf("minBox").forGetter(data -> Optional.ofNullable(data.minBox())),
            BlockPos.CODEC.optionalFieldOf("maxBox").forGetter(data -> Optional.ofNullable(data.maxBox()))
    ).apply(instance, (lasterror, mode, anchor, rotate, flags, scan, minBox, maxBox) ->
            new BuilderData(lasterror.orElse(null), mode, anchor, rotate, flags, scan.orElse(null), minBox.orElse(null), maxBox.orElse(null))));

    public static final StreamCodec<RegistryFriendlyByteBuf, BuilderData> STREAM_CODEC = CompositeStreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), data -> Optional.ofNullable(data.lastError()),
            BuilderMode.STREAM_CODEC, BuilderData::mode,
            AnchorMode.STREAM_CODEC, BuilderData::anchor,
            RotateMode.STREAM_CODEC, BuilderData::rotate,
            FLAGS_STREAM_CODEC, BuilderData::flags,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), data -> Optional.ofNullable(data.scan()),
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), data -> Optional.ofNullable(data.minBox()),
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), data -> Optional.ofNullable(data.maxBox()),
            (lastError, mode, anchor, rotate, flags, scan, minBox, maxBox) -> new BuilderData(lastError.orElse(null), mode, anchor, rotate, flags, scan.orElse(null), minBox.orElse(null), maxBox.orElse(null))
    );

    public BuilderData withLastError(String lastError) {
        return new BuilderData(lastError, mode, anchor, rotate, flags, scan, minBox, maxBox);
    }

    public BuilderData withMode(BuilderMode mode) {
        return new BuilderData(lastError, mode, anchor, rotate, flags, scan, minBox, maxBox);
    }

    public BuilderData withAnchor(AnchorMode anchor) {
        return new BuilderData(lastError, mode, anchor, rotate, flags, scan, minBox, maxBox);
    }

    public BuilderData withRotate(RotateMode rotate) {
        return new BuilderData(lastError, mode, anchor, rotate, flags, scan, minBox, maxBox);
    }

    public BuilderData withSilent(boolean silent) {
        return new BuilderData(lastError, mode, anchor, rotate,
                new Flags(silent, flags.supportMode, flags.entityMode, flags.loopMode, flags.waitMode, flags.hilightMode),
                scan, minBox, maxBox);
    }

    public BuilderData withSupportMode(boolean supportMode) {
        return new BuilderData(lastError, mode, anchor, rotate,
                new Flags(flags.silent, supportMode, flags.entityMode, flags.loopMode, flags.waitMode, flags.hilightMode),
                scan, minBox, maxBox);
    }

    public BuilderData withEntityMode(boolean entityMode) {
        return new BuilderData(lastError, mode, anchor, rotate,
                new Flags(flags.silent, flags.supportMode, entityMode, flags.loopMode, flags.waitMode, flags.hilightMode),
                scan, minBox, maxBox);
    }

    public BuilderData withLoopMode(boolean loopMode) {
        return new BuilderData(lastError, mode, anchor, rotate,
                new Flags(flags.silent, flags.supportMode, flags.entityMode, loopMode, flags.waitMode, flags.hilightMode),
                scan, minBox, maxBox);
    }

    public BuilderData withWaitMode(boolean waitMode) {
        return new BuilderData(lastError, mode, anchor, rotate,
                new Flags(flags.silent, flags.supportMode, flags.entityMode, flags.loopMode, waitMode, flags.hilightMode),
                scan, minBox, maxBox);
    }

    public BuilderData withHilightMode(boolean hilightMode) {
        return new BuilderData(lastError, mode, anchor, rotate,
                new Flags(flags.silent, flags.supportMode, flags.entityMode, flags.loopMode, flags.waitMode, hilightMode),
                scan, minBox, maxBox);
    }

    public BuilderData withScan(BlockPos scan) {
        return new BuilderData(lastError, mode, anchor, rotate, flags, scan, minBox, maxBox);
    }

    public BuilderData withMinBox(BlockPos minBox) {
        return new BuilderData(lastError, mode, anchor, rotate, flags, scan, minBox, maxBox);
    }

    public BuilderData withMaxBox(BlockPos maxBox) {
        return new BuilderData(lastError, mode, anchor, rotate, flags, scan, minBox, maxBox);
    }
}
