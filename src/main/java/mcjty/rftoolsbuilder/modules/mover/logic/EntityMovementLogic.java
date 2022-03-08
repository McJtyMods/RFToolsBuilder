package mcjty.rftoolsbuilder.modules.mover.logic;

import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import mcjty.rftoolsbuilder.modules.mover.network.PacketGrabbedEntitiesToClient;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
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

    // All 'grabbed' entities during movement
    private Map<Integer, Vec3> grabbedEntities = new HashMap<>();

    public EntityMovementLogic(MoverTileEntity mover) {
        this.mover = mover;
    }

    private final Random random = new Random();

    public void setGrabbedEntitiesClient(Set<Integer> grabbedEntities) {
        Level level = mover.getLevel();
        this.grabbedEntities = new HashMap<>();
        for (Integer id : grabbedEntities) {
            Entity entity = level.getEntity(id);
            if (entity != null) {
                this.grabbedEntities.put(id, entity.position());
            }
        }
    }


    public void tryMoveVehicleClient(float partialTicks) {
        if (destination != null) {
            Level level = mover.getLevel();
            Vec3 startPos = getMovingPosition(0, starttick);
            Vec3 currentPos = getMovingPosition(partialTicks, level.getGameTime());
            double dx = currentPos.x - startPos.x;
            double dy = currentPos.y - startPos.y;
            double dz = currentPos.z - startPos.z;
            for (var pair : grabbedEntities.entrySet()) {
                Entity entity = level.getEntity(pair.getKey());
                if (entity != null) {
                    Vec3 basePos = pair.getValue();
                    double desiredX = basePos.x + dx;
                    double desiredY = basePos.y + dy;
                    double desiredZ = basePos.z + dz;
                    desiredX = (desiredX + entity.getX()*3) / 4.0;
                    desiredY = (desiredY + entity.getY()*3) / 4.0;
                    desiredZ = (desiredZ + entity.getZ()*3) / 4.0;
                    entity.setPos(desiredX, desiredY, desiredZ);
                    entity.setOldPosAndRot();
                    entity.fallDistance = 0;
                    entity.setDeltaMovement(Vec3.ZERO);
                    entity.setOnGround(true);
                }
            }
        } else {
            grabbedEntities.clear();
        }
    }

    public void tryMoveVehicleServer() {
        ItemStack vehicle = mover.getCard();
        if (destination != null) {
            // We are moving
            actualMoveServer(vehicle);
        } else if (!mover.getNetwork().isEmpty()) {
            if (mover.isMachineEnabled() && !vehicle.isEmpty()) {
                actualStartMovementServer();
            }
        }
    }

    private void actualStartMovementServer() {
        // @todo here we should routing
        // For now we pick the first destination
        Iterator<BlockPos> iterator = mover.getNetwork().values().iterator();
        BlockPos dest = iterator.next();
        if (iterator.hasNext()) {
            float r = random.nextFloat();
            if (r < .5f) {
                dest = iterator.next();
            }
        }
        Level level = mover.getLevel();
        if (level.getBlockEntity(dest) instanceof MoverTileEntity destMover) {
            if (destMover.isAvailable()) {
                BlockPos worldPosition = mover.getBlockPos();
                destMover.setSource(worldPosition);
                destination = dest;
                totalDist = (float) Math.sqrt(worldPosition.distToCenterSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()));
                starttick = level.getGameTime();
                grabbedEntities.clear();
                mover.markDirtyClient();
            }
        }
    }

    private void actualMoveServer(ItemStack vehicle) {
        Level level = mover.getLevel();
        long totalTicks = getTotalTicks();
        long currentTick = level.getGameTime() - starttick;

        // First move entities
        Vec3 movingPosition = getMovingPosition(0, level.getGameTime());
        AABB aabb = new AABB(movingPosition, movingPosition.add(5, 5, 5));
        Vec3 startPos = getMovingPosition(0, starttick);
        Vec3 currentPos = getMovingPosition(0, level.getGameTime()+1);
        double dx = currentPos.x - startPos.x;
        double dy = currentPos.y - startPos.y;
        double dz = currentPos.z - startPos.z;
        boolean grabbedDirty = false;
        Map<Integer, Vec3> copyGrabbed = this.grabbedEntities;
        grabbedEntities = new HashMap<>();
        for (Entity entity : level.getEntitiesOfClass(Entity.class, aabb)) {
            if (copyGrabbed.containsKey(entity.getId())) {
                Vec3 basePos = copyGrabbed.get(entity.getId());
                grabbedEntities.put(entity.getId(), basePos);
                entity.setPos(basePos.x + dx, basePos.y + dy, basePos.z + dz);
                entity.setOldPosAndRot();
                entity.fallDistance = 0;
                entity.setDeltaMovement(Vec3.ZERO);
                entity.setOnGround(true);
                copyGrabbed.remove(entity.getId());
            } else {
                grabbedEntities.put(entity.getId(), entity.position());
                entity.setDeltaMovement(Vec3.ZERO);
                entity.setOnGround(true);
                grabbedDirty = true;
            }
        }
        if (!copyGrabbed.isEmpty()) {
            grabbedDirty = true;
        }

        if (grabbedDirty) {
            BlockPos worldPosition = mover.getBlockPos();
            PacketGrabbedEntitiesToClient packet = new PacketGrabbedEntitiesToClient(worldPosition, grabbedEntities.keySet());
            ChunkPos cp = new ChunkPos(worldPosition);
            RFToolsBuilderMessages.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunk(cp.x, cp.z)), packet);
        }

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
