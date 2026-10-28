package mcjty.rftoolsbuilder.modules.shield.client;

import java.util.*;
import mcjty.lib.client.AbstractDynamicBakedModel;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import mcjty.rftoolsbuilder.modules.shield.ShieldRenderingMode;
import mcjty.rftoolsbuilder.modules.shield.ShieldTexture;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingBlock;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShieldBakedModel extends AbstractDynamicBakedModel {

    public static final Identifier TEXTURE_TRANSPARENT = Identifier.fromNamespaceAndPath(RFToolsBuilder.MODID, "block/shield/shieldtransparent");
    public static final Identifier TEXTURE_FULL = Identifier.fromNamespaceAndPath(RFToolsBuilder.MODID, "block/shield/shieldfull");

    private Map<ShieldTexture, TextureAtlasSprite[]> shields;
    private TextureAtlasSprite shieldtransparent;
    private TextureAtlasSprite shieldfull;

    private synchronized void initTextures() {
        if (shields == null) {
            shields = new HashMap<>();
            for (ShieldTexture texture : ShieldTexture.values()) {
                TextureAtlasSprite[] sprites = new TextureAtlasSprite[4];
                sprites[0] = getTexture(Identifier.fromNamespaceAndPath(RFToolsBuilder.MODID, "block/" + texture.getPath() + "/shield0"));
                sprites[1] = getTexture(Identifier.fromNamespaceAndPath(RFToolsBuilder.MODID, "block/" + texture.getPath() + "/shield1"));
                sprites[2] = getTexture(Identifier.fromNamespaceAndPath(RFToolsBuilder.MODID, "block/" + texture.getPath() + "/shield2"));
                sprites[3] = getTexture(Identifier.fromNamespaceAndPath(RFToolsBuilder.MODID, "block/" + texture.getPath() + "/shield3"));
                shields.put(texture, sprites);
            }
            shieldtransparent = getTexture(TEXTURE_TRANSPARENT);
            shieldfull = getTexture(TEXTURE_FULL);
        }
    }

    @Override
    public void collectParts(BlockAndTintGetter world, BlockPos pos,
            BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        var modelData = world.getModelData(pos);
        ShieldRenderData renderData = modelData.get(ShieldingTileEntity.RENDER_DATA);
        if (renderData == null) {
            renderData = ShieldingTileEntity.DEFAULT_RENDER_DATA;
        }
        initTextures();
        ShieldRenderingMode mode = state.getValue(ShieldingBlock.RENDER_MODE);
        if (mode == ShieldRenderingMode.INVISIBLE) return;
        if (mode == ShieldRenderingMode.MIMIC) {
            BlockState mimic = modelData.get(ShieldingTileEntity.MIMIC);
            if (mimic != null && !(mimic.getBlock() instanceof ShieldingBlock)) {
                Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(mimic).collectParts(world, pos, mimic, random, parts);
            }
            return;
        }
        QuadCollection.Builder quads = new QuadCollection.Builder();
        for (Direction side : Direction.values()) {
            List<BakedQuad> faces = mode == ShieldRenderingMode.SHIELD ? getQuadsShield(side, pos, renderData)
                    : getQuadsTextured(side, mode == ShieldRenderingMode.TRANSP ? shieldtransparent : shieldfull, renderData);
            // Directional faces let the renderer apply ShieldingBlock.skipRendering.
            faces.forEach(quad -> quads.addCulledFace(side, quad));
        }
        parts.add(new SimpleModelWrapper(quads.build(), true, particleMaterial()));
    }

    private List<BakedQuad> getQuadsShield(@Nullable Direction side, BlockPos pos, ShieldRenderData renderData) {
        List<BakedQuad> quads = new ArrayList<>();
        if (side != null) {
            Integer iconTopdown = (pos.getZ() & 1) * 2 + (pos.getX() & 1);
            Integer iconSide = (pos.getY() & 1) * 2 + ((pos.getX() + pos.getZ()) & 1);
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

    private List<BakedQuad> getQuadsTextured(@Nullable Direction side, TextureAtlasSprite texture, ShieldRenderData renderData) {
        List<BakedQuad> quads = new ArrayList<>();
        if (side != null) {
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


    @Override
    public Material.Baked particleMaterial() {
        initTextures();
        return new Material.Baked(shieldfull, false);
    }
}
