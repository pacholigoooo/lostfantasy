package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockStairs;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A forest gathering place beside the through-path, with a clear central dance floor. */
final class TanukiClearing {
    private static final GensokyoAtlas SITE=GensokyoAtlas.TANUKI_FOREST;
    static final int DX=55,DZ=20;
    private TanukiClearing() {}
    static boolean contains(int x,int z,int margin) {
        return Math.abs((long)x-SITE.x-DX)<32+margin && Math.abs((long)z-SITE.z-DZ)<26+margin
                && Math.hypot((x-SITE.x-DX)/(32.0+margin),(z-SITE.z-DZ)/(26.0+margin))<1;
    }
    static double shape(int x,int z,double height) {
        if(!contains(x,z,34))return height;
        double d=Math.hypot((x-SITE.x-DX)/32.0,(z-SITE.z-DZ)/26.0);
        return GensokyoNoise.lerp(SITE.y,height,GensokyoNoise.smooth((d-1)/1.05));
    }
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,SITE,DX,DZ,SITE.title);
        for(int x=-32;x<=32;x++)for(int z=-26;z<=26;z++) {
            double d=Math.hypot(x/32.0,z/26.0);if(d>=1)continue;
            a.box(x,1,z,x,38,z,AIR);
            if(d<.82)a.block(x,0,z,Math.floorMod(x*17+z*13,19)<3?
                    Blocks.DIRT.getStateFromMeta(1):Blocks.GRASS.getDefaultState());
        }
        // Stone and ash hold a decorative fire; no spreading fire blocks or tick entities.
        for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++) {
            double r=Math.hypot(x,z);if(r>4.3)continue;
            a.block(x,0,z,r<3?Blocks.COAL_BLOCK.getDefaultState():Blocks.COBBLESTONE.getDefaultState());
            if(r>=3.1)a.block(x,1,z,Blocks.STONE_SLAB.getStateFromMeta(3));
        }
        a.block(0,1,0,ModBlocks.WOODLAND_BONFIRE.getDefaultState());
        for(int x:new int[]{-18,18})for(int z:new int[]{-10,9}) {
            for(int i=-2;i<=2;i++)a.block(x+i,1,z,WOOD);
            for(int i:new int[]{-2,0,2}) {
                a.block(x+i,2,z,ModBlocks.LACQUER_BOWL.getDefaultState());
                a.block(x+i,1,z+2,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
                a.block(x+i,1,z-2,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
            }
        }
        for(int x:new int[]{-9,8})a.box(x,1,18,x+3,1,18,Blocks.LOG.getStateFromMeta(4));
        a.chest(-9,0,-20,"village_pantry");a.chest(-6,0,-20,"night_stall");
        a.block(8,1,-20,Blocks.CRAFTING_TABLE.getDefaultState());
        a.block(11,1,-20,Blocks.FURNACE.getDefaultState());
        for(int x:new int[]{-25,25}) {
            a.box(x,1,-2,x,4,-2,Blocks.OAK_FENCE.getDefaultState());
            a.block(x,5,-2,ModBlocks.RED_LANTERN.getDefaultState());
        }
        a.room("林间空地",0,0,9);a.room("宴席",18,0,5);a.room("备餐处",0,0,-18);
    }
}
