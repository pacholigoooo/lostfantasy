package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Blue-tiled timber houses belonging to the Old Capital. */
final class OldCapitalJoinery {
    private static final IBlockState TILE=ModBlocks.BLUE_ROOF_TILE.getDefaultState();
    private OldCapitalJoinery() {}

    static void shell(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int storeys,int variant) {
        int ceiling=2+6*storeys;
        a.box(x1-1,0,z1-1,x2+1,1,z2+1,STONE);
        a.box(x1,2,z1,x2,ceiling,z2,WHITE);
        a.box(x1+1,3,z1+1,x2-1,ceiling-1,z2-1,AIR);
        for(int floor=2;floor<=ceiling;floor+=6)a.box(x1,floor,z1,x2,floor,z2,WOOD);
        for(int floor=2;floor<ceiling;floor+=6) {
            IBlockState panel=floor==2 || variant%3==0?WOOD:WHITE;
            for(int z:new int[]{z1,z2}) {
                a.box(x1,floor+1,z,x2,floor+5,z,panel);
                for(int x=x1;x<x2;x+=5) {
                    a.box(x,floor+1,z,x,floor+5,z,LOG);
                    if(x+4<x2)a.box(x+1,floor+2,z,x+4,floor+4,z,floor==2?Blocks.SPRUCE_FENCE.getDefaultState():PAPER);
                }
                a.box(x1,floor+5,z,x2,floor+5,z,DARK);
            }
            for(int x:new int[]{x1,x2}) {
                a.box(x,floor+1,z1,x,floor+5,z2,panel);
                for(int z=z1+2;z+3<z2;z+=6)a.box(x,floor+2,z,x,floor+4,z+3,PAPER);
                a.box(x,floor+5,z1,x,floor+5,z2,DARK);
            }
        }
        for(int x:new int[]{x1,x2})for(int z:new int[]{z1,z2})a.box(x,3,z,x,ceiling,z,LOG);
        roof(a,x1,z1,x2,z2,ceiling+1,variant%4==1);
    }

    static void roof(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int base,boolean ridgeZ) {
        int from=(ridgeZ?x1:z1)-2,to=(ridgeZ?x2:z2)+2,middle=(from+to)/2;
        for(int p=from;p<=to;p++) {
            int course=Math.min(p-from,to-p),y=base+course/2;
            EnumFacing uphill= ridgeZ?(p<=middle?EnumFacing.EAST:EnumFacing.WEST):(p<=middle?EnumFacing.SOUTH:EnumFacing.NORTH);
            IBlockState tile=course%2==0?ModBlocks.BLUE_ROOF_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,uphill):TILE;
            if(p==middle)tile=TILE;
            if(ridgeZ) {
                a.box(p,y,z1-2,p,y,z2+2,tile);
                if(y>base && p>=x1 && p<=x2)for(int z:new int[]{z1,z2})a.box(p,base,z,p,y-1,z,DARK);
            } else {
                a.box(x1-2,y,p,x2+2,y,p,tile);
                if(y>base && p>=z1 && p<=z2)for(int x:new int[]{x1,x2})a.box(x,base,p,x,y-1,p,DARK);
            }
        }
        int top=base+(to-from)/4+1;
        if(ridgeZ) {
            a.box(middle,top,z1-2,middle,top,z2+2,TILE);
            for(int z:new int[]{z1-2,z2+2})a.block(middle,top+1,z,TILE);
        } else {
            a.box(x1-2,top,middle,x2+2,top,middle,TILE);
            for(int x:new int[]{x1-2,x2+2})a.block(x,top+1,middle,TILE);
        }
    }

    static void entrance(GensokyoArchitecture a,int x,int front,int half) {
        a.openZ(x,front,2,half,4);
        a.stairsSouth(x,front+1,0,2,half);
    }

    static void shopfront(GensokyoArchitecture a,int half,int front,int variant,String name) {
        a.box(-half,8,front+1,half,8,front+1,TILE);
        a.box(-half,7,front+2,half,7,front+3,TILE);
        for(int x:new int[]{-half+1,half-1})a.box(x,1,front+3,x,6,front+3,LOG);
        a.box(-3,7,front+1,3,7,front+1,DARK);
        for(int x:new int[]{-2,0,2})a.block(x,6,front+1,Blocks.WOOL.getStateFromMeta(variant%3==0?14:variant%3==1?11:0));
        a.sign(0,7,front+2,EnumFacing.SOUTH,name,"");
        for(int x:new int[]{-half+3,half-3})a.block(x,6,front+2,ModBlocks.RED_LANTERN.getDefaultState());
    }

    static void stair(GensokyoArchitecture a,int right,int back) {
        int x=right-3,z=back+4;
        a.box(x-1,3,z,x+1,13,z+5,AIR);
        a.stairsSouth(x,z,2,8,1);
        a.box(x-2,9,z,x-2,9,z+5,Blocks.DARK_OAK_FENCE.getDefaultState());
        a.box(x-1,9,z+6,x+1,9,z+6,Blocks.DARK_OAK_FENCE.getDefaultState());
    }

    static void light(GensokyoArchitecture a,int x,int z,int floor) {a.block(x,floor+5,z,ModBlocks.RED_LANTERN.getDefaultState());}
    static void kitchen(GensokyoArchitecture a,int x,int z,int floor) {
        a.block(x,floor+1,z,Blocks.FURNACE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
        a.block(x+2,floor+1,z,Blocks.CAULDRON.getStateFromMeta(3));
        a.block(x+4,floor+1,z,Blocks.CRAFTING_TABLE.getDefaultState());
    }
    static void bench(GensokyoArchitecture a,int x,int z,int floor,int length,EnumFacing facing) {
        a.box(x,floor+1,z,x+length-1,floor+1,z,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,facing));
    }
}
