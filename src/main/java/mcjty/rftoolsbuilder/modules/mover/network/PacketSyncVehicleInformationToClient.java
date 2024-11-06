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

import java.util.List;
import java.util.Optional;

public record PacketSyncVehicleInformationToClient(BlockPos pos, List<String> platforms, String currentPlatform, Boolean valid, Boolean enoughPower) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsBuilder.MODID, "sync_vehicle_information_to_client");
    public static final CustomPacketPayload.Type<PacketSyncVehicleInformationToClient> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacketSyncVehicleInformationToClient> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketSyncVehicleInformationToClient::pos,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), PacketSyncVehicleInformationToClient::platforms,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), s -> Optional.ofNullable(s.currentPlatform),
            ByteBufCodecs.BOOL, PacketSyncVehicleInformationToClient::valid,
            ByteBufCodecs.BOOL, PacketSyncVehicleInformationToClient::enoughPower,
            (pos, platforms, currentPlatform, valid, enoughPower) -> new PacketSyncVehicleInformationToClient(pos, platforms, currentPlatform.orElse(null), valid, enoughPower));

    public static PacketSyncVehicleInformationToClient create(BlockPos pos, List<String> platforms, String currentPlatform, boolean valid, boolean enoughPower) {
        return new PacketSyncVehicleInformationToClient(pos, platforms, currentPlatform, valid, enoughPower);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (SafeClientTools.getClientWorld().getBlockEntity(pos) instanceof MoverTileEntity mover) {
                mover.setClientRenderInfo(platforms, currentPlatform, valid, enoughPower);
            }
        });
    }
}
