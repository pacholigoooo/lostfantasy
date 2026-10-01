package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStairs;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

final class Suzunaan {
    private Suzunaan() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.SUZUNAAN);
        VillageJoinery.house(a,-17,-16,17,17,1,2);
        a.openZ(0,17,1,2,3);a.box(-4,1,18,4,1,20,WOOD);a.stairsSouth(0,21,0,1,3);
        a.box(-2,0,22,2,0,40,Blocks.GRAVEL.getDefaultState());
        a.box(-5,5,18,5,5,18,DARK);a.sign(0,5,19,EnumFacing.SOUTH,"铃奈庵","");
        // A sheltered front display and the two seating places face the shop entrance.
        a.box(-18,5,18,18,5,20,ROOF);
        for(int x:new int[]{-13,13})a.block(x,4,19,ModBlocks.RED_LANTERN.getDefaultState());
        for(int x=-14;x<=-9;x++)a.block(x,2,13,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
        VillageJoinery.lowDesk(a,-13,1,10,3);
        a.box(9,2,12,14,2,13,Blocks.BOOKSHELF.getDefaultState());
        a.block(11,3,13,ModBlocks.RESEARCH_NOTES.getDefaultState());
        for(int x:new int[]{-16,16})a.box(x,2,-12,x,4,8,Blocks.BOOKSHELF.getDefaultState());
        for(int x:new int[]{-11,8}) {
            a.box(x,2,-6,x+3,4,5,Blocks.BOOKSHELF.getDefaultState());
            a.chest(x+1,1,7,"forest_books");
        }
        // The desk is in front of the inner door, with room to walk around either side.
        a.box(-4,2,-6,4,2,-5,DARK);a.block(-2,3,-5,ModBlocks.GRAMOPHONE.getDefaultState());
        a.block(2,3,-5,ModBlocks.RESEARCH_NOTES.getDefaultState());
        VillageJoinery.wallZ(a,-16,16,-9,1,0);
        a.room("租书与外来本",0,1,8);a.room("柜台",3,1,-8);a.room("待客席",-13,1,8);
        // Binding and paper storage are reached through the rear passage.
        a.box(-13,2,-14,-7,2,-12,WOOD);a.block(-11,3,-13,Blocks.ANVIL.getDefaultState());
        a.block(-7,3,-12,Blocks.CRAFTING_TABLE.getDefaultState());
        a.chest(2,1,-14,"bookbinding");a.chest(5,1,-14,"forest_books");
        a.box(8,2,-15,10,4,-14,Blocks.BOOKSHELF.getDefaultState());
        a.room("印刷装订",-4,1,-13);VillageJoinery.lantern(a,-4,1,-13);
        // A short rear stair reaches the enclosed loft instead of an oversized second storey hall.
        a.box(11,2,-10,15,10,-6,AIR);a.stairsSouth(13,-10,1,6,2);
        a.box(11,7,-5,15,7,-5,Blocks.SPRUCE_FENCE.getDefaultState());
        a.box(-15,7,-7,6,10,-7,WHITE);a.openZ(2,-7,6,1,3);
        a.box(6,7,-15,6,10,-7,WHITE);a.openX(6,-12,6,1,3);
        a.bed(-5,6,-12);a.box(-14,7,-15,-10,9,-15,Blocks.BOOKSHELF.getDefaultState());
        a.box(-14,7,-11,-14,8,-8,Blocks.BOOKSHELF.getDefaultState());
        a.chest(1,6,-14,"forest_books");VillageJoinery.lowDesk(a,-1,6,-10,2);
        a.block(-7,7,-13,ModBlocks.LIBRARY_LAMP.getDefaultState());a.block(-7,6,-13,WOOD);
        a.room("小铃的阁楼",-7,6,-10);
        // The loft opening overlooks the lower shop; the rail prevents a direct fall.
        a.box(-8,6,-5,4,6,13,AIR);a.box(-8,7,-6,4,7,-6,Blocks.SPRUCE_FENCE.getDefaultState());
        a.box(-9,7,-5,-9,7,13,Blocks.SPRUCE_FENCE.getDefaultState());a.box(5,7,-5,5,7,13,Blocks.SPRUCE_FENCE.getDefaultState());
        a.box(-8,7,14,4,7,14,Blocks.SPRUCE_FENCE.getDefaultState());
        a.box(-16,7,1,-14,9,11,Blocks.BOOKSHELF.getDefaultState());
        a.chest(14,6,12,"forest_books");a.room("阁楼藏书廊",10,6,6);
        for(int[] p:new int[][]{{-13,5},{12,3},{0,13}})VillageJoinery.lantern(a,p[0],1,p[1]);
        a.box(0,6,13,0,10,13,Blocks.OAK_FENCE.getDefaultState());
        VillageJoinery.lantern(a,-5,6,-12);VillageJoinery.lantern(a,10,6,5);
        // Rear household annex, connected through the binding room.
        VillageJoinery.house(a,-12,-24,8,-18,1,1);a.box(-2,2,-19,2,4,-15,AIR);
        a.box(-2,1,-18,2,1,-16,WOOD);
        a.box(-10,2,-22,-5,2,-22,WOOD);a.block(-9,2,-21,Blocks.FURNACE.getDefaultState());
        a.block(-6,2,-21,Blocks.CAULDRON.getStateFromMeta(3));a.chest(5,1,-22);
        a.room("后屋茶水间",1,1,-21);
    }
}
