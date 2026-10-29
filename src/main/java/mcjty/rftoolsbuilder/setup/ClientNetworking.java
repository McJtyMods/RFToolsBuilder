package mcjty.rftoolsbuilder.setup;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import mcjty.lib.network.PayloadContext;
import mcjty.rftoolsbuilder.modules.builder.network.*;
import mcjty.rftoolsbuilder.modules.mover.network.PacketGrabbedEntitiesToClient;
import mcjty.rftoolsbuilder.modules.mover.network.PacketSyncVehicleInformationToClient;
import mcjty.rftoolsbuilder.modules.scanner.network.PacketReturnExtraData;
import mcjty.rftoolsbuilder.modules.scanner.network.PacketReturnShapeData;

public final class ClientNetworking {

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(PacketOpenCardGuiFromBuilder.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.client())));
        ClientPlayNetworking.registerGlobalReceiver(PacketChamberInfoReady.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.client())));
        ClientPlayNetworking.registerGlobalReceiver(PacketSyncVehicleInformationToClient.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.client())));
        ClientPlayNetworking.registerGlobalReceiver(PacketGrabbedEntitiesToClient.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.client())));
        ClientPlayNetworking.registerGlobalReceiver(PacketReturnShapeData.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.client())));
        ClientPlayNetworking.registerGlobalReceiver(PacketReturnExtraData.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.client())));
    }
}
