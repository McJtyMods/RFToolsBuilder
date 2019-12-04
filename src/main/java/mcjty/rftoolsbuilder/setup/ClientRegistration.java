package mcjty.rftoolsbuilder.setup;


import com.google.common.collect.Lists;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderSetup;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderRenderer;
import mcjty.rftoolsbuilder.modules.builder.client.GuiBuilder;
import mcjty.rftoolsbuilder.modules.shield.ShieldSetup;
import mcjty.rftoolsbuilder.modules.shield.client.CamoBakedModel;
import mcjty.rftoolsbuilder.modules.shield.client.GuiShield;
import mcjty.rftoolsbuilder.shapes.ShapeDataManagerClient;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
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
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> sounds) {
    }

//    @SubscribeEvent
//    public static void onTextureStitch(TextureStitchEvent.Pre event) {
//        if (!event.getMap().getBasePath().equals("textures")) {
//            return;
//        }
//
//        event.addSprite(new ResourceLocation(RFToolsBuilder.MODID, "block/connector_side"));
//    }


    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        CamoBakedModel model = new CamoBakedModel(DefaultVertexFormats.BLOCK);
        Lists.newArrayList("shield_camo_block").stream()
                .forEach(name -> {
                    ResourceLocation rl = new ResourceLocation(RFToolsBuilder.MODID, name);
                    event.getModelRegistry().put(new ModelResourceLocation(rl, ""), model);
                });
    }

    @SubscribeEvent
    public static void onRenderWorldLastEvent(RenderWorldLastEvent event) {
        ShapeDataManagerClient.cleanupOldRenderers();
    }
}
