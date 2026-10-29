package mcjty.rftoolsbuilder.setup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

/** Non-persistent tickets shared by builders working in the same chunk. */
public final class BuilderChunkTickets {
    private static final TicketType TYPE = Registry.register(BuiltInRegistries.TICKET_TYPE,
            Identifier.fromNamespaceAndPath("rftoolsbuilder", "builder"), new TicketType(0, TicketType.FLAG_LOADING));
    private static final Map<ServerLevel, Map<ChunkPos, Set<BlockPos>>> OWNERS = new WeakHashMap<>();

    public static void init() {
    }

    public static void acquire(ServerLevel level, ChunkPos chunk, BlockPos owner) {
        var owners = OWNERS.computeIfAbsent(level, key -> new HashMap<>()).computeIfAbsent(chunk, key -> new HashSet<>());
        if (owners.add(owner.immutable()) && owners.size() == 1) {
            level.getChunkSource().addTicketWithRadius(TYPE, chunk, 0);
        }
        level.getChunk(chunk.x(), chunk.z());
    }

    public static void release(ServerLevel level, ChunkPos chunk, BlockPos owner) {
        var chunks = OWNERS.get(level);
        if (chunks == null) return;
        var owners = chunks.get(chunk);
        if (owners != null && owners.remove(owner) && owners.isEmpty()) {
            level.getChunkSource().removeTicketWithRadius(TYPE, chunk, 0);
            chunks.remove(chunk);
        }
    }
}
