package mcjty.rftoolsbuilder.modules.builder.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderConfiguration;
import mcjty.rftoolsbuilder.modules.builder.blocks.SpaceChamberControllerTileEntity;
import mcjty.rftoolsbuilder.modules.builder.client.GuiChamberDetails;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.*;

public class SpaceChamberCardItem extends Item implements ITooltipSettings {

    private final Lazy<TooltipBuilder> tooltipBuilder = () -> new TooltipBuilder()
            .info(key("message.rftoolsbuilder.shiftmessage"))
            .infoShift(header(), gold(),
                    parameter("cost", this::getCostDescription),
                    parameter("channel", this::getChannelDescription),
                    general("extra", ChatFormatting.GRAY)
                    );

    private String getCostDescription(ItemStack stack) {
        return BuilderConfiguration.builderRfPerOperation.get() + " RF/t per block";
    }

    private String getChannelDescription(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        int channel = -1;
        if (tag != null) {
            channel = tag.getInt("channel");
        }
        if (channel != -1) {
            return "Channel: " + channel;
        } else {
            return "Channel is not set!";
        }
    }

    public SpaceChamberCardItem() {
        super(RFToolsBuilder.setup.defaultProperties().stacksTo(1).defaultDurability(0));
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack itemStack, Level world, @Nonnull List<Component> list, @Nonnull TooltipFlag flag) {
        super.appendHoverText(itemStack, world, list, flag);
        tooltipBuilder.get().makeTooltip(Tools.getId(this), itemStack, list, flag);
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level world, Player player, @Nonnull InteractionHand hand) {
        if (!player.isCrouching()) {
            showDetails(world, player, player.getItemInHand(hand));
        }
        return super.use(world, player, hand);
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        ItemStack stack = player.getItemInHand(hand);
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity te = level.getBlockEntity(pos);
        CompoundTag tagCompound = stack.getOrCreateTag();

        int channel = -1;
        if (te instanceof SpaceChamberControllerTileEntity) {
            channel = ((SpaceChamberControllerTileEntity) te).getChannel();
        }

        if (channel == -1) {
            showDetails(level, player, stack);
        } else {
            tagCompound.putInt("channel", channel);
            if (level.isClientSide) {
                Logging.message(player, "Card is set to channel '" + channel + "'");
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void showDetails(Level world, Player player, ItemStack stack) {
        if (stack.getTag() != null && stack.getTag().contains("channel")) {
            int channel = stack.getTag().getInt("channel");
            if (channel != -1) {
                showDetailsGui(world, player);
            } else {
                Logging.message(player, ChatFormatting.YELLOW + "Card is not linked!");
            }
        }
    }

    private void showDetailsGui(Level world, Player player) {
        if (world.isClientSide) {
            GuiChamberDetails.open();
        }
    }

}