package dev.lostfantasy.core;

/** Fixed unit sphere coordinates; callers can cache once and scale for each draw. */
public final class SphereMesh {
    private SphereMesh() {}
    public static double[] createUnitQuads() {
        double[] vertices=new double[12*24*4*3];int index=0;
        for(int row=0;row<12;row++)for(int col=0;col<24;col++) {
            double a=-Math.PI/2+Math.PI*row/12,b=a+Math.PI/12,t=2*Math.PI*col/24,u=t+2*Math.PI/24;
            double ca=Math.cos(a),sa=Math.sin(a),cb=Math.cos(b),sb=Math.sin(b);
            double ct=Math.cos(t),st=Math.sin(t),cu=Math.cos(u),su=Math.sin(u);
            vertices[index++]=ca*ct;vertices[index++]=sa;vertices[index++]=ca*st;
            vertices[index++]=cb*ct;vertices[index++]=sb;vertices[index++]=cb*st;
            vertices[index++]=cb*cu;vertices[index++]=sb;vertices[index++]=cb*su;
            vertices[index++]=ca*cu;vertices[index++]=sa;vertices[index++]=ca*su;
        }
        return vertices;
    }
}
