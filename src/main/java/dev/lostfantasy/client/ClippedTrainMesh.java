package dev.lostfantasy.client;

import java.io.DataInputStream;
import java.io.IOException;

/** Single-plane clipping, with reusable scratch storage on the render thread. */
final class ClippedTrainMesh {
    interface Sink {void vertex(float x,float y,float z,int r,int g,int b,int a,float nx,float ny,float nz);}
    private final float[][] vertices,normals;
    private final float[] minZ,maxZ;
    private final float[][] clipped=new float[4][7];
    ClippedTrainMesh(DataInputStream in) throws IOException {
        if(in.readInt()!=0x4c464d31)throw new IOException("Bad train mesh header");
        int count=in.readInt();if(count<=0 || count%3!=0 || count>120000)throw new IOException("Invalid train vertex count");
        vertices=new float[count][7];normals=new float[count/3][3];minZ=new float[count/3];maxZ=new float[count/3];
        for(float[] v:vertices) {
            for(int axis=0;axis<3;axis++) {v[axis]=in.readFloat();if(!Float.isFinite(v[axis]))throw new IOException("Non-finite vertex");}
            for(int channel=3;channel<7;channel++)v[channel]=in.readUnsignedByte();
        }
        if(in.read()!=-1)throw new IOException("Trailing train mesh data");
        for(int i=0;i<count;i+=3) {
            float[] a=vertices[i],b=vertices[i+1],c=vertices[i+2],n=normals[i/3];
            minZ[i/3]=Math.min(a[2],Math.min(b[2],c[2]));maxZ[i/3]=Math.max(a[2],Math.max(b[2],c[2]));
            float ux=b[0]-a[0],uy=b[1]-a[1],uz=b[2]-a[2],vx=c[0]-a[0],vy=c[1]-a[1],vz=c[2]-a[2];
            n[0]=uy*vz-uz*vy;n[1]=uz*vx-ux*vz;n[2]=ux*vy-uy*vx;
            double length=Math.sqrt(n[0]*n[0]+n[1]*n[1]+n[2]*n[2]);
            if(length>1e-12)for(int j=0;j<3;j++)n[j]/=length;
        }
    }
    void draw(double plane,Sink sink) {
        for(int tri=0;tri<vertices.length;tri+=3) {
            int triangle=tri/3;
            if(maxZ[triangle]<plane)continue;
            if(minZ[triangle]>=plane) {
                emit(sink,vertices[tri],normals[triangle]);emit(sink,vertices[tri+1],normals[triangle]);emit(sink,vertices[tri+2],normals[triangle]);
                continue;
            }
            int count=0;float[] previous=vertices[tri+2];boolean wasInside=previous[2]>=plane;
            for(int j=0;j<3;j++) {
                float[] next=vertices[tri+j];boolean inside=next[2]>=plane;
                if(inside!=wasInside) {
                    double t=(plane-previous[2])/(next[2]-previous[2]);float[] v=clipped[count++];
                    for(int k=0;k<7;k++)v[k]=(float)(previous[k]+(next[k]-previous[k])*t);
                    v[2]=(float)plane;
                }
                if(inside)System.arraycopy(next,0,clipped[count++],0,7);
                previous=next;wasInside=inside;
            }
            for(int j=1;j<count-1;j++) {
                emit(sink,clipped[0],normals[triangle]);emit(sink,clipped[j],normals[triangle]);emit(sink,clipped[j+1],normals[triangle]);
            }
        }
    }
    private static void emit(Sink sink,float[] v,float[] n) {
        sink.vertex(v[0],v[1],v[2],Math.round(v[3]),Math.round(v[4]),Math.round(v[5]),Math.round(v[6]),n[0],n[1],n[2]);
    }
}
