package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Reporting, printing, patrol and communal rooms, distributed across the inhabited ledges. */
final class TenguInstitutions {
    private TenguInstitutions() {}
    static void build(GensokyoBlueprint plan) {
        printing(open(plan,TenguLayout.PRINT));newsroom(open(plan,TenguLayout.NEWS));
        council(open(plan,TenguLayout.COUNCIL));inn(open(plan,TenguLayout.INN));
        patrol(open(plan,TenguLayout.PATROL));tea(open(plan,TenguLayout.TEA));paper(open(plan,TenguLayout.PAPER));
    }
    private static GensokyoArchitecture open(GensokyoBlueprint plan,TenguLayout.Plot p) {
        GensokyoArchitecture a=TenguLayout.at(plan,p);TenguJoinery.house(a,p);
        a.box(-6,3,p.d+1,6,4,p.d+1,DARK);a.sign(0,3,p.d+2,EnumFacing.SOUTH,p.name,"");return a;
    }
    private static void printing(GensokyoArchitecture a) {
        VillageJoinery.wallX(a,5,-19,14,0,4);
        for(int x:new int[]{-23,-9})for(int z:new int[]{-10,4}) {
            a.box(x-2,1,z-2,x+2,1,z+2,WOOD);
            a.block(x,2,z,ModBlocks.TENGU_PRESS.getDefaultState());
            a.block(x+2,2,z+1,ModBlocks.RESEARCH_NOTES.getDefaultState());
            a.block(x+5,1,z,Blocks.CRAFTING_TABLE.getDefaultState());
        }
        for(int x:new int[]{-29,-19,-9})a.chest(x,0,-17,"tengu_printing");
        for(int z:new int[]{-10,3}) {
            a.box(12,1,z,26,1,z,WOOD);a.block(16,2,z,ModBlocks.RESEARCH_NOTES.getDefaultState());
            a.block(22,2,z,ModBlocks.RESEARCH_NOTES.getDefaultState());
        }
        TenguHomes.shelves(a,10,0,-17,20);a.chest(28,0,12,"tengu_printing");
        a.room("排字印刷间",-16,0,-2);a.room("装订间",18,0,8);
        TenguHomes.shelves(a,-29,6,-17,59);
        for(int x:new int[]{-23,-8,12})for(int z:new int[]{-9,1})VillageJoinery.lowDesk(a,x,6,z,6);
        for(int z:new int[]{-11,0})a.chest(30,6,z,"tengu_printing");
        a.room("校对间",1,6,4);a.room("留样书库",-4,6,-13);
        for(int x:new int[]{-21,21})for(int z:new int[]{-10,11}) {TenguJoinery.light(a,x,0,z);TenguJoinery.light(a,x,6,z);}
    }
    private static void newsroom(GensokyoArchitecture a) {
        VillageJoinery.wallZ(a,-32,32,-3,0,0);VillageJoinery.wallX(a,4,-21,-4,0,-11);
        a.box(3,1,8,18,1,8,DARK);a.block(7,2,8,ModBlocks.RESEARCH_NOTES.getDefaultState());
        VillageJoinery.lowDesk(a,-19,0,1,7);
        a.block(-12,1,9,ModBlocks.TENGU_CAMERA.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
        a.box(-27,1,-17,-9,1,-17,WOOD);a.block(-20,2,-17,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.block(-27,1,-10,Blocks.CAULDRON.getDefaultState());a.chest(-12,0,-12,"tengu_printing");
        TenguHomes.shelves(a,9,0,-18,20);a.chest(24,0,-9,"tengu_printing");
        a.room("接稿处",0,0,14);a.room("照相暗房",-5,0,-9);a.room("底片档案间",13,0,-8);
        a.box(-7,1,-18,0,1,-16,STONE);
        for(int x:new int[]{-6,-3})a.block(x,2,-17,Blocks.CAULDRON.getStateFromMeta(3));
        a.block(0,2,-17,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.box(-7,4,-20,1,4,-20,Blocks.WOODEN_SLAB.getStateFromMeta(13));
        a.block(-4,5,-20,ModBlocks.TENGU_CAMERA.getDefaultState());
        // Opaque shutters cover this room's rear windows without obstructing its side door.
        a.box(-29,2,-21,2,4,-21,DARK);
        for(int x:new int[]{-5,8})VillageJoinery.wallX(a,x,-21,10,6,3);
        for(int z:new int[]{-12,-1}) {VillageJoinery.lowDesk(a,-23,6,z,9);VillageJoinery.lowDesk(a,13,6,z,8);}
        VillageJoinery.lowDesk(a,-2,6,-8,6);a.chest(29,6,-18,"tengu_printing");
        a.room("西采编室",-10,6,5);a.room("东采编室",16,6,5);a.room("编辑室",2,6,0);
        TenguHomes.shelves(a,-28,12,-19,56);
        a.box(-10,13,-8,10,13,-6,WOOD);a.block(0,14,-7,ModBlocks.RESEARCH_NOTES.getDefaultState());
        for(int x:new int[]{-18,18}) {
            a.block(x,13,4,ModBlocks.TENGU_CAMERA.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
            VillageJoinery.lowDesk(a,x-3,12,-1,5);
        }
        a.room("阅图室",0,12,3);a.room("取材望廊",0,12,24);
    }
    private static void council(GensokyoArchitecture a) {
        for(int x:new int[]{-20,20})VillageJoinery.wallX(a,x,-19,14,0,7);
        a.box(-10,1,-9,10,1,-5,DARK);a.box(-10,2,-9,10,2,-5,Blocks.WOODEN_SLAB.getDefaultState());
        for(int x=-8;x<=8;x+=4) {
            a.box(x,1,-12,x+1,1,-12,Blocks.CARPET.getStateFromMeta(11));
            a.box(x,1,-2,x+1,1,-2,Blocks.CARPET.getStateFromMeta(11));
        }
        a.block(0,3,-7,ModBlocks.RESEARCH_NOTES.getDefaultState());
        for(int side:new int[]{-1,1}) {
            int x=side<0?-35:25;
            for(int z:new int[]{-12,0})VillageJoinery.lowDesk(a,x,0,z,9);
            a.chest(side*37,0,-16,"tengu_patrol");a.room(side<0?"西文书间":"东文书间",side*25,0,8);
        }
        a.room("议事堂",0,0,9);
        for(int x:new int[]{-29,-11,7,25})TenguHomes.shelves(a,x,6,-15,9);
        VillageJoinery.lowDesk(a,-15,6,3,10);VillageJoinery.lowDesk(a,8,6,3,10);
        for(int side:new int[]{-1,1})a.chest(side*36,6,-3,"tengu_printing");
        a.room("簿册阁",0,6,-6);a.room("会见间",0,6,9);
        TenguJoinery.roof(a,-12,13,12,26,15,1);
        a.box(-7,9,21,7,10,21,DARK);a.sign(0,9,22,EnumFacing.SOUTH,"议事会馆","");
    }
    private static void inn(GensokyoArchitecture a) {
        VillageJoinery.wallZ(a,-29,29,-5,0,0);
        TenguHomes.kitchen(a,20,0,-15);TenguHomes.kitchen(a,-25,0,-15);
        a.chest(-8,0,-14,"village_pantry");a.chest(10,0,-14,"village_pantry");
        for(int x:new int[]{-14,-2,10})for(int z:new int[]{2,10})VillageJoinery.lowDesk(a,x,0,z,5);
        a.room("食堂",0,0,15);a.room("厨间",0,0,-9);
        a.box(-29,7,-4,29,10,-4,TenguJoinery.PLASTER);
        for(int x:new int[]{-20,-10,0,10,20})a.box(x,7,-18,x,10,-5,TenguJoinery.PLASTER);
        for(int x:new int[]{-25,-15,-5,5,15,25}) {
            a.openZ(x,-4,6,1,3);a.bed(x-1,6,-14);a.chest(x+2,6,-15,"tengu_residence");
            VillageJoinery.lowDesk(a,x-2,6,-10,3);a.room("客间"+(x+25)/10,x,6,-6);
        }
        for(int x:new int[]{-12,2,15})VillageJoinery.lowDesk(a,x,6,5,5);
        a.room("旅舍望廊",0,6,12);
    }
    private static void patrol(GensokyoArchitecture a) {
        TenguHomes.shelves(a,-15,0,-14,31);
        for(int x:new int[]{-12,7}) {VillageJoinery.lowDesk(a,x,0,-6,6);a.chest(x,0,0,"tengu_patrol");}
        a.box(-5,1,6,7,1,6,WOOD);a.block(0,2,6,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.room("交班间",0,0,11);
        VillageJoinery.wallZ(a,-19,19,-4,6,0);
        for(int x:new int[]{-12,-4,4,12}) {a.bed(x,6,-11);a.chest(x+2,6,-12,"tengu_patrol");}
        VillageJoinery.lowDesk(a,-4,6,1,7);
        a.room("轮值寝间",0,6,-7);a.room("休息间",0,6,5);
    }
    private static void tea(GensokyoArchitecture a) {
        VillageJoinery.wallZ(a,-28,28,-7,0,0);TenguHomes.kitchen(a,18,0,-14);
        a.chest(-22,0,-14,"village_pantry");a.chest(-15,0,-14,"village_pantry");
        a.box(-10,1,-4,10,1,-4,DARK);a.box(-10,2,-4,10,2,-4,Blocks.WOODEN_SLAB.getDefaultState());
        for(int x:new int[]{-21,-9,7,19})for(int z:new int[]{2,10})VillageJoinery.lowDesk(a,x,0,z,4);
        a.room("茶间",1,0,7);a.room("备茶间",0,0,-11);a.room("听涧廊",0,0,21);
    }
    private static void paper(GensokyoArchitecture a) {
        a.box(-14,-1,-8,-6,0,0,STONE);a.box(-13,0,-7,-7,0,-1,Blocks.WATER.getDefaultState());
        for(int z:new int[]{-6,2}) {
            a.box(3,1,z,13,1,z,WOOD);a.block(7,2,z,ModBlocks.RESEARCH_NOTES.getDefaultState());
            a.block(15,1,z,Blocks.CRAFTING_TABLE.getDefaultState());
        }
        for(int x=-13;x<=-5;x+=4) {a.box(x,1,5,x,3,5,TenguJoinery.POST);a.box(x+1,2,5,x+3,2,5,PAPER);}
        a.chest(14,0,7,"tengu_printing");a.room("抄纸间",0,0,-3);a.room("晾纸间",0,0,7);
    }
}
