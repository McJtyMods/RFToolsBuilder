package mcjty.rftoolsbuilder.shapes;

import mcjty.lib.varia.RLE;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.scanner.network.PacketReturnShapeData;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/// Server side handling for shape data
public class ShapeDataManagerServer {


    private static class WorkUnit {
        private final List<ServerPlayer> players = new ArrayList<>();
        private ItemStack stack;
        private IFormula formula;
        private boolean optimizeRenderShell;
        private int maxOffsetY;
        private int nextOffsetY;

        public WorkUnit(ItemStack stack, int maxOffsetY, IFormula formula, boolean optimizeRenderShell, ServerPlayer player) {
            this.stack = stack;
            this.formula = formula;
            this.optimizeRenderShell = optimizeRenderShell;
            this.maxOffsetY = maxOffsetY;
            this.nextOffsetY = 0;
            this.players.add(player);
        }

        public void update(ItemStack stack, int maxOffsetY, IFormula formula, boolean optimizeRenderShell, ServerPlayer player) {
            this.stack = stack;
            this.formula = formula;
            this.optimizeRenderShell = optimizeRenderShell;
            this.maxOffsetY = maxOffsetY;
            this.nextOffsetY = 0;
            if (!players.contains(player)) {
                players.add(player);
            }
        }

        public List<ServerPlayer> getPlayers() {
            return players;
        }

        public ItemStack getStack() {
            return stack;
        }

        public IFormula getFormula() {
            return formula;
        }

        public boolean isOptimizeRenderShell() {
            return optimizeRenderShell;
        }

        public int getNextOffsetY() {
            return nextOffsetY;
        }

        public boolean advance() {
            nextOffsetY++;
            return nextOffsetY < maxOffsetY;
        }
    }

    private static class WorkQueue {
        private WorkUnit workUnit;
    }

    // Server-side
    private static final Map<ShapeID, WorkQueue> workQueues = new HashMap<>();

    public static synchronized void pushWork(ShapeID shapeID, ItemStack stack, int maxOffsetY, IFormula formula, boolean optimizeRenderShell, ServerPlayer player) {
        WorkQueue queue = workQueues.get(shapeID);
        if (queue == null) {
            queue = new WorkQueue();
            workQueues.put(shapeID, queue);
        }
        if (queue.workUnit != null) {
            queue.workUnit.update(stack, maxOffsetY, formula, optimizeRenderShell, player);
        } else {
            queue.workUnit = new WorkUnit(stack, maxOffsetY, formula, optimizeRenderShell, player);
        }
    }

    public static synchronized void handleWork() {
        Set<ShapeID> toRemove = new HashSet<>();
        for (Map.Entry<ShapeID, WorkQueue> entry : workQueues.entrySet()) {
            ShapeID shapeID = entry.getKey();
            WorkQueue queue = entry.getValue();

            if (queue.workUnit != null) {
                WorkUnit unit = queue.workUnit;
                ItemStack card = unit.getStack();
                BlockPos dimension = ShapeCardItem.getDimension(card);
                int offsetY = unit.getNextOffsetY();

                RLE positions = new RLE();
                StatePalette statePalette = new StatePalette();
                int cnt = ShapeCardItem.getRenderPositions(card, unit.isOptimizeRenderShell(), positions, statePalette, unit.getFormula(), offsetY);

                PacketReturnShapeData packet = PacketReturnShapeData.create(shapeID, positions, statePalette, dimension, cnt, offsetY, "");
                for (ServerPlayer player : unit.getPlayers()) {
                    RFToolsBuilderMessages.sendToPlayer(packet, player);
                }

                if (!unit.advance()) {
                    queue.workUnit = null;
                }
            }
            if (queue.workUnit == null) {
                toRemove.add(shapeID);
            }
        }
        for (ShapeID id : toRemove) {
            workQueues.remove(id);
        }

    }

}
