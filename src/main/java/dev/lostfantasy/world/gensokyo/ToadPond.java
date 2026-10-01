package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Shaded lotus water and a small, unnamed shore shrine from Perfect Memento. */
final class ToadPond {
    private static final GensokyoAtlas SITE=GensokyoAtlas.TOAD_POND;
    private ToadPond() {}
    static boolean grove(int x,int z) {return Math.abs((long)x-SITE.x)<146 && Math.abs((long)z-SITE.z)<113;}
    static boolean shrine(int x,int z,int margin) {
        return x>=SITE.x+65-margin && x<=SITE.x+100+margin && z>=SITE.z-16-margin && z<=SITE.z+28+margin;
    }
    static double bank(int x,int z,double height,GensokyoNoise noise) {
        if(!grove(x,z))return height;
        double dx=x-SITE.x,dz=z-SITE.z,d=Math.hypot(dx/60,dz/44);
        if(d>=1 && d<2.4) {
            double target=SITE.y+2+(d-1)*3+noise.value(x,z,25,213)*1.2;
            height=GensokyoNoise.lerp(target,height,GensokyoNoise.smooth((d-1.3)/1.1));
        }
        double pad=Math.max(Math.max(65-dx,dx-100),Math.max(-16-dz,dz-28));
        if(pad<18)height=GensokyoNoise.lerp(height,SITE.y+4,1-GensokyoNoise.smooth(pad/18));
        return height;
    }
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,SITE,81,4,SITE.title);
        a.box(-15,5,-19,19,18,24,AIR);
        a.box(-15,4,-19,19,4,24,Blocks.GRASS.getDefaultState());
        a.box(-3,4,-10,3,4,24,Blocks.GRAVEL.getDefaultState());
        a.box(0,4,19,23,4,23,Blocks.GRAVEL.getDefaultState());
        a.box(-7,4,-11,7,5,1,Blocks.MOSSY_COBBLESTONE.getDefaultState());
        a.box(-5,6,-9,5,11,-1,WOOD);a.box(-4,7,-8,4,10,-2,AIR);
        for(int x:new int[]{-5,5})for(int z:new int[]{-9,-1})a.box(x,6,z,x,11,z,LOG);
        a.openZ(0,-1,6,1,4);a.box(-5,6,0,5,6,2,WOOD);a.stairsSouth(0,3,4,6,2);
        a.gable(-8,-12,8,2,12);
        a.box(-3,7,-7,3,7,-7,DARK);a.block(0,8,-7,ModBlocks.MEDICINE_TRAY.getDefaultState());
        a.block(-3,10,-5,ModBlocks.RED_LANTERN.getDefaultState());a.block(3,10,-5,ModBlocks.RED_LANTERN.getDefaultState());
        a.box(-2,7,-9,2,9,-9,RED);
        a.box(0,7,-9,0,9,-9,WHITE);
        a.room("小祠供台",0,6,-4);a.room("祠前小径",0,4,13);
        a.lamp(-6,5,6);a.lamp(6,5,6);
        // A low landing at the west side looks back over the lotus patches.
        a.box(-21,0,10,-8,4,14,Blocks.MOSSY_COBBLESTONE.getDefaultState());
        a.box(-22,0,8,-19,4,17,Blocks.MOSSY_COBBLESTONE.getDefaultState());
        a.box(-23,5,8,-23,5,17,Blocks.COBBLESTONE_WALL.getDefaultState());
        a.box(-17,5,9,-11,5,9,Blocks.WOODEN_SLAB.getStateFromMeta(1));
        a.room("树荫观池处",-17,4,12);
    }
    static void paint(ChunkPrimer p,int cx,int cz,GensokyoTerrain terrain) {
        int ox=cx<<4,oz=cz<<4;
        if(ox>SITE.x+156 || ox+15<SITE.x-156 || oz>SITE.z+123 || oz+15<SITE.z-123)return;
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int wx=ox+x,wz=oz+z;
            if(Math.hypot((wx-SITE.x)/60.0,(wz-SITE.z)/44.0)>.98)continue;
            GensokyoTerrain.Column c=terrain.column(wx,wz);
            if(!c.wet() || c.path() || c.water!=SITE.y)continue;
            long h=terrain.hash(wx,wz,215);
            if(terrain.patchNoise(wx,wz,17,214)>-.08 && Math.floorMod(h,8)==0)
                p.setBlockState(x,c.water+1,z,Math.floorMod(h>>>9,6)==0?ModBlocks.POND_LOTUS.getDefaultState():Blocks.WATERLILY.getDefaultState());
        }
        for(int gx=Math.floorDiv(ox-9,14);gx<=Math.floorDiv(ox+24,14);gx++)for(int gz=Math.floorDiv(oz-9,14);gz<=Math.floorDiv(oz+24,14);gz++) {
            long h=terrain.hash(gx,gz,216);int x=gx*14+(int)Math.floorMod(h,7),z=gz*14+(int)Math.floorMod(h>>>8,7);
            double d=Math.hypot((x-SITE.x)/60.0,(z-SITE.z)/44.0);
            if(d<1.08 || d>2 || shrine(x,z,9) || Math.floorMod(h>>>15,5)==0)continue;
            GensokyoTerrain.Column c=terrain.column(x,z);
            if(c.wet() || c.road!=null && c.road.distance<c.road.width+9)continue;
            int height=12+(int)Math.floorMod(h>>>20,7);
            for(int y=1;y<=height;y++)put(p,ox,oz,x,c.ground+y,z,LOG,false);
            for(int dy=-3;dy<=3;dy++)for(int dx=-8;dx<=8;dx++)for(int dz=-8;dz<=8;dz++)
                if(dx*dx+dz*dz+dy*dy*3<64 && Math.floorMod(dx*13+dz*7+dy,17)!=0)
                    put(p,ox,oz,x+dx,c.ground+height+dy,z+dz,Blocks.LEAVES.getStateFromMeta(4),true);
        }
    }
    private static void put(ChunkPrimer p,int ox,int oz,int x,int y,int z,IBlockState state,boolean airOnly) {
        x-=ox;z-=oz;
        if(x>=0 && x<16 && z>=0 && z<16 && y>0 && y<255 && (!airOnly || p.getBlockState(x,y,z).getBlock()==Blocks.AIR))p.setBlockState(x,y,z,state);
    }
}
