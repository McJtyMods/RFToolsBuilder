package mcjty.rftoolsbuilder.modules.builder.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import mcjty.rftoolsbuilder.modules.builder.SpaceChamberRepository;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

import static mcjty.lib.builder.TooltipBuilder.*;

public class SpaceChamberControllerBlock extends BaseBlock {

    public SpaceChamberControllerBlock() {
        super(new BlockBuilder()
                .properties(BlockBehaviour.Properties.of(Material.METAL)
                        .strength(2.0f)
                        .sound(SoundType.METAL)
                        .noOcclusion())
                .topDriver(RFToolsBuilderTOPDriver.DRIVER)
                .tileEntitySupplier(SpaceChamberControllerTileEntity::new)
//                .manualEntry(ManualHelper.create("rftoolsbuilder:builder/builder_intro"))
                .info(key("message.rftoolsbuilder.shiftmessage"))
                .infoShift(header(),
                        parameter("channel", SpaceChamberControllerBlock::getChannelDescription)
                ));
    }

    private static String getChannelDescription(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        int channel = -1;
        CompoundTag info = tag == null ? null : tag.getCompound("BlockEntityTag").getCompound("Info");
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

    @Override
    protected boolean wrenchUse(Level level, BlockPos pos, Direction side, Player player) {
        if (level.isClientSide) {
            // @todo 1.19.3 .get()
            SoundEvent pling = SoundEvents.NOTE_BLOCK_BELL;
            level.playSound(player, pos, pling, SoundSource.BLOCKS, 1.0f, 1.0f);
        } else {
            SpaceChamberControllerTileEntity chamberControllerTileEntity = (SpaceChamberControllerTileEntity) level.getBlockEntity(pos);
            chamberControllerTileEntity.createChamber(player);
        }
        return true;
    }

    @Override
    public void onPlace(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state2, boolean p_220082_5_) {
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
    public void onRemove(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState newstate, boolean isMoving) {
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
