package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A modest shelter and low wooden pier on the continuous riverbank. */
final class SanzuLanding {
    private SanzuLanding() {}
    static void build(GensokyoBlueprint p) {
        GensokyoArchitecture a=new GensokyoArchitecture(p,GensokyoAtlas.SANZU_PIER);
        a.box(-15,-4,-26,35,0,35,STONE);a.box(-15,1,-26,35,22,35,AIR);
        a.box(-14,0,-25,34,0,35,Blocks.GRAVEL.getDefaultState());
        VillageJoinery.house(a,7,-22,30,-4,1,1);a.openZ(18,-4,1,2,3);a.stairsSouth(18,-3,0,1,4);
        a.table(11,1,-15,9);a.chest(10,1,-19,"castle_crafts");a.chest(26,1,-19,"village_pantry");
        a.block(26,2,-9,Blocks.CRAFTING_TABLE.getDefaultState());VillageJoinery.lantern(a,18,1,-13);
        a.box(9,2,-7,22,2,-7,Blocks.SPRUCE_STAIRS.getDefaultState());a.room("候船屋",18,1,-9);
        a.box(-9,-4,-4,-5,0,4,STONE);
        for(int i=0;i<4;i++) {
            int x=-10-i;a.box(x,-5,-4,x,-i,4,STONE);
            a.box(x,-i,-4,x,-i,4,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(net.minecraft.block.BlockStairs.FACING,EnumFacing.EAST));
            a.box(x,1-i,-4,x,6,4,AIR);
        }
        a.box(-71,-4,-4,-14,-4,4,WOOD);a.box(-71,-3,-4,-14,6,4,AIR);
        for(int x=-65;x<=-17;x+=8)for(int z:new int[]{-4,4}) {
            a.box(x,-13,z,x,-4,z,LOG);a.block(x,-3,z,Blocks.DARK_OAK_FENCE.getDefaultState());a.block(x,-2,z,ModBlocks.RIVER_LANTERN.getDefaultState());
        }
        a.sign(18,4,-3,EnumFacing.SOUTH,"三途河渡口","");
        a.room("此岸",0,0,19);a.room("栈桥",-45,-4,0);a.room("登船处",-70,-4,0);
        a.box(10,3,-15,19,3,-15,Blocks.WOODEN_SLAB.getStateFromMeta(9));
        for(int x:new int[]{12,16})a.block(x,4,-15,ModBlocks.LACQUER_BOWL.getDefaultState());
        a.box(8,5,-21,29,5,-21,DARK);
        GardenScenery.bench(a,19,0,27,9,EnumFacing.SOUTH);
        for(int[] rock:new int[][]{{28,10},{31,18},{23,32}}) {
            a.block(rock[0],0,rock[1],Blocks.MOSSY_COBBLESTONE.getDefaultState());
            a.block(rock[0],1,rock[1],Blocks.STONE_SLAB.getStateFromMeta(3));
        }
        for(int x=-65;x<=-17;x+=8)a.box(x,-4,-3,x,-4,3,DARK);
        farLanding(p);
    }
    private static void farLanding(GensokyoBlueprint p) {
        int dx=dev.lostfantasy.world.HiganTerrain.farBankX()-GensokyoAtlas.SANZU_PIER.x;
        GensokyoArchitecture a=new GensokyoArchitecture(p,GensokyoAtlas.SANZU_PIER,dx,0,"彼岸渡口");
        a.box(-30,-12,-12,-5,0,12,STONE);a.box(-30,1,-12,-5,14,12,AIR);
        a.box(-30,0,-12,-5,0,12,Blocks.GRAVEL.getDefaultState());
        a.box(0,-4,-4,51,-4,4,WOOD);a.box(0,-3,-4,51,6,4,AIR);
        for(int i=0;i<4;i++) {
            int x=-1-i;a.box(x,-12,-4,x,-3+i,4,STONE);
            a.box(x,-3+i,-4,x,-3+i,4,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(net.minecraft.block.BlockStairs.FACING,EnumFacing.WEST));
            a.box(x,-2+i,-4,x,6,4,AIR);
        }
        for(int x=5;x<=45;x+=8)for(int z:new int[]{-4,4}) {
            a.box(x,-13,z,x,-4,z,LOG);a.block(x,-3,z,Blocks.DARK_OAK_FENCE.getDefaultState());
            a.block(x,-2,z,ModBlocks.RIVER_LANTERN.getDefaultState());
        }
        // A low, open shelter leaves the flower field visible from the landing.
        for(int x:new int[]{-24,-12})for(int z:new int[]{-10,-5})a.box(x,1,z,x,4,z,LOG);
        a.box(-26,5,-12,-10,5,-3,Blocks.WOODEN_SLAB.getStateFromMeta(1));
        a.box(-24,1,-9,-12,1,-9,Blocks.SPRUCE_STAIRS.getDefaultState());
        VillageJoinery.lantern(a,-18,1,-6);
        GardenScenery.bench(a,-19,0,8,9,EnumFacing.SOUTH);
        GardenScenery.rocks(a,-25,7,1);
        for(int x=5;x<=45;x+=8)a.box(x,-4,-3,x,-4,3,DARK);
        a.room("登船处",50,-4,0);a.room("候船亭",-18,0,-7);a.room("花原入口",-29,0,0);
    }
}
