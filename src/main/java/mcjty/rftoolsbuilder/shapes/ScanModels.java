package mcjty.rftoolsbuilder.shapes;

import java.util.List;
import mcjty.lib.client.AbstractDynamicBakedModel;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public final class ScanModels {

    public static void collectParts(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random,
                                    List<BlockStateModelPart> parts) {
        var model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
        if (model instanceof AbstractDynamicBakedModel dynamic) {
            dynamic.collectParts(world, pos, state, random, parts);
        } else {
            var quads = new QuadCollection.Builder();
            var emitter = Renderer.get().quadEmitter(quad -> {
                var atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(quad.atlas().getId());
                var baked = quad.toBakedQuad(atlas.spriteFinder().find(quad));
                if (quad.cullFace() == null) quads.addUnculledFace(baked);
                else quads.addCulledFace(quad.cullFace(), baked);
            });
            ((FabricBlockStateModel) model).emitQuads(emitter, world, pos, state, random, direction -> false);
            parts.add(new SimpleModelWrapper(quads.build(), true, model.particleMaterial()));
        }
    }
}
