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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;

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
    private String other;

    @GuiValue
    public static final Value<?, String> VALUE_CONNECTIONS = Value.create("connections", Type.STRING, MoverTileEntity::getConnectionCount, MoverTileEntity::setConnectionCount);
    private String connections = "";

    // Counter to make setting invisible blocks more efficient
    private int cnt;

    // A cache for invisible mover blocks
    private Set<BlockPos> invisibleMoverBlocks = null;

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
        tryMoveVehicle();
    }

    private void tryMoveVehicle() {
        ItemStack vehicle = items.getStackInSlot(SLOT_VEHICLE_CARD);
        if (isMachineEnabled() && !vehicle.isEmpty()) {
            // We are enabled and a vehicle is present. Try to move it
        }
    }

    private void updateVehicleStatus() {
        if (invisibleMoverBlocks == null) {
            updateVehicle();
            cnt = 0;
        }
        cnt--;
        if (cnt <= 0) {
            cnt = 10;
            BlockState invisibleState = MoverModule.INVISIBLE_MOVER_BLOCK.get().defaultBlockState();
            invisibleMoverBlocks.forEach(p -> {
                if (level.getBlockState(p) != invisibleState && level.getBlockState(p).getMaterial().isReplaceable()) {
                    level.setBlock(p, invisibleState, Block.UPDATE_ALL);
                }
            });
        }
    }

    @Override
    protected void tickClient() {
        //@todo optimize to only render when in front and distance
        ItemStack card = getCard();
        if (VehicleBuilderTileEntity.isVehicleCard(card)) {
            DelayedRenderer.addRender(worldPosition, (poseStack, cameraVec) -> {
                MoverRenderer.actualRender(this, poseStack, cameraVec, card);
            }, (level, pos) -> level.getBlockState(pos).getBlock() == MoverModule.MOVER.get());
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
    }

    @Override
    public void loadInfo(CompoundTag tagCompound) {
        super.loadInfo(tagCompound);
        CompoundTag info = tagCompound.getCompound("Info");
        name = info.getString("name");
        other = info.getString("other");
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tagCompound) {
        super.saveAdditional(tagCompound);
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            if (network.containsKey(direction)) {
                tagCompound.put(direction.name(), NbtUtils.writeBlockPos(network.get(direction)));
            }
        }
    }

    @Override
    public void saveInfo(CompoundTag tagCompound) {
        super.saveInfo(tagCompound);
        CompoundTag info = getOrCreateInfo(tagCompound);
        if (name != null) {
            info.putString("name", name);
        }
        if (other != null) {
            info.putString("other", other);
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
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tagCompound) {
        CompoundTag tag = tagCompound.getCompound("card");
        items.setStackInSlot(SLOT_VEHICLE_CARD, ItemStack.of(tag));
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
        network.put(comingFrom.getOpposite(), other);
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
