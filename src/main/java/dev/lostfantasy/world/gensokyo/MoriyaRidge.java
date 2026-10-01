package dev.lostfantasy.world.gensokyo;

import net.minecraft.block.BlockStairs;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A rock shoulder under the shrine, with separate approaches from the mountain and lake. */
final class MoriyaRidge {
    static final int HEIGHT=12;
    private MoriyaRidge() {}
    static void build(GensokyoArchitecture a) {
        for(int y=0;y<=HEIGHT;y++) {
            int spread=HEIGHT-y,left=-94-spread*3/4,right=84+spread*2,back=-82-spread*2,front=87+spread*7/2;
            for(int z=back;z<=front;z++) {
                int cut=Math.max(0,8-Math.min(z-back,front-z));
                a.box(left+cut,y,z,right-cut,y,z,Blocks.STONE.getDefaultState());
                if(z%4!=0) {
                    a.block(left+cut,y+1,z,Blocks.GRASS.getDefaultState());
                    a.block(right-cut,y+1,z,Blocks.GRASS.getDefaultState());
                }
            }
        }
        a.box(-94,0,-82,84,HEIGHT,87,Blocks.STONE.getDefaultState());
        a.box(-95,HEIGHT+1,-83,85,HEIGHT+43,88,AIR);
    }
    static void approaches(GensokyoArchitecture a) {
        // The existing mountain road meets the low side path; it never enters a solid terrace.
        a.box(-121,-6,-4,-114,0,62,STONE);a.box(-121,1,-4,-114,16,62,AIR);
        a.box(-118,0,52,-93,0,62,STONE);a.box(-118,1,52,-93,16,62,AIR);
        for(int rise=1;rise<=HEIGHT;rise++) {
            int x=-117+(rise-1)*2;
            a.box(x,1,53,x+1,rise,61,STONE);
            a.box(x,rise,53,x,rise,61,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.EAST));
            for(int z:new int[]{52,62}) {
                a.box(x,0,z,x+1,rise,z,STONE);
                a.box(x,rise+1,z,x+1,rise+1,z,Blocks.SPRUCE_FENCE.getDefaultState());
            }
        }
        a.box(-93,1,53,-92,HEIGHT,61,STONE);
        // Broad shallow flights climb from the low torii to the upper courtyard.
        a.box(-13,-4,88,13,0,132,STONE);a.box(-13,1,88,13,16,132,AIR);
        for(int rise=1;rise<=HEIGHT;rise++) {
            int z=124-rise*3;
            a.box(-10,1,z,10,rise,z+2,STONE);
            a.box(-10,rise,z+2,10,rise,z+2,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
            for(int x:new int[]{-12,12}) {
                a.box(x,0,z,x,rise,z+2,STONE);
                a.box(x,rise+1,z,x,rise+1,z+2,Blocks.SPRUCE_FENCE.getDefaultState());
            }
        }
        for(int x:new int[]{-17,17})a.lamp(x,6,107);
        a.room("山道石阶",-102,8,57);a.room("湖向石阶",0,6,106);
    }
}
