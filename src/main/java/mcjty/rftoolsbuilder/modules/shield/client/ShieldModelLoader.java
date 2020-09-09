package mcjty.rftoolsbuilder.modules.shield.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.shield.ShieldTexture;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ShieldModelLoader implements IModelLoader<ShieldModelLoader.TankModelGeometry> {

    public static void register(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(new ResourceLocation(RFToolsBuilder.MODID, "shieldloader"), new ShieldModelLoader());
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Override
    public TankModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        return new TankModelGeometry();
    }

    public static class TankModelGeometry implements IModelGeometry<TankModelGeometry> {
        @Override
        public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
            return new ShieldBakedModel();
        }

        @Override
        public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            List<RenderMaterial> materials = new ArrayList<>();
            for (ShieldTexture texture : ShieldTexture.values()) {
                materials.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(RFToolsBuilder.MODID, "block/" + texture.getPath() + "/shield0")));
                materials.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(RFToolsBuilder.MODID, "block/" + texture.getPath() + "/shield1")));
                materials.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(RFToolsBuilder.MODID, "block/" + texture.getPath() + "/shield2")));
                materials.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(RFToolsBuilder.MODID, "block/" + texture.getPath() + "/shield3")));
            }
            materials.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(RFToolsBuilder.MODID, "block/shield/shieldtransparent")));
            materials.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(RFToolsBuilder.MODID, "block/shield/shieldfull")));
            return materials;
        }
    }
}
