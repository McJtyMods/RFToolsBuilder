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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record ShapeCardData(int channel, ShapeCardDimensions dimensions, boolean tagMatching, boolean solid,
                            Set<String> voiding, Shape shape, int scanId, ShapeModifierData modifier,
                            Optional<ResourceLocation> ghostBlock, List<ShapeCardChild> children) {

    public static final int MODE_NONE = 0;
    public static final int MODE_CORNER1 = 1;
    public static final int MODE_CORNER2 = 2;

    public record ShapeCardDimensions(BlockPos dimension, BlockPos offset, int mode, BlockPos corner1, GlobalPos selected) {
    }

    public record ShapeModifierData(String operation, boolean flipY, String rotation) {
        public static final ShapeModifierData DEFAULT = new ShapeModifierData("U", false, "0");

        public static final Codec<ShapeModifierData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("operation").forGetter(ShapeModifierData::operation),
                Codec.BOOL.fieldOf("flipY").forGetter(ShapeModifierData::flipY),
                Codec.STRING.fieldOf("rotation").forGetter(ShapeModifierData::rotation)
        ).apply(instance, ShapeModifierData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ShapeModifierData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, ShapeModifierData::operation,
                ByteBufCodecs.BOOL, ShapeModifierData::flipY,
                ByteBufCodecs.STRING_UTF8, ShapeModifierData::rotation,
                ShapeModifierData::new);
    }

    public record ShapeCardChild(ItemStack stack, ShapeModifierData modifier, Optional<ResourceLocation> ghostBlock) {
        public static final Codec<ShapeCardChild> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.CODEC.fieldOf("stack").forGetter(ShapeCardChild::stack),
                ShapeModifierData.CODEC.optionalFieldOf("modifier", ShapeModifierData.DEFAULT).forGetter(ShapeCardChild::modifier),
                ResourceLocation.CODEC.optionalFieldOf("ghostBlock").forGetter(ShapeCardChild::ghostBlock)
        ).apply(instance, ShapeCardChild::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ShapeCardChild> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, ShapeCardChild::stack,
                ShapeModifierData.STREAM_CODEC, ShapeCardChild::modifier,
                ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), ShapeCardChild::ghostBlock,
                ShapeCardChild::new);
    }

    public static final ShapeCardDimensions DEFAULT_DIMENSIONS = new ShapeCardDimensions(new BlockPos(5, 5, 5), BlockPos.ZERO, MODE_NONE, null, null);
    public static final ShapeCardData DEFAULT = new ShapeCardData(-1, DEFAULT_DIMENSIONS, false, true, new HashSet<>(),
            Shape.SHAPE_BOX, 0, ShapeModifierData.DEFAULT, Optional.empty(), List.of());

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
            Shape.CODEC.fieldOf("shape").forGetter(ShapeCardData::shape),
            Codec.INT.optionalFieldOf("scanId", 0).forGetter(ShapeCardData::scanId),
            ShapeModifierData.CODEC.optionalFieldOf("modifier", ShapeModifierData.DEFAULT).forGetter(ShapeCardData::modifier),
            ResourceLocation.CODEC.optionalFieldOf("ghostBlock").forGetter(ShapeCardData::ghostBlock),
            ShapeCardChild.CODEC.listOf().optionalFieldOf("children", List.of()).forGetter(ShapeCardData::children)
    ).apply(instance, (channel, dimensions, tagMatching, solid, voiding, shape, scanId, modifier, ghostBlock, children) ->
            new ShapeCardData(channel, dimensions, tagMatching, solid, new HashSet<>(voiding), shape, scanId, modifier, ghostBlock, List.copyOf(children))));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapeCardData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeVarInt(data.channel);
                DIMENSIONS_STREAM_CODEC.encode(buf, data.dimensions);
                buf.writeBoolean(data.tagMatching);
                buf.writeBoolean(data.solid);
                buf.writeVarInt(data.voiding.size());
                for (String voiding : data.voiding) {
                    buf.writeUtf(voiding);
                }
                Shape.STREAM_CODEC.encode(buf, data.shape);
                buf.writeVarInt(data.scanId);
                ShapeModifierData.STREAM_CODEC.encode(buf, data.modifier);
                buf.writeOptional(data.ghostBlock, (b, value) -> b.writeResourceLocation(value));
                buf.writeVarInt(data.children.size());
                for (ShapeCardChild child : data.children) {
                    ShapeCardChild.STREAM_CODEC.encode(buf, child);
                }
            },
            buf -> {
                int channel = buf.readVarInt();
                ShapeCardDimensions dimensions = DIMENSIONS_STREAM_CODEC.decode(buf);
                boolean tagMatching = buf.readBoolean();
                boolean solid = buf.readBoolean();
                int voidingSize = buf.readVarInt();
                Set<String> voiding = new HashSet<>();
                for (int i = 0; i < voidingSize; i++) {
                    voiding.add(buf.readUtf());
                }
                Shape shape = Shape.STREAM_CODEC.decode(buf);
                int scanId = buf.readVarInt();
                ShapeModifierData modifier = ShapeModifierData.STREAM_CODEC.decode(buf);
                Optional<ResourceLocation> ghostBlock = buf.readOptional(b -> b.readResourceLocation());
                int childSize = buf.readVarInt();
                List<ShapeCardChild> children = new ArrayList<>(childSize);
                for (int i = 0; i < childSize; i++) {
                    children.add(ShapeCardChild.STREAM_CODEC.decode(buf));
                }
                return new ShapeCardData(channel, dimensions, tagMatching, solid, voiding, shape, scanId, modifier, ghostBlock, List.copyOf(children));
            });

    public ShapeCardData withChannel(int channel) {
        return new ShapeCardData(channel, dimensions, tagMatching, solid, voiding, shape, scanId, modifier, ghostBlock, children);
    }

    public ShapeCardData withDimension(BlockPos dimension) {
        return new ShapeCardData(channel,
                new ShapeCardDimensions(dimension, this.dimensions.offset, this.dimensions.mode, this.dimensions.corner1, this.dimensions.selected),
                tagMatching, solid, voiding, shape, scanId, modifier, ghostBlock, children);
    }

    public ShapeCardData withOffset(BlockPos offset) {
        return new ShapeCardData(channel,
                new ShapeCardDimensions(this.dimensions.dimension, offset, this.dimensions.mode, this.dimensions.corner1, this.dimensions.selected),
                tagMatching, solid, voiding, shape, scanId, modifier, ghostBlock, children);
    }

    public ShapeCardData withMode(int mode) {
        return new ShapeCardData(channel,
                new ShapeCardDimensions(this.dimensions.dimension, this.dimensions.offset, mode, this.dimensions.corner1, this.dimensions.selected),
                tagMatching, solid, voiding, shape, scanId, modifier, ghostBlock, children);
    }

    public ShapeCardData withCorner(BlockPos corner) {
        return new ShapeCardData(channel,
                new ShapeCardDimensions(this.dimensions.dimension, this.dimensions.offset, this.dimensions.mode, corner, this.dimensions.selected),
                tagMatching, solid, voiding, shape, scanId, modifier, ghostBlock, children);
    }

    public ShapeCardData withSelected(GlobalPos selected) {
        return new ShapeCardData(channel,
                new ShapeCardDimensions(this.dimensions.dimension, this.dimensions.offset, this.dimensions.mode, this.dimensions.corner1, selected),
                tagMatching, solid, voiding, shape, scanId, modifier, ghostBlock, children);
    }

    public ShapeCardData withTagMatching(boolean tagMatching) {
        return new ShapeCardData(channel, dimensions, tagMatching, solid, voiding, shape, scanId, modifier, ghostBlock, children);
    }

    public ShapeCardData withSolid(boolean solid) {
        return new ShapeCardData(channel, dimensions, tagMatching, solid, voiding, shape, scanId, modifier, ghostBlock, children);
    }

    public ShapeCardData withShape(Shape shape) {
        return new ShapeCardData(channel, dimensions, tagMatching, solid, voiding, shape, scanId, modifier, ghostBlock, children);
    }

    public ShapeCardData addVoiding(String voiding) {
        Set<String> newVoiding = new HashSet<>(this.voiding);
        newVoiding.add(voiding);
        return new ShapeCardData(channel, dimensions, tagMatching, solid, newVoiding, shape, scanId, modifier, ghostBlock, children);
    }

    public ShapeCardData withVoiding(Set<String> voiding) {
        return new ShapeCardData(channel, dimensions, tagMatching, solid, voiding, shape, scanId, modifier, ghostBlock, children);
    }

    public ShapeCardData withScanId(int scanId) {
        return new ShapeCardData(channel, dimensions, tagMatching, solid, voiding, shape, scanId, modifier, ghostBlock, children);
    }

    public ShapeCardData withModifier(ShapeModifierData modifier) {
        return new ShapeCardData(channel, dimensions, tagMatching, solid, voiding, shape, scanId, modifier, ghostBlock, children);
    }

    public ShapeCardData withGhostBlock(Optional<ResourceLocation> ghostBlock) {
        return new ShapeCardData(channel, dimensions, tagMatching, solid, voiding, shape, scanId, modifier, ghostBlock, children);
    }

    public ShapeCardData withChildren(List<ShapeCardChild> children) {
        return new ShapeCardData(channel, dimensions, tagMatching, solid, voiding, shape, scanId, modifier, ghostBlock, List.copyOf(children));
    }
}
