package mcjty.rftoolsbuilder.modules.shield.blocks;

import com.mojang.authlib.GameProfile;
import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.information.CapabilityPowerInformation;
import mcjty.lib.api.information.IPowerInformation;
import mcjty.lib.api.infusable.CapabilityInfusable;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.api.smartwrench.ISmartWrenchSelector;
import mcjty.lib.bindings.DefaultValue;
import mcjty.lib.bindings.IValue;
import mcjty.lib.container.*;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.*;
import mcjty.rftoolsbase.modules.various.VariousSetup;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingBlock.*;

public class ShieldProjectorTileEntity extends GenericTileEntity implements ISmartWrenchSelector, ITickableTileEntity { // @todo }, IPeripheral {

    public static final String CMD_ADDFILTER = "shield.addFilter";
    public static final Key<Integer> PARAM_ACTION = new Key<>("action", Type.INTEGER);
    public static final Key<String> PARAM_TYPE = new Key<>("type", Type.STRING);
    public static final Key<String> PARAM_PLAYER = new Key<>("player", Type.STRING);
    public static final Key<Integer> PARAM_SELECTED = new Key<>("selected", Type.INTEGER);

    public static final String CMD_DELFILTER = "shield.delFilter";
    public static final String CMD_UPFILTER = "shield.upFilter";
    public static final String CMD_DOWNFILTER = "shield.downFilter";

    public static final String CMD_GETFILTERS = "getFilters";
    public static final String CLIENTCMD_GETFILTERS = "getFilters";

    public static final String COMPONENT_NAME = "shield_projector";

    public static final Key<Integer> VALUE_SHIELDVISMODE = new Key<>("shieldVisMode", Type.INTEGER);
    public static final Key<Integer> VALUE_SHIELDTEXTURE = new Key<>("shieldTexture", Type.INTEGER);
    public static final Key<Integer> VALUE_DAMAGEMODE = new Key<>("damageMode", Type.INTEGER);
    public static final Key<Integer> VALUE_COLOR = new Key<>("color", Type.INTEGER);
    public static final Key<Boolean> VALUE_LIGHT = new Key<>("light", Type.BOOLEAN);

    @Override
    public IValue<?>[] getValues() {
        return new IValue[]{
                new DefaultValue<>(VALUE_RSMODE, this::getRSModeInt, this::setRSModeInt),
                new DefaultValue<>(VALUE_SHIELDVISMODE, () -> this.getShieldRenderingMode().ordinal(), (value) -> this.setShieldRenderingMode(ShieldRenderingMode.values()[value])),
                new DefaultValue<>(VALUE_SHIELDTEXTURE, () -> this.getShieldTexture().ordinal(), (value) -> this.setShieldTexture(ShieldTexture.values()[value])),
                new DefaultValue<>(VALUE_DAMAGEMODE, () -> this.getDamageMode().ordinal(), (value) -> this.setDamageMode(DamageTypeMode.values()[value])),
                new DefaultValue<>(VALUE_COLOR, this::getShieldColor, this::setShieldColor),
                new DefaultValue<>(VALUE_LIGHT, this::isBlockLight, this::setBlockLight),
        };
    }

    // Client side
    private ShieldRenderData renderData;

    private DamageTypeMode damageMode = DamageTypeMode.DAMAGETYPE_GENERIC;

    // If true the shield is currently made.
    private boolean shieldComposed = false;
    // The state for the template blocks that were used.
    private BlockState templateState = Blocks.AIR.getDefaultState();
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

    private List<RelCoordinateShield> shieldBlocks = new ArrayList<>();
    private List<BlockState> blockStateTable = new ArrayList<>();

    public static final int SLOT_BUFFER = 0;
    public static final int SLOT_SHAPE = 1;
    public static final int SLOT_SHARD = 2;
    public static final int BUFFER_SIZE = 3;
    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(BUFFER_SIZE) {
        @Override
        protected void setup() {
            slot(SlotDefinition.input(), CONTAINER_CONTAINER, SLOT_BUFFER, 26, 142);
            slot(SlotDefinition.specific(s -> s.getItem() instanceof ShapeCardItem), CONTAINER_CONTAINER, SLOT_SHAPE, 26, 200);
            slot(SlotDefinition.specific(s -> s.getItem() == VariousSetup.DIMENSIONALSHARD.get()), CONTAINER_CONTAINER, SLOT_SHARD, 229, 118);
            playerSlots(85, 142);
        }
    };

    private NoDirectionItemHander items = createItemHandler();
    private LazyOptional<NoDirectionItemHander> itemHandler = LazyOptional.of(() -> items);
    private LazyOptional<AutomationFilterItemHander> automationItemHandler = LazyOptional.of(() -> new AutomationFilterItemHander(items));
    private LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Screen")
            .containerSupplier((windowId,player) -> new GenericContainer(ShieldSetup.CONTAINER_SHIELD.get(), windowId, CONTAINER_FACTORY, getPos(), ShieldProjectorTileEntity.this))
            .itemHandler(itemHandler));

    private LazyOptional<GenericEnergyStorage> energyHandler = LazyOptional.of(() -> new GenericEnergyStorage(this, true, getConfigMaxEnergy(), getConfigRfPerTick()));
    private LazyOptional<IInfusable> infusableHandler = LazyOptional.of(() -> new DefaultInfusable(ShieldProjectorTileEntity.this));
    private LazyOptional<IPowerInformation> powerInfoHandler = LazyOptional.of(this::createPowerInfo);

    private final int maxEnergy;
    private final int rfPerTick;

    public ShieldProjectorTileEntity(TileEntityType<?> type, int supportedBlocks, int maxEnergy, int rfPerTick) {
        super(type);
        this.supportedBlocks = supportedBlocks;
        this.maxEnergy = maxEnergy;
        this.rfPerTick = rfPerTick;
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
            BlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
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

    private Object[] setDamageMode(String mode) {
        DamageTypeMode damageMode = DamageTypeMode.getMode(mode);
        if (damageMode == null) {
            throw new IllegalArgumentException("Not a valid mode");
        }
        setDamageMode(damageMode);
        return null;
    }

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

    private Object[] setRedstoneMode(String mode) {
        RedstoneMode redstoneMode = RedstoneMode.getMode(mode);
        if (redstoneMode == null) {
            throw new IllegalArgumentException("Not a valid mode");
        }
        setRSMode(redstoneMode);
        return null;
    }

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

    private Object[] setShieldRenderingMode(String mode) {
        ShieldRenderingMode renderingMode = ShieldRenderingMode.getMode(mode);
        if (renderingMode == null) {
            throw new IllegalArgumentException("Not a valid mode");
        }
        setShieldRenderingMode(renderingMode);
        return null;
    }

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
        return new Object[] { done };
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
        return new Object[] { done };
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
        markDirtyClient();
    }

    public int getShieldColor() {
        return shieldColor;
    }

    public void setShieldColor(int shieldColor) {
        this.shieldColor = shieldColor;
        updateTimeout = 10;
        markDirtyClient();
    }

    private void delFilter(int selected) {
        filters.remove(selected);
        updateTimeout = 10;
        markDirtyClient();
    }

    private void upFilter(int selected) {
        ShieldFilter filter1 = filters.get(selected-1);
        ShieldFilter filter2 = filters.get(selected);
        filters.set(selected - 1, filter2);
        filters.set(selected, filter1);
        markDirtyClient();
    }

    private void downFilter(int selected) {
        ShieldFilter filter1 = filters.get(selected);
        ShieldFilter filter2 = filters.get(selected+1);
        filters.set(selected, filter2);
        filters.set(selected + 1, filter1);
        markDirtyClient();
    }

    private void addFilter(int action, String type, String player, int selected) {
        ShieldFilter filter = AbstractShieldFilter.createFilter(type);
        filter.setAction(action);
        if (filter instanceof PlayerFilter) {
            ((PlayerFilter)filter).setName(player);
        }
        if (selected == -1) {
            filters.add(filter);
        } else {
            filters.add(selected, filter);
        }
        updateTimeout = 10;
        markDirtyClient();
    }

    public DamageTypeMode getDamageMode() {
        return damageMode;
    }

    public void setDamageMode(DamageTypeMode damageMode) {
        this.damageMode = damageMode;
        markDirtyClient();
    }

    public ShieldRenderingMode getShieldRenderingMode() {
        return shieldRenderingMode;
    }

    public void setShieldRenderingMode(ShieldRenderingMode shieldRenderingMode) {
        this.shieldRenderingMode = shieldRenderingMode;
        updateTimeout = 10;
        markDirtyClient();
    }

    public ShieldTexture getShieldTexture() {
        return shieldTexture;
    }

    public void setShieldTexture(ShieldTexture shieldTexture) {
        this.shieldTexture = shieldTexture;
        updateTimeout = 10;
        markDirtyClient();
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

    private FakePlayer fakePlayer = null;
    private FakePlayer getFakePlayer() {
        if (fakePlayer == null) {
            fakePlayer = FakePlayerFactory.get((ServerWorld) world,  new GameProfile(UUID.nameUUIDFromBytes("rftools_shield".getBytes()), "rftools_builder"));
        }
        fakePlayer.setWorld(world);
        fakePlayer.setPosition(pos.getX(), pos.getY(), pos.getZ());
        return fakePlayer;
    }


    @Nonnull
    private BlockState getStateFromItem(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof BlockItem) {
            BlockItem blockItem = (BlockItem) item;
            FakePlayer player = getFakePlayer();
            player.setHeldItem(Hand.MAIN_HAND, stack);
            BlockRayTraceResult result = new BlockRayTraceResult(new Vec3d(.5, 0, .5), Direction.UP, pos, false);
            BlockItemUseContext context = new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, result));
            BlockState stateForPlacement = blockItem.getBlock().getStateForPlacement(context);
            return stateForPlacement == null ? blockItem.getBlock().getDefaultState() : stateForPlacement;
        } else {
            return Blocks.AIR.getDefaultState();
        }
    }

    @Nullable
    private BlockState calculateMimic() {
        if (!ShieldRenderingMode.MIMIC.equals(shieldRenderingMode)) {
            return null;
        }
        LazyOptional<BlockState> map = getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
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
            return Blocks.AIR.getDefaultState();
        }

        ShieldRenderingMode render = shieldRenderingMode;
        if (!ShieldConfiguration.allowInvisibleShield.get() && ShieldRenderingMode.INVISIBLE.equals(shieldRenderingMode)) {
            render = ShieldRenderingMode.SOLID;
        }
        if (mimic != null) {
            render = ShieldRenderingMode.MIMIC;
        }

        BlockState shielding;
        shielding = getShieldingBlock(render, mimic).getDefaultState();
        shielding = shielding.with(FLAG_OPAQUE, !blockLight);
        shielding = shielding.with(RENDER_MODE, render);
        shielding = calculateShieldCollisionData(shielding);
        shielding = calculateDamageBits(shielding);
        return shielding;
    }

    private Block getShieldingBlock(ShieldRenderingMode render, BlockState mimic) {
        if (mimic != null) {
            switch (mimic.getBlock().getRenderLayer()) {
                case SOLID:
                    return ShieldSetup.SHIELDING_SOLID.get();
                case CUTOUT_MIPPED:
                    return ShieldSetup.SHIELDING_CUTOUT.get();   // @todo check?
                case CUTOUT:
                    return ShieldSetup.SHIELDING_CUTOUT.get();
                case TRANSLUCENT:
                    return ShieldSetup.SHIELDING_TRANSLUCENT.get();
            }
        } else {
            if (render.isTranslucent()) {
                return ShieldSetup.SHIELDING_TRANSLUCENT.get();
            } else {
                return ShieldSetup.SHIELDING_SOLID.get();
            }
        }
        return ShieldSetup.SHIELDING_SOLID.get();
    }

    private BlockState calculateDamageBits(BlockState shielding) {
        for (ShieldFilter filter : filters) {
            if ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0) {
                if (ItemFilter.ITEM.equals(filter.getFilterName())) {
                    shielding = shielding.with(DAMAGE_ITEMS, true);
                } else if (AnimalFilter.ANIMAL.equals(filter.getFilterName())) {
                    shielding = shielding.with(DAMAGE_PASSIVE, true);
                } else if (HostileFilter.HOSTILE.equals(filter.getFilterName())) {
                    shielding = shielding.with(DAMAGE_HOSTILE, true);
                } else if (PlayerFilter.PLAYER.equals(filter.getFilterName())) {
                    shielding = shielding.with(DAMAGE_PLAYERS, true);
                } else if (DefaultFilter.DEFAULT.equals(filter.getFilterName())) {
                    shielding = shielding.with(DAMAGE_ITEMS, true).with(DAMAGE_PASSIVE, true).with(DAMAGE_HOSTILE, true).with(DAMAGE_PLAYERS, true);
                }
            }
        }
        return shielding;
    }

    private BlockState calculateShieldCollisionData(BlockState shielding) {
        for (ShieldFilter filter : filters) {
            if ((filter.getAction() & ShieldFilter.ACTION_SOLID) != 0) {
                if (ItemFilter.ITEM.equals(filter.getFilterName())) {
                    shielding = shielding.with(BLOCKED_ITEMS, true);
                } else if (AnimalFilter.ANIMAL.equals(filter.getFilterName())) {
                    shielding = shielding.with(BLOCKED_PASSIVE, true);
                } else if (HostileFilter.HOSTILE.equals(filter.getFilterName())) {
                    shielding = shielding.with(BLOCKED_HOSTILE, true);
                } else if (PlayerFilter.PLAYER.equals(filter.getFilterName())) {
                    shielding = shielding.with(BLOCKED_PLAYERS, true);
                } else if (DefaultFilter.DEFAULT.equals(filter.getFilterName())) {
                    shielding = shielding.with(BLOCKED_ITEMS, true).with(BLOCKED_PASSIVE, true).with(BLOCKED_HOSTILE, true).with(BLOCKED_PLAYERS, true);
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

    private FakePlayer killer = null;
    private ItemStack lootingSword = ItemStack.EMPTY;

    public void applyDamageToEntity(Entity entity) {
        energyHandler.ifPresent(h -> {
            DamageSource source;
            int rf;
            if (DamageTypeMode.DAMAGETYPE_GENERIC.equals(damageMode)) {
                rf = ShieldConfiguration.rfDamage.get();
                source = DamageSource.GENERIC;
            } else {
                rf = ShieldConfiguration.rfDamagePlayer.get();
                if (killer == null) {
                    killer = FakePlayerFactory.get(WorldTools.getOverworld(), new GameProfile(UUID.nameUUIDFromBytes("rftools_shield".getBytes()), "rftools_shield"));
                }
                killer.setWorld(world);
                killer.setPosition(pos.getX(), pos.getY(), pos.getZ());
                FakePlayer fakePlayer = killer;
                ItemStack shards = items.getStackInSlot(SLOT_SHARD);
                if (!shards.isEmpty() && shards.getCount() >= ShieldConfiguration.shardsPerLootingKill.get()) {
                    items.extractItem(SLOT_SHARD, ShieldConfiguration.shardsPerLootingKill.get(), false);
                    if (lootingSword.isEmpty()) {
                        lootingSword = createEnchantedItem(Items.DIAMOND_SWORD, Enchantments.LOOTING, ShieldConfiguration.lootingKillBonus.get());
                    }
                    lootingSword.setDamage(0);
                    fakePlayer.setHeldItem(Hand.MAIN_HAND, lootingSword);
                } else {
                    fakePlayer.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
                }
                source = DamageSource.causePlayerDamage(fakePlayer);
            }

            float factor = infusableHandler.map(inf -> inf.getInfusedFactor()).orElse(0.0f);
            rf = (int) (rf * costFactor * (4.0f - factor) / 4.0f);
            if (h.getEnergyStored() < rf) {
                // Not enough RF to do damage.
                return;
            }
            h.consumeEnergy(rf);

            float damage = (float) (double) ShieldConfiguration.damage.get();
            damage *= damageFactor;
            damage = damage * (1.0f + factor / 2.0f);

            entity.attackEntityFrom(source, damage);
        });
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
        if (!world.isRemote) {
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

        energyHandler.ifPresent(h -> {
            boolean checkPower = false;
            if (powerTimeout > 0) {
                powerTimeout--;
                markDirty();
                if (powerTimeout > 0) {
                    return;
                } else {
                    checkPower = true;
                }
            }

            boolean needsUpdate = false;

            int rf = getRfPerTick();

            if (rf > 0) {
                if (h.getEnergyStored() < rf) {
                    powerTimeout = 100;     // Wait 5 seconds before trying again.
                    needsUpdate = true;
                } else {
                    if (checkPower) {
                        needsUpdate = true;
                    }
                    h.consumeEnergy(rf);
                }
            }

            boolean newShieldActive = isMachineEnabled();
            if (newShieldActive != shieldActive) {
                needsUpdate = true;
                shieldActive = newShieldActive;
            }

            if (needsUpdate) {
                updateShield();
                markDirty();
            }
        });
    }

    private int getRfPerTick() {
        int rf = calculateRfPerTick();
        float factor = infusableHandler.map(inf -> inf.getInfusedFactor()).orElse(0.0f);
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
            templateState = Blocks.AIR.getDefaultState();

            ItemStack shapeItem = items.getStackInSlot(SLOT_SHAPE);
            Shape shape = ShapeCardItem.getShape(shapeItem);
            boolean solid = ShapeCardItem.isSolid(shapeItem);
            BlockPos dimension = ShapeCardItem.getClampedDimension(shapeItem, ShieldConfiguration.maxShieldDimension.get());
            BlockPos offset = ShapeCardItem.getClampedOffset(shapeItem, ShieldConfiguration.maxShieldOffset.get());
            Map<BlockPos, BlockState> col = new HashMap<>();
            ShapeCardItem.composeFormula(shapeItem, shape.getFormulaFactory().get(), getWorld(), getPos(), dimension, offset, col, supportedBlocks, solid, false, null);
            coordinates = col;
        } else {
            if(!findTemplateState()) return;

            Map<BlockPos, BlockState> col = new HashMap<>();
            findTemplateBlocks(col, templateState, ctrl, getPos());
            coordinates = col;
        }

        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
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
            getWorld().setBlockState(c, Blocks.AIR.getDefaultState());
        }

        shieldComposed = true;
        updateShield();
    }

    private boolean isShapedShield() {
        return !items.getStackInSlot(SLOT_SHAPE).isEmpty();
    }

    private boolean findTemplateState() {
        for (Direction dir : OrientationTools.DIRECTION_VALUES) {
            BlockPos p = getPos().offset(dir);
            if (p.getY() >= 0 && p.getY() < getWorld().getHeight()) {
                BlockState state = getWorld().getBlockState(p);
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

        float squaredDistance = (float) getPos().distanceSq(pos);
        if (squaredDistance > ShieldConfiguration.maxDisjointShieldDistance.get() * ShieldConfiguration.maxDisjointShieldDistance.get()) {
            Logging.message(player, TextFormatting.YELLOW + "This template is too far to connect to the shield!");
            return;
        }

        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();

        Block origBlock = getWorld().getBlockState(pos).getBlock();
        if (origBlock instanceof ShieldTemplateBlock) {
            if (isShapedShield()) {
                Logging.message(player, TextFormatting.YELLOW + "You cannot add template blocks to a shaped shield (using a shape card)!");
                return;
            }
            Map<BlockPos, BlockState> templateBlocks = new HashMap<>();
            BlockState state = getWorld().getBlockState(pos);
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
            shieldBlocks.remove(new RelCoordinate(pos.getX() - xCoord, pos.getY() - yCoord, pos.getZ() - zCoord));
            getWorld().setBlockState(pos, templateState, 2);
        } else {
            Logging.message(player, TextFormatting.YELLOW + "The selected shield can't do anything with this block!");
            return;
        }
        markDirtyClient();
    }

    /**
     * Update all shield blocks. Possibly creating the shield.
     */
    private void updateShield() {
        BlockState mimic = calculateMimic();
        BlockState shielding = calculateShieldBlock(mimic, blockLight);
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (RelCoordinateShield c : shieldBlocks) {
            if (Blocks.AIR.equals(shielding.getBlock())) {
                pos.setPos(xCoord + c.getDx(), yCoord + c.getDy(), zCoord + c.getDz());
                BlockState oldState = getWorld().getBlockState(pos);
                if (oldState.getBlock() instanceof ShieldingBlock) {
                    getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
                }
            } else {
                updateShieldBlock(mimic, shielding, c);
            }
        }
        markDirtyClient();
    }

    private void updateShieldBlock(BlockState mimic, BlockState shielding, RelCoordinateShield c) {
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        BlockPos pp = new BlockPos(xCoord + c.getDx(), yCoord + c.getDy(), zCoord + c.getDz());
        BlockState oldState = getWorld().getBlockState(pp);
        if (!(oldState.getBlock() instanceof ShieldingBlock)) {
            if ((!oldState.getMaterial().isReplaceable()) && !(oldState.getBlock() instanceof ShieldTemplateBlock)) {
                return;
            }
        }

        // To force an update set it to air first
        world.setBlockState(pp, Blocks.AIR.getDefaultState());
        world.setBlockState(pp, shielding, Constants.BlockFlags.BLOCK_UPDATE);

        TileEntity te = getWorld().getTileEntity(pp);
        if (te instanceof ShieldingTileEntity) {
            ShieldingTileEntity shieldingTE = (ShieldingTileEntity) te;
            if (c.getState() != -1) {
                BlockState state = blockStateTable.get(c.getState());
                shieldingTE.setMimic(state);
            } else {
                shieldingTE.setMimic(mimic);
            }
            shieldingTE.setShieldProjector(pos);
        }
    }

    public void decomposeShield() {
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        BlockPos.MutableBlockPos pp = new BlockPos.MutableBlockPos();
        for (RelCoordinate c : shieldBlocks) {
            int cx = xCoord + c.getDx();
            int cy = yCoord + c.getDy();
            int cz = zCoord + c.getDz();
            pp.setPos(cx, cy, cz);
            Block block = getWorld().getBlockState(pp).getBlock();
            if (getWorld().isAirBlock(pp) || block instanceof ShieldingBlock) {
                getWorld().setBlockState(new BlockPos(pp), templateState, 2);
            } else if (templateState.getMaterial() != Material.AIR){
                if (!isShapedShield()) {
                    // No room, just spawn the block
                    BlockTools.spawnItemStack(getWorld(), cx, cy, cz, templateState.getBlock().getItem(getWorld(), new BlockPos(cx, cy, cz), templateState));
                }
            }
        }
        shieldComposed = false;
        shieldActive = false;
        shieldBlocks.clear();
        blockStateTable.clear();
        markDirtyClient();
    }

    /**
     * Find all template blocks recursively.
     * @param coordinateSet the set with coordinates to update during the search
     * @param templateState the state for the shield template block we support
     * @param ctrl if true also scan for blocks in corners
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
            BlockPos pp = coordinate.offset(dir);
            if (pp.getY() >= 0 && pp.getY() < getWorld().getHeight()) {
                if (!coordinateSet.containsKey(pp)) {
                    BlockState state = getWorld().getBlockState(pp);
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
        for (int xx = x-1 ; xx <= x+1 ; xx++) {
            for (int yy = y-1 ; yy <= y+1 ; yy++) {
                for (int zz = z-1 ; zz <= z+1 ; zz++) {
                    if (xx != x || yy != y || zz != z) {
                        if (yy >= 0 && yy < getWorld().getHeight()) {
                            c.setPos(xx, yy, zz);
                            if (!coordinateSet.containsKey(c)) {
                                BlockState state = getWorld().getBlockState(c);
                                if (state == templateState) {
                                    if (!todo.contains(c)) {
                                        todo.addLast(c.toImmutable());
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
                    templateState = ShieldSetup.TEMPLATE_BLUE.get().getDefaultState();
                    break;
                case RED:
                    templateState = ShieldSetup.TEMPLATE_RED.get().getDefaultState();
                    break;
                case GREEN:
                    templateState = ShieldSetup.TEMPLATE_GREEN.get().getDefaultState();
                    break;
                case YELLOW:
                    templateState = ShieldSetup.TEMPLATE_YELLOW.get().getDefaultState();
                    break;
            }
        } else {
            templateState = Blocks.AIR.getDefaultState();
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
            tagCompound.putInt("templateColor", ((ShieldTemplateBlock)templateState.getBlock()).getColor().ordinal());
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
                        templateState = ShieldSetup.TEMPLATE_BLUE.get().getDefaultState();
                        break;
                    case RED:
                        templateState = ShieldSetup.TEMPLATE_RED.get().getDefaultState();
                        break;
                    case GREEN:
                        templateState = ShieldSetup.TEMPLATE_GREEN.get().getDefaultState();
                        break;
                    case YELLOW:
                        templateState = ShieldSetup.TEMPLATE_YELLOW.get().getDefaultState();
                        break;
                }
            } else {
                templateState = Blocks.AIR.getDefaultState();
            }
        } else {
            templateState = Blocks.AIR.getDefaultState();
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
                BlockState state = block.getDefaultState(); // @todo 1.14 getStateFromMeta(m);
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
        for (int i = 0 ; i < filterList.size() ; i++) {
            CompoundNBT compound = filterList.getCompound(i);
            filters.add(AbstractShieldFilter.createFilter(compound));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        tagCompound.putBoolean("composed", shieldComposed);
        tagCompound.putBoolean("active", shieldActive);
        tagCompound.putInt("powerTimeout", powerTimeout);
        if (templateState.getMaterial() != Material.AIR) {
            tagCompound.putInt("templateColor", ((ShieldTemplateBlock)templateState.getBlock()).getColor().ordinal());
        }
        byte[] blocks = new byte[shieldBlocks.size() * 8];
        int j = 0;
        for (RelCoordinateShield c : shieldBlocks) {
            blocks[j+0] = shortToByte1((short) c.getDx());
            blocks[j+1] = shortToByte2((short) c.getDx());
            blocks[j+2] = shortToByte1((short) c.getDy());
            blocks[j+3] = shortToByte2((short) c.getDy());
            blocks[j+4] = shortToByte1((short) c.getDz());
            blocks[j+5] = shortToByte2((short) c.getDz());
            blocks[j+6] = shortToByte1((short) c.getState());
            blocks[j+7] = shortToByte2((short) c.getState());
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

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_ADDFILTER.equals(command)) {
            int action = params.get(PARAM_ACTION);
            String type = params.get(PARAM_TYPE);
            String player = params.get(PARAM_PLAYER);
            int selected = params.get(PARAM_SELECTED);
            addFilter(action, type, player, selected);
            return true;
        } else if (CMD_DELFILTER.equals(command)) {
            int selected = params.get(PARAM_SELECTED);
            delFilter(selected);
            return true;
        } else if (CMD_UPFILTER.equals(command)) {
            int selected = params.get(PARAM_SELECTED);
            upFilter(selected);
            return true;
        } else if (CMD_DOWNFILTER.equals(command)) {
            int selected = params.get(PARAM_SELECTED);
            downFilter(selected);
            return true;
        }

        return false;
    }

    @Nonnull
    @Override
    public <T> List<T> executeWithResultList(String command, TypedMap args, Type<T> type) {
        List<T> rc = super.executeWithResultList(command, args, type);
        if (!rc.isEmpty()) {
            return rc;
        }
        if (CMD_GETFILTERS.equals(command)) {
            return type.convert(getFilters());
        }
        return Collections.emptyList();
    }

    @Override
    public <T> boolean receiveListFromServer(String command, List<T> list, Type<T> type) {
        boolean rc = super.receiveListFromServer(command, list, type);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETFILTERS.equals(command)) {
            GuiShield.storeFiltersForClient(Type.create(ShieldFilter.class).convert(list));
            return true;
        }
        return false;
    }

    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(ShieldProjectorTileEntity.this, CONTAINER_FACTORY) {
            @Override
            protected void onUpdate(int index) {
                super.onUpdate(index);
                if (index == SLOT_SHAPE && !getStackInSlot(index).isEmpty()) {
                    // Restart if we go from having a stack to not having stack or the other way around.
                    decomposeShield();
                }
            }
        };
    }

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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return automationItemHandler.cast();
        }
        if (cap == CapabilityEnergy.ENERGY) {
            return energyHandler.cast();
        }
        if (cap == CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY) {
            return screenHandler.cast();
        }
        if (cap == CapabilityInfusable.INFUSABLE_CAPABILITY) {
            return infusableHandler.cast();
        }
        if (cap == CapabilityPowerInformation.POWER_INFORMATION_CAPABILITY) {
            return powerInfoHandler.cast();
        }
        return super.getCapability(cap, facing);
    }
}
