package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStairs;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Weathered alpine houses, with a dragon pipe marking the furnished gaming room. */
final class KomakusaDen {
    private KomakusaDen() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.FALSE_HEAVEN);
        a.box(-48,1,-41,48,24,41,AIR);
        a.box(-5,0,9,5,0,47,Blocks.GRAVEL.getDefaultState());
        a.box(-43,0,14,34,0,18,Blocks.GRAVEL.getDefaultState());
        mainHouse(a);backRooms(a);kitchen(a);oldHouses(a);
        for(int[] p:new int[][]{{-22,26},{21,27},{-37,-28}})a.lamp(p[0],0,p[1]);
        for(int x=-19;x<=18;x+=5)a.block(x,1,31,ModBlocks.KOMAKUSA.getDefaultState());
        grounds(a);
        a.room("棚上小径",0,0,35);a.room("旧屋前庭",-33,0,16);
    }
    private static void mainHouse(GensokyoArchitecture a) {
        a.hall(-17,-13,16,11,1,6,LOG);
        a.box(-19,1,12,18,1,15,WOOD);a.openZ(0,11,1,2,4);
        for(int x=-3;x<=3;x++)a.block(x,1,16,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
        a.box(-5,6,15,5,6,15,LOG);a.box(-5,2,15,-5,5,15,LOG);a.box(5,2,15,5,5,15,LOG);
        a.block(0,5,15,ModBlocks.DRAGON_PIPE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
        // Tables, cushions and trays occupy the hall, with an aisle through the centre.
        for(int x:new int[]{-11,10})for(int z:new int[]{-7,4}) {
            a.box(x-4,1,z-3,x+4,1,z+3,Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(4));
            for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)if(dx!=0 || dz!=0)
                a.block(x+dx,2,z+dz,ModBlocks.WRITING_DESK.getDefaultState());
            a.block(x,2,z,ModBlocks.MOUNTAIN_GAMING_TABLE.getDefaultState());
            for(int dx:new int[]{-3,3})for(int dz:new int[]{-2,0,2})a.block(x+dx,2,z+dz,Blocks.CARPET.getStateFromMeta(10));
            a.box(x-1,2,z+3,x+1,2,z+3,Blocks.CARPET.getStateFromMeta(10));
            a.block(x,6,z,ModBlocks.RED_LANTERN.getDefaultState());
            a.room("桌边"+x+"/"+z,x,1,z+2);
        }
        for(int x:new int[]{-5,4})for(int z:new int[]{-7,4}) {
            a.box(x,2,z-2,x,3,z+2,WOOD);a.box(x,4,z-2,x,4,z+2,PAPER);
        }
        for(int x:new int[]{-15,14})a.box(x,2,-2,x,2,0,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,x<0?EnumFacing.EAST:EnumFacing.WEST));
        a.box(-5,2,-10,5,2,-9,WOOD);a.block(-2,3,-9,ModBlocks.MOUNTAIN_GAMING_TABLE.getDefaultState());
        a.box(-7,2,-12,-6,4,-10,Blocks.BOOKSHELF.getDefaultState());
        a.box(6,2,-12,7,3,-10,WOOD);
        a.chest(13,1,-10,"komakusa_supplies");a.chest(-14,1,-10,"komakusa_supplies");
        a.openZ(0,-13,1,1,3);a.openX(16,0,1,1,3);
        // Patched plaster and darkened corners retain the worn exterior without exposing the main room.
        for(int x:new int[]{-15,-9,7,13})a.block(x,2,11,Blocks.STONEBRICK.getStateFromMeta(2));
        a.block(-17,3,-10,Blocks.MOSSY_COBBLESTONE.getDefaultState());a.block(16,2,-9,Blocks.MOSSY_COBBLESTONE.getDefaultState());
        for(int x=-16;x<16;x++)if(Math.floorMod(x,6)!=1 && Math.abs(x)>2) {
            a.block(x,2,11,DARK);
            if(Math.floorMod(x,5)==0)a.box(x,3,11,x,4,11,WOOD);
        }
        for(int x=-19;x<=18;x++)for(int z=12;z<=14;z++)if(Math.floorMod(x*17+z*11,31)<3)
            a.block(x,8+(15-Math.abs(z+1))/2,z,Blocks.MOSSY_COBBLESTONE.getDefaultState());
        a.room("正厅",0,1,3);a.room("门廊",0,1,13);
    }
    private static void backRooms(GensokyoArchitecture a) {
        VillageJoinery.house(a,-17,-29,16,-14,1,1);a.openZ(0,-14,1,1,3);
        VillageJoinery.wallX(a,-4,-28,-15,1,-20);VillageJoinery.wallX(a,5,-28,-15,1,-20);
        VillageJoinery.wallZ(a,-16,-5,-21,1,-10);
        VillageJoinery.wallX(a,-11,-28,-22,1,-26);
        a.bed(-13,1,-23);a.bed(-8,1,-23);a.chest(-13,1,-17,"village_pantry");
        a.box(-15,2,-27,-12,2,-27,WOOD);a.box(-9,2,-27,-6,2,-27,WOOD);
        a.box(-16,2,-19,-16,4,-16,Blocks.BOOKSHELF.getDefaultState());a.box(-7,2,-18,-6,2,-16,WOOD);
        VillageJoinery.lowDesk(a,8,1,-23,5);a.chest(13,1,-17,"komakusa_supplies");
        a.box(8,2,-27,13,3,-27,Blocks.BOOKSHELF.getDefaultState());
        a.box(7,2,-16,11,3,-16,WOOD);a.box(-2,2,-26,-2,2,-23,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.EAST));
        for(int x:new int[]{-10,0,10})VillageJoinery.lantern(a,x,1,-20);
        a.room("歇脚寝间",-10,1,-20);a.room("筹具收存",10,1,-19);a.room("后侧走廊",0,1,-21);
        a.room("西寝位",-14,1,-25);a.room("东寝位",-7,1,-25);
    }
    private static void kitchen(GensokyoArchitecture a) {
        VillageJoinery.house(a,17,-13,30,11,1,1);a.openX(17,0,1,1,3);a.openZ(23,11,1,1,3);
        for(int x=21;x<=25;x++)a.block(x,1,12,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
        VillageJoinery.wallZ(a,18,29,-4,1,23);
        a.box(19,2,-10,27,2,-9,WOOD);a.block(21,3,-9,Blocks.CRAFTING_TABLE.getDefaultState());a.block(27,2,-6,Blocks.FURNACE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.WEST));
        a.block(19,2,-7,Blocks.CAULDRON.getStateFromMeta(3));a.chest(27,1,-11,"village_pantry");
        a.box(29,2,-11,29,4,-7,WOOD);a.box(19,2,-2,20,2,2,WOOD);
        VillageJoinery.lowDesk(a,19,1,6,8);a.chest(27,1,2,"komakusa_supplies");
        a.box(28,2,5,29,3,9,WOOD);
        VillageJoinery.lantern(a,23,1,-8);VillageJoinery.lantern(a,23,1,3);
        a.room("茶食厨房",23,1,-7);a.room("备茶间",23,1,2);
    }
    private static void oldHouses(GensokyoArchitecture a) {
        VillageJoinery.house(a,-45,-13,-27,7,1,1);a.openZ(-35,7,1,1,3);
        for(int x=-37;x<=-33;x++)a.block(x,1,8,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
        a.box(-43,6,-11,-38,14,-6,AIR);a.box(-45,2,-11,-45,4,-8,AIR);
        a.box(-43,2,-10,-40,2,-8,Blocks.MOSSY_COBBLESTONE.getDefaultState());
        a.box(-30,2,-10,-29,3,-6,WOOD);a.chest(-30,1,3,"yamawaro_tools");
        a.room("旧屋储物",-34,1,1);
        // The second old house is a low foundation and surviving timber frame, not a sealed shell.
        a.box(31,0,20,45,1,35,STONE);a.box(32,2,21,44,2,21,WOOD);a.box(44,2,22,44,3,33,WHITE);
        for(int x:new int[]{32,44})a.box(x,2,34,x,6,34,LOG);
        a.box(32,7,34,44,7,34,LOG);a.box(32,8,33,44,8,34,ROOF);
        for(int x=35;x<=39;x++)a.block(x,1,36,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
        a.block(33,2,23,ModBlocks.KOMAKUSA.getDefaultState());a.block(33,1,23,Blocks.GRASS.getDefaultState());
        a.room("旧屋台基",37,1,28);
    }
    private static void grounds(GensokyoArchitecture a) {
        for(int[] b:new int[][]{{-24,-33,4},{36,-25,5},{-25,30,3},{19,37,3}}) {
            for(int x=-b[2];x<=b[2];x++)for(int z=-b[2];z<=b[2];z++) {
                double d=(x*x+z*z)/(double)(b[2]*b[2]);
                if(d<1) {
                    int h=(int)Math.floor((1-d)*3);
                    a.box(b[0]+x,0,b[1]+z,b[0]+x,h,b[1]+z,Blocks.STONE.getStateFromMeta(5));
                }
                else if(d<1.65 && Math.floorMod(x*7+z*13,3)==0) {
                    a.block(b[0]+x,0,b[1]+z,Blocks.GRASS.getDefaultState());a.block(b[0]+x,1,b[1]+z,ModBlocks.KOMAKUSA.getDefaultState());
                }
            }
        }
        a.box(-41,0,9,-38,0,13,Blocks.GRAVEL.getDefaultState());
        a.box(32,0,18,36,0,19,Blocks.GRAVEL.getDefaultState());
    }
}
