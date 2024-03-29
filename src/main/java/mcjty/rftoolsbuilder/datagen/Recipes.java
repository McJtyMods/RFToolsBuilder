package mcjty.rftoolsbuilder.datagen;

import mcjty.lib.crafting.CopyNBTRecipeBuilder;
import mcjty.lib.datagen.BaseRecipeProvider;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class Recipes extends BaseRecipeProvider {

    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
        add('F', VariousModule.MACHINE_FRAME.get());
        add('s', VariousModule.DIMENSIONALSHARD.get());
    }

    @Override
    protected void buildShapelessRecipes(@Nonnull Consumer<IFinishedRecipe> consumer) {
        build(consumer, ShapedRecipeBuilder.shaped(BuilderModule.BUILDER.get())
                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                "BoB", "rFr", "BrB");
        build(consumer, ShapedRecipeBuilder.shaped(BuilderModule.SHAPE_CARD_DEF.get())
                        .unlockedBy("iron_ingot", has(Items.IRON_INGOT)),
                "pBp", "rir", "pBp");

        build(consumer, ShapedRecipeBuilder.shaped(ShieldModule.SHIELD_BLOCK1.get())
                        .define('g', Tags.Items.INGOTS_GOLD)
                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                "gTg", "rFr", "OOO");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(ShieldModule.SHIELD_BLOCK2.get())
                        .define('M', ShieldModule.SHIELD_BLOCK1.get())
                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                "ROR", "OMO", "ROR");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(ShieldModule.SHIELD_BLOCK3.get())
                        .define('M', ShieldModule.SHIELD_BLOCK2.get())
                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                "sOs", "OMO", "sOs");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(ShieldModule.SHIELD_BLOCK4.get())
                        .define('M', ShieldModule.SHIELD_BLOCK3.get())
                        .define('n', Items.NETHER_STAR)
                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                "nOs", "OMO", "sOn");

        build(consumer, ShapedRecipeBuilder.shaped(ShieldModule.TEMPLATE_BLUE.get(), 8)
                        .define('w', ItemTags.WOOL)
                        .define('l', Tags.Items.DYES_BLUE)
                        .unlockedBy("glass", has(Items.GLASS)),
                "www", "lGl", "www");
        build(consumer, ShapedRecipeBuilder.shaped(ShieldModule.TEMPLATE_RED.get(), 8)
                        .define('w', ItemTags.WOOL)
                        .define('l', Tags.Items.DYES_RED)
                        .unlockedBy("glass", has(Items.GLASS)),
                "www", "lGl", "www");
        build(consumer, ShapedRecipeBuilder.shaped(ShieldModule.TEMPLATE_YELLOW.get(), 8)
                        .define('w', ItemTags.WOOL)
                        .define('l', Tags.Items.DYES_YELLOW)
                        .unlockedBy("glass", has(Items.GLASS)),
                "www", "lGl", "www");
        build(consumer, ShapedRecipeBuilder.shaped(ShieldModule.TEMPLATE_GREEN.get(), 8)
                        .define('w', ItemTags.WOOL)
                        .define('l', Tags.Items.DYES_GREEN)
                        .unlockedBy("glass", has(Items.GLASS)),
                "www", "lGl", "www");

        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderModule.SHAPE_CARD_PUMP.get())
                        .define('M', BuilderModule.SHAPE_CARD_DEF.get())
                        .unlockedBy("shape_card", has(BuilderModule.SHAPE_CARD_DEF.get())),
                "rWr", "bMb", "rLr");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderModule.SHAPE_CARD_PUMP_CLEAR.get())
                        .define('M', BuilderModule.SHAPE_CARD_PUMP.get())
                        .unlockedBy("shape_card", has(BuilderModule.SHAPE_CARD_DEF.get())),
                "GGG", "GMG", "GGG");
        build(consumer, new ResourceLocation(RFToolsBuilder.MODID, "shape_card_pump_dirt"), CopyNBTRecipeBuilder.shapedRecipe(BuilderModule.SHAPE_CARD_PUMP.get())
                        .define('M', BuilderModule.SHAPE_CARD_PUMP_CLEAR.get())
                        .unlockedBy("shape_card", has(BuilderModule.SHAPE_CARD_DEF.get())),
                "DDD", "DMD", "DDD");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderModule.SHAPE_CARD_LIQUID.get())
                        .define('M', BuilderModule.SHAPE_CARD_DEF.get())
                        .unlockedBy("shape_card", has(BuilderModule.SHAPE_CARD_DEF.get())),
                "rWr", "iMi", "rLr");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderModule.SHAPE_CARD_QUARRY.get())
                        .define('P', Items.DIAMOND_PICKAXE)
                        .define('S', Items.DIAMOND_SHOVEL)
                        .define('M', BuilderModule.SHAPE_CARD_DEF.get())
                        .unlockedBy("shape_card", has(BuilderModule.SHAPE_CARD_DEF.get())),
                "rPr", "iMi", "rSr");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderModule.SHAPE_CARD_QUARRY_CLEAR.get())
                        .define('M', BuilderModule.SHAPE_CARD_QUARRY.get())
                        .unlockedBy("shape_card", has(BuilderModule.SHAPE_CARD_DEF.get())),
                "GGG", "GMG", "GGG");
        build(consumer, new ResourceLocation(RFToolsBuilder.MODID, "shape_card_quarry_dirt"), CopyNBTRecipeBuilder.shapedRecipe(BuilderModule.SHAPE_CARD_QUARRY.get())
                        .define('M', BuilderModule.SHAPE_CARD_QUARRY_CLEAR.get())
                        .unlockedBy("shape_card", has(BuilderModule.SHAPE_CARD_DEF.get())),
                "DDD", "DMD", "DDD");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderModule.SHAPE_CARD_QUARRY_FORTUNE.get())
                        .define('n', Items.GHAST_TEAR)
                        .define('M', BuilderModule.SHAPE_CARD_QUARRY.get())
                        .unlockedBy("shape_card", has(BuilderModule.SHAPE_CARD_DEF.get())),
                "sns", "eMd", "srs");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderModule.SHAPE_CARD_QUARRY_CLEAR_FORTUNE.get())
                        .define('M', BuilderModule.SHAPE_CARD_QUARRY_FORTUNE.get())
                        .unlockedBy("shape_card", has(BuilderModule.SHAPE_CARD_DEF.get())),
                "GGG", "GMG", "GGG");
        build(consumer, new ResourceLocation(RFToolsBuilder.MODID, "shape_card_quarry_fortune_dirt"), CopyNBTRecipeBuilder.shapedRecipe(BuilderModule.SHAPE_CARD_QUARRY_FORTUNE.get())
                        .define('M', BuilderModule.SHAPE_CARD_QUARRY_CLEAR_FORTUNE.get())
                        .unlockedBy("shape_card", has(BuilderModule.SHAPE_CARD_DEF.get())),
                "DDD", "DMD", "DDD");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderModule.SHAPE_CARD_QUARRY_SILK.get())
                        .define('n', Items.NETHER_STAR)
                        .define('M', BuilderModule.SHAPE_CARD_QUARRY.get())
                        .unlockedBy("shape_card", has(BuilderModule.SHAPE_CARD_DEF.get())),
                "sns", "dMd", "sds");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderModule.SHAPE_CARD_QUARRY_CLEAR_SILK.get())
                        .define('M', BuilderModule.SHAPE_CARD_QUARRY_SILK.get())
                        .unlockedBy("shape_card", has(BuilderModule.SHAPE_CARD_DEF.get())),
                "GGG", "GMG", "GGG");
        build(consumer, new ResourceLocation(RFToolsBuilder.MODID, "shape_card_quarry_silk_dirt"), CopyNBTRecipeBuilder.shapedRecipe(BuilderModule.SHAPE_CARD_QUARRY_SILK.get())
                        .define('M', BuilderModule.SHAPE_CARD_QUARRY_CLEAR_SILK.get())
                        .unlockedBy("shape_card", has(BuilderModule.SHAPE_CARD_DEF.get())),
                "DDD", "DMD", "DDD");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(BuilderModule.SHAPE_CARD_VOID.get())
                        .define('x', Tags.Items.DYES_BLACK)
                        .define('M', BuilderModule.SHAPE_CARD_DEF.get())
                        .unlockedBy("shape_card", has(BuilderModule.SHAPE_CARD_DEF.get())),
                "xOx", "OMO", "xOx");

        build(consumer, ShapedRecipeBuilder.shaped(BuilderModule.SPACE_CHAMBER_CARD.get(), 1)
                        .unlockedBy("glass", has(Items.GLASS)),
                " B ", "rir", " B ");
        build(consumer, ShapedRecipeBuilder.shaped(BuilderModule.SPACE_CHAMBER.get(), 1)
                        .define('x', Tags.Items.DYES_BLUE)
                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                "xGx", "GFG", "xGx");
        build(consumer, ShapedRecipeBuilder.shaped(BuilderModule.SPACE_CHAMBER_CONTROLLER.get(), 1)
                        .define('X', BuilderModule.SPACE_CHAMBER.get())
                        .unlockedBy("machine_frame", has(VariousModule.MACHINE_FRAME.get())),
                " o ", "TXT", " o ");
    }
}
