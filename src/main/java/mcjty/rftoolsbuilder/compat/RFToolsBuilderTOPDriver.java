package mcjty.rftoolsbuilder.compat;

import mcjty.lib.compat.theoneprobe.McJtyLibTOPDriver;
import mcjty.lib.compat.theoneprobe.TOPDriver;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.blocks.SpaceChamberControllerTileEntity;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldProjectorBlock;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldProjectorTileEntity;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class RFToolsBuilderTOPDriver implements TOPDriver {

    public static final RFToolsBuilderTOPDriver DRIVER = new RFToolsBuilderTOPDriver();

    private final Map<ResourceLocation, TOPDriver> drivers = new HashMap<>();

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
        ResourceLocation id = Tools.getId(blockState);
        if (!drivers.containsKey(id)) {
            if (blockState.getBlock() == BuilderModule.BUILDER.get()) {
                drivers.put(id, new BuilderDriver());
            } else if (blockState.getBlock() == BuilderModule.SPACE_CHAMBER_CONTROLLER.get()) {
                drivers.put(id, new SpaceChamberControllerDriver());
            } else if (blockState.getBlock() instanceof ShieldProjectorBlock) {
                drivers.put(id, new ShieldProjectorDriver());
            } else {
                drivers.put(id, new DefaultDriver());
            }
        }
        TOPDriver driver = drivers.get(id);
        if (driver != null) {
            driver.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        }
    }

    private static class DefaultDriver implements TOPDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
            McJtyLibTOPDriver.DRIVER.addStandardProbeInfo(mode, probeInfo, player, world, blockState, data);
        }
    }

    private static class SpaceChamberControllerDriver implements TOPDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
            McJtyLibTOPDriver.DRIVER.addStandardProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getBlockEntity(data.getPos()), (SpaceChamberControllerTileEntity te) -> {
                int channel = te.getChannel();
                probeInfo.text(CompoundText.createLabelInfo("Channel: ", channel));
                if (channel != -1) {
                    int size = te.getChamberSize();
                    if (size == -1) {
                        probeInfo.text(ChatFormatting.YELLOW + "Chamber not formed!");
                    } else {
                        probeInfo.text(ChatFormatting.GREEN + "Area: " + size + " blocks");
                    }
                }
            }, "Bad tile entity!");
        }
    }

    private static class BuilderDriver implements TOPDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
            McJtyLibTOPDriver.DRIVER.addStandardProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getBlockEntity(data.getPos()), (BuilderTileEntity te) -> {
                int scan = te.getCurrentLevel();
                probeInfo.text(CompoundText.createLabelInfo("Current level: ", (scan == -1 ? "not scanning" : scan)));
            }, "Bad tile entity!");
        }
    }

    private static class ShieldProjectorDriver implements TOPDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
            McJtyLibTOPDriver.DRIVER.addStandardProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getBlockEntity(data.getPos()), (ShieldProjectorTileEntity te) -> {
                boolean composed = te.isShieldComposed();
                if (composed) {
                    probeInfo.text(CompoundText.create().label("Composed: ").info(ComponentFactory.literal("yes")));
                } else {
                    probeInfo.text(CompoundText.create().label("Composed: ").info(ComponentFactory.literal("no")));
                }
                boolean active = te.isShieldActive();
                if (active) {
                    probeInfo.text(CompoundText.create().label("Active: ").info(ComponentFactory.literal("yes")));
                } else {
                    probeInfo.text(CompoundText.create().label("Active: ").info(ComponentFactory.literal("no")));
                }
            }, "Bad tile entity!");
        }
    }

}
