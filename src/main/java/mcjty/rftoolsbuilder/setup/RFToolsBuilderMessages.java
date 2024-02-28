package mcjty.rftoolsbuilder.setup;

import mcjty.lib.network.IPayloadRegistrar;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.NetworkDirection;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;

public class RFToolsBuilderMessages {

    private static IPayloadRegistrar registrar;

    public static void registerMessages() {
        registrar = Networking.registrar(RFToolsBuilder.MODID)
                .versioned("1.0")
                .optional();

        registrar.play(PacketUpdateNBTShapeCard.class, PacketUpdateNBTShapeCard::create, handler -> handler.server(PacketUpdateNBTShapeCard::handle));
        registrar.play(PacketUpdateNBTItemInventoryShape.class, PacketUpdateNBTItemInventoryShape::create, handler -> handler.server(PacketUpdateNBTItemInventoryShape::handle));
        registrar.play(PacketRequestShapeData.class, PacketRequestShapeData::create, handler -> handler.server(PacketRequestShapeData::handle));
        registrar.play(PacketCloseContainerAndOpenCardGui.class, PacketCloseContainerAndOpenCardGui::create, handler -> handler.server(PacketCloseContainerAndOpenCardGui::handle));
        registrar.play(PacketOpenCardGuiFromBuilder.class, PacketOpenCardGuiFromBuilder::create, handler -> handler.server(PacketOpenCardGuiFromBuilder::handle));
        registrar.play(PacketOpenBuilderGui.class, PacketOpenBuilderGui::create, handler -> handler.server(PacketOpenBuilderGui::handle));
        registrar.play(PacketNotifyServerClientReady.class, PacketNotifyServerClientReady::create, handler -> handler.server(PacketNotifyServerClientReady::handle));
        registrar.play(PacketClickMover.class, PacketClickMover::create, handler -> handler.server(PacketClickMover::handle));

        registrar.play(PacketGrabbedEntitiesToClient.class, PacketGrabbedEntitiesToClient::create, handler -> handler.client(PacketGrabbedEntitiesToClient::handle));
        registrar.play(PacketReturnShapeData.class, PacketReturnShapeData::create, handler -> handler.client(PacketReturnShapeData::handle));
        registrar.play(PacketChamberInfoReady.class, PacketChamberInfoReady::create, handler -> handler.client(PacketChamberInfoReady::handle));
        registrar.play(PacketReturnExtraData.class, PacketReturnExtraData::create, handler -> handler.client(PacketReturnExtraData::handle));
        registrar.play(PacketSyncVehicleInformationToClient.class, PacketSyncVehicleInformationToClient::create, handler -> handler.client(PacketSyncVehicleInformationToClient::handle));
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

    public static <T> void sendToPlayer(T packet, Player player) {
        registrar.getChannel().sendTo(packet, ((ServerPlayer)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T> void sendToServer(T packet) {
        registrar.getChannel().sendToServer(packet);
    }

    public static <T> void sendToChunk(T packet, Level level, BlockPos pos) {
        registrar.getChannel().send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), packet);
    }

    public static <T> void sendToChunk(T packet, LevelChunk chunk) {
        registrar.getChannel().send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), packet);
    }
}
