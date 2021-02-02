package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.rftoolsbuilder.modules.shield.ShieldRenderingMode;
import mcjty.rftoolsbuilder.modules.shield.filters.ShieldFilter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ShieldingBlock extends Block {

    public static final BooleanProperty BLOCKED_ITEMS = BooleanProperty.create("bi");               // If set then blocked for items
    public static final BooleanProperty BLOCKED_PASSIVE = BooleanProperty.create("bp");             // If set the blocked for passive mobs
    public static final BooleanProperty BLOCKED_HOSTILE = BooleanProperty.create("bh");             // If set the blocked for hostile mobs
    public static final BooleanProperty BLOCKED_PLAYERS = BooleanProperty.create("bplay");          // If set the blocked for (some) players
    public static final BooleanProperty DAMAGE_ITEMS = BooleanProperty.create("di");                // If set then damage for items
    public static final BooleanProperty DAMAGE_PASSIVE = BooleanProperty.create("dp");              // If set then damage for passive mobs
    public static final BooleanProperty DAMAGE_HOSTILE = BooleanProperty.create("dh");              // If set then damage for hostile mobs
    public static final BooleanProperty DAMAGE_PLAYERS = BooleanProperty.create("dplay");           // If set then damage for (some) players
    public static final BooleanProperty FLAG_OPAQUE = BooleanProperty.create("opaque");             // If the block is opaque or not
    public static final EnumProperty<ShieldRenderingMode> RENDER_MODE = EnumProperty.create("render", ShieldRenderingMode.class);


    public static final VoxelShape COLLISION_SHAPE = VoxelShapes.create(0.002, 0.002, 0.002, 0.998, 0.998, 0.998);

    public ShieldingBlock() {
        super(Block.Properties.create(Material.GLASS)
                .notSolid()
                .setOpaque((state, world, pos) -> false)
                .hardnessAndResistance(-1.0F, 3600000.0F)
                .noDrops());
        setDefaultState(getDefaultState()
                .with(BLOCKED_ITEMS, false)
                .with(BLOCKED_PASSIVE, false)
                .with(BLOCKED_HOSTILE, false)
                .with(BLOCKED_PLAYERS, false)
                .with(DAMAGE_ITEMS, false)
                .with(DAMAGE_PASSIVE, false)
                .with(DAMAGE_HOSTILE, false)
                .with(DAMAGE_PLAYERS, false)
                .with(FLAG_OPAQUE, true)
        );
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ShieldingTileEntity();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(BLOCKED_ITEMS, BLOCKED_HOSTILE, BLOCKED_PASSIVE, BLOCKED_PLAYERS,
                DAMAGE_ITEMS, DAMAGE_HOSTILE, DAMAGE_PASSIVE, DAMAGE_PLAYERS,
                FLAG_OPAQUE, RENDER_MODE);
    }

    @Override
    public int getOpacity(BlockState state, IBlockReader world, BlockPos pos) {
        return state.get(FLAG_OPAQUE) ? 0 : 255;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return state.get(RENDER_MODE) == ShieldRenderingMode.INVISIBLE ? BlockRenderType.INVISIBLE : BlockRenderType.MODEL;
    }

    @Override
    public float getAmbientOcclusionLightValue(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return super.getAmbientOcclusionLightValue(state, worldIn, pos);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, IBlockReader world, BlockPos pos, Entity entity) {
        return false;
    }

    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ShieldingTileEntity) {
            BlockState mimic = ((ShieldingTileEntity) te).getMimic();
            if (mimic != null) {
                return mimic.getShape(world, pos, context);
            }
        }
        if (state.get(RENDER_MODE) == ShieldRenderingMode.INVISIBLE) {
            return VoxelShapes.empty();
        } else {
            return super.getShape(state, world, pos, context);
        }
    }


    @Override
    public VoxelShape getRaytraceShape(BlockState state, IBlockReader world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ShieldingTileEntity) {
            BlockState mimic = ((ShieldingTileEntity) te).getMimic();
            if (mimic != null) {
                return mimic.getRaytraceShape(world, pos, ISelectionContext.dummy());   // @todo 1.16 is dummy() ok?
            }
        }
        if (state.get(RENDER_MODE) == ShieldRenderingMode.INVISIBLE) {
            return VoxelShapes.empty();
        } else {
            return super.getRaytraceShape(state, world, pos);
        }
    }

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ShieldingTileEntity) {
            BlockState mimic = ((ShieldingTileEntity) te).getMimic();
            if (mimic != null) {
                return mimic.getRenderShape(world, pos);
            }
        }
        return super.getRenderShape(state, world, pos);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        Boolean blkHostile = state.get(BLOCKED_HOSTILE);
        Boolean blkPassive = state.get(BLOCKED_PASSIVE);
        Boolean blkPlayer = state.get(BLOCKED_PLAYERS);
        Boolean blkItems = state.get(BLOCKED_ITEMS);

        if (!blkHostile && !blkPassive && !blkPlayer && !blkItems) {
            return VoxelShapes.empty();
        }

        ShieldProjectorTileEntity projector = getShieldProjector(world, pos);
        if (projector != null && checkEntityAction(projector, context.getEntity(), ShieldFilter.ACTION_SOLID)) {
            return COLLISION_SHAPE;
        }
        return VoxelShapes.empty();
    }

    private boolean checkEntityAction(@Nonnull ShieldProjectorTileEntity projector, Entity entity, int action) {
        List<ShieldFilter> filters = projector.getFilters();
        for (ShieldFilter filter : filters) {
            if (filter.match(entity)) {
                return (filter.getAction() & action) != 0;
            }
        }
        return false;
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
        return adjacentBlockState.getBlock() == this ? true : super.isSideInvisible(state, adjacentBlockState, side);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        handleDamage(state, world, pos, entity);
    }

    @Nullable
    private ShieldProjectorTileEntity getShieldProjector(IBlockReader world, BlockPos shieldingPos) {
        TileEntity te = world.getTileEntity(shieldingPos);
        if (te instanceof ShieldingTileEntity) {
            BlockPos projectorPos = ((ShieldingTileEntity) te).getShieldProjector();
            if (projectorPos != null) {
                TileEntity tileEntity = world.getTileEntity(projectorPos);
                if (tileEntity instanceof ShieldProjectorTileEntity) {
                    return (ShieldProjectorTileEntity) tileEntity;
                }
            }
        }
        return null;
    }

    public void handleDamage(BlockState state, World world, BlockPos pos, Entity entity) {
        Boolean dmgHostile = state.get(DAMAGE_HOSTILE);
        Boolean dmgPassive = state.get(DAMAGE_PASSIVE);
        Boolean dmgPlayer = state.get(DAMAGE_PLAYERS);
        Boolean dmgItems = state.get(DAMAGE_ITEMS);

        if ((!dmgHostile && !dmgPassive && !dmgPlayer && !dmgItems) || world.isRemote || world.getGameTime() % 10 != 0) {     // @todo 1.14 was getTotalWorldTime()
            return;
        }

        int xCoord = pos.getX();
        int yCoord = pos.getY();
        int zCoord = pos.getZ();
        AxisAlignedBB beamBox = new AxisAlignedBB(xCoord - .4, yCoord - .4, zCoord - .4, xCoord + 1.4, yCoord + 2.0, zCoord + 1.4);

        if (entity.getBoundingBox().intersects(beamBox)) {
            ShieldProjectorTileEntity projector = getShieldProjector(world, pos);
            if (projector != null && checkEntityAction(projector, entity, ShieldFilter.ACTION_DAMAGE)) {
                projector.applyDamageToEntity(entity);
            }
        }
    }
}
