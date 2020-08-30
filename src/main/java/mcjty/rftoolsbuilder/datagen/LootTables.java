package mcjty.rftoolsbuilder.datagen;

import mcjty.lib.datagen.BaseLootTableProvider;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import net.minecraft.data.DataGenerator;

public class LootTables extends BaseLootTableProvider {

    public LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        lootTables.put(BuilderModule.BUILDER.get(), createStandardTable("builder", BuilderModule.BUILDER.get()));
        lootTables.put(ShieldModule.SHIELD_BLOCK1.get(), createStandardTable("shield_block1", ShieldModule.SHIELD_BLOCK1.get()));
        lootTables.put(ShieldModule.SHIELD_BLOCK2.get(), createStandardTable("shield_block1", ShieldModule.SHIELD_BLOCK2.get()));
        lootTables.put(ShieldModule.SHIELD_BLOCK3.get(), createStandardTable("shield_block1", ShieldModule.SHIELD_BLOCK3.get()));
        lootTables.put(ShieldModule.SHIELD_BLOCK4.get(), createStandardTable("shield_block1", ShieldModule.SHIELD_BLOCK4.get()));
        lootTables.put(ShieldModule.TEMPLATE_YELLOW.get(), createSimpleTable("template_yellow", ShieldModule.TEMPLATE_YELLOW.get()));
        lootTables.put(ShieldModule.TEMPLATE_GREEN.get(), createSimpleTable("template_green", ShieldModule.TEMPLATE_GREEN.get()));
        lootTables.put(ShieldModule.TEMPLATE_RED.get(), createSimpleTable("template_red", ShieldModule.TEMPLATE_RED.get()));
        lootTables.put(ShieldModule.TEMPLATE_BLUE.get(), createSimpleTable("template_blue", ShieldModule.TEMPLATE_BLUE.get()));
    }

    @Override
    public String getName() {
        return "RFToolsBuilder LootTables";
    }
}
