package mcjty.rftoolsbuilder.modules.shield;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public enum ShieldRenderingMode implements IStringSerializable {
    INVISIBLE("Invisible", true),
    SHIELD("Shield", true),
    MIMIC("Mimic", false),  // @todo translucent mimic?
    TRANSP("Transp", true),
    SOLID("Solid", false),
    ;

    private static final Map<String,ShieldRenderingMode> MODE_TO_MODE = new HashMap<>();

    private final String description;
    private final boolean translucent;

    ShieldRenderingMode(String description, boolean translucent) {
        this.description = description;
        this.translucent = translucent;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTranslucent() {
        return translucent;
    }

    public static ShieldRenderingMode getMode(String mode) {
        return MODE_TO_MODE.get(mode);
    }

    static {
        for (ShieldRenderingMode mode : values()) {
            MODE_TO_MODE.put(mode.description, mode);
        }
    }

    @Nonnull
    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
