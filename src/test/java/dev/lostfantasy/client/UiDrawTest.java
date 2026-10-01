package dev.lostfantasy.client;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.junit.Test;
import java.nio.ByteBuffer;
import static org.junit.Assert.*;

/** Inspect the HUD's actual packed triangles without creating a GL context. */
public class UiDrawTest {
    private BufferBuilder bar(float spirit,int capacity) {
        BufferBuilder buffer=new BufferBuilder(32768);
        buffer.begin(4,DefaultVertexFormats.POSITION_COLOR); // GL_TRIANGLES, no GL call.
        UiDraw.appendSpiritBar(buffer,100,50,spirit,capacity);buffer.finishDrawing();return buffer;
    }
    @Test public void packedBarStaysOpaqueFiniteAndInsideTheOriginalSixCellBounds() {
        for(int capacity=2;capacity<=6;capacity++)for(float spirit:new float[]{0,.04f,.5f,1.99f,3,5.99f,6}) {
            BufferBuilder buffer=bar(spirit,capacity);ByteBuffer bytes=buffer.getByteBuffer();
            assertEquals(4,buffer.getDrawMode());assertEquals(0,buffer.getVertexCount()%3);
            assertTrue(buffer.getVertexCount()>=324 && buffer.getVertexCount()<=756);
            for(int vertex=0;vertex<buffer.getVertexCount();vertex++) {
                int offset=vertex*16;float x=bytes.getFloat(offset),y=bytes.getFloat(offset+4),z=bytes.getFloat(offset+8);
                assertTrue(Float.isFinite(x)&&Float.isFinite(y));assertEquals(0,z,0);
                assertTrue(x>=92.5f && x<=182.5f && y>=42.5f && y<=57.5f);
                assertEquals(255,bytes.get(offset+15)&255);
            }
        }
    }
    @Test public void partialFillBordersAndLockedCellsKeepTheirColorsAndCoverage() {
        BufferBuilder partial=bar(.5f,2);
        assertEquals(0xff2e3040,colorAt(partial,107,50.1)); // outer rim
        assertEquals(0xffe9e4cf,colorAt(partial,105.8,50.1)); // unlocked rim
        assertEquals(0xff171c37,colorAt(partial,100.1,47.7)); // unfilled upper half
        assertEquals(0xff0096ff,colorAt(partial,100.1,50.3)); // sixth gradient row
        assertEquals(0xff00d2ff,colorAt(partial,100.1,53.8)); // second gradient row
        assertEquals(0xff5b5763,colorAt(partial,135.8,50.1)); // locked third cell
        assertEquals(0xff171c37,colorAt(bar(6,2),130.1,50.3)); // locked cells never fill
        assertEquals(0,colorAt(partial,108,42)); // outside hexagons
        assertEquals(0xff171c37,colorAt(bar(0,6),100.1,50.3));
        assertEquals(0xff0096ff,colorAt(bar(6,6),175.1,50.3));
    }
    private int colorAt(BufferBuilder buffer,double x,double y) {
        ByteBuffer b=buffer.getByteBuffer();int color=0;
        for(int i=0;i<buffer.getVertexCount();i+=3) {
            int a=i*16,c=a+16,d=a+32;
            double ax=b.getFloat(a),ay=b.getFloat(a+4),bx=b.getFloat(c),by=b.getFloat(c+4),cx=b.getFloat(d),cy=b.getFloat(d+4);
            double u=(bx-ax)*(y-ay)-(by-ay)*(x-ax),v=(cx-bx)*(y-by)-(cy-by)*(x-bx),w=(ax-cx)*(y-cy)-(ay-cy)*(x-cx);
            if((u>=0&&v>=0&&w>=0)||(u<=0&&v<=0&&w<=0))
                color=(b.get(a+15)&255)<<24|(b.get(a+12)&255)<<16|(b.get(a+13)&255)<<8|(b.get(a+14)&255);
        }
        return color;
    }
}
