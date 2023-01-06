package mcjty.rftoolsbuilder.modules.mover;

import mcjty.lib.datagen.BaseBlockStateProvider;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverControlBlock;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;

import static mcjty.lib.datagen.BaseBlockStateProvider.RFTOOLSBASE_SIDE;
import static mcjty.lib.datagen.BaseBlockStateProvider.RFTOOLSBASE_TOP;
import static net.minecraftforge.client.model.generators.ModelProvider.BLOCK_FOLDER;

public class DataGenHelper {

    public static void create24Model(BaseBlockStateProvider provider, Block block, String modelPrefix, String txt) {
        ModelFile model0 = controlModuleBlock(provider, modelPrefix + "0", provider.modLoc(txt), 0, ModelBuilder.FaceRotation.ZERO);
        ModelFile model90 = controlModuleBlock(provider, modelPrefix + "90", provider.modLoc(txt), 0, ModelBuilder.FaceRotation.CLOCKWISE_90);
        ModelFile model180 = controlModuleBlock(provider, modelPrefix + "180", provider.modLoc(txt), 0, ModelBuilder.FaceRotation.UPSIDE_DOWN);
        ModelFile model270 = controlModuleBlock(provider, modelPrefix + "270", provider.modLoc(txt), 0, ModelBuilder.FaceRotation.COUNTERCLOCKWISE_90);
        provider.orientedBlock(block, (blockState, bld) -> {
            ModelFile model = getModelOriented24(model0, model90, model180, model270, blockState);
            bld.modelFile(model);
        });
    }

    private static ModelFile getModelOriented24(ModelFile model0, ModelFile model90, ModelFile model180, ModelFile model270, BlockState blockState) {
        return switch (blockState.getValue(BlockStateProperties.FACING)) {
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
    }

    public static ModelFile controlModuleBlock(BaseBlockStateProvider provider, String modelName, ResourceLocation texture, int offset, ModelBuilder.FaceRotation faceRotation) {
        BlockModelBuilder model = provider.models().getBuilder(BLOCK_FOLDER + "/" + modelName)
                .parent(provider.models().getExistingFile(provider.mcLoc("block")));
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
