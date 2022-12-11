package mcjty.rftoolsbuilder.modules.mover.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blocks.BaseBlock;
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
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.client.MoverRenderer;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleCard;
import mcjty.rftoolsbuilder.modules.mover.logic.EntityMovementLogic;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

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


    @Cap(type = CapType.INFUSABLE)
    private final IInfusable infusable = new DefaultInfusable(MoverTileEntity.this);

    @GuiValue
    private String name;

    // @todo REMOVE ME
    @GuiValue
    public static final Value<?, String> VALUE_CONNECTIONS = Value.create("connections", Type.STRING, MoverTileEntity::getConnectionCount, MoverTileEntity::setConnectionCount);
    private String connections = "";

    // @todo a bit clumsy but it works. Better would be a cap in the player
    public static final Set<Integer> wantUnmount = new HashSet<>();

    // Counter to make setting invisible blocks more efficient
    private int cnt;

    // A cache for invisible mover blocks
    private Set<BlockPos> invisibleMoverBlocks = null;

    private final EntityMovementLogic logic = new EntityMovementLogic(this);

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
        updateVehicleStatus();
        logic.tryMoveVehicleServer();
    }

    @Override
    protected void tickClient() {
        //@todo optimize to only render when in front and distance
        handleRender();
    }

    // Return true if there is a direct connection to the given position
    public boolean hasDirectContectionTo(BlockPos destination) {
        return network.containsValue(destination);
    }

    private float prevPartialTicks = Float.NaN;
    private float dpartial = 0;

    private void handleRender() {
        ItemStack vehicle = getCard();
        if (VehicleBuilderTileEntity.isVehicleCard(vehicle)) {
            MoverRenderer.addPreRender(worldPosition, () -> {
                float partialTicks = MoverRenderer.getPartialTicks();
//                Vec3 offset = logic.tryMoveVehicleThisPlayer(partialTicks);
                logic.tryMoveVehicleClientEntities(partialTicks);

            }, this::isMoverThere);
            DelayedRenderer.addRender(worldPosition, (poseStack, cameraVec, renderType) -> {
                float partialTicks = MoverRenderer.getPartialTicks();
                Vec3 offset = logic.tryMoveVehicleThisPlayer(partialTicks);
//                logic.tryMoveVehicleClientEntities(partialTicks + dpartial);
//                if (!Float.isNaN(prevPartialTicks)) {
//                    dpartial = partialTicks-prevPartialTicks;
//                }
//                prevPartialTicks = partialTicks;
                MoverRenderer.actualRender(this, poseStack, cameraVec, vehicle, partialTicks, offset, renderType);
            }, this::isMoverThere);
        }
    }

    @NotNull
    private Boolean isMoverThere(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof MoverTileEntity mover) {
            return !mover.getCard().isEmpty();
        }
        return false;
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
                invisibleMoverBlocks.forEach(p -> {
                    if (level.getBlockState(p) == invisibleState) {
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
                invisibleMoverBlocks.forEach(p -> {
                    if (level.getBlockState(p) != invisibleState && level.getBlockState(p).getMaterial().isReplaceable()) {
                        level.setBlock(p, invisibleState, Block.UPDATE_ALL);
                    }
                });
            }
        }
    }

    private void updateVehicle() {
        ItemStack vehicle = items.getStackInSlot(SLOT_VEHICLE_CARD);
        if (invisibleMoverBlocks == null) {
            invisibleMoverBlocks = new HashSet<>();
        }
        cnt = 0;
        removeInvisibleBlocks();
        if (!vehicle.isEmpty()) {
            Map<BlockState, List<BlockPos>> blocks = VehicleCard.getBlocks(vehicle, worldPosition.offset(1, 1, 1));
            blocks.values().forEach(invisibleMoverBlocks::addAll);
        } else {
            logic.setDestination(null);
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

    public void arriveAtDestination() {
        if (level.getBlockEntity(logic.getDestination()) instanceof MoverTileEntity destMover) {
            destMover.items.setStackInSlot(SLOT_VEHICLE_CARD, getCard());
            items.setStackInSlot(SLOT_VEHICLE_CARD, ItemStack.EMPTY);
            destMover.setSource(null);
            destMover.updateVehicle();
            destMover.updateVehicleStatus();
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
        logic.load(tagCompound);
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
        logic.save(tagCompound);
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
        logic.saveClientDataToNBT(tagCompound);
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tagCompound) {
        CompoundTag tag = tagCompound.getCompound("card");
        items.setStackInSlot(SLOT_VEHICLE_CARD, ItemStack.of(tag));
        logic.loadClientDataFromNBT(tagCompound);
    }
}
