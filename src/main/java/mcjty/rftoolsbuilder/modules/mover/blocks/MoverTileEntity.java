package mcjty.rftoolsbuilder.modules.mover.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import mcjty.rftoolsbuilder.modules.mover.MoverConfiguration;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;

import static mcjty.lib.api.container.DefaultContainerProvider.container;
import static mcjty.lib.builder.TooltipBuilder.*;
import static mcjty.lib.container.SlotDefinition.specific;

public class MoverTileEntity extends TickingTileEntity {

    public static final int SLOT_VEHICLE_CARD = 0;

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(1)
            .slot(specific(MoverModule.VEHICLE_CARD.get()).in().out(), SLOT_VEHICLE_CARD, 152, 10)
            .playerSlots(10, 70));

    @Cap(type = CapType.ITEMS_AUTOMATION)
    private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY)
            .onUpdate((integer, itemStack) -> {
                markDirtyClient();
            })
            .build();

    @Cap(type = CapType.ENERGY)
    private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(this, true, MoverConfiguration.MAXENERGY.get(), MoverConfiguration.RECEIVEPERTICK.get());

    @Cap(type = CapType.CONTAINER)
    private final LazyOptional<MenuProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Mover")
            .containerSupplier(container(MoverModule.CONTAINER_MOVER, CONTAINER_FACTORY,this))
            .itemHandler(() -> items)
            .energyHandler(() -> energyStorage)
            .setupSync(this));


    @Cap(type = CapType.INFUSABLE)
    private final IInfusable infusable = new DefaultInfusable(MoverTileEntity.this);

    @GuiValue
    private String name;
    @GuiValue
    private String other;

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .tileEntitySupplier(MoverTileEntity::new)
                .topDriver(RFToolsBuilderTOPDriver.DRIVER)
                .infusable()
                .manualEntry(ManualHelper.create("rftoolsbuilder:todo"))
                .info(key("message.rftoolsbuilder.shiftmessage"))
                .infoShift(header(), gold()));
    }


    public MoverTileEntity(BlockPos pos, BlockState state) {
        super(MoverModule.TYPE_MOVER.get(), pos, state);
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }


    @Override
    protected void tickServer() {
    }

    public ItemStack getCard() {
        return items.getStackInSlot(SLOT_VEHICLE_CARD);
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
    }

    @Override
    public void loadInfo(CompoundTag tagCompound) {
        super.loadInfo(tagCompound);
        CompoundTag info = tagCompound.getCompound("Info");
        name = info.getString("name");
        other = info.getString("other");
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tagCompound) {
        super.saveAdditional(tagCompound);
    }

    @Override
    public void saveInfo(CompoundTag tagCompound) {
        super.saveInfo(tagCompound);
        CompoundTag info = getOrCreateInfo(tagCompound);
        if (name != null) {
            info.putString("name", name);
        }
        if (other != null) {
            info.putString("other", other);
        }
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tagCompound) {
        ItemStack card = items.getStackInSlot(SLOT_VEHICLE_CARD);
        CompoundTag tag = new CompoundTag();
        card.save(tag);
        tagCompound.put("card", tag);
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tagCompound) {
        CompoundTag tag = tagCompound.getCompound("card");
        items.setStackInSlot(SLOT_VEHICLE_CARD, ItemStack.of(tag));
    }

    private void doScan() {
        // @todo
    }

    @ServerCommand
    public static final Command<?> CMD_SCAN = Command.<MoverTileEntity>create("scan", (te, player, params) -> te.doScan());
}
