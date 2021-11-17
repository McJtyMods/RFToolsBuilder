package mcjty.rftoolsbuilder.modules.shield;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public enum ShieldTexture implements IStringSerializable {
    SHIELD("Shield", "shield"),
    STRIPES("Rain", "shieldstripes")
    ;

    private static final Map<String, ShieldTexture> MODE_TO_MODE = new HashMap<>();

    private final String description;
    private final String path;

    ShieldTexture(String description, String path) {
        this.description = description;
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public String getPath() {
        return path;
    }

    public static ShieldTexture getMode(String mode) {
        return MODE_TO_MODE.get(mode);
    }

    static {
        for (ShieldTexture mode : values()) {
            MODE_TO_MODE.put(mode.description, mode);
        }
    }

    @Nonnull
    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
