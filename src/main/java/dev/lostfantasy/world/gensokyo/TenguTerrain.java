package dev.lostfantasy.world.gensokyo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.block.BlockVine;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;

/** An asymmetric inhabited ravine: individual rock shoulders replace continuous contour terraces. */
final class TenguTerrain {
    private static final int[] LIPS={-166,-45,126,245};
    private static final int[][] SPURS={{-382,82,60,86,50},{-230,-40,41,53,27},
            {-68,-239,32,49,21},{82,-292,35,61,23},{365,-99,57,93,36},
            {255,81,43,54,22},{-326,-287,50,66,18},{215,286,37,50,19}};
    private TenguTerrain() {}
    static Sample at(int wx,int wz,GensokyoNoise noise) {
        int x=wx-GensokyoAtlas.TENGU.x,z=wz-GensokyoAtlas.TENGU.z;
        if(Math.abs(x)>510 || z < -426 || z>438)return null;
        double shoulder=395+18*Math.sin(z/97.0);
        double blend=(1-GensokyoNoise.smooth((Math.abs(x)-shoulder)/90.0))
                *GensokyoNoise.smooth((z+408+12*Math.sin(x/82.0))/75.0)
                *(1-GensokyoNoise.smooth((z-392)/46.0));
        double fold=z+38*Math.sin(x/92.0)+noise.value(wx,wz,70,981)*31;
        double mass=116+107*GensokyoNoise.smooth((325-fold)/625);
        mass+=noise.value(wx,wz,47,982)*12+noise.value(wx,wz,12,983)*2;
        for(int[] spur:SPURS) {
            double d=Math.hypot((x-spur[0])/(double)spur[2],(z-spur[1])/(double)spur[3]);
            if(d<1)mass+=spur[4]*YoukaiMountain.needle(d);
        }
        // A tilted rock step meanders through each side; neither extends across the whole village.
        double west=z+105+48*Math.sin((x+180)/61.0),east=z-36+54*Math.sin((x-170)/84.0);
        if(x<-80)mass+=14*(1-GensokyoNoise.smooth(west/10))*(1-GensokyoNoise.smooth((-x-365)/65.0));
        if(x>105)mass+=12*(1-GensokyoNoise.smooth(east/9))*(1-GensokyoNoise.smooth((x-360)/65.0));
        double centre=riverX(z),width=riverWidth(z),distance=Math.abs(x-centre);
        boolean wet=width>0 && distance<width;
        int along=z+(int)Math.round(4*Math.sin((x-centre)/13)+2*Math.sin(x/19.0));
        int water=level(along),bed=water-6;
        boolean falling=false;
        for(int lip:LIPS)if(along>=lip && along<=lip+2) {water=level(lip-1);falling=true;}
        if(width>0 && distance<width+26) {
            double bank=GensokyoNoise.smooth((distance-width)/26);
            mass=wet?bed:GensokyoNoise.lerp(water+1,Math.max(water+1,mass),bank);
        }
        if(!wet)mass=TenguLayout.grade(x,z,mass);
        boolean rock=!TenguLayout.nearFloor(x,z) && (noise.value(wx,wz,36,984)>.08 || width>0 && distance<width+26);
        return new Sample(Math.min(242,mass),blend,wet?water:-1,wet&&falling,rock);
    }
    static double riverX(int z) {
        double x=20+52*Math.sin((z+50)/145.0);
        return GensokyoNoise.lerp(x,42,GensokyoNoise.smooth((z-285)/45.0));
    }
    static double riverWidth(int z) {
        if(z < -307 || z>391)return -1;
        if(z < -275)return 18*Math.sqrt(Math.max(0,1-Math.pow((z+275)/32.0,2)));
        double width=12+4*Math.pow(Math.sin(z/19.0),2)+18*Math.exp(-Math.pow((z-270)/50.0,2));
        if(z>285) {
            double pool=50*Math.sqrt(Math.max(0,1-Math.pow((z-333)/58.0,2)));
            width=GensokyoNoise.lerp(width,pool,GensokyoNoise.smooth((z-285)/30.0));
        }
        return width;
    }
    private static int level(int z) {return z < -166?217:z < -45?183:z<126?145:z<245?112:105;}
    static void paint(ChunkPrimer p,int cx,int cz,GensokyoTerrain terrain) {
        int ox=(cx<<4)-GensokyoAtlas.TENGU.x,oz=(cz<<4)-GensokyoAtlas.TENGU.z;
        if(ox < -526 || ox>510 || oz < -424 || oz>438)return;
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int wx=(cx<<4)+x,wz=(cz<<4)+z,lx=ox+x,lz=oz+z;
            GensokyoTerrain.Column c=terrain.column(wx,wz);
            if(!c.rockSurface || Math.abs(lx)>390 || lz < -345 || lz>365 || TenguLayout.nearFloor(lx,lz))continue;
            double seam=terrain.patchNoise(wx,wz,17,989),moss=terrain.patchNoise(wx,wz,39,990);
            for(int y=Math.max(80,c.ground-62);y<=c.ground;y++) {
                IBlockState stone=seam<-.12?Blocks.STONE.getStateFromMeta(5):seam>.32 && y%19<4?Blocks.COBBLESTONE.getDefaultState():Blocks.STONE.getDefaultState();
                if(y>c.ground-4 && moss>.12 && c.ground<220)stone=Blocks.MOSSY_COBBLESTONE.getDefaultState();
                p.setBlockState(x,y,z,stone);
            }
            if(!c.wet() && Math.floorMod(terrain.hash(wx,wz/4,989),29)<3) {
                int upper=terrain.column(wx,wz-1).ground;
                for(int y=c.ground+2;y<=Math.min(upper-1,c.ground+14);y++)
                    p.setBlockState(x,y,z,Blocks.VINE.getDefaultState().withProperty(BlockVine.NORTH,true));
            }
        }
        woodland(p,cx,cz,terrain);
    }
    private static void woodland(ChunkPrimer p,int cx,int cz,GensokyoTerrain terrain) {
        int ox=cx<<4,oz=cz<<4;
        for(int gx=Math.floorDiv(ox-9,18);gx<=Math.floorDiv(ox+24,18);gx++)
            for(int gz=Math.floorDiv(oz-9,18);gz<=Math.floorDiv(oz+24,18);gz++) {
                long hash=terrain.hash(gx,gz,991);if(Math.floorMod(hash,100)>55)continue;
                int x=gx*18+(int)Math.floorMod(hash>>>8,9),z=gz*18+(int)Math.floorMod(hash>>>16,9);
                int lx=x-GensokyoAtlas.TENGU.x,lz=z-GensokyoAtlas.TENGU.z;
                if(Math.abs(lx)>365 || lz < -315 || lz>357)continue;
                boolean clear=true;
                for(int[] side:new int[][]{{0,0},{-9,0},{9,0},{0,-9},{0,9}})
                    if(TenguLayout.nearFloor(lx+side[0],lz+side[1]))clear=false;
                GensokyoTerrain.Column c=terrain.column(x,z);
                if(!clear || c.wet() || c.ground>235 || c.path())continue;
                if(Math.abs(terrain.column(x-3,z).ground-c.ground)>4 || Math.abs(terrain.column(x+3,z).ground-c.ground)>4)continue;
                int height=8+(int)Math.floorMod(hash>>>24,7),lean=(hash&1)==0?-1:1;
                IBlockState leaves=Math.floorMod(hash>>>28,5)==0?dev.lostfantasy.ModBlocks.CHERRY_LEAVES.getDefaultState():Blocks.LEAVES.getStateFromMeta(4);
                for(int y=1;y<=height;y++)put(p,ox,oz,x,c.ground+y,z,Blocks.LOG.getDefaultState());
                for(int n=1;n<=4;n++)put(p,ox,oz,x+n*lean,c.ground+height-3+n/2,z+n/2,Blocks.LOG.getStateFromMeta(12));
                for(int crown=0;crown<2;crown++)for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++)for(int dy=-2;dy<=2;dy++)
                    if(dx*dx+dz*dz+dy*dy*3<=18)put(p,ox,oz,x+dx+crown*lean*4,c.ground+height+dy-crown,z+dz+crown*2,leaves);
            }
    }
    private static void put(ChunkPrimer p,int ox,int oz,int x,int y,int z,IBlockState state) {
        x-=ox;z-=oz;if(x>=0 && x<16 && z>=0 && z<16 && y>0 && y<255 && p.getBlockState(x,y,z).getBlock()==Blocks.AIR)p.setBlockState(x,y,z,state);
    }
    static final class Sample {
        final double ground,blend;final int water;final boolean falling,rock;
        Sample(double ground,double blend,int water,boolean falling,boolean rock) {this.ground=ground;this.blend=blend;this.water=water;this.falling=falling;this.rock=rock;}
    }
}
