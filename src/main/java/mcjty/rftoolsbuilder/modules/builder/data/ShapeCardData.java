package mcjty.rftoolsbuilder.modules.builder.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.rftoolsbuilder.shapes.Shape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public record ShapeCardData(int channel, ShapeCardDimensions dimensions, boolean tagMatching, boolean solid, Set<String> voiding, Shape shape) {

    public static final int MODE_NONE = 0;
    public static final int MODE_CORNER1 = 1;
    public static final int MODE_CORNER2 = 2;

    public record ShapeCardDimensions(BlockPos dimension, BlockPos offset, int mode, BlockPos corner1, GlobalPos selected) {
    }

    public static final ShapeCardDimensions DEFAULT_DIMENSIONS = new ShapeCardDimensions(new BlockPos(5, 5, 5), BlockPos.ZERO, MODE_NONE, null, null);
    public static final ShapeCardData DEFAULT = new ShapeCardData(-1, DEFAULT_DIMENSIONS, false, true, new HashSet<>(), Shape.SHAPE_BOX);

    public static final Codec<ShapeCardDimensions> DIMENSIONS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("dimension").forGetter(ShapeCardDimensions::dimension),
            BlockPos.CODEC.fieldOf("offset").forGetter(ShapeCardDimensions::offset),
            Codec.INT.fieldOf("mode").forGetter(ShapeCardDimensions::mode),
            BlockPos.CODEC.optionalFieldOf("corner1").forGetter(o -> Optional.ofNullable(o.corner1)),
            GlobalPos.CODEC.optionalFieldOf("selected").forGetter(o -> Optional.ofNullable(o.selected))
    ).apply(instance, (dim, offs, m, c1, sel) -> new ShapeCardDimensions(dim, offs, m, c1.orElse(null), sel.orElse(null))));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapeCardDimensions> DIMENSIONS_STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ShapeCardDimensions::dimension,
            BlockPos.STREAM_CODEC, ShapeCardDimensions::offset,
            ByteBufCodecs.INT, ShapeCardDimensions::mode,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), s -> Optional.ofNullable(s.corner1()),
            ByteBufCodecs.optional(GlobalPos.STREAM_CODEC), s -> Optional.ofNullable(s.selected()),
            (dimension, offset, mode, corner1, selected) -> new ShapeCardDimensions(dimension, offset, mode, corner1.orElse(null), selected.orElse(null)
    ));

    public static final Codec<ShapeCardData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("channel").forGetter(ShapeCardData::channel),
            DIMENSIONS_CODEC.fieldOf("dimensions").forGetter(ShapeCardData::dimensions),
            Codec.BOOL.fieldOf("tagMatching").forGetter(ShapeCardData::tagMatching),
            Codec.BOOL.optionalFieldOf("solid", true).forGetter(ShapeCardData::solid),
            Codec.STRING.listOf().fieldOf("voiding").forGetter(card -> new ArrayList<>(card.voiding)),
            Shape.CODEC.fieldOf("shape").forGetter(ShapeCardData::shape)
    ).apply(instance, (channel, dimensions, tagMatching, solid, voiding, shape) -> new ShapeCardData(channel, dimensions, tagMatching, solid, new HashSet<>(voiding), shape)));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapeCardData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ShapeCardData::channel,
            DIMENSIONS_STREAM_CODEC, ShapeCardData::dimensions,
            ByteBufCodecs.BOOL, ShapeCardData::tagMatching,
            ByteBufCodecs.BOOL, ShapeCardData::solid,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), s -> new ArrayList<>(s.voiding),
            Shape.STREAM_CODEC, ShapeCardData::shape,
            (channel, dimensions, tagMatching, solid, voiding, shape) -> new ShapeCardData(channel, dimensions, tagMatching, solid, new HashSet<>(voiding), shape)
    );

    public ShapeCardData withChannel(int channel) {
        return new ShapeCardData(channel, dimensions, tagMatching, solid, voiding, shape);
    }

    public ShapeCardData withDimension(BlockPos dimension) {
        return new ShapeCardData(channel,
                new ShapeCardDimensions(dimension, this.dimensions.offset, this.dimensions.mode, this.dimensions.corner1, this.dimensions.selected),
                tagMatching, solid, voiding, shape);
    }

    public ShapeCardData withOffset(BlockPos offset) {
        return new ShapeCardData(channel,
                new ShapeCardDimensions(this.dimensions.dimension, offset, this.dimensions.mode, this.dimensions.corner1, this.dimensions.selected),
                tagMatching, solid, voiding, shape);
    }

    public ShapeCardData withMode(int mode) {
        return new ShapeCardData(channel,
                new ShapeCardDimensions(this.dimensions.dimension, this.dimensions.offset, mode, this.dimensions.corner1, this.dimensions.selected),
                tagMatching, solid, voiding, shape);
    }

    public ShapeCardData withCorner(BlockPos corner) {
        return new ShapeCardData(channel,
                new ShapeCardDimensions(this.dimensions.dimension, this.dimensions.offset, this.dimensions.mode, corner, this.dimensions.selected),
                tagMatching, solid, voiding, shape);
    }

    public ShapeCardData withSelected(GlobalPos selected) {
        return new ShapeCardData(channel,
                new ShapeCardDimensions(this.dimensions.dimension, this.dimensions.offset, this.dimensions.mode, this.dimensions.corner1, selected),
                tagMatching, solid, voiding, shape);
    }

    public ShapeCardData withTagMatching(boolean tagMatching) {
        return new ShapeCardData(channel, dimensions, tagMatching, solid, voiding, shape);
    }

    public ShapeCardData withSolid(boolean solid) {
        return new ShapeCardData(channel, dimensions, tagMatching, solid, voiding, shape);
    }

    public ShapeCardData withShape(Shape shape) {
        return new ShapeCardData(channel, dimensions, tagMatching, solid, voiding, shape);
    }

    public ShapeCardData addVoiding(String voiding) {
        Set<String> newVoiding = new HashSet<>(this.voiding);
        newVoiding.add(voiding);
        return new ShapeCardData(channel, dimensions, tagMatching, solid, newVoiding, shape);
    }

    public ShapeCardData withVoiding(Set<String> voiding) {
        return new ShapeCardData(channel, dimensions, tagMatching, solid, voiding, shape);
    }
}
