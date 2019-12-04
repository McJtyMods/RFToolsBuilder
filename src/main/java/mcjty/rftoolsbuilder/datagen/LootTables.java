package mcjty.rftoolsbuilder.datagen;

import mcjty.lib.datagen.BaseLootTableProvider;
import mcjty.rftoolsbuilder.modules.builder.BuilderSetup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.LootTableProvider;

public class LootTables extends BaseLootTableProvider {

    public LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        lootTables.put(BuilderSetup.BUILDER.get(), createStandardTable("builder", BuilderSetup.BUILDER.get()));
    }

    @Override
    public String getName() {
        return "RFToolsBuilder LootTables";
    }
}
