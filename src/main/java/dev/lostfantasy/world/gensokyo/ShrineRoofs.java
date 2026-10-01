package dev.lostfantasy.world.gensokyo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Thin roof courses used only by the two shrines. Other settlements keep their own joinery. */
final class ShrineRoofs {
    private ShrineRoofs() {}
    static void irimoya(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int base) {
        int skirt=8;
        for(int inset=0;inset<skirt;inset++) {
            int half=inset==0?1:Math.max(0,inset-1),y=base+half/2;
            IBlockState tile=tile(false,half);
            a.box(x1+inset,y,z1+inset,x2-inset,y,z1+inset,tile);
            a.box(x1+inset,y,z2-inset,x2-inset,y,z2-inset,tile);
            a.box(x1+inset,y,z1+inset,x1+inset,y,z2-inset,tile);
            a.box(x2-inset,y,z1+inset,x2-inset,y,z2-inset,tile);
        }
        int front=z2-skirt,back=z1+skirt;
        for(int z=back;z<=front;z++) {
            int half=skirt-1+Math.min(z-back,front-z)/2,y=base+half/2;
            a.box(x1+skirt,y,z,x2-skirt,y,z,tile(false,half));
            if(y>base+3)for(int x:new int[]{x1+skirt,x2-skirt}) {
                a.box(x,base+3,z,x,y-1,z,WHITE);
                if(z%5==0)a.box(x,base+3,z,x,y-1,z,LOG);
                if(half%2!=0)a.block(x,y,z,STONE);
            }
        }
        int middle=(front+back)/2,top=base+(skirt-1+(front-back)/4)/2;
        a.box(x1+skirt-1,top,middle,x2-skirt+1,top,middle,STONE);
        a.box(x1+skirt-1,top+1,middle,x2-skirt+1,top+1,middle,tile(false,0));
    }
    static void replaceGable(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int base,boolean ridgeZ,boolean timber) {
        clearGable(a,x1,z1,x2,z2,base);
        gable(a,x1,z1,x2,z2,base,ridgeZ,timber);
    }
    static void clearGable(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int base) {
        a.box(x1-1,base,z1-1,x2+1,base+Math.max(x2-x1,z2-z1)/2+3,z2+1,AIR);
    }
    static void gable(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int base,boolean ridgeZ,boolean timber) {
        int low=ridgeZ?x1:z1,high=ridgeZ?x2:z2;
        for(int p=low;p<=high;p++) {
            int distance=Math.min(p-low,high-p),half=distance==0?2:Math.max(1,distance-1),y=base+half/2;
            if(ridgeZ) {
                a.box(p,y,z1,p,y,z2,tile(timber,half));
                if(y>base && p>=x1+3 && p<=x2-3)for(int z:new int[]{z1+3,z2-3}) {
                    a.box(p,base,z,p,y-1,z,DARK);if(half%2!=0)a.block(p,y,z,timber?DARK:STONE);
                }
            } else {
                a.box(x1,y,p,x2,y,p,tile(timber,half));
                if(y>base && p>=z1+3 && p<=z2-3)for(int x:new int[]{x1+3,x2-3}) {
                    a.box(x,base,p,x,y-1,p,DARK);if(half%2!=0)a.block(x,y,p,timber?DARK:STONE);
                }
            }
        }
        int middle=(low+high)/2,top=base+Math.max(1,(high-low)/2-1)/2;
        if(ridgeZ) {
            a.box(middle,top,z1-1,middle,top,z2+1,timber?DARK:STONE);
            a.box(middle,top+1,z1-1,middle,top+1,z2+1,tile(timber,0));
        } else {
            a.box(x1-1,top,middle,x2+1,top,middle,timber?DARK:STONE);
            a.box(x1-1,top+1,middle,x2+1,top+1,middle,tile(timber,0));
        }
    }
    static void hakureiFront(GensokyoArchitecture a) {
        // The upper triangular pediment stands above a separate, softly arched entrance eave.
        gable(a,-10,-3,10,13,15,true,false);
        a.box(-7,14,10,7,14,10,DARK);
        a.box(-1,15,10,1,18,10,LOG);a.box(-6,16,10,6,16,10,LOG);
        for(int x=-10;x<=10;x++) {
            int distance=Math.abs(x),half=distance<=2?4:distance==3?3:distance==4?2:distance<=7?1:distance==8?2:3;
            int y=11+half/2;
            a.box(x,y,9,x,y,17,tile(false,half));
            a.block(x,y-1,17,DARK);
        }
        for(int x:new int[]{-10,10}) {
            a.box(x,4,14,x,10,14,LOG);
            a.box(x-1,10,13,x+1,10,15,DARK);
            a.box(x-2,11,13,x+2,11,15,DARK);
        }
    }
    static void canopy(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int base,boolean timber) {
        for(int z=z1;z<=z2;z++) {
            int half=(z2-z)/3;
            a.box(x1,base+half/2,z,x2,base+half/2,z,tile(timber,half));
        }
        a.box(x1,base-1,z2,x2,base-1,z2,DARK);
    }
    private static IBlockState tile(boolean timber,int half) {
        return (timber?Blocks.WOODEN_SLAB:Blocks.STONE_SLAB).getStateFromMeta(5+(half%2==0?0:8));
    }
}
