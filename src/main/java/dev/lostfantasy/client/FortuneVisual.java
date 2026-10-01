package dev.lostfantasy.client;

import dev.lostfantasy.core.RibbonFortune;
import dev.lostfantasy.network.FortuneMessage;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

/** Fortune threads end at the real ribbon mesh, inside its held-item transform. */
public final class FortuneVisual {
    public static final int DURATION=RibbonFortune.DRAW_TICKS;
    private static final Map<UUID,FortuneMessage> active=new LinkedHashMap<>();
    private static World world;
    private FortuneVisual() {}
    public static void tick() {
        World current=Minecraft.getMinecraft().world;
        if(world!=current){active.clear();world=current;}
        if(world!=null)active.values().removeIf(m->{
            long age=world.getTotalWorldTime()-m.started;
            EntityPlayer player=world.getPlayerEntityByUUID(m.playerId);
            return age>=DURATION || age< -40 || m.dimension!=world.provider.getDimension()
                    || (player!=null && !player.isEntityAlive());
        });
    }
    public static void receive(FortuneMessage message) {
        tick();if(world==null || !message.valid() || world.provider.getDimension()!=message.dimension)return;
        long age=world.getTotalWorldTime()-message.started;
        if(age< -40 || age>=DURATION)return;
        FortuneMessage old=active.get(message.playerId);
        if(old!=null && old.started>=message.started)return;
        if(active.size()>=16)active.remove(active.keySet().iterator().next());
        active.put(message.playerId,message);
    }
    public static boolean isPlaying(UUID player){tick();return active.containsKey(player);}
    static boolean matches(ItemStack stack,UUID player) {
        tick();FortuneMessage effect=active.get(player);
        return effect!=null && RibbonFortune.matches(stack,effect.ribbonId);
    }
    static void drawAttached(ItemStack stack,UUID player,Vec3d[] sources,Vec3d eye,double now) {
        FortuneMessage effect=active.get(player);if(effect==null || !RibbonFortune.matches(stack,effect.ribbonId))return;
        double age=Math.max(0,now-effect.started),fade=smooth(age/8);
        Vec3d target=RibbonItemRenderer.KNOT;
        BufferBuilder b=Tessellator.getInstance().getBuffer();b.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        for(int strand=0;strand<sources.length;strand++) {
            double theta=strand*Math.PI*2/sources.length;
            double head=smooth((age-5-strand*.45)/28),tail=smooth((age-38-strand*.45)/28),length=head-tail;
            if(length<.0001)continue;
            Vec3d start=sources[strand],delta=target.subtract(start);
            Vec3d curl=normal(delta.crossProduct(eye.subtract(start))).scale(.18*Math.sin(theta));
            Vec3d control1=start.add(delta.scale(.25)).add(curl),control2=target.subtract(delta.scale(.20)).subtract(curl.scale(.45));
            Vec3d[] points=new Vec3d[41];
            for(int segment=0;segment<points.length;segment++) {
                double u=segment/(double)(points.length-1),t=tail+u*length;
                points[segment]=bezier(start,control1,control2,target,t)
                        .add(curl.scale(Math.sin(t*Math.PI)*Math.sin(t*10+age*.12+theta)*.15));
            }
            int alpha=(int)(fade*Math.min(1,length*10)*220);
            stroke(b,points,eye,.017,.25,246,183,92,alpha/7);
            stroke(b,points,eye,.004,.20,255,225,150,alpha);
        }
        Tessellator.getInstance().draw();
    }
    private static Vec3d bezier(Vec3d a,Vec3d b,Vec3d c,Vec3d d,double t) {
        double q=1-t;return a.scale(q*q*q).add(b.scale(3*q*q*t)).add(c.scale(3*q*t*t)).add(d.scale(t*t*t));
    }
    private static double smooth(double t){t=Math.max(0,Math.min(1,t));return t*t*(3-2*t);}
    private static void stroke(BufferBuilder b,Vec3d[] points,Vec3d eye,double width,double tip,int r,int g,int blue,int alpha) {
        if(alpha<=0)return;
        Vec3d previousLeft=null,previousRight=null;
        for(int i=0;i<points.length;i++) {
            Vec3d tangent=points[Math.min(points.length-1,i+1)].subtract(points[Math.max(0,i-1)]);
            double taper=tip+(1-tip)*Math.sin(Math.PI*i/(points.length-1));
            Vec3d across=normal(tangent.crossProduct(eye.subtract(points[i]))).scale(width*taper);
            Vec3d left=points[i].subtract(across),right=points[i].add(across);
            if(i>0) {
                vertex(b,previousLeft,r,g,blue,alpha);vertex(b,previousRight,r,g,blue,alpha);
                vertex(b,right,r,g,blue,alpha);vertex(b,left,r,g,blue,alpha);
            }
            previousLeft=left;previousRight=right;
        }
    }
    private static Vec3d normal(Vec3d vector) {
        double length=Math.sqrt(vector.lengthSquared());return length<1e-12?Vec3d.ZERO:vector.scale(1/length);
    }
    private static void vertex(BufferBuilder b,Vec3d p,int r,int g,int blue,int alpha){b.pos(p.x,p.y,p.z).tex(.5,.5).color(r,g,blue,alpha).normal(0,0,1).endVertex();}
}
