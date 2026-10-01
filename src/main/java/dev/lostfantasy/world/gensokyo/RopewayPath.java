package dev.lostfantasy.world.gensokyo;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/** Continuous route geometry shared by the cable, carrier and offline clearance checks. */
public final class RopewayPath {
    public static final int TRAVEL_TICKS=3600, RAMP=80;
    public static final double LANE=4,HANGER=7;
    private static final double[] T={0,.016,.065,.27,.50,.73,.94,.984,1},Y={92,92,129,152,187,212,213,200,200};
    private RopewayPath() {}
    public static GensokyoAtlas station(int end) {return end==0?GensokyoAtlas.ROPEWAY:GensokyoAtlas.ROPEWAY_MOUNTAIN;}
    public static BlockPos console(int end) {GensokyoAtlas s=station(end);return new BlockPos(s.x+(end==0?-9:9),s.y+5,s.z+4);}
    public static BlockPos landing(int end) {GensokyoAtlas s=station(end);return new BlockPos(s.x+(end==0?9:-9),s.y+5,s.z+8);}
    public static float yaw(int start) {return (float)Math.toDegrees(Math.atan2(200,-2600))+(start==0?0:180);}
    public static double fraction(double age) {
        double t=Math.max(0,Math.min(TRAVEL_TICKS,age));
        return (t<RAMP?t*t/(2*RAMP):t>TRAVEL_TICKS-RAMP?TRAVEL_TICKS-RAMP-(TRAVEL_TICKS-t)*(TRAVEL_TICKS-t)/(2*RAMP):t-RAMP*.5)/(TRAVEL_TICKS-RAMP);
    }
    public static double height(double u) {
        u=Math.max(0,Math.min(1,u));int i=0;while(i<T.length-2 && u>T[i+1])i++;
        double span=T[i+1]-T[i],t=(u-T[i])/span,t2=t*t,t3=t2*t;
        double m0=slope(i),m1=slope(i+1);
        return (2*t3-3*t2+1)*Y[i]+(t3-2*t2+t)*span*m0+(-2*t3+3*t2)*Y[i+1]+(t3-t2)*span*m1;
    }
    private static double slope(int i) {
        if(i==0 || i==T.length-1)return 0;
        double a=(Y[i]-Y[i-1])/(T[i]-T[i-1]),b=(Y[i+1]-Y[i])/(T[i+1]-T[i]);
        return a*b<=0?0:2*a*b/(a+b);
    }
    public static Vec3d point(double u,double lane) {
        u=Math.max(0,Math.min(1,u));GensokyoAtlas a=station(0),b=station(1);
        double dx=b.x-a.x,dz=b.z-a.z,length=Math.hypot(dx,dz);
        return new Vec3d(a.x+.5+dx*u-dz/length*lane,height(u),a.z+.5+dz*u+dx/length*lane);
    }
    public static Vec3d ride(int start,double age) {double u=fraction(age);return point(start==0?u:1-u,start==0?-LANE:LANE);}
    public static int nearest(int start,int age) {return fraction(age)<.5?start:1-start;}
}
