package mcjty.rftoolsbuilder.datagen;

import mcjty.lib.datagen.BaseBlockStateProvider;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderSetup;
import mcjty.rftoolsbuilder.modules.shield.ShieldSetup;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ModelFile;

public class BlockStates extends BaseBlockStateProvider {

    public BlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, RFToolsBuilder.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        singleTextureBlock(ShieldSetup.TEMPLATE_BLUE, "blue_shield_template", "block/shieldtemplate");
        singleTextureBlock(ShieldSetup.TEMPLATE_RED, "red_shield_template", "block/shieldtemplate1");
        singleTextureBlock(ShieldSetup.TEMPLATE_GREEN, "green_shield_template", "block/shieldtemplate2");
        singleTextureBlock(ShieldSetup.TEMPLATE_YELLOW, "yellow_shield_template", "block/shieldtemplate3");

        horizontalOrientedBlock(BuilderSetup.BUILDER, frontBasedModel("builder", modLoc("block/machinebuilder")));

        ModelFile shieldModel = cubeAll("shield_block", modLoc("block/machineshieldprojector"));
        simpleBlock(ShieldSetup.SHIELD_BLOCK1, shieldModel);
        simpleBlock(ShieldSetup.SHIELD_BLOCK2, shieldModel);
        simpleBlock(ShieldSetup.SHIELD_BLOCK3, shieldModel);
        simpleBlock(ShieldSetup.SHIELD_BLOCK4, shieldModel);
    }
}
