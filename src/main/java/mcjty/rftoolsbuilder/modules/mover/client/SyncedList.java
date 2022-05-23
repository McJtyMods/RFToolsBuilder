package mcjty.rftoolsbuilder.modules.mover.client;

import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.WidgetList;

import java.util.List;
import java.util.function.Function;

public class SyncedList<T> {

    private final WidgetList list;

    private List<T> fromServerList = null;
    private boolean needsRefresh = true;
    private int listDirty = 10;

    private final Runnable listRequester;
    private final Function<T, Panel> panelCreator;
    private final int refreshRate;

    /**
     * Use refreshRate equal to -1 to disable autorefresh
     */
    public SyncedList(WidgetList list, Runnable listRequester, Function<T, Panel> panelCreator, int refreshRate) {
        this.list = list;
        this.listRequester = listRequester;
        this.panelCreator = panelCreator;
        this.refreshRate = refreshRate;
    }

    public void setFromServerList(List<T> fromServerList) {
        this.fromServerList = fromServerList;
    }

    private void requestListIfNeeded() {
        if (refreshRate > 0 || fromServerList == null) {
            listDirty--;
            if (listDirty <= 0) {
                listRequester.run();
                listDirty = refreshRate;
            }
        }
    }

    public void refresh() {
        fromServerList = null;
        needsRefresh = true;
        listDirty = 3;
        requestListIfNeeded();
    }

    private boolean listReady() {
        return fromServerList != null;
    }

    public WidgetList getList() {
        return list;
    }

    public T getSelected() {
        int idx = list.getSelected();
        if (idx < 0 || idx >= list.getChildCount()) {
            return null;
        }
        return (T) list.getChild(idx).getUserObject();
    }

    public void select(T value) {
        for (int i = 0 ; i < list.getChildCount() ; i++) {
            if (list.getChild(i).getUserObject().equals(value)) {
                list.selected(i);
                return;
            }
        }
    }

    public void populateLists() {
        requestListIfNeeded();

        if (!listReady()) {
            return;
        }
        if (!needsRefresh) {
            return;
        }
        needsRefresh = false;

        list.removeChildren();
        int sel = list.getSelected();

        for (T vehicle : fromServerList) {
            Panel panel = panelCreator.apply(vehicle);
            panel.userObject(vehicle);
            list.children(panel);
        }

        list.selected(sel);
    }


}
