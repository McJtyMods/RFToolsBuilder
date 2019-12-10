package mcjty.rftoolsbuilder.modules.shield.client;

import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.shield.ShieldRenderingMode;
import mcjty.rftoolsbuilder.modules.shield.ShieldTexture;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingBlock;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ShieldingBakedModel implements IDynamicBakedModel {

    private VertexFormat format;
    private static Map<ShieldTexture, TextureAtlasSprite[]> shields;
    private static TextureAtlasSprite shieldtransparent;
    private static TextureAtlasSprite shieldfull;

    public ShieldingBakedModel(VertexFormat format) {
        this.format = format;
    }

    private static void initTextures() {
        if (shields == null) {
            shields = new HashMap<>();
            for (ShieldTexture texture : ShieldTexture.values()) {
                TextureAtlasSprite[] sprites = new TextureAtlasSprite[4];
                sprites[0] = Minecraft.getInstance().getTextureMap().getAtlasSprite(RFToolsBuilder.MODID + ":block/" + texture.getPath() + "/shield0");
                sprites[1] = Minecraft.getInstance().getTextureMap().getAtlasSprite(RFToolsBuilder.MODID + ":block/" + texture.getPath() + "/shield1");
                sprites[2] = Minecraft.getInstance().getTextureMap().getAtlasSprite(RFToolsBuilder.MODID + ":block/" + texture.getPath() + "/shield2");
                sprites[3] = Minecraft.getInstance().getTextureMap().getAtlasSprite(RFToolsBuilder.MODID + ":block/" + texture.getPath() + "/shield3");
                shields.put(texture, sprites);
            }
            shieldtransparent = Minecraft.getInstance().getTextureMap().getAtlasSprite(RFToolsBuilder.MODID + ":block/shield/shieldtransparent");
            shieldfull = Minecraft.getInstance().getTextureMap().getAtlasSprite(RFToolsBuilder.MODID + ":block/shield/shieldfull");
        }
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        initTextures();
        ShieldRenderingMode mode = state.get(ShieldingBlock.RENDER_MODE);
        switch (mode) {
            case INVISIBLE:
                return Collections.emptyList();
            case SHIELD:
                return getQuadsShield(side, extraData);
            case MIMIC:
                return getQuadsMimic(state, side, rand, extraData);
            case TRANSP:
                return getQuadsTextured(side, shieldtransparent, extraData);
            case SOLID:
                return getQuadsTextured(side, shieldfull, extraData);
        }
        return Collections.emptyList();
    }

    private void putVertex(UnpackedBakedQuad.Builder builder, Vec3d normal,
                           double x, double y, double z, float u, float v, TextureAtlasSprite sprite,
                           float r, float g, float b, float a) {
        for (int e = 0; e < format.getElementCount(); e++) {
            switch (format.getElement(e).getUsage()) {
                case POSITION:
                    builder.put(e, (float)x, (float)y, (float)z, 1.0f);
                    break;
                case COLOR:
                    builder.put(e, r, g, b, a);
                    break;
                case UV:
                    if (format.getElement(e).getIndex() == 0) {
                        u = sprite.getInterpolatedU(u);
                        v = sprite.getInterpolatedV(v);
                        builder.put(e, u, v, 0f, 1f);
                        break;
                    }
                case NORMAL:
                    builder.put(e, (float) normal.x, (float) normal.y, (float) normal.z, 0f);
                    break;
                default:
                    builder.put(e);
                    break;
            }
        }
    }


    private BakedQuad createQuad(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, TextureAtlasSprite sprite) {
        Vec3d normal = v3.subtract(v2).crossProduct(v1.subtract(v2)).normalize();

        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setTexture(sprite);
        putVertex(builder, normal, v1.x, v1.y, v1.z, 0, 0, sprite, 1.0f, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v2.x, v2.y, v2.z, 0, 16, sprite, 1.0f, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v3.x, v3.y, v3.z, 16, 16, sprite, 1.0f, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v4.x, v4.y, v4.z, 16, 0, sprite, 1.0f, 1.0f, 1.0f, 1.0f);
        return builder.build();
    }

    private BakedQuad createColoredQuad(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, TextureAtlasSprite sprite,
                                        float r, float g, float b, float a) {
        Vec3d normal = v3.subtract(v2).crossProduct(v1.subtract(v2)).normalize();

        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setTexture(sprite);
        putVertex(builder, normal, v1.x, v1.y, v1.z, 0, 0, sprite, r, g, b, a);
        putVertex(builder, normal, v2.x, v2.y, v2.z, 0, 16, sprite, r, g, b, a);
        putVertex(builder, normal, v3.x, v3.y, v3.z, 16, 16, sprite, r, g, b, a);
        putVertex(builder, normal, v4.x, v4.y, v4.z, 16, 0, sprite, r, g, b, a);
        return builder.build();
    }

    private static Vec3d v(int x, int y, int z) {
        return new Vec3d(x, y, z);
    }

    private List<BakedQuad> getQuadsShield(@Nullable Direction side, IModelData extraData) {
        List<BakedQuad> quads = new ArrayList<>();
        if (side != null) {
            Integer iconTopdown = extraData.getData(ShieldingTileEntity.ICON_TOPDOWN);
            Integer iconSide = extraData.getData(ShieldingTileEntity.ICON_SIDE);
            ShieldRenderData renderData = extraData.getData(ShieldingTileEntity.RENDER_DATA);
            float r = renderData.getRed();
            float g = renderData.getGreen();
            float b = renderData.getBlue();
            float a = renderData.getAlpha();
            TextureAtlasSprite[] shield = shields.get(renderData.getShieldTexture());
            switch (side) {
                case DOWN:
                    quads.add(createColoredQuad(v(0, 0, 0), v(1, 0, 0), v(1, 0, 1), v(0, 0, 1), shield[iconTopdown], r, g, b, a));
                    break;
                case UP:
                    quads.add(createColoredQuad(v(0, 1, 0), v(0, 1, 1), v(1, 1, 1), v(1, 1, 0), shield[iconTopdown], r, g, b, a));
                    break;
                case NORTH:
                    quads.add(createColoredQuad(v(1, 1, 0), v(1, 0, 0), v(0, 0, 0), v(0, 1, 0), shield[iconSide], r, g, b, a));
                    break;
                case SOUTH:
                    quads.add(createColoredQuad(v(0, 1, 1), v(0, 0, 1), v(1, 0, 1), v(1, 1, 1), shield[iconSide], r, g, b, a));
                    break;
                case WEST:
                    quads.add(createColoredQuad(v(0, 1, 0), v(0, 0, 0), v(0, 0, 1), v(0, 1, 1), shield[iconSide], r, g, b, a));
                    break;
                case EAST:
                    quads.add(createColoredQuad(v(1, 1, 1), v(1, 0, 1), v(1, 0, 0), v(1, 1, 0), shield[iconSide], r, g, b, a));
                    break;
            }
        }
        return quads;
    }

    private List<BakedQuad> getQuadsTextured(@Nullable Direction side, TextureAtlasSprite texture, IModelData extraData) {
        List<BakedQuad> quads = new ArrayList<>();
        if (side != null) {
            ShieldRenderData renderData = extraData.getData(ShieldingTileEntity.RENDER_DATA);
            float r = renderData.getRed();
            float g = renderData.getGreen();
            float b = renderData.getBlue();
            float a = renderData.getAlpha();
            switch (side) {
                case DOWN:
                    quads.add(createColoredQuad(v(0, 0, 0), v(1, 0, 0), v(1, 0, 1), v(0, 0, 1), texture, r, g, b, a));
                    break;
                case UP:
                    quads.add(createColoredQuad(v(0, 1, 0), v(0, 1, 1), v(1, 1, 1), v(1, 1, 0), texture, r, g, b, a));
                    break;
                case NORTH:
                    quads.add(createColoredQuad(v(1, 1, 0), v(1, 0, 0), v(0, 0, 0), v(0, 1, 0), texture, r, g, b, a));
                    break;
                case SOUTH:
                    quads.add(createColoredQuad(v(0, 1, 1), v(0, 0, 1), v(1, 0, 1), v(1, 1, 1), texture, r, g, b, a));
                    break;
                case WEST:
                    quads.add(createColoredQuad(v(0, 1, 0), v(0, 0, 0), v(0, 0, 1), v(0, 1, 1), texture, r, g, b, a));
                    break;
                case EAST:
                    quads.add(createColoredQuad(v(1, 1, 1), v(1, 0, 1), v(1, 0, 0), v(1, 1, 0), texture, r, g, b, a));
                    break;
            }
        }
        return quads;
    }


    private List<BakedQuad> getQuadsMimic(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, IModelData extraData) {
        BlockState camo = extraData.getData(ShieldingTileEntity.MIMIC);
        if (camo == null) {
            return Collections.emptyList();
        }

        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        if (layer != null && !camo.getBlock().canRenderInLayer(camo, layer)) { // always render in the null layer or the block-breaking textures don't show up
            return Collections.emptyList();
        }
        IBakedModel model = getModel(camo);
        try {
            return model.getQuads(state, side, rand);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private IBakedModel getModel(@Nonnull BlockState facadeState) {
        initTextures();
        IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(facadeState);
        return model;
    }


    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        initTextures();
        return shieldfull;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

}
