package dev.lostfantasy.world.gensokyo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Flush floor inlays and ceiling joinery; furniture and circulation stay in each room plan. */
final class InteriorFinishes {
    private InteriorFinishes() {}
    static void tatami(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int floor) {
        a.box(x1,floor,z1,x2,floor,z2,Blocks.WOOL.getStateFromMeta(13));
        for(int x=x1+1;x<x2;x+=5)for(int z=z1+1;z<z2;z+=7)
            a.box(x,floor,z,Math.min(x+3,x2-1),floor,Math.min(z+5,z2-1),Blocks.WOOL.getStateFromMeta(5));
    }
    static void rug(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int floor,int dye) {
        a.box(x1,floor,z1,x2,floor,z2,Blocks.WOOL.getStateFromMeta(12));
        a.box(x1+1,floor,z1+1,x2-1,floor,z2-1,Blocks.WOOL.getStateFromMeta(dye));
        for(int x:new int[]{x1+1,x2-1})for(int z:new int[]{z1+1,z2-1})
            a.block(x,floor,z,Blocks.WOOL.getStateFromMeta(4));
    }
    static void ceiling(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int y,IBlockState panel) {
        a.box(x1,y,z1,x2,y,z2,panel);
        for(int x=x1;x<=x2;x+=6)a.box(x,y,z1,x,y,z2,DARK);
        for(int z=z1;z<=z2;z+=6)a.box(x1,y,z,x2,y,z,DARK);
    }
}
