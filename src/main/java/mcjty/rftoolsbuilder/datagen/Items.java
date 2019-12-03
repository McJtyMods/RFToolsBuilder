package mcjty.rftoolsbuilder.datagen;

import mcjty.rftoolsbuilder.RFToolsBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;

public class Items extends ItemModelProvider {

    public Items(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, RFToolsBuilder.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        getBuilder("blue_shield_template_block")
                .parent(new ModelFile.UncheckedModelFile(modLoc("block/blue_shield_template")));
    }

    @Override
    public String getName() {
        return "RFTools Builder Item Models";
    }
}
