package mcjty.rftoolsbuilder.datagen;

import mcjty.lib.crafting.CopyNBTRecipeBuilder;
import mcjty.lib.datagen.BaseRecipeProvider;
import mcjty.rftoolsbase.modules.various.VariousSetup;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderSetup;
import mcjty.rftoolsbuilder.modules.shield.ShieldSetup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class Recipes extends BaseRecipeProvider {

    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
        add('F', VariousSetup.MACHINE_FRAME.get());
        add('s', VariousSetup.DIMENSIONALSHARD.get());
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        build(consumer, ShapedRecipeBuilder.shapedRecipe(BuilderSetup.BUILDER.get())
                        .addCriterion("machine_frame", hasItem(VariousSetup.MACHINE_FRAME.get())),
                "BoB", "rFr", "BrB");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_DEF.get())
                        .addCriterion("iron_ingot", hasItem(Items.IRON_INGOT)),
                "pBp", "rir", "pBp");

        build(consumer, ShapedRecipeBuilder.shapedRecipe(ShieldSetup.SHIELD_BLOCK1.get())
                        .key('g', Tags.Items.INGOTS_GOLD)
                        .addCriterion("machine_frame", hasItem(VariousSetup.MACHINE_FRAME.get())),
                "gTg", "rFr", "OOO");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(ShieldSetup.SHIELD_BLOCK2.get())
                        .key('M', ShieldSetup.SHIELD_BLOCK1.get())
                        .addCriterion("machine_frame", hasItem(VariousSetup.MACHINE_FRAME.get())),
                "ROR", "OMO", "ROR");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(ShieldSetup.SHIELD_BLOCK3.get())
                        .key('M', ShieldSetup.SHIELD_BLOCK2.get())
                        .addCriterion("machine_frame", hasItem(VariousSetup.MACHINE_FRAME.get())),
                "sOs", "OMO", "sOs");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(ShieldSetup.SHIELD_BLOCK4.get())
                        .key('M', ShieldSetup.SHIELD_BLOCK3.get())
                        .key('n', Items.NETHER_STAR)
                        .addCriterion("machine_frame", hasItem(VariousSetup.MACHINE_FRAME.get())),
                "nOs", "OMO", "sOn");

        build(consumer, ShapedRecipeBuilder.shapedRecipe(ShieldSetup.TEMPLATE_BLUE.get())
                        .key('w', ItemTags.WOOL)
                        .key('l', Tags.Items.DYES_BLUE)
                        .addCriterion("glass", hasItem(Items.GLASS)),
                "www", "lGl", "www");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(ShieldSetup.TEMPLATE_RED.get())
                        .key('w', ItemTags.WOOL)
                        .key('l', Tags.Items.DYES_RED)
                        .addCriterion("glass", hasItem(Items.GLASS)),
                "www", "lGl", "www");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(ShieldSetup.TEMPLATE_YELLOW.get())
                        .key('w', ItemTags.WOOL)
                        .key('l', Tags.Items.DYES_YELLOW)
                        .addCriterion("glass", hasItem(Items.GLASS)),
                "www", "lGl", "www");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(ShieldSetup.TEMPLATE_GREEN.get())
                        .key('w', ItemTags.WOOL)
                        .key('l', Tags.Items.DYES_GREEN)
                        .addCriterion("glass", hasItem(Items.GLASS)),
                "www", "lGl", "www");

        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_PUMP.get())
                        .key('M', BuilderSetup.SHAPE_CARD_DEF.get())
                        .addCriterion("shape_card", hasItem(BuilderSetup.SHAPE_CARD_DEF.get())),
                "rWr", "bMb", "rLr");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_PUMP_CLEAR.get())
                        .key('M', BuilderSetup.SHAPE_CARD_PUMP.get())
                        .addCriterion("shape_card", hasItem(BuilderSetup.SHAPE_CARD_DEF.get())),
                "GGG", "GMG", "GGG");
        build(consumer, new ResourceLocation(RFToolsBuilder.MODID, "shape_card_pump_dirt"), CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_PUMP.get())
                        .key('M', BuilderSetup.SHAPE_CARD_PUMP_CLEAR.get())
                        .addCriterion("shape_card", hasItem(BuilderSetup.SHAPE_CARD_DEF.get())),
                "DDD", "DMD", "DDD");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_LIQUID.get())
                        .key('M', BuilderSetup.SHAPE_CARD_DEF.get())
                        .addCriterion("shape_card", hasItem(BuilderSetup.SHAPE_CARD_DEF.get())),
                "rWr", "iMi", "rLr");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY.get())
                        .key('P', Items.DIAMOND_PICKAXE)
                        .key('S', Items.DIAMOND_SHOVEL)
                        .key('M', BuilderSetup.SHAPE_CARD_DEF.get())
                        .addCriterion("shape_card", hasItem(BuilderSetup.SHAPE_CARD_DEF.get())),
                "rPr", "iMi", "rSr");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_CLEAR.get())
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY.get())
                        .addCriterion("shape_card", hasItem(BuilderSetup.SHAPE_CARD_DEF.get())),
                "GGG", "GMG", "GGG");
        build(consumer, new ResourceLocation(RFToolsBuilder.MODID, "shape_card_quarry_dirt"), CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY.get())
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY_CLEAR.get())
                        .addCriterion("shape_card", hasItem(BuilderSetup.SHAPE_CARD_DEF.get())),
                "DDD", "DMD", "DDD");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_FORTUNE.get())
                        .key('n', Items.GHAST_TEAR)
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY.get())
                        .addCriterion("shape_card", hasItem(BuilderSetup.SHAPE_CARD_DEF.get())),
                "sns", "eMd", "srs");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_CLEAR_FORTUNE.get())
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY_FORTUNE.get())
                        .addCriterion("shape_card", hasItem(BuilderSetup.SHAPE_CARD_DEF.get())),
                "GGG", "GMG", "GGG");
        build(consumer, new ResourceLocation(RFToolsBuilder.MODID, "shape_card_quarry_fortune_dirt"), CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_FORTUNE.get())
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY_CLEAR_FORTUNE.get())
                        .addCriterion("shape_card", hasItem(BuilderSetup.SHAPE_CARD_DEF.get())),
                "DDD", "DMD", "DDD");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_SILK.get())
                        .key('n', Items.NETHER_STAR)
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY.get())
                        .addCriterion("shape_card", hasItem(BuilderSetup.SHAPE_CARD_DEF.get())),
                "sns", "dMd", "sds");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_CLEAR_SILK.get())
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY_SILK.get())
                        .addCriterion("shape_card", hasItem(BuilderSetup.SHAPE_CARD_DEF.get())),
                "GGG", "GMG", "GGG");
        build(consumer, new ResourceLocation(RFToolsBuilder.MODID, "shape_card_quarry_silk_dirt"), CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_SILK.get())
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY_CLEAR_SILK.get())
                        .addCriterion("shape_card", hasItem(BuilderSetup.SHAPE_CARD_DEF.get())),
                "DDD", "DMD", "DDD");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_VOID.get())
                        .key('x', Tags.Items.DYES_BLACK)
                        .key('M', BuilderSetup.SHAPE_CARD_DEF.get())
                        .addCriterion("shape_card", hasItem(BuilderSetup.SHAPE_CARD_DEF.get())),
                "xOx", "OMO", "xOx");
    }
}
