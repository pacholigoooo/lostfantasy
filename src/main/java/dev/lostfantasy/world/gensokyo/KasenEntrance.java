package dev.lostfantasy.world.gensokyo;

import net.minecraft.init.Blocks;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** The mountain contains a zelkova grove and the approach, not a second copy of the house. */
final class KasenEntrance {
    private KasenEntrance() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.KASEN);
        a.box(-37,-2,-35,35,0,67,Blocks.GRASS.getDefaultState());
        for(int[] p:new int[][]{{-33,49},{30,40},{-35,16},{32,2},{18,-28},{-25,-27},{-8,41}})KasenGarden.tree(a,p[0],p[1],13,7);
        // Broad branching tree, with roots confined to its own bank beside the route.
        zelkova(a);
        for(int n=1;n<=6;n++) {a.box(5-n,1,13,5-n,Math.max(1,4-n/2),14,Blocks.LOG.getDefaultState());a.box(5,1,13+n,6,Math.max(1,4-n/2),13+n,Blocks.LOG.getDefaultState());}
        for(int[] b:new int[][]{{-10,50,3},{11,42,4},{-6,20,3},{-26,2,4},{-16,-20,3}})
            for(int x=-b[2];x<=b[2];x++)for(int z=-b[2];z<=b[2];z++)for(int y=1;y<=b[2];y++)
                if(x*x+z*z+y*y<b[2]*b[2]+3)a.block(b[0]+x,y,b[1]+z,Math.floorMod(x+z+y,4)==0?Blocks.MOSSY_COBBLESTONE.getDefaultState():Blocks.STONE.getDefaultState());
        for(int x=-32;x<=31;x++)for(int z=-30;z<=67;z++) {
            boolean path=z>=62 && Math.abs(x)<=2;
            for(int i=1;i<HermitPath.POINTS.length;i++)path|=HermitPath.distance(x,z,HermitPath.POINTS[i-1],HermitPath.POINTS[i])<=1.7;
            if(path) {a.block(x,0,z,Math.floorMod(x+z,5)==0?STONE:Blocks.GRAVEL.getDefaultState());a.box(x,1,z,x,4,z,AIR);}
        }
        for(int[] p:HermitPath.POINTS)a.room("榉树小径",p[0],0,p[1]);
        a.box(8,1,57,12,1,57,STONE);a.chest(11,1,57,"hermit_path");
    }
    private static void zelkova(GensokyoArchitecture a) {
        for(int[] c:new int[][]{{5,25,13,9},{-2,22,11,8},{11,22,17,8},{7,24,6,7}})
            for(int x=-c[3];x<=c[3];x++)for(int z=-c[3];z<=c[3];z++)for(int y=-4;y<=4;y++)
                if(x*x+z*z+y*y*3<c[3]*c[3] && Math.floorMod(x*11+z*7+y,29)!=0)a.block(c[0]+x,c[1]+y,c[2]+z,Blocks.LEAVES.getStateFromMeta(4));
        a.box(4,1,12,6,19,14,Blocks.LOG.getDefaultState());
        for(int[] d:new int[][]{{-1,0},{1,1},{0,-1}})for(int n=1;n<=7;n++) {
            int x=5+d[0]*(n-1),z=13+d[1]*(n-1);
            a.box(Math.min(x,x+d[0]),15+n-1,Math.min(z,z+d[1]),Math.max(x,x+d[0]),16+n,Math.max(z,z+d[1]),Blocks.LOG.getStateFromMeta(12));
        }
    }
}
