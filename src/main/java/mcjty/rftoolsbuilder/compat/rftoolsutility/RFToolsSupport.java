package mcjty.rftoolsbuilder.compat.rftoolsutility;

import mcjty.rftoolsbase.api.screens.IScreenModuleRegistry;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleControlScreenModule;

import javax.annotation.Nullable;
import java.util.function.Function;

public class RFToolsSupport {

    public static class GetScreenModuleRegistry implements Function<IScreenModuleRegistry, Void> {
        @Nullable
        @Override
        public Void apply(IScreenModuleRegistry manager) {
            manager.registerModuleDataFactory(VehicleControlScreenModule.ModuleDataStacks.ID, VehicleControlScreenModule.ModuleDataStacks::new);
            return null;
        }
    }
}
