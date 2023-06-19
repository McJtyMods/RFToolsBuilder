package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class PacketCloseContainerAndOpenCardGui {

    private final BlockPos builderPos;

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(builderPos);
    }

    public PacketCloseContainerAndOpenCardGui(BlockPos pos) {
        this.builderPos = pos;
    }

    public PacketCloseContainerAndOpenCardGui(FriendlyByteBuf buf) {
        builderPos = buf.readBlockPos();
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ctx.getSender().doCloseContainer();
            RFToolsBuilderMessages.INSTANCE.send(PacketDistributor.PLAYER.with(ctx::getSender), new PacketOpenCardGuiFromBuilder());
            BlockEntity te = ctx.getSender().level().getBlockEntity(builderPos);
            if (te instanceof BuilderTileEntity) {
                ((BuilderTileEntity) te).setSupportMode(false);
            }
        });
        ctx.setPacketHandled(true);
    }
}
