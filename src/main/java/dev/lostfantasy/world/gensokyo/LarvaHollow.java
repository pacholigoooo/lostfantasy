package dev.lostfantasy.world.gensokyo;

import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A small forest opening leads into Larva's underground summer, from Three Fairies V, chapter 9. */
final class LarvaHollow {
    private static final IBlockState EARTH=Blocks.DIRT.getDefaultState(),GRASS=Blocks.GRASS.getDefaultState(),
            ROCK=Blocks.STONE.getDefaultState(),ROOT=Blocks.LOG.getStateFromMeta(12),LEAVES=Blocks.LEAVES.getStateFromMeta(4);
    private LarvaHollow() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.LARVA);
        // Keep a continuous earth roof. The interior is a sheltered meadow, not a mansion below ground.
        a.box(-18,-18,-21,18,-1,19,ROCK);
        a.box(-18,-3,-21,18,-1,19,EARTH);
        for(int x=-16;x<=16;x++)for(int z=-19;z<=13;z++)if(inside(x,z)) {
            int floor=floor(x,z),ceiling=ceiling(x,z);
            a.box(x,floor-2,z,x,floor,z,EARTH);a.block(x,floor,z,GRASS);
            a.box(x,floor+1,z,x,ceiling,z,AIR);
        }
        mouth(a);summer(a);roots(a);
        // The last cut protects the path from planting and roots and links the landing to the meadow.
        for(int z=10;z<=26;z++) {
            int y=Math.max(-14,Math.min(0,z-24));
            a.box(-1,y-2,z,1,y,z,EARTH);a.box(-1,y+1,z,1,y+4,z,AIR);
            a.box(-1,y,z,1,y,z,Blocks.GRAVEL.getDefaultState());
        }
        for(int z=-14;z<=9;z++)clearPath(a,0,z);
        for(int x=-12;x<=12;x++)clearPath(a,x,0);
        a.room("林间洞口",0,0,25);a.room("坡道下沿",0,-14,10);
        a.room("盛夏花地",0,floor(0,0),0);a.room("北侧根壁",0,floor(0,-14),-14);
        a.room("西侧花丛",-12,floor(-12,0),0);a.room("东侧花丛",12,floor(12,0),0);
        a.room("叶荫歇脚处",6,floor(6,-3),-3);
    }
    private static boolean inside(int x,int z) {return x*x/256.0+(z+3)*(z+3)/256.0<1;}
    private static double distance(int x,int z) {return x*x/256.0+(z+3)*(z+3)/256.0;}
    private static int floor(int x,int z) {return -14+(int)(distance(x,z)*3);}
    private static int ceiling(int x,int z) {return -3-(int)(distance(x,z)*3);}
    private static void mouth(GensokyoArchitecture a) {
        // A low root-covered bank hides the descending opening from the surrounding path.
        for(int x=-8;x<=8;x++)for(int z=12;z<=24;z++) {
            double d=x*x/64.0+(z-17)*(z-17)/64.0;if(d>=1)continue;
            int top=(int)(4*(1-d));a.box(x,0,z,x,top,z,EARTH);a.block(x,top,z,GRASS);
        }
        ForestDetails.tree(a,-13,13,21,6);ForestDetails.tree(a,14,-13,25,6);
        for(int x:new int[]{-5,5})for(int z=16;z<=21;z++)a.block(x,z<19?3:2,z,ROOT);
        a.box(-5,3,18,-3,3,18,ROOT);a.box(3,3,18,5,3,18,ROOT);
        for(int[] at:new int[][]{{-7,13},{7,16},{-6,21},{5,11}}) {
            double d=at[0]*at[0]/64.0+(at[1]-17)*(at[1]-17)/64.0;
            int y=(int)(4*(1-d));if(y>=0)a.block(at[0],y+1,at[1],Blocks.TALLGRASS.getStateFromMeta(2));
        }
    }
    private static void summer(GensokyoArchitecture a) {
        for(int x=-14;x<=14;x++)for(int z=-17;z<=10;z++)if(inside(x,z) && Math.abs(x)>2 && Math.abs(z)>2) {
            int y=floor(x,z)+1,n=Math.floorMod(x*19+z*13,17);
            if(n<3 && y+1<=ceiling(x,z)) {
                a.block(x,y,z,Blocks.DOUBLE_PLANT.getStateFromMeta(0));a.block(x,y+1,z,Blocks.DOUBLE_PLANT.getStateFromMeta(8));
            } else if(n<6)a.block(x,y,z,Blocks.RED_FLOWER.getStateFromMeta(n==3?8:n==4?3:0));
            else if(n<9)a.block(x,y,z,Blocks.TALLGRASS.getStateFromMeta(2));
        }
        // Warm ordinary block light works with the world's light map, including shader packs.
        for(int x:new int[]{-14,-10,-5,5,10,14})for(int z:new int[]{-15,-10,-5,0,5,10})if(distance(x,z)<.96) {
            int y=floor(x,z);a.block(x,y,z,LIGHT);
            a.block(x,y+1,z,Blocks.CARPET.getStateFromMeta(13));a.block(x,y+2,z,AIR);
        }
        for(int[] p:new int[][]{{-6,-9},{7,-8}}) {
            int y=floor(p[0],p[1]);a.box(p[0],y+1,p[1],p[0],y+5,p[1],ROOT);
            for(int dy=0;dy<=2;dy++)for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++) {
                int x=p[0]+dx,z=p[1]+dz,at=y+4+dy;
                if(dx*dx+dz*dz+dy*dy>11 || !inside(x,z) || at>=ceiling(x,z))continue;
                if(a.plan.at(a.site.x+x,a.site.y+at,a.site.z+z).getBlock()==Blocks.AIR)a.block(x,at,z,LEAVES);
            }
        }
        // Clear a modest patch under the leaves for resting, with no invented domestic fixtures.
        a.box(4,-13,-5,8,-11,-2,AIR);
        for(int x=4;x<=8;x++)for(int z=-5;z<=-2;z++)a.block(x,-14,z,GRASS);
        a.box(5,-13,-4,7,-13,-4,Blocks.CARPET.getStateFromMeta(5));
        a.block(4,-13,-4,ROOT);a.block(8,-13,-4,ROOT);
        // The resting patch replaced one meadow lamp; keep its warm light under the clear seating edge.
        a.block(6,-14,-3,LIGHT);a.block(6,-13,-3,Blocks.CARPET.getStateFromMeta(5));
    }
    private static void roots(GensokyoArchitecture a) {
        for(int x:new int[]{-12,12})for(int z=-11;z<=6;z++)if(inside(x,z)) {
            int top=ceiling(x,z);a.block(x,top,z,ROOT);
            if(Math.floorMod(z,3)==0)a.block(x,top-1,z,ROOT);
        }
        // Place supported vine strips only after the root wall is finished.
        for(int x:new int[]{-12,12})for(int z=-10;z<=4;z+=3)if(inside(x,z)) {
            int top=ceiling(x,z)-1,at=x+(x<0?1:-1);EnumFacing side=x<0?EnumFacing.WEST:EnumFacing.EAST;
            if(a.plan.at(a.site.x+x,a.site.y+top,a.site.z+z).isFullCube())
                a.block(at,top,z,Blocks.VINE.getDefaultState().withProperty(BlockVine.getPropertyFor(side),true));
        }
    }
    private static void clearPath(GensokyoArchitecture a,int x,int z) {
        int y=floor(x,z);a.block(x,y,z,GRASS);a.box(x,y+1,z,x,y+3,z,AIR);
    }
}
