package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.rftoolsbuilder.modules.shield.ShieldConfiguration;

import static mcjty.rftoolsbuilder.modules.shield.ShieldSetup.TYPE_SHIELD_BLOCK2;

public class ShieldTileEntity2 extends ShieldProjectorTileEntity {

    public ShieldTileEntity2() {
        super(TYPE_SHIELD_BLOCK2.get(), ShieldConfiguration.maxShieldSize.get() * 4, ShieldConfiguration.MAXENERGY.get(), ShieldConfiguration.RECEIVEPERTICK.get());
    }
}
