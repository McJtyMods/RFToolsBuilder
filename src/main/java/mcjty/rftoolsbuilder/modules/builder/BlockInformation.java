package mcjty.rftoolsbuilder.modules.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.blocks.SupportBlock;
import net.minecraft.block.Block;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class BlockInformation {

    private static Map<String,BlockInformation> blockInformationMap = new HashMap<>();

    private final String blockName;
    private final int blockLevel; // One of SupportBlock.SUPPORT_ERROR/WARN
    private final double costFactor;
    private final int rotateInfo;

    public static final int ROTATE_invalid = -1;
    public static final int ROTATE_mmmm = 0;
    public static final int ROTATE_mfff = 1;

    public static final BlockInformation INVALID = new BlockInformation("", SupportBlock.STATUS_ERROR, 1.0);
    public static final BlockInformation OK = new BlockInformation("", SupportBlock.STATUS_OK, 1.0, ROTATE_mmmm);
    public static final BlockInformation FREE = new BlockInformation("", SupportBlock.STATUS_OK, 0.0, ROTATE_mmmm);

    private static int rotateStringToId(String rotateString) {
        if ("mmmm".equals(rotateString)) {
            return ROTATE_mmmm;
        } else if ("mfff".equals(rotateString)) {
            return ROTATE_mfff;
        } else {
            return ROTATE_invalid;
        }
    }

    public BlockInformation(String blockName, int blockLevel, double costFactor) {
        this.blockName = blockName;
        this.blockLevel = blockLevel;
        this.costFactor = costFactor;
        this.rotateInfo = ROTATE_mmmm;
    }

    public BlockInformation(String blockName, int blockLevel, double costFactor, int rotateInfo) {
        this.blockName = blockName;
        this.blockLevel = blockLevel;
        this.costFactor = costFactor;
        this.rotateInfo = rotateInfo;
    }

    public BlockInformation(BlockInformation other, String rotateInfo) {
        this(other.blockName, other.blockLevel, other.costFactor, rotateStringToId(rotateInfo));
    }

    public BlockInformation(BlockInformation other, String blockName, int blockLevel, double costFactor) {
        this(blockName, blockLevel, costFactor, other.rotateInfo);
    }

    static void readBuilderBlocksInternal() {
        try(InputStream inputstream = RFToolsBuilder.class.getResourceAsStream("/data/rftoolsbuilder/text/builder.json")) {
            parseBuilderJson(inputstream);
        } catch (IOException e) {
            Logging.logError("Error reading builder.json", e);
        }
    }

    static void readBuilderBlocksConfig() {
        // @todo 1.14
//        File modConfigDir = RFTools.setup.getModConfigDir();
//        File file = new File(modConfigDir.getPath() + File.separator + "rftools", "userbuilder.json");
//        try(FileInputStream inputstream = new FileInputStream(file)) {
//            parseBuilderJson(inputstream);
//        } catch (IOException e) {
//            Logging.log("Could not read 'userbuilder.json', this is not an error!");
//        }
    }

    private static void parseBuilderJson(InputStream inputstream) throws UnsupportedEncodingException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(br);
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            if ("movables".equals(entry.getKey())) {
                readMovablesFromJson(entry.getValue());
            } else if ("rotatables".equals(entry.getKey())) {
                readRotatablesFromJson(entry.getValue());
            }
        }
    }

    private static void readMovablesFromJson(JsonElement element) {
        for (JsonElement entry : element.getAsJsonArray()) {
            String blockName = entry.getAsJsonArray().get(0).getAsString();
            String warningType = entry.getAsJsonArray().get(1).getAsString();
            double costFactor = entry.getAsJsonArray().get(2).getAsDouble();
            int status;
            if ("-".equals(warningType)) {
                status = SupportBlock.STATUS_ERROR;
            } else if ("+".equals(warningType)) {
                status = SupportBlock.STATUS_OK;
            } else {
                status = SupportBlock.STATUS_WARN;
            }
            BlockInformation old = blockInformationMap.get(blockName);
            if (old == null) {
                old = OK;
            }

            blockInformationMap.put(blockName, new BlockInformation(old, blockName, status, costFactor));
        }
    }

    private static void readRotatablesFromJson(JsonElement element) {
        for (JsonElement entry : element.getAsJsonArray()) {
            String blockName = entry.getAsJsonArray().get(0).getAsString();
            String rotatable = entry.getAsJsonArray().get(1).getAsString();
            BlockInformation old = blockInformationMap.get(blockName);
            if (old == null) {
                old = OK;
            }
            blockInformationMap.put(blockName, new BlockInformation(old, rotatable));
        }
    }

    public static BlockInformation getBlockInformation(Block block) {
        BlockInformation information = blockInformationMap.get(block.getRegistryName().toString());
        if (information == null) {
            String modid = BlockTools.getModidForBlock(block);
            information = blockInformationMap.get("modid:" + modid);
        }
        return information;
    }

    // @todo 1.14 call me?
    public static void init() {
        readBuilderBlocksInternal();
        readBuilderBlocksConfig();
    }

    public int getBlockLevel() {
        return blockLevel;
    }

    public String getBlockName() {
        return blockName;
    }

    public double getCostFactor() {
        return costFactor;
    }

    public int getRotateInfo() {
        return rotateInfo;
    }
}
