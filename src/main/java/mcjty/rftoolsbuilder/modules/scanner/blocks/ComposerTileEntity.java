package mcjty.rftoolsbuilder.modules.scanner.blocks;

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
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.shapes.Shape;
import mcjty.rftoolsbuilder.shapes.ShapeModifier;
import mcjty.rftoolsbuilder.shapes.ShapeOperation;
import mcjty.rftoolsbuilder.shapes.ShapeRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import java.util.List;

import static mcjty.lib.api.container.DefaultContainerProvider.container;
import static mcjty.lib.container.SlotDefinition.specific;

public class ComposerTileEntity extends TickingTileEntity {

    public static final String CMD_SETTINGS_ID = "composer.settings";

    public static final int SLOT_COUNT = 9;
    public static final int SLOT_OUT = 0;
    public static final int SLOT_TABS = 1;
    public static final int SLOT_GHOSTS = SLOT_TABS + SLOT_COUNT;
    private static final int SIDE_PANEL_WIDTH = 80;
    private static final int CARD_SLOT_X = SIDE_PANEL_WIDTH + 18;
    private static final int MATERIAL_SLOT_X = SIDE_PANEL_WIDTH + 36;
    private static final int PLAYER_SLOTS_X = SIDE_PANEL_WIDTH + 85;

    public static final List<Key<String>> PARAM_OPS = List.of(
            new Key<>("op0", Type.STRING), new Key<>("op1", Type.STRING), new Key<>("op2", Type.STRING),
            new Key<>("op3", Type.STRING), new Key<>("op4", Type.STRING), new Key<>("op5", Type.STRING),
            new Key<>("op6", Type.STRING), new Key<>("op7", Type.STRING), new Key<>("op8", Type.STRING));
    public static final List<Key<Boolean>> PARAM_FLIPS = List.of(
            new Key<>("flip0", Type.BOOLEAN), new Key<>("flip1", Type.BOOLEAN), new Key<>("flip2", Type.BOOLEAN),
            new Key<>("flip3", Type.BOOLEAN), new Key<>("flip4", Type.BOOLEAN), new Key<>("flip5", Type.BOOLEAN),
            new Key<>("flip6", Type.BOOLEAN), new Key<>("flip7", Type.BOOLEAN), new Key<>("flip8", Type.BOOLEAN));
    public static final List<Key<String>> PARAM_ROTS = List.of(
            new Key<>("rot0", Type.STRING), new Key<>("rot1", Type.STRING), new Key<>("rot2", Type.STRING),
            new Key<>("rot3", Type.STRING), new Key<>("rot4", Type.STRING), new Key<>("rot5", Type.STRING),
            new Key<>("rot6", Type.STRING), new Key<>("rot7", Type.STRING), new Key<>("rot8", Type.STRING));

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> {
        ContainerFactory factory = new ContainerFactory(SLOT_GHOSTS + SLOT_COUNT)
                .slot(specific(s -> s.getItem() instanceof ShapeCardItem).in().out(), SLOT_OUT, CARD_SLOT_X, 200)
                .playerSlots(PLAYER_SLOTS_X, 142);
        for (int i = 0; i < SLOT_COUNT; i++) {
            factory.slot(specific(s -> s.getItem() instanceof ShapeCardItem).in().out(), SLOT_TABS + i, CARD_SLOT_X, 7 + i * 18);
            factory.slot(specific(s -> true).in().out(), SLOT_GHOSTS + i, MATERIAL_SLOT_X, 7 + i * 18);
        }
        return factory;
    });

    @Cap(type = CapType.ITEMS_AUTOMATION)
    private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY)
            .itemValid((slot, stack) -> {
                if (slot == SLOT_OUT || (slot >= SLOT_TABS && slot < SLOT_GHOSTS)) {
                    return stack.getItem() instanceof ShapeCardItem;
                }
                return slot >= SLOT_GHOSTS && slot < SLOT_GHOSTS + SLOT_COUNT;
            })
            .onUpdate((slot, stack) -> markComposerDirty())
            .build();

    @Cap(type = CapType.CONTAINER)
    private final Lazy<MenuProvider> screenHandler = Lazy.of(() -> new DefaultContainerProvider<GenericContainer>("Composer")
            .containerSupplier(container(ScannerModule.CONTAINER_COMPOSER, CONTAINER_FACTORY, this))
            .itemHandler(() -> items)
            .setupSync(this));

    private final ShapeModifier[] modifiers = new ShapeModifier[SLOT_COUNT];
    private boolean composeDirty = true;

    public ComposerTileEntity(BlockPos pos, BlockState state) {
        this(ScannerModule.TYPE_COMPOSER.get(), pos, state);
    }

    public ComposerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        for (int i = 0; i < SLOT_COUNT; i++) {
            modifiers[i] = new ShapeModifier(ShapeOperation.UNION, false, ShapeRotation.NONE);
        }
    }

    @Override
    protected void tickServer() {
        if (composeDirty) {
            updateOutput();
            composeDirty = false;
        }
    }

    public ShapeModifier[] getModifiers() {
        return modifiers;
    }

    private void markComposerDirty() {
        composeDirty = true;
        setChanged();
        markDirtyClient();
    }

    private void updateOutput() {
        ItemStack output = items.getStackInSlot(SLOT_OUT);
        if (output.isEmpty()) {
            return;
        }

        ListTag children = new ListTag();
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack card = items.getStackInSlot(SLOT_TABS + i);
            if (card.isEmpty()) {
                continue;
            }
            CompoundTag child = card.getOrCreateTag().copy();
            ShapeCardItem.setModifier(child, modifiers[i]);
            ShapeCardItem.setGhostMaterial(child, items.getStackInSlot(SLOT_GHOSTS + i));
            children.add(child);
        }
        ShapeCardItem.setChildren(output, children);
        if (!ShapeCardItem.getShape(output).isComposition()) {
            ShapeCardItem.setShape(output, Shape.SHAPE_COMPOSITION, true);
        }
        setChanged();
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        ListTag list = tag.getList("ops", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(SLOT_COUNT, list.size()); i++) {
            CompoundTag tc = list.getCompound(i);
            ShapeOperation operation = ShapeOperation.getByName(tc.getString("mod_op"));
            if (operation == null) {
                operation = ShapeOperation.UNION;
            }
            ShapeRotation rotation = ShapeRotation.getByName(tc.getString("mod_rot"));
            if (rotation == null) {
                rotation = ShapeRotation.NONE;
            }
            modifiers[i] = new ShapeModifier(operation, tc.getBoolean("mod_flipy"), rotation);
        }
        composeDirty = true;
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (ShapeModifier modifier : modifiers) {
            CompoundTag tc = new CompoundTag();
            tc.putString("mod_op", modifier.getOperation().getCode());
            tc.putBoolean("mod_flipy", modifier.isFlipY());
            tc.putString("mod_rot", modifier.getRotation().getCode());
            list.add(tc);
        }
        tag.put("ops", list);
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tag) {
        ListTag list = new ListTag();
        for (ShapeModifier modifier : modifiers) {
            CompoundTag tc = new CompoundTag();
            tc.putString("mod_op", modifier.getOperation().getCode());
            tc.putBoolean("mod_flipy", modifier.isFlipY());
            tc.putString("mod_rot", modifier.getRotation().getCode());
            list.add(tc);
        }
        tag.put("ops", list);
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tag) {
        ListTag list = tag.getList("ops", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(SLOT_COUNT, list.size()); i++) {
            CompoundTag tc = list.getCompound(i);
            ShapeOperation operation = ShapeOperation.getByName(tc.getString("mod_op"));
            if (operation == null) {
                operation = ShapeOperation.UNION;
            }
            ShapeRotation rotation = ShapeRotation.getByName(tc.getString("mod_rot"));
            if (rotation == null) {
                rotation = ShapeRotation.NONE;
            }
            modifiers[i] = new ShapeModifier(operation, tc.getBoolean("mod_flipy"), rotation);
        }
    }

    @ServerCommand
    public static final Command<?> CMD_SETTINGS = Command.<ComposerTileEntity>create(CMD_SETTINGS_ID,
            (te, player, params) -> te.applySettings(params));

    private void applySettings(TypedMap params) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            ShapeOperation operation = ShapeOperation.getByName(params.get(PARAM_OPS.get(i)));
            if (operation == null) {
                operation = ShapeOperation.UNION;
            }
            ShapeRotation rotation = ShapeRotation.getByName(params.get(PARAM_ROTS.get(i)));
            if (rotation == null) {
                rotation = ShapeRotation.NONE;
            }
            modifiers[i] = new ShapeModifier(operation, params.get(PARAM_FLIPS.get(i)), rotation);
        }
        markComposerDirty();
    }
}
