package mcjty.rftoolsbuilder.modules.mover.network;

import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record PacketSyncVehicleInformationToClient(BlockPos pos, List<String> platforms, String currentPlatform, Boolean valid, Boolean enoughPower) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsBuilder.MODID, "sync_vehicle_information_to_client");

    public static PacketSyncVehicleInformationToClient create(BlockPos pos, List<String> platforms, String currentPlatform, boolean valid, boolean enoughPower) {
        return new PacketSyncVehicleInformationToClient(pos, platforms, currentPlatform, valid, enoughPower);
    }

    public static PacketSyncVehicleInformationToClient create(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int size = buf.readInt();
        List<String> platforms = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            platforms.add(buf.readUtf(32767));
        }
        String currentPlatform;
        if (buf.readBoolean()) {
            currentPlatform = buf.readUtf(32767);
        } else {
            currentPlatform = null;
        }
        boolean valid = buf.readBoolean();
        boolean enoughPower = buf.readBoolean();
        return new PacketSyncVehicleInformationToClient(pos, platforms, currentPlatform, valid, enoughPower);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(platforms.size());
        for (String s : platforms) {
            buf.writeUtf(s);
        }
        if (currentPlatform != null) {
            buf.writeBoolean(true);
            buf.writeUtf(currentPlatform);
        } else {
            buf.writeBoolean(false);
        }
        buf.writeBoolean(valid);
        buf.writeBoolean(enoughPower);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            if (SafeClientTools.getClientWorld().getBlockEntity(pos) instanceof MoverTileEntity mover) {
                mover.setClientRenderInfo(platforms, currentPlatform, valid, enoughPower);
            }
        });
    }
}
