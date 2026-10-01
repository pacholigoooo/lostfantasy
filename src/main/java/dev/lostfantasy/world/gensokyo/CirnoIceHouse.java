package dev.lostfantasy.world.gensokyo;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.ChunkPrimer;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A small round house on a permanent ice floe; furnishings are an explorable adaptation. */
final class CirnoIceHouse {
    static final GensokyoAtlas SITE=GensokyoAtlas.CIRNO;
    private static final IBlockState ICE=Blocks.PACKED_ICE.getDefaultState();
    private CirnoIceHouse() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,SITE);
        a.box(-21,1,-17,20,22,26,AIR);
        // A compact dome and a short, full-height entrance fit the human player collision box.
        for(int x=-9;x<=9;x++)for(int z=-11;z<=7;z++) {
            double radial=x*x+(z+2)*(z+2);
            if(radial<=81) {
                a.box(x,-1,z,x,0,z,ICE);
                if(radial>64)a.block(x,1,z,Blocks.SNOW_LAYER.getDefaultState());
            }
            for(int y=1;y<=9;y++) {
                double outer=radial/64+(y-1)*(y-1)/81.0;
                if(outer>1)continue;
                double inner=radial/42+(y-1)*(y-1)/49.0;
                a.block(x,y,z,inner<1?AIR:ICE);
            }
        }
        // Thin translucent ice admits daylight; the structural packed ice does not melt.
        a.box(-1,7,-3,1,9,-1,Blocks.ICE.getDefaultState());
        for(int x:new int[]{-7,-6,6,7})for(int z=-3;z<=-1;z++)for(int y=3;y<=4;y++)
            if(plan.at(SITE.x+x,SITE.y+y,SITE.z+z).getBlock()==Blocks.PACKED_ICE)
                a.block(x,y,z,Blocks.ICE.getDefaultState());
        for(int x=-3;x<=3;x++) {
            int top=Math.abs(x)<=1?5:Math.abs(x)==2?4:2;
            a.box(x,-1,5,x,top,13,ICE);
        }
        a.box(-1,1,3,1,3,13,AIR);
        a.box(-1,0,3,1,0,13,Blocks.WOOL.getStateFromMeta(3));
        a.box(-2,-1,14,2,0,30,ICE);a.box(-2,1,14,2,3,30,AIR);
        // The floating floor is at lake level; two steps reach the raised shore boardwalk.
        for(int z=30;z<=31;z++) {
            int floor=z-29;
            a.box(-2,0,z,2,floor-1,z,ICE);
            a.box(-2,floor,z,2,floor,z,Blocks.QUARTZ_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
            a.box(-2,floor+1,z,2,floor+3,z,AIR);
        }
        a.box(-2,2,32,2,2,35,WOOD);a.box(-2,3,32,2,5,35,AIR);
        a.box(-1,0,-1,1,0,2,Blocks.WOOL.getStateFromMeta(3));
        a.box(-4,0,-6,-3,0,-3,Blocks.WOOL.getStateFromMeta(0));a.bed(-4,0,-4);
        a.box(-1,1,-4,1,1,-3,Blocks.STONE_SLAB.getStateFromMeta(15));
        for(int x:new int[]{-2,2})a.block(x,1,-4,Blocks.QUARTZ_STAIRS.getDefaultState()
                .withProperty(BlockStairs.FACING,x<0?EnumFacing.WEST:EnumFacing.EAST));
        a.chest(4,0,-4,"fairy_keepsakes");a.chest(4,0,0,"fairy_keepsakes");
        a.block(4,1,2,Blocks.CRAFTING_TABLE.getDefaultState());
        // Low ordinary block lights sit beneath carpet, safely away from the ice windows.
        for(int[] p:new int[][]{{-3,-2},{3,-2},{-3,2},{3,2}}) {
            a.block(p[0],0,p[1],Blocks.SEA_LANTERN.getDefaultState());
            a.block(p[0],1,p[1],Blocks.CARPET.getStateFromMeta(3));
        }
        // A clear strip crosses the ice to a low lakeside seat.
        a.box(-18,0,16,-2,0,18,ICE);
        a.box(-19,0,4,-17,0,18,ICE);
        a.box(-20,-3,1,-16,0,6,ICE);a.box(-20,1,1,-16,3,6,AIR);
        a.box(-16,1,1,-16,1,3,Blocks.STONE_SLAB.getStateFromMeta(7));
        for(int[] p:new int[][]{{-13,-10},{12,-6},{12,8},{-12,7}}) {
            a.box(p[0],0,p[1],p[0]+1,1,p[1]+1,ICE);
            a.block(p[0],2,p[1],Blocks.SNOW_LAYER.getDefaultState());
        }
        a.room("冰屋门前",0,0,18);a.room("圆厅",0,0,1);
        a.room("矮桌旁",0,0,-2);a.room("寝位旁",-3,0,-5);
        a.room("小物收存",3,0,0);a.room("湖畔冰台",-18,0,4);
    }
    static void paintIce(ChunkPrimer p,int cx,int cz,GensokyoTerrain terrain) {
        int ox=cx<<4,oz=cz<<4;
        if(ox<SITE.x-64 || ox>SITE.x+48 || oz<SITE.z-64 || oz>SITE.z+48)return;
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int wx=ox+x,wz=oz+z,dx=wx-SITE.x,dz=wz-SITE.z;
            double radius=Math.hypot(dx/39.0,(dz+2)/34.0);
            double edge=1+terrain.patchNoise(wx,wz,11,321)*.11+terrain.patchNoise(wx,wz,5,322)*.04;
            if(radius>edge)continue;
            GensokyoTerrain.Column c=terrain.column(wx,wz);if(c.water!=72 || c.ground>=71)continue;
            boolean glassy=radius>.64 && (terrain.patchNoise(wx,wz,8,323)>.18 || radius>edge-.06);
            p.setBlockState(x,72,z,glassy?Blocks.ICE.getDefaultState():ICE);
            if(radius<edge-.10)p.setBlockState(x,71,z,ICE);
            if(radius>.55 && !glassy && Math.floorMod(terrain.hash(wx,wz,324),100)<12 && !c.path())
                p.setBlockState(x,73,z,Blocks.SNOW_LAYER.getDefaultState());
        }
    }
}
