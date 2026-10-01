package dev.lostfantasy.world.gensokyo;

import net.minecraft.init.Blocks;
import net.minecraft.block.BlockStairs;
import net.minecraft.util.EnumFacing;

/** Low garden stones and pruned pines, placed on the residence's graded ground. */
final class GardenScenery {
    private GardenScenery() {}
    static void bench(GensokyoArchitecture a,int x,int floor,int z,int length,EnumFacing facing) {
        a.box(x,floor+1,z,x+length-1,floor+1,z,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,facing));
    }
    /** A small open timber arbour, with a planted lattice instead of a solid roof. */
    static void arbour(GensokyoArchitecture a,int x,int z,int halfWidth,int halfDepth) {
        a.box(x-halfWidth,0,z-halfDepth,x+halfWidth,0,z+halfDepth,Blocks.GRAVEL.getDefaultState());
        for(int xx:new int[]{x-halfWidth,x+halfWidth})for(int zz:new int[]{z-halfDepth,z+halfDepth})
            a.box(xx,1,zz,xx,5,zz,Blocks.LOG.getStateFromMeta(1));
        for(int zz:new int[]{z-halfDepth,z+halfDepth})
            a.box(x-halfWidth-1,5,zz,x+halfWidth+1,5,zz,Blocks.LOG.getStateFromMeta(5));
        for(int xx=x-halfWidth-1;xx<=x+halfWidth+1;xx+=2) {
            a.box(xx,6,z-halfDepth-1,xx,6,z+halfDepth+1,Blocks.LOG.getStateFromMeta(9));
            for(int zz=z-halfDepth;zz<=z+halfDepth;zz++)if(Math.floorMod(xx*3+zz,7)<4)
                a.block(xx,7,zz,Blocks.LEAVES.getStateFromMeta(4));
        }
        bench(a,x-halfWidth+2,0,z-halfDepth+1,halfWidth*2-3,EnumFacing.NORTH);
    }
    static void plantedBed(GensokyoArchitecture a,int x,int z,int rx,int rz,int flower) {
        for(int dx=-rx;dx<=rx;dx++)for(int dz=-rz;dz<=rz;dz++) {
            double distance=dx*dx/(double)(rx*rx)+dz*dz/(double)(rz*rz);
            if(distance>1)continue;
            a.block(x+dx,0,z+dz,Blocks.DIRT.getStateFromMeta(2));
            int pattern=Math.floorMod(dx*17+dz*7+x,11);
            if(distance>.72 && pattern<7)a.block(x+dx,1,z+dz,Blocks.MOSSY_COBBLESTONE.getDefaultState());
            else if(distance<.7 && pattern<4)a.block(x+dx,1,z+dz,Blocks.RED_FLOWER.getStateFromMeta(flower));
            else if(distance<.72 && pattern>7)a.block(x+dx,1,z+dz,Blocks.TALLGRASS.getStateFromMeta(2));
        }
    }
    static void rocks(GensokyoArchitecture a,int x,int z,int facing) {
        stone(a,x,z,2,2,4);
        stone(a,x+facing*3,z+1,2,2,2);
        stone(a,x-facing*2,z+3,2,1,1);
    }
    private static void stone(GensokyoArchitecture a,int x,int z,int rx,int rz,int height) {
        for(int dx=-rx;dx<=rx;dx++)for(int dz=-rz;dz<=rz;dz++) {
            double d=dx*dx/(double)(rx*rx+1)+dz*dz/(double)(rz*rz+1);
            if(d>=1)continue;
            int top=Math.max(1,(int)Math.round(height*Math.sqrt(1-d)));
            for(int y=0;y<=top;y++)a.block(x+dx,y,z+dz,y<2 && Math.floorMod(dx+dz,3)!=0
                    ?Blocks.MOSSY_COBBLESTONE.getDefaultState():Blocks.STONE.getStateFromMeta(5));
        }
    }
    static void pine(GensokyoArchitecture a,int x,int z,int facing) {
        a.box(x,1,z,x,5,z,Blocks.LOG.getStateFromMeta(1));
        a.box(x+facing,5,z,x+facing,9,z,Blocks.LOG.getStateFromMeta(1));
        for(int n=1;n<=4;n++)a.block(x+facing*n,5+n/2,z+1,Blocks.LOG.getStateFromMeta(13));
        crown(a,x+facing*4,7,z+1,4,3);
        crown(a,x-facing*2,8,z-1,3,3);
        crown(a,x+facing,10,z,3,2);
    }
    private static void crown(GensokyoArchitecture a,int x,int y,int z,int rx,int rz) {
        for(int dx=-rx;dx<=rx;dx++)for(int dz=-rz;dz<=rz;dz++)for(int dy=-1;dy<=1;dy++)
            if(dx*dx/(double)(rx*rx)+dz*dz/(double)(rz*rz)+dy*dy*.48<=1)
                a.block(x+dx,y+dy,z+dz,Blocks.LEAVES.getStateFromMeta(5));
    }
}
