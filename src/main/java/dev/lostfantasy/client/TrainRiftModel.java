package dev.lostfantasy.client;

import dev.lostfantasy.core.CastMotion;
import dev.lostfantasy.core.TrainRiftShape;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import java.util.*;

/** Cached, two-sided mesh: black spindle, torn purple rim, eyes and red fabric bows. */
final class TrainRiftModel {
    private static final List<Point> MESH=build();
    private TrainRiftModel() {}
    static void draw(Vec3d origin,Vec3d forward,double age) {
        double open=Math.min(CastMotion.charge(age,20),CastMotion.smooth((CastMotion.TRAIN_DURATION-age)/12));
        if(open<=0)return;
        try(RenderMatrix matrix=new RenderMatrix()) {
            GlStateManager.translate(origin.x,origin.y,origin.z);
            GlStateManager.rotate((float)Math.toDegrees(Math.atan2(forward.x,forward.z)),0,1,0);
            GlStateManager.translate(0,TrainRiftShape.CENTER,0);
            GlStateManager.scale(open,.3+.7*open,1);
            GlStateManager.translate(0,-TrainRiftShape.CENTER,0);
            GlStateManager.tryBlendFuncSeparate(770,771,1,0);GlStateManager.depthMask(true);
            BufferBuilder b=Tessellator.getInstance().getBuffer();b.begin(GL11.GL_TRIANGLES,DefaultVertexFormats.POSITION_COLOR);
            for(Point p:MESH)b.pos(p.x,p.y,p.z).color(p.color>>16&255,p.color>>8&255,p.color&255,p.color>>>24&255).endVertex();
            Tessellator.getInstance().draw();
        }finally {GlStateManager.depthMask(false);}
    }
    private static List<Point> build() {
        List<Point> mesh=new ArrayList<>();int segments=64;
        for(int i=0;i<segments;i++) {
            double a=i*Math.PI*2/segments,b=(i+1)*Math.PI*2/segments;
            double y1=TrainRiftShape.CENTER+Math.cos(a)*TrainRiftShape.HALF_HEIGHT,y2=TrainRiftShape.CENTER+Math.cos(b)*TrainRiftShape.HALF_HEIGHT;
            double x1=Math.signum(Math.sin(a))*TrainRiftShape.widthAt(y1),x2=Math.signum(Math.sin(b))*TrainRiftShape.widthAt(y2);
            triangle(mesh,0,TrainRiftShape.CENTER,x1,y1,x2,y2,0,0xff08020f);
            double tear1=1.035+.016*Math.sin(a*13),tear2=1.035+.016*Math.sin(b*13);
            quad(mesh,x1,y1,x1*tear1,TrainRiftShape.CENTER+(y1-TrainRiftShape.CENTER)*1.025,x2*tear2,TrainRiftShape.CENTER+(y2-TrainRiftShape.CENTER)*1.025,x2,y2,0,0xff68215c);
            quad(mesh,x1*.985,TrainRiftShape.CENTER+(y1-TrainRiftShape.CENTER)*.99,x1,y1,x2,y2,x2*.985,TrainRiftShape.CENTER+(y2-TrainRiftShape.CENTER)*.99,.004,0xffcd397b);
            quad(mesh,x1*.985,TrainRiftShape.CENTER+(y1-TrainRiftShape.CENTER)*.99,x1,y1,x2,y2,x2*.985,TrainRiftShape.CENTER+(y2-TrainRiftShape.CENTER)*.99,-.004,0xffcd397b);
        }
        for(int side:new int[]{-1,1}) {
            double z=side*.016;
            eye(mesh,-.65,.28,.35,z);eye(mesh,.68,.92,.42,z);eye(mesh,-1.03,1.69,.42,z);
            eye(mesh,.68,2.37,.46,z);eye(mesh,-.76,3.16,.38,z);eye(mesh,.35,3.96,.30,z);
            bow(mesh,TrainRiftShape.CENTER+TrainRiftShape.HALF_HEIGHT,1,side*.045);
            bow(mesh,TrainRiftShape.CENTER-TrainRiftShape.HALF_HEIGHT,-1,side*.045);
        }
        return Collections.unmodifiableList(mesh);
    }
    private static void eye(List<Point> m,double x,double y,double width,double z) {
        double h=width*.35;
        quad(m,x-width,y,x,y+h,x+width,y,x,y-h,z,0xffc5a0ca);
        double front=z+Math.copySign(.006,z);
        quad(m,x-.075,y,x,y+h*.93,x+.075,y,x,y-h*.93,front,0xffd4385f);
        quad(m,x-.018,y-h*.75,x+.018,y-h*.75,x+.018,y+h*.75,x-.018,y+h*.75,front+Math.copySign(.004,z),0xff100616);
    }
    private static void bow(List<Point> m,double y,int vertical,double z) {
        for(int side:new int[]{-1,1}) {
            quad(m,side*.035,y,side*.70,y+vertical*.29,side*.60,y-vertical*.30,side*.07,y-vertical*.065,z,0xff9f204a);
            triangle(m,side*.06,y,side*.70,y+vertical*.29,side*.40,y+vertical*.01,z+Math.copySign(.006,z),0xffdc4164);
            quad(m,side*.12,y-vertical*.05,side*.34,y-vertical*.08,side*.55,y-vertical*.62,side*.30,y-vertical*.48,z,0xffc42c53);
            triangle(m,side*.30,y-vertical*.48,side*.55,y-vertical*.62,side*.27,y-vertical*.91,z,0xff922344);
        }
        quad(m,-.105,y-.13,.105,y-.13,.105,y+.13,-.105,y+.13,z+Math.copySign(.01,z),0xffe35376);
    }
    private static void quad(List<Point> m,double ax,double ay,double bx,double by,double cx,double cy,double dx,double dy,double z,int color) {
        triangle(m,ax,ay,bx,by,cx,cy,z,color);triangle(m,ax,ay,cx,cy,dx,dy,z,color);
    }
    private static void triangle(List<Point> m,double ax,double ay,double bx,double by,double cx,double cy,double z,int color) {
        m.add(new Point(ax,ay,z,color));m.add(new Point(bx,by,z,color));m.add(new Point(cx,cy,z,color));
    }
    private static final class Point {
        final double x,y,z;final int color;
        Point(double x,double y,double z,int color) {this.x=x;this.y=y;this.z=z;this.color=color;}
    }
}
