package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.rftoolsbuilder.modules.shield.ShieldRenderingMode;
import mcjty.rftoolsbuilder.modules.shield.filters.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ShieldingBlock extends Block implements EntityBlock {

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


    public static final VoxelShape COLLISION_SHAPE = Shapes.box(0.002, 0.002, 0.002, 0.998, 0.998, 0.998);

    public ShieldingBlock() {
        super(BlockBehaviour.Properties.of()
                .forceSolidOn()
                .sound(SoundType.GLASS)
                .noOcclusion()
                .isRedstoneConductor((state, world, pos) -> false)
                .pushReaction(PushReaction.BLOCK)
                .strength(-1.0F, 3600000.0F)
                .noLootTable());
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ShieldingTileEntity(pPos, pState);
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BLOCKED_ITEMS, BLOCKED_HOSTILE, BLOCKED_PASSIVE, BLOCKED_PLAYERS,
                DAMAGE_ITEMS, DAMAGE_HOSTILE, DAMAGE_PASSIVE, DAMAGE_PLAYERS,
                FLAG_OPAQUE, RENDER_MODE);
    }

    @Override
    public int getLightBlock(BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos) {
        return state.getValue(FLAG_OPAQUE) ? 0 : 255;
    }

    @Nonnull
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(RENDER_MODE) == ShieldRenderingMode.INVISIBLE ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    public float getShadeBrightness(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos) {
        return super.getShadeBrightness(state, worldIn, pos);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter world, BlockPos pos, Entity entity) {
        return false;
    }

    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull BlockState state, BlockGetter world, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ShieldingTileEntity) {
            BlockState mimic = ((ShieldingTileEntity) te).getMimic();
            if (mimic != null) {
                return mimic.getShape(world, pos, context);
            }
        }
        if (state.getValue(RENDER_MODE) == ShieldRenderingMode.INVISIBLE) {
            return Shapes.empty();
        } else {
            return super.getShape(state, world, pos, context);
        }
    }


    @Nonnull
    @Override
    public VoxelShape getInteractionShape(@Nonnull BlockState state, BlockGetter world, @Nonnull BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ShieldingTileEntity) {
            BlockState mimic = ((ShieldingTileEntity) te).getMimic();
            if (mimic != null) {
                return mimic.getVisualShape(world, pos, CollisionContext.empty());   // @todo 1.16 is dummy() ok?
            }
        }
        if (state.getValue(RENDER_MODE) == ShieldRenderingMode.INVISIBLE) {
            return Shapes.empty();
        } else {
            return super.getInteractionShape(state, world, pos);
        }
    }

    @Nonnull
    @Override
    public VoxelShape getOcclusionShape(@Nonnull BlockState state, BlockGetter world, @Nonnull BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ShieldingTileEntity) {
            BlockState mimic = ((ShieldingTileEntity) te).getMimic();
            if (mimic != null) {
                return mimic.getOcclusionShape(world, pos);
            }
        }
        return super.getOcclusionShape(state, world, pos);
    }

    public static boolean isHostile(Entity entity) {
        return entity instanceof Enemy;
    }

    public static boolean isPassive(Entity entity) {
        if (entity instanceof Enemy) {
            return false;
        }
        if (entity instanceof Player) {
            return false;
        }
        return entity instanceof Mob;
    }

    public static boolean isItem(Entity entity) {
//        return entity instanceof ItemEntity;
        return !(entity instanceof LivingEntity);
    }

    @Nonnull
    @Override
    public VoxelShape getCollisionShape(BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext ctxt) {
            Entity entity = ctxt.getEntity();
            if (state.getValue(BLOCKED_HOSTILE)) {
                if (isHostile(entity)) {
                    if (checkEntityCD(world, pos, HostileFilter.HOSTILE)) {
                        return COLLISION_SHAPE;
                    }
                    return Shapes.empty();
                }
            }
            if (state.getValue(BLOCKED_PASSIVE)) {
                if (isPassive(entity)) {
                    if (checkEntityCD(world, pos, AnimalFilter.ANIMAL)) {
                        return COLLISION_SHAPE;
                    }
                    return Shapes.empty();
                }
            }
            if (state.getValue(BLOCKED_PLAYERS)) {
                if (entity instanceof Player) {
                    if (checkPlayerCD(world, pos, (Player) entity)) {
                        return COLLISION_SHAPE;
                    }
                    return Shapes.empty();
                }
            }
            if (state.getValue(BLOCKED_ITEMS)) {
                if (isItem(entity)) {
                    if (checkEntityCD(world, pos, ItemFilter.ITEM)) {
                        return COLLISION_SHAPE;
                    }
                    return Shapes.empty();
                }
            }
        }
        return Shapes.empty();
    }

    private boolean checkEntityCD(BlockGetter world, BlockPos pos, String filterName) {
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


    private boolean checkPlayerCD(BlockGetter world, BlockPos pos, Player entity) {
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
    public boolean skipRendering(@Nonnull BlockState state, BlockState adjacentBlockState, @Nonnull Direction side) {
        return adjacentBlockState.getBlock() == this ? true : super.skipRendering(state, adjacentBlockState, side);
    }

    @Override
    public void entityInside(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            if (!state.getValue(BLOCKED_ITEMS)) {
                // Items should be able to pass through. We just move the entity to below this block.
                entity.setPos(entity.getX(), entity.getY() - 1, entity.getZ());
            }
        }
        handleDamage(state, world, pos, entity);
    }

    @Nullable
    private ShieldProjectorTileEntity getShieldProjector(BlockGetter world, BlockPos shieldingPos) {
        BlockEntity te = world.getBlockEntity(shieldingPos);
        if (te instanceof ShieldingTileEntity) {
            BlockPos projectorPos = ((ShieldingTileEntity) te).getShieldProjector();
            if (projectorPos != null) {
                BlockEntity tileEntity = world.getBlockEntity(projectorPos);
                if (tileEntity instanceof ShieldProjectorTileEntity) {
                    return (ShieldProjectorTileEntity) tileEntity;
                }
            }
        }
        return null;
    }

    public void handleDamage(BlockState state, Level world, BlockPos pos, Entity entity) {
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
        AABB beamBox = new AABB(xCoord - .4, yCoord - .4, zCoord - .4, xCoord + 1.4, yCoord + 2.0, zCoord + 1.4);

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
                } else if (dmgPlayer && entity instanceof Player) {
                    if (checkPlayerDamage(projector, (Player) entity)) {
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

    private boolean checkPlayerDamage(@Nonnull ShieldProjectorTileEntity shieldTileEntity, Player entity) {
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
