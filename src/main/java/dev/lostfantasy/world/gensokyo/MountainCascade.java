package dev.lostfantasy.world.gensokyo;

import net.minecraft.init.Blocks;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** One narrow cataract framed by rock fins above a closed circular plunge pool. */
public final class MountainCascade {
    public static final int UPPER_WATER=173,POOL_WATER=108;
    static final int[][] VIEW_APPROACH={{GensokyoAtlas.WATERFALL.x-264,GensokyoAtlas.WATERFALL.z+104,90},
            {GensokyoAtlas.WATERFALL.x-180,GensokyoAtlas.WATERFALL.z+20,151}};
    private MountainCascade() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.WATERFALL);
        a.box(-177,-65,17,-171,13,23,STONE);
        a.box(-177,14,17,-69,14,23,WOOD);a.box(-178,15,16,-68,52,24,AIR);
        a.box(-92,14,11,-62,14,30,WOOD);a.box(-93,15,10,-61,52,31,AIR);
        for(int x:new int[]{-163,-145,-127,-109,-91,-64})for(int z:new int[]{17,23})a.box(x,-65,z,x,13,z,LOG);
        for(int z:new int[]{17,23})a.box(-175,15,z,-94,15,z,Blocks.SPRUCE_FENCE.getDefaultState());
        for(int z:new int[]{11,30})a.box(-92,15,z,-62,15,z,Blocks.SPRUCE_FENCE.getDefaultState());
        a.box(-62,15,12,-62,15,29,Blocks.SPRUCE_FENCE.getDefaultState());
        a.box(-92,15,12,-92,15,16,Blocks.SPRUCE_FENCE.getDefaultState());a.box(-92,15,24,-92,15,29,Blocks.SPRUCE_FENCE.getDefaultState());
        a.box(-87,15,27,-81,15,28,WOOD);a.lamp(-87,15,12);
        a.room("西侧观瀑台",-76,14,20);
    }
    static Sample at(int wx,int wz,GensokyoNoise noise) {
        int x=wx-GensokyoAtlas.WATERFALL.x,z=wz-GensokyoAtlas.WATERFALL.z;
        if(x < -270 || x > 440 || z < -495 || z > 205)return null;
        double centre=centreX(z),distance=Math.abs(x-centre);
        double radius=Math.hypot(x+8,z-20);
        double channel=GensokyoNoise.lerp(22,14,GensokyoNoise.smooth((z+80)/32.0));
        double shore=z<20?Math.min(radius-52,distance-channel):radius-52;
        if(shore>170)return null;
        boolean water=radius<52 || z<20 && distance<channel;
        double station=z-lip(x);
        boolean curtain=station>=0 && station<4;
        int level=station<4?UPPER_WATER:POOL_WATER;
        double headwater=1-GensokyoNoise.smooth((z+230)/65.0);
        double endFade=GensokyoNoise.smooth((z+495)/44.0)*(1-GensokyoNoise.smooth((z-118)/87.0));
        double blend=(1-GensokyoNoise.smooth((shore-GensokyoNoise.lerp(110,38,headwater))/GensokyoNoise.lerp(60,52,headwater)))*endFade;
        double rough=noise.value(wx,wz,28,192)*3;
        double pool=radius/52;
        double bed=level-(station<0?4:3+7*Math.max(0,1-pool*pool));
        if(curtain)bed=101;
        // The complete circular rim stays at or above the pool surface, including its south side.
        double bank=181-34*GensokyoNoise.smooth((z-25)/270.0)+rough;
        bank+=headwater*(8+9*Math.sin(z/51.0))*YoukaiMountain.needle(Math.abs(distance-50)/36);
        double fin=Math.max(fin(x,z,-90,-78,23,43,61),fin(x,z,73,-50,21,42,60));
        fin=Math.max(fin,Math.max(fin(x,z,-122,-101,17,29,40),fin(x,z,111,-30,17,32,47)));
        fin=Math.max(fin,Math.max(fin(x,z,-96,102,19,36,38),fin(x,z,136,129,23,44,46)));
        fin=Math.max(fin,Math.max(fin(x,z,-126,74,14,26,28),fin(x,z,159,163,15,28,29)));
        bank+=fin;
        double edge=Math.max(0,shore);
        // Short planted ledges interrupt the sheer face; vertical grooves break its outline.
        double fluting=2.8*Math.sin(z/7.0+x/33.0)+1.2*Math.sin(z/2.7);
        double wall=GensokyoNoise.smooth((edge+fluting-2)/10)*.52
                +GensokyoNoise.smooth((edge+fluting-20)/8)*.32
                +GensokyoNoise.smooth((edge-40)/28)*.16;
        double ground=water?bed:level+(Math.max(level,bank)-level)*wall;
        boolean rock=edge>1 && (wall<.94 || fin>10);
        return new Sample(ground,blend,water?level:-1,curtain && water,rock && endFade>.8);
    }
    static double centreX(int z) {
        if(z<-80)return (-80-z)*.76;
        if(z<-42)return -8*GensokyoNoise.smooth((z+80)/38.0);
        return -8;
    }
    static double approach(int x,int z,double ground) {
        int[] a=VIEW_APPROACH[0],b=VIEW_APPROACH[1];
        if(x<a[0]-46 || x>b[0]+46 || z<b[1]-46 || z>a[1]+46)return ground;
        double dx=b[0]-a[0],dz=b[1]-a[1];
        double t=Math.max(0,Math.min(1,((x-a[0])*dx+(z-a[1])*dz)/(dx*dx+dz*dz)));
        double distance=Math.hypot(x-a[0]-t*dx,z-a[1]-t*dz);
        // A planted hillside carries the viewing-platform approach into the west bank.
        return GensokyoNoise.lerp(ground,GensokyoNoise.lerp(a[2],b[2],t),1-GensokyoNoise.smooth((distance-8)/38));
    }
    private static double fin(double x,double z,double cx,double cz,double rx,double rz,double rise) {
        double dx=(x-cx+.22*(z-cz)+2*Math.sin(z/8.0))/rx,dz=(z-cz)/rz;
        double d=.35*Math.hypot(dx,dz)+.65*Math.max(Math.abs(dx),Math.abs(dz));
        double shape=YoukaiMountain.needle(d);
        double cleft=8*Math.pow(Math.max(0,Math.sin((x+.32*z)/4.8)),10);
        return Math.max(0,(rise-cleft)*shape);
    }
    public static int lip(int x) {return -35-(int)Math.round(7*Math.sin(x/29.0)+3*Math.sin(x/9.0));}
    static final class Sample {
        final double ground,blend;final int water;final boolean falling,rock;
        Sample(double ground,double blend,int water,boolean falling,boolean rock) {
            this.ground=ground;this.blend=blend;this.water=water;this.falling=falling;this.rock=rock;
        }
    }
}
