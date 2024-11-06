package mcjty.rftoolsbuilder.modules.mover.network;

import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public record PacketGrabbedEntitiesToClient(BlockPos pos, Set<Integer> grabbedEntities) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsBuilder.MODID, "grabbed_entities_to_client");
    public static final CustomPacketPayload.Type<PacketGrabbedEntitiesToClient> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacketGrabbedEntitiesToClient> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketGrabbedEntitiesToClient::pos,
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()), s -> new ArrayList<>(s.grabbedEntities),
            (pos, list) -> new PacketGrabbedEntitiesToClient(pos, new HashSet<>(list)));

    public PacketGrabbedEntitiesToClient(BlockPos pos, Set<Integer> grabbedEntities) {
        this.pos = pos;
        this.grabbedEntities = new HashSet<>(grabbedEntities);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static PacketGrabbedEntitiesToClient create(BlockPos worldPosition, Set<Integer> integers) {
        return new PacketGrabbedEntitiesToClient(worldPosition, integers);
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (SafeClientTools.getClientWorld().getBlockEntity(pos) instanceof MoverTileEntity mover) {
                mover.getLogic().setGrabbedEntitiesClient(grabbedEntities);
            }
        });
    }
}
