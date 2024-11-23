package mcjty.rftoolsbuilder.modules.builder.blocks;

import com.mojang.serialization.Codec;
import mcjty.lib.varia.NamedEnum;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public enum AnchorMode implements NamedEnum<AnchorMode> {
    ANCHOR_SW("0"),
    ANCHOR_SE("90"),
    ANCHOR_NW("180"),
    ANCHOR_NE("270");

    private final String name;

    public static final Codec<AnchorMode> CODEC = StringRepresentable.fromEnum(AnchorMode::values);
    public static final StreamCodec<FriendlyByteBuf, AnchorMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(AnchorMode.class);

    AnchorMode(String name) {
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
