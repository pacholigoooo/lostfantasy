package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** The sealed, bare cherry. Its curved limbs are static chunk geometry. */
final class SaigyouAyakashi {
    private SaigyouAyakashi() {}
    static void build(GensokyoArchitecture a) {
        a.box(-69,-4,-281,69,0,-141,Blocks.DIRT.getDefaultState());
        a.box(-69,1,-281,69,89,-141,AIR);a.box(-69,0,-281,69,0,-141,Blocks.GRASS.getDefaultState());
        for(int y=1;y<=70;y++)disc(a,(int)Math.round(3*Math.sin(y*.05)),y,-211+(int)Math.round(y*.08),Math.max(2,8-y/10));
        for(int i=0;i<9;i++) {
            double angle=i*Math.PI*2/9+.2;int level=24+(i%3)*11;
            for(int t=0;t<=52;t++) {
                double b=angle+.35*Math.sin(t*.045);int x=(int)Math.round(Math.cos(b)*t),z=-211+(int)Math.round(Math.sin(b)*t);
                int y=level+(int)Math.round(.65*t-0.003*t*t);disc(a,x,y,z,Math.max(1,4-t/15));
                if(t==29 || t==43)for(int side:new int[]{-1,1})for(int n=0;n<=17;n++) {
                    int fx=x+(int)Math.round(Math.cos(b+side*.7)*n),fz=z+(int)Math.round(Math.sin(b+side*.7)*n);
                    disc(a,fx,y+n/2,fz,n<5?2:1);
                }
            }
            for(int n=0;n<24;n++)disc(a,(int)(Math.cos(angle)*n),Math.max(1,4-n/7),-211+(int)(Math.sin(angle)*n),Math.max(1,4-n/7));
        }
        for(int x=-32;x<=32;x++)for(int z=-32;z<=32;z++) {
            int r=x*x+z*z;if(r>=29*29 && r<=32*32)a.block(x,0,-211+z,STONE);
        }
        a.box(29,0,-215,69,0,-207,STONE);
        for(int x:new int[]{-11,11}) {
            a.box(x,7,-223,x,7,-199,ModBlocks.SHRINE_ROPE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.EAST));
            for(int z:new int[]{-223,-199})a.box(x,1,z,x,8,z,LOG);
        }
        for(int z:new int[]{-223,-199}) {
            a.box(-11,7,z,11,7,z,ModBlocks.SHRINE_ROPE.getDefaultState());
            for(int x:new int[]{-8,-4,0,4,8})a.block(x,6,z,ModBlocks.SHIDE.getDefaultState());
        }
        a.room("西行妖",35,0,-211);
    }
    private static void disc(GensokyoArchitecture a,int x,int y,int z,int r) {
        for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++)if(dx*dx+dz*dz<=r*r)
            a.block(x+dx,y,z+dz,Blocks.LOG.getStateFromMeta(12));
    }
}
