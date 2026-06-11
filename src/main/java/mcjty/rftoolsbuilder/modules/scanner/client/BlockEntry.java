package mcjty.rftoolsbuilder.modules.scanner.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

record BlockEntry(BlockPos pos, BlockState state) {
    public static BlockEntry of(BlockPos pos, BlockState state) {
        return new BlockEntry(pos, state);
    }

    // A builder for creating a list of Bl objects
    public static class Builder {
        private final List<BlockEntry> list = new ArrayList<>();

        public Builder add(BlockPos pos, BlockState state) {
            list.add(BlockEntry.of(pos, state));
            return this;
        }

        public List<BlockEntry> build() {
            return list;
        }
    }
}
