package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.NetworkHooks;

import java.util.function.Supplier;

public record PacketOpenBuilderGui(BlockPos pos) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsBuilder.MODID, "open_builder_gui");

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static PacketOpenBuilderGui create(FriendlyByteBuf buf) {
        return new PacketOpenBuilderGui(buf.readBlockPos());
    }

    public static PacketOpenBuilderGui create(BlockPos fromTEPos) {
        return new PacketOpenBuilderGui(fromTEPos);
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(player -> {
                Level world = player.getCommandSenderWorld();
                BlockEntity te = world.getBlockEntity(pos);
                if (te == null) {
                    return;
                }
                te.getCapability(CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY).ifPresent(h -> {
                    NetworkHooks.openScreen((ServerPlayer) player, h, pos);
                });
            });
        });
    }
}
