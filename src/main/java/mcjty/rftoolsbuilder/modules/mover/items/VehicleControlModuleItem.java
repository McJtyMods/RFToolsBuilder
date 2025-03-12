package mcjty.rftoolsbuilder.modules.mover.items;

import com.mojang.serialization.Codec;
import mcjty.lib.crafting.IComponentsToPreserve;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.TextAlign;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.rftoolsbuilder.modules.mover.MoverConfiguration;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverControllerTileEntity;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class VehicleControlModuleItem extends GenericModuleItem implements IComponentsToPreserve {

    @Override
    protected int getUses(ItemStack stack) {
        return MoverConfiguration.VEHICLE_CONTROL_RFPERTICK.get();
    }

    @Override
    protected boolean hasGoldMessage(ItemStack stack) {
        return data(stack).getPos().pos() == BlockPosTools.INVALID;
    }

    @Override
    protected String getInfoString(ItemStack stack) {
        VehicleControlScreenModule data = data(stack);
        return ModuleTools.getTargetString(data.getMonitor(), data.getPos());
    }

    @Override
    public @Nullable Codec<? extends IScreenModule<?, ?>> codec() {
        return VehicleControlScreenModule.CODEC;
    }

    @Override
    public @Nullable StreamCodec<RegistryFriendlyByteBuf, ? extends IScreenModule<?, ?>> streamCodec() {
        return VehicleControlScreenModule.STREAM_CODEC;
    }

    @Override
    public @Nullable DataComponentType<? extends IScreenModule<?, ?>> componentType() {
        return MoverModule.MODULE_VEHICLECONTROL_DATA.get();
    }

    @Override
    public IScreenModule<?, ?> createServerScreenModule() {
        return VehicleControlScreenModule.DEFAULT;
    }

    @Override
    public IClientScreenModule<?> createClientScreenModule() {
        return new VehicleControlClientScreenModule();
    }

    public VehicleControlModuleItem() {
        super(Registration.createStandardProperties().stacksTo(1));
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level world = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        BlockEntity te = world.getBlockEntity(pos);
        VehicleControlScreenModule data = data(stack);
        if (te instanceof MoverControllerTileEntity) {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            String name = "<invalid>";
            if (block != null && !world.getBlockState(pos).isAir()) {
                name = Tools.getReadableName(world, pos);
            }
            data = data.withPos(GlobalPos.of(world.dimension(), pos));
            data = data.withMonitor(name);
            if (world.isClientSide) {
                Logging.message(player, "Vehicle control module is set to block '" + name + "'");
            }
        } else {
            data = data.withPos(GlobalPos.of(Level.OVERWORLD, BlockPosTools.INVALID));
            data = data.withMonitor("");
            if (world.isClientSide) {
                Logging.message(player, "Vehicle control module is cleared");
            }
        }
        stack.set(MoverModule.MODULE_VEHICLECONTROL_DATA, data);
        return InteractionResult.SUCCESS;
    }

    @Override
    public String getModuleName() {
        return "Veh";
    }

    public static VehicleControlScreenModule data(ItemStack stack) {
        VehicleControlScreenModule data = stack.get(MoverModule.MODULE_VEHICLECONTROL_DATA);
        if (data == null) {
            data = VehicleControlScreenModule.DEFAULT;
        }
        return data;
    }

    public static void data(ItemStack stack, Function<VehicleControlScreenModule, VehicleControlScreenModule> setter) {
        VehicleControlScreenModule data = data(stack);
        data = setter.apply(data);
        stack.set(MoverModule.MODULE_VEHICLECONTROL_DATA, data);
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .label("Label:")
                .text((stack, s) -> data(stack).withLine(s), stack -> data(stack).getLine(), "Label text")
                .color((stack, c) -> data(stack).withColor(c), stack -> data(stack).getColor(), "Label color")
                .nl()

                .label("Button:")
                .text((stack, s) -> data(stack).withButton(s), stack -> data(stack).getButton(), "Button text")
                .color((stack, c) -> data(stack).withButtonColor(c), stack -> data(stack).getButtonColor(), "Button color")
                .nl()

                .label("Mover:")
                .text((stack, s) -> data(stack).withMover(s), stack -> data(stack).getMover(), "Name of the mover")
                .nl()

                .label("Vehicle:")
                .text((stack, s) -> data(stack).withVehicle(s), stack -> data(stack).getVehicle(), "Name of the vehicle")
                .nl()

                .choices((stack, c) -> data(stack).withAlign(TextAlign.get(c)), stack -> data(stack).getAlign().name(), "Label alignment", "Left", "Center", "Right")
                .nl();
    }

    @Override
    public Collection<DataComponentType<?>> getComponentsToPreserve() {
        return List.of(MoverModule.MODULE_VEHICLECONTROL_DATA.get());
    }
}