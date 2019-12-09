package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.rftoolsbuilder.modules.shield.ShieldConfiguration;

import static mcjty.rftoolsbuilder.modules.shield.ShieldSetup.TYPE_SHIELD_BLOCK3;

public class ShieldTileEntity3 extends ShieldProjectorTileEntity {

    public ShieldTileEntity3() {
        super(TYPE_SHIELD_BLOCK3.get(), ShieldConfiguration.maxShieldSize.get() * 16, ShieldConfiguration.MAXENERGY.get() * 3, ShieldConfiguration.RECEIVEPERTICK.get() * 2);
        setDamageFactor(4.0f);
        setCostFactor(2.0f);
    }
}
