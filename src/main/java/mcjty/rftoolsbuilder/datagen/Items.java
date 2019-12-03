package mcjty.rftoolsbuilder.datagen;

import mcjty.lib.datagen.BaseItemModelProvider;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderSetup;
import mcjty.rftoolsbuilder.modules.shield.ShieldSetup;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

public class Items extends BaseItemModelProvider {

    public Items(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, RFToolsBuilder.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        parentedBlock(ShieldSetup.TEMPLATE_BLUE, "block/blue_shield_template");
        parentedBlock(ShieldSetup.TEMPLATE_RED, "block/red_shield_template");
        parentedBlock(ShieldSetup.TEMPLATE_YELLOW, "block/yellow_shield_template");
        parentedBlock(ShieldSetup.TEMPLATE_GREEN, "block/green_shield_template");

        parentedBlock(ShieldSetup.SHIELD_BLOCK1, "block/shield_block1");
        parentedBlock(ShieldSetup.SHIELD_BLOCK2, "block/shield_block2");
        parentedBlock(ShieldSetup.SHIELD_BLOCK3, "block/shield_block3");
        parentedBlock(ShieldSetup.SHIELD_BLOCK4, "block/shield_block4");

        parentedBlock(BuilderSetup.BUILDER,"block/builder");
    }

    @Override
    public String getName() {
        return "RFTools Builder Item Models";
    }
}
