package mcjty.rftoolsbuilder.modules.builder.blocks;

import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbuilder.modules.builder.BuilderConfiguration;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.SpaceChamberRepository;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

public class SpaceChamberControllerTileEntity extends GenericTileEntity {

    private BlockPos minCorner;
    private BlockPos maxCorner;
    private int channel = -1;

    public SpaceChamberControllerTileEntity() {
        super(BuilderModule.TYPE_SPACE_CHAMBER_CONTROLLER.get());
    }

    public BlockPos getMinCorner() {
        return minCorner;
    }

    public BlockPos getMaxCorner() {
        return maxCorner;
    }

    public void createChamber(PlayerEntity player) {
        BlockPos pos = getBlockPos();
        int x1 = pos.getX();
        int y1 = pos.getY();
        int z1 = pos.getZ();
        int x2 = x1;
        int y2 = y1;
        int z2 = z1;
        for (int i = 1; i < BuilderConfiguration.maxSpaceChamberDimension.get(); i++) {
            if (x2 == x1) {
                if (getLevel().getBlockState(new BlockPos(x1 - i, y1, z1)).getBlock() == BuilderModule.SPACE_CHAMBER.get()) {
                    x2 = x1-i;
                } else if (level.getBlockState(new BlockPos(x1 + i, y1, z1)).getBlock() == BuilderModule.SPACE_CHAMBER.get()) {
                    x2 = x1+i;
                }
            }
            if (z2 == z1) {
                if (level.getBlockState(new BlockPos(x1, y1, z1 - i)).getBlock() == BuilderModule.SPACE_CHAMBER.get()) {
                    z2 = z1-i;
                } else if (level.getBlockState(new BlockPos(x1, y1, z1 + i)).getBlock() == BuilderModule.SPACE_CHAMBER.get()) {
                    z2 = z1+i;
                }
            }
        }

        if (x1 == x2 || z2 == z1) {
            Logging.message(player, TextFormatting.RED + "Not a valid chamber shape!");
            return;
        }

        if (level.getBlockState(new BlockPos(x2, y1, z2)).getBlock() != BuilderModule.SPACE_CHAMBER.get()) {
            Logging.message(player, TextFormatting.RED + "Not a valid chamber shape!");
            return;
        }

        for (int i = 1 ; i < BuilderConfiguration.maxSpaceChamberDimension.get(); i++) {
            if (level.getBlockState(new BlockPos(x1, y1 - i, z1)).getBlock() == BuilderModule.SPACE_CHAMBER.get()) {
                y2 = y1-i;
                break;
            }
            if (level.getBlockState(new BlockPos(x1, y1 + i, z1)).getBlock() == BuilderModule.SPACE_CHAMBER.get()) {
                y2 = y1+i;
                break;
            }
        }

        if (y1 == y2) {
            Logging.message(player, TextFormatting.RED + "Not a valid chamber shape!");
            return;
        }

        if (level.getBlockState(new BlockPos(x2, y2, z2)).getBlock() != BuilderModule.SPACE_CHAMBER.get()) {
            Logging.message(player, TextFormatting.RED + "Not a valid chamber shape!");
            return;
        }

        if (level.getBlockState(new BlockPos(x1, y2, z2)).getBlock() != BuilderModule.SPACE_CHAMBER.get()) {
            Logging.message(player, TextFormatting.RED + "Not a valid chamber shape!");
            return;
        }

        if (level.getBlockState(new BlockPos(x2, y2, z1)).getBlock() != BuilderModule.SPACE_CHAMBER.get()) {
            Logging.message(player, TextFormatting.RED + "Not a valid chamber shape!");
            return;
        }

        // We have a valid shape.
        minCorner = new BlockPos(Math.min(x1, x2)+1, Math.min(y1, y2)+1, Math.min(z1, z2)+1);
        maxCorner = new BlockPos(Math.max(x1, x2)-1, Math.max(y1, y2)-1, Math.max(z1, z2)-1);
        if (minCorner.getX() > maxCorner.getX() || minCorner.getY() > maxCorner.getY() || minCorner.getZ() > maxCorner.getZ()) {
            Logging.message(player, TextFormatting.RED + "Chamber is too small!");
            minCorner = null;
            maxCorner = null;
            return;
        }

        Logging.message(player, TextFormatting.WHITE + "Chamber succesfully created!");

        SpaceChamberRepository chamberRepository = SpaceChamberRepository.get(level);
        SpaceChamberRepository.SpaceChamberChannel chamberChannel = chamberRepository.getOrCreateChannel(channel);
        chamberChannel.setDimension(level.dimension());
        chamberChannel.setMinCorner(minCorner);
        chamberChannel.setMaxCorner(maxCorner);
        chamberRepository.save();

        setChanged();
    }

    public int getChannel() {
        return channel;
    }

    public int getChamberSize() {
        if (channel == -1) {
            return -1;
        }
        if (minCorner == null) {
            return -1;
        }
        return (maxCorner.getX() - minCorner.getX()) * (maxCorner.getY() - minCorner.getY()) * (maxCorner.getZ() - minCorner.getZ());
    }

    public void setChannel(int channel) {
        this.channel = channel;
        setChanged();
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        minCorner = BlockPosTools.read(tagCompound, "minCorner");
        maxCorner = BlockPosTools.read(tagCompound, "maxCorner");
    }

    @Override
    public CompoundNBT save(CompoundNBT tagCompound) {
        super.save(tagCompound);
        BlockPosTools.write(tagCompound, "minCorner", minCorner);
        BlockPosTools.write(tagCompound, "maxCorner", maxCorner);
        return tagCompound;
    }
}
