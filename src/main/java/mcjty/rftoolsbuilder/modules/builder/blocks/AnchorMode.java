package mcjty.rftoolsbuilder.modules.builder.blocks;

import mcjty.lib.varia.NamedEnum;

public enum AnchorMode implements NamedEnum<AnchorMode> {
    ANCHOR_SW("0"),
    ANCHOR_SE("90"),
    ANCHOR_NW("180"),
    ANCHOR_NE("270");

    private final String name;

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
}
