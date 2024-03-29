package mcjty.rftoolsbuilder.shapes;

import mcjty.lib.varia.RLE;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.scanner.ScannerConfiguration;
import mcjty.rftoolsbuilder.modules.scanner.network.PacketReturnShapeData;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;

import java.util.*;

/// Server side handling for shape data
public class ShapeDataManagerServer {


    private static class WorkUnit {
        private final List<ServerPlayerEntity> players = new ArrayList<>();
        private ItemStack stack;
        private int offsetY;
        private IFormula formula;

        public WorkUnit(ItemStack stack, int offsetY, IFormula formula, ServerPlayerEntity player) {
            this.stack = stack;
            this.offsetY = offsetY;
            this.formula = formula;
            this.players.add(player);
        }

        public void update(ItemStack stack, int offsetY, IFormula formula, ServerPlayerEntity player) {
            this.stack = stack;
            this.offsetY = offsetY;
            this.formula = formula;
            if (!players.contains(player)) {
                players.add(player);
            }
        }

        public List<ServerPlayerEntity> getPlayers() {
            return players;
        }

        public ItemStack getStack() {
            return stack;
        }

        public int getOffsetY() {
            return offsetY;
        }

        public IFormula getFormula() {
            return formula;
        }
    }

    private static class WorkQueue {
        private final ArrayDeque<WorkUnit> workQueue = new ArrayDeque<>();
        private final Map<Integer, WorkUnit> workingOn = new HashMap<>();
    }

    // Server-side
    private static final Map<ShapeID, WorkQueue> workQueues = new HashMap<>();

    public static void pushWork(ShapeID shapeID, ItemStack stack, int offsetY, IFormula formula, ServerPlayerEntity player) {
        WorkQueue queue = workQueues.get(shapeID);
        if (queue == null) {
            queue = new WorkQueue();
            workQueues.put(shapeID, queue);
        }
        if (queue.workingOn.containsKey(offsetY)) {
            queue.workingOn.get(offsetY).update(stack, offsetY, formula, player);
        } else {
            WorkUnit unit = new WorkUnit(stack, offsetY, formula, player);
            queue.workQueue.addLast(unit);
            queue.workingOn.put(offsetY, unit);
        }
    }

    public static void handleWork() {
        Set<ShapeID> toRemove = new HashSet<>();
        for (Map.Entry<ShapeID, WorkQueue> entry : workQueues.entrySet()) {
            ShapeID shapeID = entry.getKey();
            WorkQueue queue = entry.getValue();

            int pertick = ScannerConfiguration.planeSurfacePerTick.get();
            while (!queue.workQueue.isEmpty()) {
                WorkUnit unit = queue.workQueue.removeFirst();
                queue.workingOn.remove(unit.getOffsetY());

                ItemStack card = unit.getStack();
                boolean solid = ShapeCardItem.isSolid(card);
                BlockPos dimension = ShapeCardItem.getDimension(card);

                RLE positions = new RLE();
                StatePalette statePalette = new StatePalette();
                int cnt = ShapeCardItem.getRenderPositions(card, solid, positions, statePalette, unit.getFormula(), unit.getOffsetY());

                for (ServerPlayerEntity player : unit.getPlayers()) {
                    RFToolsBuilderMessages.INSTANCE.sendTo(new PacketReturnShapeData(shapeID, positions, statePalette, dimension, cnt, unit.getOffsetY(), ""),
                            player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                }
                if (cnt > 0) {
                    pertick -= dimension.getX() * dimension.getZ();
                    if (pertick <= 0) {
                        break;
                    }
                }
            }
            if (queue.workQueue.isEmpty()) {
                toRemove.add(shapeID);
            }
        }
        for (ShapeID id : toRemove) {
            workQueues.remove(id);
        }

    }

}
