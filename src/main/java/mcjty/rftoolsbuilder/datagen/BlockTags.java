package mcjty.rftoolsbuilder.datagen;

import mcjty.lib.datagen.BaseBlockTagsProvider;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

public class BlockTags extends BaseBlockTagsProvider {

    public BlockTags(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, RFToolsBuilder.MODID, helper);
    }

    @Override
    protected void addTags() {
        ironPickaxe(
                MoverModule.MOVER, MoverModule.VEHICLE_BUILDER,
                BuilderModule.BUILDER, BuilderModule.SPACE_CHAMBER, BuilderModule.SPACE_CHAMBER_CONTROLLER, BuilderModule.SUPPORT,
                ShieldModule.SHIELD_BLOCK1, ShieldModule.SHIELD_BLOCK2, ShieldModule.SHIELD_BLOCK3, ShieldModule.SHIELD_BLOCK4,
                ShieldModule.SHIELDING_CUTOUT, ShieldModule.SHIELDING_SOLID, ShieldModule.SHIELDING_TRANSLUCENT
        );
    }

    @Override
    @Nonnull
    public String getName() {
        return "RFToolsBuilder Tags";
    }
}
