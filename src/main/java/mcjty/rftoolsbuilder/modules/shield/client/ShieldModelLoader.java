package mcjty.rftoolsbuilder.modules.shield.client;

import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.level.block.state.BlockState;

/** Resolve cable geometry before Minecraft groups visually equivalent block states. */
public final class ShieldModelLoader {
    public static void register() {
        ModelLoadingPlugin.register(plugin -> {
            var model = new ShieldBakedModel();
            var root = new BlockStateModel.UnbakedRoot() {
                @Override public BlockStateModel bake(BlockState state, ModelBaker baker) { return model; }
                @Override public Object visualEqualityGroup(BlockState state) { return state; }
                @Override public void resolveDependencies(ResolvableModel.Resolver resolver) { }
            };
            for (var block : java.util.List.of(ShieldModule.SHIELDING_SOLID.get(), ShieldModule.SHIELDING_TRANSLUCENT.get(), ShieldModule.SHIELDING_CUTOUT.get())) {
                plugin.registerBlockStateResolver(block, context ->
                        context.block().getStateDefinition().getPossibleStates().forEach(state -> context.setModel(state, root)));
            }
        });
    }
}
