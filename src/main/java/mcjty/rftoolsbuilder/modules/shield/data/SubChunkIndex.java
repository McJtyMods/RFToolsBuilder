package mcjty.rftoolsbuilder.modules.shield.data;

import java.util.Objects;

public class SubChunkIndex {
    private final int sx;
    private final int sy;
    private final int sz;

    public SubChunkIndex(int sx, int sy, int sz) {
        this.sx = sx;
        this.sy = sy;
        this.sz = sz;
    }

    public int getSx() {
        return sx;
    }

    public int getSy() {
        return sy;
    }

    public int getSz() {
        return sz;
    }

    public SubChunkIndex offset(int dx, int dy, int dz) {
        return new SubChunkIndex(sx + dx, sy + dy, sz + dz);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubChunkIndex that = (SubChunkIndex) o;
        return sx == that.sx &&
                sy == that.sy &&
                sz == that.sz;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sx, sy, sz);
    }
}
