package dev.lostfantasy.world.gensokyo;

/** A mountain tributary feeding the workshop before joining the existing lower gorge. */
final class KappaWatercourse {
    private static final int[][] POINTS={{264,-2386,139},{264,-2240,113},{264,-2110,113},
            {207,-2010,112},{166,-1950,112},{177,-1910,112},{85,-1860,111},
            {86,-1828,110},{-36,-1780,110},{-57,-1740,109},{-164,-1700,109},
            {-185,-1660,108},{-280,-1620,102},{-360,-1568,96}};
    private KappaWatercourse() {}
    /** Monotone downstream station used by the continuous bank path. */
    static double centreX(int z) {return station(z,0);}
    static double level(int z) {return station(z,2);}
    private static double station(int z,int value) {
        for(int i=1;i<POINTS.length;i++)if(z<=POINTS[i][1]) {
            int[] a=POINTS[i-1],b=POINTS[i];double t=Math.max(0,(z-a[1])/(double)(b[1]-a[1]));
            return GensokyoNoise.lerp(a[value],b[value],t);
        }
        return POINTS[POINTS.length-1][value];
    }
    static Sample at(int x,int z) {
        // Navigation approaches fade farther out than the actual excavated bed.
        if(x<-488 || x>392 || z<-2514 || z>-1440)return null;
        Sample best=null;
        for(int i=1;i<POINTS.length;i++) {
            int[] a=POINTS[i-1],b=POINTS[i];double dx=b[0]-a[0],dz=b[1]-a[1];
            double t=Math.max(0,Math.min(1,((x-a[0])*dx+(z-a[1])*dz)/(dx*dx+dz*dz)));
            double d=Math.hypot(x-a[0]-t*dx,z-a[1]-t*dz);
            if(d<128 && (best==null || d<best.distance))best=new Sample(d,GensokyoNoise.lerp(a[2],b[2],t));
        }
        return best;
    }
    static final class Sample {
        final double distance,level;
        Sample(double distance,double level) {this.distance=distance;this.level=level;}
    }
}
