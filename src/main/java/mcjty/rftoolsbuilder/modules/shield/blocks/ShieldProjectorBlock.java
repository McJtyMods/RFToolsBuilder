package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.crafting.INBTPreservingIngredient;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbase.modules.various.items.SmartWrenchItem;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static mcjty.lib.builder.TooltipBuilder.*;

public class ShieldProjectorBlock extends BaseBlock implements INBTPreservingIngredient {

    public ShieldProjectorBlock(BlockEntityType.BlockEntitySupplier<BlockEntity> te, int max) {
        super(new BlockBuilder()
                .manualEntry(ManualHelper.create("rftoolsbuilder:shield/shield_intro"))
                .topDriver(RFToolsBuilderTOPDriver.DRIVER)
                .infusable()
                .info(key("message.rftoolsbuilder.shiftmessage"))
                .infoShift(header(), gold(), parameter("info", stack -> Integer.toString(max)))
                .tileEntitySupplier(te));
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    @Override
    public Collection<String> getTagsToPreserve() {
        return Collections.singleton("BlockEntityTag");
    }

    // @todo 1.14
//    @Override
//    protected IModuleSupport getModuleSupport() {
//        return new ModuleSupport(ShieldContainer.SLOT_SHAPE) {
//            @Override
//            public boolean isModule(ItemStack itemStack) {
//                return itemStack.getItem() == BuilderSetup.shapeCardItem;
//            }
//        };
//    }


    @Override
    public void setPlacedBy(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
// @todo 1.14
        //        restoreBlockFromNBT(world, pos, stack);
        super.setPlacedBy(world, pos, state, placer, stack);
        setOwner(world, pos, placer);
    }

    @Override
    public void attack(@Nonnull BlockState state, Level world, @Nonnull BlockPos pos, @Nonnull Player player) {
        if (!world.isClientSide) {
            composeDecomposeShield(world, pos, true);
            // @todo achievements
//            Achievements.trigger(playerIn, Achievements.shieldSafety);
        }
    }

    @Override
    protected boolean wrenchUse(Level world, BlockPos pos, Direction side, Player player) {
        composeDecomposeShield(world, pos, false);
        // @todo achievements
//        Achievements.trigger(player, Achievements.shieldSafety);
        return true;
    }

    @Override
    protected boolean wrenchSneakSelect(Level world, BlockPos pos, Player player) {
        if (!world.isClientSide) {
            Optional<GlobalPos> currentBlock = SmartWrenchItem.getCurrentBlock(player.getItemInHand(InteractionHand.MAIN_HAND));
            if (!currentBlock.isPresent()) {
                SmartWrenchItem.setCurrentBlock(player.getItemInHand(InteractionHand.MAIN_HAND), GlobalPos.of(world.dimension(), pos));
                Logging.message(player, ChatFormatting.YELLOW + "Selected block");
            } else {
                SmartWrenchItem.setCurrentBlock(player.getItemInHand(InteractionHand.MAIN_HAND), null);
                Logging.message(player, ChatFormatting.YELLOW + "Cleared selected block");
            }
        }
        return true;
    }

    private void composeDecomposeShield(Level world, BlockPos pos, boolean ctrl) {
        if (!world.isClientSide) {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof ShieldProjectorTileEntity) {
                ((ShieldProjectorTileEntity)te).composeDecomposeShield(ctrl);
            }
        }
    }

    @Override
    public void onRemove(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState newstate, boolean isMoving) {
        if (newstate.getBlock() != this) {
            removeShield(world, pos);
        }
        super.onRemove(state, world, pos, newstate, isMoving);
    }

    @Override
    public void destroy(@Nonnull LevelAccessor world, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        removeShield(world, pos);
        super.destroy(world, pos, state);
    }

    @Override
    public void wasExploded(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Explosion explosionIn) {
        removeShield(world, pos);
        super.wasExploded(world, pos, explosionIn);
    }

    private void removeShield(LevelAccessor world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ShieldProjectorTileEntity shield) {
            if (!world.isClientSide()) {
                if (shield.isShieldComposed()) {
                    shield.decomposeShield();
                }
            }
        }
    }


    //
//    @Override
//    public boolean shouldRedstoneConduitConnect(World world, int x, int y, int z, Direction from) {
//        return true;
//    }
}
