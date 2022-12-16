package mcjty.rftoolsbuilder.modules.mover.blocks;

import mcjty.lib.varia.SafeClientTools;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class InvisibleMoverControlBlock extends Block implements EntityBlock {

    public InvisibleMoverControlBlock() {
        super(Properties.of(Material.AIR).noLootTable().noOcclusion().randomTicks());
    }


    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < .1f) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void attack(@Nonnull BlockState state, Level world, @Nonnull BlockPos pos, @Nonnull Player player) {
        if (world.isClientSide) {
            HitResult mouseOver = SafeClientTools.getClientMouseOver();
            if (world.getBlockEntity(pos) instanceof InvisibleMoverControlBE invisibleMover) {
                if (mouseOver instanceof BlockHitResult blockResult) {
//                    screenTileEntity.hitScreenClient(mouseOver.getLocation().x - pos.getX(), mouseOver.getLocation().y - pos.getY(), mouseOver.getLocation().z - pos.getZ(),
//                            blockResult.getDirection(), world.getBlockState(pos).getValue(HORIZ_FACING));
                }
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InvisibleMoverControlBE(pos, state);
    }
}
