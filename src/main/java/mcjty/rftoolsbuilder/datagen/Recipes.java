package mcjty.rftoolsbuilder.datagen;

import mcjty.lib.crafting.CopyNBTRecipeBuilder;
import mcjty.lib.datagen.BaseRecipeProvider;
import mcjty.rftoolsbase.items.ModItems;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderSetup;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.block.Blocks;
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
        add('F', ModItems.MACHINE_FRAME);
        add('s', ModItems.DIMENSIONALSHARD);
        group("rftools");
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        build(consumer, ShapedRecipeBuilder.shapedRecipe(BuilderSetup.BUILDER)
                        .addCriterion("machine_frame", InventoryChangeTrigger.Instance.forItems(ModItems.MACHINE_FRAME)),
                "BoB", "rFr", "BrB");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_DEF)
                        .addCriterion("iron_ingot", InventoryChangeTrigger.Instance.forItems(Items.IRON_INGOT)),
                "pBp", "rir", "pBp");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_PUMP)
                        .key('M', BuilderSetup.SHAPE_CARD_DEF)
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF)),
                "rWr", "bMb", "rLr");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_PUMP_CLEAR)
                        .key('M', BuilderSetup.SHAPE_CARD_PUMP)
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF)),
                "GGG", "GMG", "GGG");
        build(consumer, new ResourceLocation(RFToolsBuilder.MODID, "shape_card_pump_dirt"), CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_PUMP)
                        .key('M', BuilderSetup.SHAPE_CARD_PUMP_CLEAR)
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF)),
                "DDD", "DMD", "DDD");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_LIQUID)
                        .key('M', BuilderSetup.SHAPE_CARD_DEF)
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF)),
                "rWr", "iMi", "rLr");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY)
                        .key('P', Items.DIAMOND_PICKAXE)
                        .key('S', Items.DIAMOND_SHOVEL)
                        .key('M', BuilderSetup.SHAPE_CARD_DEF)
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF)),
                "rPr", "iMi", "rSr");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_CLEAR)
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY)
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF)),
                "GGG", "GMG", "GGG");
        build(consumer, new ResourceLocation(RFToolsBuilder.MODID, "shape_card_quarry_dirt"), CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY)
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY_CLEAR)
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF)),
                "DDD", "DMD", "DDD");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_FORTUNE)
                        .key('n', Items.GHAST_TEAR)
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY)
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF)),
                "sns", "eMd", "srs");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_CLEAR_FORTUNE)
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY_FORTUNE)
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF)),
                "GGG", "GMG", "GGG");
        build(consumer, new ResourceLocation(RFToolsBuilder.MODID, "shape_card_quarry_fortune_dirt"), CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_FORTUNE)
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY_CLEAR_FORTUNE)
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF)),
                "DDD", "DMD", "DDD");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_SILK)
                        .key('n', Items.NETHER_STAR)
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY)
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF)),
                "sns", "dMd", "sds");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_CLEAR_SILK)
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY_SILK)
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF)),
                "GGG", "GMG", "GGG");
        build(consumer, new ResourceLocation(RFToolsBuilder.MODID, "shape_card_quarry_silk_dirt"), CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_SILK)
                        .key('M', BuilderSetup.SHAPE_CARD_QUARRY_CLEAR_SILK)
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF)),
                "DDD", "DMD", "DDD");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_VOID)
                        .key('x', Tags.Items.DYES_BLACK)
                        .key('M', BuilderSetup.SHAPE_CARD_DEF)
                        .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF)),
                "xOx", "OMO", "xOx");
    }
}
