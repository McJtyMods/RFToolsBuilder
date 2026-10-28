package mcjty.rftoolsbuilder.modules.scanner.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public class DummyBlockGetter implements BlockAndTintGetter {

    private final RegistryAccess access;
    private final Map<BlockPos, BlockState> states;
    private final BlockState air;
    private final LevelLightEngine lightEngine;

    public DummyBlockGetter(RegistryAccess access, List<BlockEntry> blocks) {
        this.access = access;
        this.states = new HashMap<>();
        for (BlockEntry block : blocks) {
            states.put(block.pos(), block.state());
        }
        this.air = Blocks.AIR.defaultBlockState();
        this.lightEngine = new LevelLightEngine(new LightChunkGetter() {
            @Nullable
            @Override
            public LightChunk getChunkForLighting(int i, int i1) {
                return null;
            }

            @Override
            public BlockGetter getLevel() {
                return DummyBlockGetter.this;
            }
        }, true, true);
    }

    public DummyBlockGetter(RegistryAccess access, Map<BlockPos, BlockState> states) {
        this.access = access;
        this.states = new HashMap<>(states);
        this.air = Blocks.AIR.defaultBlockState();
        this.lightEngine = new LevelLightEngine(new LightChunkGetter() {
            @Nullable
            @Override
            public LightChunk getChunkForLighting(int i, int i1) {
                return null;
            }

            @Override
            public BlockGetter getLevel() {
                return DummyBlockGetter.this;
            }
        }, true, true);
    }

    @Override
    public CardinalLighting cardinalLighting() {
        return CardinalLighting.DEFAULT;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return lightEngine;
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver color) {
        var plains = access.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
        return color.getColor(plains.value(), pos.getX(), pos.getZ());
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        // Scans contain block states, not block entity data or a backing Level.
        // Synthetic entities have no level and can crash dynamic models that query
        // capabilities or other world-dependent data. Let models use their fallback.
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return states.getOrDefault(pos, air);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public int getHeight() {
        return 255; // @todo check?
    }

    @Override
    public int getMinY() {
        return 0;   // @todo check?
    }

    @Override
    public int getBrightness(LightLayer lightLayer, BlockPos pos) {
        return 15;
    }

    @Override
    public int getRawBrightness(BlockPos pos, int amount) {
        return 15;
    }

    @Override
    public boolean canSeeSky(BlockPos pos) {
        return true;
    }
}
