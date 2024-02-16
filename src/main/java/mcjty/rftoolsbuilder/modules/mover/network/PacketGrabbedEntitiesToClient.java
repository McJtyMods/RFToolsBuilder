package mcjty.rftoolsbuilder.modules.mover.network;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public record PacketGrabbedEntitiesToClient(BlockPos pos, Set<Integer> grabbedEntities) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsBuilder.MODID, "grabbed_entities_to_client");

    public PacketGrabbedEntitiesToClient(BlockPos pos, Set<Integer> grabbedEntities) {
        this.pos = pos;
        this.grabbedEntities = new HashSet<>(grabbedEntities);
    }

    public static PacketGrabbedEntitiesToClient create(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Set<Integer> grabbedEntities = new HashSet<>(size);
        for (int i = 0 ; i < size ; i++) {
            grabbedEntities.add(buf.readInt());
        }
        BlockPos pos = buf.readBlockPos();
        return new PacketGrabbedEntitiesToClient(pos, grabbedEntities);
    }

    public static PacketGrabbedEntitiesToClient create(BlockPos worldPosition, Set<Integer> integers) {
        return new PacketGrabbedEntitiesToClient(worldPosition, integers);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(grabbedEntities.size());
        for (Integer entity : grabbedEntities) {
            buf.writeInt(entity);
        }
        buf.writeBlockPos(pos);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            if (SafeClientTools.getClientWorld().getBlockEntity(pos) instanceof MoverTileEntity mover) {
                mover.getLogic().setGrabbedEntitiesClient(grabbedEntities);
            }
        });
    }
}
