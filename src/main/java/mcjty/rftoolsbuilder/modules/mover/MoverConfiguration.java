package mcjty.rftoolsbuilder.modules.mover;

import net.minecraftforge.common.ForgeConfigSpec;

public class MoverConfiguration {

    public static final String CATEGORY_MOVER = "mover";

    public static ForgeConfigSpec.IntValue MAXENERGY;
    public static ForgeConfigSpec.IntValue RECEIVEPERTICK;
    public static ForgeConfigSpec.IntValue RF_PER_OPERATION;
    public static ForgeConfigSpec.IntValue CALLCARD_RFPERTICK;

    public static void init(ForgeConfigSpec.Builder SERVER_BUILDER, ForgeConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Settings for the mover").push(CATEGORY_MOVER);

        RF_PER_OPERATION = SERVER_BUILDER
                .comment("Amount of RF used per tick while moving")
                .defineInRange("rfPerTickOperation", 100, 0, Integer.MAX_VALUE);
        MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the mover can hold")
                .defineInRange("moverMaxRF", 100000, 0, Integer.MAX_VALUE);
        RECEIVEPERTICK = SERVER_BUILDER
                .comment("RF per tick that the mover can receive")
                .defineInRange("moverRFPerTick", 1000, 0, Integer.MAX_VALUE);
        CALLCARD_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the callcard module")
                .defineInRange("callcardRFPerTick", 0, 0, Integer.MAX_VALUE);

        SERVER_BUILDER.pop();
    }
}
