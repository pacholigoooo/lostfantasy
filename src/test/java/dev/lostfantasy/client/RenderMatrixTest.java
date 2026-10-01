package dev.lostfantasy.client;

import java.nio.FloatBuffer;
import org.junit.Test;
import org.lwjgl.opengl.GL11;
import static org.junit.Assert.*;

public class RenderMatrixTest {
    @Test public void changedMatrixModeCannotRestoreIntoTheWrongMatrix() {
        Matrices gl=new Matrices();
        float[] model=gl.model.clone(),projection=gl.projection.clone();
        try(RenderMatrix ignored=new RenderMatrix(gl,GL11.GL_MODELVIEW)) {
            gl.model[12]=900;
            gl.mode(GL11.GL_PROJECTION);
        }
        assertArrayEquals(model,gl.model,0);
        assertArrayEquals(projection,gl.projection,0);
        assertEquals(GL11.GL_MODELVIEW,gl.mode());
    }

    @Test public void nestedOverlayRestoresWorldAndProjectionEvenAfterAnException() {
        Matrices gl=new Matrices();gl.mode(GL11.GL_TEXTURE);
        float[] model=gl.model.clone(),projection=gl.projection.clone();
        try {
            try(RenderMatrix outer=new RenderMatrix(gl,GL11.GL_PROJECTION)) {
                gl.projection[0]=12;
                try(RenderMatrix inner=new RenderMatrix(gl,GL11.GL_MODELVIEW)) {
                    gl.model[5]=24;
                    throw new IllegalStateException("draw failed");
                }
            }
        } catch(IllegalStateException expected) {assertEquals("draw failed",expected.getMessage());}
        assertArrayEquals(model,gl.model,0);assertArrayEquals(projection,gl.projection,0);
        assertEquals(GL11.GL_TEXTURE,gl.mode());
    }

    @Test public void nestedModelsRestoreTheirOwnTransforms() {
        Matrices gl=new Matrices();float[] original=gl.model.clone();
        try(RenderMatrix outer=new RenderMatrix(gl,GL11.GL_MODELVIEW)) {
            gl.model[12]=42;
            try(RenderMatrix inner=new RenderMatrix(gl,GL11.GL_MODELVIEW)) {gl.model[12]=99;}
            assertEquals(42,gl.model[12],0);
        }
        assertArrayEquals(original,gl.model,0);
    }

    @Test public void repeatedFramesDoNotReuseAnActiveSnapshotOrRestoreTwice() {
        Matrices gl=new Matrices();float[] original=gl.model.clone();
        for(int frame=0;frame<1000;frame++) {
            RenderMatrix scope=new RenderMatrix(gl,GL11.GL_MODELVIEW);
            gl.model[13]=frame;
            scope.close();scope.close();
            assertArrayEquals(original,gl.model,0);
        }
        assertEquals(1000,gl.loads);
    }

    private static final class Matrices implements RenderMatrix.Access {
        final float[] model=new float[16],projection=new float[16];
        int selected=GL11.GL_MODELVIEW,loads;
        Matrices() {for(int i=0;i<16;i++){model[i]=i+.25f;projection[i]=i+20.5f;}}
        public int mode(){return selected;}
        public void mode(int value){selected=value;}
        public void read(int matrix,FloatBuffer buffer) {
            float[] values=matrix==GL11.GL_MODELVIEW_MATRIX?model:projection;
            // LWJGL's glGetFloat writes without advancing the buffer position.
            for(int i=0;i<16;i++)buffer.put(i,values[i]);
        }
        public void load(FloatBuffer buffer) {
            assertTrue(selected==GL11.GL_MODELVIEW || selected==GL11.GL_PROJECTION);
            float[] target=selected==GL11.GL_MODELVIEW?model:projection;
            for(int i=0;i<16;i++)target[i]=buffer.get(i);
            loads++;
        }
    }
}
