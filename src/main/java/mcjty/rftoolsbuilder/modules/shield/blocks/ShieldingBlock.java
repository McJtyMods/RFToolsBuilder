package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.rftoolsbuilder.modules.shield.data.ShieldWorldInfo;
import mcjty.rftoolsbuilder.modules.shield.filters.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class ShieldingBlock extends Block {

    public static final int BLOCKED_ITEMS = 1;             // If set then blocked for items
    public static final int BLOCKED_PASSIVE = 2;           // If set the blocked for passive mobs
    public static final int BLOCKED_HOSTILE = 4;           // If set the blocked for hostile mobs
    public static final int BLOCKED_PLAYERS = 8;           // If set the blocked for (some) players
    public static final int DAMAGE_PASSIVE = 16;           // If set then damage for passive mobs
    public static final int DAMAGE_HOSTILE = 32;           // If set then damage for hostile mobs
    public static final int DAMAGE_PLAYERS = 64;           // If set then damage for (some) players

    public static final int DAMAGE_MASK = (DAMAGE_HOSTILE + DAMAGE_PASSIVE + DAMAGE_PLAYERS);

    public static final VoxelShape COLLISION_SHAPE = VoxelShapes.create(0.002, 0.002, 0.002, 0.998, 0.998, 0.998);

    private final int flags;

    public ShieldingBlock(Properties properties, int flags) {
        super(properties);
        this.flags = flags;
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
        if ((flags & BLOCKED_HOSTILE) != 0) {
            if (entity instanceof IMob) {
                if (checkEntityCD(world, pos, HostileFilter.HOSTILE)) {
                    return COLLISION_SHAPE;
                }
                return VoxelShapes.empty();
            }
        }
        if ((flags & BLOCKED_PASSIVE) != 0) {
            if (entity instanceof AnimalEntity && !(entity instanceof IMob)) {
                if (checkEntityCD(world, pos, AnimalFilter.ANIMAL)) {
                    return COLLISION_SHAPE;
                }
                return VoxelShapes.empty();
            }
        }
        if ((flags & BLOCKED_PLAYERS) != 0) {
            if (entity instanceof PlayerEntity) {
                if (checkPlayerCD(world, pos, (PlayerEntity) entity)) {
                    return COLLISION_SHAPE;
                }
                return VoxelShapes.empty();
            }
        }
        if ((flags & BLOCKED_ITEMS) != 0) {
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
        NoTickShieldBlockTileEntity shieldBlockTileEntity = (NoTickShieldBlockTileEntity) world.getTileEntity(pos);
        BlockPos shieldBlock = shieldBlockTileEntity.getShieldBlock();
        if (shieldBlock != null) {
            ShieldTEBase shieldTileEntity = (ShieldTEBase) world.getTileEntity(shieldBlock);
            if (shieldTileEntity != null) {
                List<ShieldFilter> filters = shieldTileEntity.getFilters();
                for (ShieldFilter filter : filters) {
                    if (DefaultFilter.DEFAULT.equals(filter.getFilterName())) {
                        return (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
                    } else if (filterName.equals(filter.getFilterName())) {
                        return (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
                    }
                }
            }
        }
        return false;
    }


    private boolean checkPlayerCD(IBlockReader world, BlockPos pos, PlayerEntity entity) {
        NoTickShieldBlockTileEntity shieldBlockTileEntity = (NoTickShieldBlockTileEntity) world.getTileEntity(pos);
        BlockPos shieldBlock = shieldBlockTileEntity.getShieldBlock();
        if (shieldBlock != null) {
            ShieldTEBase shieldTileEntity = (ShieldTEBase) world.getTileEntity(shieldBlock);
            if (shieldTileEntity != null) {
                List<ShieldFilter> filters = shieldTileEntity.getFilters();
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
        }
        return false;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            if ((flags & BLOCKED_ITEMS) == 0) {
                // Items should be able to pass through. We just move the entity to below this block.
                entity.setPosition(entity.posX, entity.posY - 1, entity.posZ);
            }
        }
        handleDamage(world, pos, entity);
    }

    public void handleDamage(World world, BlockPos pos, Entity entity) {
        if ((flags & DAMAGE_MASK) == 0 || world.isRemote || world.getGameTime() % 10 != 0) {     // @todo 1.14 was getTotalWorldTime()
            return;
        }

        int xCoord = pos.getX();
        int yCoord = pos.getY();
        int zCoord = pos.getZ();
        AxisAlignedBB beamBox = new AxisAlignedBB(xCoord - .4, yCoord - .4, zCoord - .4, xCoord + 1.4, yCoord + 2.0, zCoord + 1.4);

        if (entity.getBoundingBox().intersects(beamBox)) {
            ShieldTEBase shieldTileEntity = null;
            BlockPos shieldBlock = ShieldWorldInfo.get(world).getShieldProjector(pos);
            if (shieldBlock != null) {
                TileEntity shieldTE = world.getTileEntity(shieldBlock);
                if (shieldTE instanceof ShieldTEBase) {
                    shieldTileEntity = (ShieldTEBase) shieldTE;

                    if ((flags & AbstractShieldBlock.META_HOSTILE) != 0 && entity instanceof IMob) {
                        if (checkEntityDamage(shieldTileEntity, HostileFilter.HOSTILE)) {
                            shieldTileEntity.applyDamageToEntity(entity);
                        }
                    } else if ((flags & AbstractShieldBlock.META_PASSIVE) != 0 && entity instanceof AnimalEntity) {
                        if (checkEntityDamage(shieldTileEntity, AnimalFilter.ANIMAL)) {
                            shieldTileEntity.applyDamageToEntity(entity);
                        }
                    } else if ((flags & AbstractShieldBlock.META_PLAYERS) != 0 && entity instanceof PlayerEntity) {
                        if (checkPlayerDamage(shieldTileEntity, (PlayerEntity) entity)) {
                            shieldTileEntity.applyDamageToEntity(entity);
                        }
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
