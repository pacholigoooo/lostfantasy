package dev.lostfantasy.world.gensokyo;

/** Seed-specific road elevations, sampled once from terrain without road earthworks. */
final class GensokyoRoadProfile {
    static final double MAX_GRADE=.60;
    private final double[] heights;
    GensokyoRoadProfile(GensokyoRoads.Segment road,GensokyoTerrain terrain) {
        double length=Math.hypot(road.bx-road.ax,road.bz-road.az);
        double start=terrain.roadJunction(road.ax,road.az,road.ay),end=terrain.roadJunction(road.bx,road.bz,road.by);
        int count=Math.max(1,(int)Math.ceil(length/3));
        double rise=Math.max(length*MAX_GRADE,Math.abs(end-start))/count;
        double[] raw=new double[count+1],water=new double[count+1];
        heights=new double[count+1];
        for(int i=0;i<=count;i++) {
            double t=i/(double)count;
            GensokyoTerrain.Column c=terrain.naturalColumn((int)Math.round(GensokyoNoise.lerp(road.ax,road.bx,t)),
                    (int)Math.round(GensokyoNoise.lerp(road.az,road.bz,t)));
            water[i]=c.wet()?c.water+2:0;
            raw[i]=Math.max(c.ground,water[i]);
        }
        // Reserve gradual approaches on both sides of every water crossing.
        for(int i=1;i<=count;i++)water[i]=Math.max(water[i],water[i-1]-rise);
        for(int i=count-1;i>=0;i--)water[i]=Math.max(water[i],water[i+1]-rise);
        for(int i=0;i<=count;i++) {
            double sum=0,weight=0;
            for(int j=Math.max(0,i-3);j<=Math.min(count,i+3);j++) {
                int w=4-Math.abs(j-i);sum+=raw[j]*w;weight+=w;
            }
            double low=Math.max(start-rise*i,end-rise*(count-i));
            double high=Math.min(start+rise*i,end+rise*(count-i));
            heights[i]=Math.max(low,Math.min(high,Math.max(water[i],sum/weight)));
        }
        // The endpoint bounds above keep shared junctions and building thresholds fixed.
        for(int i=1;i<=count;i++)heights[i]=Math.max(heights[i-1]-rise,Math.min(heights[i-1]+rise,heights[i]));
        for(int i=count-1;i>=0;i--)heights[i]=Math.max(heights[i+1]-rise,Math.min(heights[i+1]+rise,heights[i]));
    }
    double height(double t) {
        double position=t*(heights.length-1);int i=Math.min(heights.length-2,(int)position);
        return GensokyoNoise.lerp(heights[i],heights[i+1],position-i);
    }
}
