package mcjty.rftoolsbuilder.modules.mover.logic;

import mcjty.lib.varia.OrientationTools;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

public class MoverGraphNode {
    private final Map<Direction, MoverGraphNode> children = new HashMap<>();

    public void add(Direction direction, MoverGraphNode child) {
        children.put(direction, child);
    }

    public Map<Direction, MoverGraphNode> getChildren() {
        return children;
    }

    public void clear() {
        children.clear();
    }

    // Count number of nodes and children
    public int getNodes() {
        return 0;
    }

    public void load(CompoundTag tag) {
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            if (tag.contains(direction.name())) {
                MoverGraphNode child = new MoverGraphNode();
                child.load(tag.getCompound(direction.name()));
                children.put(direction, child);
            }
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        for (var entry : children.entrySet()) {
            tag.put(entry.getKey().name(), entry.getValue().save());
        }
        return tag;
    }
}
