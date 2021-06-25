package mcjty.rftoolsbuilder.modules.builder.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.Deque;

import net.minecraft.block.AbstractBlock.Properties;

public class SupportBlock extends Block {

    public static final int STATUS_OK = 0;
    public static final int STATUS_WARN = 1;
    public static final int STATUS_ERROR = 2;

    public static IntegerProperty STATUS = IntegerProperty.create("status", 0, 2);

    public SupportBlock() {
        super(Properties.of(Material.GLASS).isRedstoneConductor((state, world, pos) -> false));
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!world.isClientSide) {
            // Find all connected blocks and remove them.
            Deque<BlockPos> todo = new ArrayDeque<>();
            todo.add(pos);
            removeBlock(world, todo);
        }
        return super.use(state, world, pos, player, handIn, hit);
    }

    private void removeBlock(World world, Deque<BlockPos> todo) {
        while (!todo.isEmpty()) {
            BlockPos c = todo.pollFirst();
            world.setBlockAndUpdate(c, Blocks.AIR.defaultBlockState());
            if (world.getBlockState(c.west()).getBlock() == this) {
                todo.push(c.west());
            }
            if (world.getBlockState(c.east()).getBlock() == this) {
                todo.push(c.east());
            }
            if (world.getBlockState(c.below()).getBlock() == this) {
                todo.push(c.below());
            }
            if (world.getBlockState(c.above()).getBlock() == this) {
                todo.push(c.above());
            }
            if (world.getBlockState(c.south()).getBlock() == this) {
                todo.push(c.south());
            }
            if (world.getBlockState(c.north()).getBlock() == this) {
                todo.push(c.north());
            }
        }
    }

    // @todo 1.14
//    @SideOnly(Side.CLIENT)
//    @Override
//    public boolean shouldSideBeRendered(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
//        BlockState state = blockAccess.getBlockState(pos);
//        Block block = state.getBlock();
//        BlockState state2 = blockAccess.getBlockState(pos.offset(side));
//        Block block2 = state2.getBlock();
//        if (block.getMetaFromState(state) != block2.getMetaFromState(state2)) {
//            return true;
//        }
//
//        if (block2 == this) {
//            return false;
//        }
//
//        return block2 != this && super.shouldSideBeRendered(blockState, blockAccess, pos, side);
//    }


    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(STATUS);
    }


//    @Override
//    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> tab) {
//        super.getSubBlocks(itemIn, tab);
//    }

}
