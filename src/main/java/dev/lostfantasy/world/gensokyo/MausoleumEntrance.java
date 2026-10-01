package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** The abandoned underground approach; the inhabited dojo belongs to Miko's separate world. */
final class MausoleumEntrance {
    private MausoleumEntrance() {}
    static void build(GensokyoBlueprint p) {
        GensokyoArchitecture a=new GensokyoArchitecture(p,GensokyoAtlas.MYOUREN,0,0,"梦殿旧址");
        // A low opening behind the cemetery, followed by two opposing flights.
        a.box(-5,0,-91,5,6,-81,STONE);a.box(-3,1,-91,3,4,-81,AIR);
        a.box(-3,0,-82,3,0,-78,STONE);a.box(-6,7,-91,6,7,-81,SLAB);
        for(int t=0;t<27;t++)step(a,0,-83-t,-t-1,EnumFacing.SOUTH);
        a.box(-4,-28,-117,16,-27,-110,STONE);a.box(-3,-26,-116,15,-22,-110,AIR);
        for(int t=0;t<27;t++)step(a,12,-110+t,-28-t,EnumFacing.NORTH);
        a.box(8,-55,-83,17,-54,-74,STONE);a.box(9,-53,-83,16,-49,-74,AIR);
        // The chamber keeps its rock roof and a worn octagonal plinth, not a duplicate occupied temple.
        for(int x=-43;x<=43;x++)for(int z=-72;z<=21;z++) {
            double r=x*x/1849.0+(z+25)*(z+25)/2209.0;if(r>1)continue;
            int top=-48+(int)(25*Math.sqrt(1-r));
            a.box(x,-57,z,x,-55,z,STONE);a.box(x,-54,z,x,top,z,AIR);
            a.box(x,top+1,z,x,top+3,z,Blocks.STONE.getDefaultState());
            if(Math.floorMod(x*23+z*17,97)==0 && Math.abs(x)>27)a.block(x,top,z,Blocks.STAINED_GLASS.getStateFromMeta(Math.floorMod(x+z,16)));
        }
        a.box(9,-54,-78,15,-54,-65,STONE);a.box(9,-53,-78,15,-49,-65,AIR);
        for(int x=-19;x<=19;x++)for(int z=-19;z<=19;z++)if(Math.abs(x)+Math.abs(z)<28) {
            a.block(x,-55,z-23,Blocks.STONEBRICK.getStateFromMeta(Math.floorMod(x*3+z,17)==0?2:0));
            if(Math.abs(x)==19 || Math.abs(z)==19 || Math.abs(x)+Math.abs(z)==27)a.block(x,-54,z-23,SLAB);
        }
        for(int z:new int[]{-60,-39,-18,3})for(int x:new int[]{-27,27}) {
            a.box(x,-54,z,x,-53,z,STONE);a.block(x,-52,z,ModBlocks.LIBRARY_LAMP.getDefaultState());
        }
        for(int side:new int[]{-1,1}) {
            int x=side*35;
            a.box(x-3,-55,-33,x+3,-48,-9,STONE);a.box(x-2,-54,-32,x+2,-49,-10,AIR);
            a.openX(x-side*3,-21,-55,2,4);
            a.chest(x,-55,-29,"hieda_records");a.chest(x,-55,-13,"kourindou_tools");
            a.room("旧藏龛",x,-55,-21);
        }
        a.box(-7,-54,8,7,-47,10,DARK);a.box(-3,-54,8,3,-49,10,AIR);
        a.box(-3,-55,3,3,-55,16,STONE);a.box(-3,-54,11,3,-49,16,AIR);
        a.sign(0,-48,7,EnumFacing.NORTH,"仙界","神灵庙");
        a.room("墓地下行口",0,0,-80);a.room("折返石阶",6,-27,-113);
        a.room("旧殿台基",0,-55,-23);a.room("仙界入口",0,-55,9);
    }
    private static void step(GensokyoArchitecture a,int x,int z,int floor,EnumFacing facing) {
        a.box(x-4,floor-2,z,x+4,floor+6,z,STONE);
        a.box(x-3,floor+1,z,x+3,floor+5,z,AIR);
        a.box(x-3,floor,z,x+3,floor,z,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(net.minecraft.block.BlockStairs.FACING,facing));
        if(Math.floorMod(z,9)==0)a.block(x+4,floor+3,z,Blocks.GLOWSTONE.getDefaultState());
    }
}
