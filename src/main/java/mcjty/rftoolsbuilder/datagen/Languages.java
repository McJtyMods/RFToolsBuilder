package mcjty.rftoolsbuilder.datagen;

import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderSetup;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class Languages extends LanguageProvider {

    public Languages(DataGenerator gen, String locale) {
        super(gen, RFToolsBuilder.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add(BuilderSetup.BUILDER, "Builder");
    }
}
