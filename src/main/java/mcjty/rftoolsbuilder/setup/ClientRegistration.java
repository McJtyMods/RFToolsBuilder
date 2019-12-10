package mcjty.rftoolsbuilder.setup;


import com.google.common.collect.Lists;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderSetup;
import mcjty.rftoolsbuilder.modules.builder.client.BuilderRenderer;
import mcjty.rftoolsbuilder.modules.builder.client.GuiBuilder;
import mcjty.rftoolsbuilder.modules.shield.ShieldSetup;
import mcjty.rftoolsbuilder.modules.shield.client.ShieldingBakedModel;
import mcjty.rftoolsbuilder.modules.shield.client.GuiShield;
import mcjty.rftoolsbuilder.shapes.ShapeDataManagerClient;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingBlock.*;

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

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (!event.getMap().getBasePath().equals("textures")) {
            return;
        }

        event.addSprite(new ResourceLocation(RFToolsBuilder.MODID, "block/shield/shield0"));
        event.addSprite(new ResourceLocation(RFToolsBuilder.MODID, "block/shield/shield1"));
        event.addSprite(new ResourceLocation(RFToolsBuilder.MODID, "block/shield/shield2"));
        event.addSprite(new ResourceLocation(RFToolsBuilder.MODID, "block/shield/shield3"));
        event.addSprite(new ResourceLocation(RFToolsBuilder.MODID, "block/shield/shieldtransparent"));
        event.addSprite(new ResourceLocation(RFToolsBuilder.MODID, "block/shield/shieldfull"));
    }


    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        ShieldingBakedModel model = new ShieldingBakedModel(DefaultVertexFormats.BLOCK);
        Lists.newArrayList("shielding").stream()
                .forEach(name -> {
                    ResourceLocation rl = new ResourceLocation(RFToolsBuilder.MODID, name);
                    event.getModelRegistry().put(new ModelResourceLocation(rl, ""), model);
                    Tools.permutateProperties(s -> event.getModelRegistry().put(new ModelResourceLocation(rl, s), model),
                            BLOCKED_HOSTILE, BLOCKED_ITEMS, BLOCKED_PASSIVE, BLOCKED_PLAYERS,
                            DAMAGE_HOSTILE, DAMAGE_ITEMS, DAMAGE_PASSIVE, DAMAGE_PLAYERS,
                            FLAG_OPAQUE, RENDER_MODE);
                });
    }

    @SubscribeEvent
    public static void onRenderWorldLastEvent(RenderWorldLastEvent event) {
        ShapeDataManagerClient.cleanupOldRenderers();
    }
}
