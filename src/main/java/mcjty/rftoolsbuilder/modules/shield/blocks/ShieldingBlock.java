package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.rftoolsbuilder.modules.shield.ShieldRenderingMode;
import mcjty.rftoolsbuilder.modules.shield.filters.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
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
                .hardnessAndResistance(-1.0F, 3600000.0F)
                .noDrops());
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
    public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return false;
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
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        Entity entity = context.getEntity();
        if (state.get(BLOCKED_HOSTILE)) {
            if (entity instanceof IMob) {
                if (checkEntityCD(world, pos, HostileFilter.HOSTILE)) {
                    return COLLISION_SHAPE;
                }
                return VoxelShapes.empty();
            }
        }
        if (state.get(BLOCKED_PASSIVE)) {
            if (entity instanceof AnimalEntity && !(entity instanceof IMob)) {
                if (checkEntityCD(world, pos, AnimalFilter.ANIMAL)) {
                    return COLLISION_SHAPE;
                }
                return VoxelShapes.empty();
            }
        }
        if (state.get(BLOCKED_PLAYERS)) {
            if (entity instanceof PlayerEntity) {
                if (checkPlayerCD(world, pos, (PlayerEntity) entity)) {
                    return COLLISION_SHAPE;
                }
                return VoxelShapes.empty();
            }
        }
        if (state.get(BLOCKED_ITEMS)) {
            if (!(entity instanceof LivingEntity)) {
                if (checkEntityCD(world, pos, ItemFilter.ITEM)) {
                    return COLLISION_SHAPE;
                }
                return VoxelShapes.empty();
            }
        }
        return VoxelShapes.empty();
    }

    private boolean checkEntityCD(IBlockReader world, BlockPos pos, String filterName) {
        ShieldTEBase projector = getShieldProjector(world, pos);
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
        ShieldTEBase projector = getShieldProjector(world, pos);
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
                    } else if (name.equals(entity.getName())) {
                        return (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            if (!state.get(BLOCKED_ITEMS)) {
                // Items should be able to pass through. We just move the entity to below this block.
                entity.setPosition(entity.posX, entity.posY - 1, entity.posZ);
            }
        }
        handleDamage(state, world, pos, entity);
    }

    @Nullable
    private ShieldTEBase getShieldProjector(IBlockReader world, BlockPos shieldingPos) {
        TileEntity te = world.getTileEntity(shieldingPos);
        if (te instanceof ShieldingTileEntity) {
            BlockPos projectorPos = ((ShieldingTileEntity) te).getShieldProjector();
            TileEntity tileEntity = world.getTileEntity(projectorPos);
            if (tileEntity instanceof ShieldTEBase) {
                return (ShieldTEBase) tileEntity;
            }
        }
        return null;

//        // Needs to work both client and server side so use getI()
//        BlockPos shieldBlock = ShieldWorldInfo.getI((World) world).getShieldProjector(shieldingPos);
//        if (shieldBlock != null) {
//            TileEntity shieldTE = world.getTileEntity(shieldBlock);
//            if (shieldTE instanceof ShieldTEBase) {
//                return (ShieldTEBase) shieldTE;
//            }
//        }
//        return null;
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
            ShieldTEBase projector = getShieldProjector(world, pos);
            if (projector != null) {
                if (dmgItems && entity instanceof ItemEntity) {
                    if (checkEntityDamage(projector, ItemFilter.ITEM)) {
                        projector.applyDamageToEntity(entity);
                    }
                } else if (dmgHostile && entity instanceof IMob) {
                    if (checkEntityDamage(projector, HostileFilter.HOSTILE)) {
                        projector.applyDamageToEntity(entity);
                    }
                } else if (dmgPassive && entity instanceof AnimalEntity) {
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

    private boolean checkEntityDamage(@Nonnull ShieldTEBase shieldTileEntity, String filterName) {
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

    private boolean checkPlayerDamage(@Nonnull ShieldTEBase shieldTileEntity, PlayerEntity entity) {
        List<ShieldFilter> filters = shieldTileEntity.getFilters();
        for (ShieldFilter filter : filters) {
            if (DefaultFilter.DEFAULT.equals(filter.getFilterName())) {
                return ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0);
            } else if (PlayerFilter.PLAYER.equals(filter.getFilterName())) {
                PlayerFilter playerFilter = (PlayerFilter) filter;
                String name = playerFilter.getName();
                if ((name == null || name.isEmpty())) {
                    return ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0);
                } else if (name.equals(entity.getName())) {
                    return ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0);
                }
            }
        }
        return false;
    }

}
