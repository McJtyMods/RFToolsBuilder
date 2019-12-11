package mcjty.rftoolsbuilder.datagen;

import mcjty.lib.datagen.BaseItemModelProvider;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderSetup;
import mcjty.rftoolsbuilder.modules.shield.ShieldSetup;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

public class Items extends BaseItemModelProvider {

    public Items(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, RFToolsBuilder.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        parentedBlock(ShieldSetup.TEMPLATE_BLUE.get(), "block/blue_shield_template");
        parentedBlock(ShieldSetup.TEMPLATE_RED.get(), "block/red_shield_template");
        parentedBlock(ShieldSetup.TEMPLATE_YELLOW.get(), "block/yellow_shield_template");
        parentedBlock(ShieldSetup.TEMPLATE_GREEN.get(), "block/green_shield_template");

        parentedBlock(ShieldSetup.SHIELD_BLOCK1.get(), "block/shield_block1");
        parentedBlock(ShieldSetup.SHIELD_BLOCK2.get(), "block/shield_block2");
        parentedBlock(ShieldSetup.SHIELD_BLOCK3.get(), "block/shield_block3");
        parentedBlock(ShieldSetup.SHIELD_BLOCK4.get(), "block/shield_block4");

        parentedBlock(BuilderSetup.BUILDER.get(),"block/builder");

        itemGenerated(BuilderSetup.SHAPE_CARD_DEF.get(), "item/shapecarditem");
        itemGenerated(BuilderSetup.SHAPE_CARD_LIQUID.get(), "item/shapecardliquiditem");
        itemGenerated(BuilderSetup.SHAPE_CARD_PUMP.get(), "item/shapecardpumpitem");
        itemGenerated(BuilderSetup.SHAPE_CARD_PUMP_CLEAR.get(), "item/shapecardpumpclearitem");
        itemGenerated(BuilderSetup.SHAPE_CARD_QUARRY.get(), "item/shapecardquarryitem");
        itemGenerated(BuilderSetup.SHAPE_CARD_QUARRY_CLEAR.get(), "item/shapecardcquarryitem");
        itemGenerated(BuilderSetup.SHAPE_CARD_QUARRY_CLEAR_FORTUNE.get(), "item/shapecardcfortuneitem");
        itemGenerated(BuilderSetup.SHAPE_CARD_QUARRY_CLEAR_SILK.get(), "item/shapecardcsilkitem");
        itemGenerated(BuilderSetup.SHAPE_CARD_QUARRY_FORTUNE.get(), "item/shapecardfortuneitem");
        itemGenerated(BuilderSetup.SHAPE_CARD_QUARRY_SILK.get(), "item/shapecardsilkitem");
        itemGenerated(BuilderSetup.SHAPE_CARD_VOID.get(), "item/shapecardvoiditem");
    }

    @Override
    public String getName() {
        return "RFTools Builder Item Models";
    }
}
