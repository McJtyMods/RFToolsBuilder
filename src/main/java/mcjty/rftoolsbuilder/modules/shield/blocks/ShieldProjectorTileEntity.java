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
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.FakePlayerGetter;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.OrientationTools;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.shield.*;
import mcjty.rftoolsbuilder.modules.shield.client.GuiShield;
import mcjty.rftoolsbuilder.modules.shield.client.ShieldRenderData;
import mcjty.rftoolsbuilder.modules.shield.filters.*;
import mcjty.rftoolsbuilder.shapes.Shape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.IntStream;

import static mcjty.lib.api.container.DefaultContainerProvider.container;
import static mcjty.lib.container.SlotDefinition.generic;
import static mcjty.lib.container.SlotDefinition.specific;
import static mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingBlock.*;

public class ShieldProjectorTileEntity extends GenericTileEntity implements ISmartWrenchSelector, ITickableTileEntity {

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
    private final LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Shield")
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

    public ShieldProjectorTileEntity(TileEntityType<?> type, int supportedBlocks, int maxEnergy, int rfPerTick) {
        super(type);
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
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        int oldColor = shieldColor;
        ShieldTexture oldTexture = shieldTexture;
        super.onDataPacket(net, packet);
        if (oldColor != shieldColor || oldTexture != shieldTexture) {
            renderData = null;
            // @todo this doesn't help to automatically update the color
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
        }
    }

    //    @Override
//    @Optional.Method(modid = "opencomputers")
//    public String getComponentName() {
//        return COMPONENT_NAME;
//    }
//
//    @Callback(doc = "Get or set the current damage mode for the shield. 'Generic' means normal damage while 'Player' means damage like a player would do", getter = true, setter = true)
//    @Optional.Method(modid = "opencomputers")
//    public Object[] damageMode(Context context, Arguments args) {
//        if(args.count() == 0) {
//            return new Object[] { getDamageMode().getDescription() };
//        } else {
//            String mode = args.checkString(0);
//            return setDamageMode(mode);
//        }
//    }

//    private Object[] setDamageMode(String mode) {
//        DamageTypeMode damageMode = DamageTypeMode.getMode(mode);
//        if (damageMode == null) {
//            throw new IllegalArgumentException("Not a valid mode");
//        }
//        setDamageMode(damageMode);
//        return null;
//    }

//    @Callback(doc = "Get or set the current redstone mode. Values are 'Ignored', 'Off', or 'On'", getter = true, setter = true)
//    @Optional.Method(modid = "opencomputers")
//    public Object[] redstoneMode(Context context, Arguments args) {
//        if(args.count() == 0) {
//            return new Object[] { getRSMode().getDescription() };
//        } else {
//            String mode = args.checkString(0);
//            return setRedstoneMode(mode);
//        }
//    }

//    private Object[] setRedstoneMode(String mode) {
//        RedstoneMode redstoneMode = RedstoneMode.getMode(mode);
//        if (redstoneMode == null) {
//            throw new IllegalArgumentException("Not a valid mode");
//        }
//        setRSMode(redstoneMode);
//        return null;
//    }

//    @Callback(doc = "Get or set the current shield rendering mode. Values are 'Invisible', 'Shield', or 'Solid'", getter = true, setter = true)
//    @Optional.Method(modid = "opencomputers")
//    public Object[] shieldRenderingMode(Context context, Arguments args) {
//        if(args.count() == 0) {
//            return new Object[] { getShieldRenderingMode().getDescription() };
//        } else {
//            String mode = args.checkString(0);
//            return setShieldRenderingMode(mode);
//        }
//    }

//    private Object[] setShieldRenderingMode(String mode) {
//        ShieldRenderingMode renderingMode = ShieldRenderingMode.getMode(mode);
//        if (renderingMode == null) {
//            throw new IllegalArgumentException("Not a valid mode");
//        }
//        setShieldRenderingMode(renderingMode);
//        return null;
//    }

//    @Callback(doc = "Return true if the shield is active", getter = true)
//    @Optional.Method(modid = "opencomputers")
//    public Object[] isShieldActive(Context context, Arguments args) {
//        return new Object[] { isShieldActive() };
//    }
//
//    @Callback(doc = "Return true if the shield is composed (i.e. formed)", getter = true)
//    @Optional.Method(modid = "opencomputers")
//    public Object[] isShieldComposed(Context context, Arguments args) {
//        return new Object[] { isShieldComposed() };
//    }
//
//    @Callback(doc = "Form the shield (compose it)")
//    @Optional.Method(modid = "opencomputers")
//    public Object[] composeShield(Context context, Arguments args) {
//        return composeShieldComp(false);
//    }
//
//    @Callback(doc = "Form the shield (compose it). This version works in disconnected mode (template blocks will connect on corners too)")
//    @Optional.Method(modid = "opencomputers")
//    public Object[] composeShieldDsc(Context context, Arguments args) {
//        return composeShieldComp(true);
//    }

    private Object[] composeShieldComp(boolean ctrl) {
        boolean done = false;
        if (!isShieldComposed()) {
            composeShield(ctrl);
            done = true;
        }
        return new Object[]{done};
    }

//    @Callback(doc = "Break down the shield (decompose it)")
//    @Optional.Method(modid = "opencomputers")
//    public Object[] decomposeShield(Context context, Arguments args) {
//        return decomposeShieldComp();
//    }

    private Object[] decomposeShieldComp() {
        boolean done = false;
        if (isShieldComposed()) {
            decomposeShield();
            done = true;
        }
        return new Object[]{done};
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

//    @Override
//    public int[] getSlotsForFace(Direction side) {
//        return new int[] { ShieldContainer.SLOT_SHARD };
//    }
//
//    @Override
//    public boolean isItemValidForSlot(int index, ItemStack stack) {
//        if (index == ShieldContainer.SLOT_SHAPE && stack.getItem() != BuilderSetup.shapeCardItem) {
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public boolean canInsertItem(int index, ItemStack itemStackIn, Direction direction) {
//        return index == ShieldContainer.SLOT_SHARD && itemStackIn.getItem() == ModItems.dimensionalShardItem;
//    }

    @Nonnull
    private BlockState getStateFromItem(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof BlockItem) {
            BlockItem blockItem = (BlockItem) item;
            PlayerEntity player = fakePlayer.get();
            player.setItemInHand(Hand.MAIN_HAND, stack);
            BlockRayTraceResult result = new BlockRayTraceResult(new Vector3d(.5, 0, .5), Direction.UP, worldPosition, false);
            BlockItemUseContext context = new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, result));
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
        Optional<BlockState> map = getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
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
            source = DamageSource.GENERIC;
        } else {
            rf = ShieldConfiguration.rfDamagePlayer.get();
            ServerPlayerEntity killer = fakePlayer.get();
            killer.setLevel(level);
            killer.setPos(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
            new FakePlayerConnection(killer);
            ItemStack shards = items.getStackInSlot(SLOT_SHARD);
            if (!shards.isEmpty() && shards.getCount() >= ShieldConfiguration.shardsPerLootingKill.get()) {
                items.extractItem(SLOT_SHARD, ShieldConfiguration.shardsPerLootingKill.get(), false);
                if (lootingSword.isEmpty()) {
                    lootingSword = createEnchantedItem(Items.DIAMOND_SWORD, Enchantments.MOB_LOOTING, ShieldConfiguration.lootingKillBonus.get());
                }
                lootingSword.setDamageValue(0);
                killer.setItemInHand(Hand.MAIN_HAND, lootingSword);
            } else {
                killer.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            }
            source = DamageSource.playerAttack(killer);
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
    public void tick() {
        if (level == null)
            return;

        if (!level.isClientSide) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
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
            setChanged();
        }
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
            if (p.getY() >= 0 && p.getY() < getLevel().getMaxBuildHeight()) {
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
    public void selectBlock(PlayerEntity player, BlockPos pos) {
        if (!shieldComposed) {
            Logging.message(player, TextFormatting.YELLOW + "Shield is not composed. Nothing happens!");
            return;
        }

        float squaredDistance = (float) getBlockPos().distSqr(pos);
        if (squaredDistance > ShieldConfiguration.maxDisjointShieldDistance.get() * ShieldConfiguration.maxDisjointShieldDistance.get()) {
            Logging.message(player, TextFormatting.YELLOW + "This template is too far to connect to the shield!");
            return;
        }

        int xCoord = getBlockPos().getX();
        int yCoord = getBlockPos().getY();
        int zCoord = getBlockPos().getZ();

        Block origBlock = getLevel().getBlockState(pos).getBlock();
        if (origBlock instanceof ShieldTemplateBlock) {
            if (isShapedShield()) {
                Logging.message(player, TextFormatting.YELLOW + "You cannot add template blocks to a shaped shield (using a shape card)!");
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
            getLevel().setBlock(pos, templateState, 2);
        } else {
            Logging.message(player, TextFormatting.YELLOW + "The selected shield can't do anything with this block!");
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
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (RelCoordinateShield c : shieldBlocks) {
            if (Blocks.AIR.equals(shielding.getBlock())) {
                pos.set(xCoord + c.getDx(), yCoord + c.getDy(), zCoord + c.getDz());
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
        BlockPos pp = new BlockPos(xCoord + c.getDx(), yCoord + c.getDy(), zCoord + c.getDz());
        BlockState oldState = getLevel().getBlockState(pp);
        if (!(oldState.getBlock() instanceof ShieldingBlock)) {
            if ((!oldState.getMaterial().isReplaceable()) && !(oldState.getBlock() instanceof ShieldTemplateBlock)) {
                return;
            }
        }

        // To force an update set it to air first
        level.setBlockAndUpdate(pp, Blocks.AIR.defaultBlockState());
        level.setBlock(pp, shielding, Constants.BlockFlags.BLOCK_UPDATE);

        TileEntity te = getLevel().getBlockEntity(pp);
        if (te instanceof ShieldingTileEntity) {
            ShieldingTileEntity shieldingTE = (ShieldingTileEntity) te;
            if (c.getState() != -1) {
                BlockState state = blockStateTable.get(c.getState());
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
        BlockPos.Mutable pp = new BlockPos.Mutable();
        for (RelCoordinate c : shieldBlocks) {
            int cx = xCoord + c.getDx();
            int cy = yCoord + c.getDy();
            int cz = zCoord + c.getDz();
            pp.set(cx, cy, cz);
            Block block = getLevel().getBlockState(pp).getBlock();
            if (getLevel().isEmptyBlock(pp) || block instanceof ShieldingBlock) {
                getLevel().setBlock(new BlockPos(pp), templateState, 2);
            } else if (templateState.getMaterial() != Material.AIR) {
                if (!isShapedShield()) {
                    // No room, just spawn the block
                    InventoryHelper.dropItemStack(getLevel(), cx, cy, cz, templateState.getBlock().getCloneItemStack(getLevel(), new BlockPos(cx, cy, cz), templateState));
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
            if (pp.getY() >= 0 && pp.getY() < getLevel().getMaxBuildHeight()) {
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
        BlockPos.Mutable c = new BlockPos.Mutable();
        for (int xx = x - 1; xx <= x + 1; xx++) {
            for (int yy = y - 1; yy <= y + 1; yy++) {
                for (int zz = z - 1; zz <= z + 1; zz++) {
                    if (xx != x || yy != y || zz != z) {
                        if (yy >= 0 && yy < getLevel().getMaxBuildHeight()) {
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
    public void readClientDataFromNBT(CompoundNBT tagCompound) {
        powerLevel = tagCompound.getByte("powered");
        shieldComposed = tagCompound.getBoolean("composed");
        shieldActive = tagCompound.getBoolean("active");
        powerTimeout = tagCompound.getInt("powerTimeout");
        if (tagCompound.contains("templateColor")) {
            int templateColor = tagCompound.getInt("templateColor");
            ShieldTemplateBlock.TemplateColor color = ShieldTemplateBlock.TemplateColor.values()[templateColor];
            switch (color) {
                case BLUE:
                    templateState = ShieldModule.TEMPLATE_BLUE.get().defaultBlockState();
                    break;
                case RED:
                    templateState = ShieldModule.TEMPLATE_RED.get().defaultBlockState();
                    break;
                case GREEN:
                    templateState = ShieldModule.TEMPLATE_GREEN.get().defaultBlockState();
                    break;
                case YELLOW:
                    templateState = ShieldModule.TEMPLATE_YELLOW.get().defaultBlockState();
                    break;
            }
        } else {
            templateState = Blocks.AIR.defaultBlockState();
        }

        readEnergyCap(tagCompound);

        if (tagCompound.contains("Info")) {
            CompoundNBT info = tagCompound.getCompound("Info");
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
    }

    @Override
    public void writeClientDataToNBT(CompoundNBT tagCompound) {
        tagCompound.putByte("powered", (byte) powerLevel);
        tagCompound.putBoolean("composed", shieldComposed);
        tagCompound.putBoolean("active", shieldActive);
        tagCompound.putInt("powerTimeout", powerTimeout);
        if (templateState.getMaterial() != Material.AIR) {
            tagCompound.putInt("templateColor", ((ShieldTemplateBlock) templateState.getBlock()).getColor().ordinal());
        }

        CompoundNBT info = getOrCreateInfo(tagCompound);
        info.putInt("visMode", shieldRenderingMode.ordinal());
        info.putInt("shieldTexture", shieldTexture.ordinal());
        info.putByte("rsMode", (byte) rsMode.ordinal());
        info.putByte("damageMode", (byte) damageMode.ordinal());

        info.putBoolean("blocklight", blockLight);
        info.putInt("shieldColor", shieldColor);

        writeFiltersToNBT(info);
        writeEnergyCap(tagCompound);
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        shieldComposed = tagCompound.getBoolean("composed");
        shieldActive = tagCompound.getBoolean("active");
        powerTimeout = tagCompound.getInt("powerTimeout");
        if (!isShapedShield()) {
            if (tagCompound.contains("templateColor")) {
                int templateColor = tagCompound.getInt("templateColor");
                ShieldTemplateBlock.TemplateColor color = ShieldTemplateBlock.TemplateColor.values()[templateColor];
                switch (color) {
                    case BLUE:
                        templateState = ShieldModule.TEMPLATE_BLUE.get().defaultBlockState();
                        break;
                    case RED:
                        templateState = ShieldModule.TEMPLATE_RED.get().defaultBlockState();
                        break;
                    case GREEN:
                        templateState = ShieldModule.TEMPLATE_GREEN.get().defaultBlockState();
                        break;
                    case YELLOW:
                        templateState = ShieldModule.TEMPLATE_YELLOW.get().defaultBlockState();
                        break;
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

            ListNBT list = tagCompound.getList("gstates", Constants.NBT.TAG_COMPOUND);
            for (INBT inbt : list) {
                CompoundNBT tc = (CompoundNBT) inbt;
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
    protected void readInfo(CompoundNBT tagCompound) {
        super.readInfo(tagCompound);
        if (tagCompound.contains("Info")) {
            CompoundNBT info = tagCompound.getCompound("Info");
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

    private void readFiltersFromNBT(CompoundNBT tagCompound) {
        filters.clear();
        ListNBT filterList = tagCompound.getList("filters", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < filterList.size(); i++) {
            CompoundNBT compound = filterList.getCompound(i);
            filters.add(AbstractShieldFilter.createFilter(compound));
        }
    }

    @Nonnull
    @Override
    public CompoundNBT save(@Nonnull CompoundNBT tagCompound) {
        super.save(tagCompound);
        tagCompound.putBoolean("composed", shieldComposed);
        tagCompound.putBoolean("active", shieldActive);
        tagCompound.putInt("powerTimeout", powerTimeout);
        if (templateState.getMaterial() != Material.AIR) {
            tagCompound.putInt("templateColor", ((ShieldTemplateBlock) templateState.getBlock()).getColor().ordinal());
        }
        byte[] blocks = new byte[shieldBlocks.size() * 8];
        int j = 0;
        for (RelCoordinateShield c : shieldBlocks) {
            blocks[j + 0] = shortToByte1((short) c.getDx());
            blocks[j + 1] = shortToByte2((short) c.getDx());
            blocks[j + 2] = shortToByte1((short) c.getDy());
            blocks[j + 3] = shortToByte2((short) c.getDy());
            blocks[j + 4] = shortToByte1((short) c.getDz());
            blocks[j + 5] = shortToByte2((short) c.getDz());
            blocks[j + 6] = shortToByte1((short) c.getState());
            blocks[j + 7] = shortToByte2((short) c.getState());
            j += 8;
        }
        tagCompound.putByteArray("relcoordsNew", blocks);

        ListNBT list = new ListNBT();
        for (BlockState state : blockStateTable) {
            CompoundNBT tc = new CompoundNBT();
            tc.putString("b", state.getBlock().getRegistryName().toString());
            list.add(tc);
        }
        tagCompound.put("gstates", list);
        return tagCompound;
    }

    @Override
    protected void writeInfo(CompoundNBT tagCompound) {
        super.writeInfo(tagCompound);
        CompoundNBT info = getOrCreateInfo(tagCompound);
        info.putInt("visMode", shieldRenderingMode.ordinal());
        info.putInt("shieldTexture", shieldTexture.ordinal());
        info.putByte("damageMode", (byte) damageMode.ordinal());

        info.putBoolean("blocklight", blockLight);
        info.putInt("shieldColor", shieldColor);

        writeFiltersToNBT(info);
    }

    private void writeFiltersToNBT(CompoundNBT tagCompound) {
        ListNBT filterList = new ListNBT();
        for (ShieldFilter filter : filters) {
            CompoundNBT compound = new CompoundNBT();
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

            @Nullable
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

            @Nullable
            @Override
            public String getMachineStatus() {
                return shieldActive ? "active" : "idle";
            }
        };
    }
}
