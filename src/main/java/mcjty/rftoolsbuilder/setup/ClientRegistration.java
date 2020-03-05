package mcjty.rftoolsbuilder.setup;


import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderSetup;
import mcjty.rftoolsbuilder.modules.builder.client.BuilderRenderer;
import mcjty.rftoolsbuilder.modules.builder.client.GuiBuilder;
import mcjty.rftoolsbuilder.modules.shield.ShieldSetup;
import mcjty.rftoolsbuilder.modules.shield.client.GuiShield;
import mcjty.rftoolsbuilder.modules.shield.client.ShieldModelLoader;
import mcjty.rftoolsbuilder.shapes.ShapeDataManagerClient;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RFToolsBuilder.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistration {

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        GenericGuiContainer.register(BuilderSetup.CONTAINER_BUILDER.get(), GuiBuilder::new);
        GenericGuiContainer.register(ShieldSetup.CONTAINER_SHIELD.get(), GuiShield::new);
        BuilderRenderer.register();
        MinecraftForge.EVENT_BUS.addListener(ShapeDataManagerClient::cleanupOldRenderers);

        RenderTypeLookup.setRenderLayer(BuilderSetup.SUPPORT.get(), RenderType.getTranslucent());
        RenderTypeLookup.setRenderLayer(ShieldSetup.SHIELDING_TRANSLUCENT.get(), RenderType.getTranslucent());
        RenderTypeLookup.setRenderLayer(ShieldSetup.SHIELDING_SOLID.get(), RenderType.getSolid());
        RenderTypeLookup.setRenderLayer(ShieldSetup.SHIELDING_CUTOUT.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(ShieldSetup.TEMPLATE_GREEN.get(), RenderType.getTranslucent());
        RenderTypeLookup.setRenderLayer(ShieldSetup.TEMPLATE_BLUE.get(), RenderType.getTranslucent());
        RenderTypeLookup.setRenderLayer(ShieldSetup.TEMPLATE_RED.get(), RenderType.getTranslucent());
        RenderTypeLookup.setRenderLayer(ShieldSetup.TEMPLATE_YELLOW.get(), RenderType.getTranslucent());

        ModelLoaderRegistry.registerLoader(new ResourceLocation(RFToolsBuilder.MODID, "shieldloader"), new ShieldModelLoader());
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> sounds) {
    }

//    @SubscribeEvent
//    public static void onModelBake(ModelBakeEvent event) {
//        ShieldBakedModel model = new ShieldBakedModel();
//        Lists.newArrayList("shielding_solid", "shielding_translucent", "shielding_cutout").stream()
//                .forEach(name -> {
//                    ResourceLocation rl = new ResourceLocation(RFToolsBuilder.MODID, name);
//                    event.getModelRegistry().put(new ModelResourceLocation(rl, ""), model);
//                    Tools.permutateProperties(s -> event.getModelRegistry().put(new ModelResourceLocation(rl, s), model),
//                            BLOCKED_HOSTILE, BLOCKED_ITEMS, BLOCKED_PASSIVE, BLOCKED_PLAYERS,
//                            DAMAGE_HOSTILE, DAMAGE_ITEMS, DAMAGE_PASSIVE, DAMAGE_PLAYERS,
//                            FLAG_OPAQUE, RENDER_MODE);
//                });
//    }
}
