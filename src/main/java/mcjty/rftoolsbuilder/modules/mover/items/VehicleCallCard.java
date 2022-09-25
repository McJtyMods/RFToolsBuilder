package mcjty.rftoolsbuilder.modules.mover.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.NBTTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.rftoolsbuilder.modules.mover.MoverConfiguration;
import mcjty.rftoolsbuilder.modules.mover.modules.CallCardScreenModule;
import mcjty.rftoolsbuilder.modules.mover.modulesclient.CallCardClientScreenModule;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.*;

public class VehicleCallCard extends GenericModuleItem {

    private final Lazy<TooltipBuilder> tooltipBuilder = () -> new TooltipBuilder()
            .info(key("message.rftoolsbuilder.shiftmessage"))
            .infoShift(header(),
                    parameter("name", VehicleCallCard::getVehicleName));

    public VehicleCallCard() {
        super(Registration.createStandardProperties().stacksTo(1));
    }

    public static String getVehicleName(ItemStack stack) {
        return NBTTools.getString(stack, "vehicleName", "<not bound>");
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack itemStack, Level world, @Nonnull List<Component> list, @Nonnull TooltipFlag flag) {
        super.appendHoverText(itemStack, world, list, flag);
        tooltipBuilder.get().makeTooltip(Tools.getId(this), itemStack, list, flag);
    }

    public static void setName(ItemStack vehicleCard, String vehicleName) {
        if (vehicleName == null || vehicleName.strip().isEmpty()) {
            vehicleCard.getOrCreateTag().remove("vehicleName");
        } else {
            vehicleCard.getOrCreateTag().putString("vehicleName", vehicleName);
        }
    }

    @Override
    public Class<? extends IScreenModule<?>> getServerScreenModule() {
        return CallCardScreenModule.class;
    }

    @Override
    public Class<? extends IClientScreenModule<?>> getClientScreenModule() {
        return CallCardClientScreenModule.class;
    }

    @Override
    public String getModuleName() {
        return "Call";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .label("Vehicle:").text("name", "Vehicle name (or empty)").nl()
                .label("Label:").text("text", "Label text").color("color", "Label color").nl()
                .label("Button:").text("button", "Button text").color("buttonColor", "Button color").nl()
                .toggle("toggle", "Toggle", "Toggle button mode")
                .choices("align", "Label alignment", "Left", "Center", "Right").nl();
    }

    @Override
    protected int getUses(ItemStack stack) {
        return MoverConfiguration.CALLCARD_RFPERTICK.get();
    }
}
