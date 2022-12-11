package mcjty.rftoolsbuilder.modules.mover.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.NBTTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static mcjty.lib.builder.TooltipBuilder.*;

/**
 * A vehicle is stored in the card as a list of compounds with each compound equal to:
 *   {
 *       "state": <the blockstate compound>
 *       "blocks": [ relative positions converted to int ]
 *   }
 */
public class VehicleCard extends Item implements ITooltipSettings {

    private final Lazy<TooltipBuilder> tooltipBuilder = () -> new TooltipBuilder()
            .info(key("message.rftoolsbuilder.shiftmessage"))
            .infoShift(header(),
                    parameter("name", VehicleCard::getVehicleName),
                    parameter("contents", VehicleCard::getContentsDescription));

    public VehicleCard() {
        super(Registration.createStandardProperties().stacksTo(1));
    }

    public static String getVehicleName(ItemStack stack) {
        return NBTTools.getString(stack, "vehicleName", "<unknown>");
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
    public void appendHoverText(@Nonnull ItemStack itemStack, Level world, @Nonnull List<Component> list, @Nonnull TooltipFlag flag) {
        super.appendHoverText(itemStack, world, list, flag);
        tooltipBuilder.get().makeTooltip(Tools.getId(this), itemStack, list, flag);
    }

    public static void storeVehicleInCard(ItemStack vehicleCard, Map<BlockState, List<Integer>> blocks, String vehicleName) {
        ListTag list = new ListTag();
        blocks.forEach((state, positions) -> {
            CompoundTag tag = new CompoundTag();
            tag.put("state", NbtUtils.writeBlockState(state));
            tag.putIntArray("blocks", positions);
            list.add(tag);
        });
        vehicleCard.getOrCreateTag().put("blocks", list);
        vehicleCard.getOrCreateTag().putString("vehicleName", vehicleName);
    }

    public static void setDesiredDestination(ItemStack vehicleCard, BlockPos pos) {
        vehicleCard.getOrCreateTag().putIntArray("desiredPos", new int[] { pos.getX(), pos.getY(), pos.getZ()});
    }

    @Nullable
    public static BlockPos getDesiredDestination(ItemStack vehicleCard) {
        CompoundTag tag = vehicleCard.getTag();
        if (tag == null ) {
            return null;
        }
        if (tag.contains("desiredPos")) {
            int[] desiredPos = tag.getIntArray("desiredPos");
            return new BlockPos(desiredPos[0], desiredPos[1], desiredPos[2]);
        }
        return null;
    }

    public static void clearDesiredDestination(ItemStack vehicleCard) {
        vehicleCard.getOrCreateTag().remove("desiredPos");
    }

    public static Map<BlockState, List<BlockPos>> getBlocks(ItemStack vehicleCard, BlockPos minPos) {
        Map<BlockState, List<BlockPos>> result = new HashMap<>();
        CompoundTag compoundTag = vehicleCard.getTag();
        if (compoundTag != null) {
            ListTag list = compoundTag.getList("blocks", Tag.TAG_COMPOUND);
            for (Tag tag : list) {
                CompoundTag c = (CompoundTag) tag;
                BlockState state = NbtUtils.readBlockState(c.getCompound("state"));
                int[] blocks = c.getIntArray("blocks");
                List<BlockPos> blockPosList = Arrays.stream(blocks).mapToObj(i -> convertIntToPos(minPos, i)).collect(Collectors.toList());
                result.put(state, blockPosList);
            }

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
