package mcjty.rftoolsbuilder.modules.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.BaseBlockItem;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.blocks.SupportBlock;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardType;
import mcjty.rftoolsbuilder.modules.builder.items.SuperHarvestingTool;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class BuilderSetup {
//    public static SpaceChamberBlock spaceChamberBlock;
//    public static SpaceChamberControllerBlock spaceChamberControllerBlock;
//    public static BaseBlock composerBlock;
//    public static BaseBlock scannerBlock;
//    public static BaseBlock remoteScannerBlock;
//    public static BaseBlock projectorBlock;
//    public static BaseBlock locatorBlock;

    @ObjectHolder("rftoolsbuilder:support_block")
    public static SupportBlock SUPPORT;

    @ObjectHolder("rftoolsbuilder:builder")
    public static BaseBlock BUILDER;

    @ObjectHolder("rftoolsbuilder:builder")
    public static TileEntityType<?> TYPE_BUILDER;

    @ObjectHolder("rftoolsbuilder:builder")
    public static ContainerType<GenericContainer> CONTAINER_BUILDER;

//    @ObjectHolder("rftools:composer")
//    public static TileEntityType<?> TYPE_COMPOSER;

//    @ObjectHolder("rftools:scanner")
//    public static TileEntityType<?> TYPE_SCANNER;

//    @ObjectHolder("rftools:locator")
//    public static TileEntityType<?> TYPE_LOCATOR;

//    @ObjectHolder("rftools:projector")
//    public static TileEntityType<?> TYPE_PROJECTOR;

//    @ObjectHolder("rftools:space_chamber")
//    public static TileEntityType<?> TYPE_SPACE_CHAMBER;

//    @ObjectHolder("rftools:modifier")
//    public static ContainerType<GenericContainer> CONTAINER_MODIFIER;

    //    public static SpaceChamberCardItem spaceChamberCardItem;
    @ObjectHolder("rftoolsbuilder:space_chamber_card")
    public static Item SPACE_CHAMBER_CARD;    // @todo 1.14

    @ObjectHolder("rftoolsbuilder:shape_card_def")
    public static ShapeCardItem SHAPE_CARD_DEF;
    @ObjectHolder("rftoolsbuilder:shape_card_liquid")
    public static ShapeCardItem SHAPE_CARD_LIQUID;
    @ObjectHolder("rftoolsbuilder:shape_card_pump")
    public static ShapeCardItem SHAPE_CARD_PUMP;
    @ObjectHolder("rftoolsbuilder:shape_card_pump_clear")
    public static ShapeCardItem SHAPE_CARD_PUMP_CLEAR;
    @ObjectHolder("rftoolsbuilder:shape_card_quarry")
    public static ShapeCardItem SHAPE_CARD_QUARRY;
    @ObjectHolder("rftoolsbuilder:shape_card_quarry_clear")
    public static ShapeCardItem SHAPE_CARD_QUARRY_CLEAR;
    @ObjectHolder("rftoolsbuilder:shape_card_quarry_clear_fortune")
    public static ShapeCardItem SHAPE_CARD_QUARRY_CLEAR_FORTUNE;
    @ObjectHolder("rftoolsbuilder:shape_card_quarry_clear_silk")
    public static ShapeCardItem SHAPE_CARD_QUARRY_CLEAR_SILK;
    @ObjectHolder("rftoolsbuilder:shape_card_quarry_fortune")
    public static ShapeCardItem SHAPE_CARD_QUARRY_FORTUNE;
    @ObjectHolder("rftoolsbuilder:shape_card_quarry_silk")
    public static ShapeCardItem SHAPE_CARD_QUARRY_SILK;
    @ObjectHolder("rftoolsbuilder:shape_card_void")
    public static ShapeCardItem SHAPE_CARD_VOID;

    @ObjectHolder("rftoolsbuilder:superharvestingtool")
    public static Item SUPER_HARVESTING_TOOL;

    private static Map<String,BlockInformation> blockInformationMap = new HashMap<>();

    public static void registerBlocks(final RegistryEvent.Register<Block> event) {
        event.getRegistry().register(BuilderTileEntity.createBlock());
        event.getRegistry().register(new SupportBlock());
    }

    public static void registerItems(final RegistryEvent.Register<Item> event) {
        for (ShapeCardType type : ShapeCardType.values()) {
            if (type.isItem()) {
                event.getRegistry().register(new ShapeCardItem(type));
            }
        }
        Item.Properties properties = new Item.Properties().group(RFToolsBuilder.setup.getTab());
        event.getRegistry().register(new BaseBlockItem(BUILDER, properties));
        event.getRegistry().register(new SuperHarvestingTool());
    }

    public static void registerTiles(final RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(TileEntityType.Builder.create(BuilderTileEntity::new, BUILDER).build(null).setRegistryName("builder"));
    }

    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().register(GenericContainer.createContainerType("builder"));
    }

    // @todo 1.14 call me?
    public static void init() {
        readBuilderBlocksInternal();
        readBuilderBlocksConfig();
    }

//    @SideOnly(Side.CLIENT)
//    public static void initClient() {
//        spaceChamberBlock.initModel();
//        spaceChamberControllerBlock.initModel();
//
//        builderBlock.initModel();
//        builderBlock.setGuiFactory(GuiBuilder::new);
//        BuilderRenderer.register();
//
//        supportBlock.initModel();
//
//        composerBlock.initModel();
//        composerBlock.setGuiFactory(GuiComposer::new);
//
//        scannerBlock.initModel();
//        scannerBlock.setGuiFactory(GuiScanner::new);
//
//        remoteScannerBlock.initModel();
//        remoteScannerBlock.setGuiFactory(GuiScanner::new);
//
//        projectorBlock.initModel();
//        projectorBlock.setGuiFactory(GuiProjector::new);
//        ProjectorRenderer.register();
//
//        locatorBlock.initModel();
//        locatorBlock.setGuiFactory(GuiLocator::new);
//
//        spaceChamberCardItem.initModel();
//        shapeCardItem.initModel();
//    }


    private static void readBuilderBlocksInternal() {
        try(InputStream inputstream = RFToolsBuilder.class.getResourceAsStream("/data/rftoolsbuilder/text/builder.json")) {
            parseBuilderJson(inputstream);
        } catch (IOException e) {
            Logging.logError("Error reading builder.json", e);
        }
    }

    private static void readBuilderBlocksConfig() {
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
                old = BlockInformation.OK;
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
                old = BlockInformation.OK;
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

    public static class BlockInformation {
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
}