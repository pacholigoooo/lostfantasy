package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A self-sufficient woodland home. The domestic plan is adapted for Minecraft, not an official floor plan. */
final class NemunoHome {
    private NemunoHome() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.NEMUNO);
        shell(a);inside(a);yard(a);trees(a);
    }
    private static void shell(GensokyoArchitecture a) {
        a.box(-17,0,-13,11,0,11,Blocks.COBBLESTONE.getDefaultState());
        a.box(-16,1,-12,10,6,10,Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(4));
        a.box(-15,2,-11,9,5,9,AIR);a.box(-16,1,-12,10,1,10,WOOD);a.box(-16,6,-12,10,6,10,DARK);
        for(int x:new int[]{-16,-9,-1,10})for(int z:new int[]{-12,10})a.box(x,1,z,x,5,z,LOG);
        for(int z:new int[]{-12,-4,3,10})for(int x:new int[]{-16,10})a.box(x,1,z,x,5,z,LOG);
        for(int x:new int[]{-13,-6,3})a.box(x,3,-12,x+2,4,-12,PAPER);
        a.box(-16,3,-2,-16,4,0,PAPER);a.box(-14,3,10,-5,4,10,PAPER);
        a.box(10,3,-8,10,4,-6,PAPER);
        // The east entrance and food preparation area retain an earthen floor.
        a.box(0,0,-11,9,0,9,Blocks.HARDENED_CLAY.getDefaultState());a.box(0,1,-11,9,5,9,AIR);
        a.openX(10,6,0,1,3);a.box(9,4,3,12,4,9,DARK);
        thatch(a,-19,-15,13,13,7);
        a.box(-16,1,11,-2,1,13,WOOD);a.openZ(-9,10,1,1,3);a.stairsSouth(-9,14,0,1,2);
        a.box(-17,5,10,-1,5,14,Blocks.WOODEN_SLAB.getStateFromMeta(1));
        for(int x:new int[]{-16,-2})a.box(x,2,13,x,4,13,LOG);
    }
    private static void thatch(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int base) {
        IBlockState straw=ModBlocks.THATCH.getDefaultState();int half=Math.min(x2-x1,z2-z1)/2;
        for(int n=0;n<=half;n++) {
            int y=base+n/2;
            a.box(x1+n,y,z1+n,x2-n,y,z1+n,straw);a.box(x1+n,y,z2-n,x2-n,y,z2-n,straw);
            a.box(x1+n,y,z1+n,x1+n,y,z2-n,straw);a.box(x2-n,y,z1+n,x2-n,y,z2-n,straw);
        }
        a.box(x1+half,base+half/2+1,(z1+z2)/2,x2-half,base+half/2+1,(z1+z2)/2,Blocks.LOG.getStateFromMeta(5));
    }
    private static void inside(GensokyoArchitecture a) {
        a.box(-1,1,-11,-1,5,9,WOOD);a.openX(-1,6,1,1,3);
        VillageJoinery.wallZ(a,-15,-2,-4,1,-10);VillageJoinery.wallX(a,-9,-11,-5,1,-7);
        a.bed(-13,1,-8);a.bed(-5,1,-8);
        a.chest(-13,1,-5,"village_pantry");a.chest(-3,1,-5,"village_pantry");
        a.box(-7,2,-11,-3,2,-11,WOOD);a.block(-5,3,-11,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.box(-14,2,2,-13,2,5,Blocks.WOODEN_SLAB.getStateFromMeta(1));
        a.box(-11,1,2,-9,1,4,Blocks.COBBLESTONE.getDefaultState());
        a.block(-10,2,3,Blocks.FURNACE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.EAST));
        a.box(-11,5,2,-9,5,4,Blocks.COBBLESTONE.getDefaultState());a.box(-10,6,3,-10,15,3,Blocks.COBBLESTONE.getDefaultState());
        VillageJoinery.lowDesk(a,-6,1,4,3);
        a.box(0,1,-3,9,5,-3,WOOD);a.openZ(4,-3,0,1,3);
        a.box(2,1,-10,8,1,-9,WOOD);a.block(3,2,-10,Blocks.CAULDRON.getStateFromMeta(3));
        a.block(8,1,-7,Blocks.FURNACE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.WEST));
        a.chest(2,0,-6,"village_pantry");a.block(8,1,-5,Blocks.CRAFTING_TABLE.getDefaultState());
        a.box(8,1,0,9,1,3,WOOD);a.block(8,2,1,ModBlocks.MEDICINE_TRAY.getDefaultState());
        for(int[] p:new int[][]{{-12,-7},{-4,-7},{-5,1},{4,-7},{4,4}}) {
            a.block(p[0],5,p[1],ModBlocks.RED_LANTERN.getDefaultState());
        }
        a.room("寝间",-12,1,-6);a.room("留宿间",-5,1,-6);a.room("炉边起居",-7,1,1);
        a.room("土间入口",4,0,6);a.room("厨房",5,0,-7);a.room("屋前缘侧",-6,1,12);
    }
    private static void yard(GensokyoArchitecture a) {
        a.box(-12,0,15,25,0,19,Blocks.GRAVEL.getDefaultState());a.box(11,0,3,25,0,15,Blocks.GRAVEL.getDefaultState());
        a.box(-2,0,17,2,0,40,Blocks.GRAVEL.getDefaultState());
        a.box(16,0,-10,28,0,9,Blocks.COBBLESTONE.getDefaultState());
        a.box(17,1,-9,27,5,8,WOOD);a.box(18,2,-8,26,4,7,AIR);a.box(17,5,-9,27,5,8,DARK);
        a.openZ(22,8,1,1,3);a.stairsSouth(22,9,0,1,2);
        thatch(a,15,-11,29,10,6);
        a.chest(19,1,-6,"nemuno_tools");a.chest(25,1,-6,"nemuno_tools");
        a.box(19,2,0,21,2,0,WOOD);a.block(20,3,0,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.block(25,2,0,Blocks.CRAFTING_TABLE.getDefaultState());a.block(22,4,2,ModBlocks.RED_LANTERN.getDefaultState());
        a.room("工具与干货",22,1,4);
        for(int x:new int[]{15,23})for(int dx=0;dx<5;dx++)for(int z=-24;z<=-16;z++) {
            if(dx==2 && z==-20) {a.block(x+dx,0,z,Blocks.WATER.getDefaultState());continue;}
            a.block(x+dx,0,z,Blocks.FARMLAND.getStateFromMeta(7));a.block(x+dx,1,z,Blocks.CARROTS.getStateFromMeta(7));
        }
        a.box(20,0,-25,22,0,-13,Blocks.GRAVEL.getDefaultState());a.box(21,0,-14,31,0,-12,Blocks.GRAVEL.getDefaultState());
        a.box(30,0,-13,32,0,13,Blocks.GRAVEL.getDefaultState());a.box(24,0,11,32,0,13,Blocks.GRAVEL.getDefaultState());
        a.room("菜畦小径",21,0,-19);
        a.box(-29,1,-6,-24,2,1,Blocks.LOG.getStateFromMeta(5));
        a.block(-26,1,7,LOG);a.block(-23,1,7,Blocks.CRAFTING_TABLE.getDefaultState());a.room("柴木工具台",-23,0,9);
        a.box(-27,0,10,-24,0,17,Blocks.GRAVEL.getDefaultState());a.box(-26,0,16,-11,0,18,Blocks.GRAVEL.getDefaultState());
        a.box(-30,0,21,-22,0,27,Blocks.MOSSY_COBBLESTONE.getDefaultState());a.box(-29,0,22,-23,0,26,Blocks.WATER.getDefaultState());
        a.lamp(14,1,16);a.lamp(29,1,-15);
    }
    private static void trees(GensokyoArchitecture a) {
        for(int[] t:new int[][]{{-29,-24,16,6},{-28,13,14,5},{-19,29,17,6},{12,29,15,6},{31,27,15,5},
                {-7,-29,16,6},{7,-29,14,5},{32,-29,15,4},{-33,31,17,6}})
            VillageJoinery.maple(a,t[0],t[1],t[2],t[3]);
    }
}
