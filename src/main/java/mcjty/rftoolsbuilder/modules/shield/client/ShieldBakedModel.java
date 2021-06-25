package mcjty.rftoolsbuilder.modules.shield.client;

import mcjty.lib.client.AbstractDynamicBakedModel;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.shield.ShieldRenderingMode;
import mcjty.rftoolsbuilder.modules.shield.ShieldTexture;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingBlock;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ShieldBakedModel extends AbstractDynamicBakedModel {

    public static final ResourceLocation TEXTURE_TRANSPARENT = new ResourceLocation(RFToolsBuilder.MODID, "block/shield/shieldtransparent");
    public static final ResourceLocation TEXTURE_FULL = new ResourceLocation(RFToolsBuilder.MODID, "block/shield/shieldfull");

    private static Map<ShieldTexture, TextureAtlasSprite[]> shields;
    private static TextureAtlasSprite shieldtransparent;
    private static TextureAtlasSprite shieldfull;

    private static void initTextures() {
        if (shields == null) {
            shields = new HashMap<>();
            for (ShieldTexture texture : ShieldTexture.values()) {
                TextureAtlasSprite[] sprites = new TextureAtlasSprite[4];
                sprites[0] = getTexture(new ResourceLocation(RFToolsBuilder.MODID, "block/" + texture.getPath() + "/shield0"));
                sprites[1] = getTexture(new ResourceLocation(RFToolsBuilder.MODID, "block/" + texture.getPath() + "/shield1"));
                sprites[2] = getTexture(new ResourceLocation(RFToolsBuilder.MODID, "block/" + texture.getPath() + "/shield2"));
                sprites[3] = getTexture(new ResourceLocation(RFToolsBuilder.MODID, "block/" + texture.getPath() + "/shield3"));
                shields.put(texture, sprites);
            }
            shieldtransparent = getTexture(TEXTURE_TRANSPARENT);
            shieldfull = getTexture(TEXTURE_FULL);
        }
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        initTextures();
        ShieldRenderingMode mode = state.getValue(ShieldingBlock.RENDER_MODE);
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

    private List<BakedQuad> getQuadsShield(@Nullable Direction side, IModelData extraData) {
        List<BakedQuad> quads = new ArrayList<>();
        if (side != null) {
            Integer iconTopdown = extraData.getData(ShieldingTileEntity.ICON_TOPDOWN);
            Integer iconSide = extraData.getData(ShieldingTileEntity.ICON_SIDE);
            ShieldRenderData renderData = extraData.getData(ShieldingTileEntity.RENDER_DATA);
            if (renderData == null) {
                return quads;
            }
            float r = renderData.getRed();
            float g = renderData.getGreen();
            float b = renderData.getBlue();
            float a = renderData.getAlpha();
            TextureAtlasSprite[] shield = shields.get(renderData.getShieldTexture());
            switch (side) {
                case DOWN:
                    quads.add(createQuad(v(0, 0, 0), v(1, 0, 0), v(1, 0, 1), v(0, 0, 1), shield[iconTopdown], r, g, b, a));
                    break;
                case UP:
                    quads.add(createQuad(v(0, 1, 0), v(0, 1, 1), v(1, 1, 1), v(1, 1, 0), shield[iconTopdown], r, g, b, a));
                    break;
                case NORTH:
                    quads.add(createQuad(v(1, 1, 0), v(1, 0, 0), v(0, 0, 0), v(0, 1, 0), shield[iconSide], r, g, b, a));
                    break;
                case SOUTH:
                    quads.add(createQuad(v(0, 1, 1), v(0, 0, 1), v(1, 0, 1), v(1, 1, 1), shield[iconSide], r, g, b, a));
                    break;
                case WEST:
                    quads.add(createQuad(v(0, 1, 0), v(0, 0, 0), v(0, 0, 1), v(0, 1, 1), shield[iconSide], r, g, b, a));
                    break;
                case EAST:
                    quads.add(createQuad(v(1, 1, 1), v(1, 0, 1), v(1, 0, 0), v(1, 1, 0), shield[iconSide], r, g, b, a));
                    break;
            }
        }
        return quads;
    }

    private List<BakedQuad> getQuadsTextured(@Nullable Direction side, TextureAtlasSprite texture, IModelData extraData) {
        List<BakedQuad> quads = new ArrayList<>();
        if (side != null) {
            ShieldRenderData renderData = extraData.getData(ShieldingTileEntity.RENDER_DATA);
            if (renderData == null) {
                return quads;
            }
            float r = renderData.getRed();
            float g = renderData.getGreen();
            float b = renderData.getBlue();
            float a = renderData.getAlpha();
            switch (side) {
                case DOWN:
                    quads.add(createQuad(v(0, 0, 0), v(1, 0, 0), v(1, 0, 1), v(0, 0, 1), texture, r, g, b, a));
                    break;
                case UP:
                    quads.add(createQuad(v(0, 1, 0), v(0, 1, 1), v(1, 1, 1), v(1, 1, 0), texture, r, g, b, a));
                    break;
                case NORTH:
                    quads.add(createQuad(v(1, 1, 0), v(1, 0, 0), v(0, 0, 0), v(0, 1, 0), texture, r, g, b, a));
                    break;
                case SOUTH:
                    quads.add(createQuad(v(0, 1, 1), v(0, 0, 1), v(1, 0, 1), v(1, 1, 1), texture, r, g, b, a));
                    break;
                case WEST:
                    quads.add(createQuad(v(0, 1, 0), v(0, 0, 0), v(0, 0, 1), v(0, 1, 1), texture, r, g, b, a));
                    break;
                case EAST:
                    quads.add(createQuad(v(1, 1, 1), v(1, 0, 1), v(1, 0, 0), v(1, 1, 0), texture, r, g, b, a));
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

        // @todo 1.15
//        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
//        if (layer != null && !camo.getBlock().canRenderInLayer(camo, layer)) { // always render in the null layer or the block-breaking textures don't show up
//            return Collections.emptyList();
//        }
        IBakedModel model = getModel(camo);
        try {
            return model.getQuads(state, side, rand, null);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private IBakedModel getModel(@Nonnull BlockState facadeState) {
        initTextures();
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(facadeState);
    }


    @Override
    public TextureAtlasSprite getParticleIcon() {
        initTextures();
        return shieldfull;
    }
}
