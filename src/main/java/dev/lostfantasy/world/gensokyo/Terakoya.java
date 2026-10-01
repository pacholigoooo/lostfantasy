package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.init.Blocks;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

final class Terakoya {
    private Terakoya() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.TERAKOYA);
        VillageJoinery.house(a,-28,-19,28,8,2,1);
        a.box(-30,2,9,30,2,13,WOOD);a.openZ(0,8,2,2,3);a.stairsSouth(0,14,0,2,4);
        a.box(-3,0,16,3,0,44,Blocks.GRAVEL.getDefaultState());
        a.box(-4,6,9,4,6,9,DARK);a.sign(0,6,10,net.minecraft.util.EnumFacing.SOUTH,"寺子屋","");
        // A central entry separates teaching and a smaller reading room.
        VillageJoinery.wallX(a,7,-18,7,2,3);
        for(int x:new int[]{-23,-15,-7})for(int z:new int[]{-7,-1,5})VillageJoinery.lowDesk(a,x,2,z,3);
        a.box(-25,3,-15,-6,3,-14,WOOD);a.block(-14,4,-14,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.box(-24,4,-18,-7,5,-18,Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(13));
        a.box(-27,3,-12,-27,5,5,Blocks.BOOKSHELF.getDefaultState());
        a.room("授课间",-2,2,-4);a.room("讲席",-4,2,-14);
        a.box(27,3,-17,27,5,5,Blocks.BOOKSHELF.getDefaultState());
        for(int z:new int[]{-12,-4,4})VillageJoinery.lowDesk(a,13,2,z,5);
        a.room("习字与阅读",11,2,0);
        for(int[] p:new int[][]{{-20,-6},{-9,1},{15,-8},{15,2}})VillageJoinery.lantern(a,p[0],2,p[1]);
        // A quiet rear study holds reference books and the history compilation desk.
        VillageJoinery.house(a,-22,-28,22,-19,2,1);
        a.box(-2,3,-20,2,5,-18,AIR);
        VillageJoinery.wallX(a,7,-27,-20,2,-24);
        a.box(-21,3,-27,-7,5,-27,Blocks.BOOKSHELF.getDefaultState());
        VillageJoinery.lowDesk(a,-17,2,-24,5);a.chest(-3,2,-26,"forest_books");
        a.room("历史编纂室",2,2,-23);a.bed(17,2,-24);a.chest(10,2,-26);
        a.room("值宿间",11,2,-23);VillageJoinery.lantern(a,-3,2,-23);VillageJoinery.lantern(a,13,2,-23);
        // Courtyard, wash basin, and a small supply shed have a purpose outside class.
        a.box(-30,0,15,-12,0,28,Blocks.GRAVEL.getDefaultState());
        VillageJoinery.maple(a,-25,23,8,5);
        a.box(14,1,18,21,1,21,STONE);a.box(15,2,19,20,2,20,Blocks.WATER.getDefaultState());
        a.box(14,2,18,21,2,18,STONE);a.box(14,2,21,21,2,21,STONE);
        a.box(14,2,19,14,2,20,STONE);a.box(21,2,19,21,2,20,STONE);
        VillageJoinery.house(a,24,18,33,27,1,1);a.openZ(28,27,1,1,3);
        a.chest(26,1,20,"school_supplies");a.block(31,2,20,Blocks.CRAFTING_TABLE.getDefaultState());
        a.room("教学用具间",28,1,24);a.stairsSouth(28,28,0,1,2);
        for(int x:new int[]{-10,10})a.lamp(x,1,24);
    }
}
