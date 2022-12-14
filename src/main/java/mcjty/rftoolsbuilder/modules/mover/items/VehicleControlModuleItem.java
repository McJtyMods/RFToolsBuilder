package mcjty.rftoolsbuilder.modules.mover.items;

import mcjty.lib.crafting.INBTPreservingIngredient;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.storage.IStorageScanner;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.mover.MoverConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

public class VehicleControlModuleItem extends GenericModuleItem implements INBTPreservingIngredient {

    @Override
    protected int getUses(ItemStack stack) {
        return MoverConfiguration.VEHICLE_CONTROL_RFPERTICK.get();
    }

    @Override
    protected boolean hasGoldMessage(ItemStack stack) {
        return !ModuleTools.hasModuleTarget(stack);
    }

    @Override
    protected String getInfoString(ItemStack stack) {
        return ModuleTools.getTargetString(stack);
    }

    public VehicleControlModuleItem() {
        super(new Properties().stacksTo(1).defaultDurability(1).tab(RFToolsBuilder.setup.getTab()));
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level world = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof IStorageScanner) {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            String name = "<invalid>";
            if (block != null && !world.getBlockState(pos).isAir()) {
                name = Tools.getReadableName(world, pos);
            }
            ModuleTools.setPositionInModule(stack, world.dimension(), pos, name);
            if (world.isClientSide) {
                Logging.message(player, "Vehicle control module is set to block '" + name + "'");
            }
        } else {
            ModuleTools.clearPositionInModule(stack);
            if (world.isClientSide) {
                Logging.message(player, "Vehicle control module is cleared");
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public Class<VehicleControlScreenModule> getServerScreenModule() {
        return VehicleControlScreenModule.class;
    }

    @Override
    public Class<VehicleControlClientScreenModule> getClientScreenModule() {
        return VehicleControlClientScreenModule.class;
    }

    @Override
    public String getModuleName() {
        return "Veh";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .label("Label:").text("text", "Label text").color("color", "Label color").nl()
                .label("Button:").text("button", "Button text").color("buttonColor", "Button color").nl()
                .label("Mover:").text("mover", "Name of the mover").nl()
                .choices("align", "Label alignment", "Left", "Center", "Right").nl();
    }

    // @todo 1.14 implement!
    @Override
    public Collection<String> getTagsToPreserve() {
        return Collections.emptyList();
    }
}