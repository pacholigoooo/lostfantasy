package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Timber and plaster construction shared by the village's individually planned interiors. */
final class VillageJoinery {
    private VillageJoinery() {}
    static void house(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int floor,int storeys) {
        a.box(x1-1,0,z1-1,x2+1,floor-1,z2+1,STONE);
        int ceiling=floor+5*storeys;
        a.box(x1,floor,z1,x2,ceiling,z2,WHITE);a.box(x1+1,floor+1,z1+1,x2-1,ceiling-1,z2-1,AIR);
        for(int level=0;level<=storeys;level++)a.box(x1,floor+level*5,z1,x2,floor+level*5,z2,WOOD);
        for(int f=floor;f<ceiling;f+=5) {
            for(int x=x1;x<=x2;x+=5)for(int z:new int[]{z1,z2}) {
                a.box(x,f+1,z,x,f+4,z,LOG);
                if(x+3<x2)a.box(x+1,f+2,z,x+3,f+3,z,PAPER);
            }
            for(int z=z1;z<=z2;z+=5)for(int x:new int[]{x1,x2}) {
                a.box(x,f+1,z,x,f+4,z,LOG);
                if(z+3<z2)a.box(x,f+2,z+1,x,f+3,z+3,PAPER);
            }
        }
        if(z2-z1>(x2-x1)*1.25)a.gableZ(x1-2,z1-2,x2+2,z2+2,ceiling+1);
        else a.gable(x1-2,z1-2,x2+2,z2+2,ceiling+1);
    }
    static void wallZ(GensokyoArchitecture a,int x1,int x2,int z,int floor,int door) {
        a.box(x1,floor+1,z,x2,floor+4,z,WHITE);a.openZ(door,z,floor,1,3);
    }
    static void wallX(GensokyoArchitecture a,int x,int z1,int z2,int floor,int door) {
        a.box(x,floor+1,z1,x,floor+4,z2,WHITE);a.openX(x,door,floor,1,3);
    }
    static void lowDesk(GensokyoArchitecture a,int x,int floor,int z,int length) {
        for(int i=0;i<length;i++)a.block(x+i,floor+1,z,ModBlocks.WRITING_DESK.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
        a.box(x,floor+1,z+2,x+length-1,floor+1,z+2,Blocks.CARPET.getStateFromMeta(13));
    }
    static void teaTable(GensokyoArchitecture a,int x,int floor,int z,int length) {
        for(int i=0;i<length;i+=3) {
            a.block(x+i,floor+1,z,ModBlocks.TEA_TABLE.getDefaultState());
            a.block(x+i,floor+1,z+2,ModBlocks.FLOOR_CUSHION.getDefaultState());
        }
    }
    static void cabinet(GensokyoArchitecture a,int x,int floor,int z,EnumFacing facing) {
        a.block(x,floor+1,z,ModBlocks.DRAWER_CABINET.getDefaultState().withProperty(BlockHorizontal.FACING,facing));
    }
    static void lantern(GensokyoArchitecture a,int x,int floor,int z) {a.block(x,floor+4,z,ModBlocks.RED_LANTERN.getDefaultState());}
    static void maple(GensokyoArchitecture a,int x,int z,int height,int radius) {
        a.box(x,1,z,x+1,height,z+1,LOG);
        for(int[] d:new int[][]{{1,0},{-1,1},{0,-1}})for(int n=1;n<=radius-1;n++)
            a.block(x+d[0]*n,height-3+n/2,z+d[1]*n,Blocks.LOG.getStateFromMeta(12));
        for(int dy=-3;dy<=3;dy++)for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)
            if(dx*dx+dz*dz+dy*dy*2<=radius*radius && Math.floorMod(dx*11+dz*7+dy,13)!=0)
                a.block(x+dx,height+dy,z+dz,ModBlocks.MAPLE_LEAVES.getDefaultState());
    }
    static void pond(GensokyoArchitecture a,int x,int z,int rx,int rz) {
        for(int dx=-rx-2;dx<=rx+2;dx++)for(int dz=-rz-2;dz<=rz+2;dz++) {
            double d=dx*dx/(double)(rx*rx)+dz*dz/(double)(rz*rz);
            if(d<1) {a.box(x+dx,-2,z+dz,x+dx,-1,z+dz,STONE);a.block(x+dx,0,z+dz,Blocks.WATER.getDefaultState());}
            else if(d<1.2)a.block(x+dx,0,z+dz,Blocks.MOSSY_COBBLESTONE.getDefaultState());
        }
    }
}
