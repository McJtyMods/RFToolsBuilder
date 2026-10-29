package mcjty.rftoolsbuilder.modules.shield;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import mcjty.lib.network.StreamCodecs;
import java.util.HashMap;
import java.util.Map;

public enum DamageTypeMode implements StringRepresentable {
    DAMAGETYPE_GENERIC("Generic"),
    DAMAGETYPE_PLAYER("Player"),
    ;

    private static final Map<String,DamageTypeMode> modeToMode = new HashMap<>();

    private final String description;

    public static final Codec<DamageTypeMode> CODEC = StringRepresentable.fromEnum(DamageTypeMode::values);
    public static final StreamCodec<FriendlyByteBuf, DamageTypeMode> STREAM_CODEC = StreamCodecs.enumCodec(DamageTypeMode.class);

    DamageTypeMode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static DamageTypeMode getMode(String mode) {
        return modeToMode.get(mode);
    }

    static {
        for (DamageTypeMode mode : values()) {
            modeToMode.put(mode.description, mode);
        }
    }

    @Override
    public String getSerializedName() {
        return name();
    }
}
