package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.lib.McJtyLib;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.crafting.INBTPreservingIngredient;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbase.modules.various.SmartWrenchItem;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

//@Optional.InterfaceList({
//        @Optional.Interface(iface = "crazypants.enderio.api.redstone.IRedstoneConnectable", modid = "EnderIO")})
public class ShieldProjectorBlock extends BaseBlock implements INBTPreservingIngredient
        /*, IRedstoneConnectable*/ {

    private final int max;

    public ShieldProjectorBlock(Supplier<TileEntity> te, int max) {
        super(new BlockBuilder()
            .tileEntitySupplier(te));
        this.max = max;
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> list, ITooltipFlag advanced) {
        super.addInformation(stack, world, list, advanced);

        list.add(new StringTextComponent(TextFormatting.GREEN + "Supports " + max + " blocks"));

        if (McJtyLib.proxy.isShiftKeyDown()) {
            list.add(new StringTextComponent(TextFormatting.WHITE + "This machine forms a shield out of adjacent"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "template blocks. It can filter based on type of"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "mob and do various things (damage, solid, ...)"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "Use the Smart Wrench to add sections to the shield"));
            list.add(new StringTextComponent(TextFormatting.RED + "Note: block mimic is not implemented yet!"));
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Infusing bonus: reduced power consumption and"));
            list.add(new StringTextComponent(TextFormatting.YELLOW + "increased damage."));
        } else {
            list.add(new StringTextComponent(TextFormatting.WHITE + RFToolsBuilder.SHIFT_MESSAGE));
        }
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
            GlobalCoordinate currentBlock = SmartWrenchItem.getCurrentBlock(player.getHeldItem(Hand.MAIN_HAND));
            if (currentBlock == null) {
                SmartWrenchItem.setCurrentBlock(player.getHeldItem(Hand.MAIN_HAND), new GlobalCoordinate(pos, world.getDimension().getType()));
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
    public void onPlayerDestroy(IWorld world, BlockPos pos, BlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ShieldProjectorTileEntity) {
            if (!world.getWorld().isRemote) {
                ShieldProjectorTileEntity shieldTileEntity = (ShieldProjectorTileEntity) te;
                if (shieldTileEntity.isShieldComposed()) {
                    shieldTileEntity.decomposeShield();
                }
            }
        }

        super.onPlayerDestroy(world, pos, state);
    }

    @Override
    public void onExplosionDestroy(World world, BlockPos pos, Explosion explosionIn) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ShieldProjectorTileEntity) {
            if (!world.getWorld().isRemote) {
                ShieldProjectorTileEntity shieldTileEntity = (ShieldProjectorTileEntity) te;
                if (shieldTileEntity.isShieldComposed()) {
                    shieldTileEntity.decomposeShield();
                }
            }
        }

        super.onExplosionDestroy(world, pos, explosionIn);
    }

    //
//    @Override
//    public boolean shouldRedstoneConduitConnect(World world, int x, int y, int z, Direction from) {
//        return true;
//    }
}
