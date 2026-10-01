package dev.lostfantasy.client;
import java.io.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class ClippedTrainMeshTest {
    private ClippedTrainMesh mesh() throws IOException {
        ByteArrayOutputStream out=new ByteArrayOutputStream();DataOutputStream d=new DataOutputStream(out);
        d.writeInt(0x4c464d31);d.writeInt(3);
        for(float[] p:new float[][]{{0,0,-1},{2,0,1},{0,2,1}}){for(float v:p)d.writeFloat(v);d.writeInt(0xff8040ff);}
        return new ClippedTrainMesh(new DataInputStream(new ByteArrayInputStream(out.toByteArray())));
    }
    @Test public void clipsAtPortalAndRetainsNormalsColorsAndArea() throws IOException {
        List<float[]> points=new ArrayList<>();mesh().draw(0,(x,y,z,r,g,b,a,nx,ny,nz)->{
            assertTrue(z>=0);assertEquals(255,r);assertEquals(128,g);assertEquals(64,b);assertEquals(255,a);
            assertEquals(1,nx*nx+ny*ny+nz*nz,1e-6);points.add(new float[]{x,y,z});
        });
        assertEquals(6,points.size());
        double area=0;for(int i=0;i<6;i+=3){float[] a=points.get(i),b=points.get(i+1),c=points.get(i+2);area+=Math.abs((b[0]-a[0])*(c[1]-a[1])-(b[1]-a[1])*(c[0]-a[0]))/2;}
        assertEquals(1.5,area,1e-6);
    }
    @Test public void hiddenAndFullyEmergedMeshKeepExpectedVertexCounts() throws IOException {
        ClippedTrainMesh m=mesh();int[] count={0};ClippedTrainMesh.Sink sink=(x,y,z,r,g,b,a,nx,ny,nz)->count[0]++;
        m.draw(2,sink);assertEquals(0,count[0]);m.draw(-2,sink);assertEquals(3,count[0]);
    }
}
