package mcjty.rftoolsbuilder.modules.shield.filters;

import mcjty.lib.varia.Logging;

public abstract class AbstractShieldFilter<T extends ShieldFilter<?>> implements ShieldFilter<T> {
    private int action = ACTION_PASS;

    @Override
    public int getAction() {
        return action;
    }

    @Override
    public void setAction(int action) {
        this.action = action;
    }

    public static ShieldFilter<?> createFilter(String type) {
        ShieldFilter<?> filter;
        // @todo: improve this if in a nicer manner
        if ("animal".equals(type)) {
            filter = new AnimalFilter();
        } else if ("hostile".equals(type)) {
            filter = new HostileFilter();
        } else if ("player".equals(type)) {
            filter = new PlayerFilter("");
        } else if ("item".equals(type)) {
            filter = new ItemFilter();
        } else if ("default".equals(type)) {
            filter = new DefaultFilter();
        } else {
            Logging.log("Unknown filter type = " + type);
            filter = new DefaultFilter();
        }
        return filter;
    }
}
