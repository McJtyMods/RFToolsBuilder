package mcjty.rftoolsbuilder.datagen;

import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderSetup;
import mcjty.rftoolsbuilder.modules.shield.ShieldSetup;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.data.LanguageProvider;

public class Languages extends LanguageProvider {

    public Languages(DataGenerator gen, String locale) {
        super(gen, RFToolsBuilder.MODID, locale);
    }

    @Override
    protected void addTranslations() {

        add("itemGroup.rftoolsbuilder", "RFTools Builder");

        add(BuilderSetup.BUILDER.get(), "Builder");
        add(BuilderSetup.SHAPE_CARD_DEF.get(), "Shape Card");
        add(BuilderSetup.SHAPE_CARD_VOID.get(), "Shape Card (Void)");
        add(BuilderSetup.SHAPE_CARD_QUARRY.get(), "Shape Card (Quarry)");
        add(BuilderSetup.SHAPE_CARD_QUARRY_SILK.get(), "Shape Card (Silk Quarry)");
        add(BuilderSetup.SHAPE_CARD_QUARRY_FORTUNE.get(), "Shape Card (Fortune Quarry)");
        add(BuilderSetup.SHAPE_CARD_QUARRY_CLEAR.get(), "Shape Card (Clearing Quarry)");
        add(BuilderSetup.SHAPE_CARD_QUARRY_CLEAR_SILK.get(), "Shape Card (Clearing Silk Quarry)");
        add(BuilderSetup.SHAPE_CARD_QUARRY_CLEAR_FORTUNE.get(), "Shape Card (Clearing Fortune Quarry)");
        add(BuilderSetup.SHAPE_CARD_PUMP.get(), "Shape Card (Pump)");
        add(BuilderSetup.SHAPE_CARD_PUMP_CLEAR.get(), "Shape Card (Clearing Pump)");
        add(BuilderSetup.SHAPE_CARD_LIQUID.get(), "Shape Card (Placing Liquids)");

        add(ShieldSetup.SHIELD_BLOCK1.get(), "Shield Projector Tier 1");
        add(ShieldSetup.SHIELD_BLOCK2.get(), "Shield Projector Tier 2");
        add(ShieldSetup.SHIELD_BLOCK3.get(), "Shield Projector Tier 3");
        add(ShieldSetup.SHIELD_BLOCK4.get(), "Shield Projector Tier 4");
        add(ShieldSetup.TEMPLATE_BLUE.get(), "Blue Shield Template");
        add(ShieldSetup.TEMPLATE_RED.get(), "Red Shield Template");
        add(ShieldSetup.TEMPLATE_GREEN.get(), "Green Shield Template");
        add(ShieldSetup.TEMPLATE_YELLOW.get(), "Yellow Shield Template");
        add(ShieldSetup.SHIELDING_CUTOUT.get(), "Shield");
        add(ShieldSetup.SHIELDING_SOLID.get(), "Shield");
        add(ShieldSetup.SHIELDING_TRANSLUCENT.get(), "Shield");

        add("message.rftoolsbuilder.shiftmessage", "<Press Shift>");
        add("message.rftoolsbuilder.builder", "@fThis block can quarry areas, pump liquids,\n"
                + "@fmove/copy/swap structures, collect items\n"
                + "@fand XP, move entities, build structures, ...\n"
                + "@eInfusing bonus: reduced power consumption\n"
                + "@eand increased speed.");
        add("message.rftoolsbuilder.composer", "@fThis block can construct more complex\n"
                + "@fshape cards for the Builder or Shield\n"
                + "@fby creating combinations of other shape\n"
                + "@fcards");
        add("message.rftoolsbuilder.locator", "@fPlace this block on top of a Scanner\n"
                + "@fto extend its functionality with the ability\n"
                + "@fto locate entities and machines using power");
        add("message.rftoolsbuilder.projector", "@fThis block can project the contents\n"
                + "@fof a shape card like a hologram");
        add("message.rftoolsbuilder.scanner", "@2Scan id: %s\n"
                + "@fThis block can scan an area and link\n"
                + "@fto shape cards for the Builder or Shield.\n"
                + "@fThe resulting shape card can also be used\n"
                + "@fin the Composer");
        add("message.rftoolsbuilder.remote_scanner", "@2Scan id: %s\n"
                + "@fThis block can scan an area and link\n"
                + "@fto shape cards for the Builder or Shield.\n"
                + "@fThe resulting shape card can also be used\n"
                + "@fin the Composer\n"
                + "@6This version can scan scan through a\n"
                + "@6dialed matter transmitter which should be\n"
                + "@6located above it");
    }
}
