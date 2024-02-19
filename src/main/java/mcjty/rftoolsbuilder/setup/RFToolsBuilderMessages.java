package mcjty.rftoolsbuilder.setup;

import mcjty.lib.McJtyLib;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nonnull;

import static mcjty.lib.network.PlayPayloadContext.wrap;

public class RFToolsBuilderMessages {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void registerMessages(String name) {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(RFToolsBuilder.MODID, name))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.registerMessage(id(), PacketUpdateNBTShapeCard.class, PacketUpdateNBTShapeCard::write, PacketUpdateNBTShapeCard::create, wrap(PacketUpdateNBTShapeCard::handle));
        net.registerMessage(id(), PacketUpdateNBTItemInventoryShape.class, PacketUpdateNBTItemInventoryShape::write, PacketUpdateNBTItemInventoryShape::create, wrap(PacketUpdateNBTItemInventoryShape::handle));
        net.registerMessage(id(), PacketRequestShapeData.class, PacketRequestShapeData::write, PacketRequestShapeData::create, wrap(PacketRequestShapeData::handle));
        net.registerMessage(id(), PacketChamberInfoReady.class, PacketChamberInfoReady::write, PacketChamberInfoReady::create, wrap(PacketChamberInfoReady::handle));
        net.registerMessage(id(), PacketReturnShapeData.class, PacketReturnShapeData::write, PacketReturnShapeData::create, wrap(PacketReturnShapeData::handle));
        net.registerMessage(id(), PacketReturnExtraData.class, PacketReturnExtraData::write, PacketReturnExtraData::create, wrap(PacketReturnExtraData::handle));
        net.registerMessage(id(), PacketCloseContainerAndOpenCardGui.class, PacketCloseContainerAndOpenCardGui::write, PacketCloseContainerAndOpenCardGui::create, wrap(PacketCloseContainerAndOpenCardGui::handle));
        net.registerMessage(id(), PacketOpenCardGuiFromBuilder.class, PacketOpenCardGuiFromBuilder::write, PacketOpenCardGuiFromBuilder::create, wrap(PacketOpenCardGuiFromBuilder::handle));
        net.registerMessage(id(), PacketOpenBuilderGui.class, PacketOpenBuilderGui::write, PacketOpenBuilderGui::create, wrap(PacketOpenBuilderGui::handle));
        net.registerMessage(id(), PacketGrabbedEntitiesToClient.class, PacketGrabbedEntitiesToClient::write, PacketGrabbedEntitiesToClient::create, wrap(PacketGrabbedEntitiesToClient::handle));
        net.registerMessage(id(), PacketSyncVehicleInformationToClient.class, PacketSyncVehicleInformationToClient::write, PacketSyncVehicleInformationToClient::create, wrap(PacketSyncVehicleInformationToClient::handle));
        net.registerMessage(id(), PacketNotifyServerClientReady.class, PacketNotifyServerClientReady::write, PacketNotifyServerClientReady::create, wrap(PacketNotifyServerClientReady::handle));
        net.registerMessage(id(), PacketClickMover.class, PacketClickMover::write, PacketClickMover::create, wrap(PacketClickMover::handle));
    }

    // @TODO MOVE TO McJtyLib
    public static void sendToServer(String command, @Nonnull TypedMap.Builder argumentBuilder) {
        McJtyLib.sendToServer(new PacketSendServerCommand(RFToolsBuilder.MODID, command, argumentBuilder.build()));
    }

    public static void sendToServer(String command) {
        McJtyLib.sendToServer(new PacketSendServerCommand(RFToolsBuilder.MODID, command, TypedMap.EMPTY));
    }

    public static void sendToClient(Player player, String command, @Nonnull TypedMap.Builder argumentBuilder) {
        McJtyLib.sendToPlayer(new PacketSendClientCommand(RFToolsBuilder.MODID, command, argumentBuilder.build()), player);
    }

    public static void sendToClient(Player player, String command) {
        McJtyLib.sendToPlayer(new PacketSendClientCommand(RFToolsBuilder.MODID, command, TypedMap.EMPTY), player);
    }

    public static <T> void sendToPlayer(T packet, Player player) {
        INSTANCE.sendTo(packet, ((ServerPlayer)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T> void sendToServer(T packet) {
        INSTANCE.sendToServer(packet);
    }

    public static <T> void sendToChunk(T packet, Level level, BlockPos pos) {
        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), packet);
    }

    public static <T> void sendToChunk(T packet, LevelChunk chunk) {
        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), packet);
    }
}
