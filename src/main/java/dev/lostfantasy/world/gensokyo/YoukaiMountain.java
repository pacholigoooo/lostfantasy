package dev.lostfantasy.world.gensokyo;

import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;
import static dev.lostfantasy.world.gensokyo.GensokyoAtlas.*;

/** Narrow connected divides above low valleys, with a dominant western summit. */
final class YoukaiMountain {
    static final int[][] SUMMIT_TRAIL={{ROPEWAY_MOUNTAIN.x,ROPEWAY_MOUNTAIN.approachZ(),ROPEWAY_MOUNTAIN.y},
            {-925,-2985,197},{-1056,-2920,204},{-1196,-2880,214},{-1310,-2920,225},
            {-1390,-2980,233},{MOUNTAIN_TOP.x,MOUNTAIN_TOP.z,MOUNTAIN_TOP.y}};
    // x, z, crest height. These are map-scale design coordinates, not canonical measurements.
    private static final Ridge[] RIDGES={
        new Ridge(220,new int[][]{{-1620,-3350,214},{-1500,-3170,226},{-1456,-3016,238},
                {-1620,-2820,220},{-1750,-2540,202},{-1740,-2150,169},{-1340,-1850,134},{-1070,-1550,100}}),
        new Ridge(200,new int[][]{{-1620,-3350,214},{-820,-3450,173},{60,-3380,197},
                {780,-3300,177},{1430,-3110,188},{1690,-2900,150}}),
        new Ridge(180,new int[][]{{1430,-3110,188},{1420,-2810,185},{1550,-2420,176},
                {1650,-2160,132}}),
        new Ridge(155,new int[][]{{60,-3380,197},{350,-3060,176},{680,-2750,194},
                {620,-2330,160},{450,-1980,132},{370,-1710,106}}),
        new Ridge(140,new int[][]{{-1740,-2150,169},{-1210,-2030,150},{-980,-1710,111}}),
        new Ridge(80,new int[][]{{-442,-2710,188},{-446,-2520,176},{-478,-2350,157}}),
        new Ridge(115,0,SUMMIT_TRAIL),
        new Ridge(85,new int[][]{{680,-2750,194},{510,-2760,171},{368,-2728,157}})
    };
    private static final GensokyoAtlas[] SHELVES={MORIYA,ROPEWAY_MOUNTAIN,NEMUNO,KASEN,
            CUCUMBER_FACTORY,KAPPA,RAINBOW_CAVE,SECRET_CLIFF,FALSE_HEAVEN,YAKUMO,PEONY_FIELD,
            MAYOHIGA,AQUATIC_MARKET,GEYSER,BLOWHOLE,RUINED_MANSION};
    // Secondary rock teeth sit on the connected divides, rather than replacing them with isolated cones.
    private static final int[][] TEETH={
        {-1540,-3090,238,38,58},{-1720,-2940,221,36,62},{-1870,-2700,205,43,70},
        {-1590,-3380,217,43,60},{-730,-3440,181,44,60},{90,-3380,202,40,67},
        {680,-2760,212,40,65},{1500,-3030,199,43,62},{1630,-2400,189,40,62},
        {-948,-2828,220,30,57},{-887,-2734,208,23,44},{-987,-3036,222,30,51},
        {1140,-2775,207,36,65},{1222,-2850,215,30,48}
    };
    private YoukaiMountain() {}

    static Sample at(int x,int z,GensokyoNoise noise) {
        if(x<=-2650 || x>=2900 || z<=-4250 || z>=-900)return null;
        double blend=GensokyoNoise.smooth((x+2650)/500.0)*GensokyoNoise.smooth((2900-x)/500.0)
                *GensokyoNoise.smooth((z+4250)/420.0)*GensokyoNoise.smooth((-z-900)/450.0);
        double warpX=x+noise.value(x,z,210,950)*19,warpZ=z+noise.value(x,z,230,951)*19;
        double floor=82+10*GensokyoNoise.smooth((-z-1250)/1850.0)+noise.value(x,z,170,952)*5;
        double height=floor,flank=0;
        for(Ridge ridge:RIDGES)for(int i=1;i<ridge.points.length;i++) {
            int[] a=ridge.points[i-1],b=ridge.points[i];
            if(warpX<Math.min(a[0],b[0])-ridge.width || warpX>Math.max(a[0],b[0])+ridge.width
                    || warpZ<Math.min(a[1],b[1])-ridge.width || warpZ>Math.max(a[1],b[1])+ridge.width)continue;
            double dx=b[0]-a[0],dz=b[1]-a[1];
            double t=Math.max(0,Math.min(1,((warpX-a[0])*dx+(warpZ-a[1])*dz)/(dx*dx+dz*dz)));
            double distance=Math.hypot(warpX-a[0]-t*dx,warpZ-a[1]-t*dz)/ridge.width;
            if(distance>=1)continue;
            double along=t*Math.hypot(dx,dz);
            double saddle=Math.pow(.5+.5*Math.sin(along/71.0+i*1.7),6);
            double crest=GensokyoNoise.lerp(a[2],b[2],t)-ridge.saddleDepth*saddle;
            double side=((warpX-a[0])*dz-(warpZ-a[1])*dx)/Math.hypot(dx,dz);
            double profile=side>0?.52:1.3;
            double folded=distance+Math.sin(along/32.0+distance*5)*.027
                    +Math.sin(along/13.0)*.009;
            double shoulder=floor+(crest-floor)*Math.pow(Math.max(0,1-folded),profile);
            // Narrow gullies descend the steep flank; the reverse slope remains wooded.
            if(side>0 && ridge.saddleDepth>0)shoulder-=16*Math.pow(.5+.5*Math.sin(along/39.0+distance*6),8)
                    *GensokyoNoise.smooth(distance/.12)*(1-GensokyoNoise.smooth((distance-.65)/.3));
            if(shoulder>height) {height=shoulder;flank=distance;}
        }
        boolean tooth=false;
        for(int[] peak:TEETH) {
            if(Math.abs(x-peak[0])>peak[3]+10 || Math.abs(z-peak[1])>peak[4]+10)continue;
            double px=(x-peak[0]+5*Math.sin(z/14.0))/(double)peak[3];
            double pz=(z-peak[1]+3*Math.sin(x/17.0))/(double)peak[4];
            double d=.35*Math.hypot(px,pz)+.65*Math.max(Math.abs(px),Math.abs(pz));
            if(d>=1)continue;
            double cleft=11*Math.pow(Math.max(0,Math.sin((x+.3*z)/6.0)),10);
            double tip=floor+20+(peak[2]-floor-20-cleft)*needle(d);
            if(tip>height) {height=tip;tooth=d>.12;}
        }
        double rough=noise.value(x,z,39,953)*5+noise.value(x,z,14,954)*1.8;
        double exposed=GensokyoNoise.smooth((height-floor)/30);
        // Broken ledges on exposed flanks; inhabited shelves are graded below.
        double strata=Math.sin(height*.47+noise.value(x,z,84,956)*2)*1.8;
        height+=(rough+strata)*exposed;
        height=Math.min(244,height);
        boolean rock=tooth || height>154 && flank>.04 && flank<.67 && noise.value(x,z,72,955)>-.27;
        // Rounded, uneven skirts blend the occupied footprints into the mountain shoulders.
        for(GensokyoAtlas site:SHELVES) {
            double edge=Math.hypot(Math.max(0,Math.abs((long)x-site.x)-site.rx),Math.max(0,Math.abs((long)z-site.z)-site.rz));
            if(edge>=120)continue;
            double front=GensokyoNoise.smooth((z-site.z)/(double)Math.max(1,site.rz));
            double skirt=68+20*front+12*noise.value(x,z,65,959);
            double shelf=1-GensokyoNoise.smooth((edge-8)/skirt);
            height=GensokyoNoise.lerp(height,site.y,shelf);
            if(shelf>.2)rock=false;
        }
        double lake=Math.hypot((x-WIND_LAKE.x)/320.0,(z-WIND_LAKE.z)/240.0);
        if(lake<1) {
            height=GensokyoNoise.lerp(height,178,1-GensokyoNoise.smooth((lake-.55)/.45));
            rock=false;
        }
        double summit=Math.hypot((x-MOUNTAIN_TOP.x)/55.0,(z-MOUNTAIN_TOP.z)/48.0);
        if(summit<1)height=GensokyoNoise.lerp(height,MOUNTAIN_TOP.y,1-GensokyoNoise.smooth((summit-.25)/.75));
        return new Sample(height,blend,rock);
    }
    /** A steep-sided rock blade, with a narrow taper above its wooded shoulder. */
    static double needle(double distance) {
        if(distance>=1)return 0;
        return .72*(1-GensokyoNoise.smooth((distance-.42)/.58))
                +.28*Math.pow(Math.max(0,1-distance/.68),1.15);
    }
    static void paint(ChunkPrimer p,int cx,int cz,GensokyoTerrain terrain) {
        int ox=cx<<4,oz=cz<<4;
        if(ox < -2666 || ox>2900 || oz < -4266 || oz>-900)return;
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int wx=ox+x,wz=oz+z;
            GensokyoTerrain.Column c=terrain.column(wx,wz);
            if(!c.rockSurface || c.basalt || c.path() || c.region!=GensokyoTerrain.Region.MOUNTAIN
                    || GensokyoAtlas.reserved(wx,wz,8))continue;
            double seam=terrain.patchNoise(wx,wz,14,957),moss=terrain.patchNoise(wx,wz,31,958);
            int top=c.ground;
            for(int y=Math.max(90,top-70);y<=top;y++) {
                if(p.getBlockState(x,y,z).getBlock()!=Blocks.STONE)continue;
                double vein=seam+.15*Math.sin(y/13.0+wx/47.0);
                p.setBlockState(x,y,z,y>top-3 && top<216 && moss>.28?Blocks.MOSSY_COBBLESTONE.getDefaultState():
                        vein>.28?Blocks.STONE.getStateFromMeta(5):vein<-.42?Blocks.COBBLESTONE.getDefaultState():Blocks.STONE.getDefaultState());
            }
        }
    }
    static final class Sample {
        final double height,blend;final boolean rock;
        Sample(double height,double blend,boolean rock) {this.height=height;this.blend=blend;this.rock=rock;}
    }
    private static final class Ridge {
        final int width,saddleDepth;final int[][] points;
        Ridge(int width,int[][] points) {this(width,18,points);}
        Ridge(int width,int saddleDepth,int[][] points) {this.width=width;this.saddleDepth=saddleDepth;this.points=points;}
    }
}
