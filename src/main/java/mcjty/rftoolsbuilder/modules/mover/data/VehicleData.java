package mcjty.rftoolsbuilder.modules.mover.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;

public record VehicleData(List<StateWithCount> states, String name, BlockPos desiredPos, String desiredPosName) {

    public record StateWithCount(BlockState state, List<Integer> positions) {}

    public static final VehicleData DEFAULT = new VehicleData(Collections.emptyList(), "", BlockPos.ZERO, "");

    private static final Codec<StateWithCount> STATE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("state").forGetter(StateWithCount::state),
            Codec.INT.listOf().fieldOf("positions").forGetter(StateWithCount::positions)
    ).apply(instance, StateWithCount::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, StateWithCount> STATE_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), StateWithCount::state,
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()), StateWithCount::positions,
            StateWithCount::new);

    public static final Codec<VehicleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            STATE_CODEC.listOf().fieldOf("states").forGetter(VehicleData::states),
            Codec.STRING.fieldOf("name").forGetter(VehicleData::name),
            BlockPos.CODEC.fieldOf("desiredPos").forGetter(VehicleData::desiredPos),
            Codec.STRING.fieldOf("desiredPosName").forGetter(VehicleData::desiredPosName)
    ).apply(instance, VehicleData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, VehicleData> STREAM_CODEC = StreamCodec.composite(
            STATE_STREAM_CODEC.apply(ByteBufCodecs.list()), VehicleData::states,
            ByteBufCodecs.STRING_UTF8, VehicleData::name,
            BlockPos.STREAM_CODEC, VehicleData::desiredPos,
            ByteBufCodecs.STRING_UTF8, VehicleData::desiredPosName,
            VehicleData::new);

    public VehicleData withStates(List<StateWithCount> states) {
        return new VehicleData(states, name, desiredPos, desiredPosName);
    }

    public VehicleData withName(String name) {
        return new VehicleData(states, name, desiredPos, desiredPosName);
    }

    public VehicleData withDesiredPos(BlockPos desiredPos) {
        return new VehicleData(states, name, desiredPos, desiredPosName);
    }

    public VehicleData withDesiredPosName(String desiredPosName) {
        return new VehicleData(states, name, desiredPos, desiredPosName);
    }
}
