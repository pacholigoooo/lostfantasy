package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Surface research centre, public baths and a daylight shaft with a continuous service stair. */
final class GeyserCenter {
    static final GensokyoAtlas SITE=GensokyoAtlas.GEYSER;
    static final int BOTTOM=-93, TOP=3;
    private static final IBlockState FRAME=ModBlocks.COLUMNAR_BASALT.getDefaultState(),
            METAL=Blocks.IRON_BLOCK.getDefaultState(),RAIL=Blocks.IRON_BARS.getDefaultState(),
            GREEN=Blocks.STAINED_GLASS.getStateFromMeta(5);
    private GeyserCenter() {}
    static BlockPos local(int x,int y,int z) {return new BlockPos(SITE.x+x,SITE.y+y,SITE.z+z);}
    static BlockPos gate() {return local(0,BOTTOM+1,-35);}
    static BlockPos arrival() {return local(0,BOTTOM+1,-27);}
    static BlockPos lowerGate() {return OldHellWorld.local(0,BlazingHell.DECK+97,BlazingHell.CORE_Z-98);}
    static BlockPos lowerArrival() {return OldHellWorld.local(0,BlazingHell.DECK+97,BlazingHell.CORE_Z-88);}
    static void build(GensokyoBlueprint p) {
        GensokyoArchitecture a=new GensokyoArchitecture(p,SITE);
        a.box(-57,-4,-46,57,0,52,STONE);a.box(-57,1,-46,57,41,52,AIR);
        a.box(-56,1,-45,56,2,47,STONE);a.box(-56,3,-45,56,3,47,Blocks.STONE.getStateFromMeta(6));
        for(int x:new int[]{-57,57})a.box(x,4,-46,x,5,48,FRAME);
        for(int z:new int[]{-46,48})a.box(-57,4,z,57,5,z,FRAME);
        a.box(-7,4,48,7,8,48,AIR);a.stairsSouth(0,48,0,3,7);
        tower(a);wing(a,-55,-32,true);wing(a,32,55,false);reception(a);
        for(int side:new int[]{-1,1})a.box(side<0?-33:27,4,-3,side<0?-27:33,9,3,AIR);
        a.box(-5,4,27,5,10,33,AIR);
        for(int x:new int[]{-53,53})for(int z:new int[]{-40,42})a.lamp(x,3,z);
        a.room("入口前庭",0,0,52);a.room("地上井口",0,3,24);
    }
    private static void tower(GensokyoArchitecture a) {
        for(int x=-30;x<=30;x++)for(int z=-30;z<=30;z++) {
            int d=x*x+z*z;if(d>900)continue;
            a.box(x,BOTTOM-3,z,x,37,z,FRAME);
            if(d<28*28)a.box(x,BOTTOM+1,z,x,36,z,AIR);
            if(d<13*13) {
                // An open bore admits real sky light. Its small bottom pool is fully contained.
                a.box(x,BOTTOM+1,z,x,43,z,AIR);a.block(x,BOTTOM,z,Blocks.WATER.getDefaultState());
                a.block(x,BOTTOM-1,z,STONE);
            }else if(d<28*28) {
                a.block(x,TOP,z,METAL);
                if(d<14*14)a.block(x,TOP+1,z,RAIL);
            }
            if(d>=28*28) {
                for(int y:new int[]{3,15,29,37})a.block(x,y,z,METAL);
                if(Math.abs(x)<12 || Math.abs(z)<12)a.box(x,7,z,x,13,z,GREEN);
            }
            if(d>=13*13 && d<28*28)a.block(x,BOTTOM,z,METAL);
        }
        for(int side=0;side<4;side++) {
            int x=side==0?-18:side==1?18:side==2?18:-18,z=side<2?-18:18;
            int f=BOTTOM+side*24;
            a.box(x-3,f,z-3,x+3,f,z+3,METAL);a.box(x-3,f+1,z-3,x+3,f+5,z+3,AIR);
        }
        // Wide stair winds once around the bore: four 24-step flights, with long corner landings.
        for(int side=0;side<4;side++)for(int t=0;t<36;t++) {
            int x=side==0?-18+t:side==1?18:side==2?18-t:-18;
            int z=side==0?-18:side==1?-18+t:side==2?18:18-t;
            int f=BOTTOM+24*side+Math.min(t,24);boolean alongX=(side&1)==0;
            a.box(x-(alongX?0:3),f-2,z-(alongX?3:0),x+(alongX?0:3),f,z+(alongX?3:0),FRAME);
            a.box(x-(alongX?0:3),f+1,z-(alongX?3:0),x+(alongX?0:3),f+5,z+(alongX?3:0),AIR);
            if(t>0 && t<=24)a.box(x-(alongX?0:2),f,z-(alongX?2:0),x+(alongX?0:2),f,z+(alongX?2:0),
                    Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,new EnumFacing[]{EnumFacing.EAST,EnumFacing.SOUTH,EnumFacing.WEST,EnumFacing.NORTH}[side]));
            if(t>=4 && t<32)for(int sign:new int[]{-1,1})a.block(x+(alongX?0:sign*3),f+1,z+(alongX?sign*3:0),RAIL);
            if(t%12==0) {
                a.block(x+(alongX?0:3),f,z+(alongX?3:0),LIGHT);
                a.room("井壁检修梯"+side+"-"+t,x,f,z);
            }
        }
        // Bottom side passage ends in a walking connection to the reactor's upper gallery.
        a.box(-6,BOTTOM-2,-39,6,BOTTOM+9,-25,FRAME);
        a.box(-4,BOTTOM+1,-38,4,BOTTOM+7,-24,AIR);a.box(-4,BOTTOM,-38,4,BOTTOM,-24,METAL);
        a.room("地下井厅",0,BOTTOM,-24);a.room("炉心检修通路",0,BOTTOM,-33);
        for(int y=BOTTOM+8;y<30;y+=16)for(int x:new int[]{-26,26}) {
            a.box(x,y,-1,x,y+3,1,METAL);a.block(x<0?x+1:x-1,y+2,0,ModBlocks.LIBRARY_LAMP.getDefaultState());
        }
        // Exposed vertical braces and top rim give the shaft a distinct circular silhouette.
        for(int[] pos:new int[][]{{-30,0},{30,0},{0,-30},{0,30},{-21,-21},{21,-21},{-21,21},{21,21}}) {
            a.box(pos[0],4,pos[1],pos[0],40,pos[1],METAL);a.block(pos[0],41,pos[1],SLAB);
        }
        a.sign(5,BOTTOM+4,-32,EnumFacing.WEST,"炉心检修道","旧灼热地狱");a.box(6,BOTTOM+3,-32,6,BOTTOM+5,-32,FRAME);
    }
    private static void wing(GensokyoArchitecture a,int left,int right,boolean bath) {
        int mid=(left+right)/2;
        a.box(left,3,-28,right,24,27,STONE);a.box(left+1,4,-27,right-1,23,26,AIR);
        a.box(left,14,-28,right,14,27,METAL);a.box(left,25,-29,right,25,28,FRAME);
        for(int f:new int[]{3,14}) {
            a.box(left+1,f+1,-2,right-1,f+9,-2,STONE);a.openZ(mid,-2,f,2,5);
            for(int z:new int[]{-19,-8,8,20})for(int x:new int[]{left,right}) {
                a.box(x,f+3,z-3,x,f+6,z+3,GREEN);a.block(x<0?x+1:x-1,f+7,z,ModBlocks.LIBRARY_LAMP.getDefaultState());
            }
            a.chest(left+2,f,-25,bath?"old_hell_household":"kappa_parts");a.chest(right-2,f,-25,bath?"old_hell_household":"kappa_tools");
            a.room(bath?f==3?"温泉浴室":"浴场备品间":f==3?"核融合研究室":"观测资料室",bath && f==3?left+2:mid,f,-10);
            a.room(bath?f==3?"更衣与洗涤":"轮值起居":f==3?"器具修配":"研究员休息",mid+6,f,8);
        }
        a.box(mid-2,14,3,mid+2,14,15,AIR);a.stairsSouth(mid,3,3,14,2);
        a.openX(bath?right:left,0,3,3,6);
        for(int x:new int[]{left+3,right-3}) {
            a.bed(x,14,24);a.chest(x,14,17,"kappa_tools");
            a.block(x,4,23,Blocks.CAULDRON.getDefaultState());
        }
        a.table(left+6,14,19,5);a.box(left+6,15,21,left+10,15,21,Blocks.STONE_BRICK_STAIRS.getDefaultState());
        if(bath) {
            a.box(left+3,4,-23,right-3,4,-7,STONE);
            a.box(left+4,4,-22,right-4,4,-8,Blocks.WATER.getDefaultState());
            a.box(left+4,5,-22,right-4,5,-8,AIR);
            a.box(mid-2,4,-7,mid+2,4,-7,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
            for(int x:new int[]{left+3,right-3})for(int z:new int[]{7,12,17})a.chest(x,3,z,"old_hell_household");
            a.box(left+3,15,-22,left+3,17,-7,Blocks.BOOKSHELF.getDefaultState());
            a.box(right-3,15,-22,right-3,17,-7,Blocks.WOOL.getStateFromMeta(0));
            a.table(mid-3,14,-16,6);
        }else {
            for(int x:new int[]{left+4,right-4}) {
                a.box(x,4,-22,x,5,-9,METAL);
                for(int z:new int[]{-20,-15,-10})a.block(x,6,z,Blocks.FLOWER_POT.getDefaultState());
                a.box(x,15,-21,x,17,-6,Blocks.BOOKSHELF.getDefaultState());
                a.block(x,4,15,Blocks.CRAFTING_TABLE.getDefaultState());a.block(x,4,19,Blocks.FURNACE.getDefaultState());
            }
            a.block(mid,4,-7,ModBlocks.RESEARCH_NOTES.getDefaultState());a.block(mid,15,-12,ModBlocks.WRITING_DESK.getDefaultState());
        }
    }
    private static void reception(GensokyoArchitecture a) {
        a.box(-31,3,32,31,12,46,STONE);a.box(-30,4,33,30,11,45,AIR);
        a.box(-32,13,31,32,13,47,FRAME);
        for(int z:new int[]{32,46})a.openZ(0,z,3,5,7);
        for(int x:new int[]{-24,-14,14,24})a.box(x-3,6,46,x+3,9,46,GREEN);
        a.table(-25,3,37,14);a.box(14,4,37,26,4,37,Blocks.STONE_BRICK_STAIRS.getDefaultState());
        a.chest(-26,3,35,"kappa_parts");a.block(-12,4,35,ModBlocks.RESEARCH_NOTES.getDefaultState());
        for(int x:new int[]{-26,-13,0,13,26}) {a.box(x,10,41,x,11,41,RAIL);a.block(x,9,41,ModBlocks.LIBRARY_LAMP.getDefaultState());}
        a.room("接待与浴场入口",0,3,40);a.room("登记台",-18,3,40);
    }
    static void buildLower(GensokyoBlueprint p) {
        GensokyoArchitecture a=new GensokyoArchitecture(p,OldHellWorld.ORIGIN,0,BlazingHell.CORE_Z,"间歇泉深层通路");
        int f=BlazingHell.DECK+96;
        a.box(-6,f-3,-102,6,f+10,-76,FRAME);
        a.box(-4,f+1,-101,4,f+8,-73,AIR);a.box(-4,f,-101,4,f,-73,METAL);
        for(int z:new int[]{-95,-81})a.block(0,f+8,z,LIGHT);
        a.room("上行接续",0,f,-98);a.room("返回炉心",0,f,-88);
    }
}
