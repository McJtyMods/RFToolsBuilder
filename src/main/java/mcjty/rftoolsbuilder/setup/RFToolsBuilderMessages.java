package mcjty.rftoolsbuilder.setup;

import mcjty.lib.network.Networking;
import mcjty.lib.network.PacketSendClientCommand;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import javax.annotation.Nonnull;

public class RFToolsBuilderMessages {

    public static void registerMessages(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(RFToolsBuilder.MODID)
                .versioned("1.0")
                .optional();

        // @todo 1.21
//        registrar.play(PacketUpdateNBTShapeCard.class, PacketUpdateNBTShapeCard::create, handler -> handler.server(PacketUpdateNBTShapeCard::handle));
//        registrar.play(PacketUpdateNBTItemInventoryShape.class, PacketUpdateNBTItemInventoryShape::create, handler -> handler.server(PacketUpdateNBTItemInventoryShape::handle));
//        registrar.play(PacketRequestShapeData.class, PacketRequestShapeData::create, handler -> handler.server(PacketRequestShapeData::handle));
//        registrar.play(PacketCloseContainerAndOpenCardGui.class, PacketCloseContainerAndOpenCardGui::create, handler -> handler.server(PacketCloseContainerAndOpenCardGui::handle));
//        registrar.play(PacketOpenCardGuiFromBuilder.class, PacketOpenCardGuiFromBuilder::create, handler -> handler.server(PacketOpenCardGuiFromBuilder::handle));
//        registrar.play(PacketOpenBuilderGui.class, PacketOpenBuilderGui::create, handler -> handler.server(PacketOpenBuilderGui::handle));
//        registrar.play(PacketNotifyServerClientReady.class, PacketNotifyServerClientReady::create, handler -> handler.server(PacketNotifyServerClientReady::handle));
//        registrar.play(PacketClickMover.class, PacketClickMover::create, handler -> handler.server(PacketClickMover::handle));
//
//        registrar.play(PacketGrabbedEntitiesToClient.class, PacketGrabbedEntitiesToClient::create, handler -> handler.client(PacketGrabbedEntitiesToClient::handle));
//        registrar.play(PacketReturnShapeData.class, PacketReturnShapeData::create, handler -> handler.client(PacketReturnShapeData::handle));
//        registrar.play(PacketChamberInfoReady.class, PacketChamberInfoReady::create, handler -> handler.client(PacketChamberInfoReady::handle));
//        registrar.play(PacketReturnExtraData.class, PacketReturnExtraData::create, handler -> handler.client(PacketReturnExtraData::handle));
//        registrar.play(PacketSyncVehicleInformationToClient.class, PacketSyncVehicleInformationToClient::create, handler -> handler.client(PacketSyncVehicleInformationToClient::handle));
    }

    public static void sendToServer(String command, @Nonnull TypedMap.Builder argumentBuilder) {
        Networking.sendToServer(new PacketSendServerCommand(RFToolsBuilder.MODID, command, argumentBuilder.build()));
    }

    public static void sendToServer(String command) {
        Networking.sendToServer(new PacketSendServerCommand(RFToolsBuilder.MODID, command, TypedMap.EMPTY));
    }

    public static void sendToClient(Player player, String command, @Nonnull TypedMap.Builder argumentBuilder) {
        Networking.sendToPlayer(new PacketSendClientCommand(RFToolsBuilder.MODID, command, argumentBuilder.build()), player);
    }

    public static void sendToClient(Player player, String command) {
        Networking.sendToPlayer(new PacketSendClientCommand(RFToolsBuilder.MODID, command, TypedMap.EMPTY), player);
    }

    public static <T extends CustomPacketPayload> void sendToPlayer(T packet, Player player) {
        PacketDistributor.sendToPlayer((ServerPlayer)player, packet);
    }

    public static <T extends CustomPacketPayload> void sendToServer(T packet) {
        PacketDistributor.sendToServer(packet);
    }

    public static <T extends CustomPacketPayload> void sendToChunk(T packet, ServerLevel level, BlockPos pos) {
        PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(pos), packet);
    }

    public static <T extends CustomPacketPayload> void sendToChunk(T packet, LevelChunk chunk) {
        PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) chunk.getLevel(), chunk.getPos(), packet);
    }
}
