package dev.lostfantasy.world.gensokyo;

import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;

/** Small, local planting details; the regional forest is generated independently. */
final class ForestDetails {
    private ForestDetails() {}
    static void tree(GensokyoArchitecture a,int x,int z,int height,int radius) {
        a.box(x,0,z,x+1,height,z+1,Blocks.LOG.getStateFromMeta(12));
        for(int d=1;d<6;d++)for(int side:new int[]{-1,1}) {
            a.box(x+side*d,0,z,x+side*d,Math.max(1,4-d),z+1,Blocks.LOG.getStateFromMeta(12));
            a.box(x,0,z+side*d,x+1,Math.max(1,4-d),z+side*d,Blocks.LOG.getStateFromMeta(12));
        }
        for(int[] direction:new int[][]{{1,0},{-1,-1},{0,1}}) {
            for(int d=1;d<=6;d++)a.box(x+direction[0]*d,height-10+d/2,z+direction[1]*d,
                    x+direction[0]*d+1,height-9+d/2,z+direction[1]*d+1,Blocks.LOG.getStateFromMeta(12));
            crown(a,x+direction[0]*5,z+direction[1]*5,height-5,Math.max(4,radius-3));
        }
        crown(a,x,z,height,radius);
    }
    private static void crown(GensokyoArchitecture a,int x,int z,int height,int radius) {
        for(int dy=-5;dy<=5;dy++) {
            double r=radius*Math.sqrt(Math.max(0,1-dy*dy/36.0));
            for(int dz=-(int)r;dz<=r;dz++) {
                int dx=(int)Math.sqrt(r*r-dz*dz);
                a.box(x-dx,height+dy,z+dz,x+dx,height+dy,z+dz,Blocks.LEAVES.getStateFromMeta(4));
            }
        }
    }
    static void vinesZ(GensokyoArchitecture a,int x1,int x2,int z,int bottom,int top,EnumFacing backing) {
        IBlockState vine=Blocks.VINE.getDefaultState().withProperty(BlockVine.getPropertyFor(backing),true);
        for(int x=x1;x<=x2;x+=3)for(int y=bottom;y<=top-Math.floorMod(x,4);y++)
            if(a.plan.at(a.site.x+x+backing.getXOffset(),a.site.y+y,a.site.z+z+backing.getZOffset()).isFullCube())a.block(x,y,z,vine);
    }
}
