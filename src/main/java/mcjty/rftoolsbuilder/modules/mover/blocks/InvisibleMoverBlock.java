package mcjty.rftoolsbuilder.modules.mover.blocks;

import mcjty.lib.varia.SafeClientTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvisibleMoverBlock extends Block implements EntityBlock {

    public static record MoverData(BlockPos mover, BlockPos controlPos, Direction horizDirection, Direction direction) {}

    // Indexed by position of the control
    private final Map<BlockPos, MoverData> dataByControl = new HashMap<>();
    // Same data indexed by the position of the mover
    private final Map<BlockPos, List<MoverData>> dataByMover = new HashMap<>();

    public InvisibleMoverBlock() {
        super(Properties.of(Material.STONE).noLootTable().strength(-1.0F, 3600000.0F).noOcclusion().randomTicks());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InvisibleMoverBE(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof InvisibleMoverBE invisibleMover) {
            BlockState originalState = invisibleMover.getOriginalState();
            if (originalState != null && !originalState.isAir()) {
                return originalState.getShape(level, pos, context);
            }
        }
        return super.getShape(state, level, pos, context);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < .1f) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    public void registerData(BlockPos moverPos, BlockPos controlPos, Direction horizDirection, Direction direction) {
        var data = new MoverData(moverPos, controlPos, horizDirection, direction);
        dataByControl.put(controlPos, data);
        dataByMover.computeIfAbsent(moverPos, p -> new ArrayList<>()).add(data);
    }

    public void removeData(BlockPos moverPos) {
        var set = dataByMover.get(moverPos);
        if (set != null) {
            for (MoverData data : set) {
                dataByControl.remove(data.controlPos);
            }
            dataByMover.put(moverPos, new ArrayList<>());
        }
    }

    public List<MoverData> getData(BlockPos moverPos) {
        return dataByMover.get(moverPos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        activate(level, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void attack(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Player player) {
        activate(level, pos);
    }

    private void activate(Level level, BlockPos pos) {
        if (level.isClientSide) {
            MoverData data = dataByControl.get(pos);
            if (data != null) {
                HitResult mouseOver = SafeClientTools.getClientMouseOver();
                if (mouseOver instanceof BlockHitResult blockResult) {
                    if (level.getBlockEntity(data.mover) instanceof MoverTileEntity mover) {
                        mover.hitScreenClient(blockResult.getBlockPos(), mouseOver.getLocation().x - pos.getX(), mouseOver.getLocation().y - pos.getY(), mouseOver.getLocation().z - pos.getZ(),
                                blockResult.getDirection(),
                                data.horizDirection, data.direction);
                    }
                }
            }
        }
    }
}
