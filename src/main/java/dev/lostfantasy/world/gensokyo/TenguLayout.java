package dev.lostfantasy.world.gensokyo;

import java.util.*;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

/** Individually sited cliff houses and two winding lanes, joined across the ravine at unequal intervals. */
final class TenguLayout {
    static final Plot[] HOMES={
        home(-253,310,130,18,11,1,0,2),home(-172,308,130,12,13,3,1,2),
        home(-94,308,125,14,10,2,2,2),home(-324,276,134,12,17,2,3,2),
        home(-345,195,142,11,15,1,4,3),home(-279,65,160,19,11,2,5,0),
        home(-323,-3,182,12,14,3,1,3),home(-176,-19,180,14,12,2,6,3),
        home(-274,-106,202,16,10,1,7,1),home(-128,-110,199,12,11,3,1,2),
        home(-342,-222,216,13,10,1,5,3),home(-126,-240,216,18,13,2,6,0),
        home(112,323,125,16,11,1,7,2),home(210,333,131,23,12,2,2,2),
        home(315,324,135,12,16,3,1,2),home(187,201,146,16,11,2,3,2),
        home(200,98,158,13,10,1,5,1),home(333,40,181,12,12,2,4,1),
        home(277,-27,185,16,11,1,6,3),home(210,-151,205,13,12,2,0,0),
        home(314,-168,211,18,12,2,7,2),home(116,-230,216,11,10,2,4,3),
        home(257,-250,220,19,13,1,3,0)
    };
    static final Plot PRINT=new Plot("印务院",-223,22,168,34,20,2,5,0),
            NEWS=new Plot("取材报社",265,81,174,33,22,3,6,2),
            COUNCIL=new Plot("议事会馆",-225,-213,214,42,20,2,0,0),
            INN=new Plot("山客宿屋",256,221,140,30,19,2,7,0),
            PATROL=new Plot("轮值舍",-222,169,147,20,17,2,2,0),
            TEA=new Plot("听涧茶院",208,-59,187,29,18,1,3,3),
            PAPER=new Plot("抄纸作坊",131,-55,180,18,11,1,5,2),
            WEST_TOWER=new Plot("上崖巡山楼",-350,-160,210,13,13,3,1,0),
            EAST_TOWER=new Plot("谷口瞭望楼",329,113,153,12,12,3,1,2);
    static final Plot[] PUBLIC={PRINT,NEWS,COUNCIL,INN,PATROL,TEA,PAPER,WEST_TOWER,EAST_TOWER};
    static final Bridge[] BRIDGES={
        new Bridge("谷口廊桥",-68,119,269,125,4,0),
        new Bridge("编印拱桥",-108,146,58,170,3,1),
        new Bridge("上崖吊桥",-92,112,-128,206,3,2)
    };
    static final List<Path> PATHS=new ArrayList<>();
    private static final Map<Long,List<Plot>> PLOTS=new HashMap<>();
    private static final Map<Long,List<Path>> ROUTES=new HashMap<>();
    static {
        lane(new int[][]{{-68,269,125},{-132,251,129},{-203,259,132},{-281,224,137},
                {-310,153,147},{-261,97,155},{-176,82,164},{-108,58,170},
                {-125,-28,181},{-217,-62,193},{-302,-48,197},{-322,-111,203},
                {-260,-148,210},{-168,-185,214},{-92,-128,206}});
        lane(new int[][]{{119,269,125},{174,285,130},{278,270,134},{317,208,143},
                {268,169,148},{183,142,152},{120,103,158},{146,58,170},
                {211,4,177},{297,20,181},{315,-61,190},{252,-104,199},
                {171,-113,202},{112,-128,206},{190,-222,216}});
        path(0,331,121,0,269,125,4);
        List<Path> lanes=new ArrayList<>(PATHS);
        for(Plot p:all()) {
            int margin=44;
            index(PLOTS,p,p.x-p.rx()-margin,p.z-p.rz()-margin,p.x+p.rx()+margin,p.z+p.rz()+margin);
            BlockPos door=p.local(0,0,p.d+6),front=p.local(0,0,p.d+(p==TEA?22:14));
            int fx=front.getX()-door.getX(),fz=front.getZ()-door.getZ();
            Path best=null;double bestT=0,score=Double.MAX_VALUE;
            for(Path route:lanes) {
                double t=route.t(front.getX(),front.getZ());
                double x=route.x+t*(route.bx-route.x),z=route.z+t*(route.bz-route.z);
                if((x-door.getX())*fx+(z-door.getZ())*fz < -1)continue;
                double cost=Math.hypot(x-front.getX(),z-front.getZ())+Math.abs(route.height(t)-p.y)*2;
                if(cost<score) {score=cost;best=route;bestT=t;}
            }
            if(best==null)throw new IllegalStateException("No lane in front of "+p.name);
            int tx=(int)Math.round(best.x+bestT*(best.bx-best.x)),tz=(int)Math.round(best.z+bestT*(best.bz-best.z));
            path(door.getX(),door.getZ(),p.y,front.getX(),front.getZ(),p.y,2);
            path(front.getX(),front.getZ(),p.y,tx,tz,best.height(bestT),2);
        }
    }
    private TenguLayout() {}
    private static Plot home(int x,int z,int y,int w,int d,int floors,int form,int turn) {
        return new Plot("山居"+x+"·"+z,x,z,y,w,d,floors,form,turn);
    }
    static List<Plot> all() {List<Plot> result=new ArrayList<>(Arrays.asList(HOMES));result.addAll(Arrays.asList(PUBLIC));return result;}
    static GensokyoArchitecture at(GensokyoBlueprint plan,Plot p) {
        return new GensokyoArchitecture(plan,GensokyoAtlas.TENGU,p.x,p.z,GensokyoAtlas.TENGU.title+"·"+p.name,p.rotation(),p.y-GensokyoAtlas.TENGU.y);
    }
    private static void lane(int[][] nodes) {
        for(int i=1;i<nodes.length;i++) {
            int[] a=nodes[i-1],b=nodes[i];path(a[0],a[1],a[2],b[0],b[1],b[2],3);
        }
    }
    private static void path(int x,int z,int y,int bx,int bz,int by,int width) {
        Path p=new Path(x,z,y,bx,bz,by,width);PATHS.add(p);
        index(ROUTES,p,Math.min(x,bx)-width-8,Math.min(z,bz)-width-8,Math.max(x,bx)+width+8,Math.max(z,bz)+width+8);
    }
    static double grade(int x,int z,double ground) {
        for(Plot p:PLOTS.getOrDefault(key(x>>6,z>>6),Collections.emptyList())) {
            BlockPos local=p.inverse(x,z);
            int side=p.annex()?6:0;
            double edge=Math.max(Math.abs(local.getX()+side)-p.w-side-6,Math.abs(local.getZ()-2)-p.d-7);
            if(edge<24)ground=GensokyoNoise.lerp(ground,p.y-1,1-GensokyoNoise.smooth(edge/24));
        }
        for(Path p:ROUTES.getOrDefault(key(x>>6,z>>6),Collections.emptyList())) {
            double t=p.t(x,z),distance=Math.hypot(x-p.x-t*(p.bx-p.x),z-p.z-t*(p.bz-p.z));
            if(distance<p.width+7)ground=GensokyoNoise.lerp(ground,p.height(t)-1,1-GensokyoNoise.smooth((distance-p.width)/7));
        }
        return ground;
    }
    static boolean nearFloor(int x,int z) {
        for(Plot p:PLOTS.getOrDefault(key(x>>6,z>>6),Collections.emptyList())) {
            BlockPos local=p.inverse(x,z);
            if(Math.abs(local.getX())<=p.w+(p.annex()?17:6) && Math.abs(local.getZ())<=p.d+8)return true;
        }
        for(Path p:ROUTES.getOrDefault(key(x>>6,z>>6),Collections.emptyList())) {
            double t=p.t(x,z);if(Math.hypot(x-p.x-t*(p.bx-p.x),z-p.z-t*(p.bz-p.z))<=p.width+3)return true;
        }
        return false;
    }
    private static <T> void index(Map<Long,List<T>> map,T value,int x1,int z1,int x2,int z2) {
        for(int x=x1>>6;x<=x2>>6;x++)for(int z=z1>>6;z<=z2>>6;z++)map.computeIfAbsent(key(x,z),k->new ArrayList<>()).add(value);
    }
    private static long key(int x,int z) {return (long)x<<32^(z&0xffffffffL);}
    static final class Plot {
        final String name;final int x,z,y,w,d,floors,form,turn;
        Plot(String name,int x,int z,int y,int w,int d,int floors,int form,int turn) {
            this.name=name;this.x=x;this.z=z;this.y=y;this.w=w;this.d=d;this.floors=floors;this.form=form;this.turn=turn;
        }
        boolean annex() {return form==3 || form==5 || form==6;}
        int rx() {return turn%2==0?w:d;}
        int rz() {return turn%2==0?d:w;}
        Rotation rotation() {return Rotation.values()[turn];}
        BlockPos local(int dx,int dy,int dz) {return new BlockPos(dx,dy,dz).rotate(rotation()).add(x,y,z);}
        BlockPos inverse(int wx,int wz) {return new BlockPos(wx-x,0,wz-z).rotate(Rotation.values()[(4-turn)%4]);}
    }
    static final class Path {
        final int x,z,y,bx,bz,by,width;
        Path(int x,int z,int y,int bx,int bz,int by,int width) {this.x=x;this.z=z;this.y=y;this.bx=bx;this.bz=bz;this.by=by;this.width=width;}
        int height(double t) {return y+(int)Math.round(t*(by-y));}
        double t(int xx,int zz) {
            double dx=bx-x,dz=bz-z,d=dx*dx+dz*dz;return d==0?0:Math.max(0,Math.min(1,((xx-x)*dx+(zz-z)*dz)/d));
        }
    }
    static final class Bridge {
        final String name;final int x1,x2,z,y,width,style;
        Bridge(String name,int x1,int x2,int z,int y,int width,int style) {this.name=name;this.x1=x1;this.x2=x2;this.z=z;this.y=y;this.width=width;this.style=style;}
        int floor(int x) {
            double t=Math.max(0,Math.min(1,(x-x1)/(double)(x2-x1)));
            return y+(style==1?(int)Math.round(4*Math.sin(Math.PI*t)):style==2?-(int)Math.round(5*Math.sin(Math.PI*t)):0);
        }
    }
}
