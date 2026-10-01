package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Small dry-bank homes, a courtyard and galleries beside the workshop's watercourse. */
final class KappaHomes {
    private static final IBlockState PANEL=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(9),
            WINDOW=Blocks.STAINED_GLASS_PANE.getStateFromMeta(9),
            TILE=Blocks.STONE_SLAB.getStateFromMeta(5),TOP_TILE=Blocks.STONE_SLAB.getStateFromMeta(13);
    private KappaHomes() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture bank=new GensokyoArchitecture(plan,GensokyoAtlas.KAPPA);
        paths(bank);
        home(plan,-111,-29,18,15,2,false,Rotation.COUNTERCLOCKWISE_90,"岩边住家");
        courtHome(plan);
        home(plan,-99,88,22,13,1,false,Rotation.CLOCKWISE_180,"南岸长廊屋");
        home(plan,-32,88,16,13,2,true,Rotation.CLOCKWISE_180,"南岸修具屋");
        home(plan,64,85,14,12,1,true,Rotation.CLOCKWISE_90,"下游水边屋");
        home(plan,66,-65,16,12,1,false,Rotation.CLOCKWISE_90,"上游水边屋");
        washDeck(bank);
        for(int[] tree:new int[][]{{-140,-55},{-139,15},{-137,62},{-59,93},{92,-63},{92,87}})
            tree(bank,tree[0],tree[1]);
    }
    private static void paths(GensokyoArchitecture a) {
        path(a,-86,-94,-80,68);path(a,-129,64,84,68);
        path(a,-93,-31,-80,-27);path(a,-91,28,-80,32);
        path(a,-80,-1,-61,3);path(a,-80,44,-63,48);
        path(a,-102,68,-96,71);path(a,-35,68,-29,71);
        path(a,44,-90,47,113);path(a,47,-67,50,-63);path(a,47,83,49,87);
        // These crossings have an open underside, with narrow supports clear of the main flow.
        a.box(18,0,63,44,0,69,WOOD);
        for(int x:new int[]{19,43})for(int z:new int[]{63,69})a.box(x,-8,z,x,-1,z,LOG);
        for(int z:new int[]{63,69})a.box(19,1,z,42,1,z,Blocks.SPRUCE_FENCE.getDefaultState());
        a.box(-4,0,59,4,0,119,WOOD);a.box(-3,1,59,3,3,119,AIR);
        for(int z:new int[]{75,95,115})for(int x:new int[]{-3,3})a.box(x,-8,z,x,-1,z,LOG);
        for(int x:new int[]{-4,4})a.box(x,1,72,x,1,117,Blocks.SPRUCE_FENCE.getDefaultState());
        path(a,4,110,85,114);
        a.box(4,0,109,42,0,115,WOOD);
        for(int x:new int[]{5,40})for(int z:new int[]{109,115})a.box(x,-8,z,x,-1,z,LOG);
        for(int z:new int[]{109,115})a.box(5,1,z,41,1,z,Blocks.SPRUCE_FENCE.getDefaultState());
        for(int[] p:new int[][]{{-78,-71},{-78,9},{-78,57},{-8,62},{50,43},{50,-36},{51,108}})a.lamp(p[0],0,p[1]);
        a.room("西岸住居巷",-83,0,18);a.room("南侧步行桥",31,0,66);
        a.room("东岸长廊",45,0,44);
    }
    private static void path(GensokyoArchitecture a,int x1,int z1,int x2,int z2) {
        a.box(x1,0,z1,x2,0,z2,STONE);a.box(x1,1,z1,x2,3,z2,AIR);
    }
    private static void home(GensokyoBlueprint plan,int x,int z,int h,int d,int storeys,boolean ridgeZ,Rotation rotation,String name) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.KAPPA,x,z,"河童基地·"+name,rotation);
        int ceiling=1+storeys*5;
        a.box(-h-3,1,-d-3,h+3,25,d+5,AIR);
        a.box(-h-1,-3,-d-1,h+1,0,d+3,Blocks.MOSSY_COBBLESTONE.getDefaultState());
        volume(a,-h,-d,h,d,1,ceiling);
        for(int floor=6;floor<ceiling;floor+=5)a.box(-h+1,floor,-d+1,h-1,floor,d-1,WOOD);
        roof(a,-h,-d,h,d,ceiling+1,ridgeZ);
        porch(a,h,d);a.openZ(0,d,1,1,3);
        int bedFloor=storeys==2?6:1;
        for(int floor=1;floor<=bedFloor;floor+=5) {
            a.box(-h+1,floor+1,-3,storeys==2?h-6:h-1,floor+4,-3,WHITE);
            a.openZ(-5,-3,floor,1,3);a.openZ(5,-3,floor,1,3);
            light(a,-7,floor,-5);light(a,3,floor,d-4);
        }
        a.box(0,bedFloor+1,-d+1,0,bedFloor+4,-4,PANEL);a.openX(0,-7,bedFloor,1,3);
        a.bed(-6,bedFloor,-d+4);a.bed(4,bedFloor,-d+4);
        a.chest(-h+2,bedFloor,-d+2,"village_pantry");a.chest(h-6,bedFloor,-d+2,"kappa_parts");
        a.chest(h-2,1,d-3,"kappa_tools");
        kitchen(a,h-3,1,3);
        VillageJoinery.lowDesk(a,-h+3,1,d-5,h-6);
        a.box(-h+2,2,2,-h+2,3,4,Blocks.BOOKSHELF.getDefaultState());
        if(storeys==2) {
            int stairX=h-3,stairZ=-d+4;
            a.box(stairX-1,2,stairZ,stairX+1,10,stairZ+4,AIR);a.stairsSouth(stairX,stairZ,1,6,1);
            a.box(stairX-2,7,stairZ,stairX-2,7,stairZ+4,Blocks.SPRUCE_FENCE.getDefaultState());
            a.box(stairX-1,7,stairZ+5,stairX+1,7,stairZ+5,Blocks.SPRUCE_FENCE.getDefaultState());
            a.table(-h+3,1,-d+3,6);a.block(-h+4,2,-d+6,Blocks.CRAFTING_TABLE.getDefaultState());
            VillageJoinery.lowDesk(a,-h+3,6,d-4,6);
            a.room("修具间",-7,1,-5);a.room("楼上书桌",0,6,3);
        }
        a.room("西寝间",-5,bedFloor,-4);a.room("东寝间",5,bedFloor,-4);
        a.room("茶饭间",-5,1,2);a.room("灶间",h-6,1,5);a.room("前廊",0,1,d+2);
        InteriorFinishes.tatami(a,-h+2,-d+2,h-7,-5,bedFloor);
        // A compact, wall-fixed repair shelf gives each house a working corner.
        a.box(-h+1,4,2,-h+1,4,6,Blocks.WOODEN_SLAB.getStateFromMeta(9));
        a.block(-h+1,5,3,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.block(-h+1,5,6,ModBlocks.KAPPA_PIPE.getDefaultState());
        a.box(h-7,2,d+2,h-3,2,d+2,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
    }
    private static void courtHome(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.KAPPA,-108,30,
                "河童基地·井庭住家",Rotation.COUNTERCLOCKWISE_90);
        int h=18,d=14;
        a.box(-21,1,-17,21,20,19,AIR);a.box(-19,-3,-15,19,0,17,Blocks.MOSSY_COBBLESTONE.getDefaultState());
        volume(a,-h,-d,h,-3,1,6);volume(a,-h,-2,-9,d,1,6);volume(a,9,-2,h,d,1,6);
        roof(a,-h,-d,h,-3,7,false);roof(a,-h,-2,-9,d,7,true);roof(a,9,-2,h,d,7,true);
        a.box(-8,1,-2,8,1,0,WOOD);a.box(-2,1,1,2,1,d+2,WOOD);
        a.box(-8,1,1,-7,1,d,WOOD);a.box(7,1,1,8,1,d,WOOD);
        a.box(-8,1,10,8,1,12,WOOD);
        for(int x:new int[]{-13,0,13})a.openZ(x,-3,1,1,3);
        a.openZ(-13,-2,1,1,3);a.openZ(13,-2,1,1,3);
        a.openX(-9,10,1,1,3);a.openX(9,10,1,1,3);
        a.box(0,2,-d+1,0,5,-4,WHITE);a.openX(0,-6,1,1,3);
        a.bed(-6,1,-10);a.bed(5,1,-10);
        a.chest(-16,1,-12,"village_pantry");a.chest(15,1,-12,"kappa_parts");a.chest(16,1,12,"kappa_tools");
        VillageJoinery.lowDesk(a,-16,1,4,5);kitchen(a,15,1,3);
        for(int x:new int[]{-12,12}) {light(a,x,1,-8);light(a,x,1,8);}
        // A small, contained water court is separate from the flowing river.
        a.box(3,-2,2,6,0,7,STONE);a.box(4,-1,3,5,-1,6,Blocks.WATER.getDefaultState());
        a.box(4,0,3,5,0,6,AIR);a.box(-6,0,2,-4,0,7,Blocks.GRASS.getDefaultState());
        for(int z:new int[]{3,6})a.block(-5,1,z,Blocks.TALLGRASS.getStateFromMeta(2));
        a.box(-2,1,d+3,2,1,d+3,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
        a.room("西寝间",-7,1,-5);a.room("东寝间",6,1,-5);
        a.room("茶饭侧屋",-12,1,9);a.room("灶间侧屋",12,1,10);a.room("井庭木廊",0,1,7);
    }
    private static void volume(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int floor,int ceiling) {
        a.box(x1,floor,z1,x2,ceiling,z2,PANEL);a.box(x1+1,floor+1,z1+1,x2-1,ceiling-1,z2-1,AIR);
        a.box(x1,floor,z1,x2,floor,z2,WOOD);a.box(x1,ceiling,z1,x2,ceiling,z2,WOOD);
        for(int y=floor;y<ceiling;y+=5) {
            for(int z:new int[]{z1,z2}) {
                a.box(x1,y+4,z,x2,y+4,z,DARK);
                for(int x=x1;x<=x2;x+=6) {
                    a.box(x,y+1,z,x,y+4,z,LOG);
                    if(x+4<x2)a.box(x+1,y+2,z,x+4,y+3,z,WINDOW);
                }
            }
            for(int x:new int[]{x1,x2}) {
                a.box(x,y+4,z1,x,y+4,z2,DARK);
                for(int z=z1+2;z+3<z2;z+=6)a.box(x,y+2,z,x,y+3,z+3,WINDOW);
            }
        }
        for(int x:new int[]{x1,x2})for(int z:new int[]{z1,z2})a.box(x,floor+1,z,x,ceiling,z,LOG);
    }
    private static void porch(GensokyoArchitecture a,int h,int d) {
        a.box(-h+1,1,d+1,h-1,1,d+3,WOOD);a.box(-h+1,5,d+1,h-1,5,d+3,TOP_TILE);
        for(int x:new int[]{-h+2,h-2})a.box(x,2,d+3,x,5,d+3,LOG);
        a.box(-2,1,d+4,2,1,d+4,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
    }
    private static void roof(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int base,boolean ridgeZ) {
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
    private static void kitchen(GensokyoArchitecture a,int x,int floor,int z) {
        a.block(x,floor+1,z,Blocks.FURNACE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.WEST));
        a.block(x,floor+1,z+3,Blocks.CAULDRON.getStateFromMeta(3));a.block(x-2,floor+1,z,Blocks.CRAFTING_TABLE.getDefaultState());
        a.box(x,floor+1,z+5,x,floor+2,z+6,DARK);
    }
    private static void light(GensokyoArchitecture a,int x,int floor,int z) {
        a.block(x,floor+5,z,Blocks.SEA_LANTERN.getDefaultState());
    }
    private static void washDeck(GensokyoArchitecture a) {
        a.box(-61,0,54,-44,0,61,WOOD);a.box(-61,1,54,-44,4,61,AIR);
        a.box(-62,5,53,-43,5,60,TOP_TILE);
        for(int x:new int[]{-61,-44})a.box(x,1,54,x,5,54,LOG);
        for(int x:new int[]{-57,-50})a.block(x,1,55,Blocks.CAULDRON.getStateFromMeta(3));
        a.box(-60,1,57,-60,2,59,DARK);a.box(-45,1,57,-45,2,59,DARK);
        a.box(-54,1,61,-48,1,61,Blocks.STONE_SLAB.getDefaultState());
        a.room("共用洗涤廊",-53,0,59);
    }
    private static void tree(GensokyoArchitecture a,int x,int z) {
        a.box(x,1,z,x,8,z,LOG);
        a.box(x-3,6,z,x+3,6,z,Blocks.LOG.getStateFromMeta(5));
        for(int dy=-2;dy<=2;dy++)for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++)
            if(dx*dx+dz*dz+dy*dy*3<=19 && (dx!=0 || dz!=0 || dy>0))a.block(x+dx,9+dy,z+dz,Blocks.LEAVES.getStateFromMeta(4));
    }
}
