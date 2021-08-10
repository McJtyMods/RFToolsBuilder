package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.rftoolsbuilder.modules.shield.ShieldRenderingMode;
import mcjty.rftoolsbuilder.modules.shield.filters.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
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


    public static final VoxelShape COLLISION_SHAPE = VoxelShapes.box(0.002, 0.002, 0.002, 0.998, 0.998, 0.998);

    public ShieldingBlock() {
        super(AbstractBlock.Properties.of(Material.GLASS)
                .noOcclusion()
                .isRedstoneConductor((state, world, pos) -> false)
                .strength(-1.0F, 3600000.0F)
                .noDrops());
        registerDefaultState(defaultBlockState()
                .setValue(BLOCKED_ITEMS, false)
                .setValue(BLOCKED_PASSIVE, false)
                .setValue(BLOCKED_HOSTILE, false)
                .setValue(BLOCKED_PLAYERS, false)
                .setValue(DAMAGE_ITEMS, false)
                .setValue(DAMAGE_PASSIVE, false)
                .setValue(DAMAGE_HOSTILE, false)
                .setValue(DAMAGE_PLAYERS, false)
                .setValue(FLAG_OPAQUE, true)
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
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BLOCKED_ITEMS, BLOCKED_HOSTILE, BLOCKED_PASSIVE, BLOCKED_PLAYERS,
                DAMAGE_ITEMS, DAMAGE_HOSTILE, DAMAGE_PASSIVE, DAMAGE_PLAYERS,
                FLAG_OPAQUE, RENDER_MODE);
    }

    @Override
    public int getLightBlock(BlockState state, IBlockReader world, BlockPos pos) {
        return state.getValue(FLAG_OPAQUE) ? 0 : 255;
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return state.getValue(RENDER_MODE) == ShieldRenderingMode.INVISIBLE ? BlockRenderType.INVISIBLE : BlockRenderType.MODEL;
    }

    @Override
    public float getShadeBrightness(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return super.getShadeBrightness(state, worldIn, pos);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, IBlockReader world, BlockPos pos, Entity entity) {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof ShieldingTileEntity) {
            BlockState mimic = ((ShieldingTileEntity) te).getMimic();
            if (mimic != null) {
                return mimic.getShape(world, pos, context);
            }
        }
        if (state.getValue(RENDER_MODE) == ShieldRenderingMode.INVISIBLE) {
            return VoxelShapes.empty();
        } else {
            return super.getShape(state, world, pos, context);
        }
    }


    @Override
    public VoxelShape getInteractionShape(BlockState state, IBlockReader world, BlockPos pos) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof ShieldingTileEntity) {
            BlockState mimic = ((ShieldingTileEntity) te).getMimic();
            if (mimic != null) {
                return mimic.getVisualShape(world, pos, ISelectionContext.empty());   // @todo 1.16 is dummy() ok?
            }
        }
        if (state.getValue(RENDER_MODE) == ShieldRenderingMode.INVISIBLE) {
            return VoxelShapes.empty();
        } else {
            return super.getInteractionShape(state, world, pos);
        }
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader world, BlockPos pos) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof ShieldingTileEntity) {
            BlockState mimic = ((ShieldingTileEntity) te).getMimic();
            if (mimic != null) {
                return mimic.getOcclusionShape(world, pos);
            }
        }
        return super.getOcclusionShape(state, world, pos);
    }

    public static boolean isHostile(Entity entity) {
        return entity instanceof IMob;
    }

    public static boolean isPassive(Entity entity) {
        if (entity instanceof IMob) {
            return false;
        }
        if (entity instanceof PlayerEntity) {
            return false;
        }
        return entity instanceof MobEntity;
    }

    public static boolean isItem(Entity entity) {
//        return entity instanceof ItemEntity;
        return !(entity instanceof LivingEntity);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        Entity entity = context.getEntity();
        if (state.getValue(BLOCKED_HOSTILE)) {
            if (isHostile(entity)) {
                if (checkEntityCD(world, pos, HostileFilter.HOSTILE)) {
                    return COLLISION_SHAPE;
                }
                return VoxelShapes.empty();
            }
        }
        if (state.getValue(BLOCKED_PASSIVE)) {
            if (isPassive(entity)) {
                if (checkEntityCD(world, pos, AnimalFilter.ANIMAL)) {
                    return COLLISION_SHAPE;
                }
                return VoxelShapes.empty();
            }
        }
        if (state.getValue(BLOCKED_PLAYERS)) {
            if (entity instanceof PlayerEntity) {
                if (checkPlayerCD(world, pos, (PlayerEntity) entity)) {
                    return COLLISION_SHAPE;
                }
                return VoxelShapes.empty();
            }
        }
        if (state.getValue(BLOCKED_ITEMS)) {
            if (isItem(entity)) {
                if (checkEntityCD(world, pos, ItemFilter.ITEM)) {
                    return COLLISION_SHAPE;
                }
                return VoxelShapes.empty();
            }
        }
        return VoxelShapes.empty();
    }

    private boolean checkEntityCD(IBlockReader world, BlockPos pos, String filterName) {
        ShieldProjectorTileEntity projector = getShieldProjector(world, pos);
        if (projector != null) {
            List<ShieldFilter> filters = projector.getFilters();
            for (ShieldFilter filter : filters) {
                if (DefaultFilter.DEFAULT.equals(filter.getFilterName())) {
                    return (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
                } else if (filterName.equals(filter.getFilterName())) {
                    return (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
                }
            }
        }
        return false;
    }


    private boolean checkPlayerCD(IBlockReader world, BlockPos pos, PlayerEntity entity) {
        ShieldProjectorTileEntity projector = getShieldProjector(world, pos);
        if (projector != null) {
            List<ShieldFilter> filters = projector.getFilters();
            for (ShieldFilter filter : filters) {
                if (DefaultFilter.DEFAULT.equals(filter.getFilterName())) {
                    return (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
                } else if (PlayerFilter.PLAYER.equals(filter.getFilterName())) {
                    PlayerFilter playerFilter = (PlayerFilter) filter;
                    String name = playerFilter.getName();
                    if ((name == null || name.isEmpty())) {
                        return (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
                    } else if (name.equals(entity.getName().getString())) { // @todo getFormattedText
                        return (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        return adjacentBlockState.getBlock() == this ? true : super.skipRendering(state, adjacentBlockState, side);
    }

    @Override
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            if (!state.getValue(BLOCKED_ITEMS)) {
                // Items should be able to pass through. We just move the entity to below this block.
                entity.setPos(entity.getX(), entity.getY() - 1, entity.getZ());
            }
        }
        handleDamage(state, world, pos, entity);
    }

    @Nullable
    private ShieldProjectorTileEntity getShieldProjector(IBlockReader world, BlockPos shieldingPos) {
        TileEntity te = world.getBlockEntity(shieldingPos);
        if (te instanceof ShieldingTileEntity) {
            BlockPos projectorPos = ((ShieldingTileEntity) te).getShieldProjector();
            if (projectorPos != null) {
                TileEntity tileEntity = world.getBlockEntity(projectorPos);
                if (tileEntity instanceof ShieldProjectorTileEntity) {
                    return (ShieldProjectorTileEntity) tileEntity;
                }
            }
        }
        return null;
    }

    public void handleDamage(BlockState state, World world, BlockPos pos, Entity entity) {
        Boolean dmgHostile = state.getValue(DAMAGE_HOSTILE);
        Boolean dmgPassive = state.getValue(DAMAGE_PASSIVE);
        Boolean dmgPlayer = state.getValue(DAMAGE_PLAYERS);
        Boolean dmgItems = state.getValue(DAMAGE_ITEMS);

        if ((!dmgHostile && !dmgPassive && !dmgPlayer && !dmgItems) || world.isClientSide || world.getGameTime() % 10 != 0) {     // @todo 1.14 was getTotalWorldTime()
            return;
        }

        int xCoord = pos.getX();
        int yCoord = pos.getY();
        int zCoord = pos.getZ();
        AxisAlignedBB beamBox = new AxisAlignedBB(xCoord - .4, yCoord - .4, zCoord - .4, xCoord + 1.4, yCoord + 2.0, zCoord + 1.4);

        if (entity.getBoundingBox().intersects(beamBox)) {
            ShieldProjectorTileEntity projector = getShieldProjector(world, pos);
            if (projector != null) {
                if (dmgItems && entity instanceof ItemEntity) {
                    if (checkEntityDamage(projector, ItemFilter.ITEM)) {
                        projector.applyDamageToEntity(entity);
                    }
                } else if (dmgHostile && isHostile(entity)) {
                    if (checkEntityDamage(projector, HostileFilter.HOSTILE)) {
                        projector.applyDamageToEntity(entity);
                    }
                } else if (dmgPassive && isPassive(entity)) {
                    if (checkEntityDamage(projector, AnimalFilter.ANIMAL)) {
                        projector.applyDamageToEntity(entity);
                    }
                } else if (dmgPlayer && entity instanceof PlayerEntity) {
                    if (checkPlayerDamage(projector, (PlayerEntity) entity)) {
                        projector.applyDamageToEntity(entity);
                    }
                }
            }
        }
    }

    private boolean checkEntityDamage(@Nonnull ShieldProjectorTileEntity shieldTileEntity, String filterName) {
        List<ShieldFilter> filters = shieldTileEntity.getFilters();
        for (ShieldFilter filter : filters) {
            if (DefaultFilter.DEFAULT.equals(filter.getFilterName())) {
                return ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0);
            } else if (filterName.equals(filter.getFilterName())) {
                return ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0);
            }
        }
        return false;
    }

    private boolean checkPlayerDamage(@Nonnull ShieldProjectorTileEntity shieldTileEntity, PlayerEntity entity) {
        List<ShieldFilter> filters = shieldTileEntity.getFilters();
        for (ShieldFilter filter : filters) {
            if (DefaultFilter.DEFAULT.equals(filter.getFilterName())) {
                return ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0);
            } else if (PlayerFilter.PLAYER.equals(filter.getFilterName())) {
                PlayerFilter playerFilter = (PlayerFilter) filter;
                String name = playerFilter.getName();
                if ((name == null || name.isEmpty())) {
                    return ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0);
                } else if (name.equals(entity.getName().getString())) { // @todo getFormattedText
                    return ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0);
                }
            }
        }
        return false;
    }

}
