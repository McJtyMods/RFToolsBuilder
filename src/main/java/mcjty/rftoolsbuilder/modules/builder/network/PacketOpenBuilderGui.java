package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.container.MenuTools;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import mcjty.lib.network.PayloadContext;

public record PacketOpenBuilderGui(BlockPos pos) implements CustomPacketPayload {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(RFToolsBuilder.MODID, "open_builder_gui");
    public static final CustomPacketPayload.Type<PacketOpenBuilderGui> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacketOpenBuilderGui> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketOpenBuilderGui::pos,
            PacketOpenBuilderGui::create);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static PacketOpenBuilderGui create(BlockPos fromTEPos) {
        return new PacketOpenBuilderGui(fromTEPos);
    }

    public void handle(PayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            Level world = player.level();
            BlockEntity te = world.getBlockEntity(pos);
            if (te == null) {
                return;
            }
            MenuProvider h = CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY.find(world, te.getBlockPos(), null);
            if (h != null) {
                MenuTools.open(player, h, buffer -> {
                    buffer.writeBlockPos(pos);
                    if (h instanceof DefaultContainerProvider<?> provider) provider.writeExtraData(buffer, te);
                });
            }
        });
    }
}
