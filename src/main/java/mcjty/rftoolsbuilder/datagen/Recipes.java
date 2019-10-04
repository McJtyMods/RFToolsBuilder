package mcjty.rftoolsbuilder.datagen;

import mcjty.lib.crafting.CopyNBTRecipeBuilder;
import mcjty.rftoolsbase.items.ModItems;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderSetup;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

public class Recipes extends RecipeProvider {

    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        ShapedRecipeBuilder.shapedRecipe(BuilderSetup.BUILDER)
                .patternLine("beb")
                .patternLine("rMr")
                .patternLine("brb")
                .key('e', Items.ENDER_PEARL)
                .key('r', Items.REDSTONE)
                .key('b', Blocks.BRICKS)
                .key('M', ModItems.MACHINE_FRAME)
                .setGroup("")
                .addCriterion("machine_frame", InventoryChangeTrigger.Instance.forItems(ModItems.MACHINE_FRAME))
                .build(consumer);
        ShapedRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_DEF)
                .patternLine("pbp")
                .patternLine("rir")
                .patternLine("pbp")
                .key('i', Items.IRON_INGOT)
                .key('r', Items.REDSTONE)
                .key('b', Blocks.BRICKS)
                .key('p', Items.PAPER)
                .setGroup("")
                .addCriterion("iron_ingot", InventoryChangeTrigger.Instance.forItems(Items.IRON_INGOT))
                .build(consumer);
        CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_PUMP)
                .patternLine("rwr")
                .patternLine("bMb")
                .patternLine("rlr")
                .key('w', Items.WATER_BUCKET)
                .key('l', Items.LAVA_BUCKET)
                .key('r', Items.REDSTONE)
                .key('b', Items.BUCKET)
                .key('M', BuilderSetup.SHAPE_CARD_DEF)
                .setGroup("")
                .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF))
                .build(consumer);
        CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_PUMP_CLEAR)
                .patternLine("ggg")
                .patternLine("gMg")
                .patternLine("ggg")
                .key('g', Items.GLASS)
                .key('M', BuilderSetup.SHAPE_CARD_PUMP)
                .setGroup("")
                .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF))
                .build(consumer);
        CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_PUMP)
                .patternLine("ggg")
                .patternLine("gMg")
                .patternLine("ggg")
                .key('g', Items.DIRT)
                .key('M', BuilderSetup.SHAPE_CARD_PUMP_CLEAR)
                .setGroup("")
                .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF))
                .build(consumer, new ResourceLocation(RFToolsBuilder.MODID, "shape_card_pump_dirt"));
        CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_LIQUID)
                .patternLine("rwr")
                .patternLine("iMi")
                .patternLine("rlr")
                .key('w', Items.WATER_BUCKET)
                .key('l', Items.LAVA_BUCKET)
                .key('r', Items.REDSTONE)
                .key('i', Items.IRON_INGOT)
                .key('M', BuilderSetup.SHAPE_CARD_DEF)
                .setGroup("")
                .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF))
                .build(consumer);
        CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY)
                .patternLine("rpr")
                .patternLine("iMi")
                .patternLine("rsr")
                .key('p', Items.DIAMOND_PICKAXE)
                .key('s', Items.DIAMOND_SHOVEL)
                .key('r', Items.REDSTONE)
                .key('i', Items.IRON_INGOT)
                .key('M', BuilderSetup.SHAPE_CARD_DEF)
                .setGroup("")
                .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF))
                .build(consumer);
        CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_CLEAR)
                .patternLine("ggg")
                .patternLine("gMg")
                .patternLine("ggg")
                .key('g', Items.GLASS)
                .key('M', BuilderSetup.SHAPE_CARD_QUARRY)
                .setGroup("")
                .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF))
                .build(consumer);
        CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY)
                .patternLine("ggg")
                .patternLine("gMg")
                .patternLine("ggg")
                .key('g', Items.DIRT)
                .key('M', BuilderSetup.SHAPE_CARD_QUARRY_CLEAR)
                .setGroup("")
                .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF))
                .build(consumer, new ResourceLocation(RFToolsBuilder.MODID, "shape_card_quarry_dirt"));
        CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_FORTUNE)
                .patternLine("RnR")
                .patternLine("eMd")
                .patternLine("RrR")
                .key('R', ModItems.DIMENSIONALSHARD)
                .key('d', Items.DIAMOND)
                .key('r', Items.REDSTONE)
                .key('e', Items.EMERALD)
                .key('n', Items.GHAST_TEAR)
                .key('M', BuilderSetup.SHAPE_CARD_QUARRY)
                .setGroup("")
                .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF))
                .build(consumer);
        CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_CLEAR_FORTUNE)
                .patternLine("ggg")
                .patternLine("gMg")
                .patternLine("ggg")
                .key('g', Items.GLASS)
                .key('M', BuilderSetup.SHAPE_CARD_QUARRY_FORTUNE)
                .setGroup("")
                .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF))
                .build(consumer);
        CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_FORTUNE)
                .patternLine("ggg")
                .patternLine("gMg")
                .patternLine("ggg")
                .key('g', Items.DIRT)
                .key('M', BuilderSetup.SHAPE_CARD_QUARRY_CLEAR_FORTUNE)
                .setGroup("")
                .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF))
                .build(consumer, new ResourceLocation(RFToolsBuilder.MODID, "shape_card_quarry_fortune_dirt"));
        CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_SILK)
                .patternLine("RnR")
                .patternLine("dMd")
                .patternLine("RdR")
                .key('R', ModItems.DIMENSIONALSHARD)
                .key('d', Items.DIAMOND)
                .key('n', Items.NETHER_STAR)
                .key('M', BuilderSetup.SHAPE_CARD_QUARRY)
                .setGroup("")
                .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF))
                .build(consumer);
        CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_CLEAR_SILK)
                .patternLine("ggg")
                .patternLine("gMg")
                .patternLine("ggg")
                .key('g', Items.GLASS)
                .key('M', BuilderSetup.SHAPE_CARD_QUARRY_SILK)
                .setGroup("")
                .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF))
                .build(consumer);
        CopyNBTRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD_QUARRY_SILK)
                .patternLine("ggg")
                .patternLine("gMg")
                .patternLine("ggg")
                .key('g', Items.DIRT)
                .key('M', BuilderSetup.SHAPE_CARD_QUARRY_CLEAR_SILK)
                .setGroup("")
                .addCriterion("shape_card", InventoryChangeTrigger.Instance.forItems(BuilderSetup.SHAPE_CARD_DEF))
                .build(consumer, new ResourceLocation(RFToolsBuilder.MODID, "shape_card_quarry_silk_dirt"));

    }
}
