package dev.lostfantasy.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class SphereMeshTest {
    @Test public void cachedMeshPreservesLegacyVertexOrderAtEveryUsedScale() {
        double[] mesh=SphereMesh.createUnitQuads();
        assertEquals(12*24*4*3,mesh.length);
        for(double radius:new double[]{.1,.52,1,50}) {
            int index=0;
            for(int row=0;row<12;row++)for(int col=0;col<24;col++) {
                double a=-Math.PI/2+Math.PI*row/12,b=a+Math.PI/12,t=2*Math.PI*col/24,u=t+2*Math.PI/24;
                double[] original={
                    radius*Math.cos(a)*Math.cos(t),radius*Math.sin(a),radius*Math.cos(a)*Math.sin(t),
                    radius*Math.cos(b)*Math.cos(t),radius*Math.sin(b),radius*Math.cos(b)*Math.sin(t),
                    radius*Math.cos(b)*Math.cos(u),radius*Math.sin(b),radius*Math.cos(b)*Math.sin(u),
                    radius*Math.cos(a)*Math.cos(u),radius*Math.sin(a),radius*Math.cos(a)*Math.sin(u)};
                for(double expected:original)assertEquals(expected,radius*mesh[index++],1e-12);
            }
        }
    }
    @Test public void unitVerticesRemainOnSphereAndSeamCloses() {
        double[] mesh=SphereMesh.createUnitQuads();
        for(int i=0;i<mesh.length;i+=3)assertEquals(1,mesh[i]*mesh[i]+mesh[i+1]*mesh[i+1]+mesh[i+2]*mesh[i+2],1e-12);
        for(int row=0;row<12;row++) {
            int first=row*24*12,last=first+23*12;
            for(int axis=0;axis<3;axis++) {
                assertEquals(mesh[first+axis],mesh[last+9+axis],1e-12);
                assertEquals(mesh[first+3+axis],mesh[last+6+axis],1e-12);
            }
        }
    }
}
