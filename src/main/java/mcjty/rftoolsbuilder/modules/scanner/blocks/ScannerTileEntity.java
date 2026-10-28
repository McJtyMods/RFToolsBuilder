package mcjty.rftoolsbuilder.modules.scanner.blocks;

import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import java.util.function.Predicate;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Cached;
import mcjty.lib.varia.RLE;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftoolsbase.modules.filter.items.FilterModuleItem;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.data.ShapeCardData;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.scanner.ScannerConfiguration;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.shapes.ScanDataManager;
import mcjty.rftoolsbuilder.shapes.Shape;
import mcjty.rftoolsbuilder.shapes.StatePalette;
import mcjty.rftoolsbuilder.tools.BuilderSerialization;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

import static mcjty.lib.api.container.DefaultContainerProvider.container;
import static mcjty.lib.container.SlotDefinition.specific;
import static mcjty.lib.setup.Registration.ITEM_INVENTORY;

public class ScannerTileEntity extends TickingTileEntity {

    public static final String CMD_SCAN_ID = "scanner.scan";
    public static final String CMD_OFFSET_ID = "scanner.offset";
    public static final Key<BlockPos> PARAM_OFFSET = new Key<>("offset", Type.BLOCKPOS);

    public static final int SLOT_IN = 0;
    public static final int SLOT_OUT = 1;
    public static final int SLOT_FILTER = 2;
    public static final int SLOT_MODIFIER = 3;

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(4)
            .slot(specific(s -> s.getItem() instanceof ShapeCardItem).in().out(), SLOT_IN, 15, 7)
            .slot(specific(s -> s.getItem() instanceof ShapeCardItem).in().out(), SLOT_OUT, 15, 200)
            .slot(specific(s -> s.getItem() instanceof FilterModuleItem).in().out(), SLOT_FILTER, 35, 7)
            .slot(specific(s -> true).in().out(), SLOT_MODIFIER, 55, 7)
            .playerSlots(85, 142));

    private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY)
            .itemValid((slot, stack) -> switch (slot) {
                case SLOT_IN, SLOT_OUT -> stack.getItem() instanceof ShapeCardItem;
                case SLOT_FILTER -> stack.getItem() instanceof FilterModuleItem;
                case SLOT_MODIFIER -> true;
                default -> false;
            })
            .onUpdate(this::handleSlotUpdate)
            .build();

    @Cap(type = CapType.ITEMS_AUTOMATION)
    private static final Function<ScannerTileEntity, GenericItemHandler> ITEM_CAP = tile -> tile.items;

    @Cap(type = CapType.CONTAINER)
    private static final Function<ScannerTileEntity, MenuProvider> SCREEN_CAP = tile -> new DefaultContainerProvider<GenericContainer>("Scanner")
            .containerSupplier(container(ScannerModule.CONTAINER_SCANNER, CONTAINER_FACTORY, tile))
            .itemHandler(() -> tile.items)
            .setupSync(tile);

    private final Cached<Predicate<ItemStack>> filterCache = Cached.of(this::createFilterCache);

    private int scanId = 0;
    private ItemStack renderStack = ItemStack.EMPTY;
    private BlockPos dataDim = new BlockPos(5, 5, 5);
    private BlockPos dataOffset = BlockPos.ZERO;

    private ScanProgress progress = null;
    private int progressBusy = -1;

    public ScannerTileEntity(BlockPos pos, BlockState state) {
        this(ScannerModule.TYPE_SCANNER.get(), pos, state);
    }

    public ScannerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setRSMode(RedstoneMode.REDSTONE_ONREQUIRED);
    }

    @Override
    protected void tickServer() {
        // Initialize outside packet serialization: allocating a scan ID queues a block update.
        if (renderStack.isEmpty() && items.getStackInSlot(SLOT_OUT).isEmpty()) {
            renderStack = new ItemStack(BuilderModule.SHAPE_CARD_DEF.get());
            updateScanCard(renderStack);
            markDirtyClient();
        }
        if (progress != null) {
            int done = 0;
            while (progress != null && done < ScannerConfiguration.surfaceAreaPerTick.get()) {
                progressScan();
                if (progress != null) {
                    done += progress.dimY * progress.dimZ;
                }
            }
            int percent = progress == null ? -1 : (progress.x - progress.tl.getX()) * 100 / Math.max(1, progress.dimX);
            if (percent != progressBusy) {
                progressBusy = percent;
                markDirtyClient();
            }
        } else if (isMachineEnabled()) {
            scan();
        }
    }

    public ItemStack getRenderStack() {
        ItemStack stack = items.getStackInSlot(SLOT_OUT);
        // Packet serialization calls this getter; it must not allocate IDs or schedule updates.
        return stack.isEmpty() ? renderStack : stack;
    }

    public BlockPos getDataDim() {
        return dataDim;
    }

    public BlockPos getDataOffset() {
        return dataOffset;
    }

    public int getScanProgress() {
        return progressBusy;
    }

    public BlockPos getScanCenter() {
        return getBlockPos().offset(dataOffset);
    }

    public int getScanId() {
        if (scanId == 0 && level != null && !level.isClientSide()) {
            scanId = ScanDataManager.get(level).newScan(level);
            setChanged();
            markDirtyClient();
        }
        return scanId;
    }

    private void handleSlotUpdate(int slot, ItemStack stack) {
        if (slot == SLOT_FILTER) {
            filterCache.clear();
        }
        if (slot == SLOT_IN) {
            if (!stack.isEmpty()) {
                dataDim = ShapeCardItem.getDimension(stack);
            }
            if (renderStack.isEmpty()) {
                renderStack = new ItemStack(BuilderModule.SHAPE_CARD_DEF.get());
            }
            updateScanCard(renderStack);
        } else if (slot == SLOT_OUT && !stack.isEmpty()) {
            updateOutputCard();
        }
        setChanged();
        markDirtyClient();
    }

    private void updateOutputCard() {
        if (level == null || level.isClientSide()) {
            return;
        }
        ItemStack card = items.getStackInSlot(SLOT_OUT);
        if (!card.isEmpty()) {
            ItemStack updated = card.copy();
            updateScanCard(updated);
            // Inventory reads may return copies. Only write changes to avoid recursive slot updates.
            if (!ItemStack.matches(card, updated)) {
                items.setStackInSlot(SLOT_OUT, updated);
            }
        }
    }

    private void updateScanCard(ItemStack card) {
        if (card.isEmpty()) {
            return;
        }
        if (!ShapeCardItem.getShape(card).isScan()) {
            ShapeCardItem.setShape(card, Shape.SHAPE_SCAN, ShapeCardItem.isSolid(card));
        }
        ShapeCardItem.setDimension(card, dataDim.getX(), dataDim.getY(), dataDim.getZ());
        ShapeCardItem.setOffset(card, dataOffset.getX(), dataOffset.getY(), dataOffset.getZ());
        ShapeCardItem.setData(card, getScanId());
    }

    private void scan() {
        if (progress != null || items.getStackInSlot(SLOT_IN).isEmpty()) {
            return;
        }
        ItemStack card = items.getStackInSlot(SLOT_IN);
        dataDim = ShapeCardItem.getDimension(card);
        startScanArea(getScanCenter(), level.dimension(), dataDim.getX(), dataDim.getY(), dataDim.getZ());
    }

    private void startScanArea(BlockPos center, ResourceKey<Level> dimension, int dimX, int dimY, int dimZ) {
        progress = new ScanProgress();
        progress.rle = new RLE();
        progress.tl = new BlockPos(center.getX() - dimX / 2, center.getY() - dimY / 2, center.getZ() - dimZ / 2);
        progress.materialPalette = new StatePalette();
        progress.materialPalette.alloc(BuilderModule.SUPPORT.get().defaultBlockState(), 0);
        progress.x = progress.tl.getX();
        progress.dimX = dimX;
        progress.dimY = dimY;
        progress.dimZ = dimZ;
        progress.dimension = dimension;
        progressBusy = 0;
        setChanged();
        markDirtyClient();
    }

    private void progressScan() {
        if (progress == null) {
            return;
        }
        Level scanWorld = level.getServer().getLevel(progress.dimension);
        if (scanWorld == null) {
            stopScanArea();
            return;
        }

        Predicate<ItemStack> filter = filterCache.get();
        BlockPos.MutableBlockPos mpos = progress.mpos;
        BlockPos tl = progress.tl;
        for (int z = tl.getZ(); z < tl.getZ() + progress.dimZ; z++) {
            for (int y = tl.getY(); y < tl.getY() + progress.dimY; y++) {
                mpos.set(progress.x, y, z);
                int c = 0;
                if (!scanWorld.isEmptyBlock(mpos)) {
                    BlockState state = scanWorld.getBlockState(mpos);
                    if (filter == null || filter.test(state.getBlock().getCloneItemStack(scanWorld, mpos, state, false, null))) {
                        c = progress.materialPalette.alloc(state, 0) + 1;
                    }
                }
                progress.rle.add(c);
            }
        }
        progress.x++;
        if (progress.x >= tl.getX() + progress.dimX) {
            stopScanArea();
        }
    }

    private void stopScanArea() {
        if (progress == null) {
            return;
        }
        dataDim = new BlockPos(progress.dimX, progress.dimY, progress.dimZ);
        ScanDataManager manager = ScanDataManager.get(level);
        manager.getOrCreateScan(getScanId()).setData(progress.rle.getData(), progress.materialPalette.getPalette(), dataDim, dataOffset);
        manager.save(level, getScanId());
        if (renderStack.isEmpty()) {
            renderStack = new ItemStack(BuilderModule.SHAPE_CARD_DEF.get());
        }
        updateScanCard(renderStack);
        updateOutputCard();
        progress = null;
        progressBusy = -1;
        setChanged();
        markDirtyClient();
    }

    private Predicate<ItemStack> createFilterCache() {
        return FilterModuleItem.getCache(items.getStackInSlot(SLOT_FILTER));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        CompoundTag tag = input.read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).orElseGet(CompoundTag::new);
        HolderLookup.Provider provider = input.lookup();

        items.load(tag, "items", provider);
        scanId = tag.getIntOr("scanid", 0);
        if (tag.contains("scandimx")) {
            dataDim = new BlockPos(tag.getIntOr("scandimx", 0), tag.getIntOr("scandimy", 0), tag.getIntOr("scandimz", 0));
        }
        dataOffset = new BlockPos(tag.getIntOr("scanoffx", 0), tag.getIntOr("scanoffy", 0), tag.getIntOr("scanoffz", 0));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        CompoundTag tag = new CompoundTag();
        HolderLookup.Provider provider = level != null ? level.registryAccess() : RegistryAccess.EMPTY;

        items.save(tag, "items", provider);
        tag.putInt("scanid", scanId);
        tag.putInt("scandimx", dataDim.getX());
        tag.putInt("scandimy", dataDim.getY());
        tag.putInt("scandimz", dataDim.getZ());
        tag.putInt("scanoffx", dataOffset.getX());
        tag.putInt("scanoffy", dataOffset.getY());
        tag.putInt("scanoffz", dataOffset.getZ());
        output.store(MapCodec.assumeMapUnsafe(CompoundTag.CODEC), tag);
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tag, HolderLookup.Provider provider) {
        ItemStack render = getRenderStack();
        if (!render.isEmpty()) {
            tag.put("render", BuilderSerialization.saveItem(render, provider));
        }
        tag.putInt("scanid", scanId);
        tag.putInt("scandimx", dataDim.getX());
        tag.putInt("scandimy", dataDim.getY());
        tag.putInt("scandimz", dataDim.getZ());
        tag.putInt("scanoffx", dataOffset.getX());
        tag.putInt("scanoffy", dataOffset.getY());
        tag.putInt("scanoffz", dataOffset.getZ());
        tag.putInt("progress", progressBusy);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter input) {
        super.applyImplicitComponents(input);
        items.applyImplicitComponents(input.get(ITEM_INVENTORY));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        items.collectImplicitComponents(builder);
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        renderStack = BuilderSerialization.loadItem(provider, tag.getCompoundOrEmpty("render"));
        scanId = tag.getIntOr("scanid", 0);
        dataDim = new BlockPos(tag.getIntOr("scandimx", 0), tag.getIntOr("scandimy", 0), tag.getIntOr("scandimz", 0));
        dataOffset = new BlockPos(tag.getIntOr("scanoffx", 0), tag.getIntOr("scanoffy", 0), tag.getIntOr("scanoffz", 0));
        progressBusy = tag.getIntOr("progress", 0);
    }

    public GenericItemHandler getItems() {
        return items;
    }

    @ServerCommand
    public static final Command<?> CMD_SCAN = Command.<ScannerTileEntity>create(CMD_SCAN_ID, (te, player, params) -> te.scan());

    @ServerCommand
    public static final Command<?> CMD_OFFSET = Command.<ScannerTileEntity>create(CMD_OFFSET_ID, (te, player, params) -> te.applyOffset(params));

    private void applyOffset(TypedMap params) {
        dataOffset = params.get(PARAM_OFFSET);
        if (!renderStack.isEmpty()) {
            updateScanCard(renderStack);
        }
        updateOutputCard();
        setChanged();
        markDirtyClient();
    }

    private static class ScanProgress {
        private RLE rle;
        private BlockPos tl;
        private StatePalette materialPalette;
        private final BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        private int dimX;
        private int dimY;
        private int dimZ;
        private int x;
        private ResourceKey<Level> dimension;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            var handler = getItems();
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), handler.getStackInSlot(slot));
            }
        }
        super.preRemoveSideEffects(pos, state);
    }
}
