package mcjty.rftoolsbuilder.modules.shield.blocks;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.text.ITextComponent;

import java.util.Set;

public class FakePlayerConnection extends ServerPlayNetHandler {

    public FakePlayerConnection(ServerPlayerEntity player) {
        super(null, new NetworkManager(PacketDirection.CLIENTBOUND), player);
    }

    @Override
    public void disconnect(ITextComponent textComponent) {
    }

    @Override
    public void onDisconnect(ITextComponent reason) {
    }

    @Override
    public void teleport(double x, double y, double z, float yaw, float pitch) {
    }

    @Override
    public void handleLockDifficulty(CLockDifficultyPacket p_217261_1_) {
    }

    @Override
    public void handleChangeDifficulty(CSetDifficultyPacket p_217263_1_) {
    }

    @Override
    public void handleSetJigsawBlock(CUpdateJigsawBlockPacket p_217262_1_) {
    }

    @Override
    public void handleAnimate(CAnimateHandPacket packetIn) {
    }

    @Override
    public void handleClientCommand(CClientStatusPacket packetIn) {
    }

    @Override
    public void handleMovePlayer(CPlayerPacket packetIn) {
    }

    @Override
    public void handleContainerButtonClick(CEnchantItemPacket packetIn) {
    }

    @Override
    public void handleContainerClose(CCloseWindowPacket packetIn) {
    }

    @Override
    public void handleSeenAdvancements(CSeenAdvancementsPacket packetIn) {
    }

    @Override
    public void handleMoveVehicle(CMoveVehiclePacket packetIn) {
    }

    @Override
    public void handleResourcePackResponse(CResourcePackStatusPacket packetIn) {
    }

    @Override
    public void handleChat(CChatMessagePacket packetIn) {
    }

    @Override
    public void handleTeleportToEntityPacket(CSpectatePacket packetIn) {
    }

    @Override
    public void handleClientInformation(CClientSettingsPacket packetIn) {
    }

    @Override
    public void handleContainerClick(CClickWindowPacket packetIn) {
    }

    @Override
    public void handleCustomPayload(CCustomPayloadPacket packetIn) {
    }

    @Override
    public void handleSetCreativeModeSlot(CCreativeInventoryActionPacket packetIn) {
    }

    @Override
    public void handleAcceptTeleportPacket(CConfirmTeleportPacket packetIn) {
    }

    @Override
    public void handlePlayerCommand(CEntityActionPacket packetIn) {
    }

    @Override
    public void handleContainerAck(CConfirmTransactionPacket packetIn) {
    }

    @Override
    public void handlePlayerInput(CInputPacket packetIn) {
    }

    @Override
    public void handleEditBook(CEditBookPacket packetIn) {
    }

    @Override
    public void handleBlockEntityTagQuery(CQueryTileEntityNBTPacket packetIn) {
    }

    @Override
    public void handleSetCarriedItem(CHeldItemChangePacket packetIn) {
    }

    @Override
    public void handlePlaceRecipe(CPlaceRecipePacket packetIn) {
    }

    @Override
    public void handleKeepAlive(CKeepAlivePacket packetIn) {
    }

    @Override
    public void handlePlayerAction(CPlayerDiggingPacket packetIn) {
    }

    @Override
    public void handleEntityTagQuery(CQueryEntityNBTPacket packetIn) {
    }

    @Override
    public void handleSelectTrade(CSelectTradePacket packetIn) {
    }

    @Override
    public void handlePickItem(CPickItemPacket packetIn) {
    }

    @Override
    public void handleCustomCommandSuggestions(CTabCompletePacket packetIn) {
    }

    @Override
    public void handlePlayerAbilities(CPlayerAbilitiesPacket packetIn) {
    }

    @Override
    public void handleUseItemOn(CPlayerTryUseItemOnBlockPacket packetIn) {
    }

    @Override
    public void handlePaddleBoat(CSteerBoatPacket packetIn) {
    }

    @Override
    public void handleSetCommandBlock(CUpdateCommandBlockPacket packetIn) {
    }

    @Override
    public void handleRenameItem(CRenameItemPacket packetIn) {
    }

    @Override
    public void handleSignUpdate(CUpdateSignPacket packetIn) {
    }

    @Override
    public void handleUseItem(CPlayerTryUseItemPacket packetIn) {
    }

    @Override
    public void handleInteract(CUseEntityPacket packetIn) {
    }

    @Override
    public void handleSetCommandMinecart(CUpdateMinecartCommandBlockPacket packetIn) {
    }

    @Override
    public void handleSetStructureBlock(CUpdateStructureBlockPacket packetIn) {
    }

    @Override
    public void handleSetBeaconPacket(CUpdateBeaconPacket packetIn) {
    }

    @Override
    public void send(IPacket<?> packetIn, GenericFutureListener<? extends Future<? super Void>> futureListeners) {
    }

    @Override
    public void teleport(double x, double y, double z, float yaw, float pitch, Set<SPlayerPositionLookPacket.Flags> relativeSet) {
    }

    @Override
    public void send(IPacket<?> packetIn) {
    }

    @Override
    public void tick() {
    }
}
