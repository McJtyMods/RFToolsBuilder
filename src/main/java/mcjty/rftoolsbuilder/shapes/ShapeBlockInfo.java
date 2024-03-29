package mcjty.rftoolsbuilder.shapes;

import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShapeBlockInfo {

    private static final Col COL_DEFAULT = new Col(.5f, .3f, .5f);
    private static final Col COL_LAVA = new Col(0xd4 / 255.0f, 0x5a / 255.0f, 0x12 / 255.0f);
    private static final Col COL_NETHERBRICK = new Col(0x2d / 255.0f, 0x17 / 255.0f, 0x1b / 255.0f);
    private static final Col COL_SCANNER = new Col(0x00 / 255.0f, 0x00 / 255.0f, 0xe2 / 255.0f);

    private static final IBlockRender BD_RAIL = new DefaultRender(.1f, .2f);
    private static final IBlockRender BD_GRASS = new DefaultRender(.1f, .2f);
    private static final IBlockRender BD_TORCH = new DefaultRender(.4f, .7f);
    private static final IBlockRender BD_FLOWER = new DefaultRender(.4f, .6f);
    private static final IBlockRender BD_MUSHROOM = new DefaultRender(.3f, .5f);
    private static final IBlockRender BD_BARS = new DefaultRender(.4f, 1);
    private static final IBlockRender BD_VINE = new DefaultRender(.4f, 1);
    private static final IBlockRender BD_WALL = new DefaultRender(.25f, .9f);
    private static final IBlockRender BD_FENCE = new DefaultRender(.3f, .9f);
    private static final IBlockRender BD_SLAB = new DefaultRender(.05f, .5f);
    private static final IBlockRender BD_SLAB_UPPER = new UpperslabRender(.05f, .5f);
    private static final IBlockRender BD_SNOWLAYER = new DefaultRender(0, .3f);
    private static final IBlockRender BD_FIRE = new DefaultRender(.1f, .3f);
    private static final IBlockRender BD_REDSTONE = new DefaultRender(.3f, .1f);
    private static final IBlockRender BD_CHEST = new DefaultRender(.05f, .8f);
    private static final IBlockRender BD_TRAPDOOR = new DefaultRender(.05f, .1f);
    private static final IBlockRender BD_BUTTON = new DefaultRender(.4f, .1f);

    private final Col col;
    private final IBlockRender render;

    // WARNING! Keep up-to-date together with getBlockRender!
    private static final Set<Block> nonSolidBlocks = new HashSet<>();

    static {
        // @todo 1.14 find a better way!
//        nonSolidBlocks.add(Blocks.TORCH);
//        nonSolidBlocks.add(Blocks.TORCH);
//        nonSolidBlocks.add(Blocks.STONE_SLAB);
//        nonSolidBlocks.add(Blocks.DARK_OAK_SLAB);
//        nonSolidBlocks.add(Blocks.OAK_SLAB);
//        nonSolidBlocks.add(Blocks.ACACIA_SLAB);
//        nonSolidBlocks.add(Blocks.BIRCH_SLAB);
//        nonSolidBlocks.add(Blocks.SPRUCE_SLAB);
//        nonSolidBlocks.add(Blocks.JUNGLE_SLAB);
//        nonSolidBlocks.add(Blocks.PURPUR_SLAB);
//        nonSolidBlocks.add(Blocks.SNOW);
//        nonSolidBlocks.add(Blocks.COBBLESTONE_WALL);
//        nonSolidBlocks.add(Blocks.IRON_BARS);
//        nonSolidBlocks.add(Blocks.LADDER);
//        nonSolidBlocks.add(Blocks.VINE);
//        nonSolidBlocks.add(Blocks.RED_FLOWER);
//        nonSolidBlocks.add(Blocks.YELLOW_FLOWER);
//        nonSolidBlocks.add(Blocks.WHEAT);
//        nonSolidBlocks.add(Blocks.CARROTS);
//        nonSolidBlocks.add(Blocks.POTATOES);
//        nonSolidBlocks.add(Blocks.BEETROOTS);
//        nonSolidBlocks.add(Blocks.TALLGRASS);
//        nonSolidBlocks.add(Blocks.RAIL);
//        nonSolidBlocks.add(Blocks.ACTIVATOR_RAIL);
//        nonSolidBlocks.add(Blocks.DETECTOR_RAIL);
//        nonSolidBlocks.add(Blocks.GOLDEN_RAIL);
//        nonSolidBlocks.add(Blocks.RED_MUSHROOM);
//        nonSolidBlocks.add(Blocks.BROWN_MUSHROOM);
//        nonSolidBlocks.add(Blocks.FIRE);
//        nonSolidBlocks.add(Blocks.REDSTONE_WIRE);
//        nonSolidBlocks.add(Blocks.CHEST);
//        nonSolidBlocks.add(Blocks.TRAPPED_CHEST);
//        nonSolidBlocks.add(Blocks.TRAPDOOR);
//        nonSolidBlocks.add(Blocks.WOODEN_PRESSURE_PLATE);
//        nonSolidBlocks.add(Blocks.STONE_PRESSURE_PLATE);
//        nonSolidBlocks.add(Blocks.ACACIA_FENCE);
//        nonSolidBlocks.add(Blocks.ACACIA_FENCE_GATE);
//        nonSolidBlocks.add(Blocks.BIRCH_FENCE);
//        nonSolidBlocks.add(Blocks.BIRCH_FENCE_GATE);
//        nonSolidBlocks.add(Blocks.DARK_OAK_FENCE);
//        nonSolidBlocks.add(Blocks.DARK_OAK_FENCE_GATE);
//        nonSolidBlocks.add(Blocks.JUNGLE_FENCE);
//        nonSolidBlocks.add(Blocks.JUNGLE_FENCE_GATE);
//        nonSolidBlocks.add(Blocks.OAK_FENCE);
//        nonSolidBlocks.add(Blocks.OAK_FENCE_GATE);
//        nonSolidBlocks.add(Blocks.NETHER_BRICK_FENCE);
//        nonSolidBlocks.add(Blocks.LEVER);
//        nonSolidBlocks.add(Blocks.STONE_BUTTON);
//        nonSolidBlocks.add(Blocks.WOODEN_BUTTON);
    }

    public static boolean isNonSolidBlock(Block b) {
        return nonSolidBlocks.contains(b);
    }

    public ShapeBlockInfo(Col col, IBlockRender render) {
        this.col = col;
        this.render = render;
    }

    // WARNING! Keep up-to-date together with nonSolidBlocks!
    private static IBlockRender getBlockRender(BlockState state) {
        if (state == null) {
            return null;
        }
        IBlockRender render = null;
        Block block = state.getBlock();
        Set<ResourceLocation> tags = block.getTags();

        if (block == Blocks.TORCH || block == Blocks.REDSTONE_TORCH) {
            render = BD_TORCH;
        } else if (tags.contains(BlockTags.SLABS.getName())) {
            if (state.hasProperty(SlabBlock.TYPE) && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM) {
                render = BD_SLAB;
            } else {
                render = BD_SLAB_UPPER;
            }
        } else if (block == Blocks.SNOW) {
            render = BD_SNOWLAYER;
        } else if (tags.contains(BlockTags.WALLS.getName())) {
            render = BD_WALL;
        } else if (block == Blocks.IRON_BARS || block == Blocks.LADDER) {
            render = BD_BARS;
        } else if (block == Blocks.VINE) {
            render = BD_VINE;
        } else if (tags.contains(BlockTags.SMALL_FLOWERS.getName()) ||
                tags.contains(BlockTags.CORAL_PLANTS.getName()) ||
                block == Blocks.WHEAT || block == Blocks.CARROTS ||
                block == Blocks.POTATOES || block == Blocks.BEETROOTS) {
            render = BD_FLOWER;
        } else if (block == Blocks.GRASS) { // Tall grass
            render = BD_GRASS;
        } else if (tags.contains(BlockTags.RAILS.getName())) {
            render = BD_RAIL;
        } else if (block == Blocks.RED_MUSHROOM || block == Blocks.BROWN_MUSHROOM) {
            render = BD_MUSHROOM;
        } else if (block == Blocks.FIRE) {
            render = BD_FIRE;
        } else if (block == Blocks.REDSTONE_WIRE) {
            render = BD_REDSTONE;
        } else if (tags.contains(Tags.Blocks.CHESTS.getName())) {
            render = BD_CHEST;
        } else if (tags.contains(BlockTags.TRAPDOORS.getName()) ||
                tags.contains(BlockTags.WOODEN_PRESSURE_PLATES.getName()) ||
                block == Blocks.OAK_PRESSURE_PLATE || block == Blocks.STONE_PRESSURE_PLATE) {
            render = BD_TRAPDOOR;
        } else if (block == Blocks.LEVER ||
                tags.contains(BlockTags.BUTTONS.getName()) ||
                tags.contains(BlockTags.WOODEN_BUTTONS.getName())) {
            render = BD_BUTTON;
        } else if (
                tags.contains(BlockTags.FENCES.getName()) ||
                tags.contains(BlockTags.WOODEN_FENCES.getName())) {
            render = BD_FENCE;
        }
        return render;
    }

    private static Col getColor(BlockState state) {
        if (state == null) {
            return COL_DEFAULT;
        }
        Col col;
        Block block = state.getBlock();
        // The given world and pos are wrong but they help to avoid crashes for some code
        MaterialColor mapColor = null;
        try {
            mapColor = state.getMapColor(Minecraft.getInstance().level, new BlockPos(0, 0, 0));
        } catch (Exception e) {
            mapColor = MaterialColor.COLOR_RED;
        }
        if (block == Blocks.LAVA) {
            col = COL_LAVA;
        } else if (block == Blocks.NETHER_BRICKS || block == Blocks.NETHER_BRICK_FENCE || block == Blocks.NETHER_BRICK_STAIRS) {
            col = COL_NETHERBRICK;
            // @todo 1.14 scanner
//        } else if (block == BuilderSetup.scannerBlock) {
//            col = COL_SCANNER;
        } else if (block == BuilderModule.SUPPORT.get()) {
            col = COL_DEFAULT;
        } else {
            col = new Col(((mapColor.col >> 16) & 0xff) / 255.0f, ((mapColor.col >> 8) & 0xff) / 255.0f, (mapColor.col & 0xff) / 255.0f);
        }
        float r = col.getR();
        float g = col.getG();
        float b = col.getB();
        if (r * 1.2f > 1.0f) {
            r = 0.99f / 1.2f;
        }
        if (g * 1.2f > 1.0f) {
            g = 0.99f / 1.2f;
        }
        if (b * 1.2f > 1.0f) {
            b = 0.99f / 1.2f;
        }
        col = new Col(r, g, b);
        return col;
    }

    @Nonnull
    public static ShapeBlockInfo getBlockInfo(Map<BlockState, ShapeBlockInfo> palette, BlockState state) {
        ShapeBlockInfo info = palette.get(state);
        if (info != null) {
            return info;
        }
        info = new ShapeBlockInfo(getColor(state), getBlockRender(state));
        palette.put(state, info);
        return info;
    }

    public Col getCol() {
        return col;
    }

    public IBlockRender getRender() {
        return render;
    }

    public boolean isNonSolid() {
        return render != null;
    }

    static class Col {
        private final float r;
        private final float g;
        private final float b;

        public Col(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public float getR() {
            return r;
        }

        public float getG() {
            return g;
        }

        public float getB() {
            return b;
        }
    }

    static interface IBlockRender {
        void render(BufferBuilder buffer, int zoffset, float r, float g, float b);
    }

    static class DefaultRender implements IBlockRender {
        private final float height;
        private final float offset;

        public DefaultRender(float offset, float height) {
            this.height = height;
            this.offset = offset;
        }

        @Override
        public void render(BufferBuilder buffer, int z, float r, float g, float b) {
            float a = 0.5f;
            // Up
            buffer.vertex(offset, height, 1 - offset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();
            buffer.vertex(1 - offset, height, 1 - offset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();
            buffer.vertex(1 - offset, height, offset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();
            buffer.vertex(offset, height, offset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();

            // Down
            buffer.vertex(offset, 0, offset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();
            buffer.vertex(1 - offset, 0, offset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();
            buffer.vertex(1 - offset, 0, 1 - offset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();
            buffer.vertex(offset, 0, 1 - offset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();

            // North
            buffer.vertex(1 - offset, height, offset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();
            buffer.vertex(1 - offset, 0, offset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();
            buffer.vertex(offset, 0, offset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();
            buffer.vertex(offset, height, offset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();

            // South
            buffer.vertex(1 - offset, 0, 1 - offset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();
            buffer.vertex(1 - offset, height, 1 - offset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();
            buffer.vertex(offset, height, 1 - offset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();
            buffer.vertex(offset, 0, 1 - offset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();

            // West
            buffer.vertex(offset, 0, 1 - offset + z).color(r, g, b, a).endVertex();
            buffer.vertex(offset, height, 1 - offset + z).color(r, g, b, a).endVertex();
            buffer.vertex(offset, height, offset + z).color(r, g, b, a).endVertex();
            buffer.vertex(offset, 0, offset + z).color(r, g, b, a).endVertex();

            // East
            buffer.vertex(1 - offset, 0, offset + z).color(r, g, b, a).endVertex();
            buffer.vertex(1 - offset, height, offset + z).color(r, g, b, a).endVertex();
            buffer.vertex(1 - offset, height, 1 - offset + z).color(r, g, b, a).endVertex();
            buffer.vertex(1 - offset, 0, 1 - offset + z).color(r, g, b, a).endVertex();
        }
    }

    static class BlockRender implements IBlockRender {
        private final float height;
        private final float xoffset;
        private final float zoffset;

        public BlockRender(float xoffset, float zoffset, float height) {
            this.height = 1;
            this.xoffset = xoffset;
            this.zoffset = zoffset;
        }

        @Override
        public void render(BufferBuilder buffer, int z, float r, float g, float b) {
            float a = 0.5f;
            // Up
            buffer.vertex(xoffset, height, 1 - zoffset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();
            buffer.vertex(1 - xoffset, height, 1 - zoffset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();
            buffer.vertex(1 - xoffset, height, zoffset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();
            buffer.vertex(xoffset, height, zoffset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();

            // Down
            buffer.vertex(xoffset, 0, zoffset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();
            buffer.vertex(1 - xoffset, 0, zoffset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();
            buffer.vertex(1 - xoffset, 0, 1 - zoffset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();
            buffer.vertex(xoffset, 0, 1 - zoffset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();

            // North
            buffer.vertex(1 - xoffset, height, zoffset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();
            buffer.vertex(1 - xoffset, 0, zoffset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();
            buffer.vertex(xoffset, 0, zoffset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();
            buffer.vertex(xoffset, height, zoffset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();

            // South
            buffer.vertex(1 - xoffset, 0, 1 - zoffset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();
            buffer.vertex(1 - xoffset, height, 1 - zoffset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();
            buffer.vertex(xoffset, height, 1 - zoffset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();
            buffer.vertex(xoffset, 0, 1 - zoffset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();

            // West
            buffer.vertex(xoffset, 0, 1 - zoffset + z).color(r, g, b, a).endVertex();
            buffer.vertex(xoffset, height, 1 - zoffset + z).color(r, g, b, a).endVertex();
            buffer.vertex(xoffset, height, zoffset + z).color(r, g, b, a).endVertex();
            buffer.vertex(xoffset, 0, zoffset + z).color(r, g, b, a).endVertex();

            // East
            buffer.vertex(1 - xoffset, 0, zoffset + z).color(r, g, b, a).endVertex();
            buffer.vertex(1 - xoffset, height, zoffset + z).color(r, g, b, a).endVertex();
            buffer.vertex(1 - xoffset, height, 1 - zoffset + z).color(r, g, b, a).endVertex();
            buffer.vertex(1 - xoffset, 0, 1 - zoffset + z).color(r, g, b, a).endVertex();
        }
    }

    static class UpperslabRender implements IBlockRender {
        private final float height;
        private final float offset;

        public UpperslabRender(float offset, float height) {
            this.height = height;
            this.offset = offset;
        }

        @Override
        public void render(BufferBuilder buffer, int z, float r, float g, float b) {
            float a = 0.5f;
            // Up
            buffer.vertex(offset, height + .5, 1 - offset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();
            buffer.vertex(1 - offset, height + .5, 1 - offset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();
            buffer.vertex(1 - offset, height + .5, offset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();
            buffer.vertex(offset, height + .5, offset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();

            // Down
            buffer.vertex(offset, .5, offset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();
            buffer.vertex(1 - offset, .5, offset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();
            buffer.vertex(1 - offset, .5, 1 - offset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();
            buffer.vertex(offset, .5, 1 - offset + z).color(r * .8f, g * .8f, b * .8f, a).endVertex();

            // North
            buffer.vertex(1 - offset, height + .5, offset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();
            buffer.vertex(1 - offset, .5, offset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();
            buffer.vertex(offset, .5, offset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();
            buffer.vertex(offset, height + .5, offset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();

            // South
            buffer.vertex(1 - offset, .5, 1 - offset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();
            buffer.vertex(1 - offset, height + .5, 1 - offset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();
            buffer.vertex(offset, height + .5, 1 - offset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();
            buffer.vertex(offset, .5, 1 - offset + z).color(r * 1.2f, g * 1.2f, b * 1.2f, a).endVertex();

            // West
            buffer.vertex(offset, .5, 1 - offset + z).color(r, g, b, a).endVertex();
            buffer.vertex(offset, height + .5, 1 - offset + z).color(r, g, b, a).endVertex();
            buffer.vertex(offset, height + .5, offset + z).color(r, g, b, a).endVertex();
            buffer.vertex(offset, .5, offset + z).color(r, g, b, a).endVertex();

            // East
            buffer.vertex(1 - offset, .5, offset + z).color(r, g, b, a).endVertex();
            buffer.vertex(1 - offset, height + .5, offset + z).color(r, g, b, a).endVertex();
            buffer.vertex(1 - offset, height + .5, 1 - offset + z).color(r, g, b, a).endVertex();
            buffer.vertex(1 - offset, .5, 1 - offset + z).color(r, g, b, a).endVertex();
        }
    }
}
