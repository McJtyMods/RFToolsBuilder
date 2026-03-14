package mcjty.rftoolsbuilder.shapes;

import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/// Client side handling for shape data
public class ShapeDataManagerClient {

    // Client-side
    static final Map<ShapeID, RenderData> renderDataMap = new HashMap<>();
    private static final Queue<PendingRenderPlane> pendingRenderPlanes = new ArrayDeque<>();
    private static int cleanupCounter = 20;
    private static final int PLANES_PER_TICK = 2;

    private record PendingRenderPlane(ShapeID shapeID, @Nullable RenderData.RenderPlane plane, int offsetY, int dy, String msg) {
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

    public static synchronized void queueRenderPlane(ShapeID id, @Nullable RenderData.RenderPlane plane, int offsetY, int dy, String msg) {
        pendingRenderPlanes.add(new PendingRenderPlane(id, plane, offsetY, dy, msg));
    }

    public static synchronized void processPendingRenderPlanes(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        int cnt = PLANES_PER_TICK;
        while (cnt > 0 && !pendingRenderPlanes.isEmpty()) {
            PendingRenderPlane pending = pendingRenderPlanes.poll();
            if (pending != null) {
                ShapeRenderer.setRenderData(pending.shapeID(), pending.plane(), pending.offsetY(), pending.dy(), pending.msg());
                cnt--;
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
        pendingRenderPlanes.removeIf(pending -> toRemove.contains(pending.shapeID()));
    }
}
