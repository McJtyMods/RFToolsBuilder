package mcjty.rftoolsbuilder.modules.scanner.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.varia.Cached;
import mcjty.lib.varia.RLE;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftoolsbase.modules.filter.items.FilterModuleItem;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.shapes.StatePalette;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Lazy;

import java.util.function.Predicate;

import static mcjty.lib.api.container.DefaultContainerProvider.container;
import static mcjty.lib.builder.TooltipBuilder.*;
import static mcjty.lib.container.SlotDefinition.specific;

public class ScannerTileEntity extends TickingTileEntity {

    public static final int SLOT_IN = 0;
    public static final int SLOT_OUT = 1;
    public static final int SLOT_FILTER = 2;
    public static final int SLOT_MODIFIER = 3;

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(4)
            .slot(specific(s -> (s.getItem() instanceof ShapeCardItem)).in().out(), SLOT_IN, 15, 7)
            .slot(specific(s -> (s.getItem() instanceof ShapeCardItem)).in().out(), SLOT_OUT, 15, 200)
            .slot(specific(s -> s.getItem() instanceof FilterModuleItem).in().out(), SLOT_FILTER, 35, 7)
            .slot(specific(s -> true /*@todo*/).in().out(), SLOT_MODIFIER, 55, 7)
            .playerSlots(85, 142));

    @Cap(type = CapType.ITEMS_AUTOMATION)
    private final GenericItemHandler items = createItemHandler();

    @Cap(type = CapType.CONTAINER)
    private final Lazy<MenuProvider> screenHandler = Lazy.of(() -> new DefaultContainerProvider<GenericContainer>("Builder")
            .containerSupplier(container(BuilderModule.CONTAINER_BUILDER, CONTAINER_FACTORY,this))
            .itemHandler(() -> items)
//            .energyHandler(() -> energyStorage)
//            .shortListener(Sync.integer(() -> scan == null ? -1 : scan.getY(), v -> currentLevel = v))
            .setupSync(this));

    private int scanId = 0;
    private ItemStack renderStack = ItemStack.EMPTY;
    private BlockPos dataDim;
    private BlockPos dataOffset = new BlockPos(0, 0, 0);

    // Transient data that is used during the scan.
    private ScanProgress progress = null;
    // Client side indication if there is a scan in progress
    private int progressBusy = -1;

    private final Cached<Predicate<ItemStack>> filterCache = Cached.of(this::createFilterCache);

    public ScannerTileEntity(BlockEntityType type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setRSMode(RedstoneMode.REDSTONE_ONREQUIRED);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
//                .tileEntitySupplier(ScannerTileEntity::new)
                .topDriver(RFToolsBuilderTOPDriver.DRIVER)
                .infusable()
                .manualEntry(ManualHelper.create("rftoolsbuilder:projector/scanner"))
                .info(key("message.rftoolsbuilder.shiftmessage"))
                .infoShift(header(), gold())) {
            @Override
            public RotationType getRotationType() {
                return RotationType.HORIZROTATION;
            }
        };
    }

    @Override
    protected void tickServer() {
        int surfaceAreaPerTick = 512*256*2; // @todo ScannerConfiguration.surfaceAreaPerTick
        if (progress != null) {
//            if (getStoredPower() >= getEnergyPerTick()) { @todo
//                consumeEnergy(getEnergyPerTick());
            int done = 0;
            while (progress != null && done < surfaceAreaPerTick) {
                progressScan();
                done += dataDim.getZ() * dataDim.getY();  // We scan planes on the x axis
            }
//            }
        } else if (isMachineEnabled()) {
            scan();
        }
    }

    private void scan() {
        if (progress != null) {
            return;
        }
        if (items.getStackInSlot(SLOT_IN).isEmpty()) {
            // Cannot scan. No input card
            return;
        }

        BlockPos machinePos = getScanPos();
        if (machinePos == null) {
            // No valid destination. We cannot scan
            return;
        }

        int dimX = dataDim.getX();
        int dimY = dataDim.getY();
        int dimZ = dataDim.getZ();
        startScanArea(getScanCenter(), getScanDimension(), dimX, dimY, dimZ);
    }

    public Level getScanWorld(ResourceKey<Level> dimension) {
        return level.getServer().getLevel(dimension);
    }

    protected BlockPos getScanPos() {
        return getBlockPos();
    }

    public BlockPos getScanCenter() {
        if (getScanPos() == null) {
            return null;
        }
        return getScanPos().offset(dataOffset.getX(), dataOffset.getY(), dataOffset.getZ());
    }

    public BlockPos getFirstCorner() {
        if (getScanPos() == null) {
            return null;
        }
        return getScanPos().offset(dataOffset.getX()-dataDim.getX()/2,
                dataOffset.getY()-dataDim.getY()/2,
                dataOffset.getZ()-dataDim.getZ()/2);
    }

    public BlockPos getLastCorner() {
        if (getScanPos() == null) {
            return null;
        }
        return getScanPos().offset(dataOffset.getX()+dataDim.getX()/2,
                dataOffset.getY()+dataDim.getY()/2,
                dataOffset.getZ()+dataDim.getZ()/2);
    }

    public ResourceKey<Level> getScanDimension() {
        return level.dimension();
    }

    private void progressScan() {
        if (progress == null) {
            return;
        }
        BlockPos tl = progress.tl;
        int dimX = progress.dimX;
        int dimY = progress.dimY;
        int dimZ = progress.dimZ;
        Level world = getScanWorld(progress.dimension);
        BlockPos.MutableBlockPos mpos = progress.mpos;
        for (int z = tl.getZ() ; z < tl.getZ() + dimZ ; z++) {
            for (int y = tl.getY() ; y < tl.getY() + dimY ; y++) {
                mpos.set(progress.x, y, z);
                int c;
                // @todo
//                if (world.isAirBlock(mpos)) {
//                    c = 0;
//                } else {
//                    IBlockState state = world.getBlockState(mpos);
//                    getFilterCache();
//                    if (filterCache != null) {
//                        ItemStack item = state.getBlock().getItem(world, mpos, state);
//                        if (!filterCache.match(item)) {
//                            state = null;
//                        }
//                    }
//                    if (state != null && state != Blocks.AIR.getDefaultState()) {
//                        state = mapState(progress.modifiers, progress.modifierMapping, mpos, state);
//                    }
//                    if (state != null && state != Blocks.AIR.getDefaultState()) {
//                        c = progress.materialPalette.alloc(state, 0) + 1;
//                    } else {
//                        c = 0;
//                    }
//                }
//                progress.rle.add(c);
            }
        }
        progress.x++;
        if (progress.x >= tl.getX() + dimX) {
            stopScanArea();
        } else {
            markDirtyClient();
        }
    }

    private void startScanArea(BlockPos center, ResourceKey<Level> dimension, int dimX, int dimY, int dimZ) {
        progress = new ScanProgress();
        // @todo
//        progress.modifiers = ModifierItem.getModifiers(getStackInSlot(SLOT_MODIFIER));
//        progress.modifierMapping = new HashMap<>();
        progress.rle = new RLE();
        progress.tl = new BlockPos(center.getX() - dimX/2, center.getY() - dimY/2, center.getZ() - dimZ/2);
        progress.materialPalette = new StatePalette();
        progress.materialPalette.alloc(BuilderModule.SUPPORT.get().defaultBlockState(), 0);
        progress.x = progress.tl.getX();
        progress.dimX = dimX;
        progress.dimY = dimY;
        progress.dimZ = dimZ;
        progress.dimension = dimension;
        markDirtyClient();
    }

    private void stopScanArea() {
        this.dataDim = new BlockPos(progress.dimX, progress.dimY, progress.dimZ);
        // @todo
//        ScanDataManager scan = ScanDataManager.getScans();
//        scan.getOrCreateScan(getScanId()).setData(progress.rle.getData(), progress.materialPalette.getPalette(), dataDim, dataOffset);
//        scan.save(getScanId());
//        if (renderStack.isEmpty()) {
//            renderStack = new ItemStack(BuilderSetup.shapeCardItem);
//        }
//        updateScanCard(renderStack);
//        markDirtyClient();
        progress = null;
    }

    private Predicate<ItemStack> createFilterCache() {
        return FilterModuleItem.getCache(items.getStackInSlot(SLOT_FILTER));
    }

    private GenericItemHandler createItemHandler() {
        return new GenericItemHandler(ScannerTileEntity.this, CONTAINER_FACTORY.get());
    }

    private static class ScanProgress {
//        List<ModifierEntry> modifiers;    @todo
//        Map<BlockState, BlockState> modifierMapping;
        RLE rle;
        BlockPos tl;
        StatePalette materialPalette;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        int dimX;
        int dimY;
        int dimZ;
        int x;
        ResourceKey<Level> dimension;
    }
}
