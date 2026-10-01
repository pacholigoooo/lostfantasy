package dev.lostfantasy.world.gensokyo;

import java.util.*;
import static dev.lostfantasy.world.gensokyo.GensokyoAtlas.*;

/** Shared grading and paving geometry. Segments are indexed, never scanned world-wide per column. */
public final class GensokyoRoads {
    public static final GensokyoRoads INSTANCE=new GensokyoRoads();
    private final Map<Long,List<Segment>> cells=new HashMap<>();
    private final List<Segment> all=new ArrayList<>();
    private GensokyoRoads() {
        GensokyoAtlas v=VILLAGE;
        // Each rural road enters at a street end instead of cutting diagonally across house blocks.
        line(v.x+448,v.z+24,83,v.x+500,v.z+24,83,4);
        line(v.x+500,v.z+24,83,KOURINDOU.x,KOURINDOU.approachZ(),KOURINDOU.y,4);
        route(4,KOURINDOU,MYSTIA,YOUKAI_TRAIL,HAKUREI);
        line(HAKUREI.x+12,HAKUREI.z-86,HAKUREI.y,FAIRY_TREE.x,FAIRY_TREE.approachZ(),FAIRY_TREE.y,2);
        line(v.x+24,v.z-210,83,v.x+24,v.z-282,84,4);
        line(v.x+24,v.z-282,84,MYOUREN.x,MYOUREN.approachZ(),MYOUREN.y,4);
        route(4,MYOUREN,ROPEWAY,AQUATIC_MARKET,GEYSER,CUCUMBER_FACTORY);
        // Approach the cliff town below its houses, then cross the spring pool into the south gate.
        line(CUCUMBER_FACTORY.x,CUCUMBER_FACTORY.approachZ(),132,-865,-2140,110,3);
        line(-865,-2140,110,-1110,-2045,117,3);
        line(-1110,-2045,117,TENGU.x,TENGU.approachZ(),TENGU.y,3);
        // The mountain path skirts the cataract instead of forming a steep bridge along its flow.
        line(CUCUMBER_FACTORY.x,CUCUMBER_FACTORY.approachZ(),132,-880,-2520,95,2);
        line(-880,-2520,95,-860,-2660,90,2);
        line(-860,-2660,90,-846,-2770,102,2);
        int[] viewFoot=MountainCascade.VIEW_APPROACH[0],viewLanding=MountainCascade.VIEW_APPROACH[1];
        line(viewFoot[0],viewFoot[1],viewFoot[2],viewLanding[0],viewLanding[1],viewLanding[2],2);
        line(-846,-2770,102,-868,-2895,136,2);
        line(-868,-2895,136,-760,-2970,179,2);
        line(-760,-2970,179,MORIYA.x,MORIYA.approachZ(),179,2);
        // Descend into the now lower valleys instead of leaving a high embankment between shelves.
        line(MORIYA.x,MORIYA.approachZ(),MORIYA.y,-408,-3000,177,2);
        line(-408,-3000,177,-200,-3010,151,2);
        line(-200,-3010,151,-40,-2950,95,2);
        line(-40,-2950,95,160,-2890,95,2);
        line(160,-2890,95,285,-2760,137,2);
        line(285,-2760,137,NEMUNO.x,NEMUNO.approachZ(),NEMUNO.y,2);
        line(NEMUNO.x,NEMUNO.approachZ(),NEMUNO.y,540,-2830,162,3);
        line(540,-2830,162,600,-3010,129,3);
        line(600,-3010,129,870,-3060,111,2);
        line(870,-3060,111,970,-3160,155,2);
        line(970,-3160,155,KASEN.x,KASEN.approachZ(),KASEN.y,2);
        line(ROPEWAY_MOUNTAIN.x,ROPEWAY_MOUNTAIN.approachZ(),ROPEWAY_MOUNTAIN.y,MORIYA.x,MORIYA.approachZ(),MORIYA.y,3);
        // The ascent and its supporting spur share elevations, including every switchback landing.
        int[][] summitTrail=YoukaiMountain.SUMMIT_TRAIL;
        for(int i=1;i<summitTrail.length;i++) {
            int[] a=summitTrail[i-1],b=summitTrail[i];
            line(a[0],a[1],a[2],b[0],b[1],b[2],2);
        }
        line(MORIYA.x+132,MORIYA.z,179,MORIYA.x+186,MORIYA.z,175,3);
        line(NEMUNO.x,NEMUNO.approachZ(),NEMUNO.y,487,-2580,135,2);
        line(487,-2580,135,340,-2510,100,2);
        line(340,-2510,100,400,-2450,91,2);
        line(400,-2450,91,340,-2350,96,2);
        line(340,-2350,96,KAPPA.x+108,KAPPA.z-70,126,2);
        line(KAPPA.x,KAPPA.approachZ(),KAPPA.y,PEONY_FIELD.x,PEONY_FIELD.approachZ(),PEONY_FIELD.y,3);
        line(PEONY_FIELD.x,PEONY_FIELD.approachZ(),118,MAYOHIGA.x-168,MAYOHIGA.z+140,128,3);
        line(MAYOHIGA.x-168,MAYOHIGA.z+140,128,MAYOHIGA.x,MAYOHIGA.approachZ(),128,3);
        line(MAYOHIGA.x,MAYOHIGA.approachZ(),128,MAYOHIGA.x+168,MAYOHIGA.z+148,128,3);
        line(MAYOHIGA.x+168,MAYOHIGA.z+148,128,YAKUMO.x,YAKUMO.approachZ(),158,3);
        line(KAPPA.x+84,KAPPA.z+112,KAPPA.y,KAPPA.x+108,KAPPA.z+112,KAPPA.y,3);
        line(KAPPA.x+108,KAPPA.z+112,KAPPA.y,KAPPA.x+108,KAPPA.z-70,126,3);
        line(KAPPA.x+108,KAPPA.z-70,126,480,-2370,134,2);
        line(480,-2370,134,650,-2480,150,2);
        line(650,-2480,150,785,-2550,128,2);
        line(785,-2550,128,822,-2490,137,2);
        line(822,-2490,137,846,-2390,172,2);
        line(846,-2390,172,898,-2340,181,2);
        line(898,-2340,181,RAINBOW_CAVE.x,RAINBOW_CAVE.approachZ(),RAINBOW_CAVE.y,2);
        // Join the high shelf once; the eastern switchback serves the lower cliff terrace.
        line(RAINBOW_CAVE.x,RAINBOW_CAVE.approachZ(),188,1060,-2430,185,2);
        line(1060,-2430,185,FALSE_HEAVEN.x,FALSE_HEAVEN.approachZ(),181,2);
        line(SECRET_CLIFF.x,SECRET_CLIFF.approachZ(),132,SECRET_CLIFF.x+190,SECRET_CLIFF.approachZ(),132,3);
        line(SECRET_CLIFF.x+190,SECRET_CLIFF.approachZ(),132,SECRET_CLIFF.x+208,SECRET_CLIFF.z-80,156,3);
        line(SECRET_CLIFF.x+208,SECRET_CLIFF.z-80,156,SECRET_CLIFF.x+34,SECRET_CLIFF.z-138,179,3);
        line(SECRET_CLIFF.x+34,SECRET_CLIFF.z-138,179,FALSE_HEAVEN.x,FALSE_HEAVEN.approachZ(),181,3);
        // Keep the onward path on the upper shelf; a southward bend would cross the lower switchback.
        line(FALSE_HEAVEN.x,FALSE_HEAVEN.approachZ(),181,YAKUMO.x-312,YAKUMO.z+41,171,3);
        line(YAKUMO.x-312,YAKUMO.z+41,171,YAKUMO.x,YAKUMO.approachZ(),158,3);
        // Approach the small shrine from the bank, keeping the lotus pond unbridged.
        line(MAYOHIGA.x,MAYOHIGA.approachZ(),128,TOAD_POND.x+104,TOAD_POND.z-96,110,2);
        line(TOAD_POND.x+104,TOAD_POND.z-96,110,TOAD_POND.x+104,TOAD_POND.z+32,108,2);
        line(TOAD_POND.x+104,TOAD_POND.z+32,108,1140,-1330,99,2);
        line(1140,-1330,99,SNOW_ROAD.x,SNOW_ROAD.z,119,2);
        route(3,SNOW_ROAD,MYOUREN);
        route(3,ROPEWAY,SCARLET,RUINED_MANSION,AQUATIC_MARKET);
        line(ROPEWAY.x,ROPEWAY.approachZ(),87,-610,-396,87,3);
        line(-610,-396,87,-314,-396,86,3);
        line(-314,-396,86,-314,-34,84,3);
        line(-314,-34,84,VILLAGE.x-VILLAGE.rx+12,VILLAGE.z-162,83,3);
        line(-314,-396,86,-196,-396,86,3);
        line(-576,-274,85,-314,-274,85,2);line(-314,-274,85,112,-274,85,2);
        line(-560,-158,84,-314,-158,84,2);line(-314,-158,84,134,-158,84,2);
        // Shore path and a short raised boardwalk approach the ice house from the south.
        line(ROPEWAY.x,ROPEWAY.approachZ(),ROPEWAY.y,CIRNO.x+125,CIRNO.z+115,82,2);
        line(CIRNO.x+125,CIRNO.z+115,82,CIRNO.x,CIRNO.z+115,77,2);
        line(CIRNO.x,CIRNO.z+115,77,CIRNO.x,CIRNO.z+60,74,2);
        line(CIRNO.x,CIRNO.z+60,74,CIRNO.x,CIRNO.z+34,74,2);
        line(RUINED_MANSION.x,RUINED_MANSION.approachZ(),116,LIMINAL_ROAD.x+170,LIMINAL_ROAD.z+210,114,3);
        line(LIMINAL_ROAD.x+170,LIMINAL_ROAD.z+210,114,LIMINAL_ROAD.x,LIMINAL_ROAD.z+140,108,3);
        line(LIMINAL_ROAD.x,LIMINAL_ROAD.z+140,108,LIMINAL_ROAD.x,LIMINAL_ROAD.z-140,108,4);
        line(LIMINAL_ROAD.x,LIMINAL_ROAD.z-140,108,SAI_BANK.x,SAI_BANK.z+115,92,3);
        line(SAI_BANK.x,SAI_BANK.z+115,92,SAI_BANK.x,SAI_BANK.z-110,92,3);
        line(SAI_BANK.x,SAI_BANK.z+115,92,SANZU_PIER.x+85,SANZU_PIER.z+75,76,3);
        line(SANZU_PIER.x+85,SANZU_PIER.z+75,76,SANZU_PIER.x,SANZU_PIER.approachZ(),76,3);
        line(v.x-458,v.z-78,83,v.x-506,v.z-102,82,3);
        line(v.x-506,v.z-102,82,WILLOW_CANAL.x,WILLOW_CANAL.z,WILLOW_CANAL.y,3);
        route(3,WILLOW_CANAL,SPRING_PATH,CHERRY_PATH,MARISA,GHOST_HOUSE);
        route(2,MARISA,EARTH_RAINBOW,LARVA,ALICE,FAIRY_SHRINE,WITCH_MANOR,SUN_GARDEN);
        route(2,MARISA,FAIRY_OLD_TREE,ALICE);
        route(2,ALICE,MOKOU);
        route(2,FAIRY_SHRINE,NAMELESS_HILL,SUZURAN);
        line(v.x,v.approachZ(),83,v.x+96,v.z+302,84,3);
        line(v.x+96,v.z+302,84,MOKOU.x,MOKOU.approachZ(),MOKOU.y,3);
        route(3,MOKOU,EIENTEI,NETHER_GATE);
        for(int i=1;i<Muenzuka.PATH.length;i++) {
            int[] a=Muenzuka.PATH[i-1],b=Muenzuka.PATH[i];
            line(a[0],a[1],a[2],b[0],b[1],b[2],2);
        }
        route(2,KOURINDOU,TANUKI_FOREST,SNOW_ROAD);
        line(TANUKI_FOREST.x,TANUKI_FOREST.z,TANUKI_FOREST.y,
                TANUKI_FOREST.x+TanukiClearing.DX-26,TANUKI_FOREST.z+TanukiClearing.DZ,TANUKI_FOREST.y,2);
        route(2,AQUATIC_MARKET,BLOWHOLE);
        line(BLOWHOLE.x,BLOWHOLE.approachZ(),BLOWHOLE.y,-1040,-1600,109,2);
        line(-1040,-1600,109,GEYSER.x,GEYSER.approachZ(),GEYSER.y,2);
        for(int[] s:VillageLayout.STREETS)
            line(v.x+s[0],v.z+s[1],v.y,v.x+s[2],v.z+s[3],v.y,s[4]);
        for(GensokyoAtlas house:new GensokyoAtlas[]{TERAKOYA,SUZUNAAN,HIEDA})
            line(house.x,house.approachZ(),83,house.x,v.z+(house==HIEDA?215:176),83,3);
    }
    private void route(int width,GensokyoAtlas... sites) {
        for(int i=1;i<sites.length;i++) {
            GensokyoAtlas a=sites[i-1],b=sites[i];
            int az=a.grounded()?a.approachZ():a.z,bz=b.grounded()?b.approachZ():b.z;
            // A small bend breaks long sight lines without making the road oscillate.
            double dx=b.x-a.x,dz=bz-az,len=Math.hypot(dx,dz),bend=Math.min(65,len*.09);
            double mx=(a.x+b.x)*.5-(len==0?0:dz/len*bend),mz=(az+bz)*.5+(len==0?0:dx/len*bend);
            boolean contour=mountainRoute(a.x,az,b.x,bz);
            line(a.x,az,a.y,mx,mz,(a.y+b.y)*.5,width,contour);
            line(mx,mz,(a.y+b.y)*.5,b.x,bz,b.y,width,contour);
        }
    }
    private static boolean mountainRoute(double ax,double az,double bx,double bz) {
        return Math.min(az,bz)<-1000;
    }
    private void line(double ax,double az,double ay,double bx,double bz,double by,int width) {
        line(ax,az,ay,bx,bz,by,width,mountainRoute(ax,az,bx,bz));
    }
    private void line(double ax,double az,double ay,double bx,double bz,double by,int width,boolean contour) {
        Segment s=new Segment(ax,az,ay,bx,bz,by,width,contour); all.add(s);
        int shoulder=s.contour?28:7;
        for(int x=(int)Math.floor((Math.min(ax,bx)-width-shoulder)/128);x<=Math.floor((Math.max(ax,bx)+width+shoulder)/128);x++)
            for(int z=(int)Math.floor((Math.min(az,bz)-width-shoulder)/128);z<=Math.floor((Math.max(az,bz)+width+shoulder)/128);z++)
                cells.computeIfAbsent(GensokyoAtlas.key(x,z),k->new ArrayList<>()).add(s);
    }
    public Sample at(int x,int z) {
        List<Segment> list=cells.get(GensokyoAtlas.key(x>>7,z>>7));if(list==null)return null;
        Sample best=null;
        for(Segment s:list) {
            double dx=s.bx-s.ax,dz=s.bz-s.az;
            double t=Math.max(0,Math.min(1,((x-s.ax)*dx+(z-s.az)*dz)/(dx*dx+dz*dz)));
            double d=Math.hypot(x-(s.ax+t*dx),z-(s.az+t*dz));
            if(d<=s.width+(s.contour?28:7) && (best==null || d-s.width<best.distance-best.width))
                best=new Sample(d,t,s);
        }
        return best;
    }
    public List<Segment> segments() {return Collections.unmodifiableList(all);}
    boolean fixedJunction(double x,double z) {
        for(Segment s:all)if(!s.contour && (s.ax==x && s.az==z || s.bx==x && s.bz==z))return true;
        return false;
    }
    public static final class Segment {
        public final double ax,az,ay,bx,bz,by;
        public final int width;
        final boolean contour;
        Segment(double ax,double az,double ay,double bx,double bz,double by,int width,boolean contour) {
            this.ax=ax;this.az=az;this.ay=ay;this.bx=bx;this.bz=bz;this.by=by;this.width=width;
            this.contour=contour;
        }
    }
    public static final class Sample {
        public final double distance,height; public final int width;
        final double along;final Segment segment;
        Sample(double distance,double along,Segment segment) {
            this.distance=distance;this.width=segment.width;this.height=GensokyoNoise.lerp(segment.ay,segment.by,along);
            this.along=along;this.segment=segment;
        }
        public boolean paved() {return distance<=width;}
        double grade(double ground,double deck) {
            double shoulder=segment.contour?7+Math.min(21,Math.abs(deck-ground)*.55):7;
            return GensokyoNoise.lerp(ground,deck,1-GensokyoNoise.smooth((distance-width)/shoulder));
        }
    }
}
