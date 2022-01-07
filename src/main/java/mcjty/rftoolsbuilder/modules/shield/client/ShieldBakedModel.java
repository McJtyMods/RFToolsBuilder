package mcjty.rftoolsbuilder.modules.shield.client;

import mcjty.lib.client.AbstractDynamicBakedModel;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.shield.ShieldRenderingMode;
import mcjty.rftoolsbuilder.modules.shield.ShieldTexture;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingBlock;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingTileEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
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
        return switch (mode) {
            case INVISIBLE -> Collections.emptyList();
            case SHIELD -> getQuadsShield(side, extraData);
            case MIMIC -> getQuadsMimic(state, side, rand, extraData);
            case TRANSP -> getQuadsTextured(side, shieldtransparent, extraData);
            case SOLID -> getQuadsTextured(side, shieldfull, extraData);
        };
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
            float r = renderData.red();
            float g = renderData.green();
            float b = renderData.blue();
            float a = renderData.alpha();
            TextureAtlasSprite[] shield = shields.get(renderData.shieldTexture());
            switch (side) {
                case DOWN -> quads.add(createQuad(v(0, 0, 0), v(1, 0, 0), v(1, 0, 1), v(0, 0, 1), shield[iconTopdown], r, g, b, a));
                case UP -> quads.add(createQuad(v(0, 1, 0), v(0, 1, 1), v(1, 1, 1), v(1, 1, 0), shield[iconTopdown], r, g, b, a));
                case NORTH -> quads.add(createQuad(v(1, 1, 0), v(1, 0, 0), v(0, 0, 0), v(0, 1, 0), shield[iconSide], r, g, b, a));
                case SOUTH -> quads.add(createQuad(v(0, 1, 1), v(0, 0, 1), v(1, 0, 1), v(1, 1, 1), shield[iconSide], r, g, b, a));
                case WEST -> quads.add(createQuad(v(0, 1, 0), v(0, 0, 0), v(0, 0, 1), v(0, 1, 1), shield[iconSide], r, g, b, a));
                case EAST -> quads.add(createQuad(v(1, 1, 1), v(1, 0, 1), v(1, 0, 0), v(1, 1, 0), shield[iconSide], r, g, b, a));
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
            float r = renderData.red();
            float g = renderData.green();
            float b = renderData.blue();
            float a = renderData.alpha();
            switch (side) {
                case DOWN -> quads.add(createQuad(v(0, 0, 0), v(1, 0, 0), v(1, 0, 1), v(0, 0, 1), texture, r, g, b, a));
                case UP -> quads.add(createQuad(v(0, 1, 0), v(0, 1, 1), v(1, 1, 1), v(1, 1, 0), texture, r, g, b, a));
                case NORTH -> quads.add(createQuad(v(1, 1, 0), v(1, 0, 0), v(0, 0, 0), v(0, 1, 0), texture, r, g, b, a));
                case SOUTH -> quads.add(createQuad(v(0, 1, 1), v(0, 0, 1), v(1, 0, 1), v(1, 1, 1), texture, r, g, b, a));
                case WEST -> quads.add(createQuad(v(0, 1, 0), v(0, 0, 0), v(0, 0, 1), v(0, 1, 1), texture, r, g, b, a));
                case EAST -> quads.add(createQuad(v(1, 1, 1), v(1, 0, 1), v(1, 0, 0), v(1, 1, 0), texture, r, g, b, a));
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
        BakedModel model = getModel(camo);
        try {
            return model.getQuads(state, side, rand, null);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private BakedModel getModel(@Nonnull BlockState facadeState) {
        initTextures();
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(facadeState);
    }


    @Nonnull
    @Override
    public TextureAtlasSprite getParticleIcon() {
        initTextures();
        return shieldfull;
    }
}
