package mcjty.rftoolsbuilder.shapes;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

import java.util.Map;

/** Capture connected fluid surfaces against the scanned neighbours, including waterlogged blocks. */
final class ScanFluidRenderer {
    private ScanFluidRenderer() { }

    static void render(FluidRenderer renderer, BlockAndTintGetter world, BlockPos pos, BlockState state,
            Map<RenderType, ScanMesh.Builder> builders) {
        var fluid = state.getFluidState();
        if (fluid.isEmpty()) return;
        renderer.tesselate(world, pos, layer -> {
            var type = switch (layer) {
                case SOLID -> RenderTypes.solidMovingBlock();
                case CUTOUT -> RenderTypes.cutoutMovingBlock();
                case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
            };
            return inScanCoordinates(builders.computeIfAbsent(type, ScanMesh.Builder::new), pos);
        }, state, fluid);
    }

    static VertexConsumer inScanCoordinates(VertexConsumer buffer, BlockPos pos) {
        // FluidRenderer emits section-local vertices. Restore scan coordinates,
        // including negative coordinates and sections beyond the first 16 blocks.
        int x = pos.getX() & ~15, y = pos.getY() & ~15, z = pos.getZ() & ~15;
        return new VertexConsumerWrapper(buffer) {
            @Override
            public VertexConsumer addVertex(float vx, float vy, float vz) {
                return super.addVertex(vx + x, vy + y, vz + z);
            }
        };
    }
}
