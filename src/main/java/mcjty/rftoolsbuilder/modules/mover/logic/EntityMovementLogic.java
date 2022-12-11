package mcjty.rftoolsbuilder.modules.mover.logic;

import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleCard;
import mcjty.rftoolsbuilder.modules.mover.network.PacketGrabbedEntitiesToClient;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

public class EntityMovementLogic {

    private final MoverTileEntity mover;

    // If this is the source of a movement then this refers to the destination. Otherwise null. Synced to client
    private BlockPos destination = null;
    // For the source of the movement, this refers to the game tick when the movement started. Synced to client
    private long starttick;
    // For the source of the movement, the total distance to travel. Synced to client
    private float totalDist;

    // If this is the destination of a movement then this refers to the source. Otherwise null
    private BlockPos source = null;

    // All 'grabbed' entities during movement (key is entity id)
    private Map<Integer, Vec3> grabbedEntities = new HashMap<>();

    public EntityMovementLogic(MoverTileEntity mover) {
        this.mover = mover;
    }

    private final Random random = new Random();

    public void setGrabbedEntitiesClient(Set<Integer> grabbedEntities) {
        Level level = mover.getLevel();
        this.grabbedEntities = new HashMap<>();
        System.out.println("grabbedEntities.size() from server = " + grabbedEntities.size());
        for (Integer id : grabbedEntities) {
            Entity entity = level.getEntity(id);
            if (entity != null) {
                this.grabbedEntities.put(id, entity.position());
            }
        }
    }


    // Return the actual offset moved for the current player. Only if the player is actually on the platform
    public Vec3 tryMoveVehicleThisPlayer(float partialTicks) {
        Vec3 result = Vec3.ZERO;
        if (destination != null) {
            Player clientPlayer = SafeClientTools.getClientPlayer();
            Level level = mover.getLevel();
            Vec3 startPos = getMovingPosition(0, starttick);
            Vec3 currentPos = getMovingPosition(partialTicks, level.getGameTime());
            double dx = currentPos.x - startPos.x;
            double dy = currentPos.y - startPos.y;
            double dz = currentPos.z - startPos.z;
            if (grabbedEntities.containsKey(clientPlayer.getId())) {
                Vec3 basePos = grabbedEntities.get(clientPlayer.getId());
                double desiredX = basePos.x + dx;
                double desiredY = basePos.y + dy;
                double desiredZ = basePos.z + dz;
//                    desiredX = (desiredX + entity.getX()*3) / 4.0;
//                    desiredY = (desiredY + entity.getY()*3) / 4.0;
//                    desiredZ = (desiredZ + entity.getZ()*3) / 4.0;
                result = new Vec3(desiredX - clientPlayer.getX(), desiredY - clientPlayer.getY(), desiredZ - clientPlayer.getZ());
                clientPlayer.setPos(desiredX, desiredY, desiredZ);
                clientPlayer.setOldPosAndRot();
                clientPlayer.fallDistance = 0;
                clientPlayer.setDeltaMovement(Vec3.ZERO);
                clientPlayer.setOnGround(true);
            }
        }
        return result;
    }

    // Move all entities except for the current player
    public void tryMoveVehicleClientEntities(float partialTicks) {
        if (destination != null) {
            Player clientPlayer = SafeClientTools.getClientPlayer();
            Level level = mover.getLevel();
            Vec3 startPos = getMovingPosition(0, starttick);
            Vec3 currentPos = getMovingPosition(partialTicks, level.getGameTime());
            double dx = currentPos.x - startPos.x;
            double dy = currentPos.y - startPos.y;
            double dz = currentPos.z - startPos.z;
            for (var pair : grabbedEntities.entrySet()) {
                Entity entity = level.getEntity(pair.getKey());
                if (entity != null && entity != clientPlayer) {
                    Vec3 basePos = pair.getValue();
                    double desiredX = basePos.x + dx;
                    double desiredY = basePos.y + dy;
                    double desiredZ = basePos.z + dz;
//                    desiredX = (desiredX + entity.getX()*3) / 4.0;
//                    desiredY = (desiredY + entity.getY()*3) / 4.0;
//                    desiredZ = (desiredZ + entity.getZ()*3) / 4.0;
                    entity.setPos(desiredX, desiredY, desiredZ);
                    entity.setOldPosAndRot();
                    entity.fallDistance = 0;
                    entity.setDeltaMovement(Vec3.ZERO);
                    entity.setOnGround(true);
                }
            }
        }
    }

    public void tryMoveVehicleServer() {
        ItemStack vehicle = mover.getCard();
        if (destination != null) {
            // We are moving
            checkUnmounts();
            actualMoveServer(vehicle);
//        } else if (!mover.getNetwork().isEmpty()) {
//            if (mover.isMachineEnabled() && !vehicle.isEmpty()) {
//                actualStartMovementServer();
//            }
        } else if (!vehicle.isEmpty()) {
            // Check if the vehicle card here wants to go somewhere
            BlockPos destination = VehicleCard.getDesiredDestination(vehicle);
            if (destination != null) {
                if (destination.equals(mover.getBlockPos())) {
                    // We have arrived!
                    VehicleCard.clearDesiredDestination(vehicle);
                    return;
                }

                if (mover.hasDirectContectionTo(destination)) {
                    // We have a direct connection
                    setupMovementTo(destination);
                } else {
                    // We need to find the destination
                    for (Map.Entry<Direction, BlockPos> entry : mover.getNetwork().entrySet()) {
                        if (mover.getLevel().getBlockEntity(entry.getValue()) instanceof MoverTileEntity destMover) {
                            if (destMover.hasDirectContectionTo(destination)) {
                                setupMovementTo(entry.getValue());
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkUnmounts() {
        if (!MoverTileEntity.wantUnmount.isEmpty()) {
            Set<Integer> toRemove = new HashSet<>();
            for (Integer id : MoverTileEntity.wantUnmount) {
                if (grabbedEntities.containsKey(id)) {
                    toRemove.add(id);
                    grabbedEntities.remove(id);
                }
            }
            if (!toRemove.isEmpty()) {
                MoverTileEntity.wantUnmount.removeAll(toRemove);
                syncGrabbedToClient();
            }
        }
    }

    public void setupMovementTo(BlockPos dest) {
        Level level = mover.getLevel();
        if (level.getBlockEntity(dest) instanceof MoverTileEntity destMover) {
            if (destMover.isAvailable()) {
                BlockPos worldPosition = mover.getBlockPos();
                destMover.setSource(worldPosition);
                destination = dest;
                totalDist = (float) Math.sqrt(worldPosition.distToCenterSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()));
                starttick = level.getGameTime();
                grabEntities();
                mover.markDirtyClient();
            }
        }
    }

    private void grabEntities() {
        grabbedEntities.clear();
        AABB aabb = getVehicleAABB();
        Level level = mover.getLevel();
        for (Entity entity : level.getEntitiesOfClass(Entity.class, aabb)) {
            grabbedEntities.put(entity.getId(), entity.position());
            MoverTileEntity.wantUnmount.remove(entity.getId());
        }

        // @todo Note resend grabbed entities for new players that come into range?
        System.out.println("grabbedEntities.size() in grabEntities = " + grabbedEntities.size());
        syncGrabbedToClient();
    }

    public void syncGrabbedToClient() {
        Level level = mover.getLevel();
        BlockPos worldPosition = mover.getBlockPos();
        PacketGrabbedEntitiesToClient packet = new PacketGrabbedEntitiesToClient(worldPosition, grabbedEntities.keySet());
        ChunkPos cp = new ChunkPos(worldPosition);
        RFToolsBuilderMessages.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunk(cp.x, cp.z)), packet);
    }

    private AABB getVehicleAABB() {
        BlockPos blockPos = mover.getBlockPos();
        Map<BlockState, List<BlockPos>> blocks = VehicleCard.getBlocks(mover.getCard(), blockPos);
        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int minz = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        int maxy = Integer.MIN_VALUE;
        int maxz = Integer.MIN_VALUE;
        for (List<BlockPos> value : blocks.values()) {
            for (BlockPos pos : value) {
                minx = Math.min(minx, pos.getX());
                miny = Math.min(miny, pos.getY());
                minz = Math.min(minz, pos.getZ());
                maxx = Math.max(maxx, pos.getX()+1);
                maxy = Math.max(maxy, pos.getY()+1);
                maxz = Math.max(maxz, pos.getZ()+1);
            }
        }
        return new AABB(minx, miny, minz, maxx, maxy, maxz);
    }

    private void actualMoveServer(ItemStack vehicle) {
        Level level = mover.getLevel();
        long totalTicks = getTotalTicks();
        long currentTick = level.getGameTime() - starttick;

        // First move entities
        Vec3 startPos = getMovingPosition(0, starttick);
        Vec3 currentPos = getMovingPosition(0, level.getGameTime()+1);
        double dx = currentPos.x - startPos.x;
        double dy = currentPos.y - startPos.y;
        double dz = currentPos.z - startPos.z;
        grabbedEntities.forEach((id, basePos) -> {
            Entity entity = level.getEntity(id);
            if (entity != null && entity.isAlive()) {
                entity.setPos(basePos.x + dx, basePos.y + dy, basePos.z + dz);
                entity.setOldPosAndRot();
                entity.fallDistance = 0;
                entity.setDeltaMovement(Vec3.ZERO);
                entity.setOnGround(true);
            }
        });

        if (currentTick >= totalTicks) {
            // We are at the destination
            mover.arriveAtDestination();
        }
    }

    public long getTotalTicks() {
        // How long the entire movement should last
        return (long) (totalDist * 70);
    }

    public BlockPos getDestination() {
        return destination;
    }

    public void setDestination(BlockPos destination) {
        this.destination = destination;
    }

    public BlockPos getSource() {
        return source;
    }

    public void setSource(BlockPos source) {
        this.source = source;
    }


    @NotNull
    public Vec3 getMovingPosition(float partialTicks, long gameTick) {
        BlockPos blockPos = mover.getBlockPos();
        BlockPos destination = getDestination();
        Vec3 current = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        if (destination != null) {
            long totalTicks = getTotalTicks();
            long currentTick = gameTick - starttick;
            Vec3 dest = new Vec3(destination.getX(), destination.getY(), destination.getZ());
            current = current.lerp(dest, (currentTick + partialTicks) / (double) totalTicks);
        }
        return current;
    }

    public void load(CompoundTag tagCompound) {
        if (tagCompound.contains("source")) {
            source = NbtUtils.readBlockPos(tagCompound.getCompound("source"));
        } else {
            source = null;
        }
        if (tagCompound.contains("destination")) {
            destination = NbtUtils.readBlockPos(tagCompound.getCompound("destination"));
        } else {
            destination = null;
        }
        starttick = tagCompound.getLong("starttick");
        totalDist = tagCompound.getFloat("totalDist");
    }

    public void loadClientDataFromNBT(CompoundTag tagCompound) {
        if (tagCompound.contains("destination")) {
            destination = NbtUtils.readBlockPos(tagCompound.getCompound("destination"));
        } else {
            destination = null;
        }
        starttick = tagCompound.getLong("starttick");
        totalDist = tagCompound.getFloat("totalDist");
    }

    public void save(@Nonnull CompoundTag tagCompound) {
        if (source != null) {
            tagCompound.put("source", NbtUtils.writeBlockPos(source));
        }
        if (destination != null) {
            tagCompound.put("destination", NbtUtils.writeBlockPos(destination));
        }
        tagCompound.putLong("starttick", starttick);
        tagCompound.putFloat("totalDist", totalDist);
    }

    public void saveClientDataToNBT(CompoundTag tagCompound) {
        tagCompound.putLong("starttick", starttick);
        tagCompound.putFloat("totalDist", totalDist);
        if (destination != null) {
            tagCompound.put("destination", NbtUtils.writeBlockPos(destination));
        }
    }

}
