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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public class VehicleStatusScreenModule implements IScreenModule<IModuleDataString> {

    private GlobalPos pos = GlobalPos.of(Level.OVERWORLD, BlockPosTools.INVALID);

    private String label = "";
    private String vehicle = "";
    private String monitor = "";
    private TextAlign align = TextAlign.ALIGN_LEFT;

    private int labelColor = 0xffffff;
    private int color = 0xffffff;


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

    public VehicleStatusScreenModule() {
    }

    public GlobalPos getPos() {
        return pos;
    }

    public void setPos(GlobalPos pos) {
        this.pos = pos;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public String getMonitor() {
        return monitor;
    }

    public void setMonitor(String monitor) {
        this.monitor = monitor;
    }

    public TextAlign getAlign() {
        return align;
    }

    public void setAlign(TextAlign align) {
        this.align = align;
    }

    public int getLabelColor() {
        return labelColor;
    }

    public void setLabelColor(int labelColor) {
        this.labelColor = labelColor;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
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
    public void validate(Level world, BlockPos pos, boolean isPlus) {
    }

    @Override
    public int getRfPerTick() {
        return MoverConfiguration.VEHICLE_STATUS_RFPERTICK.get();
    }

    @Override
    public void mouseClick(Level world, int x, int y, boolean clicked, Player player) {
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
