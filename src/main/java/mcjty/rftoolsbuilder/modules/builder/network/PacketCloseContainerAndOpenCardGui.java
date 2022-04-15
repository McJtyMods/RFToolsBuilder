package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class PacketCloseContainerAndOpenCardGui {

    private final BlockPos builderPos;

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(builderPos);
    }

    public PacketCloseContainerAndOpenCardGui(BlockPos pos) {
        this.builderPos = pos;
    }

    public PacketCloseContainerAndOpenCardGui(PacketBuffer buf) {
        builderPos = buf.readBlockPos();
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ctx.getSender().doCloseContainer();
            RFToolsBuilderMessages.INSTANCE.send(PacketDistributor.PLAYER.with(ctx::getSender), new PacketOpenCardGuiFromBuilder());
            TileEntity te = ctx.getSender().getLevel().getBlockEntity(builderPos);
            if (te instanceof BuilderTileEntity) {
                ((BuilderTileEntity) te).setSupportMode(false);
            }
        });
        ctx.setPacketHandled(true);
    }
}
