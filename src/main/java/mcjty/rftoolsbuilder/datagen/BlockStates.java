package mcjty.rftoolsbuilder.datagen;

import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.shield.ShieldSetup;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ModelFile;

public class BlockStates extends BlockStateProvider {

    public BlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, RFToolsBuilder.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile blueTemplateModel = cubeAll("blue_shield_template", modLoc("block/shieldtemplate"));
        simpleBlock(ShieldSetup.TEMPLATE_BLUE, blueTemplateModel);
    }
}
