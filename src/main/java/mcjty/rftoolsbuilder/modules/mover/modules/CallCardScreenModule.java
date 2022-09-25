package mcjty.rftoolsbuilder.modules.mover.modules;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.data.IModuleDataContents;
import mcjty.rftoolsbuilder.modules.mover.MoverConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Objects;

public class CallCardScreenModule implements IScreenModule<IModuleDataContents> {
    protected ResourceKey<Level> dim = Level.OVERWORLD;
    protected BlockPos coordinate = BlockPosTools.INVALID;
    protected String name;

    @Override
    public IModuleDataContents getData(IScreenDataHelper h, Level worldObj, long millis) {
        return null;
    }

    @Override
    public void setupFromNBT(CompoundTag tagCompound, ResourceKey<Level> dim, BlockPos pos) {
        if (tagCompound != null) {
            coordinate = BlockPosTools.INVALID;
            if (tagCompound.contains("monitorx")) {
                this.dim = LevelTools.getId(tagCompound.getString("monitordim"));
                if (Objects.equals(dim, this.dim)) {
                    BlockPos c = new BlockPos(tagCompound.getInt("monitorx"), tagCompound.getInt("monitory"), tagCompound.getInt("monitorz"));
                    int dx = Math.abs(c.getX() - pos.getX());
                    int dy = Math.abs(c.getY() - pos.getY());
                    int dz = Math.abs(c.getZ() - pos.getZ());
                    coordinate = c;
                }
            }
            name = tagCompound.getString("vehilceName");
        }
    }

    @Override
    public int getRfPerTick() {
        return MoverConfiguration.CALLCARD_RFPERTICK.get();
    }

    @Override
    public void mouseClick(Level world, int x, int y, boolean clicked, Player player) {
        // @todo
    }
}
