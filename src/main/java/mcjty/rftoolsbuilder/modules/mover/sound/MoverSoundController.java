package mcjty.rftoolsbuilder.modules.mover.sound;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.sounds.SoundEvent;
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

    public static void stopSound(Level worldObj, BlockPos controllerPos, BlockPos mover) {
        Pair<BlockPos, GlobalPos> g = key(worldObj, controllerPos, mover);
        if (SOUNDS.containsKey(g)) {
            AbstractTickableSoundInstance movingSound = SOUNDS.get(g);
            Minecraft.getInstance().getSoundManager().stop(movingSound);
            SOUNDS.remove(g);
        }
    }

    private static void playSound(Level worldObj, BlockPos controllerPos, BlockPos mover, Vec3 pos, SoundEvent soundType) {
        MoverSound sound = new MoverSound(soundType, worldObj, mover);
        sound.setPosition(pos);
        stopSound(worldObj, controllerPos, mover);
        Minecraft.getInstance().getSoundManager().play(sound);
        SOUNDS.put(key(worldObj, controllerPos, mover), sound);
    }

    public static void moveSound(Level level, BlockPos controllerPos, BlockPos mover, Vec3 pos) {
        Pair<BlockPos, GlobalPos> g = key(level, controllerPos, mover);
        if (SOUNDS.containsKey(g)) {
            SOUNDS.get(g).setPosition(pos);
        }
    }


    public static void playStartup(Level worldObj, BlockPos controllerPos, BlockPos mover, Vec3 pos) {
        playSound(worldObj, controllerPos, mover, pos, Sounds.ELEVATOR_START.get());
    }

    public static void playLoop(Level worldObj, BlockPos controllerPos, BlockPos mover, Vec3 pos) {
        playSound(worldObj, controllerPos, mover, pos, Sounds.ELEVATOR_LOOP.get());
    }

    public static void playShutdown(Level worldObj, BlockPos controllerPos, BlockPos mover, Vec3 pos) {
        playSound(worldObj, controllerPos, mover, pos, Sounds.ELEVATOR_STOP.get());
    }

    public static boolean isStartupPlaying(Level worldObj, BlockPos controllerPos, BlockPos mover) {
        return isSoundTypePlayingAt(Sounds.ELEVATOR_START.get(), worldObj, controllerPos, mover);
    }

    public static boolean isLoopPlaying(Level worldObj, BlockPos controllerPos, BlockPos mover) {
        return isSoundTypePlayingAt(Sounds.ELEVATOR_LOOP.get(), worldObj, controllerPos, mover);
    }

    public static boolean isShutdownPlaying(Level worldObj, BlockPos controllerPos, BlockPos mover) {
        return isSoundTypePlayingAt(Sounds.ELEVATOR_STOP.get(), worldObj, controllerPos, mover);
    }

    private static boolean isSoundTypePlayingAt(SoundEvent event, Level world, BlockPos controllerPos, BlockPos mover) {
        MoverSound s = getSoundAt(world, controllerPos, mover);
        return s != null && s.isSoundType(event);
    }

    private static MoverSound getSoundAt(Level world, BlockPos controllerPos, BlockPos mover) {
        return SOUNDS.get(key(world, controllerPos, mover));
    }

}
