package mcjty.rftoolsbuilder.tools;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;

/** Keeps the Builder's existing NBT layouts while using the current codec APIs. */
public final class BuilderSerialization {
    private BuilderSerialization() { }

    public static CompoundTag saveItem(ItemStack stack, HolderLookup.Provider provider) {
        return (CompoundTag) ItemStack.OPTIONAL_CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
    }

    public static ItemStack loadItem(HolderLookup.Provider provider, CompoundTag tag) {
        return ItemStack.OPTIONAL_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
    }

    public static ValueInput input(CompoundTag tag, HolderLookup.Provider provider) {
        return TagValueInput.create(ProblemReporter.DISCARDING, provider, tag);
    }

    public static CompoundTag saveEntity(Entity entity) {
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());
        entity.saveWithoutId(output);
        return output.buildResult();
    }
}
