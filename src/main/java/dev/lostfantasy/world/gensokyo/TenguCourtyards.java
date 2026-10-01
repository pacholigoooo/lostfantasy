package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Small working courtyards and planted overlooks occupy the spaces between the cliff houses. */
final class TenguCourtyards {
    private TenguCourtyards() {}
    static void build(GensokyoBlueprint plan) {
        drying(plan);photoDeck(plan);gardens(plan);
        for(TenguLayout.Plot p:TenguLayout.HOMES) {
            GensokyoArchitecture a=TenguLayout.at(plan,p);
            for(int side:new int[]{-1,1}) {
                int x=side*(p.w-2);
                a.box(x-1,1,p.d+2,x+1,1,p.d+3,DARK);
                a.block(x,1,p.d+2,Blocks.GRASS.getDefaultState());
                a.block(x,2,p.d+2,p.form%2==0?ModBlocks.MAPLE_LEAVES.getDefaultState():Blocks.RED_FLOWER.getDefaultState());
            }
        }
    }
    private static void deck(GensokyoArchitecture a,TenguLayout.Plot p,int x1,int z1,int x2,int z2) {
        a.box(x1,-1,z1,x2,-1,z2,DARK);a.box(x1,0,z1,x2,0,z2,WOOD);a.box(x1,1,z1,x2,13,z2,AIR);
        for(int x:new int[]{x1,x2})for(int z:new int[]{z1,z2})a.box(x,55-p.y,z,x,-2,z,TenguJoinery.POST);
        a.box(x1,1,z1,x2,1,z1,TenguJoinery.RAIL);a.box(x1,1,z2,x2,1,z2,TenguJoinery.RAIL);
        a.box(x1,1,z1,x1,1,z2,TenguJoinery.RAIL);a.box(x2,1,z1,x2,1,z2,TenguJoinery.RAIL);
    }
    private static void drying(GensokyoBlueprint plan) {
        TenguLayout.Plot p=TenguLayout.PRINT;GensokyoArchitecture a=TenguLayout.at(plan,p);
        deck(a,p,p.w+1,-10,p.w+17,12);a.openX(p.w,4,0,2,4);a.openX(p.w+1,4,0,2,4);
        for(int x:new int[]{p.w+4,p.w+14})for(int z:new int[]{-7,0,8}) {
            a.box(x,1,z,x,5,z,TenguJoinery.POST);a.box(x-1,4,z,x+1,4,z,PAPER);
        }
        a.box(p.w+6,1,-7,p.w+10,1,-5,WOOD);a.block(p.w+8,2,-6,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.box(p.w+5,6,-8,p.w+13,6,-8,Blocks.LOG2.getStateFromMeta(5));
        for(int x:new int[]{p.w+5,p.w+13})a.box(x,1,-8,x,5,-8,TenguJoinery.POST);
        a.box(p.w+4,7,-10,p.w+14,7,-7,Blocks.WOODEN_SLAB.getStateFromMeta(5));
        a.chest(p.w+7,0,-9,"tengu_printing");a.chest(p.w+11,0,-9,"tengu_printing");
        a.room("晒纸院",p.w+8,0,4);
    }
    private static void photoDeck(GensokyoBlueprint plan) {
        GensokyoArchitecture a=TenguLayout.at(plan,TenguLayout.NEWS);
        a.box(34,0,-4,53,0,12,WOOD);a.box(34,1,-4,53,7,12,AIR);
        for(int x:new int[]{36,52})for(int z:new int[]{-3,11}) {
            a.box(x,55-TenguLayout.NEWS.y,z,x,-1,z,TenguJoinery.POST);
            a.block(x,1,z,TenguJoinery.RAIL);
        }
        a.box(53,1,-4,53,1,12,TenguJoinery.RAIL);a.box(34,1,-4,53,1,-4,TenguJoinery.RAIL);a.box(34,1,12,53,1,12,TenguJoinery.RAIL);
        a.openX(33,4,0,2,4);a.room("临谷取景台",42,0,4);
        for(int x:new int[]{38,47}) {
            a.box(x,1,9,x+3,1,9,DARK);a.box(x,2,10,x+3,2,10,TenguJoinery.RAIL);
        }
        a.block(49,1,1,ModBlocks.TENGU_CAMERA.getDefaultState()
                .withProperty(net.minecraft.block.BlockHorizontal.FACING,net.minecraft.util.EnumFacing.EAST));
        a.box(35,1,-2,39,1,-2,WOOD);a.block(37,2,-2,ModBlocks.RESEARCH_NOTES.getDefaultState());
    }
    private static void gardens(GensokyoBlueprint plan) {
        TenguLayout.Plot p=TenguLayout.COUNCIL;GensokyoArchitecture upper=TenguLayout.at(plan,p);
        int edge=-p.w;
        deck(upper,p,edge-20,-5,edge-1,17);upper.openX(edge,9,0,2,4);upper.openX(edge-1,9,0,2,4);
        upper.box(edge-18,0,-3,edge-3,0,6,Blocks.GRAVEL.getDefaultState());
        for(int[] rock:new int[][]{{-15,0,4},{-7,3,3},{-15,5,2}})
            upper.box(edge+rock[0]-1,1,rock[1]-1,edge+rock[0]+1,rock[2],rock[1]+1,Blocks.MOSSY_COBBLESTONE.getDefaultState());
        planter(upper,edge-15,12,ModBlocks.PURPLE_CHERRY_LEAVES.getDefaultState());upper.room("会馆石庭",edge-7,0,9);
        p=TenguLayout.TEA;GensokyoArchitecture tea=TenguLayout.at(plan,p);
        deck(tea,p,-p.w,p.d+7,p.w,p.d+20);
        tea.box(-3,0,p.d+5,3,0,p.d+21,WOOD);tea.box(-3,1,p.d+5,3,5,p.d+21,AIR);
        planter(tea,-p.w+6,p.d+14,ModBlocks.MAPLE_LEAVES.getDefaultState());
        planter(tea,p.w-6,p.d+14,ModBlocks.CHERRY_LEAVES.getDefaultState());
        VillageJoinery.lowDesk(tea,9,0,p.d+14,6);tea.room("茶院望涧席",8,0,p.d+10);
        for(int x:new int[]{10,13})tea.block(x,2,p.d+14,ModBlocks.LACQUER_BOWL.getDefaultState());
        // The central entrance remains clear; a bench follows the far railing.
        GardenScenery.bench(tea,-8,0,p.d+18,6,net.minecraft.util.EnumFacing.SOUTH);
    }
    private static void planter(GensokyoArchitecture a,int x,int z,IBlockState leaves) {
        a.box(x-2,0,z-2,x+2,1,z+2,STONE);a.box(x-1,1,z-1,x+1,1,z+1,Blocks.GRASS.getDefaultState());
        a.box(x,2,z,x,8,z,TenguJoinery.POST);
        for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++)for(int dy=-2;dy<=2;dy++)
            if(dx*dx+dz*dz+dy*dy*3<=17)a.block(x+dx,9+dy,z+dz,leaves);
    }
}
