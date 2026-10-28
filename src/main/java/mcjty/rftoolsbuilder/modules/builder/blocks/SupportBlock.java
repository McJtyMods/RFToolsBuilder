package mcjty.rftoolsbuilder.modules.builder.blocks;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class SupportBlock extends TransparentBlock {

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

        @NotNull
        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final EnumProperty<SupportStatus> STATUS = EnumProperty.create("status", SupportStatus.class);

    public SupportBlock(BlockBehaviour.Properties properties) {
        super(properties.replaceable().noOcclusion().isRedstoneConductor((state, world, pos) -> false));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult result) {
        if (!level.isClientSide()) {
            // Find all connected blocks and remove them.
            Deque<BlockPos> todo = new ArrayDeque<>();
            todo.add(pos);
            removeBlock(level, todo);
        }
        return super.useWithoutItem(state, level, pos, player, result);
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

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATUS);
    }


//    @Override
//    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> tab) {
//        super.getSubBlocks(itemIn, tab);
//    }

}
