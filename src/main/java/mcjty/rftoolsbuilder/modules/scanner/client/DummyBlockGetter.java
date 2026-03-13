package mcjty.rftoolsbuilder.modules.scanner.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DummyBlockGetter implements BlockAndTintGetter {

    private final RegistryAccess access;
    private final transient Map<BlockPos, BlockEntity> teCache = new HashMap<>();
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
    public float getShade(Direction direction, boolean b) {
        return 1.0f;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return lightEngine;
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver color) {
        var plains = access.registryOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
        return color.getColor(plains, pos.getX(), pos.getZ());
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        BlockState state = getBlockState(pos);
        if (state.getBlock() instanceof EntityBlock) {
            return teCache.computeIfAbsent(pos.immutable(), p -> ((EntityBlock) state.getBlock()).newBlockEntity(pos, state));
        }
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
    public int getMinBuildHeight() {
        return 0;   // @todo check?
    }

    @Override
    public int getBrightness(net.minecraft.world.level.LightLayer lightLayer, BlockPos pos) {
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
