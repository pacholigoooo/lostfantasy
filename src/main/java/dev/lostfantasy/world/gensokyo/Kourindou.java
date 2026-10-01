package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A crowded roadside shop, connected plaster storehouse and the owner's living quarters. */
final class Kourindou {
    private Kourindou() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.KOURINDOU);
        a.hall(-9,-1,22,18,1,8,LOG);
        // Hipped lower roof and raised gable: the storefront has its own silhouette.
        a.box(-12,10,-4,25,17,21,AIR);
        hipRoof(a,-12,-4,25,21,10,5);
        a.openZ(8,18,1,2,5);a.box(4,1,19,12,1,21,WOOD);
        a.stairsSouth(8,22,0,1,3);
        a.box(-2,0,24,11,0,27,Blocks.GRAVEL.getDefaultState());
        a.box(6,0,22,10,0,25,Blocks.GRAVEL.getDefaultState());
        a.box(-2,0,25,2,0,33,Blocks.GRAVEL.getDefaultState());
        a.box(-7,6,19,0,8,19,DARK);
        a.sign(-3,7,20,EnumFacing.SOUTH,"香霖堂","");
        // Shelves and objects are arranged around a clear route to the counter and rear door.
        shelf(a,-7,2,0,6);shelf(a,14,2,0,6);
        shelf(a,-7,2,16,6);shelf(a,14,2,16,6);
        a.box(-7,2,5,-5,2,11,WOOD);a.box(-7,4,5,-5,4,11,WOOD);
        a.box(-7,2,5,-7,5,5,LOG);a.box(-7,2,11,-7,5,11,LOG);
        for(int z:new int[]{6,9})a.chest(-6,1,z,"kourindou_tools");
        a.block(-6,5,7,ModBlocks.OUTSIDE_TELEVISION.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.EAST));
        a.block(-6,5,10,Blocks.FLOWER_POT.getDefaultState());
        a.box(14,2,4,19,3,5,DARK);a.block(15,4,4,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.block(18,4,4,ModBlocks.LIBRARY_LAMP.getDefaultState());a.chest(20,1,2,"forest_books");
        a.box(0,2,6,2,2,8,WOOD);a.block(1,3,7,ModBlocks.ARMILLARY.getDefaultState());
        a.block(1,3,6,Blocks.FLOWER_POT.getDefaultState());
        a.box(-1,2,2,3,3,3,Blocks.BOOKSHELF.getDefaultState());
        a.block(0,4,2,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.block(2,4,3,ModBlocks.OUTSIDE_TELEVISION.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
        a.box(-3,2,9,0,2,10,WOOD);a.block(-2,3,9,Blocks.CAULDRON.getDefaultState());
        a.block(0,3,10,ModBlocks.OUTSIDE_TELEVISION.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.EAST));
        a.box(3,2,9,4,4,10,Blocks.BOOKSHELF.getDefaultState());
        a.chest(3,1,11);a.block(4,2,13,Blocks.NOTEBLOCK.getDefaultState());
        a.block(3,2,13,Blocks.JUKEBOX.getDefaultState());a.chest(0,1,13);
        a.block(14,2,11,Blocks.FURNACE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.WEST));
        a.box(15,2,11,15,13,11,Blocks.IRON_BARS.getDefaultState());
        a.table(17,1,10,3);a.block(18,4,10,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.box(17,3,10,19,3,10,Blocks.WOODEN_SLAB.getStateFromMeta(8));
        a.block(18,2,13,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
        a.room("旧道具店",8,1,13);a.room("柜台",18,1,2);a.room("暖炉旁的座位",17,1,12);
        a.block(9,8,7,ModBlocks.RED_LANTERN.getDefaultState());

        // Raised domestic floor behind the shop, with a central passage.
        a.hall(-8,-23,22,-1,2,7,LOG);
        a.box(5,2,-2,9,6,0,AIR);a.box(5,1,-1,9,1,0,WOOD);
        a.box(5,3,-22,5,8,-3,WHITE);a.box(10,3,-22,10,8,-3,WHITE);
        a.openX(5,-7,2,1,3);a.openX(5,-17,2,1,3);
        a.openX(10,-7,2,1,3);a.openX(10,-17,2,1,3);
        a.box(-7,3,-12,4,8,-12,WHITE);a.box(11,3,-12,21,8,-12,WHITE);
        a.table(-4,2,-6,5);a.box(-5,2,-10,2,2,-4,Blocks.WOOL.getStateFromMeta(13));
        a.block(-6,3,-10,Blocks.BOOKSHELF.getDefaultState());a.chest(-6,2,-3);
        a.room("客厅",2,2,-7);
        a.box(-6,3,-21,3,3,-21,WOOD);a.block(-5,3,-20,Blocks.FURNACE.getDefaultState());
        a.block(1,3,-20,Blocks.CAULDRON.getStateFromMeta(3));a.chest(3,2,-14);
        a.table(-5,2,-16,4);a.room("厨房",0,2,-17);
        a.bed(17,2,-18);a.chest(20,2,-14);a.box(12,3,-22,14,6,-22,Blocks.BOOKSHELF.getDefaultState());
        a.room("寝室",15,2,-18);
        a.box(13,3,-10,20,3,-10,WOOD);a.block(14,4,-10,Blocks.CRAFTING_TABLE.getDefaultState());
        a.block(18,4,-10,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.block(13,3,-5,Blocks.ANVIL.getDefaultState());a.chest(20,2,-3,"kourindou_tools");
        a.room("修补间",17,2,-6);
        for(int[] p:new int[][]{{-2,-7},{-2,-17},{15,-7},{15,-17},{7,-12}})a.block(p[0],8,p[1],ModBlocks.RED_LANTERN.getDefaultState());

        storehouse(a);
        // Covered passage joins the shop's living side and the storehouse.
        a.box(-15,0,-11,-8,1,-5,STONE);a.box(-15,2,-11,-8,2,-5,WOOD);
        a.box(-15,3,-11,-8,7,-5,WHITE);a.box(-15,3,-10,-8,6,-6,AIR);
        a.box(-17,8,-12,-6,8,-4,ROOF);a.box(-16,3,-10,-14,6,-6,AIR);
        a.box(-9,3,-10,-7,6,-6,AIR);
        a.room("连廊",-11,2,-8);
        a.block(-11,6,-8,ModBlocks.LIBRARY_LAMP.getDefaultState());

        // An open-sided stock shed and the loose collection outside the shop.
        a.box(-28,0,6,-14,1,19,STONE);a.box(-28,1,6,-14,1,19,WOOD);
        for(int x:new int[]{-28,-14})for(int z:new int[]{6,19})a.box(x,2,z,x,6,z,LOG);
        a.box(-28,2,6,-14,5,6,DARK);a.box(-29,7,5,-13,7,20,ROOF);
        for(int x:new int[]{-26,-22,-18}) {a.chest(x,1,8,"kourindou_tools");a.block(x,2,15,Blocks.HAY_BLOCK.getDefaultState());}
        a.box(-26,2,12,-24,4,12,Blocks.BOOKSHELF.getDefaultState());
        a.block(-19,2,11,Blocks.JUKEBOX.getDefaultState());a.block(-16,2,11,Blocks.CAULDRON.getDefaultState());
        a.room("道具棚",-19,1,17);
        for(int x:new int[]{-11,-7,-3}) {a.chest(x,0,23);a.block(x,1,25,Blocks.HAY_BLOCK.getDefaultState());}
        a.box(-10,1,20,-8,1,20,WOOD);a.block(-9,2,20,ModBlocks.OUTSIDE_TELEVISION.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
        a.box(-11,1,25,-9,2,26,DARK);a.block(-10,3,25,Blocks.CAULDRON.getDefaultState());
        a.lamp(15,1,24);a.lamp(-14,1,24);
    }
    private static void storehouse(GensokyoArchitecture a) {
        a.box(-28,0,-21,-15,1,1,STONE);a.box(-28,2,-21,-15,15,1,WHITE);
        a.box(-27,2,-20,-16,14,0,AIR);a.box(-28,8,-21,-15,8,1,WOOD);
        a.box(-28,2,-21,-15,3,-21,DARK);a.box(-28,2,1,-15,3,1,DARK);
        for(int x:new int[]{-28,-15})a.box(x,2,-21,x,3,1,DARK);
        a.gable(-30,-23,-13,3,16);
        for(int z:new int[]{-16,-4})for(int x:new int[]{-28,-15})a.box(x,10,z,x,12,z+1,Blocks.IRON_BARS.getDefaultState());
        a.box(-27,2,-18,-23,13,-12,AIR);a.stairsSouth(-25,-18,1,8,2);
        a.box(-27,9,-11,-23,9,-11,Blocks.SPRUCE_FENCE.getDefaultState());
        for(int z:new int[]{-17,-10,-3}) {
            a.chest(-17,1,z,"kourindou_tools");a.box(-18,4,z,-16,4,z,WOOD);
            a.box(-16,2,z,-16,5,z,LOG);a.block(-17,5,z,Blocks.FLOWER_POT.getDefaultState());
            a.chest(-17,8,z,"forest_books");
        }
        a.box(-26,9,-2,-23,10,0,Blocks.BOOKSHELF.getDefaultState());
        a.block(-24,11,-1,ModBlocks.OUTSIDE_TELEVISION.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.EAST));
        a.box(-23,2,-19,-20,3,-17,DARK);a.block(-21,4,-18,ModBlocks.OUTSIDE_TELEVISION.getDefaultState());
        a.box(-27,2,-9,-26,5,-3,Blocks.BOOKSHELF.getDefaultState());
        a.box(-27,9,-9,-27,12,-4,Blocks.BOOKSHELF.getDefaultState());
        a.box(-21,9,-17,-19,10,-15,DARK);a.block(-20,11,-16,Blocks.CAULDRON.getDefaultState());
        a.room("土藏",-21,1,-7);a.room("二层藏品",-21,8,-7);
        a.block(-21,7,-6,ModBlocks.RED_LANTERN.getDefaultState());a.block(-21,14,-6,ModBlocks.RED_LANTERN.getDefaultState());
    }
    private static void shelf(GensokyoArchitecture a,int x,int floor,int z,int length) {
        a.box(x,floor,z,x+length-1,floor,z,WOOD);a.box(x,floor+2,z,x+length-1,floor+2,z,WOOD);
        for(int end:new int[]{x,x+length-1})a.box(end,floor,z,end,floor+3,z,LOG);
        a.block(x+1,floor+1,z,Blocks.BOOKSHELF.getDefaultState());a.block(x+3,floor+1,z,Blocks.FLOWER_POT.getDefaultState());
        a.block(x+2,floor+3,z,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.block(x+4,floor+3,z,ModBlocks.OUTSIDE_TELEVISION.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
    }
    private static void hipRoof(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int base,int hip) {
        int middle=(z1+z2)/2;
        for(int z=z1;z<=z2;z++) {
            int rise=(Math.min(z-z1,z2-z))/2;
            for(int x=x1;x<=x2;x++) {
                int edge=Math.min(x-x1,x2-x);
                a.block(x,base+(edge<hip?Math.min(edge,rise):rise),z,ROOF);
            }
            if(rise>hip)for(int x:new int[]{x1+hip,x2-hip})a.box(x,base+hip,z,x,base+rise-1,z,WHITE);
        }
        a.box(x1+hip,base+(z2-z1)/4+1,middle,x2-hip,base+(z2-z1)/4+1,middle,SLAB);
        for(int z:new int[]{z1,z2})a.box(x1-1,base+1,z,x2+1,base+1,z,SLAB);
    }
}
