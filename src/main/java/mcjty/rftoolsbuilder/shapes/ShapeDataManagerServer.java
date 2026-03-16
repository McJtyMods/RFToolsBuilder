package mcjty.rftoolsbuilder.shapes;

import mcjty.lib.varia.RLE;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.scanner.ScannerConfiguration;
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
        private BlockPos dimension;
        private int checksum;
        private int maxOffsetY;
        private int nextOffsetY;

        public WorkUnit(ItemStack stack, BlockPos dimension, int maxOffsetY, IFormula formula, boolean optimizeRenderShell, int checksum, ServerPlayer player) {
            this.stack = stack;
            this.dimension = dimension;
            this.formula = formula;
            this.optimizeRenderShell = optimizeRenderShell;
            this.checksum = checksum;
            this.maxOffsetY = maxOffsetY;
            this.nextOffsetY = 0;
            this.players.add(player);
        }

        public void update(ItemStack stack, BlockPos dimension, int maxOffsetY, IFormula formula, boolean optimizeRenderShell, int checksum, ServerPlayer player) {
            this.stack = stack;
            this.dimension = dimension;
            this.formula = formula;
            this.optimizeRenderShell = optimizeRenderShell;
            this.checksum = checksum;
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

        public BlockPos getDimension() {
            return dimension;
        }

        public IFormula getFormula() {
            return formula;
        }

        public boolean isOptimizeRenderShell() {
            return optimizeRenderShell;
        }

        public int getChecksum() {
            return checksum;
        }

        public int getNextOffsetY() {
            return nextOffsetY;
        }

        public boolean isDuplicateRequest(int checksum, ServerPlayer player) {
            return this.checksum == checksum && players.contains(player);
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

    public static synchronized void pushWork(ShapeID shapeID, ItemStack stack, BlockPos dimension, int maxOffsetY, IFormula formula, boolean optimizeRenderShell, int checksum, ServerPlayer player) {
        WorkQueue queue = workQueues.get(shapeID);
        if (queue == null) {
            queue = new WorkQueue();
            workQueues.put(shapeID, queue);
        }
        if (queue.workUnit != null) {
            if (queue.workUnit.isDuplicateRequest(checksum, player)) {
                return;
            }
            queue.workUnit.update(stack, dimension, maxOffsetY, formula, optimizeRenderShell, checksum, player);
        } else {
            queue.workUnit = new WorkUnit(stack, dimension, maxOffsetY, formula, optimizeRenderShell, checksum, player);
        }
    }

    public static synchronized void handleWork(int tickInterval) {
        workQueues.entrySet().removeIf(entry -> entry.getValue().workUnit == null);

        long budgetPerRun = (long) Math.max(1, ScannerConfiguration.planeSurfacePerTick.get()) * Math.max(1, tickInterval);
        Iterator<Map.Entry<ShapeID, WorkQueue>> iterator = workQueues.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ShapeID, WorkQueue> entry = iterator.next();
            ShapeID shapeID = entry.getKey();
            WorkQueue queue = entry.getValue();
            WorkUnit unit = queue.workUnit;
            if (unit == null) {
                iterator.remove();
                continue;
            }

            long remainingBudget = budgetPerRun;
            while (unit != null) {
                BlockPos dimension = unit.getDimension();
                int offsetY = unit.getNextOffsetY();

                RLE positions = new RLE();
                StatePalette statePalette = new StatePalette();
                int cnt = ShapeCardItem.getRenderPositions(dimension, unit.isOptimizeRenderShell(), positions, statePalette, unit.getFormula(), offsetY);

                PacketReturnShapeData packet = PacketReturnShapeData.create(shapeID, unit.getChecksum(), positions, statePalette, dimension, cnt, offsetY, "");
                for (ServerPlayer player : unit.getPlayers()) {
                    RFToolsBuilderMessages.sendToPlayer(packet, player);
                }

                if (!unit.advance()) {
                    queue.workUnit = null;
                    break;
                }

                if (cnt > 0) {
                    remainingBudget -= Math.max(1L, (long) dimension.getX() * dimension.getZ());
                    if (remainingBudget <= 0) {
                        break;
                    }
                }

                unit = queue.workUnit;
            }

            if (queue.workUnit == null) {
                iterator.remove();
            }
        }
    }

}
