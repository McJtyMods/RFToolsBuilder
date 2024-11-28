package mcjty.rftoolsbuilder.modules.shield.filters;

import mcjty.lib.varia.Logging;

public abstract class AbstractShieldFilter<T extends ShieldFilter<?>> implements ShieldFilter<T> {
    private int action = ACTION_PASS;

    public AbstractShieldFilter(int action) {
        this.action = action;
    }

    @Override
    public int getAction() {
        return action;
    }

    @Override
    public T setAction(int action) {
        this.action = action;
        return (T) this;
    }

    public static ShieldFilter<?> createFilter(String type) {
        ShieldFilter<?> filter;
        // @todo: improve this if in a nicer manner
        if ("animal".equals(type)) {
            filter = new AnimalFilter(ACTION_PASS);
        } else if ("hostile".equals(type)) {
            filter = new HostileFilter(ACTION_PASS);
        } else if ("player".equals(type)) {
            filter = new PlayerFilter("", ACTION_PASS);
        } else if ("item".equals(type)) {
            filter = new ItemFilter(ACTION_PASS);
        } else if ("default".equals(type)) {
            filter = new DefaultFilter(ACTION_PASS);
        } else {
            Logging.log("Unknown filter type = " + type);
            filter = new DefaultFilter(ACTION_PASS);
        }
        return filter;
    }
}
