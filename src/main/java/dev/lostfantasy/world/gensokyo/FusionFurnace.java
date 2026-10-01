package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Block-built containment hall and a fixed luminous core; lighting stays in the chunk pipeline. */
final class FusionFurnace {
    private static final int F=BlazingHell.DECK;
    private static final IBlockState FRAME=ModBlocks.COLUMNAR_BASALT.getDefaultState(),
            METAL=Blocks.IRON_BLOCK.getDefaultState(),RAIL=Blocks.IRON_BARS.getDefaultState(),
            VIOLET=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(11),
            GOLD=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(4);
    private FusionFurnace() {}
    static void build(GensokyoArchitecture region) {
        GensokyoArchitecture a=new GensokyoArchitecture(region.plan,OldHellWorld.ORIGIN,0,BlazingHell.CORE_Z,"核融合炉");
        // Floor and lava vessel; the floor is a ring and a five-pointed network of bridges.
        for(int x=-75;x<=75;x++)for(int z=-75;z<=75;z++) {
            if(!octagon(x,z,75))continue;
            a.box(x,F-20,z,x,F+140,z,AIR);
            boolean outer=octagon(x,z,58) && !octagon(x,z,54);
            if(octagon(x,z,58)) {
                a.box(x,F-21,z,x,F-19,z,FRAME);
                a.box(x,F-18,z,x,F-13,z,Blocks.LAVA.getDefaultState());
                if(outer)a.box(x,F-18,z,x,F+136,z,VIOLET);
            }
            if(!octagon(x,z,43) || star(x,z))a.box(x,F-2,z,x,F,z,FRAME);
            // Three structural rings, with walkable inner and outer galleries.
            for(int level:new int[]{0,48,96})if(!octagon(x,z,43)) {
                a.box(x,F+level-2,z,x,F+level,z,METAL);
                if(!octagon(x,z,74) || octagon(x,z,44) && (level!=0 || !star(x,z)))a.block(x,F+level+1,z,RAIL);
            }
            if(outer) {
                for(int band:new int[]{-8,8,56,104,132})a.box(x,F+band,z,x,F+band+2,z,FRAME);
                boolean cardinal=Math.abs(x)<=32 || Math.abs(z)<=32;
                if(cardinal)for(int y:new int[]{16,64,112})a.box(x,F+y,z,x,F+y+17,z,Blocks.STAINED_GLASS.getStateFromMeta(4));
                if(Math.abs(x)%12<2 || Math.abs(z)%12<2) {
                    a.box(x,F+9,z,x,F+132,z,GOLD);
                    for(int y:new int[]{29,77,125})a.block(x,F+y,z,LIGHT);
                }
            }
        }
        // The star's open edges receive barriers; all junctions stay open.
        for(int x=-42;x<=42;x++)for(int z=-42;z<=42;z++)if(star(x,z)) {
            boolean edge=!star(x-1,z) || !star(x+1,z) || !star(x,z-1) || !star(x,z+1);
            if(edge)a.block(x,F+1,z,RAIL);
        }
        // Cardinal entrances connect the whole landscape. North opens directly onto a star tip.
        for(int z=-76;z<=-39;z++) {a.box(-3,F-2,z,3,F,z,METAL);a.box(-3,F+1,z,3,F+7,z,AIR);}
        for(int z=39;z<=76;z++) {a.box(-3,F-2,z,3,F,z,METAL);a.box(-3,F+1,z,3,F+7,z,AIR);}
        for(int x:new int[]{-58,58})a.box(x-4,F+1,-4,x+4,F+8,4,AIR);
        for(int z:new int[]{-58,58})a.box(-4,F+1,z-4,4,F+8,z+4,AIR);
        for(int x:new int[]{-75,75})a.box(x,F+1,-4,x,F+4,4,AIR);
        for(int level:new int[]{48,96})a.box(-62,F+level+1,-1,-53,F+level+7,9,AIR);
        // Radial buttresses carry the rings to the cave floor, outside the doors.
        for(int[] p:new int[][]{{-65,-28},{-65,28},{65,-28},{65,28},{-28,-65},{28,-65},{-28,65},{28,65}}) {
            a.box(p[0]-2,-79,p[1]-2,p[0]+2,F+137,p[1]+2,FRAME);
            for(int band:new int[]{8,56,104,133})a.box(p[0]-3,F+band,p[1]-3,p[0]+3,F+band+2,p[1]+3,METAL);
            a.block(p[0],F+137,p[1],Blocks.REDSTONE_LAMP.getDefaultState());
        }
        core(a);stairs(a);
        a.room("星形炉底桥",-5,F,-36);a.room("下层环廊",0,F,-68);
        for(int level:new int[]{48,96})a.room(level==48?"中层观察环廊":"上层检修环廊",0,F+level,-68);
        for(int i=0;i<5;i++) {
            double angle=-Math.PI/2+i*Math.PI*2/5;
            a.room("星桥接点"+(i+1),(int)Math.round(50*Math.cos(angle)),F,(int)Math.round(50*Math.sin(angle)));
        }
    }
    private static boolean octagon(int x,int z,int r) {return Math.max(Math.abs(x),Math.abs(z))<=r && Math.abs(x)+Math.abs(z)<=Math.round(r*1.41421356);}
    private static boolean star(int x,int z) {
        for(int i=0;i<5;i++) {
            double aa=-Math.PI/2+i*Math.PI*2/5,bb=aa+Math.PI*4/5;
            double ax=50*Math.cos(aa),az=50*Math.sin(aa),dx=50*Math.cos(bb)-ax,dz=50*Math.sin(bb)-az;
            double t=Math.max(0,Math.min(1,((x-ax)*dx+(z-az)*dz)/(dx*dx+dz*dz)));
            if(Math.hypot(x-ax-t*dx,z-az-t*dz)<=4)return true;
        }
        return false;
    }
    private static void core(GensokyoArchitecture a) {
        // A solid emissive sphere avoids transparent layers crossing each other under shaders.
        int radius=24,cy=F+72;
        for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++) {
            int h2=radius*radius-x*x-z*z;if(h2<0)continue;
            int height=(int)Math.floor(Math.sqrt(h2));
            a.box(x,cy-height,z,x,cy+height,z,LIGHT);
            if((x*3+z*5)%17==0) {
                a.block(x,cy-height,z,Blocks.MAGMA.getDefaultState());a.block(x,cy+height,z,Blocks.MAGMA.getDefaultState());
            }
        }
        // A suspended containment crown leaves an open central bore above the core.
        for(int x=-61;x<=61;x++)for(int z=-61;z<=61;z++)if(octagon(x,z,61) && !octagon(x,z,29)) {
            a.box(x,F+135,z,x,F+138,z,FRAME);
            if(!octagon(x,z,59) || octagon(x,z,31))a.block(x,F+139,z,GOLD);
        }
    }
    private static void stairs(GensokyoArchitecture a) {
        a.box(-110,F-4,-32,-77,F+106,12,FRAME);a.box(-109,F+1,-31,-78,F+105,11,AIR);
        for(int level:new int[]{0,48,96}) {
            a.box(-108,F+level,0,-77,F+level,11,METAL);
            BlazingHell.bridge(a,-100,5,-54,5,F+level,3);
            a.room("西检修梯台"+level,-100,F+level,5);
        }
        for(int level:new int[]{0,48}) {
            a.box(-104,F+level+24,-29,-81,F+level+24,-25,METAL);
            a.stairsSouth(-99,-24,F+level,F+level+24,3);
            for(int rise=1;rise<=24;rise++) {
                int z=-25+rise;
                if(rise>1)a.box(-89,F+level+25,z,-83,F+level+23+rise,z,FRAME);
                a.box(-89,F+level+24+rise,z,-83,F+level+24+rise,z,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
            }
            for(int z=-24;z<=-1;z++) {
                int north=F+level-z,south=F+level+49+z;
                a.box(-102,north+1,z,-96,north+4,z,AIR);a.box(-89,south+1,z,-83,south+4,z,AIR);
                for(int x:new int[]{-103,-95})a.block(x,north+1,z,RAIL);
                for(int x:new int[]{-90,-82})a.block(x,south+1,z,RAIL);
            }
            a.box(-104,F+level+25,-30,-81,F+level+25,-30,RAIL);
            a.room("折返检修梯"+level,-92,F+level+24,-27);
        }
        for(int y=F+7;y<=F+103;y+=16) {
            a.box(-110,y,-21,-110,y+8,-7,Blocks.STAINED_GLASS.getStateFromMeta(5));
            a.block(-109,y+5,-28,ModBlocks.LIBRARY_LAMP.getDefaultState());
        }
        // Side entrance to the tower is cut after the enclosing wall.
        a.box(-110,F+1,1,-107,F+7,9,AIR);
    }
}
