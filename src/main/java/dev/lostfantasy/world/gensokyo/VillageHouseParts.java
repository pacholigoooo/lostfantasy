package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Joinery for the rebuilt human houses; other settlements keep their own architecture. */
final class VillageHouseParts {
    private static final IBlockState TILE=Blocks.STONE_SLAB.getStateFromMeta(5),
            TILE_TOP=Blocks.STONE_SLAB.getStateFromMeta(13);
    private VillageHouseParts() {}

    static void shell(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int storeys,int variant) {
        int ceiling=1+5*storeys;
        a.box(x1-1,0,z1-1,x2+1,0,z2+1,STONE);
        a.box(x1,1,z1,x2,ceiling,z2,WHITE);
        a.box(x1+1,2,z1+1,x2-1,ceiling-1,z2-1,AIR);
        for(int f=1;f<=ceiling;f+=5) {
            a.box(x1,f,z1,x2,f,z2,WOOD);
            if(f>1 && f<ceiling)InteriorFinishes.tatami(a,x1+2,z1+2,x2-2,z2-2,f);
        }
        for(int f=1;f<ceiling;f+=5) {
            // Dark boarded lower walls and pale upper rooms form a continuous shop frontage.
            IBlockState panel=(f==1?variant%3!=1:variant%4==0)?WOOD:WHITE;
            for(int z:new int[]{z1,z2}) {
                a.box(x1,f+1,z,x2,f+4,z,panel);
                for(int x=x1;x<=x2;x+=4) {
                    a.box(x,f+1,z,x,f+4,z,LOG);
                    if(x+3<x2)a.box(x+1,f+2,z,x+3,f+3,z,f==1?Blocks.SPRUCE_FENCE.getDefaultState():PAPER);
                }
                a.box(x1,f+4,z,x2,f+4,z,DARK);
            }
            for(int x:new int[]{x1,x2}) {
                a.box(x,f+1,z1,x,f+4,z2,panel);
                for(int z=z1;z<=z2;z+=5) {
                    a.box(x,f+1,z,x,f+4,z,LOG);
                    if(z+3<z2)a.box(x,f+2,z+1,x,f+3,z+3,PAPER);
                }
                a.box(x,f+4,z1,x,f+4,z2,DARK);
            }
        }
        for(int x:new int[]{x1,x2})for(int z:new int[]{z1,z2})a.box(x,2,z,x,ceiling,z,LOG);
        boolean ridgeZ=z2-z1>x2-x1+5 && variant%3==1;
        roof(a,x1,z1,x2,z2,ceiling+1,ridgeZ);
    }

    /** Half-block courses give a shallow tile roof, with closed gables and a separate ridge. */
    static void roof(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int base,boolean ridgeZ) {
        int from=(ridgeZ?x1:z1)-2,to=(ridgeZ?x2:z2)+2;
        for(int p=from;p<=to;p++) {
            int course=Math.min(p-from,to-p),y=base+course/2;
            IBlockState tile=course%2==0?TILE:TILE_TOP;
            if(ridgeZ) {
                a.box(p,y,z1-2,p,y,z2+2,tile);
                if(y>base)for(int z:new int[]{z1,z2})a.box(p,base,z,p,y-1,z,DARK);
            } else {
                a.box(x1-2,y,p,x2+2,y,p,tile);
                if(y>base)for(int x:new int[]{x1,x2})a.box(x,base,p,x,y-1,p,DARK);
            }
        }
        int middle=(from+to)/2,top=base+(to-from)/4+1;
        if(ridgeZ)a.box(middle,top,z1-2,middle,top,z2+2,TILE);
        else a.box(x1-2,top,middle,x2+2,top,middle,TILE);
    }

    static void entrance(GensokyoArchitecture a,int x,int front,int half) {
        a.openZ(x,front,1,half,3);
        a.box(x-half-1,1,front+1,x+half+1,1,front+1,WOOD);
        a.box(x-half-1,1,front+2,x+half+1,1,front+2,
                Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
    }

    static void canopy(GensokyoArchitecture a,int x1,int x2,int front,int dye,String name) {
        a.box(x1,6,front+1,x2,6,front+1,TILE);
        a.box(x1,5,front+2,x2,5,front+3,TILE_TOP);
        for(int x:new int[]{x1+1,x2-1})a.box(x,2,front+2,x,4,front+2,Blocks.SPRUCE_FENCE.getDefaultState());
        // The short noren leaves two full blocks above the entry floor.
        for(int x=-2;x<=2;x+=2)a.block(x,4,front+1,Blocks.WOOL.getStateFromMeta(dye));
        a.box(-2,5,front+1,2,5,front+1,DARK);
        a.sign(0,5,front+2,EnumFacing.SOUTH,name,"");
        a.block(x1+2,4,front+1,ModBlocks.RED_LANTERN.getDefaultState());
        // Brackets sit under the outer awning, clear of the noren and the entry steps.
        for(int x:new int[]{x1+1,x2-1})a.block(x,4,front+3,Blocks.SPRUCE_STAIRS.getDefaultState()
                .withProperty(BlockStairs.FACING,EnumFacing.NORTH).withProperty(BlockStairs.HALF,BlockStairs.EnumHalf.TOP));
    }

    static void stair(GensokyoArchitecture a,int right,int back) {
        int x=right-3,z=back+3;
        a.box(x-1,2,z,x+1,10,z+4,AIR);
        a.stairsSouth(x,z,1,6,1);
        a.box(x-2,7,z,x-2,7,z+4,Blocks.SPRUCE_FENCE.getDefaultState());
        a.box(x-1,7,z+5,x+1,7,z+5,Blocks.SPRUCE_FENCE.getDefaultState());
    }

    static void kitchen(GensokyoArchitecture a,int x,int z,int floor) {
        a.block(x,floor+1,z,Blocks.FURNACE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
        a.block(x+2,floor+1,z,Blocks.CAULDRON.getStateFromMeta(3));
        a.block(x+4,floor+1,z,Blocks.CRAFTING_TABLE.getDefaultState());
    }

    static void roomLight(GensokyoArchitecture a,int x,int z,int floor) {
        a.block(x,floor+4,z,ModBlocks.RED_LANTERN.getDefaultState());
    }

    static void bench(GensokyoArchitecture a,int x,int z,int length) {
        a.box(x,1,z,x+length-1,1,z,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
    }
}
