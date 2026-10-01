package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Individual abandoned dwellings surrounding the cats' paths and old fields. */
final class MayohigaHomes {
    static final House[] HOUSES={
            new House(-78,-70,15,11,1,2,Rotation.NONE),
            new House(-7,-72,22,13,2,0,Rotation.NONE),
            new House(68,-65,18,12,2,1,Rotation.NONE),
            new House(-82,-20,14,12,1,0,Rotation.COUNTERCLOCKWISE_90),
            new House(82,-12,14,13,1,2,Rotation.CLOCKWISE_90),
            new House(-72,43,18,11,1,1,Rotation.COUNTERCLOCKWISE_90),
            new House(76,48,16,12,1,0,Rotation.CLOCKWISE_90),
            new House(-15,18,18,12,1,2,Rotation.NONE),
            new House(40,-30,10,8,1,0,Rotation.NONE)};
    private static final IBlockState TILE=Blocks.STONE_SLAB.getStateFromMeta(5),TOP_TILE=Blocks.STONE_SLAB.getStateFromMeta(13);
    private MayohigaHomes() {}

    static void build(GensokyoBlueprint plan) {
        for(int id=0;id<HOUSES.length;id++) {
            House h=HOUSES[id];GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.MAYOHIGA,h.x,h.z,
                    "迷途之家·"+(id==8?"旧农具屋":"旧屋"+(id+1)),h.rotation);
            shell(a,h);porch(a,h);
            if(id==8)store(a,h);else home(a,h);
            weathering(a,h,id);
        }
    }
    private static void shell(GensokyoArchitecture a,House h) {
        int ceiling=1+5*h.storeys;
        a.box(-h.h-1,0,-h.d-1,h.h+1,0,h.d+1,STONE);
        a.box(-h.h,1,-h.d,h.h,ceiling,h.d,WHITE);
        a.box(-h.h+1,2,-h.d+1,h.h-1,ceiling-1,h.d-1,AIR);
        for(int floor=1;floor<=ceiling;floor+=5)a.box(-h.h,floor,-h.d,h.h,floor,h.d,WOOD);
        for(int floor=1;floor<ceiling;floor+=5) {
            for(int z:new int[]{-h.d,h.d}) {
                a.box(-h.h,floor+1,z,h.h,floor+1,z,DARK);
                for(int x=-h.h;x<=h.h;x+=5) {
                    a.box(x,floor+1,z,x,floor+4,z,LOG);
                    if(x+3<h.h)a.box(x+1,floor+2,z,x+3,floor+3,z,PAPER);
                }
                a.box(-h.h,floor+4,z,h.h,floor+4,z,DARK);
            }
            for(int x:new int[]{-h.h,h.h}) {
                a.box(x,floor+1,-h.d,x,floor+1,h.d,DARK);
                for(int z=-h.d;z<=h.d;z+=5) {
                    a.box(x,floor+1,z,x,floor+4,z,LOG);
                    if(z+3<h.d)a.box(x,floor+2,z+1,x,floor+3,z+3,PAPER);
                }
                a.box(x,floor+4,-h.d,x,floor+4,h.d,DARK);
            }
        }
        for(int x:new int[]{-h.h,h.h})for(int z:new int[]{-h.d,h.d})a.box(x,2,z,x,ceiling,z,LOG);
        roof(a,h,ceiling+1);
    }
    private static void roof(GensokyoArchitecture a,House h,int base) {
        if(h.roof==2) {
            // Broad hipped roofs end in a short ridge instead of a pyramidal point.
            int courses=Math.min(h.h,h.d)+2;
            for(int n=0;n<=courses;n++)a.box(-h.h-2+n,base+n/2,-h.d-2+n,h.h+2-n,base+n/2,h.d+2-n,n%2==0?TILE:TOP_TILE);
            a.box(-h.h+h.d,base+courses/2+1,0,h.h-h.d,base+courses/2+1,0,TILE);
            return;
        }
        boolean alongZ=h.roof==1;int extent=(alongZ?h.h:h.d)+2;
        for(int p=-extent;p<=extent;p++) {
            int course=extent-Math.abs(p),y=base+course/2;IBlockState tile=course%2==0?TILE:TOP_TILE;
            if(alongZ) {
                a.box(p,y,-h.d-2,p,y,h.d+2,tile);
                if(y>base && Math.abs(p)<=h.h)for(int z:new int[]{-h.d,h.d})a.box(p,base,z,p,y-1,z,DARK);
            } else {
                a.box(-h.h-2,y,p,h.h+2,y,p,tile);
                if(y>base && Math.abs(p)<=h.d)for(int x:new int[]{-h.h,h.h})a.box(x,base,p,x,y-1,p,DARK);
            }
        }
        if(alongZ)a.box(0,base+extent/2+1,-h.d-2,0,base+extent/2+1,h.d+2,TILE);
        else a.box(-h.h-2,base+extent/2+1,0,h.h+2,base+extent/2+1,0,TILE);
    }
    private static void porch(GensokyoArchitecture a,House h) {
        a.openZ(0,h.d,1,1,3);
        a.box(-h.h-1,0,h.d+1,h.h+1,0,h.d+3,STONE);
        a.box(-h.h-1,1,h.d+1,h.h+1,1,h.d+3,WOOD);
        a.box(-2,1,h.d+4,2,1,h.d+4,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
        a.box(-h.h-1,6,h.d+1,h.h+1,6,h.d+1,TILE);
        a.box(-h.h-1,5,h.d+2,h.h+1,5,h.d+4,TOP_TILE);
        for(int x:new int[]{-h.h+1,h.h-1})a.box(x,2,h.d+3,x,5,h.d+3,LOG);
        for(int x:new int[]{-h.h-1,h.h+1})a.box(x,2,h.d+1,x,2,h.d+3,Blocks.SPRUCE_FENCE.getDefaultState());
    }
    private static void home(GensokyoArchitecture a,House h) {
        int sleeping=h.storeys==2?6:1;
        partition(a,-h.h+1,h.storeys==2?h.h-6:h.h-1,-3,1,0);
        if(h.storeys==2) {
            int x=h.h-3,z=-h.d+3;
            a.box(x-1,2,z,x+1,10,z+4,AIR);a.stairsSouth(x,z,1,6,1);
            a.box(x-2,7,z,x-2,7,z+4,Blocks.SPRUCE_FENCE.getDefaultState());
            a.box(x-1,7,z+5,x+1,7,z+5,Blocks.SPRUCE_FENCE.getDefaultState());
            partition(a,-h.h+1,h.h-6,-3,6,0);
            a.box(-h.h+2,2,-h.d+2,-h.h+6,3,-h.d+3,Blocks.HAY_BLOCK.getDefaultState());
            a.chest(-h.h+3,1,-7,"mayohiga_household");
            a.table(2,1,-h.d+3,5);a.block(7,2,-h.d+3,Blocks.CRAFTING_TABLE.getDefaultState());
            a.room("旧后仓",-5,1,-7);
            VillageJoinery.lowDesk(a,-h.h+3,6,5,7);a.block(h.h-7,7,4,ModBlocks.WRITING_DESK.getDefaultState());
            a.box(-h.h+1,7,0,-h.h+1,9,3,Blocks.BOOKSHELF.getDefaultState());
            a.room("楼上起居",0,6,4);VillageJoinery.lantern(a,0,6,h.d-4);
        }
        a.box(0,sleeping+1,-h.d+1,0,sleeping+4,-4,WHITE);a.openX(0,-7,sleeping,1,3);
        for(int side:new int[]{-1,1}) {
            a.bed(side*6,sleeping,-h.d+4);
            a.chest(side*(h.h-(side>0 && h.storeys==2?7:3)),sleeping,-h.d+2,"mayohiga_household");
            a.box(side<0?-h.h+1:h.h-8,sleeping+1,-h.d+1,side<0?-h.h+3:h.h-6,sleeping+2,-h.d+1,DARK);
            a.room(side<0?"西旧寝间":"东旧寝间",side*6,sleeping,-5);
        }
        a.block(-h.h+3,2,0,Blocks.FURNACE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
        a.block(-h.h+6,2,0,Blocks.CAULDRON.getStateFromMeta(1));
        a.block(-h.h+9,2,0,Blocks.CRAFTING_TABLE.getDefaultState());
        a.box(-h.h+2,2,5,-h.h+6,2,5,Blocks.WOODEN_SLAB.getStateFromMeta(1));
        VillageJoinery.lowDesk(a,3,1,h.d-5,h.h-6);
        a.room("旧灶间",-h.h+7,1,3);a.room("炉边起居",0,1,4);a.room("缘侧",-4,1,h.d+2);
        VillageJoinery.lantern(a,0,1,h.d-4);
        a.block(-h.h,2,3,AIR);a.block(h.h,2,-5,AIR);
    }
    private static void partition(GensokyoArchitecture a,int left,int right,int z,int floor,int door) {
        a.box(left,floor+1,z,right,floor+4,z,WHITE);a.openZ(door,z,floor,1,3);
    }
    private static void store(GensokyoArchitecture a,House h) {
        for(int x:new int[]{-7,7})for(int z:new int[]{-5,1})a.chest(x,1,z,"mayohiga_household");
        a.box(-3,2,-6,3,3,-5,Blocks.HAY_BLOCK.getDefaultState());
        a.block(-6,2,5,Blocks.CRAFTING_TABLE.getDefaultState());a.table(2,1,4,5);
        a.room("农具收存",0,1,0);a.room("缘侧",-4,1,h.d+2);
        VillageJoinery.lantern(a,0,1,2);
    }
    private static void weathering(GensokyoArchitecture a,House h,int id) {
        for(int x=-h.h;x<=h.h;x+=4)for(int z:new int[]{-h.d,h.d})a.block(x,0,z,Blocks.MOSSY_COBBLESTONE.getDefaultState());
        a.box(3,3,-h.d,6,4,-h.d,DARK);
        for(int z=-h.d+1;z<h.d;z+=4)a.block(-h.h,3,z,Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(8));
        a.box(-h.h-1,1,-h.d+2,-h.h-1,4,-h.d+2,Blocks.VINE.getDefaultState().withProperty(net.minecraft.block.BlockVine.EAST,true));
        // Isolated lost plaster exposes timber; all occupied floors and sheltering roofs remain.
        if(id%2==0)a.box(h.h,2,h.d-5,h.h,3,h.d-4,WOOD);
    }
    static final class House {
        final int x,z,h,d,storeys,roof;final Rotation rotation;
        House(int x,int z,int h,int d,int storeys,int roof,Rotation rotation) {
            this.x=x;this.z=z;this.h=h;this.d=d;this.storeys=storeys;this.roof=roof;this.rotation=rotation;
        }
        BlockPos world(int lx,int y,int lz) {
            BlockPos p=new BlockPos(lx,y,lz).rotate(rotation);return p.add(GensokyoAtlas.MAYOHIGA.x+x,GensokyoAtlas.MAYOHIGA.y,GensokyoAtlas.MAYOHIGA.z+z);
        }
    }
}
