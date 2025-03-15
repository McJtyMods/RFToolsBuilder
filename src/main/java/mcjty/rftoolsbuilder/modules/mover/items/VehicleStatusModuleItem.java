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

public class VehicleStatusModuleItem extends GenericModuleItem implements IComponentsToPreserve {

    @Override
    protected int getUses(ItemStack stack) {
        return MoverConfiguration.VEHICLE_STATUS_RFPERTICK.get();
    }

    @Override
    protected boolean hasGoldMessage(ItemStack stack) {
        return !BlockPosTools.isValid(data(stack).getPos().pos());
    }

    @Override
    protected String getInfoString(ItemStack stack) {
        VehicleStatusScreenModule data = data(stack);
        return ModuleTools.getTargetString(data.getMonitor(), data.getPos());
    }

    public VehicleStatusModuleItem() {
        super(Registration.createStandardProperties().stacksTo(1));
    }

    @Override
    public @Nullable Codec<? extends IScreenModule<?, ?>> codec() {
        return VehicleStatusScreenModule.CODEC;
    }

    @Override
    public @Nullable StreamCodec<RegistryFriendlyByteBuf, ? extends IScreenModule<?, ?>> streamCodec() {
        return VehicleStatusScreenModule.STREAM_CODEC;
    }

    @Override
    public @Nullable DataComponentType<? extends IScreenModule<?, ?>> componentType() {
        return MoverModule.MODULE_VEHICLESTATUS_DATA.get();
    }

    @Override
    public IScreenModule<?, ?> createServerScreenModule() {
        return VehicleControlScreenModule.DEFAULT;
    }

    @Override
    public IClientScreenModule<?> createClientScreenModule() {
        return new VehicleControlClientScreenModule();
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level world = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof MoverControllerTileEntity) {
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
    public String getModuleName() {
        return "VStat";
    }

    public static VehicleStatusScreenModule data(ItemStack stack) {
        VehicleStatusScreenModule data = stack.get(MoverModule.MODULE_VEHICLESTATUS_DATA);
        if (data == null) {
            data = VehicleStatusScreenModule.DEFAULT;
        }
        return data;
    }

    public static void data(ItemStack stack, Consumer<VehicleStatusScreenModule> setter) {
        VehicleStatusScreenModule data = data(stack);
        setter.accept(data);
        stack.set(MoverModule.MODULE_VEHICLESTATUS_DATA, data);
    }


    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .label("Label:")
                .text((stack, s) -> data(stack).withLabel(s), stack -> data(stack).getLabel(), "Label text")
                .color((stack, c) -> data(stack).withLabelColor(c), stack -> data(stack).getLabelColor(), "Label color")
                .nl()

                .label("Vehicle:")
                .text((stack, s) -> data(stack).withVehicle(s), stack -> data(stack).getVehicle(), "Name of the vehicle")
                .color((stack, c) -> data(stack).withColor(c), stack -> data(stack).getColor(), "Mover color")
                .nl()

                .choices((stack, c) -> data(stack, d -> d.withAlign(TextAlign.get(c))), stack -> data(stack).getAlign().getSerializedName(), "Label alignment", "Left", "Center", "Right")
                .nl();
    }

    @Override
    public Collection<DataComponentType<?>> getComponentsToPreserve() {
        return List.of(MoverModule.MODULE_VEHICLESTATUS_DATA.get());
    }
}