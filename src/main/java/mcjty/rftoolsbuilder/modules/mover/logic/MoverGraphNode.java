package mcjty.rftoolsbuilder.modules.mover.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.HashMap;
import java.util.Map;

public class MoverGraphNode {
    private final BlockPos pos;
    private final Map<Direction, BlockPos> children = new HashMap<>();

    public void add(Direction direction, BlockPos child) {
        children.put(direction, child);
    }

    public MoverGraphNode(BlockPos pos) {
        this.pos = pos;
    }

    public Map<Direction, BlockPos> getChildren() {
        return children;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void clear() {
        children.clear();
    }

    // Count number of nodes and children
    public int getNodes() {
        return 0;
    }
}
