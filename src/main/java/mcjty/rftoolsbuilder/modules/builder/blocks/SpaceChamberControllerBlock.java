package mcjty.rftoolsbuilder.modules.builder.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftoolsbuilder.modules.builder.SpaceChamberRepository;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

import static mcjty.lib.builder.TooltipBuilder.*;

public class SpaceChamberControllerBlock extends BaseBlock {

    public SpaceChamberControllerBlock() {
        super(new BlockBuilder()
//                .properties(AbstractBlock.Properties.of(Material.METAL)
//                        .strength(2.0f)
//                        .sound(SoundType.METAL)
//                        .noOcclusion())
                .tileEntitySupplier(SpaceChamberControllerTileEntity::new)
//                .manualEntry(ManualHelper.create("rftoolsbuilder:builder/builder_intro"))
                .info(key("message.rftoolsbuilder.shiftmessage"))
                .infoShift(header(), gold(),
                        parameter("channel", SpaceChamberControllerBlock::getChannelDescription)
                ));
    }

    private static String getChannelDescription(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        int channel = -1;
        CompoundNBT info = tag == null ? null : tag.getCompound("BlockEntityTag").getCompound("Info");
        if (info != null) {
            channel = info.getInt("channel");
        }
        if (channel != -1) {
            return "Channel: " + channel;
        } else {
            return "Channel is not set!";
        }
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

//    @SideOnly(Side.CLIENT)
//    @Override
//    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
//        super.addInformation(itemStack, player, list, whatIsThis);
//        NBTTagCompound tagCompound = itemStack.getTagCompound();
//        if (tagCompound != null) {
//            int channel = tagCompound.getInteger("channel");
//            list.add(TextFormatting.GREEN + "Channel: " + channel);
//        }
//
//        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
//            list.add(TextFormatting.WHITE + "This block is one of the eight corners of an");
//            list.add(TextFormatting.WHITE + "area of space you want to copy/move elsewhere");
//        } else {
//            list.add(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE);
//        }
//    }

//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        TileEntity te = world.getTileEntity(data.getPos());
//        if (te instanceof SpaceChamberControllerTileEntity) {
//            SpaceChamberControllerTileEntity spaceChamberControllerTileEntity = (SpaceChamberControllerTileEntity) te;
//            int channel = spaceChamberControllerTileEntity.getChannel();
//            probeInfo.text(TextFormatting.GREEN + "Channel: " + channel);
//            if (channel != -1) {
//                int size = spaceChamberControllerTileEntity.getChamberSize();
//                if (size == -1) {
//                    probeInfo.text(TextFormatting.YELLOW + "Chamber not formed!");
//                } else {
//                    probeInfo.text(TextFormatting.GREEN + "Area: " + size + " blocks");
//                }
//            }
//        }
//    }


    @Override
    protected boolean wrenchUse(World level, BlockPos pos, Direction side, PlayerEntity player) {
        if (level.isClientSide) {
            SoundEvent pling = SoundEvents.NOTE_BLOCK_BELL;
            level.playSound(player, pos, pling, SoundCategory.BLOCKS, 1.0f, 1.0f);
        } else {
            SpaceChamberControllerTileEntity chamberControllerTileEntity = (SpaceChamberControllerTileEntity) level.getBlockEntity(pos);
            chamberControllerTileEntity.createChamber(player);
        }
        return true;
    }

    @Override
    public void onPlace(BlockState state, World level, BlockPos pos, BlockState state2, boolean p_220082_5_) {
        super.onPlace(state, level, pos, state2, p_220082_5_);
        if (!level.isClientSide) {
            SpaceChamberRepository chamberRepository = SpaceChamberRepository.get(level);
            SpaceChamberControllerTileEntity te = (SpaceChamberControllerTileEntity) level.getBlockEntity(pos);
            if (te.getChannel() == -1) {
                int id = chamberRepository.newChannel();
                te.setChannel(id);
                chamberRepository.save();
            }
            // @todo
//            onNeighborBlockChange(world, pos, state, this);
        }
    }

    @Override
    public void onRemove(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState newstate, boolean isMoving) {
        if (!world.isClientSide) {
            SpaceChamberRepository chamberRepository = SpaceChamberRepository.get(world);
            SpaceChamberControllerTileEntity te = (SpaceChamberControllerTileEntity) world.getBlockEntity(pos);
            if (te.getChannel() != -1) {
                chamberRepository.deleteChannel(te.getChannel());
                chamberRepository.save();
            }
        }
        super.onRemove(state, world, pos, newstate, isMoving);
    }
}
