package mcjty.rftoolsbuilder.modules.mover.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.TextAlign;
import mcjty.rftoolsbase.api.screens.data.IModuleDataString;
import mcjty.rftoolsbuilder.modules.mover.MoverConfiguration;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverControllerTileEntity;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public record VehicleStatusScreenModule(GlobalPos pos, String label, String vehicle, String monitor, TextAlign align, int labelColor, int color) implements IScreenModule<VehicleStatusScreenModule, IModuleDataString> {

    public static final VehicleStatusScreenModule DEFAULT = new VehicleStatusScreenModule(GlobalPos.of(Level.OVERWORLD, BlockPosTools.INVALID), "", "", "", TextAlign.ALIGN_LEFT, 0xffffff, 0xffffff);

    public static final Codec<VehicleStatusScreenModule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(module -> module.pos),
            Codec.STRING.fieldOf("label").forGetter(module -> module.label),
            Codec.STRING.fieldOf("vehicle").forGetter(module -> module.vehicle),
            Codec.STRING.fieldOf("monitor").forGetter(module -> module.monitor),
            TextAlign.CODEC.fieldOf("align").forGetter(module -> module.align),
            Codec.INT.fieldOf("labelColor").forGetter(module -> module.labelColor),
            Codec.INT.fieldOf("color").forGetter(module -> module.color)
    ).apply(instance, VehicleStatusScreenModule::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, VehicleStatusScreenModule> STREAM_CODEC = CompositeStreamCodec.composite(
            GlobalPos.STREAM_CODEC, module -> module.pos,
            ByteBufCodecs.STRING_UTF8, module -> module.label,
            ByteBufCodecs.STRING_UTF8, module -> module.vehicle,
            ByteBufCodecs.STRING_UTF8, module -> module.monitor,
            TextAlign.STREAM_CODEC, module -> module.align,
            ByteBufCodecs.INT, module -> module.labelColor,
            ByteBufCodecs.INT, module -> module.color,
            VehicleStatusScreenModule::new);

    public VehicleStatusScreenModule(GlobalPos pos, String label, String vehicle, String monitor, TextAlign align, int labelColor, int color) {
        this.pos = pos;
        this.label = label;
        this.vehicle = vehicle;
        this.monitor = monitor;
        this.align = align;
        this.labelColor = labelColor;
        this.color = color;
    }

    public GlobalPos getPos() {
        return pos;
    }

    public String getLabel() {
        return label;
    }

    public String getVehicle() {
        return vehicle;
    }

    public String getMonitor() {
        return monitor;
    }

    public TextAlign getAlign() {
        return align;
    }

    public int getLabelColor() {
        return labelColor;
    }

    public int getColor() {
        return color;
    }

    public VehicleStatusScreenModule withLabel(String label) {
        return new VehicleStatusScreenModule(pos, label, vehicle, monitor, align, labelColor, color);
    }

    public VehicleStatusScreenModule withVehicle(String vehicle) {
        return new VehicleStatusScreenModule(pos, label, vehicle, monitor, align, labelColor, color);
    }

    public VehicleStatusScreenModule withMonitor(String monitor) {
        return new VehicleStatusScreenModule(pos, label, vehicle, monitor, align, labelColor, color);
    }

    public VehicleStatusScreenModule withAlign(TextAlign align) {
        return new VehicleStatusScreenModule(pos, label, vehicle, monitor, align, labelColor, color);
    }

    public VehicleStatusScreenModule withLabelColor(int labelColor) {
        return new VehicleStatusScreenModule(pos, label, vehicle, monitor, align, labelColor, color);
    }

    public VehicleStatusScreenModule withColor(int color) {
        return new VehicleStatusScreenModule(pos, label, vehicle, monitor, align, labelColor, color);
    }

    public VehicleStatusScreenModule withPos(GlobalPos pos) {
        return new VehicleStatusScreenModule(pos, label, vehicle, monitor, align, labelColor, color);
    }

    @Override
    public IModuleDataString getData(IScreenDataHelper helper, Level level, long millis) {
        // For now we need no data
        String mover = getMoverController(level, pos.dimension(), pos.pos()).map(c -> {
            MoverTileEntity m = c.findVehicle(vehicle);
            if (m != null) {
                return m.getName();
            } else {
                return "<unknown>";
            }
        }).orElse("<unknown>");
        return helper.createString(mover);
    }

    @Override
    public VehicleStatusScreenModule validate(Level world, BlockPos pos, boolean isPlus) {
        return this;
    }

    @Override
    public int getRfPerTick() {
        return MoverConfiguration.VEHICLE_STATUS_RFPERTICK.get();
    }

    @Override
    public ItemStack mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked, Player player) {
        return ItemStack.EMPTY;
    }

    public static Optional<MoverControllerTileEntity> getMoverController(Level worldObj, ResourceKey<Level> dim, BlockPos coordinate) {
        Level world = LevelTools.getLevel(worldObj, dim);
        if (world == null) {
            return Optional.empty();
        }

        if (!LevelTools.isLoaded(world, coordinate)) {
            return Optional.empty();
        }

        BlockEntity te = world.getBlockEntity(coordinate);
        if (te == null) {
            return Optional.empty();
        }

        if (!(te instanceof MoverControllerTileEntity)) {
            return Optional.empty();
        }

        return Optional.of((MoverControllerTileEntity) te);
    }
}
