package mcjty.rftoolsbuilder.shapes;

import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.ToggleButton;
import net.minecraft.client.gui.screen.Screen;

public class ShapeGuiTools {

    public static ToggleButton createAxisButton(Screen gui, Panel toplevel, int x, int y) {
        ToggleButton showAxis = new ToggleButton().checkMarker(true)
                .tooltips("Enable axis rendering", "in the preview")
                .text("A").hint(x, y, 24, 16);
        showAxis.pressed(true);
        toplevel.children(showAxis);
        return showAxis;
    }

    public static ToggleButton createBoxButton(Screen gui, Panel toplevel, int x, int y) {
        ToggleButton showAxis = new ToggleButton().checkMarker(true)
                .tooltips("Enable preview of the", "outer bounds")
                .text("B").hint(x, y, 24, 16);
        showAxis.pressed(true);
        toplevel.children(showAxis);
        return showAxis;
    }

    public static ToggleButton createScanButton(Screen gui, Panel toplevel, int x, int y) {
        ToggleButton showAxis = new ToggleButton().checkMarker(true)
                .tooltips("Show a visual scanline", "wherever the preview", "is updated")
                .text("S").hint(x, y, 24, 16);
        showAxis.pressed(true);
        toplevel.children(showAxis);
        return showAxis;
    }
}
