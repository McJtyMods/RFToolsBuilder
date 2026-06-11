package mcjty.rftoolsbuilder.shapes;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatePalette {

    private List<BlockState> palette = new ArrayList<>();
    private Map<BlockState, Integer> paletteIndex = new HashMap<>();

    public static final StreamCodec<RegistryFriendlyByteBuf, StatePalette> OPTIONAL_STREAM_CODEC = StreamCodec.of(
            (buf, palette) -> {
                if (palette == null) {
                    buf.writeBoolean(false);
                } else {
                    buf.writeBoolean(true);
                    buf.writeVarInt(palette.palette.size());
                    for (BlockState state : palette.palette) {
                        ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY).encode(buf, state);
                    }
                }
            },
            buf -> {
                if (!buf.readBoolean()) {
                    return null;
                }
                StatePalette palette = new StatePalette();
                int size = buf.readVarInt();
                for (int i = 0; i < size; i++) {
                    BlockState state = ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY).decode(buf);
                    palette.add(state);
                }
                return palette;
            }
    );

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
}
