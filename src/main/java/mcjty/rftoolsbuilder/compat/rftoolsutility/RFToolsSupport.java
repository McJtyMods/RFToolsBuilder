package mcjty.rftoolsbuilder.compat.rftoolsutility;

import net.fabricmc.loader.api.FabricLoader;
import mcjty.rftoolsbase.api.screens.IScreenModuleRegistry;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleControlScreenModule;
import org.jetbrains.annotations.Nullable;
import java.util.function.Function;

public class RFToolsSupport {

    public static void register() {
        FabricLoader.getInstance().getEntrypoints("rftoolsutility:screen_module_registry", IScreenModuleRegistry.class)
                .forEach(registry -> new GetScreenModuleRegistry().apply(registry));
    }

    public static class GetScreenModuleRegistry implements Function<IScreenModuleRegistry, Void> {
        @Nullable
        @Override
        public Void apply(IScreenModuleRegistry manager) {
            manager.registerModuleDataFactory(VehicleControlScreenModule.EmptyData.ID, VehicleControlScreenModule.EmptyData::new);
            return null;
        }
    }
}
