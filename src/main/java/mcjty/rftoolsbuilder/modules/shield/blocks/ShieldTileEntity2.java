package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.rftoolsbuilder.modules.shield.ShieldConfiguration;

import static mcjty.rftoolsbuilder.modules.shield.ShieldSetup.TYPE_SHIELD_BLOCK2;

public class ShieldTileEntity2 extends ShieldTEBase {

    public ShieldTileEntity2() {
        super(TYPE_SHIELD_BLOCK2.get());
        setSupportedBlocks(ShieldConfiguration.maxShieldSize.get() * 4);
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
