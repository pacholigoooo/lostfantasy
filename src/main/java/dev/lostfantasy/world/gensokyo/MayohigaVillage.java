package dev.lostfantasy.world.gensokyo;

import net.minecraft.block.BlockLog;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A deserted mountain village, with irregular lanes, old fields and shelter for cats. */
final class MayohigaVillage {
    static final int[][] CATS=cats();
    private static final int[][] LANES={
            {0,117,0,91,3},{0,91,28,77,2},{28,77,28,35,2},{28,35,16,-6,2},
            {16,-6,16,-46,2},{16,-46,-8,-49,2},{-8,-49,-8,-55,2},
            {-8,-49,-78,-49,2},{-78,-49,-78,-55,2},{16,-46,68,-46,2},{68,-46,68,-49,2},
            {-66,-20,16,-20,2},{16,-18,40,-18,2},{40,-18,65,-12,2},
            {-15,34,28,35,2},{-57,43,-42,43,2},{-42,43,-42,75,2},{-42,75,0,91,2},
            {28,48,60,48,2}};
    private MayohigaVillage() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.MAYOHIGA);
        a.box(-124,1,-108,124,4,110,AIR);
        fields(a);
        for(int[] path:LANES)lane(a,path);
        MayohigaHomes.build(plan);well(a);gate(a);
        a.room("猫群庭院",7,0,-27);a.room("旧田埂",0,0,64);
        for(int x:new int[]{-118,118})for(int z:new int[]{-98,-52,-2,48,95})tree(a,x,z);
        for(int x:new int[]{-96,-52,42,96})tree(a,x,-103);
        for(int x:new int[]{-52,52})tree(a,x,103);
        tree(a,-49,-40);tree(a,52,18);
        for(int[] wall:new int[][]{{-108,73,-75,73},{44,83,98,83},{-41,-97,19,-97}}) {
            a.box(wall[0],0,wall[1],wall[2],0,wall[3],Blocks.MOSSY_COBBLESTONE.getDefaultState());
            for(int x=wall[0];x<=wall[2];x++)if(Math.floorMod(x,11)<7)a.block(x,1,wall[1],Blocks.COBBLESTONE_WALL.getDefaultState());
        }
    }
    private static void fields(GensokyoArchitecture a) {
        for(int x=-33;x<=20;x++)for(int z=53;z<=78;z++) {
            if(Math.abs(x)<=2)continue;
            a.block(x,0,z,Blocks.DIRT.getStateFromMeta(z%4==0?1:0));
            int scatter=Math.floorMod(x*11+z*7,19);
            if(scatter<3)a.block(x,1,z,Blocks.TALLGRASS.getStateFromMeta(scatter==0?2:1));
        }
        a.box(-34,0,51,22,0,51,Blocks.MOSSY_COBBLESTONE.getDefaultState());
        for(int x:new int[]{-34,22})for(int z:new int[]{53,60,69,78}) {
            a.block(x,1,z,LOG);
            if(z!=69)a.block(x,2,z,Blocks.SPRUCE_FENCE.getDefaultState());
        }
        // The fields retain their edges and a worn foot strip through the middle.
        a.box(-2,0,53,2,0,78,Blocks.GRAVEL.getDefaultState());
        a.box(-2,0,79,26,0,81,Blocks.GRAVEL.getDefaultState());
    }
    private static void lane(GensokyoArchitecture a,int[] path) {
        int length=Math.max(Math.abs(path[2]-path[0]),Math.abs(path[3]-path[1]));
        for(int i=0;i<=length;i++) {
            int x=(int)Math.round(path[0]+(path[2]-path[0])*(double)i/length);
            int z=(int)Math.round(path[1]+(path[3]-path[1])*(double)i/length);
            for(int dx=-path[4];dx<=path[4];dx++)for(int dz=-path[4];dz<=path[4];dz++)if(dx*dx+dz*dz<=path[4]*path[4]+1) {
                a.block(x+dx,0,z+dz,Blocks.GRAVEL.getDefaultState());a.box(x+dx,1,z+dz,x+dx,3,z+dz,AIR);
            }
        }
    }
    private static void well(GensokyoArchitecture a) {
        a.box(-32,-2,-35,-24,1,-27,Blocks.MOSSY_COBBLESTONE.getDefaultState());
        a.box(-30,-1,-33,-26,0,-29,Blocks.WATER.getDefaultState());a.box(-30,1,-33,-26,1,-29,AIR);
        for(int x:new int[]{-33,-23})a.box(x,1,-31,x,6,-31,LOG);
        a.box(-34,7,-36,-22,7,-26,SLAB);a.box(-33,6,-31,-23,6,-31,DARK);
        a.box(-37,1,-11,-28,1,-11,Blocks.SPRUCE_STAIRS.getDefaultState());
        for(int x:new int[]{-18,-14})a.block(x,1,-34,Blocks.CAULDRON.getStateFromMeta(1));
        a.room("井庭",-28,0,-24);
    }
    private static void gate(GensokyoArchitecture a) {
        for(int x:new int[]{-6,6}) {
            a.block(x,0,92,STONE);a.box(x,1,92,x,9,92,RED);a.box(x,1,92,x,2,92,DARK);
        }
        a.box(-8,7,92,8,7,92,RED);a.box(-10,9,91,10,9,93,RED);a.box(-10,10,91,10,10,93,DARK);
        for(int x:new int[]{-10,10})a.box(x,11,91,x,11,93,DARK);
        a.room("村口鸟居",0,0,94);
    }
    private static void tree(GensokyoArchitecture a,int x,int z) {
        int height=11+Math.floorMod(x+z,4);
        a.box(x,1,z,x,height,z,LOG);
        for(int side:new int[]{-1,1})for(int n=1;n<=4;n++)a.block(x+side*n,height-3+n/2,z+side,
                LOG.withProperty(BlockLog.LOG_AXIS,BlockLog.EnumAxis.X));
        for(int center:new int[]{-3,3})for(int dy=-3;dy<=3;dy++)for(int dx=-5;dx<=5;dx++)for(int dz=-5;dz<=5;dz++)
            if(dx*dx+dz*dz+dy*dy*2<27 && Math.floorMod(dx*11+dz*7+dy,17)!=0)
                a.block(x+center+dx,height+dy,z+dz,Blocks.LEAVES.getStateFromMeta(4));
    }
    private static int[][] cats() {
        int[][] positions=new int[12][3];GensokyoAtlas site=GensokyoAtlas.MAYOHIGA;
        for(int i=0;i<8;i++) {
            MayohigaHomes.House house=MayohigaHomes.HOUSES[i];BlockPos p=house.world(-4,2,house.d+2);
            positions[i]=new int[]{p.getX()-site.x,p.getY()-site.y,p.getZ()-site.z};
        }
        positions[8]=new int[]{-12,1,-22};positions[9]=new int[]{7,1,-27};
        positions[10]=new int[]{28,1,30};positions[11]=new int[]{0,1,91};return positions;
    }
}
