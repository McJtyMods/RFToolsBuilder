package mcjty.rftoolsbuilder.modules.builder;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class SpaceChamberRepository extends AbstractWorldData<SpaceChamberRepository> {

    private static final String SPACECHAMBER_CHANNELS_NAME = "RFToolsSpaceChambers";

    private int lastId = 0;

    private final Map<Integer,SpaceChamberChannel> channels = new HashMap<>();

    public SpaceChamberRepository() {
    }

    public SpaceChamberRepository(CompoundTag tag) {
        ListTag lst = tag.getList("channels", Tag.TAG_COMPOUND);
        for (int i = 0 ; i < lst.size() ; i++) {
            CompoundTag tc = lst.getCompound(i);
            int channel = tc.getInt("channel");

            SpaceChamberChannel value = new SpaceChamberChannel();
            value.setDimension(LevelTools.getId(tc.getString("dimension")));
            value.setMinCorner(BlockPosTools.read(tc, "minCorner"));
            value.setMaxCorner(BlockPosTools.read(tc, "maxCorner"));
            channels.put(channel, value);
        }
        lastId = tag.getInt("lastId");
    }

    public static SpaceChamberRepository get(Level world) {
        return getData(world, SpaceChamberRepository::new, SpaceChamberRepository::new, SPACECHAMBER_CHANNELS_NAME);
    }

    public SpaceChamberChannel getOrCreateChannel(int id) {
        SpaceChamberChannel channel = channels.get(id);
        if (channel == null) {
            channel = new SpaceChamberChannel();
            channels.put(id, channel);
        }
        return channel;
    }

    public SpaceChamberChannel getChannel(int id) {
        return channels.get(id);
    }

    public void deleteChannel(int id) {
        channels.remove(id);
    }

    public int newChannel() {
        lastId++;
        return lastId;
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag tagCompound) {
        ListTag lst = new ListTag();
        for (Map.Entry<Integer, SpaceChamberChannel> entry : channels.entrySet()) {
            CompoundTag tc = new CompoundTag();
            tc.putInt("channel", entry.getKey());
            tc.putString("dimension", entry.getValue().getDimension().location().toString());
            BlockPosTools.write(tc, "minCorner", entry.getValue().getMinCorner());
            BlockPosTools.write(tc, "maxCorner", entry.getValue().getMaxCorner());
            lst.add(tc);
        }
        tagCompound.put("channels", lst);
        tagCompound.putInt("lastId", lastId);
        return tagCompound;
    }

    public static class SpaceChamberChannel {
        private ResourceKey<Level> dimension;
        private BlockPos minCorner = null;
        private BlockPos maxCorner = null;

        public ResourceKey<Level> getDimension() {
            return dimension;
        }

        public void setDimension(ResourceKey<Level> dimension) {
            this.dimension = dimension;
        }

        public BlockPos getMinCorner() {
            return minCorner;
        }

        public void setMinCorner(BlockPos minCorner) {
            this.minCorner = minCorner;
        }

        public BlockPos getMaxCorner() {
            return maxCorner;
        }

        public void setMaxCorner(BlockPos maxCorner) {
            this.maxCorner = maxCorner;
        }
    }
}
