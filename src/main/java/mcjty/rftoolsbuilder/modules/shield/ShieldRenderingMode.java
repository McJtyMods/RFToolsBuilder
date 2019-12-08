package mcjty.rftoolsbuilder.modules.shield;

import net.minecraft.util.IStringSerializable;

import java.util.HashMap;
import java.util.Map;

public enum ShieldRenderingMode implements IStringSerializable {
    INVISIBLE("Invisible"),
    SHIELD("Shield"),
    MIMIC("Mimic"),
    TRANSP("Transp"),
    SOLID("Solid"),
    ;

    private static final Map<String,ShieldRenderingMode> modeToMode = new HashMap<>();

    private final String description;

    ShieldRenderingMode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }



    public static ShieldRenderingMode getMode(String mode) {
        return modeToMode.get(mode);
    }

    static {
        for (ShieldRenderingMode mode : values()) {
            modeToMode.put(mode.description, mode);
        }
    }

    @Override
    public String getName() {
        return name().toLowerCase();
    }
}
