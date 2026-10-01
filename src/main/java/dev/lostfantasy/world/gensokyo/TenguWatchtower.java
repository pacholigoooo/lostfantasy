package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

final class TenguWatchtower {
    private TenguWatchtower() {}
    static void build(GensokyoBlueprint plan) {
        for(TenguLayout.Plot p:new TenguLayout.Plot[]{TenguLayout.WEST_TOWER,TenguLayout.EAST_TOWER}) {
            GensokyoArchitecture a=TenguLayout.at(plan,p);TenguJoinery.house(a,p);
            for(int x:new int[]{-7,0,7})a.chest(x,0,-9,"tengu_patrol");
            VillageJoinery.lowDesk(a,-3,0,-3,6);a.room("备勤间",0,0,4);
            TenguHomes.shelves(a,-8,6,-9,17);VillageJoinery.lowDesk(a,-4,6,-3,7);
            a.room("值守间",0,6,3);
            a.block(0,13,-4,ModBlocks.TENGU_CAMERA.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.NORTH));
            a.box(-7,13,1,-4,13,1,WOOD);a.block(-6,14,1,ModBlocks.RESEARCH_NOTES.getDefaultState());
            a.room("瞭望间",0,12,3);a.room("巡山望廊",0,12,p.d+2);
            for(int side:new int[]{-1,1}) {
                a.box(side*(p.w-2),19,0,side*(p.w-2),23,0,TenguJoinery.POST);
                a.box(side*(p.w-2),22,1,side*(p.w-2),24,3,Blocks.WOOL.getStateFromMeta(11));
            }
        }
    }
}
