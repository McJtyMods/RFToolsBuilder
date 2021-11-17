package mcjty.rftoolsbuilder.modules.builder.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderConfiguration;
import mcjty.rftoolsbuilder.modules.builder.blocks.SpaceChamberControllerTileEntity;
import mcjty.rftoolsbuilder.modules.builder.client.GuiChamberDetails;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
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
                    general("extra", TextFormatting.GRAY)
                    );

    private String getCostDescription(ItemStack stack) {
        return BuilderConfiguration.builderRfPerOperation.get() + " RF/t per block";
    }

    private String getChannelDescription(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
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
        super(new Properties().stacksTo(1).defaultDurability(0).tab(RFToolsBuilder.setup.getTab()));
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack itemStack, World world, @Nonnull List<ITextComponent> list, @Nonnull ITooltipFlag flag) {
        super.appendHoverText(itemStack, world, list, flag);
        tooltipBuilder.get().makeTooltip(getRegistryName(), itemStack, list, flag);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> use(@Nonnull World world, PlayerEntity player, @Nonnull Hand hand) {
        if (!player.isCrouching()) {
            showDetails(world, player, player.getItemInHand(hand));
        }
        return super.use(world, player, hand);
    }

    @Nonnull
    @Override
    public ActionResultType useOn(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        ItemStack stack = player.getItemInHand(hand);
        World level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        TileEntity te = level.getBlockEntity(pos);
        CompoundNBT tagCompound = stack.getOrCreateTag();

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
        return ActionResultType.SUCCESS;
    }

    private void showDetails(World world, PlayerEntity player, ItemStack stack) {
        if (stack.getTag() != null && stack.getTag().contains("channel")) {
            int channel = stack.getTag().getInt("channel");
            if (channel != -1) {
                showDetailsGui(world, player);
            } else {
                Logging.message(player, TextFormatting.YELLOW + "Card is not linked!");
            }
        }
    }

    private void showDetailsGui(World world, PlayerEntity player) {
        if (world.isClientSide) {
            GuiChamberDetails.open();
        }
    }

}