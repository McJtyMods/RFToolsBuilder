package mcjty.rftoolsbuilder.modules.shield.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolsbuilder.modules.shield.DamageTypeMode;
import mcjty.rftoolsbuilder.modules.shield.ShieldRenderingMode;
import mcjty.rftoolsbuilder.modules.shield.ShieldTexture;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ShieldData(ShieldRenderingMode renderMode, ShieldTexture shieldTexture, DamageTypeMode damageMode, boolean blockLight, int shieldColor) {

    public static final ShieldData DEFAULT = new ShieldData(ShieldRenderingMode.SHIELD, ShieldTexture.SHIELD, DamageTypeMode.DAMAGETYPE_GENERIC, false, 0x96ffc8);

    public static final Codec<ShieldData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ShieldRenderingMode.CODEC.fieldOf("render").forGetter(ShieldData::renderMode),
            ShieldTexture.CODEC.fieldOf("texture").forGetter(ShieldData::shieldTexture),
            DamageTypeMode.CODEC.fieldOf("damage").forGetter(ShieldData::damageMode),
            Codec.BOOL.fieldOf("blocklight").forGetter(ShieldData::blockLight),
            Codec.INT.fieldOf("color").forGetter(ShieldData::shieldColor)
    ).apply(instance, ShieldData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShieldData> STREAM_CODEC = StreamCodec.composite(
            ShieldRenderingMode.STREAM_CODEC, ShieldData::renderMode,
            ShieldTexture.STREAM_CODEC, ShieldData::shieldTexture,
            DamageTypeMode.STREAM_CODEC, ShieldData::damageMode,
            ByteBufCodecs.BOOL, ShieldData::blockLight,
            ByteBufCodecs.INT, ShieldData::shieldColor,
            ShieldData::new);

    public ShieldData withRenderMode(ShieldRenderingMode renderMode) {
        return new ShieldData(renderMode, shieldTexture, damageMode, blockLight, shieldColor);
    }

    public ShieldData withShieldTexture(ShieldTexture shieldTexture) {
        return new ShieldData(renderMode, shieldTexture, damageMode, blockLight, shieldColor);
    }

    public ShieldData withDamageMode(DamageTypeMode damageMode) {
        return new ShieldData(renderMode, shieldTexture, damageMode, blockLight, shieldColor);
    }

    public ShieldData withBlockLight(boolean blockLight) {
        return new ShieldData(renderMode, shieldTexture, damageMode, blockLight, shieldColor);
    }

    public ShieldData withShieldColor(int shieldColor) {
        return new ShieldData(renderMode, shieldTexture, damageMode, blockLight, shieldColor);
    }
}
