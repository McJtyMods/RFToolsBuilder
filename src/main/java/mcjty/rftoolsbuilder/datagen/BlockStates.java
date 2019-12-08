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
        singleTextureBlock(ShieldSetup.TEMPLATE_BLUE.get(), "blue_shield_template", "block/shieldtemplate");
        singleTextureBlock(ShieldSetup.TEMPLATE_RED.get(), "red_shield_template", "block/shieldtemplate1");
        singleTextureBlock(ShieldSetup.TEMPLATE_GREEN.get(), "green_shield_template", "block/shieldtemplate2");
        singleTextureBlock(ShieldSetup.TEMPLATE_YELLOW.get(), "yellow_shield_template", "block/shieldtemplate3");

        horizontalOrientedBlock(BuilderSetup.BUILDER.get(), frontBasedModel("builder", modLoc("block/machinebuilder")));

        ModelFile shieldModel = cubeAll("shield_block", modLoc("block/machineshieldprojector"));
        simpleBlock(ShieldSetup.SHIELD_BLOCK1.get(), shieldModel);
        simpleBlock(ShieldSetup.SHIELD_BLOCK2.get(), shieldModel);
        simpleBlock(ShieldSetup.SHIELD_BLOCK3.get(), shieldModel);
        simpleBlock(ShieldSetup.SHIELD_BLOCK4.get(), shieldModel);

//        ModelFile.UncheckedModelFile emptyModel = new ModelFile.UncheckedModelFile(new ResourceLocation("rftoolsbase", "block/empty_model"));
//        simpleBlock(ShieldSetup.SHIELD_INVISIBLE.get(), emptyModel);
//        simpleBlock(ShieldSetup.SHIELD_INVISIBLE_NOTICK.get(), emptyModel);
//        simpleBlock(ShieldSetup.SHIELD_INVISIBLE_OPAQUE.get(), emptyModel);
//        simpleBlock(ShieldSetup.SHIELD_INVISIBLE_OPAQUE_NOTICK.get(), emptyModel);
//        simpleBlock(ShieldSetup.SHIELD_SOLID.get(), emptyModel);
//        simpleBlock(ShieldSetup.SHIELD_SOLID_NOTICK.get(), emptyModel);
//        simpleBlock(ShieldSetup.SHIELD_SOLID_OPAQUE.get(), emptyModel);
//        simpleBlock(ShieldSetup.SHIELD_SOLID_OPAQUE_NOTICK.get(), emptyModel);
//        simpleBlock(ShieldSetup.SHIELD_CAMO.get(), emptyModel);
//        simpleBlock(ShieldSetup.SHIELD_CAMO_NOTICK.get(), emptyModel);
//        simpleBlock(ShieldSetup.SHIELD_CAMO_OPAQUE.get(), emptyModel);
//        simpleBlock(ShieldSetup.SHIELD_CAMO_OPAQUE_NOTICK.get(), emptyModel);

    }
}
