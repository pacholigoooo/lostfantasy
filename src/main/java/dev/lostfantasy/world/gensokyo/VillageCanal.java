package dev.lostfantasy.world.gensokyo;

/** The village's canal passes across Suzunaan's street and bends around the Hieda estate. */
final class VillageCanal {
    private static final int[][] POINTS={{-808,-64,76},{-390,-64,81},{-300,80,81},{-230,190,81},{170,190,81},{245,228,81},{510,228,81}};
    private VillageCanal() {}
    static boolean overlaps(int x1,int z1,int x2,int z2,int margin) {
        for(int i=1;i<POINTS.length;i++) {
            int[] a=POINTS[i-1],b=POINTS[i];
            if(VillageLayout.crosses(a[0],a[1],b[0],b[1],x1,z1,x2,z2,margin))return true;
        }
        return false;
    }
    static Sample at(int wx,int wz) {
        int x=wx-GensokyoAtlas.VILLAGE.x,z=wz-GensokyoAtlas.VILLAGE.z;
        if(x<-850 || x>550 || z<-105 || z>270)return null;
        double best=Double.POSITIVE_INFINITY,level=0;
        for(int i=1;i<POINTS.length;i++) {
            int[] a=POINTS[i-1],b=POINTS[i];double dx=b[0]-a[0],dz=b[1]-a[1];
            double t=Math.max(0,Math.min(1,((x-a[0])*dx+(z-a[1])*dz)/(dx*dx+dz*dz)));
            double d=Math.hypot(x-a[0]-dx*t,z-a[1]-dz*t);
            if(d<best) {best=d;level=GensokyoNoise.lerp(a[2],b[2],t);}
        }
        return best<=35?new Sample(best,level):null;
    }
    static final class Sample {
        final double distance,level;
        Sample(double distance,double level) {this.distance=distance;this.level=level;}
    }
}
