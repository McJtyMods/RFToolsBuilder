package mcjty.rftoolsbuilder.shapes;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import mcjty.rftoolsbuilder.modules.scanner.ScannerConfiguration;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RenderData {
    public static BufferBuilder vboBuffer = new BufferBuilder(2097152);

    private RenderPlane[] planes = null;
    public String previewMessage = "";
    private long touchTime = 0;
    private long checksum = -1;
    private boolean wantData = true;
    private boolean requestInFlight = false;
    private long requestSentAt = 0L;

    private static final long REQUEST_TIMEOUT_MS = 5000L;

    public boolean hasData() {
        if (planes == null) {
            return false;
        }
        for (RenderPlane plane : planes) {
            if (plane != null) {
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

    public boolean isRequestInFlight() {
        return requestInFlight;
    }

    public void markRequestSent() {
        requestInFlight = true;
        requestSentAt = System.currentTimeMillis();
    }

    public void clearRequest() {
        requestInFlight = false;
        requestSentAt = 0L;
    }

    public void markRequestProgress() {
        if (requestInFlight) {
            requestSentAt = System.currentTimeMillis();
        }
    }

    public boolean isRequestTimedOut() {
        return requestInFlight && requestSentAt + REQUEST_TIMEOUT_MS < System.currentTimeMillis();
    }

    public void clearData() {
        cleanup();
        planes = null;
        previewMessage = "";
    }

    public RenderPlane[] getPlanes() {
        return planes;
    }

    public void setPlaneData(@Nullable RenderPlane plane, int offsetY, int dy) {
        if (dy <= 0) {
            clearData();
            return;
        }
        if (offsetY < 0 || offsetY >= dy) {
            return;
        }
        if (planes == null) {
            planes = new RenderPlane[dy];
        } else if (planes.length != dy) {
            clearData();
            planes = new RenderPlane[dy];
        }
        if (plane == null) {
            return;
        }
        if (planes[offsetY] == null) {
            plane.markUpdated();
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

//    private static final Matrix4f IDENTITY = new Matrix4f();
//
//    static {
//        IDENTITY.setIdentity();
//    }

    public static class RenderElement {
        protected VertexBuffer vbo;
        protected boolean valid = false;

        public void cleanup() {
            if (vbo != null) {
                vbo.close();
                vbo = null;
            }
            valid = false;
        }

        public void render(PoseStack poseStack) {
            if (vbo != null && valid) {
                vbo.bind();
                vbo.drawWithShader(poseStack.last().pose(), com.mojang.blaze3d.systems.RenderSystem.getProjectionMatrix(), GameRenderer.getPositionColorShader());
                VertexBuffer.unbind();
            }
        }

        public void createRenderList() {
            cleanup();
            vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
        }

        public void performRenderToList() {
            BufferBuilder.RenderedBuffer renderedBuffer = vboBuffer.end();
            valid = false;
            if (renderedBuffer != null && renderedBuffer.drawState() != null && renderedBuffer.drawState().mode() != null && vbo != null) {
                vbo.bind();
                vbo.upload(renderedBuffer);
                VertexBuffer.unbind();
                valid = true;
            } else if (renderedBuffer != null) {
                renderedBuffer.release();
            }
            vboBuffer.clear();
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
        private long flashBirthtime = 0L;

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
            markUpdated();
            super.cleanup();
        }

        public void markUpdated() {
            dirty = true;
            birthtime = System.currentTimeMillis();
            flashBirthtime = 0L;
        }

        public long getBirthtime() {
            return birthtime;
        }

        public void markFlashRendered() {
            if (flashBirthtime == 0L) {
                flashBirthtime = System.currentTimeMillis();
            }
        }

        public boolean isFlashing(long time) {
            return flashBirthtime != 0L && flashBirthtime > time - ScannerConfiguration.projectorFlashTimeout.get();
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
            if (state == null) {
                return true;
            }
            if (!state.canOcclude()) {
                return true;
            }
            if (ShapeBlockInfo.getBlockInfo(palette, state).isNonSolid()) {
                return true;
            }
            return false;
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
