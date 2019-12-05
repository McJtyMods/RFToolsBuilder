package mcjty.rftoolsbuilder.setup;

import mcjty.lib.network.*;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.network.PacketChamberInfoReady;
import mcjty.rftoolsbuilder.modules.builder.network.PacketUpdateNBTItemInventoryShape;
import mcjty.rftoolsbuilder.modules.builder.network.PacketUpdateNBTShapeCard;
import mcjty.rftoolsbuilder.modules.scanner.network.PacketRequestShapeData;
import mcjty.rftoolsbuilder.modules.scanner.network.PacketReturnExtraData;
import mcjty.rftoolsbuilder.modules.scanner.network.PacketReturnShapeData;
import mcjty.rftoolsbuilder.modules.shield.network.PacketFiltersReady;
import mcjty.rftoolsbuilder.modules.shield.network.PacketGetFilters;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import javax.annotation.Nonnull;

public class RFToolsBuilderMessages {
    public static SimpleChannel INSTANCE;

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

        // Server side
        PacketHandler.debugRegister("RFTools Builder", net, id(), PacketUpdateNBTShapeCard.class, PacketUpdateNBTShapeCard::toBytes, PacketUpdateNBTShapeCard::new, PacketUpdateNBTShapeCard::handle);
        PacketHandler.debugRegister("RFTools Builder", net, id(), PacketUpdateNBTItemInventoryShape.class, PacketUpdateNBTItemInventoryShape::toBytes, PacketUpdateNBTItemInventoryShape::new, PacketUpdateNBTItemInventoryShape::handle);
        PacketHandler.debugRegister("RFTools Builder", net, id(), PacketRequestShapeData.class, PacketRequestShapeData::toBytes, PacketRequestShapeData::new, PacketRequestShapeData::handle);
        PacketHandler.debugRegister("RFTools Builder", net, id(), PacketChamberInfoReady.class, PacketChamberInfoReady::toBytes, PacketChamberInfoReady::new, PacketChamberInfoReady::handle);
        PacketHandler.debugRegister("RFTools Builder", net, id(), PacketReturnShapeData.class, PacketReturnShapeData::toBytes, PacketReturnShapeData::new, PacketReturnShapeData::handle);
        PacketHandler.debugRegister("RFTools Builder", net, id(), PacketReturnExtraData.class, PacketReturnExtraData::toBytes, PacketReturnExtraData::new, PacketReturnExtraData::handle);
        PacketHandler.debugRegister("RFTools Builder", net, id(), PacketFiltersReady.class, PacketFiltersReady::toBytes, PacketFiltersReady::new, PacketFiltersReady::handle);
        PacketHandler.debugRegister("RFTools Builder", net, id(), PacketGetFilters.class, PacketGetFilters::toBytes, PacketGetFilters::new, PacketGetFilters::handle);

        PacketHandler.debugRegister("RFTools Builder", net, id(), PacketRequestDataFromServer.class, PacketRequestDataFromServer::toBytes, PacketRequestDataFromServer::new,
                new ChannelBoundHandler<>(net, PacketRequestDataFromServer::handle));

        PacketHandler.registerStandardMessages("RFToolsBuilder - standard", id(), net);
    }

    public static void sendToServer(String command, @Nonnull TypedMap.Builder argumentBuilder) {
        INSTANCE.sendToServer(new PacketSendServerCommand(RFToolsBuilder.MODID, command, argumentBuilder.build()));
    }

    public static void sendToServer(String command) {
        INSTANCE.sendToServer(new PacketSendServerCommand(RFToolsBuilder.MODID, command, TypedMap.EMPTY));
    }

    public static void sendToClient(PlayerEntity player, String command, @Nonnull TypedMap.Builder argumentBuilder) {
        INSTANCE.sendTo(new PacketSendClientCommand(RFToolsBuilder.MODID, command, argumentBuilder.build()), ((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToClient(PlayerEntity player, String command) {
        INSTANCE.sendTo(new PacketSendClientCommand(RFToolsBuilder.MODID, command, TypedMap.EMPTY), ((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }
}
