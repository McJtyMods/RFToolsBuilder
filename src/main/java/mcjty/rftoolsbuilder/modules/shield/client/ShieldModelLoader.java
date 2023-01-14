package mcjty.rftoolsbuilder.modules.shield.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import mcjty.lib.client.BaseGeometry;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.shield.ShieldTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ShieldModelLoader implements IGeometryLoader<ShieldModelLoader.TankModelGeometry> {

    public static void register(ModelEvent.RegisterGeometryLoaders event) {
        event.register("shieldloader", new ShieldModelLoader());
    }

    @Override
    public TankModelGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        return new TankModelGeometry();
    }

    public static class TankModelGeometry extends BaseGeometry<TankModelGeometry> {

        @Override
        public BakedModel bake() {
            return new ShieldBakedModel();
        }

        @Override
        public Collection<Material> getMaterials() {
            List<Material> materials = new ArrayList<>();
            for (ShieldTexture texture : ShieldTexture.values()) {
                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(RFToolsBuilder.MODID, "block/" + texture.getPath() + "/shield0")));
                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(RFToolsBuilder.MODID, "block/" + texture.getPath() + "/shield1")));
                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(RFToolsBuilder.MODID, "block/" + texture.getPath() + "/shield2")));
                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(RFToolsBuilder.MODID, "block/" + texture.getPath() + "/shield3")));
            }
            materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(RFToolsBuilder.MODID, "block/shield/shieldtransparent")));
            materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(RFToolsBuilder.MODID, "block/shield/shieldfull")));
            return materials;
        }
    }
}
