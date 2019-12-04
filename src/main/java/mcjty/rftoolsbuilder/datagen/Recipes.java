package mcjty.rftoolsbuilder.datagen;

import mcjty.lib.crafting.CopyNBTRecipeBuilder;
import mcjty.lib.datagen.BaseRecipeProvider;
import mcjty.rftoolsbase.items.ModItems;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderSetup;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class Recipes extends BaseRecipeProvider {

    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
        add('F', ModItems.MACHINE_FRAME);
        add('s', ModItems.DIMENSIONALSHARD);
        group("rftools");
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        build(consumer, ShapedRecipeBuilder.shapedRecipe(BuilderSetup.BUILDER.get())
                        .addCriterion("machine_frame", InventoryChangeTrigger.Instance.forItems(ModItems.MACHINE_FRAME)),
                "BoB", "rFr", "BrB");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_DEF.get())
                        .addCriterion("iron_ingot", InventoryChangeTrigger.Instance.forItems(Items.IRON_INGOT)),
                "pBp", "rir", "pBp");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_PUMP.get())
                        .key('M', BuilderSetup.SHAPE_CARD_DEF.get())
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF.get())),
                "rWr", "bMb", "rLr");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_PUMP_CLEAR.get())
                        .key('M', BuilderSetup.SHAPE_CARD_PUMP.get())
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF.get())),
                "GGG", "GMG", "GGG");
        build(consumer, new ResourceLocation(RFToolsBuilder.MODID, "shape_card_pump_dirt"), CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_PUMP.get())
                        .key('M', BuilderSetup.SHAPE_CARD_PUMP_CLEAR.get())
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF.get())),
                "DDD", "DMD", "DDD");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_LIQUID.get())
                        .key('M', BuilderSetup.SHAPE_CARD_DEF.get())
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF.get())),
                "rWr", "iMi", "rLr");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY.get())
                        .key('P', Items.DIAMOND_PICKAXE)
                        .key('S', Items.DIAMOND_SHOVEL)
                        .key('M', BuilderSetup.SHAPE_CARD_DEF.get())
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF.get())),
                "rPr", "iMi", "rSr");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_CLEAR.get())
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY.get())
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF.get())),
                "GGG", "GMG", "GGG");
        build(consumer, new ResourceLocation(RFToolsBuilder.MODID, "shape_card_quarry_dirt"), CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY.get())
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY_CLEAR.get())
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF.get())),
                "DDD", "DMD", "DDD");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_FORTUNE.get())
                        .key('n', Items.GHAST_TEAR)
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY.get())
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF.get())),
                "sns", "eMd", "srs");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_CLEAR_FORTUNE.get())
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY_FORTUNE.get())
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF.get())),
                "GGG", "GMG", "GGG");
        build(consumer, new ResourceLocation(RFToolsBuilder.MODID, "shape_card_quarry_fortune_dirt"), CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_FORTUNE.get())
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY_CLEAR_FORTUNE.get())
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF.get())),
                "DDD", "DMD", "DDD");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_SILK.get())
                        .key('n', Items.NETHER_STAR)
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY.get())
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF.get())),
                "sns", "dMd", "sds");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_CLEAR_SILK.get())
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY_SILK.get())
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF.get())),
                "GGG", "GMG", "GGG");
        build(consumer, new ResourceLocation(RFToolsBuilder.MODID, "shape_card_quarry_silk_dirt"), CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_SILK.get())
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY_CLEAR_SILK.get())
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF.get())),
                "DDD", "DMD", "DDD");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_VOID.get())
                        .key('x', Tags.Items.DYES_BLACK)
                        .key('M', BuilderSetup.SHAPE_CARD_DEF.get())
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF.get())),
                "xOx", "OMO", "xOx");
    }
}
