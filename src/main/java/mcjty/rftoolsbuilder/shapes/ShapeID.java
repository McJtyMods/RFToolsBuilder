package mcjty.rftoolsbuilder.shapes;

import mcjty.lib.varia.WorldTools;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/// ID to identify a shape for a player/projector/scanner/...
public final class ShapeID {
    private final RegistryKey<World> dimension;
    @Nullable private final BlockPos pos;     // null if there is a scanId (shapecard in hand or projector)
    private final int scanId;
    private final boolean grayscale;
    private final boolean solid;

    public ShapeID(RegistryKey<World> dimension, BlockPos pos, int scanId, boolean grayscale, boolean solid) {
        this.dimension = dimension;
        this.pos = pos;
        this.scanId = scanId;
        this.grayscale = grayscale;
        this.solid = solid;
    }

    public ShapeID(PacketBuffer buf) {
        RegistryKey<World> dim = World.OVERWORLD;
        BlockPos p = null;
        if (buf.readBoolean()) {
            dim = WorldTools.getId(buf.readResourceLocation());
            p = buf.readBlockPos();
        }
        scanId = buf.readInt();
        grayscale = buf.readBoolean();
        solid = buf.readBoolean();
        pos = p;
        dimension = dim;
    }

    public void toBytes(PacketBuffer buf) {
        if (getPos() == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeResourceLocation(getDimension().location());
            buf.writeBlockPos(getPos());
        }
        buf.writeInt(scanId);
        buf.writeBoolean(grayscale);
        buf.writeBoolean(solid);
    }

    public RegistryKey<World> getDimension() {
        return dimension;
    }

    @Nullable
    public BlockPos getPos() {
        return pos;
    }

    public int getScanId() {
        return scanId;
    }

    public boolean isGrayscale() {
        return grayscale;
    }

    public boolean isSolid() {
        return solid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShapeID shapeID = (ShapeID) o;

        if (!dimension.equals(shapeID.dimension)) return false;
        if (scanId != shapeID.scanId) return false;
        if (grayscale != shapeID.grayscale) return false;
        if (solid != shapeID.solid) return false;
        return pos != null ? pos.equals(shapeID.pos) : shapeID.pos == null;

    }

    @Override
    public int hashCode() {
        int result = dimension.hashCode();
        result = 31 * result + (pos != null ? pos.hashCode() : 0);
        result = 31 * result + scanId;
        result = 31 * result + (grayscale ? 1 : 0);
        result = 31 * result + (solid ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ShapeID{" +
                "dimension=" + dimension +
                ", pos=" + pos +
                ", scanId=" + scanId +
                ", grayscale=" + grayscale +
                ", solid=" + solid +
                '}';
    }
}
