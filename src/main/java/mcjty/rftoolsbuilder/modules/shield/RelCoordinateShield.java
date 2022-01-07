package mcjty.rftoolsbuilder.modules.shield;

public record RelCoordinateShield(int dx, int dy, int dz, int state) {

    public boolean matches(int dx, int dy, int dz) {
        return dx == this.dx && dy == this.dy && dz == this.dz;
    }
}
