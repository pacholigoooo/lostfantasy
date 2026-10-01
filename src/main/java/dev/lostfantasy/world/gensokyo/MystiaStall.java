package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Small wheeled grill stall beside the beast trail, with a lantern-lit counter. */
final class MystiaStall {
    private MystiaStall() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.MYSTIA);
        a.box(-9,0,-7,9,0,9,Blocks.GRAVEL.getDefaultState());
        a.box(-2,0,8,2,0,20,Blocks.GRAVEL.getDefaultState());
        a.box(-4,1,-3,4,1,3,DARK);a.box(-5,1,4,5,1,6,WOOD);
        // Wheels and axle are visible below the cart body; there is no moving entity.
        a.box(-5,0,-1,5,0,-1,Blocks.LOG.getStateFromMeta(5));
        for(int x:new int[]{-5,5}) {
            a.box(x,0,-2,x,2,0,DARK);a.block(x,1,-1,Blocks.IRON_BLOCK.getDefaultState());
        }
        for(int x:new int[]{-4,4})for(int z:new int[]{-3,3})a.box(x,2,z,x,5,z,LOG);
        a.box(-4,2,-3,4,3,-3,DARK);
        a.box(-3,2,3,3,2,3,WOOD);
        for(int x:new int[]{-2,2})a.block(x,3,3,Blocks.FLOWER_POT.getDefaultState());
        for(int x=-6;x<=6;x++)for(int z=-5;z<=4;z++) {
            int y=6+Math.max(0,2-Math.abs(z)/2);
            a.block(x,y,z,Blocks.WOOL.getStateFromMeta((x+12)%5==0?0:14));
        }
        for(int x=-3;x<=3;x+=3)a.box(x,5,4,x+1,6,4,Blocks.WOOL.getStateFromMeta(14));
        for(int x:new int[]{-5,5}) {
            a.block(x,4,4,ModBlocks.RED_LANTERN.getDefaultState());a.block(x,5,4,Blocks.OAK_FENCE.getDefaultState());
        }
        a.box(-1,5,-3,1,5,-3,DARK);a.sign(0,5,-4,EnumFacing.NORTH,"夜雀摊","");
        a.box(-1,5,4,1,5,4,DARK);a.sign(0,5,5,EnumFacing.SOUTH,"八目鳗","");
        // Grill, worktop and supplies can be reached from either side of the cart.
        a.box(-3,2,-2,-2,2,-2,STONE);
        a.block(-3,2,-1,Blocks.FURNACE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
        a.block(-2,2,-2,LIGHT);
        a.block(-2,3,-2,Blocks.IRON_TRAPDOOR.getDefaultState().withProperty(BlockTrapDoor.HALF,BlockTrapDoor.DoorHalf.BOTTOM));
        a.block(2,2,-2,Blocks.CRAFTING_TABLE.getDefaultState());
        a.block(3,2,-2,Blocks.CAULDRON.getStateFromMeta(3));a.chest(2,1,-1,"night_stall");
        a.stairsSouth(0,7,0,1,2);
        a.room("摊内操作台",0,1,0);a.room("柜台前",0,1,5);
        for(int x:new int[]{-2,2})a.block(x,2,5,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
        a.box(7,1,-3,8,1,0,WOOD);a.chest(7,1,-2);a.block(7,2,0,Blocks.HAY_BLOCK.getDefaultState());
        a.box(-10,1,1,-8,1,1,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
        a.room("路边歇脚处",-9,0,3);
    }
}
