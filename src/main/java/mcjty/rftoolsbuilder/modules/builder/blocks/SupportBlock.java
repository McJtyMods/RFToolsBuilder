package mcjty.rftoolsbuilder.modules.builder.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.Deque;

public class SupportBlock extends Block {

    public enum SupportStatus implements IStringSerializable {
        STATUS_OK("ok"),
        STATUS_WARN("warn"),
        STATUS_ERROR("error");

        private final String name;

        SupportStatus(String name) {
            this.name = name;
        }

        public static SupportStatus max(SupportStatus error1, SupportStatus error2) {
            if (error1 == STATUS_ERROR || error2 == STATUS_ERROR) {
                return STATUS_ERROR;
            }
            if (error1 == STATUS_WARN || error2 == STATUS_WARN) {
                return STATUS_WARN;
            }
            return STATUS_OK;
        }

        public String getName() {
            return name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static EnumProperty<SupportStatus> STATUS = EnumProperty.create("status", SupportStatus.class);

    public SupportBlock() {
        super(Properties.of(Material.GLASS).noOcclusion().isRedstoneConductor((state, world, pos) -> false));
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
