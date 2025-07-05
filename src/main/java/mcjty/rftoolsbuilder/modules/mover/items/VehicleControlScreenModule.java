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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public record VehicleControlScreenModule(GlobalPos pos, String line, String mover, String vehicle, String button, int color, int buttonColor, String monitor, TextAlign align) implements IScreenModule<VehicleControlScreenModule, VehicleControlScreenModule.EmptyData> {

    public static final VehicleControlScreenModule DEFAULT = new VehicleControlScreenModule(GlobalPos.of(Level.OVERWORLD, BlockPosTools.INVALID), "", "", "", "", 0xffffff, 0xffffff, "", TextAlign.ALIGN_LEFT);

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

    public GlobalPos getPos() {
        return pos;
    }

    public String getLine() {
        return line;
    }

    public String getMover() {
        return mover;
    }

    public String getVehicle() {
        return vehicle;
    }

    public String getButton() {
        return button;
    }

    public int getColor() {
        return color;
    }

    public int getButtonColor() {
        return buttonColor;
    }

    public String getMonitor() {
        return monitor;
    }

    public TextAlign getAlign() {
        return align;
    }

    public VehicleControlScreenModule withLine(String line) {
        return new VehicleControlScreenModule(pos, line, mover, vehicle, button, color, buttonColor, monitor, align);
    }

    public VehicleControlScreenModule withMover(String mover) {
        return new VehicleControlScreenModule(pos, line, mover, vehicle, button, color, buttonColor, monitor, align);
    }

    public VehicleControlScreenModule withVehicle(String vehicle) {
        return new VehicleControlScreenModule(pos, line, mover, vehicle, button, color, buttonColor, monitor, align);
    }

    public VehicleControlScreenModule withButton(String button) {
        return new VehicleControlScreenModule(pos, line, mover, vehicle, button, color, buttonColor, monitor, align);
    }

    public VehicleControlScreenModule withColor(int color) {
        return new VehicleControlScreenModule(pos, line, mover, vehicle, button, color, buttonColor, monitor, align);
    }

    public VehicleControlScreenModule withButtonColor(int buttonColor) {
        return new VehicleControlScreenModule(pos, line, mover, vehicle, button, color, buttonColor, monitor, align);
    }

    public VehicleControlScreenModule withMonitor(String monitor) {
        return new VehicleControlScreenModule(pos, line, mover, vehicle, button, color, buttonColor, monitor, align);
    }

    public VehicleControlScreenModule withAlign(TextAlign align) {
        return new VehicleControlScreenModule(pos, line, mover, vehicle, button, color, buttonColor, monitor, align);
    }

    public VehicleControlScreenModule withPos(GlobalPos pos) {
        return new VehicleControlScreenModule(pos, line, mover, vehicle, button, color, buttonColor, monitor, align);
    }

    public VehicleControlScreenModule withPos(ResourceKey<Level> dim, BlockPos coordinate) {
        return new VehicleControlScreenModule(GlobalPos.of(dim, coordinate), line, mover, vehicle, button, color, buttonColor, monitor, align);
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
    public VehicleControlScreenModule validate(Level world, BlockPos pos, boolean isPlus) {
        return this;
    }

    @Override
    public int getRfPerTick() {
        return MoverConfiguration.VEHICLE_CONTROL_RFPERTICK.get();
    }

    @Override
    public ItemStack mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked, Player player) {
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
