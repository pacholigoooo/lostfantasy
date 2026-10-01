package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Eientei's cultivated peonies, identified in Oriental Sacred Place chapter 14. */
final class PeonyField {
    private PeonyField() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.PEONY_FIELD);
        a.box(-33,1,-30,33,5,31,AIR);
        a.box(-2,0,-29,2,0,39,Blocks.GRAVEL.getDefaultState());
        a.box(-29,0,8,29,0,10,Blocks.GRAVEL.getDefaultState());
        a.box(-29,0,-12,29,0,-10,Blocks.GRAVEL.getDefaultState());
        for(int x:new int[]{-28,8})for(int z:new int[]{-28,-8,12}) {
            int width=x==8 && z==12?10:20;
            bed(a,x,z,width,z==12?14:15);
            a.room("花畦小径",x+width/2,0,z-1);
        }
        for(int x=-31;x<=31;x++)for(int z:new int[]{-31,30})
            if(Math.abs(x)>3)a.block(x,1,z,Blocks.SPRUCE_FENCE.getDefaultState());
        for(int z=-30;z<30;z++)for(int x:new int[]{-31,31})a.block(x,1,z,Blocks.SPRUCE_FENCE.getDefaultState());
        // A compact work shelter holds the harvest tools and drying trays.
        a.box(21,0,13,30,0,27,Blocks.COBBLESTONE.getDefaultState());
        a.box(21,1,13,30,1,27,WOOD);
        a.box(21,2,13,30,5,13,WOOD);a.box(30,2,13,30,5,27,WOOD);
        for(int z:new int[]{13,27})a.box(21,2,z,21,5,z,LOG);
        a.box(21,5,13,30,5,27,DARK);a.gableZ(19,11,32,29,6);
        a.stairsSouth(25,28,0,1,2);
        a.chest(23,1,14,"peony_tools");a.chest(29,1,18,"peony_tools");
        a.box(28,2,21,29,2,25,WOOD);
        for(int z:new int[]{21,23,25})a.block(28,3,z,ModBlocks.MEDICINE_TRAY.getDefaultState());
        a.block(24,4,20,ModBlocks.RED_LANTERN.getDefaultState());
        a.room("收存与晾药",25,1,23);
        a.box(4,1,31,4,3,31,LOG);a.sign(4,3,32,EnumFacing.SOUTH,"芍药田","永远亭管理");
        for(int x:new int[]{-33,33})for(int z:new int[]{-33,33}) {
            a.box(x,1,z,x,8,z,LOG);
            for(int dy=-2;dy<=2;dy++)for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)
                if(dx*dx+dz*dz+dy*dy*2<13)a.block(x+dx,8+dy,z+dz,Blocks.LEAVES.getStateFromMeta(4));
        }
    }
    private static void bed(GensokyoArchitecture a,int x,int z,int width,int depth) {
        a.box(x,0,z,x+width-1,0,z+depth-1,Blocks.DIRT.getDefaultState());
        for(int dx=0;dx<width;dx+=2)for(int dz=0;dz<depth;dz+=2) {
            if((dx+dz*3)%17==1)continue;
            a.block(x+dx,1,z+dz,Blocks.DOUBLE_PLANT.getStateFromMeta(5));
            a.block(x+dx,2,z+dz,Blocks.DOUBLE_PLANT.getStateFromMeta(8));
        }
    }
}
