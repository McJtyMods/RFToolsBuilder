package mcjty.rftoolsbuilder.modules.builder.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.builder.SpaceChamberControllerTileEntity;
import mcjty.rftools.setup.GuiProxy;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;

import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.*;

public class SpaceChamberCardItem extends Item implements ITooltipSettings {

    private final Lazy<TooltipBuilder> tooltipBuilder = () -> new TooltipBuilder()
            .info(key("message.rftoolsbuilder.shiftmessage"))
            .infoShift(header(), gold(),
                    parameter("channel", this::getChannelDescription)
                    );

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
    public void appendHoverText(ItemStack itemStack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        super.appendHoverText(itemStack, world, list, flag);
        // Use custom RL so that we don't have to duplicate the translation for every shape card
        tooltipBuilder.get().makeTooltip(new ResourceLocation(RFToolsBuilder.MODID, "shape_card"), itemStack, list, flag);
    }

    // @todo lang file
//    @Override
//    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
//        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
//            list.add(TextFormatting.WHITE + "Sneak right-click on a space chamber controller");
//            list.add(TextFormatting.WHITE + "to set the channel for this card.");
//            list.add(TextFormatting.WHITE + "Right-click in the air to show an overview of");
//            list.add(TextFormatting.WHITE + "the area contents.");
//            list.add(TextFormatting.WHITE + "Insert it in a builder to copy/move the");
//            list.add(TextFormatting.WHITE + "linked area");
//            list.add(TextFormatting.GREEN + "Base cost: " + BuilderConfiguration.builderRfPerOperation.get() + " RF/t per block");
//            list.add(TextFormatting.GREEN + "(final cost depends on infusion level)");
//        } else {
//            list.add(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE);
//        }
//    }


    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!player.isCrouching()) {
            showDetails(world, player, player.getItemInHand(hand));
        }
        return super.use(world, player, hand);
    }

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
            showDetails(world, player, stack);
        } else {
            tagCompound.setInteger("channel", channel);
            if (world.isRemote) {
                Logging.message(player, "Card is set to channel '" + channel + "'");
            }
        }
        return EnumActionResult.SUCCESS;
    }

    private void showDetails(World world, PlayerEntity player, ItemStack stack) {
        if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("channel")) {
            int channel = stack.getTagCompound().getInteger("channel");
            if (channel != -1) {
                showDetailsGui(world, player);
            } else {
                Logging.message(player, TextFormatting.YELLOW + "Card is not linked!");
            }
        }
    }

    private void showDetailsGui(World world, EntityPlayer player) {
        if (world.isRemote) {
            player.openGui(RFTools.instance, GuiProxy.GUI_CHAMBER_DETAILS, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
        }
    }

}