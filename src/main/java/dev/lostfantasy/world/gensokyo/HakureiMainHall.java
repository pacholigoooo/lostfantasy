package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockStairs;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** One main house: worship in front, an earthen kitchen and the living room behind. */
final class HakureiMainHall {
    private HakureiMainHall() {}
    static void build(GensokyoArchitecture a) {
        a.box(-26,0,-49,26,2,15,STONE);
        a.box(-22,3,-46,22,11,8,WHITE);a.box(-21,4,-45,21,10,7,AIR);
        a.box(-22,3,-46,22,3,8,WOOD);a.box(-22,11,-46,22,11,8,DARK);
        for(int z:new int[]{-46,8}) {
            a.box(-22,4,z,22,4,z,DARK);a.box(-22,10,z,22,10,z,DARK);
            for(int x:new int[]{-22,-16,-8,0,8,16,22})a.box(x,4,z,x,10,z,LOG);
            for(int x:new int[]{-19,-12,12,19})a.box(x-1,5,z,x+1,8,z,PAPER);
        }
        for(int x:new int[]{-22,22}) {
            a.box(x,4,-46,x,4,8,DARK);a.box(x,10,-46,x,10,8,DARK);
            for(int z=-46;z<=8;z+=9) {
                a.box(x,4,z,x,10,z,LOG);
                if(z+6<8)a.box(x,5,z+2,x,8,z+6,PAPER);
            }
        }
        a.box(-26,3,-16,26,3,15,WOOD);
        a.box(23,3,-49,27,3,8,WOOD);a.box(-1,3,-50,27,3,-47,WOOD);
        a.openZ(0,8,3,5,5);a.stairsSouth(0,16,0,3,6);
        a.openX(22,-29,3,2,4);a.openZ(12,-46,3,2,4);
        a.stairsSouth(25,-14,0,3,2);
        a.box(23,0,-12,28,0,20,Blocks.GRAVEL.getDefaultState());
        for(int x:new int[]{-25,25})for(int z:new int[]{-11,1,13}) {
            a.box(x,4,z,x,10,z,LOG);a.box(x-1,10,z,x+1,10,z,DARK);
        }
        a.box(-21,4,-18,21,10,-18,WHITE);a.openZ(-12,-18,3,2,4);a.openZ(11,-18,3,1,4);
        a.box(-1,1,-45,-1,10,-19,WHITE);a.openX(-1,-30,3,2,4);
        worship(a);kitchen(a);living(a);
        ShrineRoofs.irimoya(a,-29,-53,29,15,12);ShrineRoofs.hakureiFront(a);
        a.box(-16,9,9,16,9,9,ModBlocks.SHRINE_ROPE.getDefaultState());
        for(int x:new int[]{-12,-6,6,12})a.block(x,8,9,ModBlocks.SHIDE.getDefaultState());
        for(int x:new int[]{-16,16})a.box(x,4,9,x,10,9,LOG);
        a.block(0,4,12,ModBlocks.SAISEN_BOX.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
        a.block(0,10,12,Blocks.GOLD_BLOCK.getDefaultState());a.box(0,7,12,0,9,12,Blocks.OAK_FENCE.getDefaultState());
        a.room("拜殿",0,3,-3);a.room("正面缘侧",7,3,12);a.room("后侧缘侧",25,3,-23);
    }
    private static void worship(GensokyoArchitecture a) {
        a.box(-7,4,-15,7,4,-12,DARK);a.box(-4,5,-16,4,5,-15,WOOD);
        for(int x:new int[]{-6,6})a.block(x,5,-13,ModBlocks.LIBRARY_LAMP.getDefaultState());
        for(int side:new int[]{-1,1}) {
            int x=side<0?-13:6;
            a.box(x,4,-7,x+7,4,3,Blocks.CARPET.getStateFromMeta(13));
            a.chest(side*19,3,-13,"school_supplies");a.chest(side*19,3,-3,"village_pantry");
        }
        a.box(-21,4,-10,-19,5,-7,Blocks.HAY_BLOCK.getDefaultState());
        a.box(16,4,-10,20,5,-8,DARK);a.block(17,6,-9,Blocks.FLOWER_POT.getDefaultState());
        for(int x:new int[]{-13,13})for(int z:new int[]{-11,1})a.block(x,9,z,ModBlocks.RED_LANTERN.getDefaultState());
        a.room("祭具与储物",-16,3,-4);
    }
    private static void kitchen(GensokyoArchitecture a) {
        a.box(-21,0,-45,-2,0,-19,Blocks.DIRT.getStateFromMeta(1));a.box(-21,1,-45,-2,3,-19,AIR);
        a.openX(-22,-31,0,2,4);a.box(-26,0,-34,-22,0,-28,STONE);
        a.box(-26,1,-34,-22,4,-28,AIR);
        for(int rise=1;rise<=3;rise++) {
            int z=-23+rise;
            if(rise>1)a.box(-14,1,z,-10,rise-1,z,STONE);
            a.box(-14,rise,z,-10,rise,z,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
            int x=-6+rise;
            if(rise>1)a.box(x,1,-32,x,rise-1,-28,STONE);
            a.box(x,rise,-32,x,rise,-28,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.EAST));
        }
        a.box(-14,1,-19,-10,3,-19,WOOD);a.box(-2,1,-32,-2,3,-28,WOOD);
        a.box(-20,1,-44,-4,1,-42,STONE);
        a.block(-18,2,-43,Blocks.CAULDRON.getStateFromMeta(3));
        a.block(-20,2,-44,Blocks.IRON_BLOCK.getDefaultState());
        a.block(-20,3,-44,Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING,BlockLever.EnumOrientation.UP_X));
        a.block(-13,2,-43,Blocks.CRAFTING_TABLE.getDefaultState());
        a.block(-9,1,-42,Blocks.FURNACE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
        a.block(-6,2,-43,Blocks.CAULDRON.getDefaultState());
        a.box(-17,7,-45,-5,7,-45,Blocks.WOODEN_SLAB.getDefaultState());
        a.box(-12,5,-46,-8,7,-46,Blocks.IRON_BARS.getDefaultState());
        a.chest(-20,0,-36,"village_pantry");a.chest(-20,0,-24,"village_pantry");
        a.box(-21,1,-21,-19,2,-20,Blocks.HAY_BLOCK.getDefaultState());
        a.block(-18,1,-25,Blocks.CRAFTING_TABLE.getDefaultState());
        a.block(-14,8,-34,ModBlocks.RED_LANTERN.getDefaultState());
        a.room("土间与厨房",-14,0,-32);a.room("厨房备食",-11,0,-39);
    }
    private static void living(GensokyoArchitecture a) {
        a.box(1,4,-44,20,4,-20,Blocks.CARPET.getStateFromMeta(13));
        // The sleeping alcove opens into the tea room, with a short screen at its side.
        a.box(9,4,-44,9,7,-35,WHITE);a.box(10,4,-35,20,7,-35,WHITE);a.openZ(14,-35,3,2,3);
        a.bed(15,3,-40);a.chest(19,3,-43,"village_pantry");a.chest(3,3,-43,"school_supplies");
        a.box(2,4,-45,6,6,-44,DARK);
        VillageJoinery.teaTable(a,6,3,-28,7);
        VillageJoinery.cabinet(a,19,3,-37,EnumFacing.WEST);
        a.block(-17,1,-44,ModBlocks.KITCHEN_SHELF.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
        a.box(11,7,-19,16,7,-19,DARK);a.block(13,8,-19,Blocks.FLOWER_POT.getDefaultState());
        a.box(3,4,-20,5,4,-20,DARK);a.block(4,5,-20,Blocks.FLOWER_POT.getDefaultState());
        a.box(1,5,-46,7,9,-46,DARK);
        for(int dx=-2;dx<=2;dx++)for(int dy=-2;dy<=2;dy++)if(dx*dx+dy*dy<=4)a.block(4+dx,7+dy,-46,PAPER);
        a.block(5,9,-35,ModBlocks.RED_LANTERN.getDefaultState());a.block(15,9,-25,ModBlocks.RED_LANTERN.getDefaultState());
        a.room("起居间",12,3,-23);a.room("寝具间",14,3,-36);
    }
}
