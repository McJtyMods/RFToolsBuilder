package mcjty.rftoolsbuilder.modules.shield.client;

import mcjty.rftoolsbuilder.modules.shield.ShieldTexture;

public class ShieldRenderData {

    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;
    private final ShieldTexture shieldTexture;

    public ShieldRenderData(float red, float green, float blue, float alpha, ShieldTexture shieldTexture) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.shieldTexture = shieldTexture;
    }

    public float getRed() {
        return red;
    }

    public float getGreen() {
        return green;
    }

    public float getBlue() {
        return blue;
    }

    public float getAlpha() {
        return alpha;
    }

    public ShieldTexture getShieldTexture() {
        return shieldTexture;
    }
}
