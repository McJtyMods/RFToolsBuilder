package mcjty.rftoolsbuilder.modules.shield.data;

import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public interface IShieldWorldInfo {

    // Get the position of the shield projector given a shielding block
    @Nullable
    BlockPos getShieldProjector(BlockPos shieldingPos);
}
