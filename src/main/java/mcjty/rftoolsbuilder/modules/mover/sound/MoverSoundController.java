package mcjty.rftoolsbuilder.modules.mover.sound;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;


public final class MoverSoundController {

    // Indexed by position of the mover and position of the controller
    private static final Map<Pair<BlockPos, GlobalPos>, MoverSound> SOUNDS = Maps.newHashMap();

    private static Pair<BlockPos, GlobalPos> key(Level level, BlockPos controllerPos, BlockPos mover) {
        return Pair.of(mover, GlobalPos.of(level.dimension(), controllerPos));
    }

    public static void stop(Level level, BlockPos controllerPos, BlockPos mover) {
        Pair<BlockPos, GlobalPos> g = key(level, controllerPos, mover);
        if (SOUNDS.containsKey(g)) {
            AbstractTickableSoundInstance movingSound = SOUNDS.get(g);
            Minecraft.getInstance().getSoundManager().stop(movingSound);
            SOUNDS.remove(g);
        }
    }

    public static void move(Level level, BlockPos controllerPos, BlockPos mover, Vec3 pos) {
        Pair<BlockPos, GlobalPos> g = key(level, controllerPos, mover);
        if (SOUNDS.containsKey(g)) {
            SOUNDS.get(g).setPosition(pos);
        }
    }

    public static void play(Level level, BlockPos controllerPos, BlockPos mover, Vec3 pos) {
        MoverSound sound = new MoverSound(Sounds.MOVER_LOOP.get(), level, mover);
        sound.setPosition(pos);
        stop(level, controllerPos, mover);
        Minecraft.getInstance().getSoundManager().play(sound);
        SOUNDS.put(key(level, controllerPos, mover), sound);
    }

    public static boolean isPlaying(Level worldObj, BlockPos controllerPos, BlockPos mover) {
        MoverSound s = SOUNDS.get(key(worldObj, controllerPos, mover));
        return s != null && s.isSoundType(Sounds.MOVER_LOOP.get());
    }

}
