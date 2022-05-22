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
    private final boolean autoRefresh;

    public SyncedList(WidgetList list, Runnable listRequester, Function<T, Panel> panelCreator, boolean autoRefresh) {
        this.list = list;
        this.listRequester = listRequester;
        this.panelCreator = panelCreator;
        this.autoRefresh = autoRefresh;
    }

    public void setFromServerList(List<T> fromServerList) {
        this.fromServerList = fromServerList;
    }

    private void requestListIfNeeded() {
        if (autoRefresh || fromServerList == null) {
            listDirty--;
            if (listDirty <= 0) {
                listRequester.run();
                listDirty = 10;
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
            list.children(panelCreator.apply(vehicle));
        }

        list.selected(sel);
    }


}
