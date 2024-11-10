package mcjty.rftoolsbuilder.modules.builder.blocks;

import com.mojang.serialization.Codec;
import mcjty.lib.varia.NamedEnum;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public enum BuilderMode implements NamedEnum<BuilderMode> {
    MODE_COPY("Copy"),
    MODE_MOVE("Move"),
    MODE_SWAP("Swap"),
    MODE_BACK("Back"),
    MODE_COLLECT("Collect");

    private final String name;

    public static final Codec<BuilderMode> CODEC = StringRepresentable.fromEnum(BuilderMode::values);
    public static final StreamCodec<FriendlyByteBuf, BuilderMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(BuilderMode.class);

    BuilderMode(String name) {
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
        return name();
    }
}
