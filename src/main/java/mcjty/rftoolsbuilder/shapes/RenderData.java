package mcjty.rftoolsbuilder.shapes;

import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.rftoolsbuilder.modules.scanner.ScannerConfiguration;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RenderData {

    public static BufferBuilder vboBuffer = new BufferBuilder(2097152);

    private RenderPlane planes[] = null;
    public String previewMessage = "";
    private long touchTime = 0;
    private long checksum = -1;
    private boolean wantData = true;

    public boolean hasData() {
        if (planes == null) {
            return false;
        }
        for (RenderPlane plane : planes) {
            if (plane != null && plane.vbo != null) {
                return true;
            }
        }
        return false;
    }

    public int getBlockCount() {
        if (planes != null) {
            int cnt = 0;
            for (RenderPlane plane : planes) {
                if (plane != null) {
                    cnt += plane.getCount();
                }
            }
            return cnt;
        }
        return 0;
    }

    public long getChecksum() {
        return checksum;
    }

    public void setChecksum(long checksum) {
        this.checksum = checksum;
    }

    public boolean isWantData() {
        return planes == null || wantData;
    }

    public void setWantData(boolean wantData) {
        this.wantData = wantData;
    }

    public RenderPlane[] getPlanes() {
        return planes;
    }

    public void setPlaneData(@Nullable RenderPlane plane, int offsetY, int dy) {
        if (planes == null) {
            planes = new RenderPlane[dy];
        } else if (planes.length != dy) {
            cleanup();
            planes = new RenderPlane[dy];
        }
        if (plane == null) {
        } else if (planes[offsetY] == null) {
            planes[offsetY] = plane;
        } else {
            planes[offsetY].refreshData(plane);
        }
    }

    public void touch() {
        touchTime = System.currentTimeMillis();
    }

    public boolean tooOld() {
        return touchTime + ScannerConfiguration.clientRenderDataTimeout.get() < System.currentTimeMillis();
    }


    public void cleanup() {
        if (planes != null) {
            for (RenderPlane plane : planes) {
                if (plane != null) {
                    plane.cleanup();
                }
            }
        }
    }

    public void createRenderList(int y) {
        if (planes != null) {
            planes[y].createRenderList();
        }
    }

    public void performRenderToList(int y) {
        if (planes != null) {
            planes[y].performRenderToList();
        }
    }

    private static final Matrix4f IDENTITY = new Matrix4f();

    static {
        IDENTITY.setIdentity();
    }

    public static class RenderElement {
        protected net.minecraft.client.renderer.vertex.VertexBuffer vbo;

        public void cleanup() {
            if (vbo != null) {
                vbo.close();
                vbo = null;
            }
        }

        public void render() {
            if (vbo != null) {
                vbo.bindBuffer();
                GlStateManager.enableClientState(GL11.GL_VERTEX_ARRAY);
                GlStateManager.vertexPointer(3, GL11.GL_FLOAT, 16, 0);
                GlStateManager.enableClientState(GL11.GL_COLOR_ARRAY);
                GlStateManager.colorPointer(4, GL11.GL_UNSIGNED_BYTE, 16, 12);
                vbo.draw(IDENTITY, GL11.GL_QUADS);
                vbo.unbindBuffer();
                GlStateManager.disableClientState(GL11.GL_COLOR_ARRAY);
                GlStateManager.disableClientState(GL11.GL_VERTEX_ARRAY);
            }
        }

        public void createRenderList() {
            vbo = new net.minecraft.client.renderer.vertex.VertexBuffer(DefaultVertexFormats.POSITION_COLOR);
        }

        public void performRenderToList() {
            vboBuffer.finishDrawing();
//            vboBuffer.reset();
            vbo.upload(vboBuffer);
            vboBuffer.reset();
        }
    }

    // A render plane is a horizonal plane of data. It is made out of strips
    public static class RenderPlane extends RenderElement {
        private RenderStrip[] strips;
        private int y;
        private int offsety;
        private int startz;
        private boolean dirty = true;
        private int count = 0;
        private long birthtime;

        public RenderPlane(RenderStrip[] strips, int y, int offsety, int startz, int count) {
            this.strips = strips;
            this.y = y;
            this.offsety = offsety;
            this.startz = startz;
            this.count = count;
            birthtime = System.currentTimeMillis();
        }

        public void refreshData(RenderPlane other) {
            this.strips = other.strips;
            this.y = other.y;
            this.offsety = other.offsety;
            this.startz = other.startz;
            this.count = other.count;
            this.dirty = true;
            birthtime = System.currentTimeMillis();
            super.cleanup();
        }

        public long getBirthtime() {
            return birthtime;
        }

        public int getCount() {
            return count;
        }

        public void markClean() {
            dirty = false;
        }

        public boolean isDirty() {
            return dirty;
        }

        public RenderStrip[] getStrips() {
            return strips;
        }

        public int getOffsety() {
            return offsety;
        }

        public int getY() {
            return y;
        }

        public int getStartz() {
            return startz;
        }


    }

    // A render strip is a single horizontal (on z axis) strip of data
    public static class RenderStrip {
        private final List<Pair<Integer, BlockState>> data = new ArrayList<>();
        private final int x;
        private BlockState last;
        private int cnt = 0;

        public RenderStrip(int x) {
            this.x = x;
        }

        public int getX() {
            return x;
        }

        public List<Pair<Integer, BlockState>> getData() {
            return data;
        }

        public boolean isEmptyAt(int i, Map<BlockState, ShapeBlockInfo> palette) {
            if (i < 0) {
                return true;
            }
            if (i >= data.size()) {
                return true;
            }
            BlockState state = data.get(i).getValue();
            if (ShapeBlockInfo.getBlockInfo(palette, state).isNonSolid()) {
                return true;
            }
            return state == null;
        }

        public void add(BlockState state) {
            if (cnt == 0) {
                last = state;
                cnt = 1;
            } else {
                if (last != state) {
                    data.add(Pair.of(cnt, last));
                    last = state;
                    cnt = 1;
                } else {
                    cnt++;
                }
            }
        }

        public void close() {
            if (cnt > 0) {
                data.add(Pair.of(cnt, last));
                cnt = 0;
            }
        }
    }
}
