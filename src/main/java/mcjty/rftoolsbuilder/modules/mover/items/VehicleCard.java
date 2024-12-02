package mcjty.rftoolsbuilder.modules.mover.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.data.VehicleData;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mcjty.lib.builder.TooltipBuilder.parameter;

/**
 * A vehicle is stored in the card as a list of compounds with each compound equal to:
 *   {
 *       "state": <the blockstate compound>
 *       "blocks": [ relative positions converted to int ]
 *   }
 */
public class VehicleCard extends Item implements ITooltipSettings {

    private final Lazy<TooltipBuilder> tooltipBuilder = Lazy.of(() -> new TooltipBuilder()
            .info(
                    parameter("name", VehicleCard::getVehicleName),
                    parameter("destination", VehicleCard::isMoving, VehicleCard::getDesiredDestinationName),
                    parameter("contents", VehicleCard::getContentsDescription)));

    public VehicleCard() {
        super(Registration.createStandardProperties().stacksTo(1));
    }

    public static String getVehicleName(ItemStack stack) {
        VehicleData data = stack.getOrDefault(MoverModule.ITEM_VEHICLE_DATA.get(), VehicleData.DEFAULT);
        return data.name();
    }

    private static String getContentsDescription(ItemStack stack) {
        Map<BlockState, List<BlockPos>> blocks = getBlocks(stack, BlockPos.ZERO);
        int cnt = 0;
        for (List<BlockPos> list : blocks.values()) {
            cnt += list.size();
        }
        return cnt + " blocks";
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack itemStack, TooltipContext context, @Nonnull List<Component> list, @Nonnull TooltipFlag flag) {
        super.appendHoverText(itemStack, context, list, flag);
        tooltipBuilder.get().makeTooltip(Tools.getId(this), itemStack, list, flag);
    }

    public static void storeVehicleInCard(ItemStack vehicleCard, Map<BlockState, List<Integer>> blocks, String vehicleName) {
        List<VehicleData.StateWithCount> states = blocks.entrySet().stream().map(e -> new VehicleData.StateWithCount(e.getKey(), e.getValue())).toList();
        VehicleData data = vehicleCard.getOrDefault(MoverModule.ITEM_VEHICLE_DATA.get(), VehicleData.DEFAULT);
        data = data.withStates(states).withName(vehicleName);
        vehicleCard.set(MoverModule.ITEM_VEHICLE_DATA.get(), data);
    }

    public static void setDesiredDestination(ItemStack vehicleCard, BlockPos pos, String name) {
        VehicleData data = vehicleCard.getOrDefault(MoverModule.ITEM_VEHICLE_DATA.get(), VehicleData.DEFAULT);
        data = data.withDesiredPos(pos).withDesiredPosName(name);
        vehicleCard.set(MoverModule.ITEM_VEHICLE_DATA.get(), data);
    }

    public static void clearDesiredDestination(ItemStack vehicleCard) {
        VehicleData data = vehicleCard.getOrDefault(MoverModule.ITEM_VEHICLE_DATA.get(), VehicleData.DEFAULT);
        data = data.withDesiredPos(BlockPos.ZERO).withDesiredPosName("");
        vehicleCard.set(MoverModule.ITEM_VEHICLE_DATA.get(), data);
    }


    @Nullable
    public static BlockPos getDesiredDestination(ItemStack vehicleCard) {
        VehicleData data = vehicleCard.getOrDefault(MoverModule.ITEM_VEHICLE_DATA.get(), VehicleData.DEFAULT);
        if (data.desiredPosName().isEmpty()) {
            return null;
        }
        return data.desiredPos();
    }

    private static boolean isMoving(ItemStack vehicleCard) {
        VehicleData data = vehicleCard.getOrDefault(MoverModule.ITEM_VEHICLE_DATA.get(), VehicleData.DEFAULT);
        return !data.desiredPosName().isEmpty();
    }

    @Nullable
    public static String getDesiredDestinationName(ItemStack vehicleCard) {
        VehicleData data = vehicleCard.getOrDefault(MoverModule.ITEM_VEHICLE_DATA.get(), VehicleData.DEFAULT);
        String s = data.desiredPosName();
        if (s.isEmpty()) {
            return null;
        }
        return s;
    }

    public static Map<BlockState, List<BlockPos>> getBlocks(ItemStack vehicleCard, BlockPos minPos) {
        // @todo avoid translation
        Map<BlockState, List<BlockPos>> result = new HashMap<>();
        VehicleData data = vehicleCard.getOrDefault(MoverModule.ITEM_VEHICLE_DATA.get(), VehicleData.DEFAULT);
        for (VehicleData.StateWithCount stateWithCount : data.states()) {
            BlockState state = stateWithCount.state();
            List<BlockPos> blockPosList = stateWithCount.positions().stream().map(i -> convertIntToPos(minPos, i)).toList();
            result.put(state, blockPosList);
        }
        return result;
    }

    public static int convertPosToInt(BlockPos min, BlockPos current) {
        int dx = current.getX() - min.getX();
        int dy = current.getY() - min.getY();
        int dz = current.getZ() - min.getZ();
        return dx << 20 | dy << 10 | dz;
    }

    public static BlockPos convertIntToPos(BlockPos min, int current) {
        return new BlockPos(min.getX() + ((current >> 20) & 0x3f),
                min.getY() + ((current >> 10) & 0x3f),
                min.getZ() + (current & 0x3f));
    }

}
