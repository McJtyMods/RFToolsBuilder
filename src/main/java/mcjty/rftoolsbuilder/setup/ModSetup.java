package mcjty.rftoolsbuilder.setup;

import mcjty.lib.McJtyLib;
import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldProjectorTileEntity;
import mcjty.rftoolsbuilder.modules.shield.filters.AbstractShieldFilter;
import mcjty.rftoolsbuilder.modules.shield.filters.ShieldFilter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModSetup extends DefaultModSetup {

    public ModSetup() {
        createTab("rftoolsbuilder", () -> new ItemStack(BuilderModule.BUILDER.get()));
    }

    @Override
    public void init(FMLCommonSetupEvent e) {
        super.init(e);
        e.enqueueWork(() -> {
            CommandHandler.registerCommands();
            McJtyLib.registerCommandInfo(ShieldProjectorTileEntity.CMD_GETFILTERS.getName(), ShieldFilter.class, AbstractShieldFilter::createFilter, (buf, el) -> el.toBytes(buf));
        });
        RFToolsBuilderMessages.registerMessages("rftoolsbuilder");
    }

    @Override
    protected void setupModCompat() {
        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
//        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "mcjty.rftools.compat.theoneprobe.TheOneProbeSupport");
    }
}
