package mcjty.rftoolsbuilder.datagen;

import mcjty.lib.datagen.BaseLootTableProvider;
import mcjty.rftoolsbuilder.modules.builder.BuilderSetup;
import mcjty.rftoolsbuilder.modules.shield.ShieldSetup;
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
        lootTables.put(ShieldSetup.SHIELD_BLOCK1.get(), createStandardTable("shield_block1", ShieldSetup.SHIELD_BLOCK1.get()));
        lootTables.put(ShieldSetup.SHIELD_BLOCK2.get(), createStandardTable("shield_block1", ShieldSetup.SHIELD_BLOCK2.get()));
        lootTables.put(ShieldSetup.SHIELD_BLOCK3.get(), createStandardTable("shield_block1", ShieldSetup.SHIELD_BLOCK3.get()));
        lootTables.put(ShieldSetup.SHIELD_BLOCK4.get(), createStandardTable("shield_block1", ShieldSetup.SHIELD_BLOCK4.get()));
        lootTables.put(ShieldSetup.TEMPLATE_YELLOW.get(), createSimpleTable("template_yellow", ShieldSetup.TEMPLATE_YELLOW.get()));
        lootTables.put(ShieldSetup.TEMPLATE_GREEN.get(), createSimpleTable("template_green", ShieldSetup.TEMPLATE_GREEN.get()));
        lootTables.put(ShieldSetup.TEMPLATE_RED.get(), createSimpleTable("template_red", ShieldSetup.TEMPLATE_RED.get()));
        lootTables.put(ShieldSetup.TEMPLATE_BLUE.get(), createSimpleTable("template_blue", ShieldSetup.TEMPLATE_BLUE.get()));
    }

    @Override
    public String getName() {
        return "RFToolsBuilder LootTables";
    }
}
