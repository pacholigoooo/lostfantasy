package dev.lostfantasy.client;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
public final class NativeModels {
    private static BufferBuilder buffer;
    private static final double[] SPHERE=dev.lostfantasy.core.SphereMesh.createUnitQuads();
    private NativeModels() {}
    public static void begin() {buffer=Tessellator.getInstance().getBuffer();buffer.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_COLOR);}
    public static void end() {Tessellator.getInstance().draw();}
    private static void v(double x,double y,double z,int c) {buffer.pos(x,y,z).color((c>>16)&255,(c>>8)&255,c&255,(c>>>24)&255).endVertex();}
    public static void box(double x,double y,double z,double w,double h,double d,int c) {
        double X=x+w,Y=y+h,Z=z+d;
        v(x,y,z,c);v(x,Y,z,c);v(X,Y,z,c);v(X,y,z,c);
        v(x,y,Z,c);v(X,y,Z,c);v(X,Y,Z,c);v(x,Y,Z,c);
        v(x,y,z,c);v(x,y,Z,c);v(x,Y,Z,c);v(x,Y,z,c);
        v(X,y,z,c);v(X,Y,z,c);v(X,Y,Z,c);v(X,y,Z,c);
        v(x,Y,z,c);v(x,Y,Z,c);v(X,Y,Z,c);v(X,Y,z,c);
        v(x,y,z,c);v(X,y,z,c);v(X,y,Z,c);v(x,y,Z,c);
    }
    public static void sphere(double radius,int color) {
        begin();
        for(int i=0;i<SPHERE.length;i+=3)v(radius*SPHERE[i],radius*SPHERE[i+1],radius*SPHERE[i+2],color);
        end();
    }
}
