package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.client.GuiChamberDetails;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public record PacketChamberInfoReady(Map<BlockState, Integer> blocks, Map<BlockState, Integer> costs,
                                     Map<BlockState, ItemStack> stacks,
                                     Map<String, Integer> entities, Map<String, Integer> entityCosts,
                                     Map<String, Entity> realEntities,
                                     Map<String, String> playerNames) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsBuilder.MODID, "chamberinfoready");

    private static final byte ENTITY_NONE = 0;
    private static final byte ENTITY_NORMAL = 1;
    private static final byte ENTITY_PLAYER = 2;

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(blocks.size());
        for (Map.Entry<BlockState, Integer> entry : blocks.entrySet()) {
            BlockState bm = entry.getKey();
            buf.writeInt(Block.getId(bm));
            buf.writeInt(entry.getValue());
            buf.writeInt(costs.get(bm));
            if (stacks.containsKey(bm)) {
                buf.writeBoolean(true);
                NetworkTools.writeItemStack(buf, stacks.get(bm));
            } else {
                buf.writeBoolean(false);
            }
        }
        buf.writeInt(entities.size());
        for (Map.Entry<String, Integer> entry : entities.entrySet()) {
            String name = entry.getKey();
            buf.writeUtf(name);
            buf.writeInt(entry.getValue());
            buf.writeInt(entityCosts.get(name));
            if (realEntities.containsKey(name)) {
                Entity entity = realEntities.get(name);
                if (entity instanceof Player) {
                    buf.writeByte(ENTITY_PLAYER);
                    int entityId = entity.getId();
                    buf.writeInt(entityId);
                    buf.writeUtf(entity.getDisplayName().getString());   // @todo getFormattedText
                } else {
                    buf.writeByte(ENTITY_NORMAL);
                    CompoundTag nbt = entity.serializeNBT();
                    writeNBT(buf, nbt);
                }
            } else {
                buf.writeByte(ENTITY_NONE);
            }
        }
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    private static void writeNBT(FriendlyByteBuf dataOut, CompoundTag nbt) {
        FriendlyByteBuf buf = new FriendlyByteBuf(dataOut);
        try {
            buf.writeNbt(nbt);
        } catch (Exception e) {
            Logging.logError("Error writing packet chamber info", e);
        }
    }


    public static PacketChamberInfoReady create(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<BlockState, Integer> blocks = new HashMap<>(size);
        Map<BlockState, Integer> costs = new HashMap<>(size);
        Map<BlockState, ItemStack> stacks = new HashMap<>();
        for (int i = 0; i < size; i++) {
            BlockState bm = Block.stateById(buf.readInt());
            int count = buf.readInt();
            int cost = buf.readInt();
            blocks.put(bm, count);
            costs.put(bm, cost);
            if (buf.readBoolean()) {
                ItemStack stack = NetworkTools.readItemStack(buf);
                stacks.put(bm, stack);
            }
        }

        size = buf.readInt();
        Map<String, Integer> entities = new HashMap<>(size);
        Map<String, Integer> entityCosts = new HashMap<>(size);
        Map<String, Entity> realEntities = new HashMap<>();
        Map<String, String> playerNames = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String className = buf.readUtf(32767);
            int count = buf.readInt();
            int cost = buf.readInt();
            entities.put(className, count);
            entityCosts.put(className, cost);

            byte how = buf.readByte();
            if (how == ENTITY_NORMAL) {
                CompoundTag nbt = buf.readNbt();
                // @todo 1.14
//                EntityType<?> value = BuiltInRegistries.ENTITY_TYPE.getValue(new ResourceLocation(fixed));
//
//                entity = value.create(SafeClientTools.getClientWorld(), nbt, null, null, new BlockPos(0, 0, 0), SpawnReason.COMMAND, false, false);
//
//                Entity entity = EntityList.createEntityFromNBT(nbt, RFTools.proxy.getClientWorld());
//                realEntities.put(className, entity);
            } else if (how == ENTITY_PLAYER) {
                int entityId = buf.readInt();
                String entityName = buf.readUtf(32767);
                Entity entity = SafeClientTools.getClientWorld().getEntity(entityId);
                if (entity != null) {
                    realEntities.put(className, entity);
                }
                playerNames.put(className, entityName);
            }
        }
        return new PacketChamberInfoReady(blocks, costs, stacks, entities, entityCosts, realEntities, playerNames);
    }

    public static PacketChamberInfoReady create(Map<BlockState, Integer> blocks, Map<BlockState, Integer> costs,
                                                Map<BlockState, ItemStack> stacks,
                                                Map<String, Integer> entities, Map<String, Integer> entityCosts,
                                                Map<String, Entity> realEntities) {
        return new PacketChamberInfoReady(
                new HashMap<>(blocks), new HashMap<>(costs), new HashMap<>(stacks),
                new HashMap<>(entities), new HashMap<>(entityCosts), new HashMap<>(realEntities), new HashMap<>());
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            GuiChamberDetails.setItemsWithCount(blocks, costs, stacks,
                    entities, entityCosts, realEntities, playerNames);
        });
    }
}
