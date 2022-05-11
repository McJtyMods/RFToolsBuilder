package mcjty.rftoolsbuilder.modules.builder.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.StringRepresentable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Deque;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class SupportBlock extends Block {

    public enum SupportStatus implements StringRepresentable {
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

        @Nonnull
        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final EnumProperty<SupportStatus> STATUS = EnumProperty.create("status", SupportStatus.class);

    public SupportBlock() {
        super(Properties.of(Material.STRUCTURAL_AIR).noOcclusion().isRedstoneConductor((state, world, pos) -> false));
    }

    @Nonnull
    @Override
    public InteractionResult use(@Nonnull BlockState state, Level world, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand handIn, @Nonnull BlockHitResult hit) {
        if (!world.isClientSide) {
            // Find all connected blocks and remove them.
            Deque<BlockPos> todo = new ArrayDeque<>();
            todo.add(pos);
            removeBlock(world, todo);
        }
        return super.use(state, world, pos, player, handIn, hit);
    }

    private void removeBlock(Level world, Deque<BlockPos> todo) {
        while (!todo.isEmpty()) {
            BlockPos c = todo.pollFirst();
            world.setBlockAndUpdate(c, Blocks.AIR.defaultBlockState());
            for (int dx = -1 ; dx <= 1 ; dx++) {
                for (int dy = -1 ; dy <= 1 ; dy++) {
                    for (int dz = -1 ; dz <= 1 ; dz++) {
                        if (dx != 0 || dy != 0 || dz != 0) {
                            BlockPos offset = c.offset(dx, dy, dz);
                            if (world.getBlockState(offset).getBlock() == this) {
                                todo.push(offset);
                            }
                        }
                    }
                }
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATUS);
    }


//    @Override
//    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> tab) {
//        super.getSubBlocks(itemIn, tab);
//    }

}
