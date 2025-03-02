package mcjty.rftoolsbuilder.modules.mover.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.TextAlign;
import mcjty.rftoolsbase.api.screens.data.IModuleData;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.mover.MoverConfiguration;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverControllerTileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public class VehicleControlScreenModule implements IScreenModule<VehicleControlScreenModule.EmptyData> {

    private GlobalPos pos = GlobalPos.of(Level.OVERWORLD, BlockPosTools.INVALID);

    private String line = "";
    private String mover = "";
    private String vehicle = "";

    private String button = "";
    private int color = 0xffffff;
    private int buttonColor = 0xffffff;
    private String monitor = "";
    private TextAlign align = TextAlign.ALIGN_LEFT;

    public static final Codec<VehicleControlScreenModule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(module -> module.pos),
            Codec.STRING.fieldOf("line").forGetter(module -> module.line),
            Codec.STRING.fieldOf("mover").forGetter(module -> module.mover),
            Codec.STRING.fieldOf("vehicle").forGetter(module -> module.vehicle),
            Codec.STRING.fieldOf("button").forGetter(module -> module.button),
            Codec.INT.fieldOf("color").forGetter(module -> module.color),
            Codec.INT.fieldOf("buttonColor").forGetter(module -> module.buttonColor),
            Codec.STRING.optionalFieldOf("monitor", "").forGetter(module -> module.monitor),
            TextAlign.CODEC.fieldOf("align").forGetter(module -> module.align)
    ).apply(instance, VehicleControlScreenModule::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, VehicleControlScreenModule> STREAM_CODEC = CompositeStreamCodec.composite(
            GlobalPos.STREAM_CODEC, module -> module.pos,
            ByteBufCodecs.STRING_UTF8, module -> module.line,
            ByteBufCodecs.STRING_UTF8, module -> module.mover,
            ByteBufCodecs.STRING_UTF8, module -> module.vehicle,
            ByteBufCodecs.STRING_UTF8, module -> module.button,
            ByteBufCodecs.INT, module -> module.color,
            ByteBufCodecs.INT, module -> module.buttonColor,
            ByteBufCodecs.STRING_UTF8, module -> module.monitor,
            TextAlign.STREAM_CODEC, module -> module.align,
            VehicleControlScreenModule::new);

    public VehicleControlScreenModule(GlobalPos pos, String line, String mover, String vehicle, String button, int color, int buttonColor, String monitor, TextAlign align) {
        this.pos = pos;
        this.line = line;
        this.mover = mover;
        this.vehicle = vehicle;
        this.button = button;
        this.color = color;
        this.buttonColor = buttonColor;
        this.monitor = monitor;
        this.align = align;
    }

    public VehicleControlScreenModule() {
    }

    public GlobalPos getPos() {
        return pos;
    }

    public void setPos(GlobalPos pos) {
        this.pos = pos;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getMover() {
        return mover;
    }

    public void setMover(String mover) {
        this.mover = mover;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public String getButton() {
        return button;
    }

    public void setButton(String button) {
        this.button = button;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getButtonColor() {
        return buttonColor;
    }

    public void setButtonColor(int buttonColor) {
        this.buttonColor = buttonColor;
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

    public static class EmptyData implements IModuleData {

        public static final String ID = RFToolsBuilder.MODID + ":vehicle_control";

        public EmptyData(FriendlyByteBuf buf) {
        }

        @Override
        public String getId() {
            return ID;
        }

        @Override
        public void writeToBuf(RegistryFriendlyByteBuf buf) {
        }
    }


    @Override
    public EmptyData getData(IScreenDataHelper helper, Level worldObj, long millis) {
        // For now we need no data
        return null;
    }

    @Override
    public void validate(Level world, BlockPos pos, boolean isPlus) {
    }

    @Override
    public int getRfPerTick() {
        return MoverConfiguration.VEHICLE_CONTROL_RFPERTICK.get();
    }

    @Override
    public void mouseClick(Level world, int x, int y, boolean clicked, Player player) {
        int xoffset;
        if (!line.isEmpty()) {
            xoffset = 40;
        } else {
            xoffset = 5;
        }
        if (x >= xoffset) {
            if (!mover.isEmpty()) {
                getMoverController(world, pos.dimension(), pos.pos()).ifPresent(controller -> {
                    controller.setupMovement(mover, vehicle);
                });
            } else {
                if (player != null) {
                    player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "Module is not linked to mover controller!"), false);
                }
            }
        }
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
