package mcjty.rftoolsbuilder.shapes;

import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;

/// Client side handling for shape data
public class ShapeDataManagerClient {

    // Client-side
    static final Map<ShapeID, RenderData> renderDataMap = new HashMap<>();
    private static final Map<PendingShapeKey, TreeMap<Integer, PendingRenderPlane>> pendingRenderPlanes = new LinkedHashMap<>();
    private static final Map<PendingShapeKey, Integer> nextOffsets = new HashMap<>();
    private static int cleanupCounter = 20;
    private static final int MIN_PLANES_PER_TICK = 4;
    private static final int MAX_PLANES_PER_TICK = 24;

    private record PendingShapeKey(ShapeID shapeID, int checksum) {
    }

    private record PendingRenderPlane(ShapeID shapeID, int checksum, @Nullable RenderData.RenderPlane plane, int offsetY, int dy, String msg) {
    }

    @Nullable
    public static RenderData getRenderData(ShapeID shapeID) {
        return renderDataMap.get(shapeID);
    }

    @Nonnull
    public static RenderData getRenderDataAndCreate(ShapeID shapeID) {
        RenderData data = renderDataMap.get(shapeID);
        if (data == null) {
            data = new RenderData();
            renderDataMap.put(shapeID, data);
        }
        return data;
    }

    public static synchronized void queueRenderPlane(ShapeID id, int checksum, @Nullable RenderData.RenderPlane plane, int offsetY, int dy, String msg) {
        PendingShapeKey key = new PendingShapeKey(id, checksum);
        pendingRenderPlanes.computeIfAbsent(key, k -> new TreeMap<>())
                .put(offsetY, new PendingRenderPlane(id, checksum, plane, offsetY, dy, msg));
    }

    public static synchronized void processPendingRenderPlanes(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        int queued = pendingRenderPlanes.values().stream().mapToInt(Map::size).sum();
        int cnt = Math.min(MAX_PLANES_PER_TICK, Math.max(MIN_PLANES_PER_TICK, queued / 8));
        while (cnt > 0 && !pendingRenderPlanes.isEmpty()) {
            boolean progressed = false;
            Iterator<Map.Entry<PendingShapeKey, TreeMap<Integer, PendingRenderPlane>>> iterator = pendingRenderPlanes.entrySet().iterator();
            while (cnt > 0 && iterator.hasNext()) {
                Map.Entry<PendingShapeKey, TreeMap<Integer, PendingRenderPlane>> entry = iterator.next();
                PendingShapeKey key = entry.getKey();
                TreeMap<Integer, PendingRenderPlane> planes = entry.getValue();
                int nextOffset = nextOffsets.getOrDefault(key, 0);
                PendingRenderPlane pending = planes.remove(nextOffset);
                if (pending == null) {
                    continue;
                }

                ShapeRenderer.setRenderData(pending.shapeID(), pending.checksum(), pending.plane(), pending.offsetY(), pending.dy(), pending.msg());
                progressed = true;
                cnt--;

                if (pending.offsetY() >= pending.dy() - 1) {
                    nextOffsets.remove(key);
                    if (planes.isEmpty()) {
                        iterator.remove();
                    }
                } else {
                    nextOffsets.put(key, nextOffset + 1);
                }
            }
            if (!progressed) {
                break;
            }
        }
    }

    // @todo 1.20 correct?
    public static synchronized void cleanupOldRenderers(RenderLevelStageEvent event) {
        // @todo 1.15 is this still the correct way?
        cleanupCounter--;
        if (cleanupCounter >= 0) {
            return;
        }
        cleanupCounter = 20;
        Set<ShapeID> toRemove = new HashSet<>();
        for (Map.Entry<ShapeID, RenderData> entry : renderDataMap.entrySet()) {
            if (entry.getValue().tooOld()) {
//                System.out.println("Removing id = " + entry.getKey());
                toRemove.add(entry.getKey());
            }
        }
        for (ShapeID id : toRemove) {
            RenderData data = renderDataMap.get(id);
            data.cleanup();
            renderDataMap.remove(id);
        }
        pendingRenderPlanes.entrySet().removeIf(entry -> toRemove.contains(entry.getKey().shapeID()));
        nextOffsets.keySet().removeIf(key -> toRemove.contains(key.shapeID()));
    }
}
