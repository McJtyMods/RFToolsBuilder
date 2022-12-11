package mcjty.rftoolsbuilder.modules.mover.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.HashMap;
import java.util.Map;

public record MoverGraphNode(BlockPos pos, Map<Direction, BlockPos> children) {

    public void add(Direction direction, BlockPos child) {
        children.put(direction, child);
    }

    public MoverGraphNode(BlockPos pos) {
        this(pos, new HashMap<>());
    }

    public void clear() {
        children.clear();
    }

    // Count number of nodes and children
    public int getNodes() {
        return 0;
    }
}
