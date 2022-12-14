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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static mcjty.rftoolsbuilder.modules.mover.items.VehicleControlClientScreenModule.LARGESIZE;
import static mcjty.rftoolsbuilder.modules.mover.items.VehicleControlClientScreenModule.SMALLSIZE;

public class VehicleControlScreenModule implements IScreenModule<VehicleControlScreenModule.ModuleVehicleInfo> {

    private ResourceKey<Level> dim = Level.OVERWORLD;
    private BlockPos coordinate = BlockPosTools.INVALID;

    private boolean vertical = false;
    private boolean large = false;

    public static class ModuleVehicleInfo implements IModuleData {

        public static final String ID = RFToolsBuilder.MODID + ":vehicle_control";

        private List<String> movers;

        @Override
        public String getId() {
            return ID;
        }

        public ModuleVehicleInfo(List<String> movers) {
            this.movers = movers;
        }

        public ModuleVehicleInfo(FriendlyByteBuf buf) {
            int size = buf.readByte();
            movers = new ArrayList<>(size);
            for (int i = 0 ; i < size ; i++) {
                movers.add(buf.readUtf(32767));
            }
        }

        public List<String> getMovers() {
            return movers;
        }

        @Override
        public void writeToBuf(FriendlyByteBuf buf) {
            buf.writeByte(movers.size());
            for (String mover : movers) {
                buf.writeUtf(mover);
            }
        }
    }


    @Override
    public ModuleVehicleInfo getData(IScreenDataHelper helper, Level worldObj, long millis) {
        Level world = LevelTools.getLevel(worldObj, dim);
        if (world == null) {
            return null;
        }

        if (!LevelTools.isLoaded(world, coordinate)) {
            return null;
        }

        if (world.getBlockEntity(coordinate) instanceof MoverControllerTileEntity moverController) {
            List<String> movers = moverController.getMovers();
            return new ModuleVehicleInfo(new ArrayList<>(movers));
        }
        return null;
    }

    @Override
    public void setupFromNBT(CompoundTag tagCompound, ResourceKey<Level> dim, BlockPos pos) {
        if (tagCompound != null) {
            setupCoordinateFromNBT(tagCompound, dim, pos);
        }
    }

    protected void setupCoordinateFromNBT(CompoundTag tagCompound, ResourceKey<Level> dim, BlockPos pos) {
        coordinate = BlockPosTools.INVALID;
        if (tagCompound.contains("monitorx")) {
            this.dim = LevelTools.getId(tagCompound.getString("monitordim"));
            BlockPos c = new BlockPos(tagCompound.getInt("monitorx"), tagCompound.getInt("monitory"), tagCompound.getInt("monitorz"));
            int dx = Math.abs(c.getX() - pos.getX());
            int dy = Math.abs(c.getY() - pos.getY());
            int dz = Math.abs(c.getZ() - pos.getZ());
            coordinate = c;
        }
        vertical = tagCompound.getBoolean("vertical");
        large = tagCompound.getBoolean("large");
    }

    @Override
    public int getRfPerTick() {
        return MoverConfiguration.VEHICLE_CONTROL_RFPERTICK.get();
    }

    @Override
    public void mouseClick(Level world, int x, int y, boolean clicked, Player player) {
        if ((!clicked) || player == null) {
            return;
        }
        if (BlockPosTools.INVALID.equals(coordinate)) {
            player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "Module is not linked to storage scanner!"), false);
            return;
        }
        getMoverController(player.level, dim, coordinate).ifPresent(moverController -> {
            if (x >= 0) {
                List<String> movers = moverController.getMovers();
                int levelCount = movers.size();
                int level = -1;

                if (vertical) {
                    int max = large ? 6 : 8;
                    int numcols = (levelCount + max - 1) / max;
                    int colw = getColumnWidth(numcols);

                    int yoffset = 0;
                    if (y >= yoffset) {
                        level = (y - yoffset) / (((large ? LARGESIZE : SMALLSIZE) - 2));
                        if (level < 0) {
                            return;
                        }
                        if (numcols > 1) {
                            int col = (x - 5) / (colw + 7);
                            level = max - level - 1 + col * max;
                            if (col == numcols - 1) {
                                level -= max - (levelCount % max);
                            }
                        } else {
                            level = levelCount - level - 1;
                        }
                    }
                } else {
                    int xoffset = 5;
                    if (x >= xoffset) {
                        level = (x - xoffset) / (((large ? LARGESIZE : SMALLSIZE) - 2));
                    }
                }
                if (level >= 0 && level < levelCount) {
                    moverController.toLevel(level);
                }
            }
        });
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

    public static int getColumnWidth(int numcols) {
        int colw;
        switch (numcols) {
            case 1: colw = 120; break;
            case 2: colw = 58; break;
            case 3: colw = 36; break;
            case 4: colw = 25; break;
            case 5: colw = 19; break;
            default: colw = 15; break;
        }
        return colw;
    }



}
