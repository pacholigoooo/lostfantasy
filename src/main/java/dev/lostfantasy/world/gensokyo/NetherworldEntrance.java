package dev.lostfantasy.world.gensokyo;

import net.minecraft.block.BlockStairs;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Open boundary gate and the long, twelve-flight approach to Hakugyokurou. */
final class NetherworldEntrance {
    private NetherworldEntrance() {}
    static void build(GensokyoBlueprint p) {
        GensokyoArchitecture a=new GensokyoArchitecture(p,GensokyoAtlas.NETHER_GATE);
        a.box(-23,-4,-22,23,0,26,STONE);a.box(-23,1,-22,23,34,26,AIR);
        frame(a,0,0);a.box(-6,0,0,6,0,28,Blocks.QUARTZ_BLOCK.getDefaultState());
        a.room("门前",0,0,20);
    }
    private static void frame(GensokyoArchitecture a,int floor,int z) {
        for(int x:new int[]{-13,13})for(int dz:new int[]{-9,9}) {
            a.box(x-1,floor+1,z+dz-1,x+1,floor+25,z+dz+1,LOG);
            a.box(x-2,floor+1,z+dz-2,x+2,floor+2,z+dz+2,STONE);
        }
        a.box(-15,floor+25,z-11,15,floor+27,z+11,DARK);
        a.gable(-18,z-14,18,z+14,floor+28);
        for(int x:new int[]{-8,8}) {a.box(x,floor+1,z-7,x,floor+17,z+7,DARK);a.box(x,floor+7,z-7,x,floor+8,z+7,Blocks.IRON_BLOCK.getDefaultState());}
        a.box(-5,floor+22,z+12,5,floor+23,z+12,DARK);
        a.sign(0,floor+22,z+13,EnumFacing.SOUTH,"幽明结界","");
        for(int x:new int[]{-19,19})a.lamp(x,floor+1,z+16);
    }
    static int stairFloor(int z) {
        int n=Math.max(0,Math.min(720,z-180));return -8*(n/60)-Math.min((n%60)/2,8);
    }
    static void approach(GensokyoArchitecture a) {
        for(int z=140;z<=950;z++) {
            int y=stairFloor(z);boolean step=z<900 && stairFloor(z+1)<y;
            a.box(-12,y-3,z,12,y,z,STONE);a.box(-10,y+1,z,10,y+8,z,AIR);
            if(step)a.box(-10,y,z,10,y,z,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
            for(int x:new int[]{-12,12}) {a.block(x,y+1,z,WHITE);a.block(x,y+2,z,SLAB);}
            if(z%30==0)for(int x:new int[]{-15,15}) {a.box(x-1,y-3,z-1,x+1,y,z+1,STONE);a.lamp(x,y+1,z);}
        }
        a.box(-25,-100,904,25,-96,951,STONE); // lower landing is level with the last flight
        a.box(-25,-95,904,25,-58,951,AIR);frame(a,-96,937);
        for(int z:new int[]{270,450,630,810}) {
            int y=stairFloor(z);
            for(int side:new int[]{-1,1}) {
                int x=side*24;a.box(x-9,y-3,z-10,x+9,y,z+10,STONE);a.box(x-9,y+1,z-10,x+9,y+10,z+10,AIR);
                for(int px:new int[]{x-7,x+7})for(int pz:new int[]{z-8,z+8})a.box(px,y+1,pz,px,y+7,pz,LOG);
                a.gable(x-10,z-11,x+10,z+11,y+8);
                a.box(x-6,y+1,z-6,x+6,y+1,z-6,Blocks.SPRUCE_STAIRS.getDefaultState());
                a.box(Math.min(side*10,x),y,z-3,Math.max(side*10,x),y,z+3,STONE);
                a.box(Math.min(side*10,x),y+1,z-3,Math.max(side*10,x),y+3,z+3,AIR);
                a.room("长阶歇脚亭",x,y,z);
            }
        }
        a.room("幽明门",0,-96,927);
    }
}
