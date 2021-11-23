package mcjty.rftoolsbuilder.modules.builder.blocks;

import mcjty.lib.varia.NamedEnum;

public enum RotateMode implements NamedEnum<RotateMode> {
    ROTATE_0("0"),
    ROTATE_90("90"),
    ROTATE_180("180"),
    ROTATE_270("270");

    private final String name;

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
}
