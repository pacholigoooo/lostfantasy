package dev.lostfantasy.client;

import net.minecraft.client.renderer.GlStateManager;
import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/** Save the selected matrix itself; never pop a stack owned by Minecraft or a shader pass. */
final class RenderMatrix implements AutoCloseable {
    interface Access {
        int mode();
        void mode(int value);
        void read(int matrix,FloatBuffer buffer);
        void load(FloatBuffer buffer);
    }
    private static final Access GL=new Access() {
        public int mode() {return GL11.glGetInteger(GL11.GL_MATRIX_MODE);}
        public void mode(int value) {GlStateManager.matrixMode(value);}
        public void read(int matrix,FloatBuffer buffer) {GL11.glGetFloat(matrix,buffer);}
        public void load(FloatBuffer buffer) {GL11.glLoadMatrix(buffer);}
    };
    private static final ThreadLocal<ArrayDeque<FloatBuffer>> BUFFERS=ThreadLocal.withInitial(ArrayDeque::new);
    private final Access access;
    private final int mode,previousMode;
    private FloatBuffer saved;
    RenderMatrix() {this(GL,GL11.GL_MODELVIEW);}
    RenderMatrix(int mode) {this(GL,mode);}
    RenderMatrix(Access access,int mode) {
        if(mode!=GL11.GL_MODELVIEW && mode!=GL11.GL_PROJECTION)throw new IllegalArgumentException("Modelview or projection required");
        this.access=access;this.mode=mode;previousMode=access.mode();
        saved=BUFFERS.get().pollFirst();if(saved==null)saved=BufferUtils.createFloatBuffer(16);
        saved.clear();access.read(mode==GL11.GL_MODELVIEW?GL11.GL_MODELVIEW_MATRIX:GL11.GL_PROJECTION_MATRIX,saved);
        access.mode(mode);
    }
    @Override public void close() {
        if(saved==null)return;
        access.mode(mode);saved.rewind();access.load(saved);access.mode(previousMode);
        BUFFERS.get().addFirst(saved);saved=null;
    }
}
