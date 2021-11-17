package mcjty.rftoolsbuilder.modules.shield.blocks;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.util.Set;

public class FakePlayerConnection extends ServerPlayNetHandler {

    public FakePlayerConnection(ServerPlayerEntity player) {
        super(null, new NetworkManager(PacketDirection.CLIENTBOUND), player);
    }

    @Override
    public void disconnect(@Nonnull ITextComponent textComponent) {
    }

    @Override
    public void onDisconnect(@Nonnull ITextComponent reason) {
    }

    @Override
    public void teleport(double x, double y, double z, float yaw, float pitch) {
    }

    @Override
    public void handleLockDifficulty(@Nonnull CLockDifficultyPacket p_217261_1_) {
    }

    @Override
    public void handleChangeDifficulty(@Nonnull CSetDifficultyPacket p_217263_1_) {
    }

    @Override
    public void handleSetJigsawBlock(@Nonnull CUpdateJigsawBlockPacket p_217262_1_) {
    }

    @Override
    public void handleAnimate(@Nonnull CAnimateHandPacket packetIn) {
    }

    @Override
    public void handleClientCommand(@Nonnull CClientStatusPacket packetIn) {
    }

    @Override
    public void handleMovePlayer(@Nonnull CPlayerPacket packetIn) {
    }

    @Override
    public void handleContainerButtonClick(@Nonnull CEnchantItemPacket packetIn) {
    }

    @Override
    public void handleContainerClose(@Nonnull CCloseWindowPacket packetIn) {
    }

    @Override
    public void handleSeenAdvancements(@Nonnull CSeenAdvancementsPacket packetIn) {
    }

    @Override
    public void handleMoveVehicle(@Nonnull CMoveVehiclePacket packetIn) {
    }

    @Override
    public void handleResourcePackResponse(@Nonnull CResourcePackStatusPacket packetIn) {
    }

    @Override
    public void handleChat(@Nonnull CChatMessagePacket packetIn) {
    }

    @Override
    public void handleTeleportToEntityPacket(@Nonnull CSpectatePacket packetIn) {
    }

    @Override
    public void handleClientInformation(@Nonnull CClientSettingsPacket packetIn) {
    }

    @Override
    public void handleContainerClick(@Nonnull CClickWindowPacket packetIn) {
    }

    @Override
    public void handleCustomPayload(@Nonnull CCustomPayloadPacket packetIn) {
    }

    @Override
    public void handleSetCreativeModeSlot(@Nonnull CCreativeInventoryActionPacket packetIn) {
    }

    @Override
    public void handleAcceptTeleportPacket(@Nonnull CConfirmTeleportPacket packetIn) {
    }

    @Override
    public void handlePlayerCommand(@Nonnull CEntityActionPacket packetIn) {
    }

    @Override
    public void handleContainerAck(@Nonnull CConfirmTransactionPacket packetIn) {
    }

    @Override
    public void handlePlayerInput(@Nonnull CInputPacket packetIn) {
    }

    @Override
    public void handleEditBook(@Nonnull CEditBookPacket packetIn) {
    }

    @Override
    public void handleBlockEntityTagQuery(@Nonnull CQueryTileEntityNBTPacket packetIn) {
    }

    @Override
    public void handleSetCarriedItem(@Nonnull CHeldItemChangePacket packetIn) {
    }

    @Override
    public void handlePlaceRecipe(@Nonnull CPlaceRecipePacket packetIn) {
    }

    @Override
    public void handleKeepAlive(@Nonnull CKeepAlivePacket packetIn) {
    }

    @Override
    public void handlePlayerAction(@Nonnull CPlayerDiggingPacket packetIn) {
    }

    @Override
    public void handleEntityTagQuery(@Nonnull CQueryEntityNBTPacket packetIn) {
    }

    @Override
    public void handleSelectTrade(@Nonnull CSelectTradePacket packetIn) {
    }

    @Override
    public void handlePickItem(@Nonnull CPickItemPacket packetIn) {
    }

    @Override
    public void handleCustomCommandSuggestions(@Nonnull CTabCompletePacket packetIn) {
    }

    @Override
    public void handlePlayerAbilities(@Nonnull CPlayerAbilitiesPacket packetIn) {
    }

    @Override
    public void handleUseItemOn(@Nonnull CPlayerTryUseItemOnBlockPacket packetIn) {
    }

    @Override
    public void handlePaddleBoat(@Nonnull CSteerBoatPacket packetIn) {
    }

    @Override
    public void handleSetCommandBlock(@Nonnull CUpdateCommandBlockPacket packetIn) {
    }

    @Override
    public void handleRenameItem(@Nonnull CRenameItemPacket packetIn) {
    }

    @Override
    public void handleSignUpdate(@Nonnull CUpdateSignPacket packetIn) {
    }

    @Override
    public void handleUseItem(@Nonnull CPlayerTryUseItemPacket packetIn) {
    }

    @Override
    public void handleInteract(@Nonnull CUseEntityPacket packetIn) {
    }

    @Override
    public void handleSetCommandMinecart(@Nonnull CUpdateMinecartCommandBlockPacket packetIn) {
    }

    @Override
    public void handleSetStructureBlock(@Nonnull CUpdateStructureBlockPacket packetIn) {
    }

    @Override
    public void handleSetBeaconPacket(@Nonnull CUpdateBeaconPacket packetIn) {
    }

    @Override
    public void send(@Nonnull IPacket<?> packetIn, GenericFutureListener<? extends Future<? super Void>> futureListeners) {
    }

    @Override
    public void teleport(double x, double y, double z, float yaw, float pitch, @Nonnull Set<SPlayerPositionLookPacket.Flags> relativeSet) {
    }

    @Override
    public void send(@Nonnull IPacket<?> packetIn) {
    }

    @Override
    public void tick() {
    }
}
