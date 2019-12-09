package mcjty.rftoolsbuilder.modules.shield.client;

import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.shield.ShieldRenderingMode;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.*;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CamoBakedModel implements IDynamicBakedModel {

    public static final ModelResourceLocation modelFacade = new ModelResourceLocation(RFToolsBuilder.MODID + ":camo");

    private VertexFormat format;
    private static TextureAtlasSprite spriteCable;

    public CamoBakedModel(VertexFormat format) {
        this.format = format;
    }

    private static void initTextures() {
        if (spriteCable == null) {
            spriteCable = Minecraft.getInstance().getTextureMap().getAtlasSprite(RFToolsBuilder.MODID + ":block/shield/shield0");
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
                return getQuadsTextured(state, side, spriteCable);
            case MIMIC:
                return getQuadsMimic(state, side, rand);
            case TRANSP:
                return getQuadsTextured(state, side, spriteCable);
            case SOLID:
                return getQuadsTextured(state, side, spriteCable);
        }
        return Collections.emptyList();
    }

    private void putVertex(UnpackedBakedQuad.Builder builder, Vec3d normal,
                           double x, double y, double z, float u, float v, TextureAtlasSprite sprite, float color) {
        for (int e = 0; e < format.getElementCount(); e++) {
            switch (format.getElement(e).getUsage()) {
                case POSITION:
                    builder.put(e, (float)x, (float)y, (float)z, 1.0f);
                    break;
                case COLOR:
                    builder.put(e, color, color, color, 1.0f);
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
        putVertex(builder, normal, v1.x, v1.y, v1.z, 0, 0, sprite, 1.0f);
        putVertex(builder, normal, v2.x, v2.y, v2.z, 0, 16, sprite, 1.0f);
        putVertex(builder, normal, v3.x, v3.y, v3.z, 16, 16, sprite, 1.0f);
        putVertex(builder, normal, v4.x, v4.y, v4.z, 16, 0, sprite, 1.0f);
        return builder.build();
    }

    private static Vec3d v(int x, int y, int z) {
        return new Vec3d(x, y, z);
    }

    private List<BakedQuad> getQuadsTextured(@Nullable BlockState state, @Nullable Direction side, TextureAtlasSprite texture) {
        List<BakedQuad> quads = new ArrayList<>();
        if (side != null) {
            switch (side) {
                case DOWN:
                    quads.add(createQuad(v(0, 0, 0), v(1, 0, 0), v(1, 0, 1), v(0, 0, 1), texture));
                    break;
                case UP:
                    quads.add(createQuad(v(0, 1, 0), v(0, 1, 1), v(1, 1, 1), v(1, 1, 0), texture));
                    break;
                case NORTH:
                    quads.add(createQuad(v(1, 1, 0), v(1, 0, 0), v(0, 0, 0), v(0, 1, 0), texture));
                    break;
                case SOUTH:
                    quads.add(createQuad(v(0, 1, 1), v(0, 0, 1), v(1, 0, 1), v(1, 1, 1), texture));
                    break;
                case WEST:
                    quads.add(createQuad(v(0, 1, 0), v(0, 0, 0), v(0, 0, 1), v(0, 1, 1), texture));
                    break;
                case EAST:
                    quads.add(createQuad(v(1, 1, 1), v(1, 0, 1), v(1, 0, 0), v(1, 1, 0), texture));
                    break;
            }
        }
        return quads;
    }


    private List<BakedQuad> getQuadsMimic(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand) {
        BlockState camo = null; // @todo 1.14 extraData.getData(NoTickShieldBlockTileEntity.CAMO_PROPERTY);
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
        return spriteCable;
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
