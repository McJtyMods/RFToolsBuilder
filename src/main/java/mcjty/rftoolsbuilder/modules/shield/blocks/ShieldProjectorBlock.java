package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.crafting.INBTPreservingIngredient;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbase.modules.various.items.SmartWrenchItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import static mcjty.lib.builder.TooltipBuilder.*;

public class ShieldProjectorBlock extends BaseBlock implements INBTPreservingIngredient {

    public ShieldProjectorBlock(Supplier<TileEntity> te, int max) {
        super(new BlockBuilder()
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
        return null;    // @todo 1.14
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
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
// @todo 1.14
        //        restoreBlockFromNBT(world, pos, stack);
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        setOwner(world, pos, placer);
    }

    @Override
    public void onBlockClicked(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (!world.isRemote) {
            composeDecomposeShield(world, pos, true);
            // @todo achievements
//            Achievements.trigger(playerIn, Achievements.shieldSafety);
        }
    }

    @Override
    protected boolean wrenchUse(World world, BlockPos pos, Direction side, PlayerEntity player) {
        composeDecomposeShield(world, pos, false);
        // @todo achievements
//        Achievements.trigger(player, Achievements.shieldSafety);
        return true;
    }

    @Override
    protected boolean wrenchSneakSelect(World world, BlockPos pos, PlayerEntity player) {
        if (!world.isRemote) {
            Optional<GlobalCoordinate> currentBlock = SmartWrenchItem.getCurrentBlock(player.getHeldItem(Hand.MAIN_HAND));
            if (!currentBlock.isPresent()) {
                SmartWrenchItem.setCurrentBlock(player.getHeldItem(Hand.MAIN_HAND), new GlobalCoordinate(pos, world));
                Logging.message(player, TextFormatting.YELLOW + "Selected block");
            } else {
                SmartWrenchItem.setCurrentBlock(player.getHeldItem(Hand.MAIN_HAND), null);
                Logging.message(player, TextFormatting.YELLOW + "Cleared selected block");
            }
        }
        return true;
    }

    private void composeDecomposeShield(World world, BlockPos pos, boolean ctrl) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof ShieldProjectorTileEntity) {
                ((ShieldProjectorTileEntity)te).composeDecomposeShield(ctrl);
            }
        }
    }

    @Override
    public void onReplaced(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState newstate, boolean isMoving) {
        if (newstate.getBlock() != this) {
            removeShield(world, pos);
        }
        super.onReplaced(state, world, pos, newstate, isMoving);
    }

    @Override
    public void onPlayerDestroy(IWorld world, BlockPos pos, BlockState state) {
        removeShield(world, pos);
        super.onPlayerDestroy(world, pos, state);
    }

    @Override
    public void onExplosionDestroy(World world, BlockPos pos, Explosion explosionIn) {
        removeShield(world, pos);
        super.onExplosionDestroy(world, pos, explosionIn);
    }

    private void removeShield(IWorld world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ShieldProjectorTileEntity) {
            if (!world.getWorld().isRemote) {
                ShieldProjectorTileEntity shieldTileEntity = (ShieldProjectorTileEntity) te;
                if (shieldTileEntity.isShieldComposed()) {
                    shieldTileEntity.decomposeShield();
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
