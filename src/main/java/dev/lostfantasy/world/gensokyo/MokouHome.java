package dev.lostfantasy.world.gensokyo;

import net.minecraft.init.Blocks;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A small bamboo-forest home and covered rest space; the floor plan is an adaptation. */
final class MokouHome {
    private MokouHome() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.MOKOU);
        VillageJoinery.house(a,-13,-9,9,11,1,1);
        a.box(-17,7,-12,13,16,14,AIR);
        for(int z=-11;z<=13;z++) {
            int rise=(12-Math.abs(z-1))/2;
            a.box(-16,7+rise,z,12,7+rise,z,Blocks.HAY_BLOCK.getDefaultState());
            if(rise>0)for(int x:new int[]{-13,9})a.box(x,7,z,x,6+rise,z,WOOD);
        }
        a.box(-14,1,12,11,1,15,WOOD);a.openZ(0,11,1,2,3);a.stairsSouth(0,16,0,1,3);
        a.box(-2,0,17,2,0,34,Blocks.GRAVEL.getDefaultState());
        a.box(-12,2,-1,8,5,-1,WOOD);a.openZ(1,-1,1,1,3);
        a.bed(-8,1,-5);a.chest(5,1,-7,"village_pantry");VillageJoinery.lowDesk(a,-10,1,5,5);
        a.box(4,2,4,8,2,8,STONE);a.block(6,3,6,Blocks.FURNACE.getDefaultState());
        a.block(8,3,6,Blocks.CAULDRON.getStateFromMeta(3));a.box(8,3,4,8,13,4,Blocks.COBBLESTONE.getDefaultState());
        VillageJoinery.lantern(a,-3,1,4);VillageJoinery.lantern(a,-2,1,-5);
        a.room("起居与灶台",1,1,6);a.room("寝间",0,1,-5);a.room("门前歇脚处",-7,1,13);
        // The side store is deliberately lower than the house and has its own entrance.
        VillageJoinery.house(a,13,-7,23,5,1,1);a.openZ(18,5,1,1,3);a.stairsSouth(18,6,0,1,2);
        a.chest(15,1,-5,"kourindou_tools");a.chest(21,1,-5,"night_stall");a.block(21,2,2,Blocks.CRAFTING_TABLE.getDefaultState());
        a.box(13,0,8,23,0,16,Blocks.GRAVEL.getDefaultState());a.box(3,0,16,23,0,19,Blocks.GRAVEL.getDefaultState());
        a.room("工具与干粮",18,1,0);VillageJoinery.lantern(a,18,1,0);
        a.box(-24,0,13,-18,0,21,STONE);a.block(-21,1,17,Blocks.FURNACE.getDefaultState());
        a.box(-24,1,20,-19,1,20,WOOD);a.box(-21,0,21,-2,0,23,Blocks.GRAVEL.getDefaultState());
        a.box(-25,1,-9,-20,2,-4,Blocks.LOG.getStateFromMeta(8));
        a.box(-24,1,0,-19,1,6,Blocks.HAY_BLOCK.getDefaultState());
        for(int x:new int[]{-25,-20,21,26})for(int z:new int[]{-22,-16})
            BambooGrove.gardenCulm(a,x,z,14+Math.floorMod(x+z,5),net.minecraft.util.EnumFacing.NORTH);
    }
}
