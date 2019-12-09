package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.rftoolsbuilder.modules.shield.ShieldConfiguration;

import static mcjty.rftoolsbuilder.modules.shield.ShieldSetup.TYPE_SHIELD_BLOCK1;

public class ShieldTileEntity extends ShieldProjectorTileEntity {

    public ShieldTileEntity() {
        super(TYPE_SHIELD_BLOCK1.get(), ShieldConfiguration.maxShieldSize.get(), ShieldConfiguration.MAXENERGY.get(), ShieldConfiguration.RECEIVEPERTICK.get());
    }
}
