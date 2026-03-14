package mcjty.rftoolsbuilder.shapes;

import mcjty.lib.varia.NBTTools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatePalette {

    private List<BlockState> palette = new ArrayList<>();
    private Map<BlockState, Integer> paletteIndex = new HashMap<>();

    public int alloc(BlockState state, int def) {
        if (state == null) {
            return def;
        }
        Integer index = paletteIndex.get(state);
        if (index != null) {
            return index;
        }
        int idx = palette.size();
        if (idx > 253) {
            // Overflow! Return first entry
            return 0;
        }
        palette.add(state);
        paletteIndex.put(state, idx);
        return idx;
    }

    public void add(BlockState state) {
        paletteIndex.put(state, palette.size());
        palette.add(state);
    }

    public List<BlockState> getPalette() {
        return palette;
    }

    public ListTag writeToNBT() {
        ListTag pal = new ListTag();
        for (BlockState state : palette) {
            pal.add(NbtUtils.writeBlockState(state));
        }
        return pal;
    }

    public static StatePalette readFromNBT(ListTag list) {
        StatePalette palette = new StatePalette();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tc = list.getCompound(i);
            palette.add(NBTTools.readBlockState(tc));
        }
        return palette;
    }

    public void writeToBuf(FriendlyByteBuf buf) {
        buf.writeVarInt(palette.size());
        for (BlockState state : palette) {
            buf.writeVarInt(Block.getId(state));
        }
    }

    public static StatePalette readFromBuf(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        StatePalette palette = new StatePalette();
        for (int i = 0; i < size; i++) {
            palette.add(Block.stateById(buf.readVarInt()));
        }
        return palette;
    }
}
