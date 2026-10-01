package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Two descending, physically excavated routes. Their far ends join Old Hell. */
final class UndergroundEntrances {
    static final GensokyoAtlas[] SITES={GensokyoAtlas.BLOWHOLE,GensokyoAtlas.EARTH_RAINBOW};
    private static final IBlockState ROCK=Blocks.STONE.getDefaultState();
    private UndergroundEntrances() {}
    static int floor(int route,int z) {return -Math.min(route==0?48:40,Math.max(0,(z-2)/3));}
    static int centre(int route,int z) {return z>=122?0:(int)Math.round((route==0?5:10)*Math.sin((z+22)*Math.PI/144));}
    static int end(int route) {return route==0?154:140;}
    static BlockPos gate(int route) {GensokyoAtlas s=SITES[route];return new BlockPos(s.x,s.y+floor(route,end(route))+1,s.z+end(route));}
    static BlockPos returnPoint(int route) {GensokyoAtlas s=SITES[route];int z=end(route)-7;return new BlockPos(s.x,s.y+floor(route,z)+1,s.z+z);}
    static void build(GensokyoBlueprint plan) {
        for(int route=0;route<2;route++) {
            GensokyoArchitecture a=new GensokyoArchitecture(plan,SITES[route]);
            for(boolean carve:new boolean[]{false,true})for(int z=-22;z<=end(route)+4;z++) {
                int f=floor(route,z),cx=centre(route,z);
                int radius=route==0?6+(int)Math.round(2*Math.pow(Math.sin(z*.055),2)):12+(int)Math.round(6*Math.pow(Math.sin(z*.033),2));
                for(int x=-radius-3;x<=radius+3;x++) {
                    double d=Math.abs(x)/(double)(radius+3);
                    int roof=(route==0?5:9)+(int)Math.round((route==0?5:12)*Math.sqrt(Math.max(0,1-d*d)));
                    if(!carve)a.box(cx+x,f-4,z,cx+x,f+roof+3,z,ROCK);
                    else if(Math.abs(x)<=radius && z<=end(route)+1) {
                        a.box(cx+x,f+1,z,cx+x,f+roof-1,z,AIR);
                        a.block(cx+x,f,z,Math.abs(x)<3?Blocks.GRAVEL.getDefaultState():ROCK);
                    }
                }
            }
            // Ground-level apron opens the cave mouth without a hidden step in the terrain.
            a.box(-5,-3,-34,5,0,-23,ROCK);a.box(-5,1,-34,5,12,-23,AIR);
            for(int z=-22;z<=end(route)+1;z++) {
                int f=floor(route,z),cx=centre(route,z);
                if(floor(route,z-1)>f)a.box(cx-2,f+1,z,cx+2,f+1,z,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
                if(Math.floorMod(z,18)==0) {
                    // A small trail lamp stays beside the walking lane, with an actual bracket.
                    a.box(cx+4,f+1,z,cx+4,f+4,z,Blocks.COBBLESTONE_WALL.getDefaultState());
                    a.block(cx+3,f+4,z,WOOD);a.block(cx+3,f+3,z,ModBlocks.RED_LANTERN.getDefaultState());
                    a.room("洞道"+z,cx,f,z+1);
                }
                if(Math.floorMod(z,11)==0) {
                    int side=route==0?5:11;
                    a.box(cx-side,f+5,z,cx-side,f+(route==0?8:13),z,ROCK);
                    if(route==1)for(int dx=-2;dx<=2;dx++)a.block(cx-side+dx,f+9+Math.abs(dx),z,Blocks.WEB.getDefaultState());
                }
            }
            a.room("地上洞口",0,0,-28);
            a.room("地底接续",0,floor(route,end(route)),end(route)-5);
            if(route==1)for(int z:new int[]{44,92}) {
                int x=centre(route,z)+8,f=floor(route,z+3);
                a.box(x-2,f-3,z-3,x+2,f,z+3,Blocks.MOSSY_COBBLESTONE.getDefaultState());
                a.box(x-1,f,z-2,x+1,f+3,z+2,AIR);
                a.box(x-1,f-2,z-2,x+1,f-1,z+2,Blocks.WATER.getDefaultState());
            }
            a.box(5,1,-22,5,4,-22,ROCK);
            a.sign(5,3,-23,EnumFacing.NORTH,SITES[route].title,"旧地狱");
        }
    }
}
