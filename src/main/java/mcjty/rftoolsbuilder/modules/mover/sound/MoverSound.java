package mcjty.rftoolsbuilder.modules.mover.sound;

import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public class MoverSound extends AbstractTickableSoundInstance {

    public MoverSound(SoundEvent event, Level world, BlockPos pos) {
        super(event, SoundSource.BLOCKS, world.random);
        this.world = world;
        this.pos = pos;
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.attenuation = Attenuation.LINEAR;
        this.looping = true;
        this.delay = 0;
        this.loop = event == Sounds.ELEVATOR_LOOP.get();
        this.sound = event;
        this.relative = false;
    }

    private final Level world;
    private final boolean loop;
    private final SoundEvent sound;
    private BlockPos pos;


    @Override
    public void tick() {
        Block block = world.getBlockState(pos).getBlock();
        if (block != MoverModule.MOVER.get()) {
            stop();
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        xxx todo use pos here
        double distance = Math.sqrt(this.pos.distToCenterSqr(player.getX(), player.getY(), player.getZ()));
        if (distance > 20) {
            volume = 0;
        } else {
//            volume = (float) (GeneratorConfig.BASE_GENERATOR_VOLUME.get() * (20-distance)/20.0);
            volume = (float) (1.0 * (20-distance)/20.0);
        }
    }

    protected boolean isSoundType(SoundEvent event){
        return sound == event;
    }

    public void setPosition(Vec3 pos) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
    }
}
