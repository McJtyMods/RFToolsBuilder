package mcjty.rftoolsbuilder.modules.builder;

import mcjty.lib.varia.Counter;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.blocks.SupportBlock;
import mcjty.rftoolsbuilder.modules.builder.network.PacketChamberInfoReady;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuilderTools {

    public static void returnChamberInfo(Player player) {
        SpaceChamberRepository.SpaceChamberChannel chamberChannel = getSpaceChamberChannel(player);
        if (chamberChannel == null) return;

        Level world = LevelTools.getLevel(player.getCommandSenderWorld(), chamberChannel.getDimension());
        if (world == null) {
            return;
        }

        Counter<BlockState> blocks = new Counter<>();
        Counter<BlockState> costs = new Counter<>();
        Map<BlockState,ItemStack> stacks = new HashMap<>();

        BlockPos minCorner = chamberChannel.getMinCorner();
        BlockPos maxCorner = chamberChannel.getMaxCorner();
        findBlocks(player, world, blocks, costs, stacks, minCorner, maxCorner);

        Counter<String> entitiesWithCount = new Counter<>();
        Counter<String> entitiesWithCost = new Counter<>();
        Map<String,Entity> firstEntity = new HashMap<>();
        findEntities(world, minCorner, maxCorner, entitiesWithCount, entitiesWithCost, firstEntity);

        RFToolsBuilderMessages.sendToPlayer(PacketChamberInfoReady.create(blocks, costs, stacks,
                entitiesWithCount, entitiesWithCost, firstEntity), player);
    }

    @Nullable
    public static SpaceChamberRepository.SpaceChamberChannel getSpaceChamberChannel(Player player) {
        ItemStack cardItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        return getSpaceChamberChannel(player.getCommandSenderWorld(), cardItem);
    }

    @Nullable
    public static SpaceChamberRepository.SpaceChamberChannel getSpaceChamberChannel(Level level, ItemStack cardItem) {
        Integer channel = getChannel(cardItem);
        if (channel == null) {
            return null;
        }

        SpaceChamberRepository repository = SpaceChamberRepository.get(level);
        return repository.getChannel(channel);
    }

    @Nullable
    public static Integer getChannel(ItemStack cardItem) {
        if (cardItem.isEmpty() || cardItem.getTag() == null) {
            return null;
        }

        int channel = cardItem.getTag().getInt("channel");
        if (channel == -1) {
            return null;
        }
        return channel;
    }

    private static void findEntities(Level world, BlockPos minCorner, BlockPos maxCorner,
                                 Counter<String> entitiesWithCount, Counter<String> entitiesWithCost, Map<String, Entity> firstEntity) {
        List<Entity> entities = world.getEntities(null, new AABB(
                minCorner.getX(), minCorner.getY(), minCorner.getZ(), maxCorner.getX() + 1, maxCorner.getY() + 1, maxCorner.getZ() + 1));
        for (Entity entity : entities) {
            String canonicalName = entity.getClass().getCanonicalName();
            if (entity instanceof ItemEntity entityItem) {
                if (!entityItem.getItem().isEmpty()) {
                    String displayName = entityItem.getItem().getHoverName().getString() /* was getFormattedText() */;
                    canonicalName += " (" + displayName + ")";
                }
            }

            entitiesWithCount.increment(canonicalName);

            if (!firstEntity.containsKey(canonicalName)) {
                firstEntity.put(canonicalName, entity);
            }

            if (entity instanceof Player) {
                entitiesWithCost.increment(canonicalName, BuilderConfiguration.builderRfPerPlayer.get());
            } else {
                entitiesWithCost.increment(canonicalName, BuilderConfiguration.builderRfPerEntity.get());
            }
        }
    }

    private static void findBlocks(Player harvester, Level world, Counter<BlockState> blocks, Counter<BlockState> costs, Map<BlockState, ItemStack> stacks, BlockPos minCorner, BlockPos maxCorner) {
        for (int x = minCorner.getX() ; x <= maxCorner.getX() ; x++) {
            for (int y = minCorner.getY() ; y <= maxCorner.getY() ; y++) {
                for (int z = minCorner.getZ() ; z <= maxCorner.getZ() ; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(p);
                    Block block = state.getBlock();
                    if (!BuilderTileEntity.isEmpty(state, block)) {
                        blocks.increment(state);

                        if (!stacks.containsKey(state)) {
                            ItemStack item = block.getCloneItemStack(world, p, state);
                            if (!item.isEmpty()) {
                                stacks.put(state, item);
                            }
                        }

                        BlockEntity te = world.getBlockEntity(p);
                        BlockInformation info = BuilderTileEntity.getBlockInformation(harvester, world, p, block, te);
                        if (info.getBlockLevel() == SupportBlock.SupportStatus.STATUS_ERROR) {
                            costs.put(state, -1);
                        } else {
                            costs.increment(state, (int) (BuilderConfiguration.builderRfPerOperation.get() * info.getCostFactor()));
                        }
                    }
                }
            }
        }
    }

}
