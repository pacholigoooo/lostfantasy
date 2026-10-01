package dev.lostfantasy.world.gensokyo;

import static dev.lostfantasy.world.gensokyo.GensokyoAtlas.*;

/** Continuous, seed-stable terrain shared by chunks, ecology, routes and offline previews. */
public final class GensokyoTerrain {
    public enum Region { MOUNTAIN, FOREST, BAMBOO, MEADOW, FLOWERS, VILLAGE, HAKUREI, LAKE, RIVER }
    private final GensokyoNoise noise;
    private final java.util.concurrent.ConcurrentMap<GensokyoRoads.Segment,GensokyoRoadProfile> roadProfiles=new java.util.concurrent.ConcurrentHashMap<>();
    // Immutable entries make a concurrent miss harmless; the cache never retains a World.
    private final CachedColumn[] columns=new CachedColumn[4096];
    private static final int BAMBOO_X=EIENTEI.x-32,BAMBOO_Z=EIENTEI.z-128,BAMBOO_RX=640,BAMBOO_RZ=530;
    static boolean nearBamboo(int x,int z,int margin) {
        return Math.abs((long)x-BAMBOO_X)<=BAMBOO_RX+125+margin && Math.abs((long)z-BAMBOO_Z)<=BAMBOO_RZ+125+margin;
    }
    private static final Lake[] LAKES={
        new Lake(MIST_LAKE.x,MIST_LAKE.z,575,620,72,15),
        new Lake(CIRNO.x-80,CIRNO.z+4,148,92,72,7),
        new Lake(WIND_LAKE.x,WIND_LAKE.z,218,132,173,13),
        new Lake(TOAD_POND.x,TOAD_POND.z,60,44,104,7),
        new Lake(ABANDONED_POND.x,ABANDONED_POND.z,49,37,80,5)
    };
    private static final Stream[] STREAMS={
        stream(461,82,173,423,132,173,21),
        // The waterfall's enclosed pool has no outlet; this lower creek starts beyond its ridge.
        stream(438,179,101,455,274,96,14),stream(455,274,96,432,382,85,12),
        stream(432,382,85,369,450,72,12),stream(369,450,72,330,480,72,12),stream(330,480,72,459,493,76,9),
        stream(459,493,76,662,487,78,7),stream(285,550,72,203,695,68,13),
        stream(203,695,68,144,805,65,13),stream(144,805,65,90,1005,62,16),
        stream(66,-200,71,66,57,71,43),stream(66,57,71,48,236,71,43),stream(48,236,71,-60,355,68,40)
    };
    public GensokyoTerrain(long seed) {noise=new GensokyoNoise(seed);}
    public long hash(int x,int z,int salt) {return noise.hash(x,z,salt);}
    double patchNoise(int x,int z,int scale,int salt) {return noise.value(x,z,scale,salt);}
    public Column column(int x,int z) {
        int slot=(x*73428767 ^ z*912931)&(columns.length-1);
        CachedColumn cached=columns[slot];
        if(cached!=null && cached.x==x && cached.z==z)return cached.column;
        Column result=calculateColumn(x,z,true);
        columns[slot]=new CachedColumn(x,z,result);
        return result;
    }
    Column naturalColumn(int x,int z) {return calculateColumn(x,z,false);}
    double roadJunction(double x,double z,double planned) {
        int wx=(int)Math.round(x),wz=(int)Math.round(z);
        for(GensokyoAtlas site:plots(wx,wz))
            if(site.grounded() && site.contains(wx,wz,16))return planned;
        if(GensokyoRoads.INSTANCE.fixedJunction(x,z) || TENGU.contains(wx,wz,8) || Math.hypot(wx-MOUNTAIN_TOP.x,wz-MOUNTAIN_TOP.z)<65
                || wx==WATERFALL.x-180 && wz==WATERFALL.z+20)return planned;
        Column c=naturalColumn(wx,wz);return c.wet()?c.water+2:c.ground;
    }
    private Column calculateColumn(int x,int z,boolean includeRoads) {
        double h=FlowerLandscapes.shape(x,z,base(x,z),noise),water=-1,navigation=-1;
        h=PhantomMeadow.shape(x,z,h,noise);
        h=TanukiClearing.shape(x,z,h);
        YoukaiMountain.Sample mountain=YoukaiMountain.at(x,z,noise);
        if(mountain!=null)h=GensokyoNoise.lerp(h,mountain.height,mountain.blend);
        ForestWetlands.Sample wetland=ForestWetlands.at(x,z,noise);
        if(wetland!=null) {
            int level=(int)Math.floor(base(wetland.x,wetland.z))-1;
            double d=wetland.distance,bed=level-1-2.5*Math.max(0,1-d*d)+wetland.mound;
            h=GensokyoNoise.lerp(bed,Math.max(h,level+2),GensokyoNoise.smooth((d-.8)/.55));
            if(d<1.13)water=level;
        }
        double shore=noise.value(x,z,94,13)*.065+noise.value(x,z,31,21)*.018;
        for(Lake lake:LAKES) {
            double lx=(x-lake.x)/(double)lake.rx,lz=(z-lake.z)/(double)lake.rz;
            double angle=Math.atan2(lz,lx);
            // Broad coves and wooded headlands, with the existing smaller shoreline noise on top.
            double coves=(lake.rx>200?.065:.028)*Math.sin(angle*3+.6)
                    +(lake.rx>200?.032:.014)*Math.cos(angle*5-1.1);
            double d=Math.hypot(lx,lz)+coves+shore;
            navigation=Math.max(navigation,lake.y+2-Math.max(0,d-1)*Math.min(lake.rx,lake.rz)*.35);
            if(d<1.16) {
                double lakeBed=lake.y-lake.depth*Math.sqrt(Math.max(0,1-d*d));
                double shaped=GensokyoNoise.lerp(lakeBed,Math.max(h,lake.y+3),GensokyoNoise.smooth((d-.97)/.19));
                // An inlet must not raise a dry rim across water already belonging to the lake.
                h=water>=0?Math.min(h,shaped):shaped;
                if(d<1)water=Math.max(water,lake.y);
            }
        }
        for(Stream s:STREAMS) {
            if(x<s.minX || x>s.maxX || z<s.minZ || z>s.maxZ)continue;
            double dx=s.bx-s.ax,dz=s.bz-s.az;
            double t=Math.max(0,Math.min(1,((x-s.ax)*dx+(z-s.az)*dz)/(dx*dx+dz*dz)));
            double d=Math.hypot(x-s.ax-t*dx,z-s.az-t*dz),level=GensokyoNoise.lerp(s.ay,s.by,t);
            navigation=Math.max(navigation,level+2-Math.max(0,d-s.width)*.35);
            if(d<s.width+17) {
                double shaped=GensokyoNoise.lerp(level-4,Math.max(h,level+2),GensokyoNoise.smooth((d-s.width)/17));
                h=water>=0?Math.min(h,shaped):shaped;
                if(d<s.width)water=Math.max(water,Math.floor(level));
            }
        }
        h=SanzuCoast.shape(x,z,h,noise);
        if(SanzuCoast.water(x,z))water=71;
        MountainCascade.Sample cascade=MountainCascade.at(x,z,noise);
        if(cascade!=null) {
            h=GensokyoNoise.lerp(h,cascade.ground,cascade.blend);
            if(cascade.water>=0)water=cascade.water;
        }
        h=MountainCascade.approach(x,z,h);
        VillageCanal.Sample canal=VillageCanal.at(x,z);
        if(canal!=null)navigation=Math.max(navigation,canal.level+2-Math.max(0,canal.distance-6)*.35);
        KappaWatercourse.Sample tributary=KappaWatercourse.at(x,z);
        if(tributary!=null) {
            navigation=Math.max(navigation,tributary.level+3-Math.max(0,tributary.distance-9)*.35);
            if(tributary.distance<23)h=GensokyoNoise.lerp(tributary.level-4,Math.max(h,tributary.level+2),GensokyoNoise.smooth((tributary.distance-9)/14));
            if(tributary.distance<9)water=Math.max(water,Math.floor(tributary.level));
        }
        GenbuRavine.Sample genbu=GenbuRavine.at(x,z,tributary,noise);
        if(genbu!=null)h=GensokyoNoise.lerp(h,genbu.ground,genbu.blend);
        SecretHighland.Sample highland=SecretHighland.at(x,z,noise);
        if(highland!=null)h=GensokyoNoise.lerp(h,highland.ground,highland.blend);
        double mineRidge=RainbowRidge.height(x,z,h,noise);
        if(mineRidge>=0)h=Math.max(h,mineRidge);
        h=ToadPond.bank(x,z,h,noise);
        h=RicePaddies.shape(x,z,h);
        RicePaddies.Sample paddy=RicePaddies.at(x,z);
        if(paddy!=null) {h=paddy.ground;water=paddy.water;}
        GensokyoRoads.Sample road=includeRoads?GensokyoRoads.INSTANCE.at(x,z):null;
        double roadHeight=-1;
        if(road!=null)roadHeight=road.segment.contour?
                roadProfiles.computeIfAbsent(road.segment,s->new GensokyoRoadProfile(s,this)).height(road.along):Math.max(road.height,navigation);
        for(GensokyoAtlas plot:plots(x,z)) {
            if(!plot.grounded() || plot==TENGU)continue;
            double d=Math.max(Math.abs((double)x-plot.x)-plot.rx,Math.abs((double)z-plot.z)-plot.rz);
            if(d<48) {
                double blend=1-GensokyoNoise.smooth(d/48);
                h=GensokyoNoise.lerp(h,plot.y,blend);
                roadHeight=GensokyoNoise.lerp(roadHeight,plot.y,blend);
            }
            if(d<=0)water=-1;
        }
        // Canal cuts come after settlement grading; individual buildings remain on the dry banks.
        if(canal!=null && canal.distance<6) {h=canal.level-3;water=Math.floor(canal.level);}
        // The workshop straddles a real tributary; keep its dry rooms on either bank of the pad.
        if(tributary!=null && tributary.distance<9) {h=Math.min(h,tributary.level-4);water=Math.max(water,Math.floor(tributary.level));}
        else if(tributary!=null && tributary.distance<11)h=Math.max(h,Math.floor(tributary.level));
        TenguTerrain.Sample tengu=TenguTerrain.at(x,z,noise);
        if(tengu!=null) {
            h=GensokyoNoise.lerp(h,tengu.ground,tengu.blend);
            if(tengu.blend>.99)water=tengu.water;
        }
        // Apply earthworks once, after natural landforms and building terraces. Leave beds beneath bridges.
        if(road!=null) {
            if(water>h)roadHeight=Math.max(roadHeight,water+2);
            else h=road.grade(h,roadHeight);
        }
        int top=(int)Math.floor(Math.max(8,Math.min(244,h)));
        int waterY=water>=top+1?(int)water:-1;
        Region region=region(x,z);
        if(waterY>=0)region=lake(x,z)?Region.LAKE:Region.RIVER;
        return new Column(top,waterY,region,road,waterY>top?(int)Math.floor(roadHeight):top,
                canal!=null && canal.distance>=6 && canal.distance<8,
                cascade!=null && cascade.falling || tengu!=null && tengu.falling,
                (tengu!=null && tengu.blend>.99 && tengu.rock) || (cascade!=null && cascade.rock) || (genbu!=null && genbu.basalt)
                        || mineRidge>=h-1 && mineRidge>=155 || highland!=null && highland.cliff
                        || mountain!=null && mountain.rock && mountain.blend>.8 && Math.abs(h-mountain.height)<2,
                genbu!=null && genbu.basalt,wetland!=null);
    }
    private double base(int x,int z) {
        double north=GensokyoNoise.smooth((-z-830)/1450.0);
        double warpX=x+noise.value(x,z,580,40)*180,warpZ=z+noise.value(x,z,640,41)*180;
        double ridge=1-Math.abs(noise.value(warpX,warpZ,360,4));
        double rim=GensokyoNoise.smooth((Math.hypot(x/3400.0,z/3650.0)-.80)/.20);
        return 85+noise.value(x,z,480,1)*13+noise.value(x,z,110,2)*5+noise.value(x,z,33,3)*1.7
                +rim*(38+ridge*29)*(1-.7*north)
                +13*hill(x,z,EIENTEI.x+144,EIENTEI.z-200,520,710)
                +21*hill(x,z,HAKUREI.x+6,HAKUREI.z-84,440,380);
    }
    private static double hill(int x,int z,double cx,double cz,double rx,double rz) {
        double dx=(x-cx)/rx,dz=(z-cz)/rz;return Math.exp(-dx*dx-dz*dz);
    }
    public Region region(int x,int z) {
        double wx=x+noise.value(x,z,360,72)*125,wz=z+noise.value(x,z,320,73)*125;
        if(VILLAGE.contains(x,z,35))return Region.VILLAGE;
        if(HAKUREI.contains(x,z,110))return Region.HAKUREI;
        if(Muenzuka.forest(x,z))return Region.FOREST;
        if(SecretHighland.forest(x,z))return Region.FOREST;
        if(PhantomMeadow.contains(x,z))return Region.MEADOW;
        if(YoukaiWoodlands.contains(x,z))return Region.FOREST;
        if(ellipse(wx,wz,SEA_OF_TREES.x,SEA_OF_TREES.z,490,590)<1
                || ellipse(wx,wz,-30,-1730,470,380)<1)return Region.FOREST;
        if(wz<-930+180*Math.sin(wx/540))return Region.MOUNTAIN;
        if(ellipse(wx,wz,BAMBOO_X,BAMBOO_Z,BAMBOO_RX,BAMBOO_RZ)<1)return Region.BAMBOO;
        // The fairies' old signal tower lies in the forest, north of the flower fields.
        if(ellipse(wx,wz,FAIRY_SHRINE.x,FAIRY_SHRINE.z,240,285)<1)return Region.FOREST;
        if(FlowerLandscapes.flowers(x,z))return Region.FLOWERS;
        if(ellipse(wx,wz,-256,1264,1100,710)<1 || ellipse(wx,wz,1136,784,500,430)<1)return Region.FOREST;
        if(ellipse(wx,wz,TANUKI_FOREST.x,TANUKI_FOREST.z,390,320)<1)return Region.FOREST;
        return Region.MEADOW;
    }
    private static double ellipse(double x,double z,double cx,double cz,double rx,double rz) {
        double dx=(x-cx)/rx,dz=(z-cz)/rz;return dx*dx+dz*dz;
    }
    private static boolean lake(int x,int z) {
        for(Lake l:LAKES)if(Math.abs((long)x-l.x)<l.rx*1.18+20 && Math.abs((long)z-l.z)<l.rz*1.18+20)return true;
        return false;
    }
    private static Stream stream(int ax,int az,int ay,int bx,int bz,int by,int width) {
        return new Stream(mapX(ax),mapZ(az),ay,mapX(bx),mapZ(bz),by,width);
    }
    public static final class Column {
        public final int ground,water,roadY;public final Region region;public final GensokyoRoads.Sample road;
        public final boolean canalBank,falling,rockSurface,basalt,wetland;
        Column(int ground,int water,Region region,GensokyoRoads.Sample road,int roadY,boolean canalBank,boolean falling,boolean rockSurface,boolean basalt,boolean wetland) {
            this.ground=ground;this.water=water;this.region=region;this.road=road;this.roadY=roadY;
            this.canalBank=canalBank;this.falling=falling;this.rockSurface=rockSurface;this.basalt=basalt;this.wetland=wetland;
        }
        public boolean wet() {return water>ground;}
        public int surface() {return Math.max(ground,water);}
        public boolean path() {return road!=null && road.paved();}
    }
    private static final class CachedColumn {
        final int x,z;final Column column;
        CachedColumn(int x,int z,Column column) {this.x=x;this.z=z;this.column=column;}
    }
    private static final class Lake {
        final int x,z,rx,rz,y,depth;
        Lake(int x,int z,int rx,int rz,int y,int depth) {this.x=x;this.z=z;this.rx=rx;this.rz=rz;this.y=y;this.depth=depth;}
    }
    private static final class Stream {
        final int ax,az,ay,bx,bz,by,width,minX,maxX,minZ,maxZ;
        Stream(int ax,int az,int ay,int bx,int bz,int by,int width) {
            this.ax=ax;this.az=az;this.ay=ay;this.bx=bx;this.bz=bz;this.by=by;this.width=width;
            minX=Math.min(ax,bx)-width-512;maxX=Math.max(ax,bx)+width+512;
            minZ=Math.min(az,bz)-width-512;maxZ=Math.max(az,bz)+width+512;
        }
    }
}
