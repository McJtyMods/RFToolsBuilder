package mcjty.rftoolsbuilder.modules.mover.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.BuilderTools;
import mcjty.rftoolsbuilder.modules.builder.SpaceChamberRepository;
import mcjty.rftoolsbuilder.modules.builder.blocks.RotateMode;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleCard;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mcjty.lib.api.container.DefaultContainerProvider.container;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;
import static mcjty.lib.container.SlotDefinition.specific;

public class VehicleBuilderTileEntity extends GenericTileEntity {

    public static final int SLOT_SPACE_CARD = 0;
    public static final int SLOT_VEHICLE_CARD = 1;

    @GuiValue
    private String vehicleName = "";

    private RotateMode rotate = RotateMode.ROTATE_0;
    @GuiValue
    public static final Value<VehicleBuilderTileEntity, String> VALUE_ROTATE = Value.createEnum("rotate", RotateMode.values(), VehicleBuilderTileEntity::getRotate, VehicleBuilderTileEntity::setRotate);

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(2)
            .slot(specific(BuilderModule.SPACE_CHAMBER_CARD.get()).in(), SLOT_SPACE_CARD, 64, 24)
            .slot(specific(MoverModule.VEHICLE_CARD.get()).in().out(), SLOT_VEHICLE_CARD, 154, 24)
            .playerSlots(10, 70));

    @Cap(type = CapType.ITEMS_AUTOMATION)
    private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY)
            .onUpdate((slot, stack) -> {
                if (stack.getItem() == MoverModule.VEHICLE_CARD.get()) {
                    vehicleName = VehicleCard.getVehicleName(stack);
                } else {
                    vehicleName = "";
                }
            })
            .build();

    @Cap(type = CapType.CONTAINER)
    private final Lazy<MenuProvider> screenHandler = Lazy.of(() -> new DefaultContainerProvider<GenericContainer>("Vehicle Builder")
            .containerSupplier(container(MoverModule.CONTAINER_VEHICLE_BUILDER, CONTAINER_FACTORY,this))
            .itemHandler(() -> items)
            .setupSync(this));

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .tileEntitySupplier(VehicleBuilderTileEntity::new)
                .topDriver(RFToolsBuilderTOPDriver.DRIVER)
                .infusable()
                .manualEntry(ManualHelper.create("rftoolsbuilder:todo"))
                .info(key("message.rftoolsbuilder.shiftmessage"))
                .infoShift(header()));
    }


    public VehicleBuilderTileEntity(BlockPos pos, BlockState state) {
        super(MoverModule.TYPE_VEHICLE_BUILDER.get(), pos, state);
    }

    public GenericItemHandler getItems() {
        return items;
    }

    private static final int MAXDIM = 16;

    public RotateMode getRotate() {
        return rotate;
    }

    public void setRotate(RotateMode rotate) {
        this.rotate = rotate;
        setChanged();
    }


    private void copyVehicle(Player player) {
        ItemStack spaceCard = items.getStackInSlot(SLOT_SPACE_CARD);
        ItemStack vehicleCard = items.getStackInSlot(SLOT_VEHICLE_CARD);
        if (isUsableSpaceCard(spaceCard) && isVehicleCard(vehicleCard)) {
            SpaceChamberRepository.SpaceChamberChannel chamberChannel = BuilderTools.getSpaceChamberChannel(level, spaceCard);
            if (chamberChannel != null) {
                BlockPos minCorner = chamberChannel.getMinCorner();
                BlockPos maxCorner = chamberChannel.getMaxCorner();
                if (checkValid(player, minCorner, maxCorner)) {
                    ResourceKey<Level> dimension = chamberChannel.getDimension();
                    ServerLevel world = LevelTools.getLevel(this.level, dimension);
                    var blocks = getBlocks(minCorner, maxCorner, world);
                    VehicleCard.storeVehicleInCard(vehicleCard, blocks, vehicleName);
                }
            }
        }
    }

    @NotNull
    private Map<BlockState, List<Integer>> getBlocks(BlockPos minCorner, BlockPos maxCorner, ServerLevel world) {
        Map<BlockState, List<Integer>> blocks = new HashMap<>();
        var mpos = new BlockPos.MutableBlockPos(0, 0, 0);
        Rotation rotation = switch (rotate) {
            case ROTATE_0 -> Rotation.NONE;
            case ROTATE_90 -> Rotation.CLOCKWISE_90;
            case ROTATE_180 -> Rotation.CLOCKWISE_180;
            case ROTATE_270 -> Rotation.COUNTERCLOCKWISE_90;
        };
        BlockPos realMin;
        if (rotation == Rotation.NONE) {
            realMin = minCorner;
        } else {
            realMin = new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
            for (int x = minCorner.getX(); x <= maxCorner.getX(); x++) {
                mpos.setX(x);
                for (int y = minCorner.getY(); y <= maxCorner.getY(); y++) {
                    mpos.setY(y);
                    for (int z = minCorner.getZ(); z <= maxCorner.getZ(); z++) {
                        mpos.setZ(z);
                        BlockPos rotated = mpos.rotate(rotation);
                        realMin = new BlockPos(Math.min(realMin.getX(), rotated.getX()), Math.min(realMin.getY(), rotated.getY()), Math.min(realMin.getZ(), rotated.getZ()));
                    }
                }
            }
        }

        for (int x = minCorner.getX(); x <= maxCorner.getX(); x++) {
            mpos.setX(x);
            for (int y = minCorner.getY(); y <= maxCorner.getY(); y++) {
                mpos.setY(y);
                for (int z = minCorner.getZ(); z <= maxCorner.getZ(); z++) {
                    mpos.setZ(z);
                    BlockState state = world.getBlockState(mpos);
                    if (!state.isAir()) {
                        BlockPos p;
                        if (rotation != Rotation.NONE) {
                            if (state.getBlock() instanceof MoverControlBlock) {
                                System.out.println("Before: " + state + ", Rotation: " + rotation);
                            }
                            state = state.rotate(rotation);
                            if (state.getBlock() instanceof MoverControlBlock) {
                                System.out.println("After: " + state);
                            }
                            p = mpos.rotate(rotation);
                        } else {
                            p = mpos;
                        }
                        blocks.computeIfAbsent(state, s -> new ArrayList<>()).add(VehicleCard.convertPosToInt(realMin, p));
                    }
                }
            }
        }
        return blocks;
    }

    private void rotatePos(BlockPos.MutableBlockPos pos, Rotation rotation) {
        switch (rotation) {
            case NONE -> {
            }
            case CLOCKWISE_90 -> {
                pos.set(-pos.getZ(), pos.getY(), pos.getX());
            }
            case CLOCKWISE_180 -> {
                pos.set(-pos.getX(), pos.getY(), -pos.getZ());
            }
            case COUNTERCLOCKWISE_90 -> {
                pos.set(pos.getZ(), pos.getY(), -pos.getX());
            }
        }
    }

    private boolean checkValid(Player player, BlockPos minCorner, BlockPos maxCorner) {
        if (maxCorner.getX() - minCorner.getX() >= MAXDIM) {
            player.sendSystemMessage(ComponentFactory.literal("Space chamber too large (max 16x16x16)!"));
            return false;
        }
        if (maxCorner.getY() - minCorner.getY() >= MAXDIM) {
            player.sendSystemMessage(ComponentFactory.literal("Space chamber too large (max 16x16x16)!"));
            return false;
        }
        if (maxCorner.getZ() - minCorner.getZ() >= MAXDIM) {
            player.sendSystemMessage(ComponentFactory.literal("Space chamber too large (max 16x16x16)!"));
            return false;
        }
        return true;
    }

    public static boolean isUsableSpaceCard(ItemStack stack) {
        if (stack.getItem() != BuilderModule.SPACE_CHAMBER_CARD.get()) {
            return false;
        }
        return BuilderTools.getChannel(stack) != null;
    }

    public static boolean isVehicleCard(ItemStack stack) {
        if (stack.getItem() != MoverModule.VEHICLE_CARD.get()) {
            return false;
        }
        return true;
    }

    @Override
    protected void saveInfo(CompoundTag tagCompound) {
        super.saveInfo(tagCompound);
        getOrCreateInfo(tagCompound).putInt("rotate", rotate.ordinal());
    }

    @Override
    protected void loadInfo(CompoundTag tagCompound) {
        super.loadInfo(tagCompound);
        if (tagCompound.contains("Info")) {
            CompoundTag info = tagCompound.getCompound("Info");
            if (info.contains("rotate")) {
                rotate = RotateMode.values()[info.getInt("rotate")];
            }
        }
    }

    @ServerCommand
    public static final Command<?> CMD_CREATE = Command.<VehicleBuilderTileEntity>create("create", (te, player, params) -> te.copyVehicle(player));
}
