package mcjty.rftoolsbuilder.modules.mover.sound;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;

import java.util.Map;


public final class MoverSoundController {

    private static final Map<GlobalPos, MoverSound> SOUNDS = Maps.newHashMap();

    public static void stopSound(Level worldObj, BlockPos controllerPos) {
        GlobalPos g = GlobalPos.of(worldObj.dimension(), controllerPos);
        if (SOUNDS.containsKey(g)) {
            AbstractTickableSoundInstance movingSound = SOUNDS.get(g);
            Minecraft.getInstance().getSoundManager().stop(movingSound);
            SOUNDS.remove(g);
        }
    }

    private static void playSound(Level worldObj, BlockPos controllerPos, BlockPos pos, SoundEvent soundType) {
        MoverSound sound = new MoverSound(soundType, worldObj, pos);
        stopSound(worldObj, controllerPos);
        Minecraft.getInstance().getSoundManager().play(sound);
        GlobalPos g = GlobalPos.of(worldObj.dimension(), controllerPos);
        SOUNDS.put(g, sound);
    }


    public static void playStartup(Level worldObj, BlockPos controllerPos, BlockPos pos) {
        playSound(worldObj, controllerPos, pos, Sounds.ELEVATOR_START.get());
    }

    public static void playLoop(Level worldObj, BlockPos controllerPos, BlockPos pos) {
        playSound(worldObj, controllerPos, pos, Sounds.ELEVATOR_LOOP.get());
    }

    public static void playShutdown(Level worldObj, BlockPos controllerPos, BlockPos pos) {
        playSound(worldObj, controllerPos, pos, Sounds.ELEVATOR_STOP.get());
    }

    public static boolean isStartupPlaying(Level worldObj, BlockPos controllerPos) {
        return isSoundTypePlayingAt(Sounds.ELEVATOR_START.get(), worldObj, controllerPos);
    }

    public static boolean isLoopPlaying(Level worldObj, BlockPos controllerPos) {
        return isSoundTypePlayingAt(Sounds.ELEVATOR_LOOP.get(), worldObj, controllerPos);
    }

    public static boolean isShutdownPlaying(Level worldObj, BlockPos controllerPos) {
        return isSoundTypePlayingAt(Sounds.ELEVATOR_STOP.get(), worldObj, controllerPos);
    }

    private static boolean isSoundTypePlayingAt(SoundEvent event, Level world, BlockPos controllerPos) {
        MoverSound s = getSoundAt(world, controllerPos);
        return s != null && s.isSoundType(event);
    }

    private static MoverSound getSoundAt(Level world, BlockPos controllerPos) {
        return SOUNDS.get(GlobalPos.of(world.dimension(), controllerPos));
    }

}
