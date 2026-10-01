package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Timber homes along the forest camp's lane, with shared trade buildings kept separate. */
final class YamawaroHomes {
    private static final int[][] HOMES={{-130,-19,14,10,2,0},{-91,-20,12,11,1,0},{-55,-18,10,12,1,0},
            {-129,18,12,12,1,1},{-91,18,13,11,1,1},{-55,18,10,11,2,1}};
    private static final IBlockState PANEL=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(13),
            TILE=Blocks.WOODEN_SLAB.getStateFromMeta(5),TOP_TILE=Blocks.WOODEN_SLAB.getStateFromMeta(13);
    private YamawaroHomes() {}
    static void build(GensokyoBlueprint plan) {
        for(int id=0;id<HOMES.length;id++) {
            int[] p=HOMES[id];int h=p[2],d=p[3],storeys=p[4];
            GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.SECRET_CLIFF,p[0],p[1],
                    "秘天崖·林舍"+(id+1),p[5]==0?Rotation.NONE:Rotation.CLOCKWISE_180);
            shell(a,h,d,storeys,id);furnish(a,h,d,storeys);
        }
    }
    private static void shell(GensokyoArchitecture a,int h,int d,int storeys,int variant) {
        int ceiling=1+5*storeys;
        a.box(-h-1,0,-d-1,h+1,0,d+2,Blocks.MOSSY_COBBLESTONE.getDefaultState());
        a.box(-h,1,-d,h,ceiling,d,WOOD);a.box(-h+1,2,-d+1,h-1,ceiling-1,d-1,AIR);
        for(int floor=1;floor<=ceiling;floor+=5)a.box(-h,floor,-d,h,floor,d,WOOD);
        for(int floor=1;floor<ceiling;floor+=5) {
            for(int z:new int[]{-d,d}) {
                a.box(-h,floor+4,z,h,floor+4,z,DARK);
                for(int x=-h;x<=h;x+=5) {
                    a.box(x,floor+1,z,x,floor+4,z,LOG);
                    if(x+3<h) {
                        a.box(x+1,floor+1,z,x+3,floor+1,z,PANEL);
                        a.box(x+1,floor+2,z,x+3,floor+3,z,PAPER);
                    }
                }
            }
            for(int x:new int[]{-h,h}) {
                a.box(x,floor+4,-d,x,floor+4,d,DARK);
                for(int z=-d+2;z+3<d;z+=6)a.box(x,floor+2,z,x,floor+3,z+3,PAPER);
            }
        }
        for(int x:new int[]{-h,h})for(int z:new int[]{-d,d})a.box(x,2,z,x,ceiling,z,LOG);
        roof(a,-h,-d,h,d,ceiling+1,variant%3==1);
        a.openZ(0,d,1,1,3);a.box(-h+1,1,d+1,h-1,1,d+2,WOOD);
        a.box(-2,1,d+3,2,1,d+3,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
        a.box(-h+1,5,d+1,h-1,5,d+2,TOP_TILE);
        for(int x:new int[]{-h+2,h-2})a.box(x,2,d+2,x,5,d+2,LOG);
    }
    private static void furnish(GensokyoArchitecture a,int h,int d,int storeys) {
        int bedFloor=storeys==2?6:1;
        for(int floor=1;floor<=bedFloor;floor+=5) {
            a.box(-h+1,floor+1,-1,storeys==2?h-6:h-1,floor+4,-1,PANEL);a.openZ(0,-1,floor,1,3);
            for(int z:new int[]{-4,d-4})VillageJoinery.lantern(a,-5,floor,z);
        }
        if(storeys==2) {
            int x=h-3,z=-d+3;
            a.box(x-1,2,z,x+1,10,z+4,AIR);a.stairsSouth(x,z,1,6,1);
            a.box(x-2,7,z,x-2,7,z+4,Blocks.SPRUCE_FENCE.getDefaultState());
            a.box(x-1,7,z+5,x+1,7,z+5,Blocks.SPRUCE_FENCE.getDefaultState());
            a.table(-h+3,1,-d+3,5);a.block(-h+4,2,-d+6,Blocks.CRAFTING_TABLE.getDefaultState());
            a.box(-h+1,2,-d+1,-h+1,3,-d+4,Blocks.BOOKSHELF.getDefaultState());
            VillageJoinery.lowDesk(a,-h+3,6,d-4,5);
            a.room("修具间",-5,1,-3);a.room("楼上书桌",0,6,3);
        }
        a.box(0,bedFloor+1,-d+1,0,bedFloor+4,-2,WHITE);a.openX(0,-4,bedFloor,1,3);
        a.bed(-5,bedFloor,-d+4);a.bed(4,bedFloor,-d+4);
        a.chest(-h+2,bedFloor,-d+2,"village_pantry");a.chest(2,bedFloor,-d+2,"yamawaro_tools");
        a.chest(h-2,1,2,"yamawaro_tools");
        a.block(-h+2,2,3,Blocks.FURNACE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
        a.block(-h+4,2,3,Blocks.CAULDRON.getStateFromMeta(3));a.block(-h+6,2,3,Blocks.CRAFTING_TABLE.getDefaultState());
        VillageJoinery.lowDesk(a,2,1,d-4,h-4);
        a.room("西寝间",-5,bedFloor,-2);a.room("东寝间",4,bedFloor,-2);
        a.room("茶饭间",0,1,3);a.room("器具角",h-4,1,3);
        InteriorFinishes.tatami(a,-h+2,-d+2,h-6,-3,bedFloor);
        a.box(h-2,3,3,h-2,3,7,Blocks.WOODEN_SLAB.getStateFromMeta(9));
        for(int z:new int[]{3,6})a.block(h-2,4,z,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.box(-h+2,2,d+1,-h+5,2,d+1,Blocks.LOG.getStateFromMeta(4));
        a.box(-h+2,3,d+1,-h+5,3,d+1,Blocks.WOODEN_SLAB.getStateFromMeta(1));
    }
    static void roof(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int base,boolean ridgeZ) {
        int low=(ridgeZ?x1:z1)-2,high=(ridgeZ?x2:z2)+2;
        for(int p=low;p<=high;p++) {
            int course=Math.min(p-low,high-p),y=base+course/2;IBlockState tile=course%2==0?TILE:TOP_TILE;
            if(ridgeZ) {
                a.box(p,y,z1-2,p,y,z2+2,tile);
                if(y>base && p>=x1 && p<=x2)for(int z:new int[]{z1,z2})a.box(p,base,z,p,y-1,z,DARK);
            } else {
                a.box(x1-2,y,p,x2+2,y,p,tile);
                if(y>base && p>=z1 && p<=z2)for(int x:new int[]{x1,x2})a.box(x,base,p,x,y-1,p,DARK);
            }
        }
        int middle=(low+high)/2,y=base+(high-low)/4+1;
        if(ridgeZ)a.box(middle,y,z1-2,middle,y,z2+2,TILE);else a.box(x1-2,y,middle,x2+2,y,middle,TILE);
    }
}
