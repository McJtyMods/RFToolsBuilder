package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.rftoolsbuilder.modules.shield.ShieldConfiguration;

import static mcjty.rftoolsbuilder.modules.shield.ShieldSetup.TYPE_SHIELD_BLOCK3;

public class ShieldTileEntity3 extends ShieldTEBase {

    public ShieldTileEntity3() {
        super(TYPE_SHIELD_BLOCK3.get());
        setSupportedBlocks(ShieldConfiguration.maxShieldSize.get() * 16);
        setDamageFactor(4.0f);
        setCostFactor(2.0f);
    }

    @Override
    protected int getConfigMaxEnergy() {
        return ShieldConfiguration.MAXENERGY.get() * 3;
    }

    @Override
    protected int getConfigRfPerTick() {
        return ShieldConfiguration.RECEIVEPERTICK.get() * 2;
    }
}
