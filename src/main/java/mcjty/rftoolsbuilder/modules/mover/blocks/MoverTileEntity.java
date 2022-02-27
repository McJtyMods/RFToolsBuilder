package mcjty.rftoolsbuilder.modules.mover.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.client.DelayedRenderer;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.OrientationTools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import mcjty.rftoolsbuilder.modules.mover.MoverConfiguration;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.client.MoverRenderer;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleCard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static mcjty.lib.api.container.DefaultContainerProvider.container;
import static mcjty.lib.builder.TooltipBuilder.*;
import static mcjty.lib.container.SlotDefinition.specific;

public class MoverTileEntity extends TickingTileEntity {

    public static final int SLOT_VEHICLE_CARD = 0;
    public static final int MAXSCAN = 256;  //@todo configurable

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(1)
            .slot(specific(MoverModule.VEHICLE_CARD.get()).in().out(), SLOT_VEHICLE_CARD, 154, 11)
            .playerSlots(10, 70));

    @Cap(type = CapType.ITEMS_AUTOMATION)
    private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY)
            .onUpdate((slot, stack) -> {
                updateVehicle();
            })
            .build();

    @Cap(type = CapType.ENERGY)
    private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(this, true, MoverConfiguration.MAXENERGY.get(), MoverConfiguration.RECEIVEPERTICK.get());

    @Cap(type = CapType.CONTAINER)
    private final LazyOptional<MenuProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Mover")
            .containerSupplier(container(MoverModule.CONTAINER_MOVER, CONTAINER_FACTORY,this))
            .itemHandler(() -> items)
            .energyHandler(() -> energyStorage)
            .setupSync(this));


    @Cap(type = CapType.INFUSABLE)
    private final IInfusable infusable = new DefaultInfusable(MoverTileEntity.this);

    @GuiValue
    private String name;

    @GuiValue
    public static final Value<?, String> VALUE_CONNECTIONS = Value.create("connections", Type.STRING, MoverTileEntity::getConnectionCount, MoverTileEntity::setConnectionCount);
    private String connections = "";

    // Counter to make setting invisible blocks more efficient
    private int cnt;

    // If this is the source of a movement then this refers to the destination. Otherwise null. Synced to client
    private BlockPos destination = null;
    // For the source of the movement, this refers to the game tick when the movement started. Synced to client
    private long starttick;
    // For the source of the movement, the total distance to travel. Synced to client
    private float totalDist;

    // If this is the destination of a movement then this refers to the source. Otherwise null
    private BlockPos source = null;

    // A cache for invisible mover blocks
    private Set<BlockPos> invisibleMoverBlocks = null;

    // All 'grabbed' entities during movement
    private final Map<Integer, Vec3> grabbedEntities = new HashMap<>();

    // Neighbouring movers
    private final Map<Direction, BlockPos> network = new EnumMap<>(Direction.class);

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .tileEntitySupplier(MoverTileEntity::new)
                .topDriver(RFToolsBuilderTOPDriver.DRIVER)
                .infusable()
                .manualEntry(ManualHelper.create("rftoolsbuilder:todo"))
                .info(key("message.rftoolsbuilder.shiftmessage"))
                .infoShift(header(), gold()));
    }


    public MoverTileEntity(BlockPos pos, BlockState state) {
        super(MoverModule.TYPE_MOVER.get(), pos, state);
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (invisibleMoverBlocks != null) {
            removeInvisibleBlocks();
        }
    }

    @Override
    protected void tickServer() {
        updateVehicleStatus();
        tryMoveVehicleServer();
    }

    @Override
    protected void tickClient() {
        //@todo optimize to only render when in front and distance
        handleRender();
        tryMoveVehicleClient();
    }

    private void handleRender() {
        ItemStack vehicle = getCard();
        if (VehicleBuilderTileEntity.isVehicleCard(vehicle)) {
            DelayedRenderer.addRender(worldPosition, (poseStack, cameraVec) -> {
                MoverRenderer.actualRender(this, poseStack, cameraVec, vehicle);
            }, (level, pos) -> {
                if (level.getBlockEntity(pos) instanceof MoverTileEntity mover) {
                    return !mover.getCard().isEmpty();
                }
                return false;
            });
        }
    }

    private void tryMoveVehicleClient() {
        if (destination != null) {
//            ItemStack vehicle = items.getStackInSlot(SLOT_VEHICLE_CARD);
//            getTotalTicks(totalDist);
//            long currentTick = level.getGameTime() - starttick;
        }
    }

    public long getStarttick() {
        return starttick;
    }

    public float getTotalDist() {
        return totalDist;
    }

    public long getTotalTicks() {
        // How long the entire movement should last
        return (long) (totalDist * 70);
    }

    public BlockPos getDestination() {
        return destination;
    }

    @NotNull
    public Vec3 getMovingPosition(float partialTicks, long gameTick) {
        BlockPos blockPos = worldPosition;
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

    private final Random random = new Random();

    private void tryMoveVehicleServer() {
        ItemStack vehicle = items.getStackInSlot(SLOT_VEHICLE_CARD);
        if (destination != null) {
            // We are moving
            actualMoveServer(vehicle);
        } else if (!network.isEmpty()) {
            if (isMachineEnabled() && !vehicle.isEmpty()) {
                actualStartMovementServer();
            }
        }
    }

    private void actualStartMovementServer() {
        // @todo here we should routing
        // For now we pick the first destination
        Iterator<BlockPos> iterator = network.values().iterator();
        BlockPos dest = iterator.next();
        if (iterator.hasNext()) {
            float r = random.nextFloat();
            if (r < .5f) {
                dest = iterator.next();
            }
        }
        if (level.getBlockEntity(dest) instanceof MoverTileEntity destMover) {
            if (destMover.isAvailable()) {
                destMover.setSource(worldPosition);
                destination = dest;
                totalDist = (float) Math.sqrt(worldPosition.distSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), true));
                starttick = level.getGameTime();
                grabbedEntities.clear();
                markDirtyClient();
            }
        }
    }

    private void actualMoveServer(ItemStack vehicle) {
        long totalTicks = getTotalTicks();
        long currentTick = level.getGameTime() - starttick;

        // First move entities
        Vec3 movingPosition = getMovingPosition(0, level.getGameTime());
        AABB aabb = new AABB(movingPosition, movingPosition.add(5, 5, 5));
        Vec3 startPos = getMovingPosition(0, starttick);
        Vec3 currentPos = getMovingPosition(0, level.getGameTime()+3);
        double dx = currentPos.x - startPos.x;
        double dy = currentPos.y - startPos.y;
        double dz = currentPos.z - startPos.z;
        for (Entity entity : level.getEntitiesOfClass(Entity.class, aabb)) {
            if (grabbedEntities.containsKey(entity.getId())) {
                Vec3 basePos = grabbedEntities.get(entity.getId());
                entity.setPos(basePos.x + dx, basePos.y + dy, basePos.z + dz);
                entity.setOldPosAndRot();
                entity.fallDistance = 0;
                entity.moveDist = 0;
                entity.flyDist = 0;
                entity.walkDist = 0;
                entity.hasImpulse = false;
                entity.noPhysics = true;
                entity.setDeltaMovement(Vec3.ZERO);
                entity.setOnGround(true);
            } else {
                grabbedEntities.put(entity.getId(), entity.position());
                entity.noPhysics = true;
                entity.setDeltaMovement(Vec3.ZERO);
                entity.setOnGround(true);
            }
        }
        for (Map.Entry<Integer, Vec3> entry : grabbedEntities.entrySet()) {
            Entity entity = level.getEntity(entry.getKey());
            if (entity != null) {
                Vec3 basePos = entry.getValue();
                entity.setPos(basePos.x + dx, basePos.y + dy, basePos.z + dz);
                entity.setOldPosAndRot();
                entity.fallDistance = 0;
                entity.moveDist = 0;
                entity.flyDist = 0;
                entity.walkDist = 0;
                entity.hasImpulse = false;
                entity.noPhysics = true;
                entity.setDeltaMovement(Vec3.ZERO);
                entity.setOnGround(true);
            }
        }

        if (currentTick >= totalTicks) {
            // We are at the destination
            if (level.getBlockEntity(destination) instanceof MoverTileEntity destMover) {
                destMover.items.setStackInSlot(SLOT_VEHICLE_CARD, vehicle);
                items.setStackInSlot(SLOT_VEHICLE_CARD, ItemStack.EMPTY);
                destMover.setSource(null);
            } else {
                // Something is wrong. The destination is gone. Stop the movement
                // @todo handle this more gracefully. Move back
            }
            destination = null;
            markDirtyClient();
        }
    }

    private void updateVehicleStatus() {
        if (invisibleMoverBlocks == null) {
            updateVehicle();
            cnt = 0;
        }
        cnt--;
        if (cnt <= 0) {
            cnt = 4;
            BlockState invisibleState = MoverModule.INVISIBLE_MOVER_BLOCK.get().defaultBlockState();
            invisibleMoverBlocks.forEach(p -> {
                if (level.getBlockState(p) != invisibleState && level.getBlockState(p).getMaterial().isReplaceable()) {
                    level.setBlock(p, invisibleState, Block.UPDATE_ALL);
                }
            });
        }
    }

    private void updateVehicle() {
        ItemStack vehicle = items.getStackInSlot(SLOT_VEHICLE_CARD);
        if (invisibleMoverBlocks == null) {
            invisibleMoverBlocks = new HashSet<>();
        }
        removeInvisibleBlocks();
        if (!vehicle.isEmpty()) {
            Map<BlockState, List<BlockPos>> blocks = VehicleCard.getBlocks(vehicle, worldPosition.offset(1, 1, 1));
            blocks.values().forEach(invisibleMoverBlocks::addAll);
        } else {
            destination = null;
        }
        markDirtyClient();
    }

    private void removeInvisibleBlocks() {
        BlockState invisibleState = MoverModule.INVISIBLE_MOVER_BLOCK.get().defaultBlockState();
        invisibleMoverBlocks.forEach(p -> {
            if (level.getBlockState(p) == invisibleState) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        });
        invisibleMoverBlocks.clear();
    }

    public void setSource(BlockPos pos) {
        source = pos;
        setChanged();
    }

    public boolean isAvailable() {
        ItemStack vehicle = items.getStackInSlot(SLOT_VEHICLE_CARD);
        if (!vehicle.isEmpty()) {
            // If there is a vehicle here then this is not available
            return false;
        }
        if (destination != null) {
            // If something is moving from here then this is not available
            return false;
        }
        if (source != null) {
            // If something is moving to here then this is not available
            return false;
        }
        return true;
    }

    public String getConnectionCount() {
        return connections;
    }

    public void setConnectionCount(String v) {
        connections = v;
    }

    public ItemStack getCard() {
        return items.getStackInSlot(SLOT_VEHICLE_CARD);
    }

    private void addConnection(Direction direction, BlockPos pos) {
        network.put(direction, pos);
        connections = connections + direction.name().toUpperCase().charAt(0);
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            if (tagCompound.contains(direction.name())) {
                addConnection(direction, NbtUtils.readBlockPos(tagCompound.getCompound(direction.name())));
            }
        }
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

    @Override
    public void loadInfo(CompoundTag tagCompound) {
        super.loadInfo(tagCompound);
        CompoundTag info = tagCompound.getCompound("Info");
        name = info.getString("name");
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tagCompound) {
        super.saveAdditional(tagCompound);
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            if (network.containsKey(direction)) {
                tagCompound.put(direction.name(), NbtUtils.writeBlockPos(network.get(direction)));
            }
        }
        if (source != null) {
            tagCompound.put("source", NbtUtils.writeBlockPos(source));
        }
        if (destination != null) {
            tagCompound.put("destination", NbtUtils.writeBlockPos(destination));
        }
        tagCompound.putLong("starttick", starttick);
        tagCompound.putFloat("totalDist", totalDist);
    }

    @Override
    public void saveInfo(CompoundTag tagCompound) {
        super.saveInfo(tagCompound);
        CompoundTag info = getOrCreateInfo(tagCompound);
        if (name != null) {
            info.putString("name", name);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        loadClientDataFromNBT(tag);
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tagCompound) {
        ItemStack card = items.getStackInSlot(SLOT_VEHICLE_CARD);
        CompoundTag tag = new CompoundTag();
        card.save(tag);
        tagCompound.put("card", tag);
        tagCompound.putLong("starttick", starttick);
        tagCompound.putFloat("totalDist", totalDist);
        if (destination != null) {
            tagCompound.put("destination", NbtUtils.writeBlockPos(destination));
        }
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tagCompound) {
        CompoundTag tag = tagCompound.getCompound("card");
        items.setStackInSlot(SLOT_VEHICLE_CARD, ItemStack.of(tag));
        if (tagCompound.contains("destination")) {
            destination = NbtUtils.readBlockPos(tagCompound.getCompound("destination"));
        } else {
            destination = null;
        }
        starttick = tagCompound.getLong("starttick");
        totalDist = tagCompound.getFloat("totalDist");
    }

    private void doScan() {
        network.clear();
        connections = "";
        Set<BlockPos> done = new HashSet<>();
        done.add(worldPosition);
        doScan(done, null);
        setChanged();
    }

    private void doScanFromOther(Set<BlockPos> done, @Nonnull Direction comingFrom, BlockPos other) {
        network.clear();
        connections = "";
        addConnection(comingFrom.getOpposite(), other);
        doScan(done, comingFrom);
        setChanged();
    }

    private void doScan(Set<BlockPos> done, @Nullable Direction comingFrom) {
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            if (!Objects.equals(direction.getOpposite(), comingFrom)) {
                for (int i = 1; i < MAXSCAN; i++) {
                    BlockPos relative = worldPosition.relative(direction, i);
                    if (done.contains(relative)) {
                        break;
                    }
                    if (level.getBlockEntity(relative) instanceof MoverTileEntity mover) {
                        done.add(relative);
                        addConnection(direction, relative);
                        mover.doScanFromOther(done, direction, worldPosition);
                        break;
                    }
                }
            }
        }
    }

    @ServerCommand
    public static final Command<?> CMD_SCAN = Command.<MoverTileEntity>create("scan", (te, player, params) -> te.doScan());
}
