package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.world.BarrierStudy;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A complete old timber residence; the room plan adapts the attested household objects. */
final class BoundaryHouse {
    private BoundaryHouse() {}
    static GensokyoBlueprint create() {
        GensokyoBlueprint plan=new GensokyoBlueprint();GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.YAKUMO);
        garden(a);
        // A three-block middle corridor serves compact rooms; two side wings hold books and meals.
        VillageJoinery.house(a,-17,-18,17,9,2,1);
        VillageJoinery.house(a,-29,-16,-17,5,2,1);
        VillageJoinery.house(a,17,-8,29,9,2,1);
        a.openX(-17,1,2,1,3);a.openX(17,5,2,1,3);a.openZ(0,9,2,1,3);
        a.box(-18,0,10,18,1,13,STONE);a.box(-18,2,10,18,2,13,WOOD);
        a.box(-18,7,10,18,7,13,WOOD);a.gable(-21,9,21,16,8);
        for(int x:new int[]{-17,-9,9,17})a.box(x,3,13,x,6,13,LOG);
        a.stairsSouth(0,14,0,2,2);
        for(int x:new int[]{-2,2}) {
            a.box(x,3,-17,x,6,8,WHITE);
            for(int z:new int[]{-12,-3,6})a.openX(x,z,2,1,3);
        }
        for(int[] side:new int[][]{{-16,-3,-9},{3,16,9}}) {
            VillageJoinery.wallZ(a,side[0],side[1],-8,2,side[2]);
            VillageJoinery.wallZ(a,side[0],side[1],2,2,side[2]);
        }
        bedroom(a,false);bedroom(a,true);
        sitting(a);calculation(a);frontRooms(a);books(a);kitchen(a);
        for(int[] bounds:new int[][]{{-15,-16,-4,-10},{4,-16,15,-10},{-15,-6,-4,0},{4,-6,14,0},{4,4,13,7}})
            InteriorFinishes.tatami(a,bounds[0],bounds[1],bounds[2],bounds[3],2);
        for(int[] p:new int[][]{{0,-12},{0,-3},{0,6},{-9,-12},{9,-12},{-9,-3},{9,-3},{-9,6},{9,6},{-23,-10},{-23,1},{23,-4},{23,5}})
            VillageJoinery.lantern(a,p[0],2,p[1]);
        String[] names={"玄关","内廊","紫的寝间","蓝的寝间","起居与旧电视","演算书案","书刊整理","会客茶席","外界器具","藏书","厨房","餐间","缘侧"};
        int[][] rooms={{0,2,6},{0,2,-12},{-9,2,-11},{9,2,-11},{-9,2,-1},{9,2,-1},{-9,2,7},{9,2,7},{-23,2,1},{-23,2,-7},{23,2,-2},{23,2,6},{0,2,12}};
        for(int i=0;i<rooms.length;i++)a.room(names[i],rooms[i][0],rooms[i][1],rooms[i][2]);
        plan.seal();return plan;
    }
    private static void bedroom(GensokyoArchitecture a,boolean ran) {
        int x1=ran?3:-16,x2=ran?16:-3;
        a.box(x1+1,3,-16,x2-1,3,-9,Blocks.CARPET.getStateFromMeta(ran?0:10));
        a.bed(ran?14:-14,2,-13);a.chest(ran?4:-4,2,-16,"boundary_household");
        a.box(ran?16:-16,3,-11,ran?16:-16,5,-9,DARK);
        VillageJoinery.lowDesk(a,ran?6:-9,2,-15,3);
        a.block(ran?7:-8,4,-15,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.box(x1+4,3,-17,x1+6,3,-17,WOOD);a.block(x1+5,4,-17,Blocks.FLOWER_POT.getDefaultState());
    }
    private static void sitting(GensokyoArchitecture a) {
        a.box(-15,3,-6,-4,3,0,Blocks.CARPET.getStateFromMeta(4));
        a.box(-15,3,-7,-13,3,-7,DARK);
        a.block(-14,4,-7,ModBlocks.OUTSIDE_TELEVISION.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
        VillageJoinery.lowDesk(a,-12,2,-4,4);
        a.box(-16,3,-3,-16,3,0,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(net.minecraft.block.BlockStairs.FACING,EnumFacing.EAST));
        a.box(-6,3,-7,-3,4,-7,Blocks.BOOKSHELF.getDefaultState());a.chest(-4,2,1,"boundary_books");
    }
    private static void calculation(GensokyoArchitecture a) {
        a.box(3,3,-7,15,5,-7,Blocks.BOOKSHELF.getDefaultState());
        VillageJoinery.lowDesk(a,6,2,-4,5);a.block(8,4,-4,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.chest(15,2,-3,"boundary_books");a.chest(15,2,1,"boundary_household");
        a.box(3,3,1,5,3,1,DARK);a.block(4,4,1,Blocks.FLOWER_POT.getDefaultState());
    }
    private static void frontRooms(GensokyoArchitecture a) {
        a.box(-16,3,3,-16,5,8,Blocks.BOOKSHELF.getDefaultState());
        VillageJoinery.lowDesk(a,-13,2,4,3);a.block(-12,4,4,ModBlocks.RESEARCH_NOTES.getDefaultState());a.chest(-4,2,8,"boundary_books");
        VillageJoinery.lowDesk(a,7,2,4,4);a.box(7,3,8,10,3,8,Blocks.CARPET.getStateFromMeta(10));
        a.box(15,3,3,16,3,7,DARK);a.block(16,4,5,ModBlocks.MEDICINE_TRAY.getDefaultState());
        a.openZ(-9,9,2,1,3);a.openZ(9,9,2,1,3);
    }
    private static void books(GensokyoArchitecture a) {
        VillageJoinery.wallZ(a,-28,-18,-5,2,-23);
        a.box(-28,3,-15,-18,5,-15,Blocks.BOOKSHELF.getDefaultState());
        a.box(-28,3,-13,-28,5,-7,Blocks.BOOKSHELF.getDefaultState());
        a.box(-19,3,-12,-18,4,-11,Blocks.BOOKSHELF.getDefaultState());
        VillageJoinery.lowDesk(a,-26,2,-10,3);
        a.box(-26,3,-10,-24,3,-10,WOOD);
        a.block(BarrierStudy.LOCAL.getX(),BarrierStudy.LOCAL.getY(),BarrierStudy.LOCAL.getZ(),ModBlocks.BARRIER_STUDY.getDefaultState());
        a.chest(-18,2,-7,"boundary_collection");
        a.box(-28,3,-3,-25,3,-3,DARK);
        a.block(-27,4,-3,Blocks.OBSERVER.getDefaultState().withProperty(net.minecraft.block.BlockObserver.FACING,EnumFacing.SOUTH));
        a.block(-25,4,-3,Blocks.DISPENSER.getDefaultState().withProperty(net.minecraft.block.BlockDispenser.FACING,EnumFacing.SOUTH));
        a.box(-28,3,4,-25,3,4,DARK);a.block(-27,4,4,ModBlocks.GRAMOPHONE.getDefaultState());
        a.chest(-18,2,4,"boundary_household");
    }
    private static void kitchen(GensokyoArchitecture a) {
        VillageJoinery.wallZ(a,18,28,0,2,23);
        a.box(18,3,-7,28,3,-7,STONE);
        a.block(19,3,-6,Blocks.FURNACE.getDefaultState());a.block(22,3,-6,Blocks.CRAFTING_TABLE.getDefaultState());
        a.block(25,3,-6,Blocks.CAULDRON.getStateFromMeta(3));
        a.block(28,3,-5,DARK);a.block(28,4,-5,ModBlocks.MEDICINE_TRAY.getDefaultState());
        a.chest(18,2,-2,"village_pantry");a.chest(28,2,-2,"boundary_household");
        VillageJoinery.lowDesk(a,21,2,3,5);a.box(21,3,7,25,3,7,Blocks.CARPET.getStateFromMeta(4));
        a.chest(28,2,7,"village_pantry");
        a.openZ(23,9,2,1,3);a.stairsSouth(23,10,0,2,2);
    }
    private static void garden(GensokyoArchitecture a) {
        // A complete, sheltered residence, without a ruin layer or a frozen-daylight effect.
        a.box(-55,-2,-40,55,0,73,Blocks.GRASS.getDefaultState());
        for(int x:new int[]{-54,54}) {a.box(x,1,-40,x,3,72,WHITE);a.box(x,4,-40,x,4,72,SLAB);}
        for(int z:new int[]{-40,72}) {a.box(-54,1,z,54,3,z,WHITE);a.box(-54,4,z,54,4,z,SLAB);}
        a.box(-3,1,72,3,4,72,AIR);a.box(-3,0,14,3,0,80,Blocks.GRAVEL.getDefaultState());
        a.box(-44,0,19,43,0,22,Blocks.GRAVEL.getDefaultState());
        a.box(21,0,12,25,0,22,Blocks.GRAVEL.getDefaultState());
        VillageJoinery.pond(a,-27,40,14,10);
        // A low bridge gives the pond a crossing and a place to stop by the water.
        a.box(-29,1,28,-25,1,52,WOOD);
        for(int z:new int[]{29,35,44,51})for(int x:new int[]{-29,-25})a.box(x,-2,z,x,0,z,LOG);
        for(int x:new int[]{-30,-24})a.box(x,2,32,x,2,48,Blocks.SPRUCE_FENCE.getDefaultState());
        a.box(-29,0,27,-25,0,27,STONE);a.box(-29,0,53,-25,0,53,STONE);
        for(int x=-37;x<=-17;x+=5)for(int z=35;z<=45;z+=5)
            if(Math.abs(x+27)>3 && (x+27)*(x+27)/196.0+(z-40)*(z-40)/100.0<.7)a.block(x,1,z,Blocks.WATERLILY.getDefaultState());
        for(int[] p:new int[][]{{-46,15,13,6},{43,43,16,8},{-42,61,13,6},{34,-33,11,5},{-35,-33,11,5}})VillageJoinery.maple(a,p[0],p[1],p[2],p[3]);
        for(int x=18;x<=31;x++)for(int z=34;z<=44;z++) {
            if((x-24)*(x-24)/49.0+(z-39)*(z-39)/25.0>1)continue;
            a.block(x,0,z,Blocks.SAND.getDefaultState());
            if(Math.floorMod(x*11+z*7,23)==0)a.box(x,1,z,x,2,z,Blocks.STONE.getDefaultState());
        }
        for(int[] p:new int[][]{{-9,24},{9,24},{-11,54},{11,54},{-43,29},{39,20}})a.lamp(p[0],1,p[1]);
        BoundaryEntrance.gate(a);
        GardenScenery.arbour(a,25,57,7,4);
        a.box(4,0,57,19,0,60,Blocks.GRAVEL.getDefaultState());
        GardenScenery.rocks(a,43,62,-1);
        GardenScenery.bench(a,-40,0,24,9,EnumFacing.NORTH);
        a.room("东庭茶棚",25,0,57);
        a.room("池边桥",-27,1,40);a.room("砂庭",24,0,49);a.room("院门",0,0,61);a.room("前庭",0,0,35);
    }
}
