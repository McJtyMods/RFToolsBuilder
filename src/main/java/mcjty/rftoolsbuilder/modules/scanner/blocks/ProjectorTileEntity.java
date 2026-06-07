package mcjty.rftoolsbuilder.modules.scanner.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.bindings.GuiValue;
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
import mcjty.lib.varia.OrientationTools;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.scanner.ProjectorOpcode;
import mcjty.rftoolsbuilder.modules.scanner.ProjectorOperation;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.shapes.RenderData;
import mcjty.rftoolsbuilder.shapes.ShapeID;
import mcjty.rftoolsbuilder.shapes.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static mcjty.lib.api.container.DefaultContainerProvider.container;
import static mcjty.lib.container.SlotDefinition.specific;

public class ProjectorTileEntity extends TickingTileEntity {

    public static final String CMD_RSSETTINGS_ID = "projector.rsSettings";
    public static final String CMD_SETTINGS_ID = "projector.settings";

    public static final Key<String> PARAM_OPON_N = new Key<>("opOn_n", Type.STRING);
    public static final Key<String> PARAM_OPON_S = new Key<>("opOn_s", Type.STRING);
    public static final Key<String> PARAM_OPON_W = new Key<>("opOn_w", Type.STRING);
    public static final Key<String> PARAM_OPON_E = new Key<>("opOn_e", Type.STRING);
    public static final List<Key<String>> PARAM_OPON = Arrays.asList(PARAM_OPON_N, PARAM_OPON_S, PARAM_OPON_W, PARAM_OPON_E);

    public static final Key<String> PARAM_OPOFF_N = new Key<>("opOff_n", Type.STRING);
    public static final Key<String> PARAM_OPOFF_S = new Key<>("opOff_s", Type.STRING);
    public static final Key<String> PARAM_OPOFF_W = new Key<>("opOff_w", Type.STRING);
    public static final Key<String> PARAM_OPOFF_E = new Key<>("opOff_e", Type.STRING);
    public static final List<Key<String>> PARAM_OPOFF = Arrays.asList(PARAM_OPOFF_N, PARAM_OPOFF_S, PARAM_OPOFF_W, PARAM_OPOFF_E);

    public static final Key<Double> PARAM_VALON_N = new Key<>("valOn_n", Type.DOUBLE);
    public static final Key<Double> PARAM_VALON_S = new Key<>("valOn_s", Type.DOUBLE);
    public static final Key<Double> PARAM_VALON_W = new Key<>("valOn_w", Type.DOUBLE);
    public static final Key<Double> PARAM_VALON_E = new Key<>("valOn_e", Type.DOUBLE);
    public static final List<Key<Double>> PARAM_VALON = Arrays.asList(PARAM_VALON_N, PARAM_VALON_S, PARAM_VALON_W, PARAM_VALON_E);

    public static final Key<Double> PARAM_VALOFF_N = new Key<>("valOff_n", Type.DOUBLE);
    public static final Key<Double> PARAM_VALOFF_S = new Key<>("valOff_s", Type.DOUBLE);
    public static final Key<Double> PARAM_VALOFF_W = new Key<>("valOff_w", Type.DOUBLE);
    public static final Key<Double> PARAM_VALOFF_E = new Key<>("valOff_e", Type.DOUBLE);
    public static final List<Key<Double>> PARAM_VALOFF = Arrays.asList(PARAM_VALOFF_N, PARAM_VALOFF_S, PARAM_VALOFF_W, PARAM_VALOFF_E);

    public static final Key<Integer> PARAM_SCALE = new Key<>("scale", Type.INTEGER);
    public static final Key<Integer> PARAM_OFFSET = new Key<>("offset", Type.INTEGER);
    public static final Key<Integer> PARAM_ANGLE = new Key<>("angle", Type.INTEGER);
    public static final Key<Boolean> PARAM_AUTO = new Key<>("auto", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_SCAN = new Key<>("scan", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_SOUND = new Key<>("sound", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_GRAY = new Key<>("gray", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_RENDERMODELS = new Key<>("render_models", Type.BOOLEAN);

    public static final int SLOT_CARD = 0;
    private static final int SIDE_PANEL_WIDTH = 80;
    private static final int SHAPE_CARD_SLOT_X = SIDE_PANEL_WIDTH + 15;
    private static final int PLAYER_SLOTS_X = SIDE_PANEL_WIDTH + 85;
    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(1)
            .slot(specific(s -> s.getItem() instanceof ShapeCardItem).in().out(), SLOT_CARD, SHAPE_CARD_SLOT_X, 7)
            .playerSlots(PLAYER_SLOTS_X, 142));

    @Cap(type = CapType.ITEMS_AUTOMATION)
    private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY)
            .itemValid((slot, stack) -> stack.getItem() instanceof ShapeCardItem)
            .onUpdate((slot, stack) -> onCardSlotUpdated())
            .build();

    @Cap(type = CapType.CONTAINER)
    private final Lazy<MenuProvider> screenHandler = Lazy.of(() -> new DefaultContainerProvider<GenericContainer>("Projector")
            .containerSupplier(container(ScannerModule.CONTAINER_PROJECTOR, CONTAINER_FACTORY, this))
            .itemHandler(() -> items)
            .setupSync(this));

    private final ProjectorOperation[] operations = new ProjectorOperation[4];
    private ShapeRenderer shapeRenderer;

    private boolean active = false;
    private boolean projecting = false;

    @GuiValue
    private float verticalOffset = .2f;
    @GuiValue
    private float scale = 0.01f;
    @GuiValue
    private float angle = 0.0f;
    @GuiValue
    private boolean autoRotate = false;
    @GuiValue
    private boolean scanline = true;
    @GuiValue
    private boolean sound = true;
    @GuiValue
    private boolean grayscale = false;
    @GuiValue
    private boolean renderBlockModels = false;
    private int counter = 0;

    private int powerLevel = 0;
    private int prevPowerLevel = 0;
    private int clientCounter = 0;
    private boolean loadingClientData = false;

    private boolean isLoadingClientData() {
        return loadingClientData;
    }

    private void refreshProjectionData() {
        counter++;
        shapeRenderer = null;
    }

    private void onCardSlotUpdated() {
        if (isLoadingClientData()) {
            return;
        }
        if (level != null && level.isClientSide) {
            if (shapeRenderer != null && !getShapeID().equals(shapeRenderer.getShapeID())) {
                shapeRenderer = null;
            }
            updateProjecting();
            return;
        }
        refreshProjectionData();
        updateProjecting();
        setChanged();
        markDirtyClient();
    }

    public ProjectorTileEntity(BlockPos pos, BlockState state) {
        this(ScannerModule.TYPE_PROJECTOR.get(), pos, state);
    }

    public ProjectorTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        for (int i = 0; i < operations.length; i++) {
            operations[i] = new ProjectorOperation();
        }
        for (ProjectorOperation operation : operations) {
            operation.setOpcodeOn(ProjectorOpcode.NONE);
            operation.setOpcodeOff(ProjectorOpcode.NONE);
        }
        operations[0].setOpcodeOn(ProjectorOpcode.ON);
        operations[0].setOpcodeOff(ProjectorOpcode.ON);
        setRSMode(RedstoneMode.REDSTONE_IGNORED);
        active = true;
    }

    @Override
    protected boolean needsRedstoneMode() {
        return false;
    }

    @Override
    protected void tickServer() {
        updateRedstoneOperations();
        updateProjecting();
    }

    @Override
    protected void tickClient() {
        if (autoRotate) {
            angle += 1.0f;
            if (angle >= 360.0f) {
                angle = 0.0f;
            }
        }
        if (clientCounter != counter) {
            clientCounter = counter;
            RenderData data = ShapeRenderer.getRenderDataAndCreate(getShapeID());
            data.clearRequest();
            data.clearData();
            data.setChecksum(Long.MIN_VALUE);
            data.setWantData(true);
            shapeRenderer = null;
        }
    }

    public InteractionResult interact(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.isCrouching()) {
            if (!held.isEmpty() && held.getItem() instanceof ShapeCardItem) {
                if (!level.isClientSide) {
                    ItemStack previous = items.getStackInSlot(SLOT_CARD);
                    ItemStack inserted = held.copyWithCount(1);
                    items.setStackInSlot(SLOT_CARD, inserted);
                    held.shrink(1);
                    if (!previous.isEmpty()) {
                        if (!player.addItem(previous)) {
                            player.drop(previous, false);
                        }
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (held.isEmpty()) {
                ItemStack card = items.getStackInSlot(SLOT_CARD);
                if (!card.isEmpty()) {
                    if (!level.isClientSide) {
                        player.setItemInHand(hand, card);
                        items.setStackInSlot(SLOT_CARD, ItemStack.EMPTY);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return InteractionResult.PASS;
    }

    private void updateRedstoneOperations() {
        prevPowerLevel = powerLevel;
        powerLevel = calculatePowerLevel();
        boolean pulse = prevPowerLevel != powerLevel;
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            int index = facing.get2DDataValue();
            ProjectorOperation op = operations[index];
            int pl = index ^ 1;
            if (((powerLevel >> pl) & 1) != 0) {
                handleOpcode(op.getOpcodeOn(), op.getValueOn(), pulse);
            } else {
                handleOpcode(op.getOpcodeOff(), op.getValueOff(), pulse);
            }
        }
    }

    private int calculatePowerLevel() {
        Direction horiz = getBlockOrientation();
        if (horiz == null) {
            return 0;
        }
        Direction north = reorient(Direction.NORTH, horiz);
        Direction south = reorient(Direction.SOUTH, horiz);
        Direction west = reorient(Direction.WEST, horiz);
        Direction east = reorient(Direction.EAST, horiz);

        int powered1 = getInputStrength(north) > 0 ? 1 : 0;
        int powered2 = getInputStrength(south) > 0 ? 2 : 0;
        int powered3 = getInputStrength(west) > 0 ? 4 : 0;
        int powered4 = getInputStrength(east) > 0 ? 8 : 0;
        return powered1 + powered2 + powered3 + powered4;
    }

    private void handleOpcode(ProjectorOpcode op, @Nullable Double val, boolean pulse) {
        if (op == null) {
            op = ProjectorOpcode.NONE;
        }
        switch (op) {
            case NONE -> {
            }
            case ON -> active = true;
            case OFF -> active = false;
            case SCAN -> {
                if (pulse) {
                    refreshProjectionData();
                    markDirtyClient();
                }
            }
            case OFFSET -> {
                double o = getOffsetDouble();
                if (val != null && Math.abs(o - val) > .3) {
                    if (o < val) {
                        o++;
                    } else {
                        o--;
                    }
                    setOffsetInt(o);
                    markDirtyClient();
                }
            }
            case ROT -> {
                int o = getAngleInt();
                if (val != null && o != val.intValue()) {
                    if (o < val) {
                        o++;
                    } else {
                        o--;
                    }
                    setAngleInt(o);
                    markDirtyClient();
                }
            }
            case SCALE -> {
                double o = getScaleDouble();
                if (val != null && Math.abs(o - val) > .3) {
                    if (o < val) {
                        o++;
                    } else {
                        o--;
                    }
                    setScaleInt(o);
                    markDirtyClient();
                }
            }
            case GRAYON -> {
                if (!grayscale) {
                    grayscale = true;
                    refreshProjectionData();
                    markDirtyClient();
                }
            }
            case GRAYOFF -> {
                if (grayscale) {
                    grayscale = false;
                    refreshProjectionData();
                    markDirtyClient();
                }
            }
        }
    }

    private void updateProjecting() {
        boolean newProjecting = active && !getRenderStack().isEmpty();
        if (newProjecting != projecting) {
            projecting = newProjecting;
            markDirtyClient();
        }
    }

    @Nullable
    private Direction getBlockOrientation() {
        BlockState state = level.getBlockState(worldPosition);
        if (state.getBlock() == ScannerModule.PROJECTOR.get()) {
            return OrientationTools.getOrientationHoriz(state);
        }
        return null;
    }

    private int getInputStrength(Direction side) {
        BlockPos p = worldPosition.relative(side);
        int power = level.getSignal(p, side);
        if (power == 0) {
            BlockState blockState = level.getBlockState(p);
            if (blockState.getBlock() instanceof RedStoneWireBlock) {
                power = level.hasNeighborSignal(p) ? 15 : 0;
            }
        }
        return power;
    }

    private static Direction reorient(Direction side, Direction blockDirection) {
        return switch (blockDirection) {
            case NORTH -> {
                if (side == Direction.DOWN || side == Direction.UP) {
                    yield side;
                }
                yield side.getOpposite();
            }
            case SOUTH -> side;
            case WEST -> {
                if (side == Direction.DOWN || side == Direction.UP) {
                    yield side;
                } else if (side == Direction.WEST) {
                    yield Direction.NORTH;
                } else if (side == Direction.NORTH) {
                    yield Direction.EAST;
                } else if (side == Direction.EAST) {
                    yield Direction.SOUTH;
                } else {
                    yield Direction.WEST;
                }
            }
            case EAST -> {
                if (side == Direction.DOWN || side == Direction.UP) {
                    yield side;
                } else if (side == Direction.WEST) {
                    yield Direction.SOUTH;
                } else if (side == Direction.NORTH) {
                    yield Direction.WEST;
                } else if (side == Direction.EAST) {
                    yield Direction.NORTH;
                } else {
                    yield Direction.EAST;
                }
            }
            default -> side;
        };
    }

    public ItemStack removeCard() {
        ItemStack card = items.getStackInSlot(SLOT_CARD);
        items.setStackInSlot(SLOT_CARD, ItemStack.EMPTY);
        return card;
    }

    public ItemStack getRenderStack() {
        return items.getStackInSlot(SLOT_CARD);
    }

    public ShapeID getShapeID() {
        ItemStack stack = getRenderStack();
        int scanId = ShapeCardItem.getScanId(stack);
        boolean solid = renderBlockModels ? false : ShapeCardItem.isSolid(stack);
        ResourceKey<Level> dimension = level == null ? Level.OVERWORLD : level.dimension();
        if (scanId == 0) {
            return new ShapeID(dimension, worldPosition, scanId, grayscale, solid);
        }
        return new ShapeID(Level.OVERWORLD, null, scanId, grayscale, solid);
    }

    public ShapeRenderer getShapeRenderer() {
        ShapeID shapeID = getShapeID();
        if (shapeRenderer == null || !shapeID.equals(shapeRenderer.getShapeID())) {
            shapeRenderer = new ShapeRenderer(shapeID);
        } else {
            shapeRenderer.setShapeID(shapeID);
        }
        shapeRenderer.setRefreshCounter(counter);
        return shapeRenderer;
    }

    public ProjectorOperation[] getOperations() {
        return operations;
    }

    public float getVerticalOffset() {
        return verticalOffset;
    }

    public int getOffsetInt() {
        return Math.round((float) getOffsetDouble());
    }

    private double getOffsetDouble() {
        return verticalOffset * 20;
    }

    private void setOffsetInt(double o) {
        verticalOffset = (float) (o / 20.0);
        setChanged();
    }

    public float getScale() {
        return scale;
    }

    public int getScaleInt() {
        return Math.round((float) getScaleDouble());
    }

    private double getScaleDouble() {
        return 20.0 * Math.log((scale - 0.001f) / 0.1f * 147.4131f + 1);
    }

    private void setScaleInt(double s) {
        scale = ((float) Math.exp(s / 20.0) - 1) / 147.4131f * 0.1f + 0.001f;
        setChanged();
    }

    public float getAngle() {
        return angle;
    }

    public int getAngleInt() {
        return Math.round(angle);
    }

    private void setAngleInt(int a) {
        angle = a;
        setChanged();
    }

    public boolean isAutoRotate() {
        return autoRotate;
    }

    public boolean isScanline() {
        return scanline;
    }

    public boolean isSound() {
        return sound;
    }

    public boolean isGrayscale() {
        return grayscale;
    }

    public boolean isRenderBlockModels() {
        return renderBlockModels;
    }

    public boolean isProjecting() {
        return projecting;
    }

    public int getCounter() {
        return counter;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        verticalOffset = tag.contains("offs") ? tag.getFloat("offs") : .2f;
        scale = tag.contains("scale") ? tag.getFloat("scale") : .01f;
        angle = tag.getFloat("angle");
        autoRotate = tag.getBoolean("rot");
        scanline = !tag.contains("scan") || tag.getBoolean("scan");
        sound = !tag.contains("sound") || tag.getBoolean("sound");
        grayscale = tag.getBoolean("grayscale");
        renderBlockModels = tag.getBoolean("render_models");
        projecting = tag.getBoolean("projecting");
        active = !tag.contains("active") || tag.getBoolean("active");
        counter = tag.getInt("counter");
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            String key = "op_" + facing.getName();
            if (tag.contains(key)) {
                int index = facing.get2DDataValue();
                CompoundTag tc = tag.getCompound(key);
                ProjectorOperation op = operations[index];
                op.setOpcodeOn(ProjectorOpcode.getByCode(tc.getString("on")));
                op.setOpcodeOff(ProjectorOpcode.getByCode(tc.getString("off")));
                op.setValueOn(tc.contains("von") ? tc.getDouble("von") : null);
                op.setValueOff(tc.contains("voff") ? tc.getDouble("voff") : null);
            }
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat("offs", verticalOffset);
        tag.putFloat("scale", scale);
        tag.putFloat("angle", angle);
        tag.putBoolean("rot", autoRotate);
        tag.putBoolean("scan", scanline);
        tag.putBoolean("sound", sound);
        tag.putBoolean("grayscale", grayscale);
        tag.putBoolean("render_models", renderBlockModels);
        tag.putBoolean("projecting", projecting);
        tag.putBoolean("active", active);
        tag.putInt("counter", counter);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            int index = facing.get2DDataValue();
            ProjectorOperation op = operations[index];
            CompoundTag tc = new CompoundTag();
            tc.putString("on", op.getOpcodeOn().getCode());
            tc.putString("off", op.getOpcodeOff().getCode());
            if (op.getValueOn() != null) {
                tc.putDouble("von", op.getValueOn());
            }
            if (op.getValueOff() != null) {
                tc.putDouble("voff", op.getValueOff());
            }
            tag.put("op_" + facing.getName(), tc);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        loadClientDataFromNBT(tag);
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tag) {
        CompoundTag stackTag = new CompoundTag();
        getRenderStack().save(stackTag);
        tag.put("card", stackTag);
        tag.putBoolean("projecting", projecting);
        tag.putBoolean("active", active);
        tag.putFloat("offs", verticalOffset);
        tag.putFloat("scale", scale);
        tag.putFloat("angle", angle);
        tag.putBoolean("rot", autoRotate);
        tag.putBoolean("scan", scanline);
        tag.putBoolean("sound", sound);
        tag.putBoolean("grayscale", grayscale);
        tag.putBoolean("render_models", renderBlockModels);
        tag.putInt("counter", counter);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            int index = facing.get2DDataValue();
            ProjectorOperation op = operations[index];
            CompoundTag tc = new CompoundTag();
            tc.putString("on", op.getOpcodeOn().getCode());
            tc.putString("off", op.getOpcodeOff().getCode());
            if (op.getValueOn() != null) {
                tc.putDouble("von", op.getValueOn());
            }
            if (op.getValueOff() != null) {
                tc.putDouble("voff", op.getValueOff());
            }
            tag.put("op_" + facing.getName(), tc);
        }
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tag) {
        ShapeID oldShapeId = getShapeID();
        loadingClientData = true;
        items.setStackInSlot(SLOT_CARD, ItemStack.of(tag.getCompound("card")));
        loadingClientData = false;
        projecting = tag.getBoolean("projecting");
        active = !tag.contains("active") || tag.getBoolean("active");
        verticalOffset = tag.contains("offs") ? tag.getFloat("offs") : .2f;
        scale = tag.contains("scale") ? tag.getFloat("scale") : .01f;
        angle = tag.getFloat("angle");
        autoRotate = tag.getBoolean("rot");
        scanline = !tag.contains("scan") || tag.getBoolean("scan");
        sound = !tag.contains("sound") || tag.getBoolean("sound");
        grayscale = tag.getBoolean("grayscale");
        renderBlockModels = tag.getBoolean("render_models");
        counter = tag.getInt("counter");
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            String key = "op_" + facing.getName();
            if (tag.contains(key)) {
                int index = facing.get2DDataValue();
                CompoundTag tc = tag.getCompound(key);
                ProjectorOperation op = operations[index];
                op.setOpcodeOn(ProjectorOpcode.getByCode(tc.getString("on")));
                op.setOpcodeOff(ProjectorOpcode.getByCode(tc.getString("off")));
                op.setValueOn(tc.contains("von") ? tc.getDouble("von") : null);
                op.setValueOff(tc.contains("voff") ? tc.getDouble("voff") : null);
            }
        }
        if (!Objects.equals(oldShapeId, getShapeID())) {
            shapeRenderer = null;
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-8, 0, -8), worldPosition.offset(9, 9, 9));
    }

    @ServerCommand
    public static final Command<?> CMD_RSSETTINGS = Command.<ProjectorTileEntity>create(CMD_RSSETTINGS_ID,
            (te, player, params) -> te.applyRsSettings(params));

    @ServerCommand
    public static final Command<?> CMD_SETTINGS = Command.<ProjectorTileEntity>create(CMD_SETTINGS_ID,
            (te, player, params) -> te.applySettings(params));

    private void applyRsSettings(TypedMap params) {
        boolean changed = false;
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            int idx = facing.get2DDataValue();
            ProjectorOperation operation = operations[idx];
            ProjectorOpcode opcodeOn = ProjectorOpcode.getByCode(params.get(PARAM_OPON.get(idx)));
            ProjectorOpcode opcodeOff = ProjectorOpcode.getByCode(params.get(PARAM_OPOFF.get(idx)));
            Double valueOn = params.get(PARAM_VALON.get(idx));
            Double valueOff = params.get(PARAM_VALOFF.get(idx));
            if (operation.getOpcodeOn() != opcodeOn) {
                operation.setOpcodeOn(opcodeOn);
                changed = true;
            }
            if (operation.getOpcodeOff() != opcodeOff) {
                operation.setOpcodeOff(opcodeOff);
                changed = true;
            }
            if (!Objects.equals(operation.getValueOn(), valueOn)) {
                operation.setValueOn(valueOn);
                changed = true;
            }
            if (!Objects.equals(operation.getValueOff(), valueOff)) {
                operation.setValueOff(valueOff);
                changed = true;
            }
        }
        if (changed) {
            setChanged();
            markDirtyClient();
        }
    }

    private void applySettings(TypedMap params) {
        boolean changed = false;
        int newScale = params.get(PARAM_SCALE);
        if (newScale != getScaleInt()) {
            setScaleInt(newScale);
            changed = true;
        }
        int newOffset = params.get(PARAM_OFFSET);
        if (newOffset != getOffsetInt()) {
            setOffsetInt(newOffset);
            changed = true;
        }
        int newAngle = params.get(PARAM_ANGLE);
        if (newAngle != getAngleInt()) {
            setAngleInt(newAngle);
            changed = true;
        }
        boolean newAutoRotate = params.get(PARAM_AUTO);
        if (autoRotate != newAutoRotate) {
            autoRotate = newAutoRotate;
            changed = true;
        }
        boolean newScanline = params.get(PARAM_SCAN);
        if (scanline != newScanline) {
            scanline = newScanline;
            changed = true;
        }
        boolean newSound = params.get(PARAM_SOUND);
        if (sound != newSound) {
            sound = newSound;
            changed = true;
        }
        boolean gs = params.get(PARAM_GRAY);
        if (grayscale != gs) {
            grayscale = gs;
            refreshProjectionData();
            changed = true;
        }
        boolean newRenderModels = params.get(PARAM_RENDERMODELS);
        if (renderBlockModels != newRenderModels) {
            renderBlockModels = newRenderModels;
            refreshProjectionData();
            changed = true;
        }
        if (changed) {
            setChanged();
            markDirtyClient();
        }
    }
}
