package mcjty.rftoolsbuilder.modules.builder.blocks;

import mcjty.lib.varia.NamedEnum;

public enum BuilderMode implements NamedEnum<BuilderMode> {
    MODE_COPY("Copy"),
    MODE_MOVE("Move"),
    MODE_SWAP("Swap"),
    MODE_BACK("Back"),
    MODE_COLLECT("Collect");

    private final String name;

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
}
