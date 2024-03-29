package mcjty.rftoolsbuilder.modules.builder;

import mcjty.lib.varia.Counter;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.blocks.SupportBlock;
import mcjty.rftoolsbuilder.modules.builder.network.PacketChamberInfoReady;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuilderTools {

    public static void returnChamberInfo(PlayerEntity player) {
        ItemStack cardItem = player.getItemInHand(Hand.MAIN_HAND);
        if (cardItem.isEmpty() || cardItem.getTag() == null) {
            return;
        }

        int channel = cardItem.getTag().getInt("channel");
        if (channel == -1) {
            return;
        }

        SpaceChamberRepository repository = SpaceChamberRepository.get(player.getCommandSenderWorld());
        SpaceChamberRepository.SpaceChamberChannel chamberChannel = repository.getChannel(channel);
        if (chamberChannel == null) {
            return;
        }

        World world = LevelTools.getLevel(player.getCommandSenderWorld(), chamberChannel.getDimension());
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

        RFToolsBuilderMessages.INSTANCE.sendTo(new PacketChamberInfoReady(blocks, costs, stacks,
                entitiesWithCount, entitiesWithCost, firstEntity), ((ServerPlayerEntity) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    private static void findEntities(World world, BlockPos minCorner, BlockPos maxCorner,
                                 Counter<String> entitiesWithCount, Counter<String> entitiesWithCost, Map<String, Entity> firstEntity) {
        List<Entity> entities = world.getEntities(null, new AxisAlignedBB(
                minCorner.getX(), minCorner.getY(), minCorner.getZ(), maxCorner.getX() + 1, maxCorner.getY() + 1, maxCorner.getZ() + 1));
        for (Entity entity : entities) {
            String canonicalName = entity.getClass().getCanonicalName();
            if (entity instanceof ItemEntity) {
                ItemEntity entityItem = (ItemEntity) entity;
                if (!entityItem.getItem().isEmpty()) {
                    String displayName = entityItem.getItem().getHoverName().getString() /* was getFormattedText() */;
                    canonicalName += " (" + displayName + ")";
                }
            }

            entitiesWithCount.increment(canonicalName);

            if (!firstEntity.containsKey(canonicalName)) {
                firstEntity.put(canonicalName, entity);
            }

            if (entity instanceof PlayerEntity) {
                entitiesWithCost.increment(canonicalName, BuilderConfiguration.builderRfPerPlayer.get());
            } else {
                entitiesWithCost.increment(canonicalName, BuilderConfiguration.builderRfPerEntity.get());
            }
        }
    }

    private static void findBlocks(PlayerEntity harvester, World world, Counter<BlockState> blocks, Counter<BlockState> costs, Map<BlockState, ItemStack> stacks, BlockPos minCorner, BlockPos maxCorner) {
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

                        TileEntity te = world.getBlockEntity(p);
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
