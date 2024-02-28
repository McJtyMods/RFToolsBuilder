package mcjty.rftoolsbuilder.modules.shield.client;

import mcjty.lib.client.AbstractDynamicBakedModel;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import mcjty.rftoolsbuilder.modules.shield.ShieldRenderingMode;
import mcjty.rftoolsbuilder.modules.shield.ShieldTexture;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingBlock;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

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

    @Override
    @NotNull
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        initTextures();
        ShieldRenderingMode mode = state.getValue(ShieldingBlock.RENDER_MODE);
        return switch (mode) {
            case INVISIBLE -> Collections.emptyList();
            case SHIELD -> getQuadsShield(side, extraData);
            case MIMIC -> getQuadsMimic(state, side, rand, extraData, renderType);
            case TRANSP -> getQuadsTextured(side, shieldtransparent, extraData);
            case SOLID -> getQuadsTextured(side, shieldfull, extraData);
        };
    }

    private List<BakedQuad> getQuadsShield(@Nullable Direction side, ModelData extraData) {
        List<BakedQuad> quads = new ArrayList<>();
        if (side != null) {
            Integer iconTopdown = extraData.get(ShieldingTileEntity.ICON_TOPDOWN);
            Integer iconSide = extraData.get(ShieldingTileEntity.ICON_SIDE);
            ShieldRenderData renderData = extraData.get(ShieldingTileEntity.RENDER_DATA);
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

    private List<BakedQuad> getQuadsTextured(@Nullable Direction side, TextureAtlasSprite texture, ModelData extraData) {
        List<BakedQuad> quads = new ArrayList<>();
        if (side != null) {
            ShieldRenderData renderData = extraData.get(ShieldingTileEntity.RENDER_DATA);
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


    private List<BakedQuad> getQuadsMimic(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, ModelData extraData,
                                          @Nullable RenderType renderType) {
        BlockState camo = extraData.get(ShieldingTileEntity.MIMIC);
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
            return model.getQuads(state, side, rand, ModelData.EMPTY, renderType);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private BakedModel getModel(@Nonnull BlockState facadeState) {
        initTextures();
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(facadeState);
    }

    private static final ChunkRenderTypeSet RT_TRANSLUCENT = ChunkRenderTypeSet.of(RenderType.translucent());
    private static final ChunkRenderTypeSet RT_CUTOUT = ChunkRenderTypeSet.of(RenderType.cutout());
    private static final ChunkRenderTypeSet RT_SOLID = ChunkRenderTypeSet.of(RenderType.solid());

    @Override
    @Nonnull
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        if (state.getBlock() == ShieldModule.SHIELDING_SOLID.get()) {
            return RT_SOLID;
        } else if (state.getBlock() == ShieldModule.SHIELDING_TRANSLUCENT.get()) {
            return RT_TRANSLUCENT;
        } else {
            return RT_CUTOUT;
        }
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleIcon() {
        initTextures();
        return shieldfull;
    }
}
