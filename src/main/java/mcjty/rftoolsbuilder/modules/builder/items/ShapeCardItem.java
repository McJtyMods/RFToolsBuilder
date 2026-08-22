package mcjty.rftoolsbuilder.modules.builder.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.crafting.IComponentsToPreserve;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.*;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderConfiguration;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.client.GuiShapeCard;
import mcjty.rftoolsbuilder.modules.builder.data.ShapeCardData;
import mcjty.rftoolsbuilder.modules.builder.data.ShapeCardData.ShapeCardChild;
import mcjty.rftoolsbuilder.modules.builder.data.ShapeCardData.ShapeModifierData;
import mcjty.rftoolsbuilder.shapes.IFormula;
import mcjty.rftoolsbuilder.shapes.ScanDataManager;
import mcjty.rftoolsbuilder.shapes.Shape;
import mcjty.rftoolsbuilder.shapes.ShapeModifier;
import mcjty.rftoolsbuilder.shapes.ShapeOperation;
import mcjty.rftoolsbuilder.shapes.ShapeRotation;
import mcjty.rftoolsbuilder.shapes.StatePalette;
import mcjty.lib.gui.ManualEntry;
import mcjty.rftoolsbase.tools.ManualHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.Lazy;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

import static mcjty.lib.builder.TooltipBuilder.*;
import static mcjty.rftoolsbuilder.modules.builder.data.ShapeCardData.*;

public class ShapeCardItem extends Item implements IComponentsToPreserve, ITooltipSettings {

    private final ShapeCardType type;

    private final Lazy<TooltipBuilder> tooltipBuilder = Lazy.of(() -> new TooltipBuilder()
            .info(key("message.rftoolsbuilder.shiftmessage"))
            .infoShift(warning(stack -> isDisabledInConfig()),
                    header(),
                    parameter("shape", this::getShapeDescription),
                    parameter("dimension", this::getShapeDimension),
                    parameter("offset", this::getShapeOffset),
                    parameter("formulas", stack -> getShape(stack).isComposition(),
                            stack -> Integer.toString(getChildren(stack).size())),
                    parameter("scan", stack -> getShape(stack).isScan(),
                            stack -> Integer.toString(getScanId(stack)))
            ));

    public static final int MAXIMUM_COUNT = 50000000;

    public ShapeCardItem(ShapeCardType type) {
        super(RFToolsBuilder.setup.defaultProperties().stacksTo(1).durability(0));
        this.type = type;
    }

    public boolean isDisabledInConfig() {
        if (!BuilderConfiguration.shapeCardAllowed.get()) {
            return true;
        } else if (type != ShapeCardType.CARD_SHAPE) {
            if (!BuilderConfiguration.quarryAllowed.get()) {
                return true;
            } else if (type.isQuarry() && type.isClearing()) {
                if (!BuilderConfiguration.clearingQuarryAllowed.get()) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getShapeDescription(ItemStack itemStack) {
        Shape shape = getShape(itemStack);
        boolean issolid = isSolid(itemStack);
        return shape.getDescription() + " (" + (issolid ? "Solid" : "Hollow") + ")";
    }

    public String getShapeDimension(ItemStack itemStack) {
        Shape shape = getShape(itemStack);
        boolean issolid = isSolid(itemStack);
        return  BlockPosTools.toString(getDimension(itemStack));
    }

    public String getShapeOffset(ItemStack itemStack) {
        Shape shape = getShape(itemStack);
        boolean issolid = isSolid(itemStack);
        return BlockPosTools.toString(getOffset(itemStack));
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        Player player = context.getPlayer();
        if (!world.isClientSide && player != null) {
            InteractionHand hand = context.getHand();
            BlockPos pos = context.getClickedPos();
            ItemStack stack = context.getItemInHand();
            int mode = getMode(stack);
            if (mode == MODE_NONE) {
                if (player.isShiftKeyDown()) {
                    if (world.getBlockEntity(pos) instanceof BuilderTileEntity) {
                        setCurrentBlock(stack, GlobalPos.of(world.dimension(), pos));
                        Logging.message(player, ChatFormatting.GREEN + "Now select the first corner");
                        setMode(stack, MODE_CORNER1);
                        setCorner1(stack, null);
                    } else {
                        Logging.message(player, ChatFormatting.RED + "You can only do this on a builder!");
                    }
                } else {
                    return InteractionResult.SUCCESS;
                }
            } else if (mode == MODE_CORNER1) {
                GlobalPos currentBlock = getCurrentBlock(stack);
                if (currentBlock == null) {
                    Logging.message(player, ChatFormatting.RED + "There is no Builder selected!");
                } else if (!currentBlock.dimension().equals(world.dimension())) {
                    Logging.message(player, ChatFormatting.RED + "The Builder is in another dimension!");
                } else if (currentBlock.pos().equals(pos)) {
                    Logging.message(player, ChatFormatting.RED + "Cleared area selection mode!");
                    setMode(stack, MODE_NONE);
                } else {
                    Logging.message(player, ChatFormatting.GREEN + "Now select the second corner");
                    setMode(stack, MODE_CORNER2);
                    setCorner1(stack, pos);
                }
            } else {
                GlobalPos currentBlock = getCurrentBlock(stack);
                if (currentBlock == null) {
                    Logging.message(player, ChatFormatting.RED + "There is no Builder selected!");
                } else if (!currentBlock.dimension().equals(world.dimension())) {
                    Logging.message(player, ChatFormatting.RED + "The Builder is in another dimension!");
                } else if (currentBlock.pos().equals(pos)) {
                    Logging.message(player, ChatFormatting.RED + "Cleared area selection mode!");
                    setMode(stack, MODE_NONE);
                } else {
                    BlockPos c1 = getCorner1(stack);
                    if (c1 == null) {
                        Logging.message(player, ChatFormatting.RED + "Cleared area selection mode!");
                        setMode(stack, MODE_NONE);
                    } else {
                        Logging.message(player, ChatFormatting.GREEN + "New settings copied to the shape card!");
                        BlockPos center = new BlockPos((int) Math.ceil((c1.getX() + pos.getX()) / 2.0f), (int) Math.ceil((c1.getY() + pos.getY()) / 2.0f), (int) Math.ceil((c1.getZ() + pos.getZ()) / 2.0f));
                        setDimension(stack, Math.abs(c1.getX() - pos.getX()) + 1, Math.abs(c1.getY() - pos.getY()) + 1, Math.abs(c1.getZ() - pos.getZ()) + 1);
                        setOffset(stack, center.getX() - currentBlock.pos().getX(), center.getY() - currentBlock.pos().getY(), center.getZ() - currentBlock.pos().getZ());

                        setMode(stack, MODE_NONE);
                        setCorner1(stack, null);
                        setShape(stack, Shape.SHAPE_BOX, true);
                    }
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public Collection<DataComponentType<?>> getComponentsToPreserve() {
        return List.of(BuilderModule.ITEM_SHAPECARD_DATA.get());
    }

    public static void setData(ItemStack card, int scanID) {
        ShapeCardData data = card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT);
        card.set(BuilderModule.ITEM_SHAPECARD_DATA.get(), data.withScanId(scanID));
    }

    public static void setModifier(ItemStack card, ShapeModifier modifier) {
        ShapeCardData data = card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT);
        ShapeModifierData modifierData = new ShapeModifierData(
                modifier.getOperation().getCode(),
                modifier.isFlipY(),
                modifier.getRotation().getCode());
        card.set(BuilderModule.ITEM_SHAPECARD_DATA.get(), data.withModifier(modifierData));
    }

    public static void setGhostMaterial(ItemStack card, ItemStack materialGhost) {
        ShapeCardData data = card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT);
        if (materialGhost.isEmpty()) {
            card.set(BuilderModule.ITEM_SHAPECARD_DATA.get(), data.withGhostBlock(Optional.empty()));
        } else {
            Block block = Block.byItem(materialGhost.getItem());
            if (block == Blocks.AIR) {
                card.set(BuilderModule.ITEM_SHAPECARD_DATA.get(), data.withGhostBlock(Optional.empty()));
            } else {
                card.set(BuilderModule.ITEM_SHAPECARD_DATA.get(), data.withGhostBlock(Optional.of(Tools.getId(block))));
            }
        }
    }

    public static void setChildren(ItemStack card, List<ShapeCardChild> children) {
        ShapeCardData data = card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT);
        card.set(BuilderModule.ITEM_SHAPECARD_DATA.get(), data.withChildren(children));
    }

    public static void setDimension(ItemStack card, int x, int y, int z) {
        ShapeCardData data = card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT);
        card.set(BuilderModule.ITEM_SHAPECARD_DATA.get(), data.withDimension(new BlockPos(x, y, z)));
    }


    public static void setOffset(ItemStack card, int x, int y, int z) {
        ShapeCardData data = card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT);
        card.set(BuilderModule.ITEM_SHAPECARD_DATA.get(), data.withOffset(new BlockPos(x, y, z)));
    }

    public static void setCorner1(ItemStack card, BlockPos corner) {
        ShapeCardData data = card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT);
        card.set(BuilderModule.ITEM_SHAPECARD_DATA.get(), data.withCorner(corner));
    }

    public static BlockPos getCorner1(ItemStack card) {
        return card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT).dimensions().corner1();
    }

    public static int getMode(ItemStack card) {
        int mode = card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), DEFAULT).dimensions().mode();
        if (mode != MODE_NONE) {
            GlobalPos block = getCurrentBlock(card);
            if (block == null) {
                // Safety: if there is no selected block we consider mode to be NONE
                return MODE_NONE;
            }
        }
        return mode;
    }

    public static void setMode(ItemStack card, int mode) {
        ShapeCardData data = card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT);
        card.set(BuilderModule.ITEM_SHAPECARD_DATA.get(), data.withMode(mode));
    }

    public static void setCurrentBlock(ItemStack card, GlobalPos c) {
        ShapeCardData data = card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT);
        card.set(BuilderModule.ITEM_SHAPECARD_DATA.get(), data.withSelected(c));
    }

    @Nullable
    private static GlobalPos getCurrentBlock(ItemStack card) {
        return card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT).dimensions().selected();
    }


    @Override
    public void appendHoverText(@Nonnull ItemStack itemStack, TooltipContext context, @Nonnull List<Component> list, @Nonnull TooltipFlag flag) {
        super.appendHoverText(itemStack, context, list, flag);
        // Use custom RL so that we don't have to duplicate the translation for every shape card
        tooltipBuilder.get().makeTooltip(ResourceLocation.fromNamespaceAndPath(RFToolsBuilder.MODID, "shape_card"), itemStack, list, flag);
    }

    /**
     * Return true if the card is a normal card (not a quarry or void card)
     */
    public static boolean isNormalShapeCard(ItemStack card) {
        ShapeCardType type = getType(card);
        return type == ShapeCardType.CARD_SHAPE || type == ShapeCardType.CARD_PUMP_LIQUID;
    }

    public static ShapeCardType getType(ItemStack card) {
        if (card.getItem() instanceof ShapeCardItem) {
            return ((ShapeCardItem) card.getItem()).type;
        }
        if (card.getItem() == BuilderModule.SPACE_CHAMBER_CARD.get()) {
            return ShapeCardType.CARD_SPACE;
        }
        return ShapeCardType.CARD_UNKNOWN;
    }

    private static void addBlocks(Set<Block> blocks, Block block, TagKey<Block> tag, boolean tagMatching) {
        blocks.add(block);
        if (tagMatching && tag != null) {
            TagTools.getBlocksForTag(tag).forEach(b -> blocks.add(b.value()));
        }
    }

    public static Set<Block> getVoidedBlocks(ItemStack stack) {
        Set<Block> blocks = new HashSet<>();
        boolean tagMatching = isTagMatching(stack);
        if (isVoiding(stack, "stone")) {
            addBlocks(blocks, Blocks.STONE, Tags.Blocks.STONES, tagMatching);
        }
        if (isVoiding(stack, "cobble")) {
            addBlocks(blocks, Blocks.COBBLESTONE, Tags.Blocks.COBBLESTONES, tagMatching);
        }
        if (isVoiding(stack, "dirt")) {
            addBlocks(blocks, Blocks.DIRT, BlockTags.DIRT, tagMatching);
            addBlocks(blocks, Blocks.GRASS_BLOCK, null, tagMatching);
        }
        if (isVoiding(stack, "sand")) {
            addBlocks(blocks, Blocks.SAND, Tags.Blocks.SANDS, tagMatching);
        }
        if (isVoiding(stack, "gravel")) {
            addBlocks(blocks, Blocks.GRAVEL, Tags.Blocks.GRAVELS, tagMatching);
        }
        if (isVoiding(stack, "netherrack")) {
            addBlocks(blocks, Blocks.NETHERRACK, Tags.Blocks.NETHERRACKS, tagMatching);
        }
        if (isVoiding(stack, "endstone")) {
            addBlocks(blocks, Blocks.END_STONE, Tags.Blocks.END_STONES, tagMatching);
        }
        return blocks;
    }

    public static boolean isTagMatching(ItemStack card) {
        return card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT).tagMatching();
    }

    public static void setTagMatching(ItemStack card, boolean tagMatching) {
        ShapeCardData data = card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT);
        card.set(BuilderModule.ITEM_SHAPECARD_DATA.get(), data.withTagMatching(tagMatching));
    }

    public static boolean isVoiding(ItemStack card, String material) {
        return card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT).voiding().contains(material);
    }

    public static void addVoiding(ItemStack card, String material) {
        ShapeCardData data = card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT);
        card.set(BuilderModule.ITEM_SHAPECARD_DATA.get(), data.addVoiding(material));
    }

    public static void clearVoiding(ItemStack card) {
        ShapeCardData data = card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT);
        card.set(BuilderModule.ITEM_SHAPECARD_DATA.get(), data.withVoiding(new HashSet<>()));
    }

    public static Shape getShape(ItemStack card) {
        return card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT).shape();
    }

    public static boolean isSolid(ItemStack card) {
        if (card.isEmpty()) {
            return true;
        }
        return card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT).solid();
    }

    public static IFormula createCorrectFormula(CompoundTag tagCompound) {
        return Shape.SHAPE_BOX.getFormulaFactory().get();
    }

    public static IFormula createCorrectFormula(ItemStack stack) {
        Shape shape = getShape(stack);
        boolean solid = isSolid(stack);
        IFormula formula = shape.getFormulaFactory().get();
        return formula.correctFormula(solid);
    }

    public static int getScanId(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        Shape shape = getShape(stack);
        if (shape != Shape.SHAPE_SCAN) {
            return 0;
        }
        return stack.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT).scanId();
    }

    public static int getScanIdRecursive(ItemStack card) {
        Shape shape = getShape(card);
        int scanId = getScanId(card);
        if (scanId != 0) {
            return scanId;
        }
        if (shape == Shape.SHAPE_COMPOSITION) {
            for (ShapeCardChild child : getChildren(card)) {
                int id = getScanIdRecursive(child.stack());
                if (id != 0) {
                    return id;
                }
            }
        }
        return 0;
    }

    public static int getFormulaCheckClient(ItemStack stack) {
        Check32 crc = new Check32();
        getFormulaCheckClient(stack, crc);
        return crc.get();
    }

    public static void getFormulaCheckClient(ItemStack stack, Check32 crc) {
        Shape shape = getShape(stack);
        IFormula formula = shape.getFormulaFactory().get();
        formula.getCheckSumClient(stack, crc);
    }

    public static void getLocalChecksum(ItemStack card, Check32 crc) {
        if (card.isEmpty()) {
            return;
        }
        ShapeCardData data = card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT);
        crc.add(data.shape().ordinal());
        BlockPos dim = data.dimensions().dimension();
        crc.add(dim.getX());
        crc.add(dim.getY());
        crc.add(dim.getZ());
        BlockPos offset = data.dimensions().offset();
        crc.add(offset.getX());
        crc.add(offset.getY());
        crc.add(offset.getZ());
        crc.add(data.solid() ? 1 : 0);
        crc.add(data.scanId());
    }

    public static List<ShapeCardChild> getChildren(ItemStack card) {
        return card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT).children();
    }

    public static ShapeModifier getModifier(ItemStack card) {
        ShapeModifierData data = card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT).modifier();
        ShapeOperation operation = ShapeOperation.getByName(data.operation());
        if (operation == null) {
            operation = ShapeOperation.UNION;
        }
        ShapeRotation rotation = ShapeRotation.getByName(data.rotation());
        if (rotation == null) {
            rotation = ShapeRotation.NONE;
        }
        return new ShapeModifier(operation, data.flipY(), rotation);
    }

    public static Optional<ResourceLocation> getGhostBlock(ItemStack card) {
        return card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT).ghostBlock();
    }



    public static void setShape(ItemStack stack, Shape shape, boolean solid) {
        ShapeCardData data = stack.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT);
        stack.set(BuilderModule.ITEM_SHAPECARD_DATA.get(), data.withShape(shape).withSolid(solid));
    }

    public static BlockPos getDimension(ItemStack stack) {
        return stack.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT).dimensions().dimension();
    }

    public static BlockPos getClampedDimension(ItemStack card, int maximum) {
        BlockPos dimension = getDimension(card);
        return new BlockPos(clampDimension(dimension.getX(), maximum), clampDimension(dimension.getY(), maximum), clampDimension(dimension.getZ(), maximum));
    }

    // Shape preview and saved shape data are generated from a dimension clamped to these
    // limits. Every site that generates, sizes or decodes that data has to agree on the
    // clamp, otherwise the producer and the consumer disagree about how big a plane is.
    public static final int MAX_SHAPE_DATA_HORIZONTAL = 512;
    public static final int MAX_SHAPE_DATA_VERTICAL = 4096;

    public static BlockPos getShapeDataDimension(ItemStack card) {
        BlockPos dimension = getDimension(card);
        return new BlockPos(
                clampDimension(dimension.getX(), MAX_SHAPE_DATA_HORIZONTAL),
                clampDimension(dimension.getY(), MAX_SHAPE_DATA_VERTICAL),
                clampDimension(dimension.getZ(), MAX_SHAPE_DATA_HORIZONTAL));
    }

    private static int clampDimension(int o, int maximum) {
        if (o > maximum) {
            o = maximum;
        } else if (o < 0) {
            o = 0;
        }
        return o;
    }

    public static BlockPos getOffset(ItemStack card) {
        return card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT).dimensions().offset();
    }

    public static BlockPos getClampedOffset(ItemStack card, int maximum) {
        BlockPos offset = getOffset(card);
        return new BlockPos(clampOffset(offset.getX(), maximum), clampOffset(offset.getY(), maximum), clampOffset(offset.getZ(), maximum));
    }

    private static int clampOffset(int o, int maximum) {
        if (o < -maximum) {
            o = -maximum;
        } else if (o > maximum) {
            o = maximum;
        }
        return o;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) {
            GuiShapeCard.open(false);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    public static BlockPos getMinCorner(BlockPos thisCoord, BlockPos dimension, BlockPos offset) {
        int xCoord = thisCoord.getX();
        int yCoord = thisCoord.getY();
        int zCoord = thisCoord.getZ();
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();
        return new BlockPos(xCoord - dx/2 + offset.getX(), yCoord - dy/2 + offset.getY(), zCoord - dz/2 + offset.getZ());
    }

    public static BlockPos getMaxCorner(BlockPos thisCoord, BlockPos dimension, BlockPos offset) {
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();
        BlockPos minCorner = getMinCorner(thisCoord, dimension, offset);
        return new BlockPos(minCorner.getX() + dx, minCorner.getY() + dy, minCorner.getZ() + dz);
    }

    public static boolean xInChunk(int x, ChunkPos chunk) {
        if (chunk == null) {
            return true;
        } else {
            return chunk.x == (x>>4);
        }
    }

    public static boolean zInChunk(int z, ChunkPos chunk) {
        if (chunk == null) {
            return true;
        } else {
            return chunk.z == (z>>4);
        }
    }

    private static void placeBlockIfPossible(Level worldObj, Map<BlockPos, BlockState> blocks, int maxSize, int x, int y, int z, BlockState state, boolean forquarry) {
        BlockPos c = new BlockPos(x, y, z);
        if (worldObj == null) {
            blocks.put(c, state);
            return;
        }
        if (forquarry) {
            if (worldObj.isEmptyBlock(c)) {
                return;
            }
            blocks.put(c, state);
        } else {
            if (BuilderTileEntity.isEmptyOrReplacable(worldObj, c) && blocks.size() < maxSize) {
                blocks.put(c, state);
            }
        }
    }

    public static int getRenderPositions(ItemStack stack, boolean solid, RLE positions, StatePalette statePalette, IFormula formula, int oy) {
        BlockPos clamped = getShapeDataDimension(stack);

        int dx = clamped.getX();
        int dy = clamped.getY();
        int dz = clamped.getZ();

        int cnt = 0;
        int y = oy - dy / 2;
        for (int ox = 0; ox < dx; ox++) {
            int x = ox - dx / 2;
            for (int oz = 0; oz < dz; oz++) {
                int z = oz - dz / 2;
                int v = 255;
                if (formula.isInside(x, y, z)) {
                    cnt++;
                    BlockState lastState = formula.getLastState();
                    if (solid) {
                        if (ox == 0 || ox == dx - 1 || oy == 0 || oy == dy - 1 || oz == 0 || oz == dz - 1) {
                            v = statePalette.alloc(lastState, -1) + 1;
                        } else if (formula.isVisible(x, y, z)) {
                            v = statePalette.alloc(lastState, -1) + 1;
                        }
                    } else {
                        v = statePalette.alloc(lastState, -1) + 1;
                    }
                }
                positions.add(v);
            }
        }
        return cnt;
    }


    // Used for saving
    public static int getDataPositions(Level world, ItemStack stack, Shape shape, boolean solid, RLE positions, StatePalette statePalette) {
        BlockPos clamped = getShapeDataDimension(stack);

        IFormula formula = shape.getFormulaFactory().get();
        int dx = clamped.getX();
        int dy = clamped.getY();
        int dz = clamped.getZ();

        formula = formula.correctFormula(solid);
        formula.setup(world, new BlockPos(0, 0, 0), clamped, new BlockPos(0, 0, 0), stack);

        // For saving shape cards we need to do X/Z/Y (scanner order) instead of the usual Y/X/Z (render order)
        int cnt = 0;
        for (int ox = 0; ox < dx; ox++) {
            int x = ox - dx/2;
            for (int oz = 0; oz < dz; oz++) {
                int z = oz - dz/2;
                for (int oy = 0; oy < dy; oy++) {
                    int y = oy - dy/2;
                    int v = 255;
                    if (formula.isInside(x, y, z)) {
                        cnt++;
                        BlockState lastState = formula.getLastState();
                        if (lastState == null) {
                            lastState = Blocks.STONE.defaultBlockState();
                        }
                        v = statePalette.alloc(lastState, 0) + 1;
                    }
                    positions.add(v);
                }
            }
        }
        return cnt;
    }



    public static void composeFormula(ItemStack shapeCard, IFormula formula, Level worldObj, BlockPos thisCoord, BlockPos dimension, BlockPos offset, Map<BlockPos, BlockState> blocks, int maxSize, boolean solid, boolean forquarry, ChunkPos chunk) {
        int xCoord = thisCoord.getX();
        int yCoord = thisCoord.getY();
        int zCoord = thisCoord.getZ();
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();
        BlockPos tl = new BlockPos(xCoord - dx/2 + offset.getX(), yCoord - dy/2 + offset.getY(), zCoord - dz/2 + offset.getZ());

        formula = formula.correctFormula(solid);
        formula.setup(worldObj, thisCoord, dimension, offset, shapeCard);

        for (int ox = 0 ; ox < dx ; ox++) {
            int x = tl.getX() + ox;
            if (xInChunk(x, chunk)) {
                for (int oz = 0 ; oz < dz ; oz++) {
                    int z = tl.getZ() + oz;
                    if (zInChunk(z, chunk)) {
                        for (int oy = 0; oy < dy; oy++) {
                            int y = tl.getY() + oy;
//                            if (y >= yCoord-dy/2 && y < yCoord+dy/2) {    @todo!!!
                                if (formula.isInside(x, y, z)) {
                                    placeBlockIfPossible(worldObj, blocks, maxSize, x, y, z, formula.getLastState(), forquarry);
                                }
//                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean validFile(Player player, String filename) {
        if (filename.contains("\\") || filename.contains("/") || filename.contains(":")) {
            player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "Invalid filename '" + filename + "'! Cannot be a path!"), false);
            return false;
        }
        return true;
    }


    public static void save(Player player, ItemStack card, String filename) {
        if (!validFile(player, filename)) {
            return;
        }

        Shape shape = ShapeCardItem.getShape(card);
        boolean solid = ShapeCardItem.isSolid(card);
        BlockPos offset = ShapeCardItem.getOffset(card);
        BlockPos dimension = ShapeCardItem.getDimension(card);

        RLE positions = new RLE();
        StatePalette statePalette = new StatePalette();
        int cnt = getDataPositions(player.getCommandSenderWorld(), card, shape, solid, positions, statePalette);

        byte[] data = positions.getData();

        File dataDir = new File("rftoolsscans");
        dataDir.mkdirs();
        File file = new File(dataDir, filename);
        try(PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
            writer.println("SHAPE");
            writer.println("DIM:" + dimension.getX() + "," + dimension.getY() + "," + dimension.getZ());
            writer.println("OFF:" + offset.getX() + "," + offset.getY() + "," + offset.getZ());
            for (BlockState state : statePalette.getPalette()) {
                try {
                    writer.println("NBT:" + encodeState(state));
                } catch (IOException e) {
                    writer.println(Tools.getId(state).toString());
                }
            }
            writer.println("DATA");

            byte[] encoded = Base64.getEncoder().encode(data);
            writer.write(new String(encoded));
        } catch (FileNotFoundException e) {
            player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "Cannot write to file '" + filename + "'!"), false);
            return;
        }
        player.displayClientMessage(ComponentFactory.literal(ChatFormatting.GREEN + "Saved shape to file '" + file.getPath() + "'"), false);
    }

    public static void load(Player player, ItemStack card, String filename) {
        if (!validFile(player, filename)) {
            return;
        }

        Shape shape = ShapeCardItem.getShape(card);

        if (shape != Shape.SHAPE_SCAN) {
            player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "To load a file into this card you need a linked 'scan' type card!"), false);
            return;
        }

        int scanId = getScanId(card);
        if (scanId == 0) {
            player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "This card is not linked to scan data!"), false);
            return;
        }

        File dataDir = new File("rftoolsscans");
        dataDir.mkdirs();
        File file = new File(dataDir, filename);

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String s = reader.readLine();
            if (!"SHAPE".equals(s)) {
                player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "This does not appear to be a valid shapecard file!"), false);
                return;
            }
            s = reader.readLine();
            if (!s.startsWith("DIM:")) {
                player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "This does not appear to be a valid shapecard file!"), false);
                return;
            }
            BlockPos dim = parse(s.substring(4));
            s = reader.readLine();
            if (!s.startsWith("OFF:")) {
                player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "This does not appear to be a valid shapecard file!"), false);
                return;
            }
            BlockPos off = parse(s.substring(4));
            s = reader.readLine();
            StatePalette statePalette = new StatePalette();
            while (!"DATA".equals(s)) {
                if (s.startsWith("NBT:")) {
                    statePalette.add(decodeState(s.substring(4)));
                } else {
                    String[] split = StringUtils.split(s, '@');
                    Block block = Tools.getBlock(ResourceLocation.parse(split[0]));
                    if (block == null) {
                        player.displayClientMessage(ComponentFactory.literal(ChatFormatting.YELLOW + "Could not find block '" + split[0] + "'!"), false);
                        block = Blocks.STONE;
                    }
                    statePalette.add(block.defaultBlockState());
                }
                s = reader.readLine();
            }
            s = reader.readLine();
            byte[] decoded = Base64.getDecoder().decode(s.getBytes());

            setDataFromFile(player.getCommandSenderWorld(), scanId, card, dim, off, decoded, statePalette);
        } catch (IOException e) {
            player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "Cannot read from file '" + filename + "'!"), false);
            return;
        } catch (NullPointerException e) {
            player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "File '" + filename + "' is too short!"), false);
            return;
        } catch (ArrayIndexOutOfBoundsException e) {
            player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "File '" + filename + "' contains invalid entries!"), false);
            return;
        }
        player.displayClientMessage(ComponentFactory.literal(ChatFormatting.GREEN + "Loaded shape from file '" + file.getPath() + "'"), false);
    }

    private static void setDataFromFile(Level world, int scanId, ItemStack card, BlockPos dimension, BlockPos offset, byte[] data, StatePalette palette) {
        ScanDataManager scans = ScanDataManager.get(world);
        scans.getOrCreateScan(scanId).setData(data, palette.getPalette(), dimension, offset);
        scans.save(world, scanId);
        ShapeCardItem.setDimension(card, dimension.getX(), dimension.getY(), dimension.getZ());
        ShapeCardItem.setOffset(card, offset.getX(), offset.getY(), offset.getZ());
        ShapeCardItem.setShape(card, Shape.SHAPE_SCAN, true);
    }

    private static String encodeState(BlockState state) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        NbtIo.writeCompressed(NbtUtils.writeBlockState(state), output);
        return Base64.getEncoder().encodeToString(output.toByteArray());
    }

    private static BlockState decodeState(String encoded) throws IOException {
        byte[] decoded = Base64.getDecoder().decode(encoded);
        CompoundTag tag = NbtIo.readCompressed(new ByteArrayInputStream(decoded), NbtAccounter.unlimitedHeap());
        return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag);
    }


    private static BlockPos parse(String s) {
        String[] split = StringUtils.split(s, ',');
        return new BlockPos(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    }

    @Override
    public ManualEntry getManualEntry() {
        return switch (type) {
            case CARD_VOID -> ManualHelper.create("rftoolsbuilder:shape_cards/shape_card_def_void");
            case CARD_QUARRY, CARD_QUARRY_CLEAR, CARD_QUARRY_CLEAR_SILK, CARD_QUARRY_CLEAR_FORTUNE, CARD_QUARRY_FORTUNE, CARD_QUARRY_SILK ->
                    ManualHelper.create("rftoolsbuilder:shape_cards/shape_card_def_quarry");
            case CARD_PUMP, CARD_PUMP_CLEAR -> ManualHelper.create("rftoolsbuilder:shape_cards/shape_card_pump");
            case CARD_PUMP_LIQUID -> ManualHelper.create("rftoolsbuilder:shape_cards/shape_card_liquid");
            default -> ManualHelper.create("rftoolsbuilder:shape_cards/shape_card_def");
        };
    }

}
