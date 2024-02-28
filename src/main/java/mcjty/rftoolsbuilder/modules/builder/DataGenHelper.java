package mcjty.rftoolsbuilder.modules.builder;

import mcjty.lib.datagen.BaseBlockStateProvider;
import mcjty.rftoolsbuilder.modules.builder.blocks.SupportBlock;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;

public class DataGenHelper {

    public static void generateSupportModel(BaseBlockStateProvider provider) {
        BlockModelBuilder support0 = provider.models().cubeAll("supportblock_status0", provider.modLoc("block/supportblock")).renderType("translucent");
        BlockModelBuilder support1 = provider.models().cubeAll("supportblock_status1", provider.modLoc("block/supportyellowblock")).renderType("translucent");
        BlockModelBuilder support2 = provider.models().cubeAll("supportblock_status2", provider.modLoc("block/supportredblock")).renderType("translucent");
        VariantBlockStateBuilder builder = provider.getVariantBuilder(BuilderModule.SUPPORT.get());
        builder.partialState().with(SupportBlock.STATUS, SupportBlock.SupportStatus.STATUS_OK)
                .modelForState().modelFile(support0)
                .addModel();
        builder.partialState().with(SupportBlock.STATUS, SupportBlock.SupportStatus.STATUS_WARN)
                .modelForState().modelFile(support1)
                .addModel();
        builder.partialState().with(SupportBlock.STATUS, SupportBlock.SupportStatus.STATUS_ERROR)
                .modelForState().modelFile(support2)
                .addModel();

    }
}
