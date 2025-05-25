package mcjty.rftoolsbuilder.modules.builder.blocks;

import com.mojang.serialization.DataResult;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.api.module.DefaultModuleSupport;
import mcjty.lib.api.module.IModuleSupport;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ListCommand;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.*;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.*;
import mcjty.rftoolsbase.api.client.IHudSupport;
import mcjty.rftoolsbase.modules.filter.items.FilterModuleItem;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import mcjty.rftoolsbuilder.modules.builder.BlockInformation;
import mcjty.rftoolsbuilder.modules.builder.BuilderConfiguration;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.SpaceChamberRepository;
import mcjty.rftoolsbuilder.modules.builder.data.BuilderData;
import mcjty.rftoolsbuilder.modules.builder.data.ShapeCardData;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardType;
import mcjty.rftoolsbuilder.modules.builder.items.SpaceChamberCardItem;
import mcjty.rftoolsbuilder.setup.ClientCommandHandler;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import mcjty.rftoolsbuilder.shapes.Shape;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.SpecialPlantable;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static mcjty.lib.api.container.DefaultContainerProvider.container;
import static mcjty.lib.builder.TooltipBuilder.*;
import static mcjty.lib.container.SlotDefinition.specific;
import static mcjty.lib.setup.Registration.BASE_BE_DATA;
import static mcjty.rftoolsbase.modules.hud.Hud.COMMAND_GETHUDLOG;
import static mcjty.rftoolsbuilder.modules.builder.blocks.AnchorMode.*;
import static mcjty.rftoolsbuilder.modules.builder.blocks.BuilderMode.*;

public class BuilderTileEntity extends TickingTileEntity implements IHudSupport {

    public static final int SLOT_TAB = 0;
    public static final int SLOT_FILTER = 1;

    public static final ResourceLocation DONT_REMOVE_ME = ResourceLocation.fromNamespaceAndPath(RFToolsBuilder.MODID, "dontremoveme");
    public static final TagKey<Block> DONT_REMOVE_ME_TAG = TagTools.createBlockTagKey(DONT_REMOVE_ME);

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(2)
            .slot(specific(s -> (s.getItem() instanceof ShapeCardItem) || (s.getItem() instanceof SpaceChamberCardItem)).in().out(), SLOT_TAB, 100, 10)
            .slot(specific(s -> s.getItem() instanceof FilterModuleItem).in().out(), SLOT_FILTER, 84, 46)
            .playerSlots(10, 70));

    // For usage in the gui
    private static int currentLevel = 0;

    // Client-side
    private int scanLocCnt = 0;
    private static Map<BlockPos, Pair<Long, BlockPos>> scanLocClient = new HashMap<>();

    private int collectCounter = BuilderConfiguration.collectTimer.get();
    private int collectXP = 0;

    private boolean boxValid = false;
    private int projDx;
    private int projDy;
    private int projDz;

    private long lastHudTime = 0;
    private List<String> clientHudLog = new ArrayList<>();

    private ShapeCardType cardType = ShapeCardType.CARD_UNKNOWN;

    private static ItemStack TOOL_NORMAL;
    private static ItemStack TOOL_SILK;
    private static ItemStack TOOL_FORTUNE;

    private final Cached<Predicate<ItemStack>> filterCache = Cached.of(this::createFilterCache);

    // The currently forced chunk.
    private ChunkPos forcedChunk = null;

    // Cached set of blocks that we need to build in shaped mode
    private Map<BlockPos, BlockState> cachedBlocks = null;
    private ChunkPos cachedChunk = null;       // For which chunk are the cachedBlocks valid

    // Cached set of blocks that we want to void with the quarry.
    private final Cached<Set<Block>> cachedVoidableBlocks = Cached.of(this::getCachedVoidableBlocks);

    // Drops from a block that we broke but couldn't fit in an inventory
    private final LazyList<ItemStack> overflowItems = new LazyList<>();

    private final FakePlayerGetter harvester = new FakePlayerGetter(this, "rftools_builder");

    private final GenericItemHandler items = createItemHandler();
    @Cap(type = CapType.ITEMS_AUTOMATION)
    private static final Function<BuilderTileEntity, GenericItemHandler> ITEM_CAP = tile -> tile.items;

    private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(
            this, true, BuilderConfiguration.BUILDER_MAXENERGY.get(), BuilderConfiguration.BUILDER_RECEIVEPERTICK.get());
    @Cap(type = CapType.ENERGY)
    private static final Function<BuilderTileEntity, GenericEnergyStorage> ENERGY_CAP = tile -> tile.energyStorage;

    @Cap(type = CapType.CONTAINER)
    private static final Function<BuilderTileEntity, MenuProvider> SCREEN_CAP = tile -> new DefaultContainerProvider<GenericContainer>("Builder")
            .containerSupplier(container(BuilderModule.CONTAINER_BUILDER, CONTAINER_FACTORY, tile))
            .itemHandler(() -> tile.items)
            .energyHandler(() -> tile.energyStorage)
            .shortListener(Sync.integer(() -> tile.getScan() == null ? -1 : tile.getScan().getY(), v -> currentLevel = v))
            .data(BuilderModule.BUILDER_DATA, BuilderData.STREAM_CODEC, BuilderData.CODEC)
            .data(BASE_BE_DATA, BaseBEData.STREAM_CODEC, BaseBEData.CODEC)
            .setupSync(tile);

    private final DefaultInfusable infusable = new DefaultInfusable(BuilderTileEntity.this);
    @Cap(type = CapType.INFUSABLE)
    private static final Function<BuilderTileEntity, IInfusable> INFUSABLE_CAP = tile -> tile.infusable;

    @Cap(type = CapType.MODULE)
    private static final Function<BuilderTileEntity, IModuleSupport> MODULE_CAP = tile -> new DefaultModuleSupport(SLOT_TAB) {
        @Override
        public boolean isModule(ItemStack itemStack) {
            return (itemStack.getItem() instanceof ShapeCardItem || itemStack.getItem() == BuilderModule.SPACE_CHAMBER_CARD.get());
        }
    };

    public BuilderTileEntity(BlockPos pos, BlockState state) {
        super(BuilderModule.BUILDER.be().get(), pos, state);
        setRSMode(RedstoneMode.REDSTONE_ONREQUIRED);
    }


    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .tileEntitySupplier(BuilderTileEntity::new)
                .topDriver(RFToolsBuilderTOPDriver.DRIVER)
                .infusable()
                .manualEntry(ManualHelper.create("rftoolsbase:builder/builder_intro"))
                .info(key("message.rftoolsbuilder.shiftmessage"))
                .infoShift(header(), gold())) {
            @Override
            public RotationType getRotationType() {
                return RotationType.HORIZROTATION;
            }
        };
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    @Override
    public Direction getBlockOrientation() {
        BlockState state = level.getBlockState(worldPosition);
        if (state.getBlock() == BuilderModule.BUILDER.block().get()) {
            return OrientationTools.getOrientationHoriz(state);
        } else {
            return null;
        }
    }

    @Override
    public boolean isBlockAboveAir() {
        return level.isEmptyBlock(worldPosition.above());
    }

    @Override
    public List<String> getClientLog() {
        return clientHudLog;
    }

    public List<String> getHudLog() {
        List<String> list = new ArrayList<>();
        list.add(ChatFormatting.BLUE + "Mode:");
        if (isShapeCard()) {
            getCardType().addHudLog(list, items);
        } else {
            list.add("    Space card: " + getMode().getName().toLowerCase());
        }
        BuilderData data = getData(BuilderModule.BUILDER_DATA);
        if (data.scan() != null) {
            list.add(ChatFormatting.BLUE + "Progress:");
            list.add("    Y level: " + data.scan().getY());
            int minChunkX = data.minBox().getX() >> 4;
            int minChunkZ = data.minBox().getZ() >> 4;
            int maxChunkX = data.maxBox().getX() >> 4;
            int maxChunkZ = data.maxBox().getZ() >> 4;
            int curX = data.scan().getX() >> 4;
            int curZ = data.scan().getZ() >> 4;
            int totChunks = (maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1);
            int curChunk = (curZ - minChunkZ) * (maxChunkX - minChunkX) + curX - minChunkX;
            list.add("    Chunk:  " + curChunk + " of " + totChunks);
        }
        if (data.lastError() != null && !data.lastError().isEmpty()) {
            String[] errors = StringUtils.split(data.lastError(), "\n");
            for (String error : errors) {
                list.add(ChatFormatting.RED + error);
            }
        }
        return list;
    }

    @Override
    public BlockPos getHudPos() {
        return getBlockPos();
    }

    @Override
    public long getLastUpdateTime() {
        return lastHudTime;
    }

    @Override
    public void setLastUpdateTime(long t) {
        lastHudTime = t;
    }

    private boolean isShapeCard() {
        return items.getStackInSlot(SLOT_TAB).getItem() instanceof ShapeCardItem;
    }

    private BlockPos getScan() {
        return getData(BuilderModule.BUILDER_DATA).scan();
    }

    private void makeSupportBlocksShaped() {
        ItemStack shapeCard = items.getStackInSlot(SLOT_TAB);
        BlockPos dimension = ShapeCardItem.getClampedDimension(shapeCard, BuilderConfiguration.maxBuilderDimension.get());
        BlockPos offset = ShapeCardItem.getClampedOffset(shapeCard, BuilderConfiguration.maxBuilderOffset.get());
        Shape shape = ShapeCardItem.getShape(shapeCard);
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        ShapeCardItem.composeFormula(shapeCard, shape.getFormulaFactory().get(), level, getBlockPos(), dimension, offset, blocks, BuilderConfiguration.maxBuilderDimension.get() * 256 * BuilderConfiguration.maxBuilderDimension.get(), false, false, null);
        BlockState state = BuilderModule.SUPPORT.get().defaultBlockState().setValue(SupportBlock.STATUS, SupportBlock.SupportStatus.STATUS_OK);
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos p = entry.getKey();
            if (level.isEmptyBlock(p)) {
                level.setBlock(p, state, Block.UPDATE_CLIENTS);
            }
        }
    }

    private BuilderData makeSupportBlocks(BuilderData data) {
        if (isShapeCard()) {
            makeSupportBlocksShaped();
            return data;
        }

        Pair<BuilderData, SpaceChamberRepository.SpaceChamberChannel> result = calculateBox(data);
        data = result.getLeft();
        SpaceChamberRepository.SpaceChamberChannel chamberChannel = result.getRight();
        if (chamberChannel != null) {
            ResourceKey<Level> dimension = chamberChannel.getDimension();
            Level world = LevelTools.getLevel(this.level, dimension);
            if (world == null) {
                return data;
            }

            Player player = harvester.get();
            BlockPos.MutableBlockPos src = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos dest = new BlockPos.MutableBlockPos();
            BlockPos minBox = data.minBox();
            BlockPos maxBox = data.maxBox();
            for (int x = minBox.getX(); x <= maxBox.getX(); x++) {
                for (int y = minBox.getY(); y <= maxBox.getY(); y++) {
                    for (int z = minBox.getZ(); z <= maxBox.getZ(); z++) {
                        src.set(x, y, z);
                        sourceToDest(src, dest);
                        BlockState srcState = world.getBlockState(src);
                        Block srcBlock = srcState.getBlock();
                        BlockState dstState = world.getBlockState(dest);
                        Block dstBlock = dstState.getBlock();
                        SupportBlock.SupportStatus error = SupportBlock.SupportStatus.STATUS_OK;
                        if (getMode() != MODE_COPY) {
                            BlockEntity srcTileEntity = world.getBlockEntity(src);
                            BlockEntity dstTileEntity = world.getBlockEntity(dest);

                            SupportBlock.SupportStatus error1 = isMovable(player, world, src, srcBlock, srcTileEntity);
                            SupportBlock.SupportStatus error2 = isMovable(player, world, dest, dstBlock, dstTileEntity);
                            error = SupportBlock.SupportStatus.max(error1, error2);
                        }
                        if (isEmpty(srcState, srcBlock) && !isEmpty(dstState, dstBlock)) {
                            world.setBlock(src, BuilderModule.SUPPORT.get().defaultBlockState().setValue(SupportBlock.STATUS, error), Block.UPDATE_ALL);
                        }
                        if (isEmpty(dstState, dstBlock) && !isEmpty(srcState, srcBlock)) {
                            world.setBlock(dest, BuilderModule.SUPPORT.get().defaultBlockState().setValue(SupportBlock.STATUS, error), Block.UPDATE_ALL);
                        }
                    }
                }
            }
        }
        return data;
    }

    private void clearSupportBlocksShaped() {
        ItemStack shapeCard = items.getStackInSlot(SLOT_TAB);
        BlockPos dimension = ShapeCardItem.getClampedDimension(shapeCard, BuilderConfiguration.maxBuilderDimension.get());
        BlockPos offset = ShapeCardItem.getClampedOffset(shapeCard, BuilderConfiguration.maxBuilderOffset.get());
        Shape shape = ShapeCardItem.getShape(shapeCard);
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        ShapeCardItem.composeFormula(shapeCard, shape.getFormulaFactory().get(), level, getBlockPos(), dimension, offset, blocks, BuilderConfiguration.maxSpaceChamberDimension.get() * BuilderConfiguration.maxSpaceChamberDimension.get() * BuilderConfiguration.maxSpaceChamberDimension.get(), false, false, null);
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos p = entry.getKey();
            if (level.getBlockState(p).getBlock() == BuilderModule.SUPPORT.get()) {
                level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
            }
        }
    }

    public BuilderData clearSupportBlocks(BuilderData data) {
        if (level.isClientSide) {
            // Don't do anything on the client.
            return data;
        }

        if (isShapeCard()) {
            clearSupportBlocksShaped();
            return data;
        }

        Pair<BuilderData, SpaceChamberRepository.SpaceChamberChannel> result = calculateBox(data);
        data = result.getLeft();
        SpaceChamberRepository.SpaceChamberChannel chamberChannel = result.getRight();
        if (chamberChannel != null) {
            ResourceKey<Level> dimension = chamberChannel.getDimension();
            Level world = LevelTools.getLevel(this.level, dimension);

            BlockPos.MutableBlockPos src = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos dest = new BlockPos.MutableBlockPos();
            BlockPos minBox = data.minBox();
            BlockPos maxBox = data.maxBox();
            for (int x = minBox.getX(); x <= maxBox.getX(); x++) {
                for (int y = minBox.getY(); y <= maxBox.getY(); y++) {
                    for (int z = minBox.getZ(); z <= maxBox.getZ(); z++) {
                        src.set(x, y, z);
                        if (world != null) {
                            Block srcBlock = world.getBlockState(src).getBlock();
                            if (srcBlock == BuilderModule.SUPPORT.get()) {
                                world.setBlockAndUpdate(src, Blocks.AIR.defaultBlockState());
                            }
                        }
                        sourceToDest(src, dest);
                        Block dstBlock = world.getBlockState(dest).getBlock();
                        if (dstBlock == BuilderModule.SUPPORT.get()) {
                            world.setBlockAndUpdate(dest, Blocks.AIR.defaultBlockState());
                        }
                    }
                }
            }
        }
        return data;
    }

    @Override
    public void onDataChanged(AttachmentType<?> type, Object oldData, Object newData) {
        if (type == BuilderModule.BUILDER_DATA.get()) {
            onDataChanged((BuilderData) oldData, (BuilderData) newData);
        }
    }

    private void onDataChanged(BuilderData oldData, BuilderData newData) {
        if (level.isClientSide()) {
            return;
        }
        if (oldData.mode() != newData.mode()) {
            newData = restartScan(newData);
        }
        if (oldData.anchor() != newData.anchor()) {
            newData = onAnchorChanged(newData);
        }
        if (oldData.rotate() != newData.rotate()) {
            newData = onRotateChanged(newData);
        }
        if (oldData.flags().supportMode() != newData.flags().supportMode()) {
            if (newData.flags().supportMode()) {
                newData = makeSupportBlocks(newData);
            } else {
                newData = clearSupportBlocks(newData);
            }
        }
        setData(BuilderModule.BUILDER_DATA, newData);
    }

    public boolean isHilightMode() {
        return getData(BuilderModule.BUILDER_DATA).flags().hilightMode();
    }

    public boolean isWaitMode() {
        return getData(BuilderModule.BUILDER_DATA).flags().waitMode();
    }

    private String getLastError() {
        return getData(BuilderModule.BUILDER_DATA).lastError();
    }

    private boolean waitOrSkip(BuilderData data, String error) {
        if (isWaitMode()) {
            data = data.withLastError(error);
            setData(BuilderModule.BUILDER_DATA, data);
            return true;
        }
        return false;    }

    private boolean skip(BuilderData data) {
        data = data.withLastError(null);
        setData(BuilderModule.BUILDER_DATA, data);
        return false;
    }

    private Pair<BuilderData, Boolean> skip(BuilderData data, String error) {
        data = data.withLastError(error);
        return Pair.of(data, false);
    }

    public boolean suspend(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
        BuilderData data = getData(BuilderModule.BUILDER_DATA);
        data = data.withLastError(null);
        setData(BuilderModule.BUILDER_DATA, data);
        return true;
    }

    private Pair<BuilderData, Boolean> suspend(BuilderData data, String error) {
        data = data.withLastError(error);
        return Pair.of(data, true);
    }

    public boolean hasLoopMode() {
        return getData(BuilderModule.BUILDER_DATA).flags().loopMode();
    }

    public boolean hasEntityMode() {
        return getData(BuilderModule.BUILDER_DATA).flags().entityMode();
    }

    public boolean hasSupportMode() {
        return getData(BuilderModule.BUILDER_DATA).flags().supportMode();
    }

    public void setSupportMode(boolean supportMode) {
        BuilderData data = getData(BuilderModule.BUILDER_DATA);
        if (!level.isClientSide) {
            if (supportMode) {
                data = makeSupportBlocks(data);
            } else {
                data = clearSupportBlocks(data);
            }
            setData(BuilderModule.BUILDER_DATA, data.withSupportMode(supportMode));
        }
    }

    public boolean isSilent() {
        return getData(BuilderModule.BUILDER_DATA).flags().silent();
    }

    public BuilderMode getMode() {
        return getData(BuilderModule.BUILDER_DATA).mode();
    }

    public void resetBox() {
        boxValid = false;
    }

    public AnchorMode getAnchor() {
        return getData(BuilderModule.BUILDER_DATA).anchor();
    }

    public BuilderData onAnchorChanged(BuilderData data) {
        if (data.flags().supportMode() && !level.isClientSide()) {
            data = clearSupportBlocks(data);
        }
        boxValid = false;

        if (isShapeCard()) {
            // If there is a shape card we modify it for the new settings.
            ItemStack shapeCard = items.getStackInSlot(SLOT_TAB);
            BlockPos dimension = ShapeCardItem.getDimension(shapeCard);
            BlockPos minBox = positionBox(dimension);
            int dx = dimension.getX();
            int dy = dimension.getY();
            int dz = dimension.getZ();

            BlockPos offset = new BlockPos(minBox.getX() + (int) Math.ceil(dx / 2), minBox.getY() + (int) Math.ceil(dy / 2), minBox.getZ() + (int) Math.ceil(dz / 2));
            ShapeCardItem.setOffset(shapeCard, offset.getX(), offset.getY(), offset.getZ());
        }

        if (data.flags().supportMode() && !level.isClientSide()) {
            data = makeSupportBlocks(data);
        }
        return data;
    }

    // Give a dimension, return a min coordinate of the box right in front of the builder
    private BlockPos positionBox(BlockPos dimension) {
        BlockState state = level.getBlockState(getBlockPos());
        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int spanX = dimension.getX();
        int spanY = dimension.getY();
        int spanZ = dimension.getZ();
        int x = 0;
        int y;
        int z = 0;
        AnchorMode anchor = getAnchor();
        y = -((anchor == ANCHOR_NE || anchor == ANCHOR_NW) ? spanY - 1 : 0);
        switch (direction) {
            case SOUTH -> {
                x = -((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanX - 1 : 0);
                z = -spanZ;
            }
            case NORTH -> {
                x = 1 - spanX + ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanX - 1 : 0);
                z = 1;
            }
            case WEST -> {
                x = 1;
                z = -((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanZ - 1 : 0);
            }
            case EAST -> {
                x = -spanX;
                z = -((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? 0 : spanZ - 1);
            }
            case DOWN, UP -> {
            }
        }
        return new BlockPos(x, y, z);
    }


    public RotateMode getRotate() {
        return getData(BuilderModule.BUILDER_DATA).rotate();
    }

    public BuilderData onRotateChanged(BuilderData data) {
        if (data.flags().supportMode() && !level.isClientSide()) {
            data = clearSupportBlocks(data);
        }
        boxValid = false;
        if (data.flags().supportMode() && !level.isClientSide()) {
            data = makeSupportBlocks(data);
        }
        return data;
    }

    @Override
    public void setPowerInput(int powered) {
        boolean o = isMachineEnabled();
        super.setPowerInput(powered);
        boolean n = isMachineEnabled();
        if (o != n) {
            BuilderData data = getData(BuilderModule.BUILDER_DATA);
            if (hasLoopMode() || (n && data.scan() == null)) {
                if (!level.isClientSide) {
                    data = restartScan(data);
                    setData(BuilderModule.BUILDER_DATA, data);
                }
            }
        }
    }

    private void createProjection(BuilderData data, SpaceChamberRepository.SpaceChamberChannel chamberChannel) {
        BlockPos minC = rotate(chamberChannel.getMinCorner());
        BlockPos maxC = rotate(chamberChannel.getMaxCorner());
        BlockPos minCorner = new BlockPos(Math.min(minC.getX(), maxC.getX()), Math.min(minC.getY(), maxC.getY()), Math.min(minC.getZ(), maxC.getZ()));
        BlockPos maxCorner = new BlockPos(Math.max(minC.getX(), maxC.getX()), Math.max(minC.getY(), maxC.getY()), Math.max(minC.getZ(), maxC.getZ()));

        BlockState state = level.getBlockState(getBlockPos());
        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int xCoord = getBlockPos().getX();
        int yCoord = getBlockPos().getY();
        int zCoord = getBlockPos().getZ();
        int spanX = maxCorner.getX() - minCorner.getX();
        int spanY = maxCorner.getY() - minCorner.getY();
        int spanZ = maxCorner.getZ() - minCorner.getZ();
        AnchorMode anchor = data.anchor();
        switch (direction) {
            case SOUTH -> {
                projDx = xCoord + Direction.NORTH.getNormal().getX() - minCorner.getX() - ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanX : 0);
                projDz = zCoord + Direction.NORTH.getNormal().getZ() - minCorner.getZ() - spanZ;
            }
            case NORTH -> {
                projDx = xCoord + Direction.SOUTH.getNormal().getX() - minCorner.getX() - spanX + ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanX : 0);
                projDz = zCoord + Direction.SOUTH.getNormal().getZ() - minCorner.getZ();
            }
            case WEST -> {
                projDx = xCoord + Direction.EAST.getNormal().getX() - minCorner.getX();
                projDz = zCoord + Direction.EAST.getNormal().getZ() - minCorner.getZ() - ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanZ : 0);
            }
            case EAST -> {
                projDx = xCoord + Direction.WEST.getNormal().getX() - minCorner.getX() - spanX;
                projDz = zCoord + Direction.WEST.getNormal().getZ() - minCorner.getZ() - spanZ + ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanZ : 0);
            }
            case DOWN, UP -> {
            }
        }
        projDy = yCoord - minCorner.getY() - ((anchor == ANCHOR_NE || anchor == ANCHOR_NW) ? spanY : 0);
    }

    private BuilderData calculateBox(BuilderData data, int channel) {
        SpaceChamberRepository repository = SpaceChamberRepository.get(level);
        SpaceChamberRepository.SpaceChamberChannel chamberChannel = repository.getChannel(channel);
        BlockPos minCorner = chamberChannel.getMinCorner();
        BlockPos maxCorner = chamberChannel.getMaxCorner();
        if (minCorner == null || maxCorner == null) {
            return data;
        }

        if (boxValid) {
            // Double check if the box is indeed still valid.
            if (minCorner.equals(data.minBox()) && maxCorner.equals(data.maxBox())) {
                return data;
            }
        }

        boxValid = true;
        cardType = ShapeCardType.CARD_SPACE;

        createProjection(data, chamberChannel);

        data = data.withMinBox(minCorner).withMaxBox(maxCorner);
        data = restartScan(data);
        return data;
    }

    private void checkStateServerShaped() {
        float factor = infusable.getInfusedFactor();
        BuilderData data = getData(BuilderModule.BUILDER_DATA);
        for (int i = 0; i < BuilderConfiguration.quarryBaseSpeed.get() + (factor * BuilderConfiguration.quarryInfusionSpeedFactor.get()); i++) {
            if (data.scan() != null) {
                data = handleBlockShaped(data);
            }
        }
        setData(BuilderModule.BUILDER_DATA, data);
    }


    @Override
    public void tickServer() {
        if (!overflowItems.isEmpty()) {
            insertItems(overflowItems.extractList());
        }

        if (!isMachineEnabled() && hasLoopMode()) {
            return;
        }

        BuilderData data = getData(BuilderModule.BUILDER_DATA);
        if (data.scan() == null) {
            return;
        }

        if (isHilightMode()) {
            updateHilight();
        }

        if (isShapeCard()) {
            if (!isMachineEnabled()) {
                chunkUnload();
                return;
            }
            checkStateServerShaped();
            return;
        }

        Pair<BuilderData, SpaceChamberRepository.SpaceChamberChannel> result = calculateBox(data);
        data = result.getLeft();
        SpaceChamberRepository.SpaceChamberChannel chamberChannel = result.getRight();
        if (chamberChannel == null) {
            setData(BuilderModule.BUILDER_DATA, data.withScan(null));
            return;
        }

        ResourceKey<Level> dimension = chamberChannel.getDimension();
        Level world = LevelTools.getLevel(this.level, dimension);
        if (world == null) {
            // The other location must be loaded.
            return;
        }

        if (getMode() == MODE_COLLECT) {
            collectItems(world);
        } else {
            float factor = infusable.getInfusedFactor();
            for (int i = 0; i < 2 + (factor * 40); i++) {
                if (data.scan() != null) {
                    data = handleBlock(data, world);
                }
            }
        }
        setData(BuilderModule.BUILDER_DATA, data);
    }

    private void updateHilight() {
        scanLocCnt--;
        if (scanLocCnt <= 0) {
            scanLocCnt = 5;
            BuilderData data = getData(BuilderModule.BUILDER_DATA);
            int x = data.scan().getX();
            int y = data.scan().getY();
            int z = data.scan().getZ();
            double sqradius = 30 * 30;
            for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
                if (Objects.equals(player.getCommandSenderWorld().dimension(), level.dimension())) {
                    double d0 = x - player.getX();
                    double d1 = y - player.getY();
                    double d2 = z - player.getZ();
                    if (d0 * d0 + d1 * d1 + d2 * d2 < sqradius) {
                        RFToolsBuilderMessages.sendToClient(player, ClientCommandHandler.CMD_POSITION_TO_CLIENT,
                                TypedMap.builder().put(ClientCommandHandler.PARAM_POS, getBlockPos()).put(ClientCommandHandler.PARAM_SCAN, data.scan()));
                    }
                }
            }
        }
    }

    private void collectItems(Level world) {
        // Collect item mode
        collectCounter--;
        if (collectCounter > 0) {
            return;
        }
        collectCounter = BuilderConfiguration.collectTimer.get();

        BuilderData data = getData(BuilderModule.BUILDER_DATA);
        if (!hasLoopMode()) {
            data = data.withScan(null);
            setData(BuilderModule.BUILDER_DATA, data);
        }

        float factor = infusable.getInfusedFactor();

        BlockPos minBox = data.minBox();
        BlockPos maxBox = data.maxBox();
        long rf = energyStorage.getEnergyStored();
        float area = (maxBox.getX() - minBox.getX() + 1) * (maxBox.getY() - minBox.getY() + 1) * (maxBox.getZ() - minBox.getZ() + 1);
        float infusedFactor = (4.0f - factor) / 4.0f;
        int rfNeeded = (int) (BuilderConfiguration.collectRFPerTickPerArea.get() * area * infusedFactor) * BuilderConfiguration.collectTimer.get();
        if (rfNeeded > rf) {
            // Not enough energy.
            return;
        }
        energyStorage.consumeEnergy(rfNeeded);

        AABB bb = new AABB(minBox.getX() - .8, minBox.getY() - .8, minBox.getZ() - .8, maxBox.getX() + .8, maxBox.getY() + .8, maxBox.getZ() + .8);
        List<Entity> items = world.getEntitiesOfClass(Entity.class, bb);
        for (Entity entity : items) {
            if (entity instanceof ItemEntity) {
                if (collectItem(world, factor, (ItemEntity) entity)) {
                    return;
                }
            } else if (entity instanceof ExperienceOrb) {
                if (collectXP(world, factor, (ExperienceOrb) entity)) {
                    return;
                }
            }
        }
    }

    private boolean collectXP(Level world, float infusedFactor, ExperienceOrb orb) {
        int xp = orb.getValue();
        long rf = energyStorage.getEnergyStored();
        int rfNeeded = (int) (BuilderConfiguration.collectRFPerXP.get() * infusedFactor * xp);
        if (rfNeeded > rf) {
            // Not enough energy.
            return true;
        }

        collectXP += xp;

        int bottles = collectXP / 7;
        if (bottles > 0) {
            if (insertItem(new ItemStack(Items.EXPERIENCE_BOTTLE, bottles)).isEmpty()) {
                collectXP = collectXP % 7;
                orb.kill();
                energyStorage.consumeEnergy(rfNeeded);
            } else {
                collectXP = 0;
            }
        }

        return false;
    }

    private boolean collectItem(Level world, float infusedFactor, ItemEntity item) {
        ItemStack stack = item.getItem();

        Predicate<ItemStack> predicate = filterCache.get();
        if (predicate != null && !predicate.test(stack)) {
            return false;
        }

        long rf = energyStorage.getEnergyStored();
        int rfNeeded = (int) (BuilderConfiguration.collectRFPerItem.get() * infusedFactor) * stack.getCount();
        if (rfNeeded > rf) {
            // Not enough energy.
            return true;
        }
        energyStorage.consumeEnergy(rfNeeded);

        item.kill();
        stack = insertItem(stack);
        if (!stack.isEmpty()) {
            BlockPos position = item.blockPosition();
            ItemEntity entityItem = new ItemEntity(world, position.getX(), position.getY(), position.getZ(), stack);
            world.addFreshEntity(entityItem);
        }
        return false;
    }

    private BuilderData calculateBoxShaped(BuilderData data) {
        ItemStack shapeCard = items.getStackInSlot(SLOT_TAB);
        if (shapeCard.isEmpty()) {
            return data;
        }
        BlockPos dimension = ShapeCardItem.getClampedDimension(shapeCard, BuilderConfiguration.maxBuilderDimension.get());
        BlockPos offset = ShapeCardItem.getClampedOffset(shapeCard, BuilderConfiguration.maxBuilderOffset.get());

        BlockPos minCorner = ShapeCardItem.getMinCorner(getBlockPos(), dimension, offset);
        BlockPos maxCorner = ShapeCardItem.getMaxCorner(getBlockPos(), dimension, offset);

        int minHeight = level.getMinBuildHeight();
        int maxHeight = level.getMaxBuildHeight();
        if (minCorner.getY() < minHeight) {
            minCorner = new BlockPos(minCorner.getX(), minHeight, minCorner.getZ());
        } else if (minCorner.getY() > maxHeight) {
            minCorner = new BlockPos(minCorner.getX(), maxHeight, minCorner.getZ());
        }
        if (maxCorner.getY() < minHeight) {
            maxCorner = new BlockPos(maxCorner.getX(), minHeight, maxCorner.getZ());
        } else if (maxCorner.getY() > maxHeight) {
            maxCorner = new BlockPos(maxCorner.getX(), maxHeight, maxCorner.getZ());
        }

        if (boxValid) {
            // Double check if the box is indeed still valid.
            if (minCorner.equals(data.minBox()) && maxCorner.equals(data.maxBox())) {
                return data;
            }
        }

        boxValid = true;
        cardType = ShapeCardItem.getType(shapeCard);

        cachedBlocks = null;
        cachedChunk = null;
        cachedVoidableBlocks.clear();
        data = data.withMinBox(minCorner).withMaxBox(maxCorner);
        data = restartScan(data);
        return data;
    }

    private Pair<BuilderData, SpaceChamberRepository.SpaceChamberChannel> calculateBox(BuilderData data) {
        ItemStack card = items.getStackInSlot(SLOT_TAB);
        if (card.isEmpty()) {
            return Pair.of(data, null);
        }

        ShapeCardData shapeData = card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA, ShapeCardData.DEFAULT);
        if (shapeData.channel() == -1) {
            return Pair.of(data, null);
        }

        SpaceChamberRepository repository = SpaceChamberRepository.get(level);
        SpaceChamberRepository.SpaceChamberChannel chamberChannel = repository.getChannel(shapeData.channel());
        if (chamberChannel == null) {
            return Pair.of(data, null);
        }

        data = calculateBox(data, shapeData.channel());

        if (!boxValid) {
            return Pair.of(data, null);
        }
        return Pair.of(data, chamberChannel);
    }

    private Map<BlockPos, BlockState> getCachedBlocks(ChunkPos chunk) {
        if ((chunk != null && !chunk.equals(cachedChunk)) || (chunk == null && cachedChunk != null)) {
            cachedBlocks = null;
        }

        if (cachedBlocks == null) {
            cachedBlocks = new HashMap<>();
            ItemStack shapeCard = items.getStackInSlot(SLOT_TAB);
            Shape shape = ShapeCardItem.getShape(shapeCard);
            boolean solid = ShapeCardItem.isSolid(shapeCard);
            BlockPos dimension = ShapeCardItem.getClampedDimension(shapeCard, BuilderConfiguration.maxBuilderDimension.get());
            BlockPos offset = ShapeCardItem.getClampedOffset(shapeCard, BuilderConfiguration.maxBuilderOffset.get());
            boolean forquarry = !ShapeCardItem.isNormalShapeCard(shapeCard);
            ShapeCardItem.composeFormula(shapeCard, shape.getFormulaFactory().get(), level, getBlockPos(), dimension, offset, cachedBlocks, BuilderConfiguration.maxSpaceChamberDimension.get() * BuilderConfiguration.maxSpaceChamberDimension.get() * BuilderConfiguration.maxSpaceChamberDimension.get(), solid, forquarry, chunk);
            cachedChunk = chunk;
        }
        return cachedBlocks;
    }

    private BuilderData handleBlockShaped(BuilderData data) {
        for (int i = 0; i < 100; i++) {
            if (data.scan() == null) {
                return data;
            }
            Map<BlockPos, BlockState> blocks = getCachedBlocks(new ChunkPos(data.scan().getX() >> 4, data.scan().getZ() >> 4));
            if (blocks.containsKey(data.scan())) {
                BlockState state = blocks.get(data.scan());
                Pair<BuilderData, Boolean> result = handleSingleBlock(data, state);
                data = result.getLeft();
                if (!result.getRight()) {
                    data = nextLocation(data);
                }
                return data;
            } else {
                data = nextLocation(data);
            }
        }
        return data;
    }

    private ShapeCardType getCardType() {
        if (cardType == ShapeCardType.CARD_UNKNOWN) {
            return ShapeCardItem.getType(items.getStackInSlot(SLOT_TAB));
        }
        return cardType;
    }

    // Return true if we have to wait at this spot.
    private Pair<BuilderData, Boolean> handleSingleBlock(BuilderData data, BlockState pickState) {
        if( level == null ) {
            return Pair.of(data, false);
        }

        BlockPos srcPos = data.scan();
        int sx = data.scan().getX();
        int sy = data.scan().getY();
        int sz = data.scan().getZ();
        if (!chunkLoad(sx, sz)) {
            // The chunk is not available and we could not chunkload it. We have to wait.
            return suspend(data, "Chunk not available!");
        }

        int rfNeeded = getCardType().getRfNeeded();

        BlockState state = null;
        if (getCardType() != ShapeCardType.CARD_SHAPE && getCardType() != ShapeCardType.CARD_PUMP_LIQUID) {
            state = level.getBlockState(srcPos);
            Block block = state.getBlock();
            if (!isEmpty(state, block)) {
                float hardness;
                if (isFluidBlock(block)) {
                    hardness = 1.0f;
                } else {
                    if (cachedVoidableBlocks.get().contains(block)) {
                        rfNeeded = (int) (BuilderConfiguration.builderRfPerQuarry.get() * BuilderConfiguration.voidShapeCardFactor.get());
                    }
                    hardness = state.getDestroySpeed(level, srcPos);
                }
                rfNeeded *= (int) ((hardness + 1) * 2);
            }
        }

        float factor = infusable.getInfusedFactor();
        rfNeeded = (int) (rfNeeded * (3.0f - factor) / 3.0f);

        if (rfNeeded > energyStorage.getMaxEnergyStored()) {
            // The energy needed is more then what the builder can handle. Skip this block
            return skip(data, "Block exceeds max power!");
        }

        if (rfNeeded > energyStorage.getEnergyStored()) {
            // Not enough energy.
            return suspend(data, "Not enough power!");
        }

        boolean result = getCardType().handleSingleBlock(this, rfNeeded, srcPos, state, pickState);
        return Pair.of(data, result);
    }

    public boolean buildBlock(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
        if( level == null) {
            return false;
        }

        BuilderData data = getData(BuilderModule.BUILDER_DATA);

        if (isEmptyOrReplacable(level, srcPos)) {
            TakeableItem item = createTakeableItem(level, srcPos, pickState);
            ItemStack stack = item.peek();
            if (stack.isEmpty()) {
                return waitOrSkip(data, "Cannot find block!\nor missing inventory\non top or below");    // We could not find a block. Wait
            }

            Player fakePlayer = harvester.get();
            BlockState newState = Tools.placeStackAt(fakePlayer, stack, level, srcPos, pickState);
            if (newState == null) {
                return waitOrSkip(data, "Cannot place block!");
            }
            if (!ItemStack.matches(stack, item.peek())) { // Did we actually use up whatever we were holding?
                if (!stack.isEmpty()) { // Are we holding something else that we should put back?
                    stack = item.takeAndReplace(stack); // First try to put our new item where we got what we placed
                    if (!stack.isEmpty()) { // If that didn't work, then try to put it anywhere it will fit
                        stack = insertItem(stack);
                        if (!stack.isEmpty()) { // If that still didn't work, then just drop whatever we're holding
                            level.addFreshEntity(new ItemEntity(level, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), stack));
                        }
                    }
                } else {
                    item.take(); // If we aren't holding anything, then just consume what we placed
                }
            }

            if (!isSilent()) {
                SoundType sound = newState.getBlock().getSoundType(newState, level, srcPos, fakePlayer);
                playPlaceSoundSafe(sound, level, newState, srcPos.getX(), srcPos.getY(), srcPos.getZ());
            }

            energyStorage.consumeEnergy(rfNeeded);
        }
        return skip(data);
    }

    private void playPlaceSoundSafe(SoundType sound, Level world, BlockState state, int x, int y, int z) {
        try {
            SoundTools.playSound(world, sound.getPlaceSound(), x, y, z, 1.0f, 1.0f);
        } catch (Exception e) {
            Logging.getLogger().error("Error getting soundtype from " + Tools.getId(state) + "! Please report to the mod owner!");
        }
    }

    private void playBreakSoundSafe(SoundType sound, Level world, BlockState state, int x, int y, int z) {
        try {
            SoundTools.playSound(world, sound.getBreakSound(), x, y, z, 1.0f, 1.0f);
        } catch (Exception e) {
            Logging.getLogger().error("Error getting soundtype from " + Tools.getId(state) + "! Please report to the mod owner!");
        }
    }

    private Set<Block> getCachedVoidableBlocks() {
        ItemStack card = items.getStackInSlot(SLOT_TAB);
        if (!card.isEmpty() && card.getItem() instanceof ShapeCardItem) {
            return ShapeCardItem.getVoidedBlocks(card);
        } else {
            return Collections.emptySet();
        }
    }

    private void clearOrDirtBlock(int rfNeeded, BlockPos spos, BlockState srcState, boolean clear) {
        energyStorage.consumeEnergy(rfNeeded);
        if (!isSilent()) {
            SoundType soundType = srcState.getBlock().getSoundType(srcState, level, spos, null);
            playBreakSoundSafe(soundType, level, srcState, spos.getX(), spos.getY(), spos.getZ());
        }
        if (srcState.is(DONT_REMOVE_ME_TAG)) {
            return;
        }
        if (clear) {
            level.setBlock(spos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        } else {
            level.setBlock(spos, getReplacementBlock(), Block.UPDATE_CLIENTS);       // No block update!
        }
    }

    private BlockState getReplacementBlock() {
        return BuilderConfiguration.getQuarryReplace();
    }

    public boolean silkQuarryBlock(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
        return commonQuarryBlock(true, rfNeeded, srcPos, srcState);
    }

    private Predicate<ItemStack> createFilterCache() {
        return FilterModuleItem.getCache(items.getStackInSlot(SLOT_FILTER));
    }

    private Pair<BuilderData, Boolean> allowedToBreak(BuilderData data, BlockState state, Level world, BlockPos pos, Player player) {
        if (!state.getBlock().canEntityDestroy(state, world, pos, player)) {
            return skip(data, "Cannot destroy!\nAre fake players\nallowed?");
        }
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, player);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return skip(data, "Break was canceled!");
        }
        return Pair.of(data, true);
    }

    private static boolean allowedToBreakS(BlockState state, Level world, BlockPos pos, Player player) {
        if (!state.getBlock().canEntityDestroy(state, world, pos, player)) {
            return false;
        }
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, player);
        NeoForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    public boolean quarryBlock(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
        return commonQuarryBlock(false, rfNeeded, srcPos, srcState);
    }

    private static ItemStack getHarvesterTool(Level level, boolean silk, int fortune) {
        Registry<Enchantment> enchantments = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        if (silk) {
            if (TOOL_SILK == null || TOOL_SILK.isEmpty()) {
                TOOL_SILK = new ItemStack(BuilderModule.SUPER_HARVESTING_TOOL.get());
                TOOL_SILK.enchant(enchantments.getHolderOrThrow(Enchantments.SILK_TOUCH), 1);
            }
            return TOOL_SILK;
        } else if (fortune > 0) {
            if (TOOL_FORTUNE == null || TOOL_FORTUNE.isEmpty()) {
                TOOL_FORTUNE = new ItemStack(BuilderModule.SUPER_HARVESTING_TOOL.get());
                TOOL_FORTUNE.enchant(enchantments.getHolderOrThrow(Enchantments.FORTUNE), fortune);
            }
            return TOOL_FORTUNE;

        } else {
            if (TOOL_NORMAL == null || TOOL_NORMAL.isEmpty()) {
                TOOL_NORMAL = new ItemStack(BuilderModule.SUPER_HARVESTING_TOOL.get());
            }
            return TOOL_NORMAL;
        }
    }

    private boolean commonQuarryBlock(boolean silk, int rfNeeded, BlockPos srcPos, BlockState srcState) {
        BuilderData data = getData(BuilderModule.BUILDER_DATA);
        Block block = srcState.getBlock();
        int xCoord = getBlockPos().getX();
        int yCoord = getBlockPos().getY();
        int zCoord = getBlockPos().getZ();
        int sx = srcPos.getX();
        int sy = srcPos.getY();
        int sz = srcPos.getZ();
        if (sx >= xCoord - 1 && sx <= xCoord + 1 && sy >= yCoord - 1 && sy <= yCoord + 1 && sz >= zCoord - 1 && sz <= zCoord + 1) {
            // Skip a 3x3x3 block around the builder.
            return skip(data);
        }
        if (isEmpty(srcState, block)) {
            return skip(data);
        }
        if (srcState.getDestroySpeed(level, srcPos) >= 0) {
            boolean clear = getCardType().isClearing();
            if ((!clear) && srcState == getReplacementBlock()) {
                // We can skip dirt if we are not clearing.
                return skip(data);
            }
            if ((!BuilderConfiguration.quarryTileEntities.get()) && level.getBlockEntity(srcPos) != null) {
                // Skip tile entities
                return skip(data);
            }

            Player fakePlayer = harvester.get();
            Pair<BuilderData, Boolean> result = allowedToBreak(data, srcState, level, srcPos, fakePlayer);
            data = result.getLeft();
            setData(BuilderModule.BUILDER_DATA, data);
            if (result.getRight()) {
                ItemStack filter = items.getStackInSlot(SLOT_FILTER);
                if (!filter.isEmpty()) {
                    if (filterCache.get() != null) {
                        boolean match;
                        try {
                            match = filterCache.get().test(block.getCloneItemStack(level, srcPos, srcState));
                        } catch (Exception e) {
                            // In case block.getItem() fails (like can happen for banner blocks because the getItem()
                            // for that fails on servers due to calling a client-side method)
                            match = false;
                        }
                        if (!match) {
                            energyStorage.consumeEnergy(Math.min(rfNeeded, BuilderConfiguration.builderRfPerSkipped.get()));
                            return skip(data);   // Skip this
                        }
                    }
                }
                if (!cachedVoidableBlocks.get().contains(block)) {
                    if (!overflowItems.isEmpty()) {
                        // Don't harvest any new blocks if we're still overflowing with the drops from a previous block
                        return waitOrSkip(data, "Not enough room!\nor no usable storage\non top or below!");
                    }

                    int fortune = getCardType().isFortune() ? 3 : 0;
                    LootParams.Builder builder = new LootParams.Builder((ServerLevel) level)
//                            .withRandom(level.random)
                            .withParameter(LootContextParams.ORIGIN, new Vec3(srcPos.getX(), srcPos.getY(), srcPos.getZ()))
                            .withParameter(LootContextParams.TOOL, getHarvesterTool(level, silk, fortune))
                            .withParameter(LootContextParams.THIS_ENTITY, fakePlayer)
                            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, level.getBlockEntity(srcPos));
                    if (fortune > 0) {
                        builder.withLuck(fortune);
                    }
                    List<ItemStack> drops = srcState.getDrops(builder);
                    if (checkValidItems(block, drops) && !insertItems(drops)) {
                        clearOrDirtBlock(rfNeeded, srcPos, srcState, clear);
                        return waitOrSkip(data, "Not enough room!\nor no usable storage\non top or below!");    // Not enough room. Wait
                    }
                }
                clearOrDirtBlock(rfNeeded, srcPos, srcState, clear);
            } else {
                return waitOrSkip(data, data.lastError());
            }
        }
        return false;
    }

    private static boolean isFluidBlock(Block block) {
        return block instanceof LiquidBlock;
    }

    private static int getFluidLevel(BlockState srcState) {
        if (srcState.getBlock() instanceof LiquidBlock) {
            return srcState.getValue(LiquidBlock.LEVEL);
        }
        return -1;
    }

    public boolean placeLiquidBlock(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
        BuilderData data = getData(BuilderModule.BUILDER_DATA);
        if (isEmptyOrReplacable(level, srcPos)) {
            FluidStack stack = consumeLiquid(level, srcPos);
            if (stack.isEmpty()) {
                return waitOrSkip(data, "Cannot find liquid!\nor no usable tank\nabove or below");    // We could not find a block. Wait
            }

            Fluid fluid = stack.getFluid();
            if (fluid.getFluidType().isVaporizedOnPlacement(level, srcPos, stack) && level.dimensionType().ultraWarm()) {
                fluid.getFluidType().onVaporize(null, level, srcPos, stack);
            } else {
                // We assume here the liquid is placable.
                Block block = fluid.defaultFluidState().createLegacyBlock().getBlock();   // @todo 1.14 check blockstate
                Player fakePlayer = harvester.get();
                level.setBlock(srcPos, block.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);

                if (!isSilent()) {
                    SoundType soundType = block.getSoundType(block.defaultBlockState(), level, srcPos, fakePlayer);
                    playPlaceSoundSafe(soundType, level, block.defaultBlockState(), srcPos.getX(), srcPos.getY(), srcPos.getZ());
                }
            }

            energyStorage.consumeEnergy(rfNeeded);
        }
        return skip(data);
    }

    public boolean pumpBlock(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
        BuilderData data = getData(BuilderModule.BUILDER_DATA);

        Block block = srcState.getBlock();
        FluidState fluidState = level.getFluidState(srcPos);

        if (fluidState.isEmpty()) {
            return skip(data);
        }

        if (!fluidState.isSource()) {
            return skip(data);
        }

        FluidStack fluidStack = FluidTools.pickupFluidBlock(level, srcPos, s -> false, () -> {
        });
        if (fluidStack.isEmpty()) {
            return skip(data);
        }

        // @todo 1.14, probably no longer needed?
//        if (!isFluidBlock(block)) {
//            return skip();
//        }

        // @todo 1.14, probably no longer needed?
//        if (getFluidLevel(srcState) != 0) {
//            return skip();
//        }

        if (srcState.getDestroySpeed(level, srcPos) >= 0) {
            Player fakePlayer = harvester.get();
            Pair<BuilderData, Boolean> result = allowedToBreak(data, srcState, level, srcPos, fakePlayer);
            data = result.getLeft();
            setData(BuilderModule.BUILDER_DATA, data);
            if (result.getRight()) {
                if (checkAndInsertFluids(fluidStack)) {
                    energyStorage.consumeEnergy(rfNeeded);
                    boolean clear = getCardType().isClearing();
                    FluidTools.pickupFluidBlock(level, srcPos, s -> true, () -> {
                        if (clear) {
                            level.setBlock(srcPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                        } else {
                            level.setBlock(srcPos, getReplacementBlock(), Block.UPDATE_CLIENTS);       // No block update!
                        }
                    });
                    if (!isSilent()) {
                        SoundType soundType = block.getSoundType(srcState, level, srcPos, fakePlayer);
                        playBreakSoundSafe(soundType, level, srcState, srcPos.getX(), srcPos.getY(), srcPos.getZ());
                    }
                    return skip(data);
                }
                return waitOrSkip(data, "No room for liquid\nor no usable tank\nabove or below!");    // No room in tanks or not a valid tank: wait
            } else {
                return waitOrSkip(data, data.lastError());
            }
        }
        return skip(data);
    }

    public boolean voidBlock(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
        BuilderData data = getData(BuilderModule.BUILDER_DATA);
        Block block = srcState.getBlock();
        int xCoord = getBlockPos().getX();
        int yCoord = getBlockPos().getY();
        int zCoord = getBlockPos().getZ();
        int sx = srcPos.getX();
        int sy = srcPos.getY();
        int sz = srcPos.getZ();
        if (sx >= xCoord - 1 && sx <= xCoord + 1 && sy >= yCoord - 1 && sy <= yCoord + 1 && sz >= zCoord - 1 && sz <= zCoord + 1) {
            // Skip a 3x3x3 block around the builder.
            return skip(data);
        }
        Player fakePlayer = harvester.get();
        Pair<BuilderData, Boolean> result = allowedToBreak(data, srcState, level, srcPos, fakePlayer);
        data = result.getLeft();
        setData(BuilderModule.BUILDER_DATA, data);
        if (result.getRight()) {
            assert level != null;
            if (srcState.getDestroySpeed(level, srcPos) >= 0) {
                ItemStack filter = items.getStackInSlot(SLOT_FILTER);
                if (!filter.isEmpty()) {
                    if (filterCache.get() != null) {
                        boolean match = filterCache.get().test(block.getCloneItemStack(level, srcPos, srcState));
                        if (!match) {
                            energyStorage.consumeEnergy(Math.min(rfNeeded, BuilderConfiguration.builderRfPerSkipped.get()));
                            return skip(data);   // Skip this
                        }
                    }
                }

                if (!isSilent()) {
                    SoundType soundType = block.getSoundType(srcState, level, srcPos, fakePlayer);
                    playBreakSoundSafe(soundType, level, srcState, sx, sy, sz);
                }
                level.setBlockAndUpdate(srcPos, Blocks.AIR.defaultBlockState());
                energyStorage.consumeEnergy(rfNeeded);
            }
        } else {
            return waitOrSkip(data, data.lastError());
        }
        return skip(data);
    }

    private BuilderData handleBlock(BuilderData data, Level world) {
        BlockPos srcPos = data.scan();
        BlockPos destPos = sourceToDest(data.scan());
        int x = data.scan().getX();
        int y = data.scan().getY();
        int z = data.scan().getZ();
        int destX = destPos.getX();
        int destY = destPos.getY();
        int destZ = destPos.getZ();

        switch (getMode()) {
            case MODE_COPY -> copyBlock(world, srcPos, world, destPos);
            case MODE_MOVE -> {
                if (hasEntityMode()) {
                    moveEntities(world, x, y, z, world, destX, destY, destZ);
                }
                moveBlock(world, srcPos, world, destPos, getRotate());
            }
            case MODE_BACK -> {
                if (hasEntityMode()) {
                    moveEntities(world, destX, destY, destZ, world, x, y, z);
                }
                moveBlock(world, destPos, world, srcPos, oppositeRotate());
            }
            case MODE_SWAP -> {
                if (hasEntityMode()) {
                    swapEntities(world, x, y, z, world, destX, destY, destZ);
                }
                swapBlock(world, srcPos, world, destPos);
            }
        }

        data = nextLocation(data);
        return data;
    }

    private static final Random random = new Random();

    // Also works if block is null and just picks the first available block.
    private TakeableItem findBlockTakeableItem(IItemHandler inventory, Level srcWorld, BlockPos srcPos, BlockState state) {
        if (state == null) {
            // We are not looking for a specific block. Pick a random one out of the chest.
            List<Integer> slots = new ArrayList<>();
            for (int i = 0; i < inventory.getSlots(); i++) {
                if (isPlacable(inventory.getStackInSlot(i))) {
                    slots.add(i);
                }
            }
            if (!slots.isEmpty()) {
                return new TakeableItem(inventory, slots.get(random.nextInt(slots.size())));
            }
        } else {
            Block block = state.getBlock();
            ItemStack srcItem = block.getCloneItemStack(srcWorld, srcPos, state);
            if (isPlacable(srcItem)) {
                for (int i = 0; i < inventory.getSlots(); i++) {
                    ItemStack stack = inventory.getStackInSlot(i);
                    if (!stack.isEmpty() && ItemStack.isSameItem(stack, srcItem)) {
                        return new TakeableItem(inventory, i);
                    }
                }
            }
        }
        return TakeableItem.EMPTY;
    }

    private boolean isPlacable(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        return item instanceof BlockItem || item instanceof SpecialPlantable;
    }

    // the items that we try to insert.
    private boolean checkValidItems(Block block, List<ItemStack> items) {
        for (ItemStack stack : items) {
            if ((!stack.isEmpty()) && stack.getItem() == null) {
                Logging.logError("Builder tried to quarry " + Tools.getId(block).toString() + " and it returned null item!");
                Broadcaster.broadcast(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), "Builder tried to quarry "
                                + Tools.getId(block).toString() + " and it returned null item!\nPlease report to mod author!",
                        10);
                return false; // We don't wait for this. Just skip the item
            }
        }
        return true;
    }

    private boolean checkAndInsertFluids(FluidStack fluid) {
        if (checkFluidTank(fluid, getBlockPos().above(), Direction.DOWN)) {
            return true;
        }
        if (checkFluidTank(fluid, getBlockPos().below(), Direction.UP)) {
            return true;
        }
        return false;
    }

    private boolean checkFluidTank(FluidStack fluidStack, BlockPos up, Direction side) {
        BlockEntity te = level.getBlockEntity(up);
        if (te != null) {
            IFluidHandler h = level.getCapability(Capabilities.FluidHandler.BLOCK, up, side);
            if (h != null) {
                int amount = h.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE);
                if (amount == 1000) {
                    h.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    private final LazyList<ItemStack> couldntHandle1 = new LazyList<>();
    private final LazyList<ItemStack> couldntHandle2 = new LazyList<>();

    // Tries to insert the items in the given item handler (if the te represents an item handler)
    // All items that could not be inserted are put on the couldntHandle list (for example, because
    // there is no item handler or the item handler is full)
    private void handleItemInsertion(@Nullable BlockEntity te, Direction direction, List<ItemStack> items, LazyList<ItemStack> couldntHandle) {
        if (te == null) {
            couldntHandle.copyList(items);
            return;
        }
        IItemHandler capability = te.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, te.getBlockPos(), direction);
        if (capability == null) {
            couldntHandle.copyList(items);
            return;
        }
        couldntHandle.clear();
        capability = te.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, te.getBlockPos(), Direction.DOWN);
        if (capability != null) {
            for (ItemStack item : items) {
                ItemStack overflow = ItemHandlerHelper.insertItem(capability, item, false);
                if (!overflow.isEmpty()) {
                    couldntHandle.add(overflow);
                }
            }
        }
    }

    private boolean insertItems(List<ItemStack> items) {
        handleItemInsertion(level.getBlockEntity(worldPosition.above()), Direction.DOWN, items, couldntHandle1);
        if (couldntHandle1.isEmpty()) {
            // All ok
            return true;
        }

        handleItemInsertion(level.getBlockEntity(worldPosition.below()), Direction.UP, couldntHandle1.getList(), couldntHandle2);
        if (couldntHandle2.isEmpty()) {
            // Now it is ok
            return true;
        }

        // After trying both the top and the bottom chest there are still items remaining
        overflowItems.copyList(couldntHandle2.getList());
        return false;
    }

    // Return what could not be inserted
    private ItemStack insertItem(@Nonnull ItemStack s) {
        s = InventoryTools.insertItem(level, getBlockPos(), Direction.UP, s);
        if (!s.isEmpty()) {
            s = InventoryTools.insertItem(level, getBlockPos(), Direction.DOWN, s);
        }
        return s;
    }

    private static class TakeableItem {
        private final IItemHandler itemHandler;
        private final int slot;
        private final ItemStack peekStack;

        public static final TakeableItem EMPTY = new TakeableItem();

        private TakeableItem() {
            this.itemHandler = null;
            this.slot = -1;
            this.peekStack = ItemStack.EMPTY;
        }

        public TakeableItem(IItemHandler itemHandler, int slot) {
            Validate.inclusiveBetween(0, itemHandler.getSlots() - 1, slot);
            this.itemHandler = itemHandler;
            this.slot = slot;
            this.peekStack = itemHandler.extractItem(slot, 1, true);
        }

        public ItemStack peek() {
            return peekStack.copy();
        }

        public void take() {
            if (itemHandler != null) {
                itemHandler.extractItem(slot, 1, false);
            }
        }

        public ItemStack takeAndReplace(ItemStack replacement) {
            if (itemHandler != null) {
                itemHandler.extractItem(slot, 1, false);
                return itemHandler.insertItem(slot, replacement, false);
            }
            return replacement;
        }
    }

    /**
     * Create a way to let you consume a block out of an inventory. Returns a blockstate
     * from that inventory or else null if nothing could be found.
     * If the given blockstate parameter is null then a random block will be
     * returned. Otherwise the returned block has to match.
     */
    private TakeableItem createTakeableItem(Direction direction, Level srcWorld, BlockPos srcPos, BlockState state) {
        BlockEntity te = level.getBlockEntity(getBlockPos().relative(direction));
        if (te != null) {
            IItemHandler h = level.getCapability(Capabilities.ItemHandler.BLOCK, te.getBlockPos(), direction.getOpposite());
            if (h != null) {
                return findBlockTakeableItem(h, srcWorld, srcPos, state);
            }
        }
        return TakeableItem.EMPTY;
    }

    @Nonnull
    private FluidStack consumeLiquid(Level srcWorld, BlockPos srcPos) {
        FluidStack b = consumeLiquid(Direction.UP, srcWorld, srcPos);
        if (b.isEmpty()) {
            b = consumeLiquid(Direction.DOWN, srcWorld, srcPos);
        }
        return b;
    }

    @Nonnull
    private FluidStack consumeLiquid(Direction direction, Level srcWorld, BlockPos srcPos) {
        BlockEntity te = level.getBlockEntity(getBlockPos().relative(direction));
        if (te != null) {
            IFluidHandler fluid = level.getCapability(Capabilities.FluidHandler.BLOCK, te.getBlockPos(), direction.getOpposite());
            if (fluid != null) {
                fluid = level.getCapability(Capabilities.FluidHandler.BLOCK, te.getBlockPos(), null);
            }
            if (fluid != null) {
                return findAndConsumeLiquid(fluid, srcWorld, srcPos);
            }
        }
        return FluidStack.EMPTY;
    }

    @Nonnull
    private FluidStack findAndConsumeLiquid(IFluidHandler tank, Level srcWorld, BlockPos srcPos) {
        for (int i = 0; i < tank.getTanks(); i++) {
            FluidStack contents = tank.getFluidInTank(i);
            if (!contents.isEmpty()) {
                if (contents.getFluid() != null) {
                    if (contents.getAmount() >= 1000) {
                        return tank.drain(new FluidStack(contents.getFluidHolder(), 1000), IFluidHandler.FluidAction.EXECUTE);
                    }
                }
            }
        }
        return FluidStack.EMPTY;
    }

    private TakeableItem createTakeableItem(Level srcWorld, BlockPos srcPos, BlockState state) {
        TakeableItem b = createTakeableItem(Direction.UP, srcWorld, srcPos, state);
        if (b.peek().isEmpty()) {
            b = createTakeableItem(Direction.DOWN, srcWorld, srcPos, state);
        }
        return b;
    }

    public static BlockInformation getBlockInformation(Player fakePlayer, Level world, BlockPos pos, Block block, BlockEntity tileEntity) {
        BlockState state = world.getBlockState(pos);
        if (isEmpty(state, block)) {
            return BlockInformation.FREE;
        }

        if (!allowedToBreakS(state, world, pos, fakePlayer)) {
            return BlockInformation.INVALID;
        }

        BlockInformation blockInformation = BlockInformation.getBlockInformation(block);
        if (tileEntity != null) {
            switch (BuilderConfiguration.teMode.get()) {
                case MOVE_FORBIDDEN:
                    return BlockInformation.INVALID;
                case MOVE_WHITELIST:
                    if (blockInformation == null || blockInformation.getBlockLevel() == SupportBlock.SupportStatus.STATUS_ERROR) {
                        return BlockInformation.INVALID;
                    }
                    break;
                case MOVE_BLACKLIST:
                    if (blockInformation != null && blockInformation.getBlockLevel() == SupportBlock.SupportStatus.STATUS_ERROR) {
                        return BlockInformation.INVALID;
                    }
                    break;
                case MOVE_ALLOWED:
                    break;
            }
        }
        if (blockInformation != null) {
            return blockInformation;
        }
        return BlockInformation.OK;
    }

    private SupportBlock.SupportStatus isMovable(Player harvester, Level world, BlockPos pos, Block block, BlockEntity tileEntity) {
        return getBlockInformation(harvester, world, pos, block, tileEntity).getBlockLevel();
    }

    public static boolean isEmptyOrReplacable(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (state.canBeReplaced()) {
            return true;
        }
        return isEmpty(state, block);
    }

    // True if this block can just be overwritten (i.e. are or support block)
    public static boolean isEmpty(BlockState state, Block block) {
        if (block == null) {
            return true;
        }
        if (state.isAir()) {
            return true;
        }
        if (block == BuilderModule.SUPPORT.get()) {
            return true;
        }
        return false;
    }

    private void clearBlock(Level world, BlockPos pos) {
        if (hasSupportMode()) {
            world.setBlock(pos, BuilderModule.SUPPORT.get().defaultBlockState(), Block.UPDATE_ALL);
        } else {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private RotateMode oppositeRotate() {
        return switch (getRotate()) {
            case ROTATE_90 -> RotateMode.ROTATE_270;
            case ROTATE_270 -> RotateMode.ROTATE_90;
            default -> getRotate();
        };
    }

    private void copyBlock(Level srcWorld, BlockPos srcPos, Level destWorld, BlockPos destPos) {
        long rf = energyStorage.getEnergy();
        float factor = infusable.getInfusedFactor();
        int rfNeeded = (int) (BuilderConfiguration.builderRfPerOperation.get() * getDimensionCostFactor(srcWorld, destWorld) * (4.0f - factor) / 4.0f);
        if (rfNeeded > rf) {
            // Not enough energy.
            return;
        }

        if (isEmptyOrReplacable(destWorld, destPos)) {
            if (srcWorld.isEmptyBlock(srcPos)) {
                return;
            }
            BlockState srcState = srcWorld.getBlockState(srcPos);
            TakeableItem takeableItem = createTakeableItem(srcWorld, srcPos, srcState);
            ItemStack consumedStack = takeableItem.peek();
            if (consumedStack.isEmpty()) {
                return;
            }

            Player fakePlayer = harvester.get();
            BlockState newState = Tools.placeStackAt(fakePlayer, consumedStack, destWorld, destPos, srcState);
            if (newState == null) {
                // This block can't be placed
                return;
            }
            if (destWorld.getBlockState(destPos).is(newState.getBlock())) {
                BlockEntity srcTileEntity = srcWorld.getBlockEntity(srcPos);
                CompoundTag srcTC = null;
                if (srcTileEntity != null) {
                    Block srcBlock = srcState.getBlock();
                    srcTC = copyBlockNBTData(srcBlock,srcTileEntity);
                }
                destWorld.setBlock(destPos, newState, Block.UPDATE_ALL);  // placeBlockAt can reset the orientation. Restore it here
                if (srcTC != null) {
                    transferBlockNBTDataTo(destWorld, srcTC, destPos, srcState);
                }
            }

            if (!ItemStack.matches(consumedStack, takeableItem.peek())) { // Did we actually use up whatever we were holding?
                if (!consumedStack.isEmpty()) { // Are we holding something else that we should put back?
                    consumedStack = takeableItem.takeAndReplace(consumedStack); // First try to put our new item where we got what we placed
                    if (!consumedStack.isEmpty()) { // If that didn't work, then try to put it anywhere it will fit
                        consumedStack = insertItem(consumedStack);
                        if (!consumedStack.isEmpty()) { // If that still didn't work, then just drop whatever we're holding
                            level.addFreshEntity(new ItemEntity(level, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), consumedStack));
                        }
                    }
                } else {
                    takeableItem.take(); // If we aren't holding anything, then just consume what we placed
                }
            }

            if (!isSilent()) {
                SoundType soundType = newState.getBlock().getSoundType(newState, destWorld, destPos, fakePlayer);
                playPlaceSoundSafe(soundType, destWorld, newState, destPos.getX(), destPos.getY(), destPos.getZ());
            }

            energyStorage.consumeEnergy(rfNeeded);
        }
    }

    private double getDimensionCostFactor(Level world, Level destWorld) {
        return (Objects.equals(destWorld.dimension(), world.dimension())) ? 1.0 : BuilderConfiguration.dimensionCostFactor.get();
    }

    private boolean consumeEntityEnergy(int rfNeeded, int rfNeededPlayer, Entity entity) {
        long rf = energyStorage.getEnergy();
        int rfn;
        if (entity instanceof Player) {
            rfn = rfNeededPlayer;
        } else {
            rfn = rfNeeded;
        }
        if (rfn > rf) {
            // Not enough energy.
            return true;
        } else {
            energyStorage.consumeEnergy(rfn);
        }
        return false;
    }

    private void moveEntities(Level world, int x, int y, int z, Level destWorld, int destX, int destY, int destZ) {
        float factor = infusable.getInfusedFactor();
        int rfNeeded = (int) (BuilderConfiguration.builderRfPerEntity.get() * getDimensionCostFactor(world, destWorld) * (4.0f - factor) / 4.0f);
        int rfNeededPlayer = (int) (BuilderConfiguration.builderRfPerPlayer.get() * getDimensionCostFactor(world, destWorld) * (4.0f - factor) / 4.0f);

        // Check for entities.
        List<Entity> entities = world.getEntities(null, new AABB(x - .1, y - .1, z - .1, x + 1.1, y + 1.1, z + 1.1));
        for (Entity entity : entities) {

            if (consumeEntityEnergy(rfNeeded, rfNeededPlayer, entity)) {
                return;
            }

            double newX = destX + (entity.getX() - x);
            double newY = destY + (entity.getY() - y);
            double newZ = destZ + (entity.getZ() - z);

            teleportEntity(world, destWorld, entity, newX, newY, newZ);
        }
    }

    private void swapEntities(Level world, int x, int y, int z, Level destWorld, int destX, int destY, int destZ) {
        float factor = infusable.getInfusedFactor();
        int rfNeeded = (int) (BuilderConfiguration.builderRfPerEntity.get() * getDimensionCostFactor(world, destWorld) * (4.0f - factor) / 4.0f);
        int rfNeededPlayer = (int) (BuilderConfiguration.builderRfPerPlayer.get() * getDimensionCostFactor(world, destWorld) * (4.0f - factor) / 4.0f);

        // Check for entities.
        List<Entity> entitiesSrc = world.getEntities(null, new AABB(x, y, z, x + 1, y + 1, z + 1));
        List<Entity> entitiesDst = destWorld.getEntities(null, new AABB(destX, destY, destZ, destX + 1, destY + 1, destZ + 1));
        for (Entity entity : entitiesSrc) {
            if (isEntityInBlock(x, y, z, entity)) {
                if (consumeEntityEnergy(rfNeeded, rfNeededPlayer, entity)) {
                    return;
                }

                double newX = destX + (entity.getX() - x);
                double newY = destY + (entity.getY() - y);
                double newZ = destZ + (entity.getZ() - z);
                teleportEntity(world, destWorld, entity, newX, newY, newZ);
            }
        }
        for (Entity entity : entitiesDst) {
            if (isEntityInBlock(destX, destY, destZ, entity)) {
                if (consumeEntityEnergy(rfNeeded, rfNeededPlayer, entity)) {
                    return;
                }

                double newX = x + (entity.getX() - destX);
                double newY = y + (entity.getY() - destY);
                double newZ = z + (entity.getZ() - destZ);
                teleportEntity(destWorld, world, entity, newX, newY, newZ);
            }
        }
    }

    private void teleportEntity(Level world, Level destWorld, Entity entity, double newX, double newY, double newZ) {
        // @todo 1.14 use api to check for allow teleportation
//        if (!TeleportationTools.allowTeleport(entity, world.getDimension().getType().getId(), entity.getPosition(), destWorld.getDimension().getType().getId(), new BlockPos(newX, newY, newZ))) {
//            return;
//        }
        TeleportationTools.teleportEntity(entity, destWorld, newX, newY, newZ, null);
    }


    private boolean isEntityInBlock(int x, int y, int z, Entity entity) {
        if (entity.getX() >= x && entity.getX() < x + 1 && entity.getY() >= y && entity.getY() < y + 1 && entity.getZ() >= z && entity.getZ() < z + 1) {
            return true;
        }
        return false;
    }

    private CompoundTag copyBlockNBTData(Block srcBlock, BlockEntity srcTileEntity) {
        if (BlockInformation.shouldTransferNBT(srcBlock)) {
            RegistryAccess access = srcTileEntity.getLevel().registryAccess();
            return srcTileEntity.saveWithFullMetadata(access);
        }
        return null;
    }

    private void moveBlock(Level srcWorld, BlockPos srcPos, Level destWorld, BlockPos destPos, RotateMode rotMode) {
        BlockState oldDestState = destWorld.getBlockState(destPos);
        Block oldDestBlock = oldDestState.getBlock();
        if (isEmpty(oldDestState, oldDestBlock)) {
            BlockState srcState = srcWorld.getBlockState(srcPos);
            Block srcBlock = srcState.getBlock();
            if (isEmpty(srcState, srcBlock)) {
                return;
            }
            BlockEntity srcTileEntity = srcWorld.getBlockEntity(srcPos);
            CompoundTag tc = null;
            if (srcTileEntity != null) {
                tc = copyBlockNBTData(srcBlock,srcTileEntity);
            }
            BlockInformation srcInformation = getBlockInformation(harvester.get(), srcWorld, srcPos, srcBlock, srcTileEntity);
            if (srcInformation.getBlockLevel() == SupportBlock.SupportStatus.STATUS_ERROR) {
                return;
            }

            long rf = energyStorage.getEnergy();
            float factor = infusable.getInfusedFactor();
            int rfNeeded = (int) (BuilderConfiguration.builderRfPerOperation.get() * getDimensionCostFactor(srcWorld, destWorld) * srcInformation.getCostFactor() * (4.0f - factor) / 4.0f);
            if (rfNeeded > rf) {
                // Not enough energy.
                return;
            } else {
                energyStorage.consumeEnergy(rfNeeded);
            }

            if (srcTileEntity != null) {
                srcWorld.removeBlockEntity(srcPos);
            }
            clearBlock(srcWorld, srcPos);

            destWorld.setBlock(destPos, srcState, Block.UPDATE_ALL);
            if (tc != null) {
                configureTileEntityNBT(destWorld, tc, destPos, srcState);
            }
            if (!isSilent()) {
                SoundType srcSoundType = srcBlock.getSoundType(srcState, srcWorld, srcPos, null);
                playBreakSoundSafe(srcSoundType, srcWorld, srcState, srcPos.getX(), srcPos.getY(), srcPos.getZ());
                SoundType dstSoundtype = srcBlock.getSoundType(srcState, destWorld, destPos, null);
                playPlaceSoundSafe(dstSoundtype, destWorld, srcState, destPos.getX(), destPos.getY(), destPos.getZ());
            }
        }
    }

    private void setTileEntityNBT(Level destWorld, CompoundTag tc, BlockPos destpos, BlockState newDestState) {
        tc.putInt("x", destpos.getX());
        tc.putInt("y", destpos.getY());
        tc.putInt("z", destpos.getZ());
        BlockEntity tileEntity = BlockEntity.loadStatic(destpos, newDestState, tc, destWorld.registryAccess());
        if (tileEntity != null) {
            destWorld.getChunk(destpos).setBlockEntity(tileEntity);
            tileEntity.setChanged();
            destWorld.sendBlockUpdated(destpos, newDestState, newDestState, Block.UPDATE_ALL);
        }
    }

    private void configureTileEntityNBT(Level destWorld, CompoundTag tc, BlockPos destpos, BlockState newDestState) {
        BlockEntity tileEntity = destWorld.getBlockEntity(destpos);
        if (tileEntity != null) {
            tileEntity.loadWithComponents(tc, destWorld.registryAccess());
            tileEntity.setChanged();
            destWorld.sendBlockUpdated(destpos, newDestState, newDestState, Block.UPDATE_ALL);
        }
    }
    private void transferBlockNBTDataTo(Level destWorld, CompoundTag tc, BlockPos destpos, BlockState newDestState) {
        tc.putInt("x", destpos.getX());
        tc.putInt("y", destpos.getY());
        tc.putInt("z", destpos.getZ());
        configureTileEntityNBT(destWorld, tc, destpos, newDestState);
    }

    private void swapBlock(Level srcWorld, BlockPos srcPos, Level destWorld, BlockPos dstPos) {
        BlockState oldSrcState = srcWorld.getBlockState(srcPos);
        Block srcBlock = oldSrcState.getBlock();
        BlockEntity srcTileEntity = srcWorld.getBlockEntity(srcPos);

        BlockState oldDstState = destWorld.getBlockState(dstPos);
        Block dstBlock = oldDstState.getBlock();
        BlockEntity dstTileEntity = destWorld.getBlockEntity(dstPos);
        CompoundTag srcTC = null;
        CompoundTag dstTC = null;
        if (srcTileEntity != null) {
            srcTC = copyBlockNBTData(srcBlock,srcTileEntity);
        }
        if (dstTileEntity != null) {
            dstTC = copyBlockNBTData(dstBlock,dstTileEntity);
        }

        if (isEmpty(oldSrcState, srcBlock) && isEmpty(oldDstState, dstBlock)) {
            return;
        }

        BlockInformation srcInformation = getBlockInformation(harvester.get(), srcWorld, srcPos, srcBlock, srcTileEntity);
        if (srcInformation.getBlockLevel() == SupportBlock.SupportStatus.STATUS_ERROR) {
            return;
        }

        BlockInformation dstInformation = getBlockInformation(harvester.get(), destWorld, dstPos, dstBlock, dstTileEntity);
        if (dstInformation.getBlockLevel() == SupportBlock.SupportStatus.STATUS_ERROR) {
            return;
        }

        long rf = energyStorage.getEnergy();
        float factor = infusable.getInfusedFactor();
        int rfNeeded = (int) (BuilderConfiguration.builderRfPerOperation.get() * getDimensionCostFactor(srcWorld, destWorld) * srcInformation.getCostFactor() * (4.0f - factor) / 4.0f);
        rfNeeded += (int) (BuilderConfiguration.builderRfPerOperation.get() * getDimensionCostFactor(srcWorld, destWorld) * dstInformation.getCostFactor() * (4.0f - factor) / 4.0f);
        if (rfNeeded > rf) {
            // Not enough energy.
            return;
        } else {
            energyStorage.consumeEnergy(rfNeeded);
        }
        // @todo 1.18 IMPORTANT: HOW TO MOVE BLOCK ENTITY!

        srcWorld.removeBlockEntity(srcPos);
        srcWorld.setBlockAndUpdate(srcPos, Blocks.AIR.defaultBlockState());
        destWorld.removeBlockEntity(dstPos);
        destWorld.setBlockAndUpdate(dstPos, Blocks.AIR.defaultBlockState());

        BlockState newDstState = oldSrcState;
        destWorld.setBlock(dstPos, newDstState, Block.UPDATE_ALL);
//        destWorld.setBlockMetadataWithNotify(destX, destY, destZ, srcMeta, 3);
        if (srcTC != null) {
            configureTileEntityNBT(destWorld, srcTC, dstPos, newDstState);
        }

        BlockState newSrcState = oldDstState;
        srcWorld.setBlock(srcPos, newSrcState, Block.UPDATE_ALL);
//        world.setBlockMetadataWithNotify(x, y, z, dstMeta, 3);
        if (dstTC != null) {
            configureTileEntityNBT(srcWorld, dstTC, srcPos, newSrcState);
        }

        if (!isSilent()) {
            if (!isEmpty(oldSrcState, srcBlock)) {
                SoundType srcSoundType = srcBlock.getSoundType(oldSrcState, srcWorld, srcPos, null);
                playBreakSoundSafe(srcSoundType, srcWorld, oldSrcState, srcPos.getX(), srcPos.getY(), srcPos.getZ());
                SoundType dstSoundType = srcBlock.getSoundType(oldSrcState, destWorld, dstPos, null);
                playPlaceSoundSafe(dstSoundType, destWorld, oldSrcState, dstPos.getX(), dstPos.getY(), dstPos.getZ());
            }
            if (!isEmpty(oldDstState, dstBlock)) {
                SoundType srcSoundType = dstBlock.getSoundType(oldDstState, destWorld, dstPos, null);
                playBreakSoundSafe(srcSoundType, destWorld, oldDstState, dstPos.getX(), dstPos.getY(), dstPos.getZ());
                SoundType dstSoundType = dstBlock.getSoundType(oldDstState, srcWorld, srcPos, null);
                playPlaceSoundSafe(dstSoundType, srcWorld, oldDstState, srcPos.getX(), srcPos.getY(), srcPos.getZ());
            }
        }
    }

    private BlockPos sourceToDest(BlockPos source) {
        return rotate(source).offset(projDx, projDy, projDz);
    }

    private BlockPos rotate(BlockPos c) {
        switch (getRotate()) {
            case ROTATE_0:
                return c;
            case ROTATE_90:
                return new BlockPos(-c.getZ(), c.getY(), c.getX());
            case ROTATE_180:
                return new BlockPos(-c.getX(), c.getY(), -c.getZ());
            case ROTATE_270:
                return new BlockPos(c.getZ(), c.getY(), -c.getX());
        }
        return c;
    }

    private void sourceToDest(BlockPos source, BlockPos.MutableBlockPos dest) {
        rotate(source, dest);
        dest.set(dest.getX() + projDx, dest.getY() + projDy, dest.getZ() + projDz);
    }


    private void rotate(BlockPos c, BlockPos.MutableBlockPos dest) {
        switch (getRotate()) {
            case ROTATE_0 -> dest.set(c);
            case ROTATE_90 -> dest.set(-c.getZ(), c.getY(), c.getX());
            case ROTATE_180 -> dest.set(-c.getX(), c.getY(), -c.getZ());
            case ROTATE_270 -> dest.set(c.getZ(), c.getY(), -c.getX());
        }
    }

    private BuilderData restartScan(BuilderData data) {
        data = data.withLastError(null);
        chunkUnload();
        if (data.flags().loopMode() || (isMachineEnabled() && data.scan() == null)) {
            if (getCardType() == ShapeCardType.CARD_SPACE) {
                data = calculateBox(data).getLeft();
                data = data.withScan(data.minBox());
            } else if (getCardType() != ShapeCardType.CARD_UNKNOWN) {
                data = calculateBoxShaped(data);
                // We start at the top for a quarry or shape building
                data = data.withScan(new BlockPos(data.minBox().getX(), data.maxBox().getY(), data.minBox().getZ()));
            }
            cachedBlocks = null;
            cachedChunk = null;
            cachedVoidableBlocks.clear();
        } else {
            data = data.withScan(null);
        }
        return data;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        chunkUnload();
    }

    private void chunkUnload() {
        if (forcedChunk != null) {
            if (getOwnerUUID() != null) {
                RFToolsBuilder.setup.ticketController.forceChunk((ServerLevel) level, getOwnerUUID(), forcedChunk.x, forcedChunk.z, false, false);
            }
            forcedChunk = null;
        }
    }

    private boolean chunkLoad(int x, int z) {
        int cx = x >> 4;
        int cz = z >> 4;

        if (LevelTools.isLoaded(level, new BlockPos(x, 0, z))) {
            return true;
        }

        if (BuilderConfiguration.quarryChunkloads.get()) {
            ChunkPos pair = new ChunkPos(cx, cz);
            if (pair.equals(forcedChunk)) {
                return true;
            }
            if (forcedChunk != null) {
                if (getOwnerUUID() != null) {
                    RFToolsBuilder.setup.ticketController.forceChunk((ServerLevel) level, getOwnerUUID(), forcedChunk.x, forcedChunk.z, false, false);
                }
            }
            forcedChunk = pair;
            if (getOwnerUUID() != null) {
                RFToolsBuilder.setup.ticketController.forceChunk((ServerLevel) level, getOwnerUUID(), forcedChunk.x, forcedChunk.z, false, false);
            }
            return true;
        }
        // Chunk is not loaded and we don't do chunk loading so we cannot proceed.
        return false;
    }


    public static void setScanLocationClient(BlockPos tePos, BlockPos scanPos) {
        scanLocClient.put(tePos, Pair.of(System.currentTimeMillis(), scanPos));
    }

    public static Map<BlockPos, Pair<Long, BlockPos>> getScanLocClient() {
        if (scanLocClient.isEmpty()) {
            return scanLocClient;
        }
        Map<BlockPos, Pair<Long, BlockPos>> scans = new HashMap<>();
        long time = System.currentTimeMillis();
        for (Map.Entry<BlockPos, Pair<Long, BlockPos>> entry : scanLocClient.entrySet()) {
            if (entry.getValue().getKey() + 10000 > time) {
                scans.put(entry.getKey(), entry.getValue());
            }
        }
        scanLocClient = scans;
        return scanLocClient;
    }

    private BuilderData nextLocation(BuilderData data) {
        if (data.scan() != null) {
            int x = data.scan().getX();
            int y = data.scan().getY();
            int z = data.scan().getZ();

            if (getCardType() == ShapeCardType.CARD_SPACE) {
                data = nextLocationNormal(data, x, y, z);
            } else {
                data = nextLocationQuarry(data, x, y, z);
            }
        }
        return data;
    }

    private BuilderData nextLocationQuarry(BuilderData data, int x, int y, int z) {
        BlockPos minBox = data.minBox();
        BlockPos maxBox = data.maxBox();
        if (x >= maxBox.getX() || ((x + 1) % 16 == 0)) {
            if (z >= maxBox.getZ() || ((z + 1) % 16 == 0)) {
                if (y <= minBox.getY()) {
                    if (x < maxBox.getX()) {
                        x++;
                        z = (z >> 4) << 4;
                        y = maxBox.getY();
                        data = data.withScan(new BlockPos(x, y, z));
                    } else if (z < maxBox.getZ()) {
                        x = minBox.getX();
                        z++;
                        y = maxBox.getY();
                        data = data.withScan(new BlockPos(x, y, z));
                    } else {
                        data = restartScan(data);
                        return data;
                    }
                } else {
                    data = data.withScan(new BlockPos((x >> 4) << 4, y - 1, (z >> 4) << 4));
                }
            } else {
                data = data.withScan(new BlockPos((x >> 4) << 4, y, z + 1));
            }
        } else {
            data = data.withScan(new BlockPos(x + 1, y, z));
        }
        return data;
    }

    private BuilderData nextLocationNormal(BuilderData data, int x, int y, int z) {
        BlockPos minBox = data.minBox();
        BlockPos maxBox = data.maxBox();
        if (x >= maxBox.getX()) {
            if (z >= maxBox.getZ()) {
                if (y >= maxBox.getY()) {
                    if (getMode() != MODE_SWAP || isShapeCard()) {
                        data = restartScan(data);
                    } else {
                        // We don't restart in swap mode.
                        data = data.withScan(null);
                    }
                } else {
                    data = data.withScan(new BlockPos(minBox.getX(), y + 1, minBox.getZ()));
                }
            } else {
                data = data.withScan(new BlockPos(minBox.getX(), y, z + 1));
            }
        } else {
            data = data.withScan(new BlockPos(x + 1, y, z));
        }
        return data;
    }

    private void refreshSettings() {
        BuilderData data = getData(BuilderModule.BUILDER_DATA);
        data = clearSupportBlocks(data);
        cachedBlocks = null;
        cachedChunk = null;
        cachedVoidableBlocks.clear();
        boxValid = false;
        setData(BuilderModule.BUILDER_DATA, data.withScan(null));
        cardType = ShapeCardType.CARD_UNKNOWN;
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        energyStorage.load(tag, "energy", provider);
        try {
            items.load(tag, "items", provider);
        } catch (Exception e) {
            CompoundTag itemsTag = tag.getCompound("items");
            ListTag tagsTag = itemsTag.getList("Items", Tag.TAG_COMPOUND);
            CompoundTag compound0 = tagsTag.getCompound(0);
            CompoundTag components = compound0.getCompound("components");
            CompoundTag tags = components.getCompound("rftoolsbuilder:shapecard_data");
            DataResult<com.mojang.datafixers.util.Pair<ShapeCardData, Tag>> data = ShapeCardData.CODEC.decode(NbtOps.INSTANCE, tags);
            throw new RuntimeException(e);
        }
        infusable.load(tag, "infusable");

        if (tag.contains("overflowItems")) {
            ListTag overflowItemsNbt = tag.getList("overflowItems", Tag.TAG_COMPOUND);
            overflowItems.clear();
            for (Tag overflowNbt : overflowItemsNbt) {
                overflowItems.add(ItemStack.parseOptional(provider, (CompoundTag) overflowNbt)); // @todo 1.21 check, is this the same as ItemStack.of()?
            }
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        energyStorage.save(tag, "energy", provider);
        items.save(tag, "items", provider);
        infusable.save(tag, "infusable");

        if (!overflowItems.isEmpty()) {
            ListTag overflowItemsNbt = new ListTag();
            for (ItemStack overflow : overflowItems.getList()) {
                overflowItemsNbt.add(overflow.save(provider, new CompoundTag()));
            }
            tag.put("overflowItems", overflowItemsNbt);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        energyStorage.applyImplicitComponents(input.get(Registration.ITEM_ENERGY));
        items.applyImplicitComponents(input.get(Registration.ITEM_INVENTORY));
        infusable.applyImplicitComponents(input.get(Registration.ITEM_INFUSABLE));
        BuilderData builderData = input.get(BuilderModule.ITEM_BUILDER_DATA);
        if (builderData != null) {
            setData(BuilderModule.BUILDER_DATA, builderData);
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        energyStorage.collectImplicitComponents(builder);
        items.collectImplicitComponents(builder);
        infusable.collectImplicitComponents(builder);
        builder.set(BuilderModule.ITEM_BUILDER_DATA, getData(BuilderModule.BUILDER_DATA));
    }

    public static int getCurrentLevelClientSide() {
        return currentLevel;
    }

    public int getCurrentLevel() {
        BuilderData data = getData(BuilderModule.BUILDER_DATA);
        return data.scan() == null ? -1 : data.scan().getY();
    }

    @ServerCommand
    public static final Command<?> CMD_RESTART = Command.<BuilderTileEntity>create("restart", (te, player, params) -> {
        BuilderData data = te.getData(BuilderModule.BUILDER_DATA);
        data = te.restartScan(data);
        te.setData(BuilderModule.BUILDER_DATA, data);
    });

    @ServerCommand
    public static final ListCommand<?, ?> CMD_GETHUDLOG = ListCommand.<BuilderTileEntity, String>create(COMMAND_GETHUDLOG,
            (te, player, params) -> te.getHudLog(),
            (te, player, params, list) -> te.clientHudLog = list);

    @Override
    public void onReplaced(Level world, BlockPos pos, BlockState state, BlockState newstate) {
        if (state.getBlock() == newstate.getBlock()) {
            return;
        }

        if (hasSupportMode()) {
            BuilderData data = getData(BuilderModule.BUILDER_DATA);
            data = clearSupportBlocks(data);
            setData(BuilderModule.BUILDER_DATA, data);
        }
    }

    @Override
    public void rotateBlock(Rotation axis) {
        super.rotateBlock(axis);
        if (!level.isClientSide) {
            if (hasSupportMode()) {
                BuilderData data = getData(BuilderModule.BUILDER_DATA);
                data = clearSupportBlocks(data);
                setData(BuilderModule.BUILDER_DATA, data);
                resetBox();
            }
        }
    }

    // @todo 1.14
//    @Override
//    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, BlockState metadata, int fortune) {
//        super.getDrops(drops, world, pos, metadata, fortune);
//        List<ItemStack> overflowItems = getOverflowItems();
//        if(overflowItems != null) {
//            drops.addAll(overflowItems);
//        }
//    }

    private GenericItemHandler createItemHandler() {
        return new GenericItemHandler(BuilderTileEntity.this, CONTAINER_FACTORY.get()) {

            // @todo all methods below could be avoided with a proper onUpdate method
            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                checkShapeCard(slot, stack);
                return super.insertItem(slot, stack, simulate);
            }

            @Override
            public void setInventorySlotContents(int stackLimit, int index, ItemStack stack) {
                checkShapeCard(index, stack);
                super.setInventorySlotContents(stackLimit, index, stack);
            }

            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                checkShapeCard(slot, ItemStack.EMPTY);
                return super.extractItem(slot, amount, simulate);
            }

            @Override
            public ItemStack decrStackSize(int index, int amount) {
                checkShapeCard(index, ItemStack.EMPTY);
                return super.decrStackSize(index, amount);
            }

            @Override
            public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
                checkShapeCard(slot, stack);
                super.setStackInSlot(slot, stack);
            }

            // @todo would be better if onUpdate had an ItemStack parameter
            @Override
            protected void onUpdate(int index, ItemStack stack) {
                super.onUpdate(index, stack);
                if (index == SLOT_FILTER) {
                    filterCache.clear();
                }
            }

            private void checkShapeCard(int index, ItemStack newStack) {
                ItemStack stack = getStackInSlot(index);
                if (index == SLOT_TAB && ((stack.isEmpty()
                        && !newStack.isEmpty())
                        || (!stack.isEmpty() && newStack.isEmpty()))) {
                    // Restart if we go from having a stack to not having stack or the other way around.
                    refreshSettings();
                }
            }
        };
    }
}
