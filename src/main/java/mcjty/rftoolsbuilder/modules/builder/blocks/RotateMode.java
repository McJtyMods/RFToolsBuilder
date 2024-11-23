package mcjty.rftoolsbuilder.modules.builder.blocks;

import com.mojang.serialization.Codec;
import mcjty.lib.varia.NamedEnum;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public enum RotateMode implements NamedEnum<RotateMode> {
    ROTATE_0("0"),
    ROTATE_90("90"),
    ROTATE_180("180"),
    ROTATE_270("270");

    private final String name;

    public static final Codec<RotateMode> CODEC = StringRepresentable.fromEnum(RotateMode::values);
    public static final StreamCodec<FriendlyByteBuf, RotateMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(RotateMode.class);

    RotateMode(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String[] getDescription() {
        return new String[] { name };
    }


    @Override
    public String getSerializedName() {
        return getName();
    }
}
