package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStairs;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Lake-facing worship court, three residents' rooms and a working shrine storehouse. */
final class MoriyaShrine {
    private static final int FLOOR=4;
    private MoriyaShrine() {}
    static void build(GensokyoBlueprint plan) {
        // Local south faces east, towards the lake. The mountain path enters from the world south.
        GensokyoArchitecture shore=new GensokyoArchitecture(plan,GensokyoAtlas.MORIYA,Rotation.COUNTERCLOCKWISE_90);
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.MORIYA,0,0,GensokyoAtlas.MORIYA.title,
                Rotation.COUNTERCLOCKWISE_90,MoriyaRidge.HEIGHT);
        MoriyaRidge.build(shore);
        grounds(a);sanctuary(a);living(a);storehouse(a);office(a);kitchen(a);purification(a);roofs(a);
        MoriyaRidge.approaches(shore);torii(shore,128);lakefront(shore);
        gardenDetails(a);
    }
    private static void gardenDetails(GensokyoArchitecture a) {
        GardenScenery.arbour(a,-67,39,6,4);
        a.box(-65,0,45,-53,0,49,Blocks.GRAVEL.getDefaultState());
        GardenScenery.plantedBed(a,72,56,5,4,3);
        GardenScenery.plantedBed(a,-54,70,8,4,0);
        for(int z:new int[]{-62,-40,-18})InteriorFinishes.tatami(a,59,z,74,z+14,FLOOR);
        InteriorFinishes.tatami(a,-27,-27,27,-11,FLOOR);
        a.block(-66,6,19,ModBlocks.LACQUER_BOWL.getDefaultState());
        a.block(-62,6,19,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.room("山风休憩庭",-64,0,40);
    }
    private static void grounds(GensokyoArchitecture a) {
        a.box(-82,0,-81,82,0,81,Blocks.GRASS.getDefaultState());
        a.box(-81,1,-81,81,2,-81,STONE);a.box(-82,1,-80,-82,2,81,STONE);a.box(82,1,-80,82,2,81,STONE);
        a.box(-82,0,51,82,0,67,Blocks.GRAVEL.getDefaultState());a.box(-92,0,53,-78,0,61,STONE);
        a.box(-92,0,-4,-86,0,61,STONE);a.box(-92,1,-4,-86,3,61,AIR);
        a.box(-13,0,-3,13,0,87,STONE);a.box(-40,0,3,40,0,24,Blocks.GRAVEL.getDefaultState());
        a.box(-82,1,53,-82,2,61,AIR);
        for(int x:new int[]{-35,35})for(int z:new int[]{26,65})pillar(a,x,z,z==26?24:20);
        for(int z:new int[]{24,46,76})for(int x:new int[]{-17,17})a.lamp(x,1,z);
        torii(a,44);
        for(int x:new int[]{-69,66})for(int z:new int[]{-74,71})VillageJoinery.maple(a,x,z,11,6);
        for(int[] p:new int[][]{{-73,34},{68,62},{-72,70}}) {
            a.box(p[0]-2,0,p[1]-2,p[0]+2,0,p[1]+2,Blocks.DIRT.getStateFromMeta(1));
            a.box(p[0]-1,1,p[1]-1,p[0]+1,2,p[1]+1,Blocks.MOSSY_COBBLESTONE.getDefaultState());
            a.block(p[0]-2,1,p[1]+2,Blocks.TALLGRASS.getStateFromMeta(2));
        }
        a.room("御柱参道",0,0,60);a.room("山道入口",-78,0,57);
    }
    private static void sanctuary(GensokyoArchitecture a) {
        a.hall(-32,-37,32,-7,FLOOR,9,LOG);
        a.box(-31,13,-36,31,13,-8,WOOD);
        a.box(-35,0,-6,35,3,4,STONE);a.box(-35,4,-6,35,4,4,WOOD);
        for(int x:new int[]{-28,-18,18,28})a.box(x,5,3,x,12,3,LOG);
        for(int x:new int[]{-20,0,20})a.openZ(x,-7,FLOOR,3,5);
        a.stairsSouth(0,5,0,FLOOR,9);
        for(int x:new int[]{-34,34})a.box(x,5,-5,x,5,4,Blocks.SPRUCE_FENCE.getDefaultState());
        a.box(-34,5,4,-10,5,4,Blocks.SPRUCE_FENCE.getDefaultState());a.box(10,5,4,34,5,4,Blocks.SPRUCE_FENCE.getDefaultState());
        a.block(0,5,1,ModBlocks.SAISEN_BOX.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
        a.box(-12,5,-31,12,5,-28,DARK);a.box(-7,6,-33,7,6,-30,WOOD);
        for(int x:new int[]{-10,10})a.block(x,6,-29,ModBlocks.LIBRARY_LAMP.getDefaultState());
        for(int x:new int[]{-22,-12,12,22})for(int z:new int[]{-22,-14})a.box(x-2,5,z,x+2,5,z+1,Blocks.CARPET.getStateFromMeta(13));
        a.chest(29,FLOOR,-34,"school_supplies");a.chest(-29,FLOOR,-34,"school_supplies");
        a.box(-3,11,-6,3,12,-6,DARK);a.sign(0,11,-5,EnumFacing.SOUTH,"守矢神社","");
        rope(a,-28,28,10,3);
        for(int x:new int[]{-19,19})for(int z:new int[]{-27,-13}) {
            a.box(x,11,z,x,12,z,Blocks.OAK_FENCE.getDefaultState());a.block(x,10,z,ModBlocks.RED_LANTERN.getDefaultState());
        }
        a.openZ(0,-37,FLOOR,2,5);corridor(a,-5,-45,5,-37);
        a.hall(-19,-68,19,-45,FLOOR,8,LOG);a.openZ(0,-45,FLOOR,2,4);
        a.box(-18,12,-67,18,12,-46,WOOD);
        a.box(-8,5,-63,8,6,-59,DARK);a.box(-6,7,-65,6,8,-62,WOOD);
        a.block(0,9,-63,Blocks.GOLD_BLOCK.getDefaultState());
        for(int x:new int[]{-12,12})a.block(x,5,-61,ModBlocks.LIBRARY_LAMP.getDefaultState());
        a.chest(-16,FLOOR,-65,"school_supplies");a.chest(16,FLOOR,-65,"school_supplies");
        a.box(-2,5,-53,2,5,-52,WOOD);a.block(0,6,-52,Blocks.FLOWER_POT.getDefaultState());
        a.room("拜殿",0,FLOOR,-17);a.room("祭仪准备",25,FLOOR,-29);a.room("本殿",0,FLOOR,-56);
        a.box(23,5,-32,29,5,-31,WOOD);
        a.block(24,6,-31,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.block(27,6,-31,ModBlocks.LACQUER_BOWL.getDefaultState());
        a.block(30,5,-28,Blocks.CRAFTING_TABLE.getDefaultState());
        for(int x:new int[]{-32,32})a.openX(x,-23,FLOOR,2,4);
        corridor(a,-47,-26,-32,-20);corridor(a,32,-26,46,-20);
    }
    private static void living(GensokyoArchitecture a) {
        VillageJoinery.house(a,46,-67,77,6,FLOOR,1);a.openX(46,-23,FLOOR,2,3);
        VillageJoinery.wallX(a,56,-66,5,FLOOR,-55);a.openX(56,-33,FLOOR,1,3);a.openX(56,-8,FLOOR,1,3);
        for(int z:new int[]{-45,-22})a.box(57,5,z,76,8,z,WHITE);
        // Each resident has a private sleeping space, storage and a writing or tea setting.
        a.bed(71,FLOOR,-57);a.chest(74,FLOOR,-64,"village_pantry");VillageJoinery.teaTable(a,60,FLOOR,-51,7);
        a.box(60,5,-65,67,5,-64,DARK);a.block(63,6,-64,ModBlocks.ARMILLARY.getDefaultState());
        a.bed(72,FLOOR,-35);a.chest(74,FLOOR,-42,"village_pantry");VillageJoinery.teaTable(a,59,FLOOR,-29,6);
        a.box(59,5,-42,63,5,-40,Blocks.CARPET.getStateFromMeta(5));
        a.bed(72,FLOOR,-13);a.chest(74,FLOOR,3,"school_supplies");VillageJoinery.lowDesk(a,59,FLOOR,-5,6);
        a.box(76,5,-7,76,7,1,Blocks.BOOKSHELF.getDefaultState());a.box(67,5,3,70,5,4,WOOD);
        a.block(69,6,4,ModBlocks.OUTSIDE_TELEVISION.getDefaultState());
        for(int z:new int[]{-55,-33,-8})VillageJoinery.lantern(a,65,FLOOR,z);
        a.room("神奈子起居",67,FLOOR,-55);a.room("诹访子起居",66,FLOOR,-34);a.room("早苗起居",66,FLOOR,-12);
        a.room("生活侧廊",51,FLOOR,-36);
        for(int z:new int[]{-60,-38,-16})VillageJoinery.cabinet(a,75,FLOOR,z,EnumFacing.WEST);
        a.openZ(51,6,FLOOR,1,3);corridor(a,48,6,54,20);
    }
    private static void storehouse(GensokyoArchitecture a) {
        VillageJoinery.house(a,-78,-67,-47,-29,FLOOR,2);
        a.openZ(-53,-29,FLOOR,1,3);corridor(a,-56,-29,-50,-17);
        VillageJoinery.wallX(a,-59,-66,-30,FLOOR,-52);
        for(int x:new int[]{-74,-66})for(int z:new int[]{-62,-53,-43}) {
            a.box(x-1,5,z-1,x+2,5,z+2,DARK);
            a.box(x-1,6,z-1,x+2,7,z+2,Blocks.GLASS.getDefaultState());
            a.block(x,6,z,(z==-53?ModBlocks.OUTSIDE_TELEVISION:z==-43?Blocks.NOTEBLOCK:ModBlocks.ARMILLARY).getDefaultState());
        }
        // Cases sit in the main room; a clear eastern strip carries the folded storey access.
        a.stairsSouth(-53,-40,FLOOR,9,2);a.box(-55,9,-39,-51,11,-36,AIR);
        a.box(-55,10,-44,-51,11,-40,AIR);
        for(int x:new int[]{-74,-67,-60})for(int z:new int[]{-63,-54})a.chest(x,9,z,"kourindou_tools");
        a.box(-76,10,-37,-62,11,-36,Blocks.HAY_BLOCK.getDefaultState());a.block(-74,10,-32,Blocks.CRAFTING_TABLE.getDefaultState());
        VillageJoinery.lantern(a,-64,FLOOR,-34);VillageJoinery.lantern(a,-64,9,-47);
        a.room("宝物库陈列",-69,FLOOR,-34);a.room("库房内廊",-53,FLOOR,-55);a.room("楼上器材库",-62,9,-46);
    }
    private static void office(GensokyoArchitecture a) {
        VillageJoinery.house(a,-77,-17,-47,25,FLOOR,1);
        a.openZ(-53,-17,FLOOR,1,3);a.openZ(-53,25,FLOOR,2,3);a.openX(-47,-23,FLOOR,2,3);
        // The hall connector enters through an open side porch before reaching the office.
        a.box(-48,4,-26,-43,4,-11,WOOD);a.openX(-47,-12,FLOOR,1,3);
        VillageJoinery.wallZ(a,-76,-48,1,FLOOR,-53);
        a.box(-73,5,19,-60,5,20,DARK);a.box(-73,6,19,-60,6,19,WOOD);
        a.chest(-74,FLOOR,4,"school_supplies");a.chest(-69,FLOOR,4,"bookbinding");
        a.box(-75,5,9,-75,7,16,Blocks.BOOKSHELF.getDefaultState());
        VillageJoinery.lowDesk(a,-73,FLOOR,-10,9);a.chest(-74,FLOOR,-14,"school_supplies");
        a.block(-61,5,-14,Blocks.CRAFTING_TABLE.getDefaultState());a.block(-58,5,-14,Blocks.CAULDRON.getStateFromMeta(3));
        a.box(-80,4,26,-44,4,31,WOOD);a.stairsSouth(-53,32,0,FLOOR,4);
        a.box(-57,0,36,-49,0,56,Blocks.GRAVEL.getDefaultState());
        VillageJoinery.lantern(a,-62,FLOOR,12);VillageJoinery.lantern(a,-62,FLOOR,-8);
        a.room("授予所",-64,FLOOR,12);a.room("神札与祭具准备",-62,FLOOR,-5);
    }
    private static void kitchen(GensokyoArchitecture a) {
        VillageJoinery.house(a,46,20,77,46,FLOOR,1);a.openZ(51,20,FLOOR,1,3);a.openZ(51,46,FLOOR,2,3);
        VillageJoinery.wallX(a,63,21,45,FLOOR,34);
        VillageJoinery.lowDesk(a,49,FLOOR,30,10);a.chest(48,FLOOR,23,"village_pantry");
        a.box(66,5,22,75,5,23,WOOD);a.block(69,6,22,Blocks.CAULDRON.getStateFromMeta(3));
        a.block(75,5,28,Blocks.FURNACE.getDefaultState());a.block(75,5,32,Blocks.FURNACE.getDefaultState());
        a.chest(67,FLOOR,42,"village_pantry");a.chest(72,FLOOR,42,"village_pantry");
        a.box(45,4,47,78,4,50,WOOD);a.stairsSouth(51,51,0,FLOOR,3);
        VillageJoinery.lantern(a,55,FLOOR,37);VillageJoinery.lantern(a,69,FLOOR,35);
        a.room("日常茶饭",57,FLOOR,37);a.room("厨房",69,FLOOR,36);
    }
    private static void purification(GensokyoArchitecture a) {
        for(int x:new int[]{-31,-19})for(int z:new int[]{7,17})a.box(x,1,z,x,6,z,LOG);
        ShrineRoofs.gable(a,-33,5,-17,19,7,false,true);
        a.box(-28,1,10,-22,2,14,STONE);a.box(-27,2,11,-23,2,13,Blocks.WATER.getDefaultState());
        a.box(-28,3,9,-22,3,9,Blocks.OAK_FENCE.getDefaultState());a.room("手水舍",-24,0,17);
    }
    private static void lakefront(GensokyoArchitecture a) {
        // Ordinary water, a short shore pier and anchored pillars share the vanilla lighting path.
        for(int z=133;z<=174;z++) {
            int floor=-Math.min(4,(z-131)/10);
            a.box(-7,floor+1,z,7,12,z,AIR);
            a.box(-7,floor,z,7,floor,z,WOOD);
            if(z>=141 && (z-141)%10==0)
                a.box(-6,floor,z,6,floor,z,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
            for(int x:new int[]{-7,7}) {
                a.block(x,floor+1,z,Blocks.SPRUCE_FENCE.getDefaultState());
                if(z%10==3)a.box(x,-26,z,x,floor-1,z,LOG);
            }
        }
        a.box(-8,-4,174,8,-4,206,WOOD);
        for(int x:new int[]{-8,8}) {
            a.box(x,-3,180,x,-3,206,Blocks.SPRUCE_FENCE.getDefaultState());
            for(int z:new int[]{180,202})a.box(x,-26,z,x,-5,z,LOG);
        }
        a.box(-8,-3,206,8,-3,206,Blocks.SPRUCE_FENCE.getDefaultState());
        a.box(-6,-3,201,-3,-3,202,WOOD);a.box(3,-3,201,6,-3,202,WOOD);
        for(int x:new int[]{-49,45})for(int z:new int[]{215,266}) {
            a.box(x-1,-30,z-1,x+1,18,z+1,LOG);a.box(x,19,z,x,21,z,LOG);
            for(int zz:new int[]{z-2,z+2})a.box(x-1,12,zz,x+1,12,zz,ModBlocks.SHRINE_ROPE.getDefaultState());
            for(int xx:new int[]{x-2,x+2})a.box(xx,12,z-1,xx,12,z+1,ModBlocks.SHRINE_ROPE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.EAST));
            a.block(x,11,z+2,ModBlocks.SHIDE.getDefaultState());
        }
        a.room("湖畔栈台",0,-4,195);
    }
    private static void corridor(GensokyoArchitecture a,int x1,int z1,int x2,int z2) {
        a.box(x1,0,z1,x2,3,z2,STONE);a.box(x1,4,z1,x2,4,z2,WOOD);
        for(int x=x1+2;x<=x2-2;x+=7)for(int z:new int[]{z1,z2})a.box(x,5,z,x,8,z,LOG);
        ShrineRoofs.canopy(a,x1,z1,x2,z2,9,true);
    }
    private static void pillar(GensokyoArchitecture a,int x,int z,int height) {
        a.box(x-2,0,z-2,x+2,1,z+2,STONE);a.box(x-1,2,z-1,x+1,height,z+1,LOG);
        a.box(x-1,height+1,z,x+1,height+2,z,LOG);a.box(x,height+3,z,x,height+3,z,LOG);
        for(int n:new int[]{5,height-4}) {
            for(int zz:new int[]{z-2,z+2})a.box(x-1,n,zz,x+1,n,zz,ModBlocks.SHRINE_ROPE.getDefaultState());
            for(int xx:new int[]{x-2,x+2})a.box(xx,n,z-1,xx,n,z+1,ModBlocks.SHRINE_ROPE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.EAST));
        }
        a.block(x,4,z+2,ModBlocks.SHIDE.getDefaultState());
    }
    private static void rope(GensokyoArchitecture a,int left,int right,int y,int z) {
        a.box(left,y,z,right,y,z,ModBlocks.SHRINE_ROPE.getDefaultState());
        for(int x:new int[]{-18,-9,0,9,18})a.block(x,y-1,z,ModBlocks.SHIDE.getDefaultState());
    }
    private static void torii(GensokyoArchitecture a,int z) {
        for(int x:new int[]{-11,11}) {a.box(x-1,0,z-1,x+1,2,z+1,STONE);a.box(x,3,z,x,13,z,LOG);}
        a.box(-16,14,z-1,16,14,z+1,DARK);
        a.box(-14,13,z,14,13,z,Blocks.LOG.getStateFromMeta(5));a.box(-12,10,z,12,10,z,Blocks.LOG.getStateFromMeta(5));
        a.box(-16,15,z, -13,15,z,DARK);a.box(13,15,z,16,15,z,DARK);
    }
    private static void roofs(GensokyoArchitecture a) {
        int[][] volumes={{-38,-43,38,-1,14,1,1},{-22,-71,22,-42,13,1,1},
                {43,-70,80,9,10,1,0},{-81,-70,-44,-26,15,1,0},
                {-80,-20,-44,28,10,1,0},{43,17,80,49,10,0,0}};
        // Clear the old roof volumes together before joining the new overlapping eaves.
        for(int[] r:volumes)ShrineRoofs.clearGable(a,r[0],r[1],r[2],r[3],r[4]);
        for(int[] r:volumes)ShrineRoofs.gable(a,r[0],r[1],r[2],r[3],r[4],r[5]!=0,r[6]!=0);
        ShrineRoofs.canopy(a,-38,-6,38,6,13,true);
    }
}
