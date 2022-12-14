package mcjty.rftoolsbuilder.modules.mover.items;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.data.IModuleData;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.mover.MoverConfiguration;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverControllerTileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public class VehicleControlScreenModule implements IScreenModule<VehicleControlScreenModule.ModuleVehicleInfo> {

    private ResourceKey<Level> dim = Level.OVERWORLD;
    private BlockPos coordinate = BlockPosTools.INVALID;

    private String line = "";
    private String mover = "";

    public static class ModuleVehicleInfo implements IModuleData {

        public static final String ID = RFToolsBuilder.MODID + ":vehicle_control";

        @Override
        public String getId() {
            return ID;
        }

        @Override
        public void writeToBuf(FriendlyByteBuf buf) {
        }
    }


    @Override
    public ModuleVehicleInfo getData(IScreenDataHelper helper, Level worldObj, long millis) {
        // For now we need no data
        return null;
    }

    @Override
    public void setupFromNBT(CompoundTag tagCompound, ResourceKey<Level> dim, BlockPos pos) {
        if (tagCompound != null) {
            setupCoordinateFromNBT(tagCompound, dim, pos);
            line = tagCompound.getString("text");
            mover = tagCompound.getString("mover");
        }
    }

    private void setupCoordinateFromNBT(CompoundTag tagCompound, ResourceKey<Level> dim, BlockPos pos) {
        coordinate = BlockPosTools.INVALID;
        if (tagCompound.contains("monitorx")) {
            this.dim = LevelTools.getId(tagCompound.getString("monitordim"));
            BlockPos c = new BlockPos(tagCompound.getInt("monitorx"), tagCompound.getInt("monitory"), tagCompound.getInt("monitorz"));
            int dx = Math.abs(c.getX() - pos.getX());
            int dy = Math.abs(c.getY() - pos.getY());
            int dz = Math.abs(c.getZ() - pos.getZ());
            coordinate = c;
        }
    }

    @Override
    public int getRfPerTick() {
        return MoverConfiguration.VEHICLE_CONTROL_RFPERTICK.get();
    }

    @Override
    public void mouseClick(Level world, int x, int y, boolean clicked, Player player) {
        int xoffset;
        if (!line.isEmpty()) {
            xoffset = 80;
        } else {
            xoffset = 5;
        }
        if (x >= xoffset) {
            if (!mover.isEmpty()) {
                getMoverController(world, dim, coordinate).ifPresent(controller -> {

                });
            } else {
                if (player != null) {
                    player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "Module is not linked to mover controller!"), false);
                }
            }
        }
    }

    public static Optional<MoverControllerTileEntity> getMoverController(Level worldObj, ResourceKey<Level> dim, BlockPos coordinate) {
        Level world = LevelTools.getLevel(worldObj, dim);
        if (world == null) {
            return Optional.empty();
        }

        if (!LevelTools.isLoaded(world, coordinate)) {
            return Optional.empty();
        }

        BlockEntity te = world.getBlockEntity(coordinate);
        if (te == null) {
            return Optional.empty();
        }

        if (!(te instanceof MoverControllerTileEntity)) {
            return Optional.empty();
        }

        return Optional.of((MoverControllerTileEntity) te);
    }
}
