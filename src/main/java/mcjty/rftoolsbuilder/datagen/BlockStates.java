package mcjty.rftoolsbuilder.datagen;

import mcjty.lib.datagen.BaseBlockStateProvider;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderSetup;
import mcjty.rftoolsbuilder.modules.builder.blocks.SupportBlock;
import mcjty.rftoolsbuilder.modules.shield.ShieldSetup;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;

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

        BlockModelBuilder support0 = models().cubeAll("supportblock_status0", modLoc("block/supportblock"));
        BlockModelBuilder support1 = models().cubeAll("supportblock_status1", modLoc("block/supportyellowblock"));
        BlockModelBuilder support2 = models().cubeAll("supportblock_status2", modLoc("block/supportredblock"));
        VariantBlockStateBuilder builder = getVariantBuilder(BuilderSetup.SUPPORT.get());
        builder.partialState().with(SupportBlock.STATUS, SupportBlock.STATUS_OK)
                .modelForState().modelFile(support0)
                .addModel();
        builder.partialState().with(SupportBlock.STATUS, SupportBlock.STATUS_WARN)
                .modelForState().modelFile(support1)
                .addModel();
        builder.partialState().with(SupportBlock.STATUS, SupportBlock.STATUS_ERROR)
                .modelForState().modelFile(support2)
                .addModel();

        ModelFile shieldModel = models().cubeAll("shield_block", modLoc("block/machineshieldprojector"));
        simpleBlock(ShieldSetup.SHIELD_BLOCK1.get(), shieldModel);
        simpleBlock(ShieldSetup.SHIELD_BLOCK2.get(), shieldModel);
        simpleBlock(ShieldSetup.SHIELD_BLOCK3.get(), shieldModel);
        simpleBlock(ShieldSetup.SHIELD_BLOCK4.get(), shieldModel);

        ModelFile.UncheckedModelFile emptyModel = new ModelFile.UncheckedModelFile(new ResourceLocation("rftoolsbase", "block/empty_model"));
        simpleBlock(ShieldSetup.SHIELDING_SOLID.get(), emptyModel);
        simpleBlock(ShieldSetup.SHIELDING_TRANSLUCENT.get(), emptyModel);
        simpleBlock(ShieldSetup.SHIELDING_CUTOUT.get(), emptyModel);
    }
}
