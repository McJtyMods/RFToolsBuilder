package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.information.IPowerInformation;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.api.smartwrench.ISmartWrenchSelector;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ListCommand;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.*;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.shield.*;
import mcjty.rftoolsbuilder.modules.shield.client.GuiShield;
import mcjty.rftoolsbuilder.modules.shield.client.ShieldRenderData;
import mcjty.rftoolsbuilder.modules.shield.filters.*;
import mcjty.rftoolsbuilder.modules.shield.network.PacketNotifyServerClientReady;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import mcjty.rftoolsbuilder.shapes.Shape;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.IntStream;

import static mcjty.lib.api.container.DefaultContainerProvider.container;
import static mcjty.lib.container.SlotDefinition.generic;
import static mcjty.lib.container.SlotDefinition.specific;
import static mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingBlock.*;

public class ShieldProjectorTileEntity extends TickingTileEntity implements ISmartWrenchSelector {

    public static final String COMPONENT_NAME = "shield_projector";

    @GuiValue
    public static final Value<?, Integer> VALUE_SHIELDVISMODE = Value.<ShieldProjectorTileEntity, Integer>create("shieldVisMode", Type.INTEGER,
            te -> te.getShieldRenderingMode().ordinal(),
            (te, v) -> te.setShieldRenderingMode(ShieldRenderingMode.values()[v]));
    @GuiValue
    public static final Value<?, Integer> VALUE_SHIELDTEXTURE = Value.<ShieldProjectorTileEntity, Integer>create("shieldTexture", Type.INTEGER,
            te -> te.getShieldTexture().ordinal(),
            (te, v) -> te.setShieldTexture(ShieldTexture.values()[v]));
    @GuiValue
    public static final Value<?, Integer> VALUE_DAMAGEMODE = Value.<ShieldProjectorTileEntity, Integer>create("damageMode", Type.INTEGER,
            te -> te.getDamageMode().ordinal(),
            (te, v) -> te.setDamageMode(DamageTypeMode.values()[v]));
    @GuiValue
    public static final Value<?, Integer> VALUE_COLOR = Value.<ShieldProjectorTileEntity, Integer>create("color", Type.INTEGER, ShieldProjectorTileEntity::getShieldColor, ShieldProjectorTileEntity::setShieldColor);
    @GuiValue
    public static final Value<?, Boolean> VALUE_LIGHT = Value.<ShieldProjectorTileEntity, Boolean>create("light", Type.BOOLEAN, ShieldProjectorTileEntity::isBlockLight, ShieldProjectorTileEntity::setBlockLight);

    // Client side
    private ShieldRenderData renderData;

    private DamageTypeMode damageMode = DamageTypeMode.DAMAGETYPE_GENERIC;

    // If true the shield is currently made.
    private boolean shieldComposed = false;
    // The state for the template blocks that were used.
    private BlockState templateState = Blocks.AIR.defaultBlockState();
    // If true the shield is currently active.
    private boolean shieldActive = false;
    // Timeout in case power is low. Here we wait a bit before trying again.
    private int powerTimeout = 0;

    // Timeout for updating the shield. This is done to make sure that the shielding blocks are updated
    // a bit after the shield projector itself has had a change to update its client-side data
    private int updateTimeout = 0;

    private int shieldColor;

    // If true light is blocked
    private boolean blockLight = false;

    private int supportedBlocks;
    private float damageFactor = 1.0f;
    private float costFactor = 1.0f;

    // Filter list.
    private final List<ShieldFilter> filters = new ArrayList<>();

    private ShieldTexture shieldTexture = ShieldTexture.SHIELD;
    private ShieldRenderingMode shieldRenderingMode = ShieldRenderingMode.SHIELD;

    private final List<RelCoordinateShield> shieldBlocks = new ArrayList<>();
    private final List<BlockState> blockStateTable = new ArrayList<>();

    private final FakePlayerGetter fakePlayer = new FakePlayerGetter(this, "rftools_shield");

    public static final int SLOT_BUFFER = 0;
    public static final int SLOT_SHAPE = 1;
    public static final int SLOT_SHARD = 2;
    public static final int BUFFER_SIZE = 3;
    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(BUFFER_SIZE)
            .slot(generic().in(), SLOT_BUFFER, 26, 142)
            .slot(specific(s -> s.getItem() instanceof ShapeCardItem).in().out(), SLOT_SHAPE, 26, 200)
            .slot(specific(s -> s.getItem() == VariousModule.DIMENSIONALSHARD.get()).in().out(), SLOT_SHARD, 229, 118)
            .playerSlots(85, 142));

    @Cap(type = CapType.ITEMS_AUTOMATION)
    private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY)
            .itemValid((slot, stack) -> {
                if (slot == SLOT_SHAPE) {
                    return stack.getItem() instanceof ShapeCardItem;
                } else if (slot == SLOT_SHARD) {
                    return stack.getItem() == VariousModule.DIMENSIONALSHARD.get();
                } else {
                    return true;
                }
            })
            .onUpdate((index, stack) -> {
                if (index == SLOT_SHAPE && !stack.isEmpty()) {
                    // Restart if we go from having a stack to not having stack or the other way around.
                    decomposeShield();
                }
            })
            .build();


    private final GenericEnergyStorage energyStorage;
    @Cap(type = CapType.ENERGY)
    private final LazyOptional<GenericEnergyStorage> energyHandler = LazyOptional.of(this::getEnergyStorage);

    @Cap(type = CapType.CONTAINER)
    private final LazyOptional<MenuProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Shield")
            .containerSupplier(container(ShieldModule.CONTAINER_SHIELD, CONTAINER_FACTORY, this))
            .energyHandler(this::getEnergyStorage)
            .itemHandler(() -> items)
            .setupSync(this));

    @Cap(type = CapType.INFUSABLE)
    private final LazyOptional<IInfusable> infusableHandler = LazyOptional.of(() -> new DefaultInfusable(ShieldProjectorTileEntity.this));

    @Cap(type = CapType.POWER_INFO)
    private final LazyOptional<IPowerInformation> powerInfoHandler = LazyOptional.of(this::createPowerInfo);

    private final int maxEnergy;
    private final int rfPerTick;

    public ShieldProjectorTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int supportedBlocks, int maxEnergy, int rfPerTick) {
        super(type, pos, state);
        this.supportedBlocks = supportedBlocks;
        this.maxEnergy = maxEnergy;
        this.rfPerTick = rfPerTick;
        energyStorage = new GenericEnergyStorage(this, true, getConfigMaxEnergy(), getConfigRfPerTick());
    }

    @Nonnull
    public GenericEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    private int getConfigMaxEnergy() {
        return maxEnergy;
    }

    private int getConfigRfPerTick() {
        return rfPerTick;
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    public ShieldProjectorTileEntity setDamageFactor(float factor) {
        this.damageFactor = factor;
        return this;
    }

    public ShieldProjectorTileEntity setCostFactor(float factor) {
        this.costFactor = factor;
        return this;
    }

    public ShieldRenderData getRenderData() {
        if (renderData == null) {
            float r = ((shieldColor >> 16) & 0xff) / 255.0f;
            float g = ((shieldColor >> 8) & 0xff) / 255.0f;
            float b = (shieldColor & 0xff) / 255.0f;
            renderData = new ShieldRenderData(r, g, b, 1.0f, shieldTexture);
        }
        return renderData;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        int oldColor = shieldColor;
        ShieldTexture oldTexture = shieldTexture;
        super.onDataPacket(net, packet);
        if (oldColor != shieldColor || oldTexture != shieldTexture) {
            renderData = null;
            // @todo this doesn't help to automatically update the color
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    public boolean isPowered() {
        return powerLevel > 0;
    }

    public List<ShieldFilter> getFilters() {
        return filters;
    }

    public boolean isBlockLight() {
        return blockLight;
    }

    public void setBlockLight(boolean blockLight) {
        this.blockLight = blockLight;
        updateTimeout = 10;
        setChanged();
    }

    public int getShieldColor() {
        return shieldColor;
    }

    public void setShieldColor(int shieldColor) {
        this.shieldColor = shieldColor;
        updateTimeout = 10;
        setChanged();
    }

    private void delFilter(int selected) {
        filters.remove(selected);
        updateTimeout = 10;
        setChanged();
    }

    private void upFilter(int selected) {
        ShieldFilter filter1 = filters.get(selected - 1);
        ShieldFilter filter2 = filters.get(selected);
        filters.set(selected - 1, filter2);
        filters.set(selected, filter1);
        setChanged();
    }

    private void downFilter(int selected) {
        ShieldFilter filter1 = filters.get(selected);
        ShieldFilter filter2 = filters.get(selected + 1);
        filters.set(selected, filter2);
        filters.set(selected + 1, filter1);
        setChanged();
    }

    private void addFilter(int action, String type, String player, int selected) {
        ShieldFilter filter = AbstractShieldFilter.createFilter(type);
        filter.setAction(action);
        if (filter instanceof PlayerFilter) {
            ((PlayerFilter) filter).setName(player);
        }
        if (selected == -1) {
            filters.add(filter);
        } else {
            filters.add(selected, filter);
        }
        updateTimeout = 10;
        setChanged();
    }

    public DamageTypeMode getDamageMode() {
        return damageMode;
    }

    public void setDamageMode(DamageTypeMode damageMode) {
        this.damageMode = damageMode;
        setChanged();
    }

    public ShieldRenderingMode getShieldRenderingMode() {
        return shieldRenderingMode;
    }

    public void setShieldRenderingMode(ShieldRenderingMode shieldRenderingMode) {
        this.shieldRenderingMode = shieldRenderingMode;
        updateTimeout = 10;
        setChanged();
    }

    public ShieldTexture getShieldTexture() {
        return shieldTexture;
    }

    public void setShieldTexture(ShieldTexture shieldTexture) {
        this.shieldTexture = shieldTexture;
        updateTimeout = 10;
        setChanged();
    }

    @Nonnull
    private BlockState getStateFromItem(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof BlockItem blockItem) {
            Player player = fakePlayer.get();
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            BlockHitResult result = new BlockHitResult(new Vec3(.5, 0, .5), Direction.UP, worldPosition, false);
            BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, result));
            BlockState stateForPlacement = blockItem.getBlock().getStateForPlacement(context);
            return stateForPlacement == null ? blockItem.getBlock().defaultBlockState() : stateForPlacement;
        } else {
            return Blocks.AIR.defaultBlockState();
        }
    }

    @Nullable
    private BlockState calculateMimic() {
        if (!ShieldRenderingMode.MIMIC.equals(shieldRenderingMode)) {
            return null;
        }
        Optional<BlockState> map = getCapability(ForgeCapabilities.ITEM_HANDLER)
                .map(h -> h.getStackInSlot(SLOT_BUFFER))
                .filter(stack -> !stack.isEmpty())
                .map(this::getStateFromItem);
        if (map.isPresent()) {
            return map.orElseThrow(RuntimeException::new);
        } else {
            return null;
        }
    }

    private BlockState calculateShieldBlock(BlockState mimic, boolean blockLight) {
        if (!shieldActive || powerTimeout > 0) {
            return Blocks.AIR.defaultBlockState();
        }

        ShieldRenderingMode render = shieldRenderingMode;
        if (!ShieldConfiguration.allowInvisibleShield.get() && ShieldRenderingMode.INVISIBLE.equals(shieldRenderingMode)) {
            render = ShieldRenderingMode.SOLID;
        }
        if (mimic != null) {
            render = ShieldRenderingMode.MIMIC;
        }

        BlockState shielding;
        shielding = getShieldingBlock(render, mimic).defaultBlockState();
        shielding = shielding.setValue(FLAG_OPAQUE, !blockLight);
        shielding = shielding.setValue(RENDER_MODE, render);
        shielding = calculateShieldCollisionData(shielding);
        shielding = calculateDamageBits(shielding);
        return shielding;
    }

    private Block getShieldingBlock(ShieldRenderingMode render, BlockState mimic) {
        if (mimic != null) {
            // @todo 1.15
//            switch (mimic.getBlock().getRenderLayer()) {
//                case SOLID:
//                    return ShieldSetup.SHIELDING_SOLID.get();
//                case CUTOUT_MIPPED:
//                    return ShieldSetup.SHIELDING_CUTOUT.get();   // @todo check?
//                case CUTOUT:
//                    return ShieldSetup.SHIELDING_CUTOUT.get();
//                case TRANSLUCENT:
//                    return ShieldSetup.SHIELDING_TRANSLUCENT.get();
//            }
        } else {
            if (render.isTranslucent()) {
                return ShieldModule.SHIELDING_TRANSLUCENT.get();
            } else {
                return ShieldModule.SHIELDING_SOLID.get();
            }
        }
        return ShieldModule.SHIELDING_SOLID.get();
    }

    private BlockState calculateDamageBits(BlockState shielding) {
        for (ShieldFilter filter : filters) {
            if ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0) {
                if (ItemFilter.ITEM.equals(filter.getFilterName())) {
                    shielding = shielding.setValue(DAMAGE_ITEMS, true);
                } else if (AnimalFilter.ANIMAL.equals(filter.getFilterName())) {
                    shielding = shielding.setValue(DAMAGE_PASSIVE, true);
                } else if (HostileFilter.HOSTILE.equals(filter.getFilterName())) {
                    shielding = shielding.setValue(DAMAGE_HOSTILE, true);
                } else if (PlayerFilter.PLAYER.equals(filter.getFilterName())) {
                    shielding = shielding.setValue(DAMAGE_PLAYERS, true);
                } else if (DefaultFilter.DEFAULT.equals(filter.getFilterName())) {
                    shielding = shielding.setValue(DAMAGE_ITEMS, true).setValue(DAMAGE_PASSIVE, true).setValue(DAMAGE_HOSTILE, true).setValue(DAMAGE_PLAYERS, true);
                }
            }
        }
        return shielding;
    }

    private BlockState calculateShieldCollisionData(BlockState shielding) {
        for (ShieldFilter filter : filters) {
            if ((filter.getAction() & ShieldFilter.ACTION_SOLID) != 0) {
                if (ItemFilter.ITEM.equals(filter.getFilterName())) {
                    shielding = shielding.setValue(BLOCKED_ITEMS, true);
                } else if (AnimalFilter.ANIMAL.equals(filter.getFilterName())) {
                    shielding = shielding.setValue(BLOCKED_PASSIVE, true);
                } else if (HostileFilter.HOSTILE.equals(filter.getFilterName())) {
                    shielding = shielding.setValue(BLOCKED_HOSTILE, true);
                } else if (PlayerFilter.PLAYER.equals(filter.getFilterName())) {
                    shielding = shielding.setValue(BLOCKED_PLAYERS, true);
                } else if (DefaultFilter.DEFAULT.equals(filter.getFilterName())) {
                    shielding = shielding.setValue(BLOCKED_ITEMS, true).setValue(BLOCKED_PASSIVE, true).setValue(BLOCKED_HOSTILE, true).setValue(BLOCKED_PLAYERS, true);
                }
            }
        }
        return shielding;
    }

    private int calculateRfPerTick() {
        if (!shieldActive) {
            return 0;
        }
        int s = shieldBlocks.size() - 50;
        if (s < 10) {
            s = 10;
        }
        int rf = ShieldConfiguration.rfBase.get() * s / 10;
        if (ShieldRenderingMode.SHIELD.equals(shieldRenderingMode)) {
            rf += ShieldConfiguration.rfShield.get() * s / 10;
        } else if (ShieldRenderingMode.MIMIC.equals(shieldRenderingMode)) {
            rf += ShieldConfiguration.rfCamo.get() * s / 10;
        }
        return rf;
    }

    public boolean isShieldComposed() {
        return shieldComposed;
    }

    public boolean isShieldActive() {
        return shieldActive;
    }

    private ItemStack lootingSword = ItemStack.EMPTY;

    public void applyDamageToEntity(Entity entity) {
        DamageSource source;
        int rf;
        if (DamageTypeMode.DAMAGETYPE_GENERIC.equals(damageMode)) {
            rf = ShieldConfiguration.rfDamage.get();
            source = DamageTools.getGenericDamageSource(entity);
        } else {
            rf = ShieldConfiguration.rfDamagePlayer.get();
            ServerPlayer killer = fakePlayer.get();
//            killer.setLevel((ServerLevel) level);
            killer.setPos(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
            new FakePlayerConnection(level.getServer(), killer);
            ItemStack shards = items.getStackInSlot(SLOT_SHARD);
            if (!shards.isEmpty() && shards.getCount() >= ShieldConfiguration.shardsPerLootingKill.get()) {
                items.extractItem(SLOT_SHARD, ShieldConfiguration.shardsPerLootingKill.get(), false);
                if (lootingSword.isEmpty()) {
                    lootingSword = createEnchantedItem(Items.DIAMOND_SWORD, Enchantments.MOB_LOOTING, ShieldConfiguration.lootingKillBonus.get());
                }
                lootingSword.setDamageValue(0);
                killer.setItemInHand(InteractionHand.MAIN_HAND, lootingSword);
            } else {
                killer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
            source = DamageTools.getPlayerAttackDamageSource(killer, killer);
        }

        float factor = infusableHandler.map(IInfusable::getInfusedFactor).orElse(0.0f);
        rf = (int) (rf * costFactor * (4.0f - factor) / 4.0f);
        if (energyStorage.getEnergyStored() < rf) {
            // Not enough RF to do damage.
            return;
        }
        energyStorage.consumeEnergy(rf);

        float damage = (float) (double) ShieldConfiguration.damage.get();
        damage *= damageFactor;
        damage = damage * (1.0f + factor / 2.0f);

        entity.hurt(source, damage);
    }

    public static ItemStack createEnchantedItem(Item item, Enchantment effectId, int amount) {
        ItemStack stack = new ItemStack(item);
        Map<Enchantment, Integer> enchant = new HashMap<>();
        enchant.put(effectId, amount);
        EnchantmentHelper.setEnchantments(enchant, stack);
        return stack;
    }

    @Override
    protected void tickServer() {
        if (level == null) {
            return;
        }
        if (!shieldComposed) {
            // do nothing if the shield is not composed
            return;
        }

        // Delayed update of shield
        if (updateTimeout > 0) {
            updateTimeout--;
            if (updateTimeout <= 0) {
                updateShield();
            }
        }

        boolean checkPower = false;
        if (powerTimeout > 0) {
            powerTimeout--;
            setChanged();
            if (powerTimeout > 0) {
                return;
            } else {
                checkPower = true;
            }
        }

        boolean needsUpdate = false;

        int rf = getRfPerTick();

        if (rf > 0) {
            if (energyStorage.getEnergyStored() < rf) {
                powerTimeout = 100;     // Wait 5 seconds before trying again.
                needsUpdate = true;
            } else {
                if (checkPower) {
                    needsUpdate = true;
                }
                energyStorage.consumeEnergy(rf);
            }
        }

        boolean newShieldActive = isMachineEnabled();
        if (newShieldActive != shieldActive) {
            needsUpdate = true;
            shieldActive = newShieldActive;
        }

        if (needsUpdate) {
            updateShield();
            markDirtyClient();
        }
    }

    public void clientIsReady() {
        updateShield();
    }

    private int getRfPerTick() {
        int rf = calculateRfPerTick();
        float factor = infusableHandler.map(IInfusable::getInfusedFactor).orElse(0.0f);
        rf = (int) (rf * (2.0f - factor) / 2.0f);
        return rf;
    }

    public void composeDecomposeShield(boolean ctrl) {
        if (shieldComposed) {
            // Shield is already composed. Break it into template blocks again.
            decomposeShield();
        } else {
            // Shield is not composed. Find all nearby template blocks and form a shield.
            composeShield(ctrl);
        }
    }

    public void composeShield(boolean ctrl) {
        shieldBlocks.clear();
        blockStateTable.clear();
        Map<BlockPos, BlockState> coordinates;

        if (isShapedShield()) {
            // Special shaped mode.
            templateState = Blocks.AIR.defaultBlockState();

            ItemStack shapeItem = items.getStackInSlot(SLOT_SHAPE);
            Shape shape = ShapeCardItem.getShape(shapeItem);
            boolean solid = ShapeCardItem.isSolid(shapeItem);
            BlockPos dimension = ShapeCardItem.getClampedDimension(shapeItem, ShieldConfiguration.maxShieldDimension.get());
            BlockPos offset = ShapeCardItem.getClampedOffset(shapeItem, ShieldConfiguration.maxShieldOffset.get());
            Map<BlockPos, BlockState> col = new HashMap<>();
            ShapeCardItem.composeFormula(shapeItem, shape.getFormulaFactory().get(), getLevel(), getBlockPos(), dimension, offset, col, supportedBlocks, solid, false, null);
            coordinates = col;
        } else {
            if (!findTemplateState()) return;

            Map<BlockPos, BlockState> col = new HashMap<>();
            findTemplateBlocks(col, templateState, ctrl, getBlockPos());
            coordinates = col;
        }

        int xCoord = getBlockPos().getX();
        int yCoord = getBlockPos().getY();
        int zCoord = getBlockPos().getZ();
        for (Map.Entry<BlockPos, BlockState> entry : coordinates.entrySet()) {
            BlockPos c = entry.getKey();
            BlockState state = entry.getValue();
            int st = -1;
            if (state != null) {
                for (int i = 0; i < blockStateTable.size(); i++) {
                    if (state.equals(blockStateTable.get(i))) {
                        st = i;
                        break;
                    }
                }
                if (st == -1) {
                    st = blockStateTable.size();
                    blockStateTable.add(state);
                }
            }
            shieldBlocks.add(new RelCoordinateShield(c.getX() - xCoord, c.getY() - yCoord, c.getZ() - zCoord, st));
            getLevel().setBlockAndUpdate(c, Blocks.AIR.defaultBlockState());
        }

        shieldComposed = true;
        updateShield();
    }

    private boolean isShapedShield() {
        return !items.getStackInSlot(SLOT_SHAPE).isEmpty();
    }

    private boolean findTemplateState() {
        for (Direction dir : OrientationTools.DIRECTION_VALUES) {
            BlockPos p = getBlockPos().relative(dir);
            if (p.getY() >= level.getMinBuildHeight() && p.getY() < level.getMaxBuildHeight()) {
                BlockState state = getLevel().getBlockState(p);
                if (state.getBlock() instanceof ShieldTemplateBlock) {
                    templateState = state;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void selectBlock(Player player, BlockPos pos) {
        if (!shieldComposed) {
            Logging.message(player, ChatFormatting.YELLOW + "Shield is not composed. Nothing happens!");
            return;
        }

        float squaredDistance = (float) getBlockPos().distSqr(pos);
        if (squaredDistance > ShieldConfiguration.maxDisjointShieldDistance.get() * ShieldConfiguration.maxDisjointShieldDistance.get()) {
            Logging.message(player, ChatFormatting.YELLOW + "This template is too far to connect to the shield!");
            return;
        }

        int xCoord = getBlockPos().getX();
        int yCoord = getBlockPos().getY();
        int zCoord = getBlockPos().getZ();

        Block origBlock = getLevel().getBlockState(pos).getBlock();
        if (origBlock instanceof ShieldTemplateBlock) {
            if (isShapedShield()) {
                Logging.message(player, ChatFormatting.YELLOW + "You cannot add template blocks to a shaped shield (using a shape card)!");
                return;
            }
            Map<BlockPos, BlockState> templateBlocks = new HashMap<>();
            BlockState state = getLevel().getBlockState(pos);
            templateBlocks.put(pos, null);
            findTemplateBlocks(templateBlocks, state, false, pos);

            BlockState mimic = calculateMimic();
            BlockState shielding = calculateShieldBlock(mimic, blockLight);

            for (Map.Entry<BlockPos, BlockState> entry : templateBlocks.entrySet()) {
                BlockPos templateBlock = entry.getKey();
                RelCoordinateShield relc = new RelCoordinateShield(templateBlock.getX() - xCoord, templateBlock.getY() - yCoord, templateBlock.getZ() - zCoord, -1);
                shieldBlocks.add(relc);
                updateShieldBlock(mimic, shielding, relc);
            }
        } else if (origBlock instanceof ShieldingBlock) {
            //@todo
            int dx = pos.getX() - xCoord;
            int dy = pos.getY() - yCoord;
            int dz = pos.getZ() - zCoord;
            int idx = IntStream.range(0, shieldBlocks.size()).filter(i -> shieldBlocks.get(i).matches(dx, dy, dz)).findFirst().orElse(-1);
            if (idx != -1) {
                shieldBlocks.remove(idx);
            }
            getLevel().setBlock(pos, templateState, Block.UPDATE_CLIENTS);
        } else {
            Logging.message(player, ChatFormatting.YELLOW + "The selected shield can't do anything with this block!");
            return;
        }
        setChanged();
    }

    /**
     * Update all shield blocks. Possibly creating the shield.
     */
    private void updateShield() {
        BlockState mimic = calculateMimic();
        BlockState shielding = calculateShieldBlock(mimic, blockLight);
        int xCoord = getBlockPos().getX();
        int yCoord = getBlockPos().getY();
        int zCoord = getBlockPos().getZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (RelCoordinateShield c : shieldBlocks) {
            if (Blocks.AIR.equals(shielding.getBlock())) {
                pos.set(xCoord + c.dx(), yCoord + c.dy(), zCoord + c.dz());
                BlockState oldState = getLevel().getBlockState(pos);
                if (oldState.getBlock() instanceof ShieldingBlock) {
                    getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
            } else {
                updateShieldBlock(mimic, shielding, c);
            }
        }
        setChanged();
    }

    private void updateShieldBlock(BlockState mimic, BlockState shielding, RelCoordinateShield c) {
        int xCoord = getBlockPos().getX();
        int yCoord = getBlockPos().getY();
        int zCoord = getBlockPos().getZ();
        BlockPos pp = new BlockPos(xCoord + c.dx(), yCoord + c.dy(), zCoord + c.dz());
        BlockState oldState = getLevel().getBlockState(pp);
        if (!(oldState.getBlock() instanceof ShieldingBlock)) {
            if ((!oldState.canBeReplaced()) && !(oldState.getBlock() instanceof ShieldTemplateBlock)) {
                return;
            }
        }

        // To force an update set it to air first
        level.setBlockAndUpdate(pp, Blocks.AIR.defaultBlockState());
        level.setBlock(pp, shielding, Block.UPDATE_NEIGHBORS);

        BlockEntity te = getLevel().getBlockEntity(pp);
        if (te instanceof ShieldingTileEntity shieldingTE) {
            if (c.state() != -1) {
                BlockState state = blockStateTable.get(c.state());
                shieldingTE.setMimic(state);
            } else {
                shieldingTE.setMimic(mimic);
            }
            shieldingTE.setShieldProjector(worldPosition);
        }
    }

    public void decomposeShield() {
        int xCoord = getBlockPos().getX();
        int yCoord = getBlockPos().getY();
        int zCoord = getBlockPos().getZ();
        BlockPos.MutableBlockPos pp = new BlockPos.MutableBlockPos();
        for (RelCoordinateShield c : shieldBlocks) {
            int cx = xCoord + c.dx();
            int cy = yCoord + c.dy();
            int cz = zCoord + c.dz();
            pp.set(cx, cy, cz);
            Block block = getLevel().getBlockState(pp).getBlock();
            if (getLevel().isEmptyBlock(pp) || block instanceof ShieldingBlock) {
                getLevel().setBlock(new BlockPos(pp), templateState, Block.UPDATE_CLIENTS);
            } else if (!templateState.isAir()) {
                if (!isShapedShield()) {
                    // No room, just spawn the block
                    Containers.dropItemStack(getLevel(), cx, cy, cz, templateState.getBlock().getCloneItemStack(getLevel(), new BlockPos(cx, cy, cz), templateState));
                }
            }
        }
        shieldComposed = false;
        shieldActive = false;
        shieldBlocks.clear();
        blockStateTable.clear();
        setChanged();
    }

    /**
     * Find all template blocks recursively.
     *
     * @param coordinateSet the set with coordinates to update during the search
     * @param templateState the state for the shield template block we support
     * @param ctrl          if true also scan for blocks in corners
     */
    private void findTemplateBlocks(Map<BlockPos, BlockState> coordinateSet, BlockState templateState, boolean ctrl, BlockPos start) {
        Deque<BlockPos> todo = new ArrayDeque<>();

        if (ctrl) {
            addToTodoCornered(coordinateSet, todo, start, templateState);
            while (!todo.isEmpty() && coordinateSet.size() < supportedBlocks) {
                BlockPos coordinate = todo.pollFirst();
                coordinateSet.put(coordinate, null);
                addToTodoCornered(coordinateSet, todo, coordinate, templateState);
            }
        } else {
            addToTodoStraight(coordinateSet, todo, start, templateState);
            while (!todo.isEmpty() && coordinateSet.size() < supportedBlocks) {
                BlockPos coordinate = todo.pollFirst();
                coordinateSet.put(coordinate, null);
                addToTodoStraight(coordinateSet, todo, coordinate, templateState);
            }
        }
    }

    private void addToTodoStraight(Map<BlockPos, BlockState> coordinateSet, Deque<BlockPos> todo, BlockPos coordinate, BlockState templateState) {
        for (Direction dir : OrientationTools.DIRECTION_VALUES) {
            BlockPos pp = coordinate.relative(dir);
            if (pp.getY() >= level.getMinBuildHeight() && pp.getY() < level.getMaxBuildHeight()) {
                if (!coordinateSet.containsKey(pp)) {
                    BlockState state = getLevel().getBlockState(pp);
                    if (state == templateState) {
                        if (!todo.contains(pp)) {
                            todo.addLast(pp);
                        }
                    }
                }
            }
        }
    }

    private void addToTodoCornered(Map<BlockPos, BlockState> coordinateSet, Deque<BlockPos> todo, BlockPos coordinate, BlockState templateState) {
        int x = coordinate.getX();
        int y = coordinate.getY();
        int z = coordinate.getZ();
        BlockPos.MutableBlockPos c = new BlockPos.MutableBlockPos();
        for (int xx = x - 1; xx <= x + 1; xx++) {
            for (int yy = y - 1; yy <= y + 1; yy++) {
                for (int zz = z - 1; zz <= z + 1; zz++) {
                    if (xx != x || yy != y || zz != z) {
                        if (yy >= getLevel().getMinBuildHeight() && yy < getLevel().getMaxBuildHeight()) {
                            c.set(xx, yy, zz);
                            if (!coordinateSet.containsKey(c)) {
                                BlockState state = getLevel().getBlockState(c);
                                if (state == templateState) {
                                    if (!todo.contains(c)) {
                                        todo.addLast(c.immutable());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static short bytesToShort(byte b1, byte b2) {
        short s1 = (short) (b1 & 0xff);
        short s2 = (short) (b2 & 0xff);
        return (short) (s1 * 256 + s2);
    }

    private static byte shortToByte1(short s) {
        return (byte) ((s & 0xff00) >> 8);
    }

    private static byte shortToByte2(short s) {
        return (byte) (s & 0xff);
    }

//    @Override
//    public Object[] getDataForGUI() {
//        return new Object[] {
//                shieldColor, redstoneMode.ordinal(), shieldRenderingMode.ordinal(), damageMode.ordinal()
//        };
//    }
//
//    @Override
//    public void syncDataForGUI(Object[] data) {
//        shieldColor = (Integer) data[0];
//        redstoneMode = RedstoneMode.values()[(Integer) data[1]];
//        shieldRenderingMode = ShieldRenderingMode.values()[(Integer) data[2]];
//        damageMode = DamageTypeMode.values()[(Integer) data[3]];
//    }
//

    @Override
    public void loadClientDataFromNBT(CompoundTag tagCompound) {
        powerLevel = tagCompound.getByte("powered");
        shieldComposed = tagCompound.getBoolean("composed");
        shieldActive = tagCompound.getBoolean("active");
        powerTimeout = tagCompound.getInt("powerTimeout");
        if (tagCompound.contains("templateColor")) {
            int templateColor = tagCompound.getInt("templateColor");
            ShieldTemplateBlock.TemplateColor color = ShieldTemplateBlock.TemplateColor.values()[templateColor];
            switch (color) {
                case BLUE -> templateState = ShieldModule.TEMPLATE_BLUE.get().defaultBlockState();
                case RED -> templateState = ShieldModule.TEMPLATE_RED.get().defaultBlockState();
                case GREEN -> templateState = ShieldModule.TEMPLATE_GREEN.get().defaultBlockState();
                case YELLOW -> templateState = ShieldModule.TEMPLATE_YELLOW.get().defaultBlockState();
            }
        } else {
            templateState = Blocks.AIR.defaultBlockState();
        }

        loadEnergyCap(tagCompound);

        if (tagCompound.contains("Info")) {
            CompoundTag info = tagCompound.getCompound("Info");
            shieldRenderingMode = ShieldRenderingMode.values()[info.getInt("visMode")];
            shieldTexture = ShieldTexture.values()[info.getInt("shieldTexture")];
            rsMode = RedstoneMode.values()[(info.getByte("rsMode"))];
            damageMode = DamageTypeMode.values()[(info.getByte("damageMode"))];
            blockLight = info.getBoolean("blocklight");

            if (info.contains("shieldColor")) {
                shieldColor = info.getInt("shieldColor");
            } else {
                shieldColor = 0x96ffc8;
            }
            readFiltersFromNBT(info);
        }

        renderData = null;

        // We got our render data on the client. Notify the server so that the
        // server can make sure the shield blocks know this
        RFToolsBuilderMessages.sendToServer(PacketNotifyServerClientReady.create(worldPosition));
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tagCompound) {
        tagCompound.putByte("powered", (byte) powerLevel);
        tagCompound.putBoolean("composed", shieldComposed);
        tagCompound.putBoolean("active", shieldActive);
        tagCompound.putInt("powerTimeout", powerTimeout);
        if (!templateState.isAir()) {
            tagCompound.putInt("templateColor", ((ShieldTemplateBlock) templateState.getBlock()).getColor().ordinal());
        }

        CompoundTag info = getOrCreateInfo(tagCompound);
        info.putInt("visMode", shieldRenderingMode.ordinal());
        info.putInt("shieldTexture", shieldTexture.ordinal());
        info.putByte("rsMode", (byte) rsMode.ordinal());
        info.putByte("damageMode", (byte) damageMode.ordinal());

        info.putBoolean("blocklight", blockLight);
        info.putInt("shieldColor", shieldColor);

        writeFiltersToNBT(info);
        saveEnergyCap(tagCompound);
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        shieldComposed = tagCompound.getBoolean("composed");
        shieldActive = tagCompound.getBoolean("active");
        powerTimeout = tagCompound.getInt("powerTimeout");
        if (!isShapedShield()) {
            if (tagCompound.contains("templateColor")) {
                int templateColor = tagCompound.getInt("templateColor");
                ShieldTemplateBlock.TemplateColor color = ShieldTemplateBlock.TemplateColor.values()[templateColor];
                switch (color) {
                    case BLUE -> templateState = ShieldModule.TEMPLATE_BLUE.get().defaultBlockState();
                    case RED -> templateState = ShieldModule.TEMPLATE_RED.get().defaultBlockState();
                    case GREEN -> templateState = ShieldModule.TEMPLATE_GREEN.get().defaultBlockState();
                    case YELLOW -> templateState = ShieldModule.TEMPLATE_YELLOW.get().defaultBlockState();
                }
            } else {
                templateState = Blocks.AIR.defaultBlockState();
            }
        } else {
            templateState = Blocks.AIR.defaultBlockState();
        }

        shieldBlocks.clear();
        blockStateTable.clear();
        if (tagCompound.contains("relcoordsNew")) {
            byte[] byteArray = tagCompound.getByteArray("relcoordsNew");
            int j = 0;
            for (int i = 0; i < byteArray.length / 8; i++) {
                short dx = bytesToShort(byteArray[j + 0], byteArray[j + 1]);
                short dy = bytesToShort(byteArray[j + 2], byteArray[j + 3]);
                short dz = bytesToShort(byteArray[j + 4], byteArray[j + 5]);
                short st = bytesToShort(byteArray[j + 6], byteArray[j + 7]);
                j += 8;
                shieldBlocks.add(new RelCoordinateShield(dx, dy, dz, st));
            }

            ListTag list = tagCompound.getList("gstates", Tag.TAG_COMPOUND);
            for (Tag inbt : list) {
                CompoundTag tc = (CompoundTag) inbt;
                String b = tc.getString("b");
                int m = tc.getInt("m");
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(b));
                if (block == null) {
                    block = Blocks.STONE;
                    m = 0;
                }
                BlockState state = block.defaultBlockState(); // @todo 1.14 getStateFromMeta(m);
                blockStateTable.add(state);
            }
        } else {
            byte[] byteArray = tagCompound.getByteArray("relcoords");
            int j = 0;
            for (int i = 0; i < byteArray.length / 6; i++) {
                short dx = bytesToShort(byteArray[j + 0], byteArray[j + 1]);
                short dy = bytesToShort(byteArray[j + 2], byteArray[j + 3]);
                short dz = bytesToShort(byteArray[j + 4], byteArray[j + 5]);
                j += 6;
                shieldBlocks.add(new RelCoordinateShield(dx, dy, dz, -1));
            }
        }
    }

    @Override
    protected void loadInfo(CompoundTag tagCompound) {
        super.loadInfo(tagCompound);
        if (tagCompound.contains("Info")) {
            CompoundTag info = tagCompound.getCompound("Info");
            shieldRenderingMode = ShieldRenderingMode.values()[info.getInt("visMode")];
            shieldTexture = ShieldTexture.values()[info.getInt("shieldTexture")];
            damageMode = DamageTypeMode.values()[(info.getByte("damageMode"))];
            blockLight = info.getBoolean("blocklight");

            if (info.contains("shieldColor")) {
                shieldColor = info.getInt("shieldColor");
            } else {
                shieldColor = 0x96ffc8;
            }

            readFiltersFromNBT(info);
        }
    }

    private void readFiltersFromNBT(CompoundTag tagCompound) {
        filters.clear();
        ListTag filterList = tagCompound.getList("filters", Tag.TAG_COMPOUND);
        for (int i = 0; i < filterList.size(); i++) {
            CompoundTag compound = filterList.getCompound(i);
            filters.add(AbstractShieldFilter.createFilter(compound));
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tagCompound) {
        super.saveAdditional(tagCompound);
        tagCompound.putBoolean("composed", shieldComposed);
        tagCompound.putBoolean("active", shieldActive);
        tagCompound.putInt("powerTimeout", powerTimeout);
        if (!templateState.isAir()) {
            tagCompound.putInt("templateColor", ((ShieldTemplateBlock) templateState.getBlock()).getColor().ordinal());
        }
        byte[] blocks = new byte[shieldBlocks.size() * 8];
        int j = 0;
        for (RelCoordinateShield c : shieldBlocks) {
            blocks[j + 0] = shortToByte1((short) c.dx());
            blocks[j + 1] = shortToByte2((short) c.dx());
            blocks[j + 2] = shortToByte1((short) c.dy());
            blocks[j + 3] = shortToByte2((short) c.dy());
            blocks[j + 4] = shortToByte1((short) c.dz());
            blocks[j + 5] = shortToByte2((short) c.dz());
            blocks[j + 6] = shortToByte1((short) c.state());
            blocks[j + 7] = shortToByte2((short) c.state());
            j += 8;
        }
        tagCompound.putByteArray("relcoordsNew", blocks);

        ListTag list = new ListTag();
        for (BlockState state : blockStateTable) {
            CompoundTag tc = new CompoundTag();
            tc.putString("b", Tools.getId(state).toString());
            list.add(tc);
        }
        tagCompound.put("gstates", list);
    }

    @Override
    protected void saveInfo(CompoundTag tagCompound) {
        super.saveInfo(tagCompound);
        CompoundTag info = getOrCreateInfo(tagCompound);
        info.putInt("visMode", shieldRenderingMode.ordinal());
        info.putInt("shieldTexture", shieldTexture.ordinal());
        info.putByte("damageMode", (byte) damageMode.ordinal());

        info.putBoolean("blocklight", blockLight);
        info.putInt("shieldColor", shieldColor);

        writeFiltersToNBT(info);
    }

    private void writeFiltersToNBT(CompoundTag tagCompound) {
        ListTag filterList = new ListTag();
        for (ShieldFilter filter : filters) {
            CompoundTag compound = new CompoundTag();
            filter.writeToNBT(compound);
            filterList.add(compound);
        }
        tagCompound.put("filters", filterList);
    }

    public static final Key<Integer> PARAM_ACTION = new Key<>("action", Type.INTEGER);
    public static final Key<String> PARAM_TYPE = new Key<>("type", Type.STRING);
    public static final Key<String> PARAM_PLAYER = new Key<>("player", Type.STRING);
    public static final Key<Integer> PARAM_SELECTED = new Key<>("selected", Type.INTEGER);
    @ServerCommand
    public static final Command<?> CMD_ADDFILTER = Command.<ShieldProjectorTileEntity>create("shield.addFilter",
            (te, player, params) -> te.addFilter(params.get(PARAM_ACTION), params.get(PARAM_TYPE), params.get(PARAM_PLAYER), params.get(PARAM_SELECTED)));

    @ServerCommand
    public static final Command<?> CMD_DELFILTER = Command.<ShieldProjectorTileEntity>create("shield.delFilter",
            (te, player, params) -> te.delFilter(params.get(PARAM_SELECTED)));
    @ServerCommand
    public static final Command<?> CMD_UPFILTER = Command.<ShieldProjectorTileEntity>create("shield.upFilter",
            (te, player, params) -> te.upFilter(params.get(PARAM_SELECTED)));
    @ServerCommand
    public static final Command<?> CMD_DOWNFILTER = Command.<ShieldProjectorTileEntity>create("shield.downFilter",
            (te, player, params) -> te.downFilter(params.get(PARAM_SELECTED)));

    @ServerCommand(type = ShieldFilter.class, serializer = ShieldFilter.Serializer.class)
    public static final ListCommand<?, ?> CMD_GETFILTERS = ListCommand.<ShieldProjectorTileEntity, ShieldFilter>create("rftoolsbuilder.shield.getFilters",
            (te, player, params) -> te.getFilters(),
            (te, player, params, list) -> GuiShield.storeFiltersForClient(list));

    @Nonnull
    private IPowerInformation createPowerInfo() {
        return new IPowerInformation() {
            @Override
            public long getEnergyDiffPerTick() {
                return shieldActive ? getRfPerTick() : 0;
            }

            @Override
            public String getEnergyUnitName() {
                return "RF";
            }

            @Override
            public boolean isMachineActive() {
                return shieldActive;
            }

            @Override
            public boolean isMachineRunning() {
                return shieldActive;
            }

            @Override
            public String getMachineStatus() {
                return shieldActive ? "active" : "idle";
            }
        };
    }
}
