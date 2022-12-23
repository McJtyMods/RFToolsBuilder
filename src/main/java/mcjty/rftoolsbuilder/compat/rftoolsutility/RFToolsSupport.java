package mcjty.rftoolsbuilder.compat.rftoolsutility;

import mcjty.rftoolsbase.api.screens.IScreenModuleRegistry;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleControlScreenModule;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleStatusScreenModule;

import javax.annotation.Nullable;
import java.util.function.Function;

public class RFToolsSupport {

    public static class GetScreenModuleRegistry implements Function<IScreenModuleRegistry, Void> {
        @Nullable
        @Override
        public Void apply(IScreenModuleRegistry manager) {
            manager.registerModuleDataFactory(VehicleControlScreenModule.EmptyData.ID, VehicleControlScreenModule.EmptyData::new);
            return null;
        }
    }
}
