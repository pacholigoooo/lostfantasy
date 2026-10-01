package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStairs;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A low, connected estate: clinic to the west, private rooms north, domestic rooms east. */
final class Eientei {
    private static final int FLOOR=3;
    private Eientei() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.EIENTEI);
        grounds(a);reception(a);clinic(a);privateRooms(a);eastWing(a);bath(a);passages(a);garden(a);herbCourt(a);
        interiorDetails(a);
    }
    private static void interiorDetails(GensokyoArchitecture a) {
        InteriorFinishes.tatami(a,-37,-57,-11,-51,FLOOR);
        InteriorFinishes.tatami(a,-7,-57,22,-41,FLOOR);
        for(int z:new int[]{-51,-31,-11})InteriorFinishes.tatami(a,72,z,86,z+12,FLOOR);
        // The clinic uses washable stone strips, separate from the residential mat floors.
        a.box(-80,FLOOR,17,-65,FLOOR,29,Blocks.STONE.getStateFromMeta(6));
        a.box(-61,FLOOR,17,-45,FLOOR,29,Blocks.STONE.getStateFromMeta(6));
        for(int x:new int[]{-76,-55}) {
            a.box(x-2,4,18,x-2,6,22,PAPER);
            a.block(x+2,4,22,ModBlocks.MEDICINE_TRAY.getDefaultState());
            a.box(x-3,4,28,x+1,4,28,Blocks.SPRUCE_STAIRS.getDefaultState()
                    .withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
        }
        for(int z:new int[]{-48,-28,-8})a.block(85,5,z,ModBlocks.LACQUER_BOWL.getDefaultState());
        // Raised trays have solid shelves below; spare the bedside approach and door axes.
        for(int z:new int[]{-48,-28,-8})a.block(85,4,z,WOOD);
        GardenScenery.bench(a,30,3,-27,8,EnumFacing.NORTH);
        GardenScenery.plantedBed(a,15,68,9,5,3);
        for(int x:new int[]{-80,-57})a.box(x,6,-73,x+5,6,-71,Blocks.WOODEN_SLAB.getStateFromMeta(1));
    }
    private static void grounds(GensokyoArchitecture a) {
        a.box(-94,0,-81,94,0,83,Blocks.GRASS.getDefaultState());
        a.box(-94,1,-81,94,3,-81,WHITE);a.box(-94,4,-82,94,4,-80,DARK);
        for(int x:new int[]{-94,94}) {a.box(x,1,-80,x,3,82,WHITE);a.box(x-1,4,-81,x+1,4,83,DARK);}
        for(int[] span:new int[][]{{-94,-12},{12,94}}) {a.box(span[0],1,83,span[1],3,83,WHITE);a.box(span[0],4,82,span[1],4,84,DARK);}
        a.box(-4,0,61,4,0,96,Blocks.GRAVEL.getDefaultState());
        a.box(-12,0,76,12,0,84,STONE);
        for(int x:new int[]{-10,10})for(int z:new int[]{77,83})a.box(x,1,z,x,7,z,LOG);
        roof(a,-14,74,14,86,8,false);a.box(-2,6,84,2,7,84,DARK);a.sign(0,6,85,EnumFacing.SOUTH,"永远亭","");
        a.lamp(-13,1,69);a.lamp(13,1,69);a.room("门庭",0,0,72);
        // Kitchen gardens and a low fence leave the house fronts and the return route open.
        for(int x=42;x<=87;x++)for(int z=67;z<=77;z++) {
            boolean water=(x-42)%9==4;
            a.block(x,0,z,water?Blocks.WATER.getDefaultState():Blocks.FARMLAND.getStateFromMeta(7));
            if(!water)a.block(x,1,z,Blocks.CARROTS.getStateFromMeta(7));
        }
        a.box(40,1,65,89,1,65,Blocks.SPRUCE_FENCE.getDefaultState());
        a.box(40,1,78,89,1,78,Blocks.SPRUCE_FENCE.getDefaultState());
        a.box(89,1,66,89,1,77,Blocks.SPRUCE_FENCE.getDefaultState());
        a.room("菜圃",38,0,72);
    }
    private static void reception(GensokyoArchitecture a) {
        house(a,-14,38,24,60,false);a.openZ(0,60,FLOOR,3,4);a.openZ(5,38,FLOOR,2,4);
        a.box(-17,3,61,27,3,64,WOOD);a.stairsSouth(0,65,0,FLOOR,4);
        // Shoe shelves, seated waiting space and the visitor counter have separate circulation.
        a.box(-12,4,56,-7,5,57,DARK);a.box(14,4,56,22,4,58,WOOD);
        a.block(17,5,57,ModBlocks.MEDICINE_TRAY.getDefaultState());
        VillageJoinery.lowDesk(a,-10,FLOOR,44,6);a.box(-11,4,47,-5,4,48,Blocks.CARPET.getStateFromMeta(13));
        a.chest(21,FLOOR,41,"eientei_clinic");a.block(20,5,57,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.box(-3,8,61,3,8,61,DARK);a.sign(0,8,62,EnumFacing.SOUTH,"蓬莱药局","");
        light(a,-4,48);light(a,16,48);a.room("玄关与候诊",4,FLOOR,51);
        a.openX(-14,43,FLOOR,2,4);a.openX(24,44,FLOOR,2,4);
        a.box(1,3,34,9,3,37,WOOD);
        // North-facing steps descend into the pond court.
        for(int n=1;n<=3;n++) {
            a.box(2,0,34-n,8,3-n,34-n,STONE);
            a.box(2,4-n,34-n,8,4-n,34-n,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
        }
    }
    private static void clinic(GensokyoArchitecture a) {
        house(a,-82,0,-35,49,true);
        a.openX(-35,43,FLOOR,2,4);a.openX(-35,9,FLOOR,2,4);
        a.box(-43,4,1,-43,8,48,WHITE);
        for(int z:new int[]{9,23,42})a.openX(-43,z,FLOOR,1,4);
        a.box(-81,4,15,-44,8,15,WHITE);a.box(-81,4,31,-44,8,31,WHITE);
        // Medicine drawers and supplies surround a central preparation bench.
        for(int x=-79;x<=-49;x+=2)for(int y=4;y<=6;y++)cabinet(a,x,y,1,EnumFacing.SOUTH);
        for(int z=4;z<=11;z+=2)for(int y=4;y<=6;y++)cabinet(a,-81,y,z,EnumFacing.EAST);
        a.box(-72,4,7,-61,4,9,WOOD);
        for(int x:new int[]{-70,-66,-62})a.block(x,5,8,ModBlocks.MEDICINE_TRAY.getDefaultState());
        a.block(-76,4,10,Blocks.BREWING_STAND.getDefaultState());a.block(-76,4,6,Blocks.CAULDRON.getStateFromMeta(3));
        a.chest(-49,FLOOR,3,"eientei_herbs");a.chest(-49,FLOOR,7,"eientei_herbs");
        a.chest(-49,FLOOR,11,"eientei_clinic");a.room("配药与药橱",-57,FLOOR,10);
        // Two consultation bays retain a connecting door and an unobstructed bedside approach.
        a.box(-63,4,16,-63,8,30,WHITE);a.openX(-63,26,FLOOR,1,4);
        for(int x:new int[]{-76,-55}) {
            a.bed(x,FLOOR,21);VillageJoinery.lowDesk(a,x-3,FLOOR,27,5);
            a.chest(x-3,FLOOR,17,"eientei_clinic");a.block(x+2,4,19,ModBlocks.PHARMACY_CABINET.getDefaultState());
            a.room("诊察间",x+3,FLOOR,23);
        }
        a.box(-80,4,35,-68,4,36,WOOD);a.box(-80,4,46,-68,4,47,WOOD);
        VillageJoinery.lowDesk(a,-59,FLOOR,38,6);a.block(-52,4,46,ModBlocks.MEDICINE_TRAY.getDefaultState());
        a.chest(-79,FLOOR,40,"eientei_clinic");a.room("候诊与取药",-65,FLOOR,42);
        a.room("诊所侧廊",-39,FLOOR,24);
        for(int[] p:new int[][]{{-57,8},{-70,23},{-51,23},{-65,42}})light(a,p[0],p[1]);
    }
    private static void privateRooms(GensokyoArchitecture a) {
        house(a,-40,-59,58,-31,false);
        a.openZ(-28,-31,FLOOR,2,4);a.openZ(0,-31,FLOOR,3,4);a.openZ(54,-31,FLOOR,2,4);
        a.openX(58,-40,FLOOR,2,4);
        a.box(-39,4,-39,57,8,-39,WHITE);
        for(int x:new int[]{-25,6,40})a.openZ(x,-39,FLOOR,1,4);
        for(int x:new int[]{-9,24})a.box(x,4,-58,x,8,-40,WHITE);
        // Eirin's records and sleeping room are separate from the public pharmacy.
        a.box(-39,4,-49,-10,8,-49,WHITE);a.openZ(-16,-49,FLOOR,1,4);
        a.bed(-32,FLOOR,-54);a.chest(-38,FLOOR,-55,"eientei_herbs");
        VillageJoinery.lowDesk(a,-36,FLOOR,-44,9);a.box(-39,4,-47,-39,6,-41,Blocks.BOOKSHELF.getDefaultState());
        a.block(-20,4,-44,ModBlocks.MEDICINE_TRAY.getDefaultState());
        a.room("永琳书斋",-18,FLOOR,-43);a.room("永琳寝室",-22,FLOOR,-54);
        // Kaguya's room opens to a deep moon-viewing veranda.
        a.bed(17,FLOOR,-53);a.box(11,4,-57,11,7,-49,PAPER);a.chest(21,FLOOR,-56,"lunar_archive");
        VillageJoinery.lowDesk(a,-3,FLOOR,-46,9);a.box(-5,4,-57,6,4,-56,DARK);
        a.block(1,5,-56,ModBlocks.ARMILLARY.getDefaultState());
        a.room("辉夜起居",7,FLOOR,-44);a.room("辉夜寝位",16,FLOOR,-49);
        // Static exhibit cases use normal block lighting and can be walked around on every side.
        for(int x:new int[]{31,42,52}) {
            a.box(x-2,4,-55,x+2,4,-52,DARK);a.box(x-2,5,-55,x+2,6,-52,Blocks.GLASS.getDefaultState());
            a.block(x,5,-53,x==31?ModBlocks.ARMILLARY.getDefaultState():x==42?ModBlocks.RESEARCH_NOTES.getDefaultState():Blocks.QUARTZ_BLOCK.getDefaultState());
        }
        a.box(27,4,-42,35,6,-41,Blocks.BOOKSHELF.getDefaultState());a.chest(55,FLOOR,-42,"lunar_archive");
        a.room("月都万象展陈列间",41,FLOOR,-46);a.room("内宅长廊",17,FLOOR,-35);
        for(int[] p:new int[][]{{-18,-44},{-25,-54},{6,-47},{40,-46}})light(a,p[0],p[1]);
        a.box(-12,0,-30,45,2,-24,STONE);a.box(-12,3,-30,45,3,-24,WOOD);
        a.stairsSouth(0,-23,0,FLOOR,4);a.room("赏月缘侧",16,FLOOR,-26);
        for(int x:new int[]{-10,43})a.box(x,4,-24,x,8,-24,LOG);
        a.box(-14,9,-30,47,9,-23,DARK);
    }
    private static void eastWing(GensokyoArchitecture a) {
        house(a,63,-54,89,21,true);a.openX(63,-40,FLOOR,2,4);a.openX(63,14,FLOOR,2,4);
        a.box(70,4,-53,70,8,20,WHITE);
        for(int z:new int[]{-44,-24,-6,14})a.openX(70,z,FLOOR,1,4);
        for(int z:new int[]{-35,-15,5})a.box(71,4,z,88,8,z,WHITE);
        a.bed(81,FLOOR,-44);a.chest(87,FLOOR,-50,"village_pantry");VillageJoinery.lowDesk(a,74,FLOOR,-40,5);
        a.bed(82,FLOOR,-24);a.chest(87,FLOOR,-31,"eientei_clinic");a.chest(74,FLOOR,-31,"eientei_herbs");
        VillageJoinery.lowDesk(a,74,FLOOR,-20,4);a.box(87,4,-23,87,6,-17,Blocks.BOOKSHELF.getDefaultState());
        VillageJoinery.lowDesk(a,75,FLOOR,-5,9);a.chest(87,FLOOR,2,"village_pantry");
        a.box(73,4,8,87,4,9,WOOD);a.block(75,5,9,ModBlocks.MEDICINE_TRAY.getDefaultState());
        a.block(87,4,13,Blocks.FURNACE.getDefaultState());a.block(87,4,17,Blocks.CAULDRON.getStateFromMeta(3));
        a.chest(74,FLOOR,19,"village_pantry");
        for(int z:new int[]{-44,-24,-6,14})light(a,78,z);
        a.room("天为住处",77,FLOOR,-45);a.room("铃仙住处",78,FLOOR,-25);
        a.room("日常茶饭",77,FLOOR,-1);a.room("厨房",79,FLOOR,15);a.room("东宅长廊",67,FLOOR,-18);
    }
    private static void bath(GensokyoArchitecture a) {
        house(a,68,35,89,59,false);a.openX(68,44,FLOOR,2,4);
        a.box(69,4,47,88,8,47,WHITE);a.openZ(77,47,FLOOR,1,4);
        a.box(72,4,51,85,4,56,STONE);a.box(73,4,52,84,4,55,Blocks.WATER.getDefaultState());
        a.box(86,4,37,87,4,43,WOOD);a.chest(71,FLOOR,38,"doll_materials");
        for(int x:new int[]{74,78,82})a.block(x,4,37,Blocks.CAULDRON.getStateFromMeta(3));
        light(a,78,43);light(a,78,50);a.room("洗涤与更衣",76,FLOOR,44);a.room("浴间",77,FLOOR,49);
    }
    private static void passages(GensokyoArchitecture a) {
        passage(a,-31,-31,-25,43,true);passage(a,51,-31,57,44,true);
        passage(a,-35,40,-14,46,false);passage(a,-35,6,-25,12,false);
        passage(a,24,41,68,47,false);passage(a,57,11,63,17,false);passage(a,58,-43,63,-37,false);
        // Connecting roofs stop at room walls; their ridges do not replace interior ceilings.
        a.room("西长廊",-28,FLOOR,-12);a.room("东长廊",54,FLOOR,-5);
        a.box(-25,3,7,-21,3,11,WOOD);a.openX(-25,9,FLOOR,1,4);
        for(int n=1;n<=3;n++) {
            int x=-21+n;a.box(x,0,7,x,3-n,11,STONE);
            a.box(x,4-n,7,x,4-n,11,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.WEST));
        }
    }
    private static void garden(GensokyoArchitecture a) {
        for(int x=-5;x<=43;x++)for(int z=-12;z<=29;z++) {
            double d=Math.pow((x-19)/22.0,2)+Math.pow((z-9)/18.0,2)
                    +.10*Math.sin(x*.26+z*.17);
            if(d<1) {a.box(x,-2,z,x,-1,z,STONE);a.block(x,0,z,Blocks.WATER.getDefaultState());}
            else if(d<1.17)a.block(x,0,z,Blocks.MOSSY_COBBLESTONE.getDefaultState());
        }
        a.box(-18,0,6,-6,0,12,Blocks.GRAVEL.getDefaultState());
        a.box(-6,1,7,46,1,11,WOOD);
        for(int z:new int[]{7,11})a.box(-2,2,z,41,2,z,Blocks.SPRUCE_FENCE.getDefaultState());
        a.box(14,1,3,24,1,15,WOOD);a.box(14,2,7,24,2,11,AIR);
        VillageJoinery.lowDesk(a,16,1,4,7);a.room("池上赏月台",19,1,10);
        for(int x:new int[]{-14,43})VillageJoinery.maple(a,x,-11,8,4);
        a.box(-3,0,-20,3,0,-12,Blocks.GRAVEL.getDefaultState());
        a.box(-9,0,26,-1,0,31,Blocks.GRAVEL.getDefaultState());
        a.box(-16,0,15,-8,0,26,Blocks.GRASS.getDefaultState());
        a.box(-15,1,25,-9,1,26,WOOD);
        GardenScenery.rocks(a,-12,-16,1);
        GardenScenery.rocks(a,40,26,-1);
        GardenScenery.pine(a,43,-19,-1);
        for(int[] pad:new int[][]{{4,15},{7,21},{29,-1},{33,0}})a.block(pad[0],1,pad[1],Blocks.WATERLILY.getDefaultState());
        a.room("池庭",-13,0,18);
    }
    private static void house(GensokyoArchitecture a,int x1,int z1,int x2,int z2,boolean longZ) {
        a.box(x1-1,0,z1-1,x2+1,2,z2+1,STONE);a.box(x1,3,z1,x2,9,z2,WHITE);
        a.box(x1+1,4,z1+1,x2-1,8,z2-1,AIR);a.box(x1,3,z1,x2,3,z2,WOOD);
        InteriorFinishes.ceiling(a,x1,z1,x2,z2,9,WOOD);
        for(int x=x1;x<=x2;x+=6)for(int z:new int[]{z1,z2}) {
            a.box(x,4,z,x,8,z,LOG);if(x+4<x2)a.box(x+1,5,z,x+4,7,z,PAPER);
        }
        for(int z=z1;z<=z2;z+=6)for(int x:new int[]{x1,x2}) {
            a.box(x,4,z,x,8,z,LOG);if(z+4<z2)a.box(x,5,z+1,x,7,z+4,PAPER);
        }
        roof(a,x1-3,z1-3,x2+3,z2+3,10,longZ);
    }
    private static void passage(GensokyoArchitecture a,int x1,int z1,int x2,int z2,boolean longZ) {
        a.box(x1,0,z1,x2,2,z2,STONE);a.box(x1,3,z1,x2,3,z2,WOOD);
        if(longZ)for(int z=z1+4;z<z2;z+=8)for(int x:new int[]{x1,x2})a.box(x,4,z,x,8,z,LOG);
        else for(int x=x1+4;x<x2;x+=8)for(int z:new int[]{z1,z2})a.box(x,4,z,x,8,z,LOG);
        roof(a,x1,z1,x2,z2,9,longZ);
    }
    private static void roof(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int y,boolean longZ) {
        int low=longZ?x1:z1,high=longZ?x2:z2,mid=(low+high)/2,half=(high-low)/2;
        for(int t=low;t<=high;t++) {
            int rise=Math.max(0,(half-Math.abs(t-mid))/3);
            if(longZ) {
                a.box(t,y+rise,z1,t,y+rise,z2,DARK);
                if(rise>0)for(int z:new int[]{z1+3,z2-3})a.box(t,y,z,t,y+rise-1,z,WOOD);
            } else {
                a.box(x1,y+rise,t,x2,y+rise,t,DARK);
                if(rise>0)for(int x:new int[]{x1+3,x2-3})a.box(x,y,t,x,y+rise-1,t,WOOD);
            }
        }
        if(longZ)a.box(mid,y+half/3+1,z1,mid,y+half/3+1,z2,Blocks.WOODEN_SLAB.getStateFromMeta(5));
        else a.box(x1,y+half/3+1,mid,x2,y+half/3+1,mid,Blocks.WOODEN_SLAB.getStateFromMeta(5));
    }
    private static void cabinet(GensokyoArchitecture a,int x,int y,int z,EnumFacing f) {a.block(x,y,z,ModBlocks.PHARMACY_CABINET.getDefaultState().withProperty(BlockHorizontal.FACING,f));}
    private static void herbCourt(GensokyoArchitecture a) {
        a.openZ(-60,0,FLOOR,2,4);a.box(-64,3,-3,-56,3,-1,WOOD);
        for(int n=1;n<=3;n++) {
            a.box(-63,0,-3-n,-57,3-n,-3-n,STONE);
            a.box(-63,4-n,-3-n,-57,4-n,-3-n,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
        }
        a.box(-66,0,-13,-56,0,-7,Blocks.GRAVEL.getDefaultState());a.box(-66,0,-72,-64,0,-10,Blocks.GRAVEL.getDefaultState());
        for(int x:new int[]{-82,-61})for(int z:new int[]{-63,-36}) {
            a.box(x,0,z,x+12,1,z+15,DARK);a.box(x+1,1,z+1,x+11,1,z+14,Blocks.DIRT.getStateFromMeta(2));
            for(int dx=2;dx<=10;dx+=2)for(int dz=2;dz<=13;dz+=3)
                a.block(x+dx,2,z+dz,z==-63?Blocks.RED_FLOWER.getStateFromMeta(dx%9):Blocks.BROWN_MUSHROOM.getDefaultState());
        }
        a.box(-84,0,-75,-48,0,-70,Blocks.GRAVEL.getDefaultState());
        for(int x:new int[]{-80,-57}) {
            for(int dx:new int[]{0,5})a.box(x+dx,1,-72,x+dx,5,-72,LOG);
            a.box(x,5,-72,x+5,5,-72,WOOD);a.box(x+1,2,-73,x+4,2,-72,WOOD);
            a.block(x+2,3,-72,ModBlocks.MEDICINE_TRAY.getDefaultState());
        }
        a.chest(-68,0,-74,"eientei_herbs");a.room("药草晾晒庭",-65,0,-13);
        for(int x=-28;x<=84;x+=7)BambooGrove.gardenCulm(a,x,-72,16+Math.floorMod(x,5),EnumFacing.NORTH);
    }
    private static void light(GensokyoArchitecture a,int x,int z) {a.block(x,8,z,ModBlocks.RED_LANTERN.getDefaultState());}
}
