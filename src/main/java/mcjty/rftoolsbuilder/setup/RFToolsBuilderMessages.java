package mcjty.rftoolsbuilder.setup;

import mcjty.lib.network.PayloadContext;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import mcjty.lib.network.Networking;
import mcjty.lib.network.PacketSendClientCommand;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.network.*;
import mcjty.rftoolsbuilder.modules.mover.network.PacketClickMover;
import mcjty.rftoolsbuilder.modules.mover.network.PacketGrabbedEntitiesToClient;
import mcjty.rftoolsbuilder.modules.mover.network.PacketSyncVehicleInformationToClient;
import mcjty.rftoolsbuilder.modules.scanner.network.PacketRequestShapeData;
import mcjty.rftoolsbuilder.modules.scanner.network.PacketReturnExtraData;
import mcjty.rftoolsbuilder.modules.scanner.network.PacketReturnShapeData;
import mcjty.rftoolsbuilder.modules.shield.network.PacketNotifyServerClientReady;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;

public class RFToolsBuilderMessages {

    public static void registerMessages() {
        PayloadTypeRegistry.serverboundPlay().register(PacketUpdateCardInInventory.TYPE, PacketUpdateCardInInventory.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketUpdateCardInInventory.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.serverboundPlay().register(PacketUpdateCardInPlayer.TYPE, PacketUpdateCardInPlayer.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketUpdateCardInPlayer.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.serverboundPlay().register(PacketCloseContainerAndOpenCardGui.TYPE, PacketCloseContainerAndOpenCardGui.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketCloseContainerAndOpenCardGui.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.serverboundPlay().register(PacketOpenBuilderGui.TYPE, PacketOpenBuilderGui.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketOpenBuilderGui.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.serverboundPlay().register(PacketNotifyServerClientReady.TYPE, PacketNotifyServerClientReady.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketNotifyServerClientReady.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.serverboundPlay().register(PacketClickMover.TYPE, PacketClickMover.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketClickMover.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.serverboundPlay().register(PacketRequestShapeData.TYPE, PacketRequestShapeData.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketRequestShapeData.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.clientboundPlay().register(PacketOpenCardGuiFromBuilder.TYPE, PacketOpenCardGuiFromBuilder.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PacketChamberInfoReady.TYPE, PacketChamberInfoReady.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PacketSyncVehicleInformationToClient.TYPE, PacketSyncVehicleInformationToClient.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PacketGrabbedEntitiesToClient.TYPE, PacketGrabbedEntitiesToClient.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PacketReturnShapeData.TYPE, PacketReturnShapeData.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PacketReturnExtraData.TYPE, PacketReturnExtraData.CODEC);
    }

    public static void sendToServer(String command, @NotNull TypedMap.Builder argumentBuilder) {
        Networking.sendToServer(new PacketSendServerCommand(RFToolsBuilder.MODID, command, argumentBuilder.build()));
    }

    public static void sendToServer(String command) {
        Networking.sendToServer(new PacketSendServerCommand(RFToolsBuilder.MODID, command, TypedMap.EMPTY));
    }

    public static void sendToClient(Player player, String command, @NotNull TypedMap.Builder argumentBuilder) {
        Networking.sendToPlayer(new PacketSendClientCommand(RFToolsBuilder.MODID, command, argumentBuilder.build()), player);
    }

    public static void sendToClient(Player player, String command) {
        Networking.sendToPlayer(new PacketSendClientCommand(RFToolsBuilder.MODID, command, TypedMap.EMPTY), player);
    }

    public static <T extends CustomPacketPayload> void sendToPlayer(T packet, Player player) {
        Networking.sendToPlayer(packet, player);
    }

    public static <T extends CustomPacketPayload> void sendToServer(T packet) {
        Networking.sendToServer(packet);
    }

    public static <T extends CustomPacketPayload> void sendToChunk(T packet, ServerLevel level, BlockPos pos) {
        PlayerLookup.tracking(level, new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4)).forEach(player -> ServerPlayNetworking.send(player, packet));
    }

    public static <T extends CustomPacketPayload> void sendToChunk(T packet, LevelChunk chunk) {
        PlayerLookup.tracking((ServerLevel) chunk.getLevel(), chunk.getPos()).forEach(player -> ServerPlayNetworking.send(player, packet));
    }
}
