package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.init.Blocks;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Main house, reference rooms, family quarters and service rooms surrounding a pond garden. */
final class HiedaEstate {
    private HiedaEstate() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.HIEDA);
        perimeter(a);
        VillageJoinery.house(a,-40,-50,40,-26,2,1);
        VillageJoinery.wallX(a,-12,-49,-27,2,-34);VillageJoinery.wallX(a,12,-49,-27,2,-34);
        a.openZ(0,-26,2,3,3);a.box(-42,2,-25,42,2,-19,WOOD);a.stairsSouth(0,-18,0,2,4);
        a.box(-9,2,-47,9,2,-30,Blocks.WOOL.getStateFromMeta(13));
        VillageJoinery.lowDesk(a,-5,2,-41,10);a.room("接待厅",0,2,-31);
        a.box(-36,2,-46,-17,2,-30,Blocks.WOOL.getStateFromMeta(13));
        VillageJoinery.lowDesk(a,-32,2,-39,7);a.room("访谈室",-17,2,-34);
        a.box(-39,3,-48,-39,5,-30,Blocks.BOOKSHELF.getDefaultState());a.chest(-17,2,-47,"forest_books");
        a.box(16,2,-47,37,2,-30,Blocks.WOOL.getStateFromMeta(13));
        VillageJoinery.lowDesk(a,24,2,-41,5);a.room("阿求书写室",19,2,-34);
        a.box(16,3,-49,37,5,-49,Blocks.BOOKSHELF.getDefaultState());
        a.box(39,3,-45,39,5,-29,Blocks.BOOKSHELF.getDefaultState());
        a.box(31,3,-31,34,3,-30,DARK);a.block(32,4,-30,ModBlocks.GRAMOPHONE.getDefaultState());
        a.chest(17,2,-45,"hieda_records");a.block(26,3,-32,ModBlocks.RESEARCH_NOTES.getDefaultState());
        for(int x:new int[]{-28,0,28})VillageJoinery.lantern(a,x,2,-36);
        for(int x:new int[]{-36,-12,12,36})a.box(x,3,-20,x,6,-20,LOG);
        a.box(-43,7,-25,43,7,-18,ROOF);

        for(int side:new int[]{-1,1}) {
            int x1=side<0?-66:46,x2=side<0?-46:66;
            VillageJoinery.house(a,x1,-46,x2,22,2,1);
            int wall=side<0?-51:51,door=side<0?-57:57;
            a.box(wall,3,-45,wall,6,21,WHITE);
            for(int z:new int[]{-35,-17,1,16})a.openX(wall,z,2,1,3);
            for(int z:new int[]{-26,-8,8})a.box(side<0?-65:52,3,z,side<0?-52:65,6,z,WHITE);
            // Every room opens onto a continuous inside corridor.
            a.room(side<0?"西侧书库廊":"东侧生活廊",side<0?-48:48,2,3);
            a.openZ(side<0?-48:48,22,2,1,3);
            a.openX(side<0?-46:46,-32,2,2,3);
            a.box(side<0?-46:40,0,-35,side<0?-40:46,1,-29,STONE);
            a.box(side<0?-46:40,2,-35,side<0?-40:46,2,-29,WOOD);
            a.box(side<0?-46:40,3,-34,side<0?-40:46,6,-30,AIR);
            a.box(side<0?-48:39,7,-36,side<0?-39:48,7,-28,ROOF);
            a.openX(side<0?-40:40,-32,2,2,3);
            for(int z:new int[]{-35,-17,1,16})VillageJoinery.lantern(a,door,2,z);
        }
        // East rooms: sleeping, guest room, family room, and working kitchen.
        a.bed(59,2,-35);a.chest(54,2,-43);a.chest(63,2,-28);VillageJoinery.lowDesk(a,55,2,-40,2);
        a.room("阿求寝室",54,2,-34);
        a.bed(58,2,-16);a.bed(62,2,-16);a.chest(54,2,-24);a.room("家人居室",54,2,-16);
        VillageJoinery.lowDesk(a,56,2,0,5);a.chest(63,2,5);a.room("家人起居间",54,2,2);
        a.box(54,3,10,64,3,10,WOOD);a.block(56,3,11,Blocks.FURNACE.getDefaultState());
        a.block(61,3,11,Blocks.CAULDRON.getStateFromMeta(3));a.chest(64,2,19,"village_pantry");
        a.room("厨房",55,2,17);
        // West reference library, documents, domestic stock, and staff sleeping room.
        for(int x:new int[]{-64,-59,-54})a.box(x,3,-43,x,5,-30,Blocks.BOOKSHELF.getDefaultState());
        a.room("史料书库",-57,2,-35);a.chest(-63,2,-28,"hieda_records");
        a.box(-65,3,-24,-65,5,-10,Blocks.BOOKSHELF.getDefaultState());
        VillageJoinery.lowDesk(a,-62,2,-18,4);a.chest(-54,2,-24,"hieda_records");a.room("文书整理",-54,2,-16);
        for(int z:new int[]{-5,0,5})a.chest(-63,2,z,"village_pantry");
        a.box(-55,3,-5,-53,4,5,Blocks.WOOL.getStateFromMeta(0));a.room("布草与储藏",-60,2,1);
        for(int x:new int[]{-63,-59,-55})a.bed(x,2,14);a.chest(-54,2,20);a.room("佣人居室",-60,2,18);

        // Covered passages keep the living rooms and guest wings connected in rain.
        for(int side:new int[]{-1,1}) {
            int x1=side<0?-49:44,x2=side<0?-44:49;
            a.box(x1,0,23,x2,1,32,STONE);a.box(x1,2,23,x2,2,32,WOOD);
            for(int z:new int[]{25,31})a.box(side<0?-44:44,3,z,side<0?-44:44,6,z,LOG);
            a.box(x1-1,7,23,x2+1,7,33,ROOF);
            a.box(side<0?-49:10,0,28,side<0?-10:49,1,32,STONE);
            a.box(side<0?-49:10,2,28,side<0?-10:49,2,32,WOOD);
            a.box(side<0?-50:9,7,27,side<0?-9:50,7,33,ROOF);
        }
        VillageJoinery.house(a,-38,33,-10,49,2,1);VillageJoinery.house(a,10,33,38,49,2,1);
        for(int x:new int[]{-30,-17,17,30}) {a.openZ(x,33,2,1,3);a.openZ(x,49,2,1,3);a.stairsSouth(x,50,0,2,2);}
        VillageJoinery.wallX(a,-24,34,48,2,41);VillageJoinery.wallX(a,24,34,48,2,41);
        VillageJoinery.lowDesk(a,-34,2,40,6);a.room("膳室",-30,2,37);
        a.box(-22,3,35,-12,3,35,WOOD);a.chest(-12,2,46,"village_pantry");a.room("备餐间",-17,2,42);
        for(int x:new int[]{14,28}) {a.bed(x,2,45);a.chest(x+5,2,36);VillageJoinery.lowDesk(a,x+3,2,41,3);a.room("客寝",x+4,2,45);}
        for(int x:new int[]{-30,-17,17,30})VillageJoinery.lantern(a,x,2,40);

        VillageJoinery.pond(a,16,4,15,11);
        VillageJoinery.maple(a,-17,2,12,9);VillageJoinery.maple(a,32,17,10,7);
        VillageJoinery.maple(a,-29,19,9,6);
        a.box(-3,0,-17,3,0,65,Blocks.GRAVEL.getDefaultState());
        a.box(-41,0,25,41,0,27,Blocks.GRAVEL.getDefaultState());
        // A low bridge serves the path around the pond, rather than crossing the main entrance.
        a.box(13,1,-9,16,1,17,Blocks.PLANKS.getStateFromMeta(1));
        for(int z:new int[]{-10,18})a.box(13,1,z,16,1,z,Blocks.STONE_BRICK_STAIRS.getDefaultState());
        a.room("池畔庭院",-8,0,19);
        for(int x=-34;x<=-24;x+=2)for(int z=-14;z<=-8;z+=2)a.block(x,1,z,Blocks.RED_FLOWER.getStateFromMeta(0));
        for(int z=0;z<=14;z+=2) {a.block(-38,1,z,Blocks.LEAVES.getStateFromMeta(4));a.block(38,1,z,Blocks.LEAVES.getStateFromMeta(4));}
        a.box(-21,1,12,-14,1,12,Blocks.SPRUCE_STAIRS.getDefaultState());
        for(int x:new int[]{-37,37})for(int z:new int[]{-15,25,55})a.lamp(x,1,z);
    }
    private static void perimeter(GensokyoArchitecture a) {
        a.box(-72,0,-58,72,0,59,Blocks.GRASS.getDefaultState());
        for(int z:new int[]{-57,58}) {
            a.box(-72,1,z,72,4,z,WHITE);a.box(-73,5,z,73,5,z,ROOF);
        }
        for(int x:new int[]{-72,72}) {
            a.box(x,1,-57,x,4,58,WHITE);a.box(x,5,-58,x,5,59,ROOF);
        }
        a.box(-6,1,57,6,5,59,AIR);
        for(int x:new int[]{-7,7})a.box(x,1,57,x,7,59,LOG);
        a.box(-7,7,57,7,7,59,DARK);a.gable(-10,55,10,61,8);
        a.room("正门",0,0,57);
    }
}
