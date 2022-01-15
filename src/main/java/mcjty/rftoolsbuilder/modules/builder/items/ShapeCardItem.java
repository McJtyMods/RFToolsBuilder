package mcjty.rftoolsbuilder.modules.builder.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.crafting.INBTPreservingIngredient;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.*;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderConfiguration;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.client.GuiShapeCard;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldProjectorTileEntity;
import mcjty.rftoolsbuilder.shapes.IFormula;
import mcjty.rftoolsbuilder.shapes.Shape;
import mcjty.rftoolsbuilder.shapes.ShapeModifier;
import mcjty.rftoolsbuilder.shapes.StatePalette;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tags.ITag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

import static mcjty.lib.builder.TooltipBuilder.*;

public class ShapeCardItem extends Item implements INBTPreservingIngredient, ITooltipSettings {

    private final ShapeCardType type;

    private final Lazy<TooltipBuilder> tooltipBuilder = () -> new TooltipBuilder()
            .info(key("message.rftoolsbuilder.shiftmessage"))
            .infoShift(warning(stack -> isDisabledInConfig()),
                    header(),
                    parameter("shape", this::getShapeDescription),
                    parameter("dimension", this::getShapeDimension),
                    parameter("offset", this::getShapeOffset),
                    parameter("formulas", stack -> getShape(stack).isComposition(),
                            stack -> {
                                CompoundNBT card = stack.getTag();
                                if (card != null) {
                                    ListNBT children = card.getList("children", Constants.NBT.TAG_COMPOUND);
                                    return Integer.toString(children.size());
                                }
                                return "<none>";
                            }),
                    parameter("scan", stack -> getShape(stack).isScan(),
                            stack -> {
                                CompoundNBT card = stack.getTag();
                                if (card != null) {
                                    int scanid = card.getInt("scanid");
                                    return Integer.toString(scanid);
                                }
                                return "<none>";
                            })
            );

    public static final int MAXIMUM_COUNT = 50000000;

    public static final int MODE_NONE = 0;
    public static final int MODE_CORNER1 = 1;
    public static final int MODE_CORNER2 = 2;

    public ShapeCardItem(ShapeCardType type) {
        super(new Properties().stacksTo(1).defaultDurability(0).tab(RFToolsBuilder.setup.getTab()));
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
    public ActionResultType useOn(ItemUseContext context) {
        World world = context.getLevel();
        PlayerEntity player = context.getPlayer();
        if (!world.isClientSide && player != null) {
            Hand hand = context.getHand();
            BlockPos pos = context.getClickedPos();
            ItemStack stack = context.getItemInHand();
            int mode = getMode(stack);
            if (mode == MODE_NONE) {
                if (player.isShiftKeyDown()) {
                    if (world.getBlockEntity(pos) instanceof BuilderTileEntity || world.getBlockEntity(pos) instanceof ShieldProjectorTileEntity) {
                        setCurrentBlock(stack, GlobalPos.of(world.dimension(), pos));
                        Logging.message(player, TextFormatting.GREEN + "Now select the first corner");
                        setMode(stack, MODE_CORNER1);
                        setCorner1(stack, null);
                    } else {
                        Logging.message(player, TextFormatting.RED + "You can only do this on a builder or shield Projector!");
                    }
                } else {
                    return ActionResultType.SUCCESS;
                }
            } else if (mode == MODE_CORNER1) {
                GlobalPos currentBlock = getCurrentBlock(stack);
                if (currentBlock == null) {
                    Logging.message(player, TextFormatting.RED + "There is no Builder selected!");
                } else if (!currentBlock.dimension().equals(world.dimension())) {
                    Logging.message(player, TextFormatting.RED + "The Builder is in another dimension!");
                } else if (currentBlock.pos().equals(pos)) {
                    Logging.message(player, TextFormatting.RED + "Cleared area selection mode!");
                    setMode(stack, MODE_NONE);
                } else {
                    Logging.message(player, TextFormatting.GREEN + "Now select the second corner");
                    setMode(stack, MODE_CORNER2);
                    setCorner1(stack, pos);
                }
            } else {
                GlobalPos currentBlock = getCurrentBlock(stack);
                if (currentBlock == null) {
                    Logging.message(player, TextFormatting.RED + "There is no Builder selected!");
                } else if (!currentBlock.dimension().equals(world.dimension())) {
                    Logging.message(player, TextFormatting.RED + "The Builder is in another dimension!");
                } else if (currentBlock.pos().equals(pos)) {
                    Logging.message(player, TextFormatting.RED + "Cleared area selection mode!");
                    setMode(stack, MODE_NONE);
                } else {
                    CompoundNBT tag = stack.getOrCreateTag();
                    BlockPos c1 = getCorner1(stack);
                    if (c1 == null) {
                        Logging.message(player, TextFormatting.RED + "Cleared area selection mode!");
                        setMode(stack, MODE_NONE);
                    } else {
                        Logging.message(player, TextFormatting.GREEN + "New settings copied to the shape card!");
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
        return ActionResultType.SUCCESS;
    }

    @Override
    public Collection<String> getTagsToPreserve() {
        return Arrays.asList("mod_op", "mod_flipy", "mod_rot", "ghost_block", "children", "dimX", "dimY", "dimZ",
                "offsetX", "offsetY", "offsetZ", "mode", "selectedX", "selectedY", "selectedZ", "selectedDim",
                "corner1x", "corner1y", "corner1z");
    }

    public static void setData(CompoundNBT tagCompound, int scanID) {
        tagCompound.putInt("scanid", scanID);
    }

    public static void setModifier(CompoundNBT tag, ShapeModifier modifier) {
        tag.putString("mod_op", modifier.getOperation().getCode());
        tag.putBoolean("mod_flipy", modifier.isFlipY());
        tag.putString("mod_rot", modifier.getRotation().getCode());
    }

    public static void setGhostMaterial(CompoundNBT tag, ItemStack materialGhost) {
        if (materialGhost.isEmpty()) {
            tag.remove("ghost_block");
//            tag.remove("ghost_meta");         // @todo 1.14 not more meta
        } else {
            Block block = Block.byItem(materialGhost.getItem());
            tag.putString("ghost_block", block.getRegistryName().toString());
//                tag.putInt("ghost_meta", materialGhost.getMetadata());        // @todo 1.14 no more meta
        }
    }

    public static void setChildren(ItemStack itemStack, ListNBT list) {
        CompoundNBT tagCompound = itemStack.getOrCreateTag();
        tagCompound.put("children", list);
    }

    public static void setDimension(ItemStack itemStack, int x, int y, int z) {
        CompoundNBT tagCompound = itemStack.getOrCreateTag();
        if (tagCompound.getInt("dimX") == x && tagCompound.getInt("dimY") == y && tagCompound.getInt("dimZ") == z) {
            return;
        }
        tagCompound.putInt("dimX", x);
        tagCompound.putInt("dimY", y);
        tagCompound.putInt("dimZ", z);
    }


    public static void setOffset(ItemStack itemStack, int x, int y, int z) {
        CompoundNBT tagCompound = itemStack.getOrCreateTag();
        if (tagCompound.getInt("offsetX") == x && tagCompound.getInt("offsetY") == y && tagCompound.getInt("offsetZ") == z) {
            return;
        }
        tagCompound.putInt("offsetX", x);
        tagCompound.putInt("offsetY", y);
        tagCompound.putInt("offsetZ", z);
    }

    public static void setCorner1(ItemStack itemStack, BlockPos corner) {
        CompoundNBT tagCompound = itemStack.getOrCreateTag();
        if (corner == null) {
            tagCompound.remove("corner1x");
            tagCompound.remove("corner1y");
            tagCompound.remove("corner1z");
        } else {
            tagCompound.putInt("corner1x", corner.getX());
            tagCompound.putInt("corner1y", corner.getY());
            tagCompound.putInt("corner1z", corner.getZ());
        }
    }

    public static BlockPos getCorner1(ItemStack stack1) {
        CompoundNBT tagCompound = stack1.getTag();
        if (tagCompound == null) {
            return null;
        }
        if (!tagCompound.contains("corner1x")) {
            return null;
        }
        return new BlockPos(tagCompound.getInt("corner1x"), tagCompound.getInt("corner1y"), tagCompound.getInt("corner1z"));
    }

    public static int getMode(ItemStack itemStack) {
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            int mode = tagCompound.getInt("mode");
            GlobalPos block = getCurrentBlock(itemStack);
            if (block == null) {
                // Safety: if there is no selected block we consider mode to be NONE
                return MODE_NONE;
            }
            return mode;
        } else {
            return MODE_NONE;
        }
    }

    public static void setMode(ItemStack itemStack, int mode) {
        CompoundNBT tagCompound = itemStack.getOrCreateTag();
        if (tagCompound.getInt("mode") == mode) {
            return;
        }
        tagCompound.putInt("mode", mode);
    }

    public static void setCurrentBlock(ItemStack itemStack, GlobalPos c) {
        CompoundNBT tagCompound = itemStack.getOrCreateTag();

        if (c == null) {
            tagCompound.remove("selectedX");
            tagCompound.remove("selectedY");
            tagCompound.remove("selectedZ");
            tagCompound.remove("selectedDim");
        } else {
            tagCompound.putInt("selectedX", c.pos().getX());
            tagCompound.putInt("selectedY", c.pos().getY());
            tagCompound.putInt("selectedZ", c.pos().getZ());
            tagCompound.putString("selectedDim", c.dimension().location().toString());
        }
    }

    @Nullable
    private static GlobalPos getCurrentBlock(ItemStack itemStack) {
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null && tagCompound.contains("selectedX")) {
            int x = tagCompound.getInt("selectedX");
            int y = tagCompound.getInt("selectedY");
            int z = tagCompound.getInt("selectedZ");
            String dim = tagCompound.getString("selectedDim");
            return GlobalPos.of(LevelTools.getId(dim), new BlockPos(x, y, z));
        }
        return null;
    }


    @Override
    public void appendHoverText(@Nonnull ItemStack itemStack, World world, @Nonnull List<ITextComponent> list, @Nonnull ITooltipFlag flag) {
        super.appendHoverText(itemStack, world, list, flag);
        // Use custom RL so that we don't have to duplicate the translation for every shape card
        tooltipBuilder.get().makeTooltip(new ResourceLocation(RFToolsBuilder.MODID, "shape_card"), itemStack, list, flag);
    }

    /**
     * Return true if the card is a normal card (not a quarry or void card)
     * @param stack
     * @return
     */
    public static boolean isNormalShapeCard(ItemStack stack) {
        ShapeCardType type = getType(stack);
        return type == ShapeCardType.CARD_SHAPE || type == ShapeCardType.CARD_PUMP_LIQUID;
    }

    public static ShapeCardType getType(ItemStack stack) {
        if (stack.getItem() instanceof ShapeCardItem) {
            return ((ShapeCardItem) stack.getItem()).type;
        }
        if (stack.getItem() == BuilderModule.SPACE_CHAMBER_CARD.get()) {
            return ShapeCardType.CARD_SPACE;
        }
        return ShapeCardType.CARD_UNKNOWN;
    }

    private static void addBlocks(Set<Block> blocks, Block block, ITag<Block> tag, boolean tagMatching) {
        blocks.add(block);
        if (tagMatching && tag != null) {
            for (Block b : tag.getValues()) {
                blocks.add(b);
            }
        }
    }

    public static Set<Block> getVoidedBlocks(ItemStack stack) {
        Set<Block> blocks = new HashSet<>();
        boolean tagMatching = isTagMatching(stack);
        if (isVoiding(stack, "stone")) {
            addBlocks(blocks, Blocks.STONE, Tags.Blocks.STONE, tagMatching);
        }
        if (isVoiding(stack, "cobble")) {
            addBlocks(blocks, Blocks.COBBLESTONE, Tags.Blocks.COBBLESTONE, tagMatching);
        }
        if (isVoiding(stack, "dirt")) {
            addBlocks(blocks, Blocks.DIRT, Tags.Blocks.DIRT, tagMatching);
            addBlocks(blocks, Blocks.GRASS, null, tagMatching);
        }
        if (isVoiding(stack, "sand")) {
            addBlocks(blocks, Blocks.SAND, Tags.Blocks.SAND, tagMatching);
        }
        if (isVoiding(stack, "gravel")) {
            addBlocks(blocks, Blocks.GRAVEL, Tags.Blocks.GRAVEL, tagMatching);
        }
        if (isVoiding(stack, "netherrack")) {
            addBlocks(blocks, Blocks.NETHERRACK, Tags.Blocks.NETHERRACK, tagMatching);
        }
        if (isVoiding(stack, "endstone")) {
            addBlocks(blocks, Blocks.END_STONE, Tags.Blocks.END_STONES, tagMatching);
        }
        return blocks;
    }

    public static boolean isTagMatching(ItemStack stack) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            return false;
        }
        return tagCompound.getBoolean("tagMatching");
    }

    public static boolean isVoiding(ItemStack stack, String material) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            return false;
        }
        return tagCompound.getBoolean("void" + material);
    }

    public static Shape getShape(ItemStack stack) {
        CompoundNBT tagCompound = stack.getTag();
        return getShape(tagCompound);
    }

    public static Shape getShape(CompoundNBT tagCompound) {
        if (tagCompound == null) {
            return Shape.SHAPE_BOX;
        }
        if (!tagCompound.contains("shape")) {
            return Shape.SHAPE_BOX;
        }
        String sn = tagCompound.getString("shape");
        Shape shape = Shape.getShape(sn);
        if (shape == null) {
            return Shape.SHAPE_BOX;
        }
        return shape;
    }

    public static boolean isSolid(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        CompoundNBT tagCompound = stack.getTag();
        return isSolid(tagCompound);
    }

    public static boolean isSolid(CompoundNBT tagCompound) {
        if (tagCompound == null) {
            return true;
        }
        if (tagCompound.contains("shape")) {
            return tagCompound.getBoolean("solid");
        } else {
            return false;
        }
    }

    public static IFormula createCorrectFormula(CompoundNBT tagCompound) {
        Shape shape = getShape(tagCompound);
        boolean solid = isSolid(tagCompound);
        IFormula formula = shape.getFormulaFactory().get();
        return formula.correctFormula(solid);
    }

    public static int getScanId(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        CompoundNBT tagCompound = stack.getOrCreateTag();
        Shape shape = getShape(tagCompound);
        if (shape != Shape.SHAPE_SCAN) {
            return 0;
        }
        return tagCompound.getInt("scanid");
    }

    // Also find scanId's from children
    public static int getScanIdRecursive(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return getScanIdRecursive(stack.getOrCreateTag());
    }

    private static int getScanIdRecursive(CompoundNBT tagCompound) {
        Shape shape = getShape(tagCompound);
        if (tagCompound.contains("scanid") && shape == Shape.SHAPE_SCAN) {
            return tagCompound.getInt("scanid");
        }
        if (shape == Shape.SHAPE_COMPOSITION) {
            // See if there is a scan in the composition that has a scan id
            ListNBT children = tagCompound.getList("children", Constants.NBT.TAG_COMPOUND);
            for (int i = 0 ; i < children.size() ; i++) {
                CompoundNBT childTag = children.getCompound(i);
                int id = getScanIdRecursive(childTag);
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
        formula.getCheckSumClient(stack.getTag(), crc);
    }

    public static void getLocalChecksum(CompoundNBT tagCompound, Check32 crc) {
        if (tagCompound == null) {
            return;
        }
        crc.add(getShape(tagCompound).ordinal());
        BlockPos dim = getDimension(tagCompound);
        crc.add(dim.getX());
        crc.add(dim.getY());
        crc.add(dim.getZ());
        crc.add(isSolid(tagCompound) ? 1 : 0);
    }



    public static void setShape(ItemStack stack, Shape shape, boolean solid) {
        CompoundNBT tagCompound = stack.getOrCreateTag();
        if (isSolid(tagCompound) == solid && getShape(tagCompound).equals(shape)) {
            // Nothing happens
            return;
        }
        tagCompound.putString("shape", shape.getDescription());
        tagCompound.putBoolean("solid", solid);
    }

    public static BlockPos getDimension(ItemStack stack) {
        CompoundNBT tagCompound = stack.getTag();
        return getDimension(tagCompound);
    }

    public static BlockPos getDimension(CompoundNBT tagCompound) {
        if (tagCompound == null) {
            return new BlockPos(5, 5, 5);
        }
        if (!tagCompound.contains("dimX")) {
            return new BlockPos(5, 5, 5);
        }
        int dimX = tagCompound.getInt("dimX");
        int dimY = tagCompound.getInt("dimY");
        int dimZ = tagCompound.getInt("dimZ");
        return new BlockPos(dimX, clampDimension(dimY, 256), dimZ);
    }

    public static BlockPos getClampedDimension(ItemStack stack, int maximum) {
        CompoundNBT tagCompound = stack.getTag();
        return getClampedDimension(tagCompound, maximum);
    }

    public static BlockPos getClampedDimension(CompoundNBT tagCompound, int maximum) {
        if (tagCompound == null) {
            return new BlockPos(5, 5, 5);
        }
        int dimX = tagCompound.getInt("dimX");
        int dimY = tagCompound.getInt("dimY");
        int dimZ = tagCompound.getInt("dimZ");
        return new BlockPos(clampDimension(dimX, maximum), clampDimension(dimY, maximum), clampDimension(dimZ, maximum));
    }

    private static int clampDimension(int o, int maximum) {
        if (o > maximum) {
            o = maximum;
        } else if (o < 0) {
            o = 0;
        }
        return o;
    }

    public static BlockPos getOffset(ItemStack stack) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            return new BlockPos(0, 0, 0);
        }
        int offsetX = tagCompound.getInt("offsetX");
        int offsetY = tagCompound.getInt("offsetY");
        int offsetZ = tagCompound.getInt("offsetZ");
        return new BlockPos(offsetX, offsetY, offsetZ);
    }

    public static BlockPos getClampedOffset(ItemStack stack, int maximum) {
        CompoundNBT tagCompound = stack.getTag();
        return getClampedOffset(tagCompound, maximum);
    }

    public static BlockPos getClampedOffset(CompoundNBT tagCompound, int maximum) {
        if (tagCompound == null) {
            return new BlockPos(0, 0, 0);
        }
        int offsetX = tagCompound.getInt("offsetX");
        int offsetY = tagCompound.getInt("offsetY");
        int offsetZ = tagCompound.getInt("offsetZ");
        return new BlockPos(clampOffset(offsetX, maximum), clampOffset(offsetY, maximum), clampOffset(offsetZ, maximum));
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
    public ActionResult<ItemStack> use(World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) {
            GuiShapeCard.open(false);
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
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

    private static void placeBlockIfPossible(World worldObj, Map<BlockPos, BlockState> blocks, int maxSize, int x, int y, int z, BlockState state, boolean forquarry) {
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
        BlockPos dimension = ShapeCardItem.getDimension(stack);
        BlockPos clamped = new BlockPos(Math.min(dimension.getX(), 512), Math.min(dimension.getY(), 256), Math.min(dimension.getZ(), 512));

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
    public static int getDataPositions(World world, ItemStack stack, Shape shape, boolean solid, RLE positions, StatePalette statePalette) {
        BlockPos dimension = ShapeCardItem.getDimension(stack);
        BlockPos clamped = new BlockPos(Math.min(dimension.getX(), 512), Math.min(dimension.getY(), 256), Math.min(dimension.getZ(), 512));

        IFormula formula = shape.getFormulaFactory().get();
        int dx = clamped.getX();
        int dy = clamped.getY();
        int dz = clamped.getZ();

        formula = formula.correctFormula(solid);
        formula.setup(world, new BlockPos(0, 0, 0), clamped, new BlockPos(0, 0, 0), !stack.isEmpty() ? stack.getTag() : null);

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



    public static void composeFormula(ItemStack shapeCard, IFormula formula, World worldObj, BlockPos thisCoord, BlockPos dimension, BlockPos offset, Map<BlockPos, BlockState> blocks, int maxSize, boolean solid, boolean forquarry, ChunkPos chunk) {
        int xCoord = thisCoord.getX();
        int yCoord = thisCoord.getY();
        int zCoord = thisCoord.getZ();
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();
        BlockPos tl = new BlockPos(xCoord - dx/2 + offset.getX(), yCoord - dy/2 + offset.getY(), zCoord - dz/2 + offset.getZ());

        formula = formula.correctFormula(solid);
        formula.setup(worldObj, thisCoord, dimension, offset, shapeCard != null ? shapeCard.getTag() : null);

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

    // @todo 1.14
//    @Override
//    public String getUnlocalizedName(ItemStack itemStack) {
//        if (itemStack.getItemDamage() == 0) {
//            return super.getUnlocalizedName(itemStack);
//        } else {
//            return super.getUnlocalizedName(itemStack) + itemStack.getItemDamage();
//        }
//    }
//    @Override
//    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
//        if (this.isInCreativeTab(tab)) {
//            for(ShapeCardType type : ShapeCardType.values()) {
//                int damage = type.getDamage();
//                if(damage >= 0) {
//                    items.add(new ItemStack(this, 1, damage));
//                }
//            }
//        }
//    }

    private static boolean validFile(PlayerEntity player, String filename) {
        if (filename.contains("\\") || filename.contains("/") || filename.contains(":")) {
            player.displayClientMessage(new StringTextComponent(TextFormatting.RED + "Invalid filename '" + filename + "'! Cannot be a path!"), false);
            return false;
        }
        return true;
    }


    public static void save(PlayerEntity player, ItemStack card, String filename) {
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
                String r = state.getBlock().getRegistryName().toString();
//                writer.println(r + "@" + state.getBlock().getMetaFromState(state));   // @todo 1.14 no more meta!
                writer.println(r);
            }
            writer.println("DATA");

            byte[] encoded = Base64.getEncoder().encode(data);
            writer.write(new String(encoded));
        } catch (FileNotFoundException e) {
            player.displayClientMessage(new StringTextComponent(TextFormatting.RED + "Cannot write to file '" + filename + "'!"), false);
            return;
        }
        player.displayClientMessage(new StringTextComponent(TextFormatting.GREEN + "Saved shape to file '" + file.getPath() + "'"), false);
    }

    public static void load(PlayerEntity player, ItemStack card, String filename) {
        if (!validFile(player, filename)) {
            return;
        }

        Shape shape = ShapeCardItem.getShape(card);

        if (shape != Shape.SHAPE_SCAN) {
            player.displayClientMessage(new StringTextComponent(TextFormatting.RED + "To load a file into this card you need a linked 'scan' type card!"), false);
            return;
        }

        CompoundNBT compound = card.getOrCreateTag();
        int scanId = compound.getInt("scanid");
        if (scanId == 0) {
            player.displayClientMessage(new StringTextComponent(TextFormatting.RED + "This card is not linked to scan data!"), false);
            return;
        }

        File dataDir = new File("rftoolsscans");
        dataDir.mkdirs();
        File file = new File(dataDir, filename);

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String s = reader.readLine();
            if (!"SHAPE".equals(s)) {
                player.displayClientMessage(new StringTextComponent(TextFormatting.RED + "This does not appear to be a valid shapecard file!"), false);
                return;
            }
            s = reader.readLine();
            if (!s.startsWith("DIM:")) {
                player.displayClientMessage(new StringTextComponent(TextFormatting.RED + "This does not appear to be a valid shapecard file!"), false);
                return;
            }
            BlockPos dim = parse(s.substring(4));
            s = reader.readLine();
            if (!s.startsWith("OFF:")) {
                player.displayClientMessage(new StringTextComponent(TextFormatting.RED + "This does not appear to be a valid shapecard file!"), false);
                return;
            }
            BlockPos off = parse(s.substring(4));
            s = reader.readLine();
            StatePalette statePalette = new StatePalette();
            while (!"DATA".equals(s)) {
                String[] split = StringUtils.split(s, '@');
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(split[0]));
                int meta = Integer.parseInt(split[1]);
                if (block == null) {
                    player.displayClientMessage(new StringTextComponent(TextFormatting.YELLOW + "Could not find block '" + split[0] + "'!"), false);
                    block = Blocks.STONE;
                    meta = 0;
                }
//                statePalette.add(block.getStateFromMeta(meta));
                statePalette.add(block.defaultBlockState());  // @todo 1.14 no more meta!
                s = reader.readLine();
            }
            s = reader.readLine();
            byte[] decoded = Base64.getDecoder().decode(s.getBytes());

            setDataFromFile(scanId, card, dim, off, decoded, statePalette);
        } catch (FileNotFoundException e) {
            player.displayClientMessage(new StringTextComponent(TextFormatting.RED + "Cannot read from file '" + filename + "'!"), false);
            return;
        } catch (IOException e) {
            player.displayClientMessage(new StringTextComponent(TextFormatting.RED + "Cannot read from file '" + filename + "'!"), false);
            return;
        } catch (NullPointerException e) {
            player.displayClientMessage(new StringTextComponent(TextFormatting.RED + "File '" + filename + "' is too short!"), false);
            return;
        } catch (ArrayIndexOutOfBoundsException e) {
            player.displayClientMessage(new StringTextComponent(TextFormatting.RED + "File '" + filename + "' contains invalid entries!"), false);
            return;
        }
        player.displayClientMessage(new StringTextComponent(TextFormatting.GREEN + "Loaded shape from file '" + file.getPath() + "'"), false);
    }

    private static void setDataFromFile(int scanId, ItemStack card, BlockPos dimension, BlockPos offset, byte[] data, StatePalette palette) {
        // @todo 1.14 scanner
//        ScanDataManager scans = ScanDataManager.get();
//        scans.getOrCreateScan(scanId).setData(data, palette.getPalette(), dimension, offset);
//        scans.save(scanId);
//        ShapeCardItem.setDimension(card, dimension.getX(), dimension.getY(), dimension.getZ());
//        ShapeCardItem.setOffset(card, offset.getX(), offset.getY(), offset.getZ());
//        ShapeCardItem.setShape(card, Shape.SHAPE_SCAN, true);
    }


    private static BlockPos parse(String s) {
        String[] split = StringUtils.split(s, ',');
        return new BlockPos(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    }
}
