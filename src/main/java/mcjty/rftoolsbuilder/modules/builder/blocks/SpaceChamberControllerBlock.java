package mcjty.rftoolsbuilder.modules.builder.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static mcjty.lib.builder.TooltipBuilder.*;

public class SpaceChamberControllerBlock extends BaseBlock {

    public SpaceChamberControllerBlock() {
        super(new BlockBuilder()
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
        // @todo WRONG TAG FOR CHANNEL
        if (tag != null) {
            channel = tag.getInt("channel");
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
    protected boolean wrenchUse(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
        if (world.isRemote) {
            SoundEvent pling = SoundEvent.REGISTRY.getObject(new ResourceLocation("block.note.pling"));
            world.playSound(pos.getX(), pos.getY(), pos.getZ(), pling, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
        } else {
            SpaceChamberControllerTileEntity chamberControllerTileEntity = (SpaceChamberControllerTileEntity) world.getTileEntity(pos);
            chamberControllerTileEntity.createChamber(player);
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, entityLivingBase, itemStack);
        if (!world.isRemote) {
            SpaceChamberRepository chamberRepository = SpaceChamberRepository.getChannels(world);
            SpaceChamberControllerTileEntity te = (SpaceChamberControllerTileEntity) world.getTileEntity(pos);
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
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            SpaceChamberRepository chamberRepository = SpaceChamberRepository.getChannels(world);
            SpaceChamberControllerTileEntity te = (SpaceChamberControllerTileEntity) world.getTileEntity(pos);
            if (te.getChannel() != -1) {
                chamberRepository.deleteChannel(te.getChannel());
                chamberRepository.save();
            }
        }
        super.breakBlock(world, pos, state);
    }


    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }
}
