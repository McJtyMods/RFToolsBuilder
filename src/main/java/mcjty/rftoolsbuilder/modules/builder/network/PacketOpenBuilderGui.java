package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketOpenBuilderGui(BlockPos pos) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsBuilder.MODID, "open_builder_gui");
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

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            Level world = player.getCommandSenderWorld();
            BlockEntity te = world.getBlockEntity(pos);
            if (te == null) {
                return;
            }
            MenuProvider h = world.getCapability(CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY, te.getBlockPos(), null);
            player.openMenu(h);
        });
    }
}
