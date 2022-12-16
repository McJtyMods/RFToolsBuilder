package mcjty.rftoolsbuilder.datagen;

import mcjty.lib.datagen.BaseBlockStateProvider;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.SupportBlock;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import static net.minecraftforge.client.model.generators.ModelProvider.BLOCK_FOLDER;

public class BlockStates extends BaseBlockStateProvider {

    public BlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, RFToolsBuilder.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        singleTextureBlockC(ShieldModule.TEMPLATE_BLUE.get(), "blue_shield_template", "block/shieldtemplate", builder -> builder.renderType("cutout"));
        singleTextureBlockC(ShieldModule.TEMPLATE_RED.get(), "red_shield_template", "block/shieldtemplate1", builder -> builder.renderType("cutout"));
        singleTextureBlockC(ShieldModule.TEMPLATE_GREEN.get(), "green_shield_template", "block/shieldtemplate2", builder -> builder.renderType("cutout"));
        singleTextureBlockC(ShieldModule.TEMPLATE_YELLOW.get(), "yellow_shield_template", "block/shieldtemplate3", builder -> builder.renderType("cutout"));

        horizontalOrientedBlock(BuilderModule.BUILDER.get(), frontBasedModel("builder", modLoc("block/machinebuilder")));
        singleTextureBlock(BuilderModule.SPACE_CHAMBER.get(), "space_chamber", "block/machinespacechamber");
        singleTextureBlock(BuilderModule.SPACE_CHAMBER_CONTROLLER.get(), "space_chamber_controller", "block/machinespacechambercontroller");

        orientedBlock(MoverModule.MOVER.get(), frontBasedModel("mover", modLoc("block/machinemover")));
        orientedBlock(MoverModule.MOVER_CONTROLLER.get(), frontBasedModel("mover_controller", modLoc("block/machinemovercontroller")));
        orientedBlock(MoverModule.VEHICLE_BUILDER.get(), frontBasedModel("vehicle_builder", modLoc("block/machinevehiclebuilder")));

        ModelFile model = controlModuleBlock("mover_control", modLoc("block/movercontrol"), 0);
        orientedBlock(MoverModule.MOVER_CONTROL_BLOCK.get(), model);

        BlockModelBuilder support0 = models().cubeAll("supportblock_status0", modLoc("block/supportblock")).renderType("translucent");
        BlockModelBuilder support1 = models().cubeAll("supportblock_status1", modLoc("block/supportyellowblock")).renderType("translucent");
        BlockModelBuilder support2 = models().cubeAll("supportblock_status2", modLoc("block/supportredblock")).renderType("translucent");
        VariantBlockStateBuilder builder = getVariantBuilder(BuilderModule.SUPPORT.get());
        builder.partialState().with(SupportBlock.STATUS, SupportBlock.SupportStatus.STATUS_OK)
                .modelForState().modelFile(support0)
                .addModel();
        builder.partialState().with(SupportBlock.STATUS, SupportBlock.SupportStatus.STATUS_WARN)
                .modelForState().modelFile(support1)
                .addModel();
        builder.partialState().with(SupportBlock.STATUS, SupportBlock.SupportStatus.STATUS_ERROR)
                .modelForState().modelFile(support2)
                .addModel();

        ModelFile shieldModel = models().cubeAll("shield_block", modLoc("block/machineshieldprojector"));
        simpleBlock(ShieldModule.SHIELD_BLOCK1.get(), shieldModel);
        simpleBlock(ShieldModule.SHIELD_BLOCK2.get(), shieldModel);
        simpleBlock(ShieldModule.SHIELD_BLOCK3.get(), shieldModel);
        simpleBlock(ShieldModule.SHIELD_BLOCK4.get(), shieldModel);

//        ModelFile.UncheckedModelFile emptyModel = new ModelFile.UncheckedModelFile(new ResourceLocation("rftoolsbase", "block/empty_model"));
        ModelFile.UncheckedModelFile shieldingModel = new ModelFile.UncheckedModelFile(new ResourceLocation(RFToolsBuilder.MODID, "block/shielding"));
        simpleBlock(ShieldModule.SHIELDING_SOLID.get(), shieldingModel);
        simpleBlock(ShieldModule.SHIELDING_TRANSLUCENT.get(), shieldingModel);
        simpleBlock(ShieldModule.SHIELDING_CUTOUT.get(), shieldingModel);
    }

    public ModelFile controlModuleBlock(String modelName, ResourceLocation texture, int offset) {
        BlockModelBuilder model = models().getBuilder(BLOCK_FOLDER + "/" + modelName)
                .parent(models().getExistingFile(mcLoc("block")));
        model.element().from(0, 0, offset).to(16, 16, 16)
                .face(Direction.DOWN).cullface(Direction.DOWN).texture("#side").end()
                .face(Direction.UP).cullface(Direction.UP).texture("#top").end()
                .face(Direction.EAST).cullface(Direction.EAST).texture("#side").end()
                .face(Direction.WEST).cullface(Direction.WEST).texture("#side").end()
                .face(Direction.NORTH).texture("#front").end()
                .face(Direction.SOUTH).cullface(Direction.SOUTH).texture("#side").end()
                .end()
                .texture("side", RFTOOLSBASE_SIDE)
                .texture("top", RFTOOLSBASE_TOP)
                .texture("particle", RFTOOLSBASE_SIDE)
                .texture("front", texture);
        return model;
    }

}
