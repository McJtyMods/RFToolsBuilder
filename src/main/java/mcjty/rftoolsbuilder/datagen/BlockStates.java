package mcjty.rftoolsbuilder.datagen;

import mcjty.lib.datagen.BaseBlockStateProvider;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.SupportBlock;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverControlBlock;
import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
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

        ModelFile model0 = controlModuleBlock("mover_control_0", modLoc("block/movercontrol"), 0, ModelBuilder.FaceRotation.ZERO);
        ModelFile model90 = controlModuleBlock("mover_control_90", modLoc("block/movercontrol"), 0, ModelBuilder.FaceRotation.CLOCKWISE_90);
        ModelFile model180 = controlModuleBlock("mover_control_180", modLoc("block/movercontrol"), 0, ModelBuilder.FaceRotation.UPSIDE_DOWN);
        ModelFile model270 = controlModuleBlock("mover_control_270", modLoc("block/movercontrol"), 0, ModelBuilder.FaceRotation.COUNTERCLOCKWISE_90);
        orientedBlock(MoverModule.MOVER_CONTROL_BLOCK.get(), (blockState, bld) -> {
            ModelFile model = getModelOriented24(model0, model90, model180, model270, blockState);
            bld.modelFile(model);
        });

        ModelFile smodel0 = controlModuleBlock("mover_status_0", modLoc("block/moverstatus"), 0, ModelBuilder.FaceRotation.ZERO);
        ModelFile smodel90 = controlModuleBlock("mover_status_90", modLoc("block/moverstatus"), 0, ModelBuilder.FaceRotation.CLOCKWISE_90);
        ModelFile smodel180 = controlModuleBlock("mover_status_180", modLoc("block/moverstatus"), 0, ModelBuilder.FaceRotation.UPSIDE_DOWN);
        ModelFile smodel270 = controlModuleBlock("mover_status_270", modLoc("block/moverstatus"), 0, ModelBuilder.FaceRotation.COUNTERCLOCKWISE_90);
        orientedBlock(MoverModule.MOVER_STATUS_BLOCK.get(), (blockState, bld) -> {
            ModelFile model = getModelOriented24(smodel0, smodel90, smodel180, smodel270, blockState);
            bld.modelFile(model);
        });

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

    private ModelFile getModelOriented24(ModelFile model0, ModelFile model90, ModelFile model180, ModelFile model270, BlockState blockState) {
        ModelFile model = switch (blockState.getValue(BlockStateProperties.FACING)) {
            case UP -> switch (blockState.getValue(MoverControlBlock.HORIZ_FACING)) {
                case DOWN -> model0;
                case UP -> model0;
                case NORTH -> model0;
                case SOUTH -> model180;
                case WEST -> model270;
                case EAST -> model90;
            };
            case DOWN -> switch (blockState.getValue(MoverControlBlock.HORIZ_FACING)) {
                case DOWN -> model0;
                case UP -> model0;
                case NORTH -> model180;
                case SOUTH -> model0;
                case WEST -> model90;
                case EAST -> model270;
            };
            case NORTH -> model0;
            case SOUTH -> model0;
            case WEST -> model0;
            case EAST -> model0;
        };
        return model;
    }

    public ModelFile controlModuleBlock(String modelName, ResourceLocation texture, int offset, ModelBuilder.FaceRotation faceRotation) {
        BlockModelBuilder model = models().getBuilder(BLOCK_FOLDER + "/" + modelName)
                .parent(models().getExistingFile(mcLoc("block")));
        model.element().from(0, 0, offset).to(16, 16, 16)
                .face(Direction.DOWN).cullface(Direction.DOWN).texture("#side").end()
                .face(Direction.UP).cullface(Direction.UP).texture("#top").end()
                .face(Direction.EAST).cullface(Direction.EAST).texture("#side").end()
                .face(Direction.WEST).cullface(Direction.WEST).texture("#side").end()
                .face(Direction.NORTH).texture("#front").rotation(faceRotation).end()
                .face(Direction.SOUTH).cullface(Direction.SOUTH).texture("#side").end()
                .end()
                .texture("side", RFTOOLSBASE_SIDE)
                .texture("top", RFTOOLSBASE_TOP)
                .texture("particle", RFTOOLSBASE_SIDE)
                .texture("front", texture);
        return model;
    }

}
