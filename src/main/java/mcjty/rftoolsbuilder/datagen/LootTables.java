package mcjty.rftoolsbuilder.datagen;

import mcjty.lib.datagen.BaseLootTableProvider;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import net.minecraft.data.DataGenerator;

import javax.annotation.Nonnull;

public class LootTables extends BaseLootTableProvider {

    public LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        lootTables.put(BuilderModule.BUILDER.get(), createStandardTable("builder", BuilderModule.BUILDER.get(), BuilderModule.TYPE_BUILDER.get()));
        lootTables.put(BuilderModule.SPACE_CHAMBER_CONTROLLER.get(), createStandardTable("space_chamber_controller", BuilderModule.SPACE_CHAMBER_CONTROLLER.get(), BuilderModule.TYPE_SPACE_CHAMBER_CONTROLLER.get()));
        lootTables.put(ShieldModule.SHIELD_BLOCK1.get(), createStandardTable("shield_block1", ShieldModule.SHIELD_BLOCK1.get(), ShieldModule.TYPE_SHIELD_BLOCK1.get()));
        lootTables.put(ShieldModule.SHIELD_BLOCK2.get(), createStandardTable("shield_block1", ShieldModule.SHIELD_BLOCK2.get(), ShieldModule.TYPE_SHIELD_BLOCK2.get()));
        lootTables.put(ShieldModule.SHIELD_BLOCK3.get(), createStandardTable("shield_block1", ShieldModule.SHIELD_BLOCK3.get(), ShieldModule.TYPE_SHIELD_BLOCK3.get()));
        lootTables.put(ShieldModule.SHIELD_BLOCK4.get(), createStandardTable("shield_block1", ShieldModule.SHIELD_BLOCK4.get(), ShieldModule.TYPE_SHIELD_BLOCK4.get()));
        lootTables.put(ShieldModule.TEMPLATE_YELLOW.get(), createSimpleTable("template_yellow", ShieldModule.TEMPLATE_YELLOW.get()));
        lootTables.put(ShieldModule.TEMPLATE_GREEN.get(), createSimpleTable("template_green", ShieldModule.TEMPLATE_GREEN.get()));
        lootTables.put(ShieldModule.TEMPLATE_RED.get(), createSimpleTable("template_red", ShieldModule.TEMPLATE_RED.get()));
        lootTables.put(ShieldModule.TEMPLATE_BLUE.get(), createSimpleTable("template_blue", ShieldModule.TEMPLATE_BLUE.get()));
        lootTables.put(BuilderModule.SPACE_CHAMBER.get(), createSimpleTable("space_chamber", BuilderModule.SPACE_CHAMBER.get()));
        lootTables.put(MoverModule.MOVER.get(), createStandardTable("mover", MoverModule.MOVER.get(), MoverModule.TYPE_MOVER.get()));
        lootTables.put(MoverModule.MOVER_CONTROLLER.get(), createStandardTable("mover_controller", MoverModule.MOVER_CONTROLLER.get(), MoverModule.TYPE_MOVER_CONTROLLER.get()));
        lootTables.put(MoverModule.VEHICLE_BUILDER.get(), createStandardTable("vehicle_builder", MoverModule.VEHICLE_BUILDER.get(), MoverModule.TYPE_VEHICLE_BUILDER.get()));
        lootTables.put(MoverModule.PLACEHOLDER_MOVER_CONTROL_BLOCK.get(), createSimpleTable("placeholder_mover_control", MoverModule.PLACEHOLDER_MOVER_CONTROL_BLOCK.get()));
    }

    @Nonnull
    @Override
    public String getName() {
        return "RFToolsBuilder LootTables";
    }
}
