package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.rftoolsbuilder.modules.shield.ShieldConfiguration;

import static mcjty.rftoolsbuilder.modules.shield.ShieldSetup.TYPE_SHIELD_BLOCK4;

public class ShieldTileEntity4 extends ShieldProjectorTileEntity {

    public ShieldTileEntity4() {
        super(TYPE_SHIELD_BLOCK4.get(), ShieldConfiguration.maxShieldSize.get() * 128, ShieldConfiguration.MAXENERGY.get() * 6, ShieldConfiguration.RECEIVEPERTICK.get() * 6);
        setDamageFactor(4.0f);
        setCostFactor(2.0f);
    }
}
