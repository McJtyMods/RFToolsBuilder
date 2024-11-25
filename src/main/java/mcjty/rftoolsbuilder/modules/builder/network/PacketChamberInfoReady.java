package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.client.GuiChamberDetails;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record PacketChamberInfoReady(Map<BlockState, Integer> blocks,
                                     Map<BlockState, Integer> costs,
                                     Map<BlockState, ItemStack> stacks,
                                     Map<String, Integer> entities,
                                     Map<String, Integer> entityCosts,
                                     Map<String, CompoundTag> realEntities,
                                     Map<String, String> playerNames) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsBuilder.MODID, "chamberinfoready");
    public static final CustomPacketPayload.Type<PacketChamberInfoReady> TYPE = new Type<>(ID);

    private static final byte ENTITY_NONE = 0;
    private static final byte ENTITY_NORMAL = 1;
    private static final byte ENTITY_PLAYER = 2;

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketChamberInfoReady> CODEC = CompositeStreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), ByteBufCodecs.INT), PacketChamberInfoReady::blocks,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), ByteBufCodecs.INT), PacketChamberInfoReady::costs,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), ItemStack.STREAM_CODEC), PacketChamberInfoReady::stacks,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.INT), PacketChamberInfoReady::entities,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.INT), PacketChamberInfoReady::entityCosts,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.COMPOUND_TAG), PacketChamberInfoReady::realEntities,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8), PacketChamberInfoReady::playerNames,
            PacketChamberInfoReady::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static PacketChamberInfoReady create(Map<BlockState, Integer> blocks, Map<BlockState, Integer> costs,
                                                Map<BlockState, ItemStack> stacks,
                                                Map<String, Integer> entities, Map<String, Integer> entityCosts,
                                                Map<String, CompoundTag> realEntities) {
        return new PacketChamberInfoReady(
                new HashMap<>(blocks), new HashMap<>(costs), new HashMap<>(stacks),
                new HashMap<>(entities), new HashMap<>(entityCosts),
                new HashMap<>(realEntities), new HashMap<>());
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            GuiChamberDetails.setItemsWithCount(blocks, costs, stacks,
                    entities, entityCosts, realEntities, playerNames);
        });
    }
}
