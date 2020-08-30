package mcjty.rftoolsbuilder.datagen;

import mcjty.lib.datagen.BaseItemModelProvider;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

public class Items extends BaseItemModelProvider {

    public Items(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, RFToolsBuilder.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        parentedBlock(ShieldModule.TEMPLATE_BLUE.get(), "block/blue_shield_template");
        parentedBlock(ShieldModule.TEMPLATE_RED.get(), "block/red_shield_template");
        parentedBlock(ShieldModule.TEMPLATE_YELLOW.get(), "block/yellow_shield_template");
        parentedBlock(ShieldModule.TEMPLATE_GREEN.get(), "block/green_shield_template");

        parentedBlock(ShieldModule.SHIELD_BLOCK1.get(), "block/shield_block");
        parentedBlock(ShieldModule.SHIELD_BLOCK2.get(), "block/shield_block");
        parentedBlock(ShieldModule.SHIELD_BLOCK3.get(), "block/shield_block");
        parentedBlock(ShieldModule.SHIELD_BLOCK4.get(), "block/shield_block");

        parentedBlock(BuilderModule.BUILDER.get(),"block/builder");

        itemGenerated(BuilderModule.SHAPE_CARD_DEF.get(), "item/shapecarditem");
        itemGenerated(BuilderModule.SHAPE_CARD_LIQUID.get(), "item/shapecardliquiditem");
        itemGenerated(BuilderModule.SHAPE_CARD_PUMP.get(), "item/shapecardpumpitem");
        itemGenerated(BuilderModule.SHAPE_CARD_PUMP_CLEAR.get(), "item/shapecardpumpclearitem");
        itemGenerated(BuilderModule.SHAPE_CARD_QUARRY.get(), "item/shapecardquarryitem");
        itemGenerated(BuilderModule.SHAPE_CARD_QUARRY_CLEAR.get(), "item/shapecardcquarryitem");
        itemGenerated(BuilderModule.SHAPE_CARD_QUARRY_CLEAR_FORTUNE.get(), "item/shapecardcfortuneitem");
        itemGenerated(BuilderModule.SHAPE_CARD_QUARRY_CLEAR_SILK.get(), "item/shapecardcsilkitem");
        itemGenerated(BuilderModule.SHAPE_CARD_QUARRY_FORTUNE.get(), "item/shapecardfortuneitem");
        itemGenerated(BuilderModule.SHAPE_CARD_QUARRY_SILK.get(), "item/shapecardsilkitem");
        itemGenerated(BuilderModule.SHAPE_CARD_VOID.get(), "item/shapecardvoiditem");
    }

    @Override
    public String getName() {
        return "RFTools Builder Item Models";
    }
}
