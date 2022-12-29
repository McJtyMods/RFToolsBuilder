package mcjty.rftoolsbuilder.modules.mover.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.client.DelayedRenderer;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.OrientationTools;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.client.MoverRenderer;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleCard;
import mcjty.rftoolsbuilder.modules.mover.logic.EntityMovementLogic;
import mcjty.rftoolsbuilder.modules.mover.network.PacketClickMover;
import mcjty.rftoolsbuilder.modules.mover.network.PacketSyncVehicleInformationToClient;
import mcjty.rftoolsbuilder.modules.mover.sound.MoverSoundController;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static mcjty.lib.api.container.DefaultContainerProvider.container;
import static mcjty.lib.builder.TooltipBuilder.*;
import static mcjty.lib.container.SlotDefinition.specific;

public class MoverTileEntity extends TickingTileEntity {

    public static final int SLOT_VEHICLE_CARD = 0;

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(1)
            .slot(specific(MoverModule.VEHICLE_CARD.get()).in().out(), SLOT_VEHICLE_CARD, 154, 11)
            .playerSlots(10, 70));

    @Cap(type = CapType.ITEMS_AUTOMATION)
    private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY)
            .onUpdate((slot, stack) -> {
                updateVehicle();
            })
            .build();

    @Cap(type = CapType.CONTAINER)
    private final LazyOptional<MenuProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Mover")
            .containerSupplier(container(MoverModule.CONTAINER_MOVER, CONTAINER_FACTORY,this))
            .itemHandler(() -> items)
            .setupSync(this));


    @GuiValue
    private String name;

    @GuiValue
    private boolean down = true;
    @GuiValue
    private boolean up = true;
    @GuiValue
    private boolean north = true;
    @GuiValue
    private boolean south = true;
    @GuiValue
    private boolean west = true;
    @GuiValue
    private boolean east = true;

    @GuiValue
    public static final Value<?, String> VALUE_CONNECTIONS = Value.create("connections", Type.STRING, MoverTileEntity::getConnectionCount, MoverTileEntity::setConnectionCount);
    private String connections = "";

    // @todo a bit clumsy but it works. Better would be a cap in the player
    public static final Set<Integer> wantUnmount = new HashSet<>();

    // The offset as set by the controller
    private BlockPos offset = new BlockPos(1, 1, 1);

    // A reference to the controller (synced to client)
    private BlockPos controller;

    // Counter to make setting invisible blocks more efficient
    private int cnt;

    // Counter for sending info to the clients
    private int clientUpdateCnt;
    // Data on server, is synced with client
    private boolean enoughPower = false;

    // Client side variables
    private List<String> platformsFromServer = Collections.emptyList();
    private String currentPlatform = "";
    private BlockPos cursorBlock;
    private double cursorX;
    private double cursorY;
    private String highlightedMover;
    private boolean moverValid = false;
    private int currentPage = 0;
    private int renderCopyTimer = 0;
    private BlockPos lastDestination;

    // A cache for invisible mover blocks
    private Map<BlockPos, BlockState> invisibleMoverBlocks = null;

    private final EntityMovementLogic logic = new EntityMovementLogic(this);

    // Neighbouring movers
    private final Map<Direction, BlockPos> network = new EnumMap<>(Direction.class);

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .tileEntitySupplier(MoverTileEntity::new)
                .topDriver(RFToolsBuilderTOPDriver.DRIVER)
                .manualEntry(ManualHelper.create("rftoolsbuilder:todo"))
                .info(key("message.rftoolsbuilder.shiftmessage"))
                .infoShift(header(), gold())) {
            @Override
            public RotationType getRotationType() {
                return RotationType.NONE;
            }
        };
    }


    public MoverTileEntity(BlockPos pos, BlockState state) {
        super(MoverModule.TYPE_MOVER.get(), pos, state);
    }

    public Map<Direction, BlockPos> getNetwork() {
        return network;
    }

    public EntityMovementLogic getLogic() {
        return logic;
    }

    public String getName() {
        return name;
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
        if (!isMoving()) {
            logic.clearGrabbedEntities();
        }
        updateVehicleStatus();
        logic.tryMoveVehicleServer();
        // If there is a vehicle we sync status to clients
        syncVehicleStatus();
        MoverControllerTileEntity controller = getController();
        enoughPower = false;
        if (controller != null) {
            enoughPower = controller.hasEnoughPower();
        }
    }

    @Override
    protected void tickClient() {
        //@todo optimize to only render when in front and distance
        handleRender();
        handleSound();
        setCursor();
    }

    // Only call client side
    public void setHighlightedMover(String highlightedMover) {
        this.highlightedMover = highlightedMover;
    }

    // Only call client side
    public BlockPos getCursorBlock() {
        return cursorBlock;
    }

    // Only call client side
    public double getCursorX() {
        return cursorX;
    }

    // Only call client side
    public double getCursorY() {
        return cursorY;
    }

    // Only call client side
    public boolean isMoverValid() {
        return moverValid;
    }

    // Only call client side
    public boolean hasEnoughPower() {
        return enoughPower;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    private void setCursor() {
        HitResult mouseOver = SafeClientTools.getClientMouseOver();
        if (mouseOver instanceof BlockHitResult blockResult) {
            BlockPos pos = blockResult.getBlockPos();
            List<InvisibleMoverBlock.MoverData> list = MoverModule.INVISIBLE_MOVER_BLOCK.get().getData(worldPosition);
            if (list != null) {
                for (InvisibleMoverBlock.MoverData data : list) {
                    if (pos.equals(data.controlPos())) {
                        Pair<Double, Double> cursor = getCursor(mouseOver.getLocation().x - pos.getX(), mouseOver.getLocation().y - pos.getY(), mouseOver.getLocation().z - pos.getZ(),
                                data.horizDirection(), data.direction());
                        cursorBlock = pos;
                        cursorX = cursor.getLeft();
                        cursorY = cursor.getRight();
                        return;
                    }
                }
            }
        }
    }

    // Return true if there is a direct connection to the given position
    public boolean hasDirectContectionTo(BlockPos destination) {
        return network.containsValue(destination);
    }

    public void setClientRenderInfo(List<String> platforms, String currentPlatform, boolean valid, boolean enoughPower) {
        this.platformsFromServer = platforms;
        this.currentPlatform = currentPlatform;
        this.moverValid = valid;
        this.enoughPower = enoughPower;
    }

    public List<String> getPlatformsFromServer() {
        return platformsFromServer;
    }

    public String getCurrentPlatform() {
        return currentPlatform;
    }

    private void syncVehicleStatus() {
        clientUpdateCnt--;
        if (clientUpdateCnt <= 0) {
            clientUpdateCnt = 20;
            if (!getCard().isEmpty()) {
                List<String> platforms;
                boolean valid = isValid();
                if (valid) {
                    platforms = traverseAndCollect().values().stream().map(MoverTileEntity::getName).sorted().collect(Collectors.toList());
                } else {
                    platforms = Collections.emptyList();
                }
                RFToolsBuilderMessages.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(worldPosition)),
                        new PacketSyncVehicleInformationToClient(worldPosition, platforms, getName(), valid, hasEnoughPower()));
            }
        }
    }

    private void handleSound() {
        if (controller == null) {
            return;
        }
        long starttick = getLogic().getStarttick();
        long totalTicks = getLogic().getTotalTicks();
        long current = level.getGameTime();
        long endtick = starttick + totalTicks;
        if (current >= starttick && current <= endtick) {
            Vec3 currentPos = getLogic().getMovingPosition(0, level.getGameTime());
            if (MoverSoundController.isPlaying(level, controller, worldPosition)) {
                MoverSoundController.move(level, controller, worldPosition, currentPos);
            } else {
                MoverSoundController.play(level, controller, worldPosition, currentPos);
            }
        } else {
            MoverSoundController.stop(level, controller, worldPosition);
        }
    }

    private void handleRender() {
        ItemStack vehicle = getCard();
        if (VehicleBuilderTileEntity.isVehicleCard(vehicle)) {
            renderCopyTimer = 1;
            MoverRenderer.addPreRender(worldPosition, () -> {
                float partialTicks = MoverRenderer.getPartialTicks();
                logic.tryMoveVehicleClientEntities(partialTicks);

            }, this::isMoverThere);
            DelayedRenderer.addRender(worldPosition, (poseStack, cameraVec) -> {
                float partialTicks = MoverRenderer.getPartialTicks();
                Vec3 offset = logic.tryMoveVehicleThisPlayer(partialTicks);
                Vec3 current;
                if (getCard().isEmpty()) {
                    // Render at the last known destination
                    if (lastDestination == null) {
                        return;
                    }
                    current = new Vec3(lastDestination.getX(), lastDestination.getY(), lastDestination.getZ());
                } else {
                    current = logic.getMovingPosition(partialTicks, level.getGameTime());
                    lastDestination = logic.getDestination();
                }
                MoverRenderer.actualRender(this, poseStack, cameraVec, vehicle, current, offset);
            }, this::isMoverThere);
        } else if (renderCopyTimer > 0) {
            renderCopyTimer--;
        }
    }

    @NotNull
    private Boolean isMoverThere(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof MoverTileEntity mover) {
            // renderCopyTimer is used to 'linger' the rendering to avoid flickering
            if (renderCopyTimer > 0) {
                return true;
            }
            return !mover.getCard().isEmpty();
        }
        return false;
    }


    @Nonnull
    public Map<BlockPos, MoverTileEntity> traverseAndCollect() {
        Map<BlockPos, MoverTileEntity> alreadyHandled = new HashMap<>();
        alreadyHandled.put(worldPosition, this);
        traverseAndCollectInt(alreadyHandled);
        return alreadyHandled;
    }

    private void traverseAndCollectInt(Map<BlockPos, MoverTileEntity> alreadyHandled) {
        for (Map.Entry<Direction, BlockPos> entry : getNetwork().entrySet()) {
            BlockPos p = entry.getValue();
            if (!alreadyHandled.containsKey(p)) {
                if (level.getBlockEntity(p) instanceof MoverTileEntity child) {
                    alreadyHandled.put(p, child);
                    child.traverseAndCollectInt(alreadyHandled);
                }
            }
        }
    }

    @Nullable
    public <T> T traverseDepthFirst(BiFunction<BlockPos, MoverTileEntity, T> function) {
        Set<BlockPos> alreadyHandled = new HashSet<>();
        alreadyHandled.add(worldPosition);
        return traverseDepthFirstInt(alreadyHandled, function);
    }

    @Nullable
    private <T> T traverseDepthFirstInt(Set<BlockPos> alreadyHandled, BiFunction<BlockPos, MoverTileEntity, T> function) {
        T result = function.apply(worldPosition, this);
        if (result != null) {
            return result;
        }
        for (Map.Entry<Direction, BlockPos> entry : getNetwork().entrySet()) {
            BlockPos p = entry.getValue();
            if (!alreadyHandled.contains(p)) {
                alreadyHandled.add(p);
                if (level.getBlockEntity(p) instanceof MoverTileEntity child) {
                    result = child.traverseDepthFirstInt(alreadyHandled, function);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    public <T> T traverseBreadthFirst(BiFunction<BlockPos, MoverTileEntity, T> function) {
        Set<BlockPos> alreadyHandled = new HashSet<>();
        List<Pair<BlockPos, MoverTileEntity>> todo = new ArrayList<>();
        todo.add(Pair.of(worldPosition, this));
        alreadyHandled.add(worldPosition);
        int toProcess = 0;
        int toExpand = 0;
        while (toExpand < todo.size()) {
            // We can already process everything we expanded
            while (toProcess < todo.size()) {
                Pair<BlockPos, MoverTileEntity> pair = todo.get(toProcess);
                T result = function.apply(pair.getLeft(), pair.getRight());
                if (result != null) {
                    return result;
                }
                toProcess++;
            }
            // Expand the current node
            for (Map.Entry<Direction, BlockPos> entry : todo.get(toExpand).getRight().getNetwork().entrySet()) {
                BlockPos childPos = entry.getValue();
                if (!alreadyHandled.contains(childPos)) {
                    if (level.getBlockEntity(childPos) instanceof MoverTileEntity childMover) {
                        alreadyHandled.add(childPos);
                        todo.add(Pair.of(childPos, childMover));
                    }
                }
            }
            toExpand++;
        }
        return null;
    }


    // The position of the current mover (in the function) is not included
    @Nullable
    public <T> T traverseBreadthFirstWithPath(BiFunction<List<BlockPos>, MoverTileEntity, T> function) {
        Set<BlockPos> alreadyHandled = new HashSet<>();
        List<Pair<List<BlockPos>, MoverTileEntity>> todo = new ArrayList<>();
        todo.add(Pair.of(new ArrayList<>(), this));
        alreadyHandled.add(worldPosition);
        int toProcess = 0;
        int toExpand = 0;
        while (toExpand < todo.size()) {
            // We can already process everything we expanded
            while (toProcess < todo.size()) {
                Pair<List<BlockPos>, MoverTileEntity> pair = todo.get(toProcess);
                T result = function.apply(pair.getLeft(), pair.getRight());
                if (result != null) {
                    return result;
                }
                toProcess++;
            }
            // Expand the current node
            for (Map.Entry<Direction, BlockPos> entry : todo.get(toExpand).getRight().getNetwork().entrySet()) {
                BlockPos childPos = entry.getValue();
                if (!alreadyHandled.contains(childPos)) {
                    if (level.getBlockEntity(childPos) instanceof MoverTileEntity childMover) {
                        alreadyHandled.add(childPos);
                        if (childMover.isAvailable()) {
                            List<BlockPos> path = new ArrayList<>(todo.get(toExpand).getLeft());
                            path.add(childPos);
                            todo.add(Pair.of(path, childMover));
                        }
                    }
                }
            }
            toExpand++;
        }
        return null;
    }


    /**
     * Return true if this mover is valid and connected to a network
     */
    public boolean isValid() {
        if (controller == null) {
            return false;
        }
        return level.getBlockEntity(controller) instanceof MoverControllerTileEntity;
    }

    public boolean isMoving() {
        if (getCard().isEmpty()) {
            return false;
        }
        return logic.getDestination() != null;
    }

    private void updateVehicleStatus() {
        if (logic.getDestination() != null) {
            // We are moving. Remove the mover blocks if there are any
            if (invisibleMoverBlocks != null) {
                BlockState invisibleState = MoverModule.INVISIBLE_MOVER_BLOCK.get().defaultBlockState();
                invisibleMoverBlocks.forEach((p, st) -> {
                    BlockState state = level.getBlockState(p);
                    if (state == invisibleState) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                });
                invisibleMoverBlocks = null;
            }
        } else {
            // We are not moving
            if (invisibleMoverBlocks == null) {
                updateVehicle();
                cnt = 0;
            }
            cnt--;
            if (cnt <= 0) {
                cnt = 4;
                BlockState invisibleState = MoverModule.INVISIBLE_MOVER_BLOCK.get().defaultBlockState();
                invisibleMoverBlocks.forEach((p, originalState) -> {
                    BlockState state = level.getBlockState(p);
                    if (state != invisibleState && state.getMaterial().isReplaceable()) {
                        level.setBlock(p, invisibleState, Block.UPDATE_ALL);
                        if (level.getBlockEntity(p) instanceof InvisibleMoverBE invisibleMover) {
                            invisibleMover.setOriginalState(originalState);
                            level.sendBlockUpdated(p, invisibleState, invisibleState, Block.UPDATE_CLIENTS + Block.UPDATE_NEIGHBORS);
                        }
                    }
                });
            }
        }
    }

    private void updateVehicle() {
        ItemStack vehicle = items.getStackInSlot(SLOT_VEHICLE_CARD);
        if (invisibleMoverBlocks == null) {
            invisibleMoverBlocks = new HashMap<>();
        }
        cnt = 0;
        removeInvisibleBlocks();
        if (!vehicle.isEmpty()) {
            Map<BlockState, List<BlockPos>> blocks = VehicleCard.getBlocks(vehicle, worldPosition.offset(offset));
            for (Map.Entry<BlockState, List<BlockPos>> entry : blocks.entrySet()) {
                for (BlockPos pos : entry.getValue()) {
                    invisibleMoverBlocks.put(pos, entry.getKey());
                }
            }
        } else {
            logic.setDestination(null);
        }
        markDirtyClient();
    }

    private void removeInvisibleBlocks() {
        BlockState invisibleState = MoverModule.INVISIBLE_MOVER_BLOCK.get().defaultBlockState();
        invisibleMoverBlocks.forEach((p, st) -> {
            BlockState state = level.getBlockState(p);
            if (state == invisibleState) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        });
        invisibleMoverBlocks.clear();
    }

    public void arriveAtDestination() {
        if (level.getBlockEntity(logic.getDestination()) instanceof MoverTileEntity destMover) {
            getLogic().endMoveServer();

            destMover.items.setStackInSlot(SLOT_VEHICLE_CARD, getCard());
            items.setStackInSlot(SLOT_VEHICLE_CARD, ItemStack.EMPTY);
            destMover.setSource(null);
            destMover.getLogic().setWaitABit(5);
            destMover.updateVehicle();
            destMover.updateVehicleStatus();
//            destMover.getLogic().grabEntities();
//            destMover.getLogic().setGrabTimeout(5);
        } else {
            // Something is wrong. The destination is gone. Stop the movement
            // @todo handle this more gracefully. Move back
        }
        logic.setDestination(null);
        // Make sure the invisible blocks are back
        cnt = 0;
        updateVehicle();
        updateVehicleStatus();
        markDirtyClient();

    }

    public void setSource(BlockPos pos) {
        logic.setSource(pos);
        setChanged();
    }

    public boolean isAvailable() {
        ItemStack vehicle = items.getStackInSlot(SLOT_VEHICLE_CARD);
        if (!vehicle.isEmpty()) {
            // If there is a vehicle here then this is not available
            return false;
        }
        if (logic.getDestination() != null) {
            // If something is moving from here then this is not available
            return false;
        }
        if (logic.getSource() != null) {
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

    public void clearNetwork() {
        network.clear();
        connections = "";
        setChanged();
    }

    public boolean canConnect(Direction direction) {
        return switch (direction) {
            case DOWN -> down;
            case UP -> up;
            case NORTH -> north;
            case SOUTH -> south;
            case WEST -> west;
            case EAST -> east;
        };
    }

    public void hitScreenClient(BlockPos pos, double x, double y, double z, Direction hitDirection, Direction horizDirection, Direction direction) {
//        System.out.println("x = " + x + "," + y + "," + z);
//        System.out.println("hitDirection = " + hitDirection);
//        System.out.println("horizDirection = " + horizDirection);
//        System.out.println("direction = " + direction);

        if (hitDirection == direction) {
            Pair<Double, Double> pair = getCursor(x, y, z, horizDirection, direction);
            cursorBlock = pos;
            cursorX = pair.getLeft();
            cursorY = pair.getRight();
            if (highlightedMover != null && !highlightedMover.isEmpty()) {
                if ("___<___".equals(highlightedMover)) {
                    // Previous page
                    if (currentPage > 0) {
                        currentPage--;
                    }
                } else if ("___>___".equals(highlightedMover)) {
                    // Next page
                    currentPage++;
                    int pages = (platformsFromServer.size() + MoverRenderer.LINES_SUPPORTED-1) / MoverRenderer.LINES_SUPPORTED;
                    if (currentPage >= pages) {
                        currentPage = pages-1;
                    }
                } else {
                    RFToolsBuilderMessages.INSTANCE.sendToServer(new PacketClickMover(worldPosition, highlightedMover));
                }
            }
        }
    }

    /**
     * Start movement of the vehicle currently on this platform towards the destination mover
     */
    public void startMove(String mover) {
        if (controller != null && !getCard().isEmpty()) {
            if (level.getBlockEntity(controller) instanceof MoverControllerTileEntity controllerTile) {
                controllerTile.setupMovement(mover, VehicleCard.getVehicleName(getCard()));
            }
        }
    }

    public void setController(MoverControllerTileEntity controller) {
        this.controller = controller.getBlockPos();
        setChanged();
    }

    public MoverControllerTileEntity getController() {
        if (controller == null) {
            return null;
        }
        if (level.getBlockEntity(controller) instanceof MoverControllerTileEntity controller) {
            return controller;
        }
        return null;
    }

    @NotNull
    private Pair<Double, Double> getCursor(double x, double y, double z, Direction horizDirection, Direction direction) {
        return switch (direction) {
            case UP -> switch (horizDirection) {
                case DOWN -> Pair.of(1- x, 1- z);
                case UP -> Pair.of(1- x, 1- z);
                case NORTH -> Pair.of(1- x, 1- z);    // <- OK
                case SOUTH -> Pair.of(x, z);
                case WEST -> Pair.of(1- z, 1- x);
                case EAST -> Pair.of(z, x);
            };
            case DOWN -> switch (horizDirection) {
                case DOWN -> Pair.of(1- x, z);
                case UP -> Pair.of(1- x, z);
                case NORTH -> Pair.of(1- x, z);      // <- OK
                case SOUTH -> Pair.of(x, 1- z);
                case WEST -> Pair.of(z, 1- x);
                case EAST -> Pair.of(1- z, x);
            };
            case NORTH -> Pair.of(1- x, 1- y);
            case SOUTH -> Pair.of(x, 1- y);
            case WEST -> Pair.of(z, 1- y);
            case EAST -> Pair.of(1- z, 1- y);
        };
    }

    public void addConnection(Direction direction, BlockPos pos) {
        network.put(direction, pos);
        connections = connections + direction.name().toUpperCase().charAt(0);
        setChanged();
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            if (tagCompound.contains(direction.name())) {
                addConnection(direction, NbtUtils.readBlockPos(tagCompound.getCompound(direction.name())));
            }
        }
        logic.load(tagCompound);
        int[] controller = tagCompound.getIntArray("controller");
        if (controller.length >= 3) {
            this.controller = new BlockPos(controller[0], controller[1], controller[2]);
        } else {
            this.controller = null;
        }
        offset = new BlockPos(tagCompound.getInt("offsetX"), tagCompound.getInt("offsetY"), tagCompound.getInt("offsetZ"));
    }

    @Override
    public void loadInfo(CompoundTag tagCompound) {
        super.loadInfo(tagCompound);
        CompoundTag info = tagCompound.getCompound("Info");
        name = info.getString("name");
        down = info.getBoolean("down");
        up = info.getBoolean("up");
        north = info.getBoolean("north");
        south = info.getBoolean("south");
        west = info.getBoolean("west");
        east = info.getBoolean("east");
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tagCompound) {
        super.saveAdditional(tagCompound);
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            if (network.containsKey(direction)) {
                tagCompound.put(direction.name(), NbtUtils.writeBlockPos(network.get(direction)));
            }
        }
        logic.save(tagCompound);
        if (controller != null) {
            tagCompound.putIntArray("controller", new int[] { controller.getX(), controller.getY(), controller.getZ() });
        }
        tagCompound.putInt("offsetX", offset.getX());
        tagCompound.putInt("offsetY", offset.getY());
        tagCompound.putInt("offsetZ", offset.getZ());
    }

    @Override
    public void saveInfo(CompoundTag tagCompound) {
        super.saveInfo(tagCompound);
        CompoundTag info = getOrCreateInfo(tagCompound);
        if (name != null) {
            info.putString("name", name);
        }
        info.putBoolean("down", down);
        info.putBoolean("up", up);
        info.putBoolean("north", north);
        info.putBoolean("south", south);
        info.putBoolean("west", west);
        info.putBoolean("east", east);
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
        logic.saveClientDataToNBT(tagCompound);
        if (controller != null) {
            tagCompound.putIntArray("controller", new int[] { controller.getX(), controller.getY(), controller.getZ() });
        }
        tagCompound.putInt("offsetX", offset.getX());
        tagCompound.putInt("offsetY", offset.getY());
        tagCompound.putInt("offsetZ", offset.getZ());
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tagCompound) {
        CompoundTag tag = tagCompound.getCompound("card");
        items.setStackInSlot(SLOT_VEHICLE_CARD, ItemStack.of(tag));
        logic.loadClientDataFromNBT(tagCompound);
        int[] controller = tagCompound.getIntArray("controller");
        if (controller.length >= 3) {
            this.controller = new BlockPos(controller[0], controller[1], controller[2]);
        } else {
            this.controller = null;
        }
        offset = new BlockPos(tagCompound.getInt("offsetX"), tagCompound.getInt("offsetY"), tagCompound.getInt("offsetZ"));
    }

    public void setOffset(int x, int y, int z) {
        offset = new BlockPos(x, y, z);
        updateVehicle();
        markDirtyClient();
    }

    public BlockPos getOffset() {
        return offset;
    }
}
