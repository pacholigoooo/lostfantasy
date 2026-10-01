package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Three occupied storeys, fitted between overlapping hipped eaves. */
final class KasenHouse {
    private static final IBlockState TILE=Blocks.PRISMARINE.getStateFromMeta(2),
            POST=Blocks.LOG2.getStateFromMeta(0),GLASS=Blocks.STAINED_GLASS.getStateFromMeta(0);
    private KasenHouse() {}
    static GensokyoBlueprint create() {
        GensokyoBlueprint plan=new GensokyoBlueprint();GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.KASEN);
        KasenGarden.build(a);
        a.box(-19,0,-20,19,2,14,STONE);
        storey(a,-18,-19,18,13,3);storey(a,-15,-16,15,10,11);storey(a,-12,-13,12,7,19);
        roof(a,-22,-23,22,17,11,-15,-16,15,10,1);
        roof(a,-19,-20,19,14,19,-12,-13,12,7,1);
        roof(a,-16,-17,16,11,27,1,1,0,0,7);
        a.openZ(0,13,3,2,4);a.stairsSouth(0,14,0,3,3);
        wing(a,-33,-15,-18,4);wing(a,18,-15,33,4);
        a.openX(-18,0,3,1,4);a.openX(18,0,3,1,4);
        // Entrance hall opens into the reception room; side rooms retain a circulation lane.
        partitionZ(a,-17,17,4,3,0);partitionX(a,-7,-18,3,3,-4);partitionX(a,7,-18,3,3,-4);
        partitionX(a,-7,5,12,3,8);partitionX(a,7,5,12,3,8);
        partitionZ(a,-6,6,-4,3,0);partitionZ(a,8,17,-7,3,12);
        a.box(-4,4,6,4,4,10,Blocks.CARPET.getStateFromMeta(6));
        a.box(-5,4,-12,5,4,-7,Blocks.CARPET.getStateFromMeta(0));
        for(int x:new int[]{-5,3}) {VillageJoinery.lowDesk(a,x,3,-11,3);seat(a,x,4,-8,3);}
        a.box(-4,4,-17,4,5,-17,DARK);a.box(-3,6,-17,3,6,-17,Blocks.BOOKSHELF.getDefaultState());
        a.chest(-5,3,-16,"kasen_books");a.chest(5,3,-16,"kasen_books");
        VillageJoinery.lowDesk(a,10,3,-13,4);a.chest(16,3,-16,"kasen_supplies");
        a.box(9,4,-18,16,5,-18,Blocks.BOOKSHELF.getDefaultState());
        VillageJoinery.lowDesk(a,10,3,-2,4);
        a.box(-16,4,5,-11,4,5,DARK);a.box(11,4,5,16,4,5,DARK);
        a.block(-14,5,5,Blocks.FLOWER_POT.getDefaultState());a.block(14,5,5,Blocks.FLOWER_POT.getDefaultState());
        a.box(-16,4,9,-10,4,9,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(net.minecraft.block.BlockStairs.FACING,EnumFacing.NORTH));
        a.box(10,4,9,16,4,9,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(net.minecraft.block.BlockStairs.FACING,EnumFacing.NORTH));
        for(int x:new int[]{-10,0,10})hanging(a,x,10,6);
        hanging(a,0,10,-6);hanging(a,12,10,-7);
        kitchen(a);careRoom(a);
        // Reading room, archive stacks and one guest room on the middle floor.
        partitionX(a,4,-15,9,11,2);partitionZ(a,5,14,0,11,9);
        a.box(-13,12,-15,-4,15,-15,Blocks.BOOKSHELF.getDefaultState());
        for(int z:new int[]{-8,-3})a.box(-5,12,z,1,14,z,Blocks.BOOKSHELF.getDefaultState());
        a.box(-14,12,-3,-14,14,2,Blocks.BOOKSHELF.getDefaultState());
        VillageJoinery.lowDesk(a,-11,11,0,3);
        a.box(1,12,6,2,14,8,Blocks.BOOKSHELF.getDefaultState());
        a.chest(1,11,-14,"kasen_books");a.chest(-13,11,7,"kasen_books");
        VillageJoinery.lowDesk(a,-8,11,4,5);a.box(-7,12,7,-4,12,7,Blocks.CARPET.getStateFromMeta(6));
        a.bed(12,11,6);a.chest(6,11,7,"kasen_supplies");VillageJoinery.lowDesk(a,6,11,3,2);
        hanging(a,-1,18,5);hanging(a,10,18,5);hanging(a,-1,18,-10);
        // Private sleeping and meditation spaces upstairs, with a small study facing the garden.
        partitionX(a,-2,-12,6,19,2);partitionZ(a,-11,-3,-2,19,-7);
        partitionZ(a,-1,11,-2,19,3);
        a.bed(-8,19,-7);a.chest(-4,19,-11,"kasen_supplies");
        a.box(-11,20,-11,-11,23,-8,DARK);VillageJoinery.lowDesk(a,-9,19,2,4);
        a.box(1,20,-10,4,20,-7,Blocks.CARPET.getStateFromMeta(0));
        a.box(0,20,-12,5,20,-12,DARK);a.block(2,21,-12,ModBlocks.LIBRARY_LAMP.getDefaultState());
        a.box(0,20,5,6,22,5,Blocks.BOOKSHELF.getDefaultState());a.chest(10,19,4,"kasen_books");
        hanging(a,-7,26,1);hanging(a,4,26,0);
        stairs(a,-11,-12,3,11);stairs(a,9,-10,11,19);
        int roomIndex=0;
        for(int[] p:new int[][]{{0,3,9},{0,3,-7},{12,3,-5},{-25,3,0},{-27,3,-10},{26,3,-3},
                {-1,11,3},{-8,11,-6},{9,11,3},{-7,19,4},{-7,19,-5},{3,19,-4}})
            a.room(new String[]{"玄关","会客","茶器收存","茶饭","厨房","照料器具","阅书","藏书","客间","书斋","寝间","冥想"}[roomIndex++],p[0],p[1],p[2]);
        plan.seal();return plan;
    }
    private static void storey(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int f) {
        a.box(x1,f,z1,x2,f+8,z2,WHITE);a.box(x1+1,f+1,z1+1,x2-1,f+7,z2-1,AIR);
        a.box(x1,f,z1,x2,f,z2,WOOD);a.box(x1,f+8,z1,x2,f+8,z2,WOOD);
        for(int x:new int[]{x1,x2})for(int z:new int[]{z1,z2})a.box(x,f+1,z,x,f+7,z,POST);
        for(int z:new int[]{z1,z2})for(int x:new int[]{x1+6,x2-6})roundWindow(a,x,f+4,z,false);
        for(int x:new int[]{x1,x2})for(int z:new int[]{z1+6,z2-6})roundWindow(a,x,f+4,z,true);
        for(int z:new int[]{z1,z2})a.box(x1,f+7,z,x2,f+7,z,DARK);
        for(int x:new int[]{x1,x2})a.box(x,f+7,z1,x,f+7,z2,DARK);
    }
    private static void roundWindow(GensokyoArchitecture a,int x,int y,int z,boolean side) {
        for(int u=-2;u<=2;u++)for(int v=-2;v<=2;v++) {
            int d=u*u+v*v;if(d>5)continue;
            IBlockState s=d>2?DARK:GLASS;a.block(x+(side?0:u),y+v,z+(side?u:0),s);
        }
    }
    private static void roof(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int base,int hx1,int hz1,int hx2,int hz2,int riseLimit) {
        for(int x=x1;x<=x2;x++)for(int z=z1;z<=z2;z++) {
            if(x>=hx1 && x<=hx2 && z>=hz1 && z<=hz2)continue;
            int ix=Math.min(x-x1,x2-x),iz=Math.min(z-z1,z2-z);
            int y=base+Math.min(riseLimit,Math.min(ix,iz)/2)+(ix<2 && iz<2?1:0);
            a.box(x,base,z,x,y,z,TILE);
            if(ix==0 || iz==0)a.block(x,y-1,z,DARK);
        }
        if(hx1>hx2) {
            int z=(z1+z2)/2;
            for(int x=-6;x<=6;x++) {
                int top=base+Math.min(Math.min(x-x1,x2-x),Math.min(z-z1,z2-z))/2;
                a.box(x,top,z,x,base+(Math.abs(x)==6?9:8),z,TILE);
            }
        }
    }
    private static void wing(GensokyoArchitecture a,int x1,int z1,int x2,int z2) {
        a.box(x1,0,z1,x2,2,z2,STONE);storey(a,x1,z1,x2,z2,3);
        // The wing's eaves stop at the main building, leaving its windows clear.
        roof(a,x1-3,z1-3,x2+3,z2+3,11,-18,-19,18,13,3);
    }
    private static void partitionX(GensokyoArchitecture a,int x,int z1,int z2,int f,int door) {
        a.box(x,f+1,z1,x,f+7,z2,WHITE);a.openX(x,door,f,1,4);
    }
    private static void partitionZ(GensokyoArchitecture a,int x1,int x2,int z,int f,int door) {
        a.box(x1,f+1,z,x2,f+7,z,WHITE);a.openZ(door,z,f,1,4);
    }
    private static void stairs(GensokyoArchitecture a,int x,int z,int f,int top) {
        a.box(x-1,f+1,z,x+1,top+3,z+top-f-1,AIR);
        a.stairsSouth(x,z,f,top,1);
        for(int side:new int[]{x-2,x+2})a.box(side,top+1,z,side,top+1,z+top-f-1,Blocks.SPRUCE_FENCE.getDefaultState());
    }
    private static void hanging(GensokyoArchitecture a,int x,int y,int z) {a.block(x,y,z,ModBlocks.RED_LANTERN.getDefaultState());}
    private static void seat(GensokyoArchitecture a,int x,int y,int z,int n) {a.box(x,y,z,x+n-1,y,z,Blocks.CARPET.getStateFromMeta(6));}
    private static void kitchen(GensokyoArchitecture a) {
        partitionZ(a,-32,-19,-5,3,-25);
        a.box(-31,4,-14,-27,4,-14,STONE);a.block(-31,4,-13,Blocks.FURNACE.getDefaultState());
        a.block(-29,4,-13,Blocks.CRAFTING_TABLE.getDefaultState());a.block(-27,4,-13,Blocks.CAULDRON.getStateFromMeta(3));
        a.chest(-20,3,-13,"kasen_supplies");a.chest(-20,3,-8,"village_pantry");
        a.box(-31,6,-14,-20,6,-14,Blocks.WOODEN_SLAB.getStateFromMeta(1));
        a.box(-31,4,-8,-29,5,-8,Blocks.HAY_BLOCK.getDefaultState());
        VillageJoinery.lowDesk(a,-30,3,0,4);seat(a,-30,4,2,4);hanging(a,-25,10,-9);hanging(a,-25,10,0);
    }
    private static void careRoom(GensokyoArchitecture a) {
        partitionZ(a,19,32,-6,3,26);
        a.box(20,4,-14,23,5,-14,DARK);a.chest(25,3,-13,"kasen_supplies");
        VillageJoinery.lowDesk(a,20,3,-10,3);
        a.block(30,4,-12,Blocks.CRAFTING_TABLE.getDefaultState());a.block(30,4,-9,Blocks.CAULDRON.getStateFromMeta(3));
        a.box(30,4,-3,31,4,1,Blocks.HAY_BLOCK.getDefaultState());
        a.box(20,4,2,24,4,2,WOOD);a.box(20,5,2,24,5,2,Blocks.WOODEN_SLAB.getStateFromMeta(1));
        a.chest(20,3,-4,"kasen_supplies");hanging(a,26,10,-10);hanging(a,26,10,0);
    }
}
