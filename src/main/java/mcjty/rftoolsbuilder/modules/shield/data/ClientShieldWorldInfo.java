package mcjty.rftoolsbuilder.modules.shield.data;

import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ClientShieldWorldInfo implements IShieldWorldInfo{

    private Map<SubChunkIndex, ShieldChunkInfo> shieldData = new HashMap<>();

    // Get the position of the shield projector given a shielding block
    @Nullable
    @Override
    public BlockPos getShieldProjector(BlockPos shieldingPos) {
        SubChunkIndex index = ShieldWorldInfo.calculateSubChunkIndex(shieldingPos);
        ShieldChunkInfo info = shieldData.get(index);
        if (info == null) {
            return null;
        }
        return info.getShieldProjector(shieldingPos);
    }


}
