package dev.lostfantasy.core;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Geometry shared by server collisions, network snapshots and the client mesh. */
public final class EmeraldCity {
    public static final int WINDUP = 12, RISE = 4, HOLD = 20, SINK = 8, DURATION = 64, MAX_HITS = 3;
    public static final double RADIUS = .7, HEIGHT = 4;
    public static final int RINGS = 4, MAX_COLUMNS = 80;
    private EmeraldCity() {}
    public static int columnsInRing(int round) { return 8*(round+1); }
    public static double ringRadius(int round) { return 2+round*2; }
    public static Vec3d direction(double heading,int round,int index) {
        double angle=heading+index*Math.PI*2/columnsInRing(round);
        return new Vec3d(Math.sin(angle),0,Math.cos(angle));
    }

    public static final class Column {
        public final double x, y, z, height;
        public final int round;
        public Column(double x, double y, double z, double height, int round) {
            if (!Double.isFinite(x+y+z+height) || height <= 0 || height > HEIGHT || round < 0 || round > 3)
                throw new IllegalArgumentException("Invalid emerald column");
            this.x=x; this.y=y; this.z=z; this.height=height; this.round=round;
        }
        public int start() { return 8 + round*8; }
        public boolean rising(int age) { int local=age-start(); return local>=0 && local<RISE; }
        public boolean shielding(int age) { int local=age-start(); return local>=RISE && local<RISE+HOLD+SINK; }
        public double heightAt(double age) {
            double local=age-start();
            if(local<0 || local>=RISE+HOLD+SINK) return 0;
            if(local<RISE) return height*local/RISE;
            return height*(1-Math.max(0,local-RISE-HOLD)/SINK);
        }
        public AxisAlignedBB bounds(double age) {
            return new AxisAlignedBB(x-RADIUS,y,z-RADIUS,x+RADIUS,y+heightAt(age),z+RADIUS);
        }

        /** Clip a segment against the actual octagonal shaft and pointed cap, not the whole spell area. */
        public Vec3d contact(Vec3d from, Vec3d to, double age) {
            double h=heightAt(age);
            if(h<=0) return null;
            double[] interval={0,1};
            Vec3d p=from.subtract(x,y,z), d=to.subtract(from);
            if(!clip(p,d,0,-1,0,0,interval) || !clip(p,d,0,1,0,h,interval)) return null;
            double apothem=RADIUS*Math.cos(Math.PI/8), tip=Math.min(.8,height);
            for(int face=0;face<8;face++) {
                double angle=(face+.5)*Math.PI/4, nx=Math.cos(angle), nz=Math.sin(angle);
                if(!clip(p,d,nx,0,nz,apothem,interval)
                        || !clip(p,d,nx,apothem/tip,nz,apothem*h/tip,interval)) return null;
            }
            return from.add(d.scale(interval[0]));
        }
    }
    private static boolean clip(Vec3d p,Vec3d d,double nx,double ny,double nz,double limit,double[] interval) {
        double distance=limit-(nx*p.x+ny*p.y+nz*p.z), velocity=nx*d.x+ny*d.y+nz*d.z;
        if(Math.abs(velocity)<1e-9) return distance>=0;
        double t=distance/velocity;
        if(velocity>0) interval[1]=Math.min(interval[1],t); else interval[0]=Math.max(interval[0],t);
        return interval[0]<=interval[1];
    }

    /** One attempt per pillar, at most three successful hits for the entire cast. */
    public static final class Hits {
        private final Map<UUID,Integer> totals=new HashMap<>();
        private final Map<Integer,Set<UUID>> attempted=new HashMap<>();
        public boolean attempt(int column,UUID target) {
            return count(target)<MAX_HITS && attempted.computeIfAbsent(column,key->new HashSet<>()).add(target);
        }
        public void landed(UUID target) { totals.put(target,Math.min(MAX_HITS,count(target)+1)); }
        public int count(UUID target) { return totals.getOrDefault(target,0); }
    }
}
