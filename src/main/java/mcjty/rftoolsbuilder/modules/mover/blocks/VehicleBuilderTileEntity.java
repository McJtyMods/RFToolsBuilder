package mcjty.rftoolsbuilder.modules.mover.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.bindings.GuiValue;
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
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.BuilderTools;
import mcjty.rftoolsbuilder.modules.builder.SpaceChamberRepository;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleCard;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(2)
            .slot(specific(BuilderModule.SPACE_CHAMBER_CARD.get()).in(), SLOT_SPACE_CARD, 64, 24)
            .slot(specific(MoverModule.VEHICLE_CARD.get()).in().out(), SLOT_VEHICLE_CARD, 118, 24)
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
    private final LazyOptional<MenuProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Vehicle Builder")
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
        for (int x = minCorner.getX(); x <= maxCorner.getX(); x++) {
            mpos.setX(x);
            for (int y = minCorner.getY(); y <= maxCorner.getY(); y++) {
                mpos.setY(y);
                for (int z = minCorner.getZ(); z <= maxCorner.getZ(); z++) {
                    mpos.setZ(z);
                    BlockState state = world.getBlockState(mpos);
                    if (!state.isAir()) {
                        blocks.computeIfAbsent(state, s -> new ArrayList<>()).add(VehicleCard.convertPosToInt(minCorner, mpos));
                    }
                }
            }
        }
        return blocks;
    }

    private boolean checkValid(Player player, BlockPos minCorner, BlockPos maxCorner) {
        if (maxCorner.getX() - minCorner.getX() >= MAXDIM) {
            player.sendMessage(new TextComponent("Space chamber too large (max 16x16x16)!"), Util.NIL_UUID);
            return false;
        }
        if (maxCorner.getY() - minCorner.getY() >= MAXDIM) {
            player.sendMessage(new TextComponent("Space chamber too large (max 16x16x16)!"), Util.NIL_UUID);
            return false;
        }
        if (maxCorner.getZ() - minCorner.getZ() >= MAXDIM) {
            player.sendMessage(new TextComponent("Space chamber too large (max 16x16x16)!"), Util.NIL_UUID);
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

    @ServerCommand
    public static final Command<?> CMD_CREATE = Command.<VehicleBuilderTileEntity>create("create", (te, player, params) -> te.copyVehicle(player));
}
