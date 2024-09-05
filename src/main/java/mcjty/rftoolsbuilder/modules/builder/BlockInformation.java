package mcjty.rftoolsbuilder.modules.builder;

import mcjty.lib.varia.Tools;
import mcjty.rftoolsbuilder.modules.builder.blocks.SupportBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BlockInformation {

    private static Map<ResourceLocation,BlockInformation> blockInformationMap = null;
    private static Map<ResourceLocation,Boolean> blackWhiteListedNBTBlocksMap = null;
    private final ResourceLocation blockName;
    private final SupportBlock.SupportStatus blockLevel;
    private final double costFactor;
    private final int rotateInfo;

    public static final int ROTATE_invalid = -1;
    public static final int ROTATE_mmmm = 0;
    public static final int ROTATE_mfff = 1;

    public static final BlockInformation INVALID = new BlockInformation(null, SupportBlock.SupportStatus.STATUS_ERROR, 1.0);
    public static final BlockInformation OK = new BlockInformation(null, SupportBlock.SupportStatus.STATUS_OK, 1.0, ROTATE_mmmm);
    public static final BlockInformation FREE = new BlockInformation(null, SupportBlock.SupportStatus.STATUS_OK, 0.0, ROTATE_mmmm);

    private static int rotateStringToId(String rotateString) {
        if ("mmmm".equals(rotateString)) {
            return ROTATE_mmmm;
        } else if ("mfff".equals(rotateString)) {
            return ROTATE_mfff;
        } else {
            return ROTATE_invalid;
        }
    }

    private static void initBlackWhiteListedNBTBlocksMap() {
        if (blackWhiteListedNBTBlocksMap == null) {
            blackWhiteListedNBTBlocksMap = new HashMap<>();
            List<? extends String> blocks = BuilderConfiguration.blackWhiteListedNBTBlocks.get();
            Boolean whitelist = false;
            for (String block : blocks) {
                if (block.contains("=")) {
                    String[] split = block.split("=");
                    block = split[0];
                    whitelist = Boolean.valueOf(split[1]);
                }
                ResourceLocation id = new ResourceLocation(block);
                blackWhiteListedNBTBlocksMap.put(id,whitelist);
            }
        }
    }

    private static void initMap() {
        if (blockInformationMap == null) {
            blockInformationMap = new HashMap<>();
            List<? extends String> blocks = BuilderConfiguration.blackWhiteListedBlocks.get();
            for (String block : blocks) {
                String costS = "1.0f";
                if (block.contains("=")) {
                    String[] split = block.split("=");
                    block = split[0];
                    costS = split[1];
                }
                double cost = Double.parseDouble(costS);
                ResourceLocation id = new ResourceLocation(block);
                if (BuilderConfiguration.teMode.get() == BuilderTileEntityMode.MOVE_BLACKLIST) {
                    blockInformationMap.put(id, new BlockInformation(id, SupportBlock.SupportStatus.STATUS_ERROR, cost));
                } else if (BuilderConfiguration.teMode.get() == BuilderTileEntityMode.MOVE_WHITELIST) {
                    blockInformationMap.put(id, new BlockInformation(id, SupportBlock.SupportStatus.STATUS_OK, cost));
                }
            }
        }
    }

    public BlockInformation(ResourceLocation blockName, SupportBlock.SupportStatus blockLevel, double costFactor) {
        this.blockName = blockName;
        this.blockLevel = blockLevel;
        this.costFactor = costFactor;
        this.rotateInfo = ROTATE_mmmm;
    }

    public BlockInformation(ResourceLocation blockName, SupportBlock.SupportStatus blockLevel, double costFactor, int rotateInfo) {
        this.blockName = blockName;
        this.blockLevel = blockLevel;
        this.costFactor = costFactor;
        this.rotateInfo = rotateInfo;
    }

    public BlockInformation(BlockInformation other, String rotateInfo) {
        this(other.blockName, other.blockLevel, other.costFactor, rotateStringToId(rotateInfo));
    }

    public BlockInformation(BlockInformation other, ResourceLocation blockName, SupportBlock.SupportStatus blockLevel, double costFactor) {
        this(blockName, blockLevel, costFactor, other.rotateInfo);
    }

    @Nullable
    public static BlockInformation getBlockInformation(Block block) {
        initMap();
        return blockInformationMap.get(Tools.getId(block));
    }

    public static boolean shouldTransferNBT(Block block) {
        initBlackWhiteListedNBTBlocksMap();
        return blackWhiteListedNBTBlocksMap.getOrDefault(Tools.getId(block), false);
    }

    public SupportBlock.SupportStatus getBlockLevel() {
        return blockLevel;
    }

    public ResourceLocation getBlockName() {
        return blockName;
    }

    public double getCostFactor() {
        return costFactor;
    }

    public int getRotateInfo() {
        return rotateInfo;
    }
}
