package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.rftoolsbuilder.modules.shield.ShieldConfiguration;

import static mcjty.rftoolsbuilder.modules.shield.ShieldSetup.TYPE_SHIELD_BLOCK1;

public class ShieldTileEntity extends ShieldTEBase {

    public ShieldTileEntity() {
        super(TYPE_SHIELD_BLOCK1.get());
        setSupportedBlocks(ShieldConfiguration.maxShieldSize.get());
    }

    @Override
    protected int getConfigMaxEnergy() {
        return ShieldConfiguration.MAXENERGY.get();
    }

    @Override
    protected int getConfigRfPerTick() {
        return ShieldConfiguration.RECEIVEPERTICK.get();
    }
}
