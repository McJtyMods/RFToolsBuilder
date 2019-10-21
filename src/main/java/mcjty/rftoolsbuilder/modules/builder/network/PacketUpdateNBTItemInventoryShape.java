package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.lib.network.NetworkTools;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUpdateNBTItemInventoryShape {

    public BlockPos pos;
    public int slotIndex;
    public CompoundNBT tagCompound;

    public PacketUpdateNBTItemInventoryShape() {
    }

    public PacketUpdateNBTItemInventoryShape(PacketBuffer buf) {
        pos = buf.readBlockPos();
        slotIndex = buf.readInt();
        tagCompound = NetworkTools.readTag(buf);
    }

    public PacketUpdateNBTItemInventoryShape(BlockPos pos, int slotIndex, CompoundNBT tagCompound) {
        this.pos = pos;
        this.slotIndex = slotIndex;
        this.tagCompound = tagCompound;
    }

    protected boolean isValidBlock(World world, BlockPos blockPos, TileEntity tileEntity) {
        return /* @todo 1.14 tileEntity instanceof ComposerTileEntity || */tileEntity instanceof BuilderTileEntity;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(slotIndex);
        NetworkTools.writeTag(buf, tagCompound);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            World world = ctx.getSender().getEntityWorld();
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof IInventory) {
                if (!isValidBlock(world, pos, te)) {
                    return;
                }
                IInventory inv = (IInventory) te;
                ItemStack stack = inv.getStackInSlot(slotIndex);
                if (!stack.isEmpty()) {
                    stack.setTag(tagCompound);
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
