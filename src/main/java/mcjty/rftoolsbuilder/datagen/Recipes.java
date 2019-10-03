package mcjty.rftoolsbuilder.datagen;

import mcjty.rftoolsbase.items.ModItems;
import mcjty.rftoolsbuilder.modules.builder.BuilderSetup;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Items;

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
        ShapedRecipeBuilder.shapedRecipe(BuilderSetup.SHAPE_CARD)
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
    }
}
