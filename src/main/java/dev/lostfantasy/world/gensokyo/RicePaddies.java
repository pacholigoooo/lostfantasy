package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.ChunkPrimer;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Three low terraces northwest of the village. Every water trench has a solid bed and banks. */
final class RicePaddies {
    // Each row has its own contour; unequal end fields follow the narrowing shore-side land.
    static final int[][] BANDS={{-382,-282,85,-568,84},{-266,-166,84,-552,105},{-150,-58,83,-518,128}};
    static final int[][] DIVISIONS={{-600,-456},{-442,-332},{-294,-188},{-174,-54},{-40,150}};
    private RicePaddies() {}

    static boolean near(int x,int z,int margin) {return x>=-590-margin && x<=145+margin && z>=-448-margin && z<=-46+margin;}
    static boolean treeless(int x,int z) {return near(x,z,12);}

    static double shape(int x,int z,double h) {
        if(!near(x,z,20))return h;
        for(int[] b:BANDS) {
            double d=distance(x,z,b[3]-2,b[0]-5,b[4]+2,b[1]+2);
            if(d<14)h=GensokyoNoise.lerp(h,b[2]+1,1-GensokyoNoise.smooth(Math.max(0,d)/14));
        }
        double yard=distance(x,z,-242,-440,-151,-394);
        if(yard<14)h=GensokyoNoise.lerp(h,86,1-GensokyoNoise.smooth(Math.max(0,yard)/14));
        return h;
    }
    private static double distance(int x,int z,int x1,int z1,int x2,int z2) {
        return Math.max(Math.max(x1-x,x-x2),Math.max(z1-z,z-z2));
    }

    static Sample at(int x,int z) {
        if(!near(x,z,0))return null;
        for(int row=0;row<BANDS.length;row++) {
            int[] b=BANDS[row];int level=b[2];
            if(z<b[0]-5 || z>b[1] || x<b[3]-2 || x>b[4]+2)continue;
            // A continuous header ditch feeds each parcel through a narrow opening in its bank.
            if(z==b[0]-4 && x>=b[3]-1 && x<=b[4]+1)return new Sample(level-1,level,false,0);
            for(int col=0;col<DIVISIONS.length;col++) {
                int x1=Math.max(b[3],DIVISIONS[col][0]),x2=Math.min(b[4],DIVISIONS[col][1]);
                if(x<x1 || x>x2)continue;
                int inlet=(x1+x2)/2;
                if(x==inlet && z<=b[0]+3)return new Sample(level-1,level,false,0);
                if(z<b[0])continue;
                if(x-x1<2 || x2-x<2 || z-b[0]<2 || b[1]-z<2)return new Sample(level+1,-1,false,0);
                // Narrow planted ridges sit among actual water blocks, without simulated water quads.
                boolean crop=Math.floorMod(x-x1,3)==0;
                int age=row==0 && col==4?2:row==1 && col==0?4:7;
                return new Sample(level-(crop?0:1),crop?-1:level,crop,age);
            }
        }
        return null;
    }

    static void paint(ChunkPrimer p,int cx,int cz,GensokyoTerrain terrain) {
        int ox=cx<<4,oz=cz<<4;if(!near(ox+8,oz+8,10))return;
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int wx=ox+x,wz=oz+z;Sample s=at(wx,wz);if(s==null)continue;
            GensokyoTerrain.Column c=terrain.column(wx,wz);
            if(c.path() || c.ground!=s.ground)continue;
            if(s.crop) {
                p.setBlockState(x,c.ground,z,Blocks.FARMLAND.getStateFromMeta(7));
                p.setBlockState(x,c.ground+1,z,((BlockCrops)ModBlocks.RICE_CROP).withAge(s.age));
            } else if(c.wet())p.setBlockState(x,c.ground,z,Blocks.CLAY.getDefaultState());
            else {
                p.setBlockState(x,c.ground,z,Blocks.GRASS_PATH.getDefaultState());
                p.setBlockState(x,c.ground+1,z,AIR);
            }
        }
    }

    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.RICE_FIELDS,60,-204,"稻田农具院");
        a.box(-44,-3,-20,45,1,26,Blocks.DIRT.getDefaultState());
        a.box(-44,2,-20,45,21,26,AIR);
        a.box(-43,1,-19,44,1,25,Blocks.GRAVEL.getDefaultState());
        // Raised grain store and an open work bay share a timber roof.
        a.box(-15,1,-12,15,1,10,Blocks.COBBLESTONE.getDefaultState());
        a.box(-14,2,-11,14,2,9,WOOD);
        for(int x:new int[]{-14,-5,5,14})for(int z:new int[]{-11,9})a.box(x,3,z,x,8,z,LOG);
        a.box(-14,3,-11,14,7,-11,WOOD);a.box(-14,3,-10,-14,7,9,WOOD);
        a.box(-5,3,-10,-5,7,9,WOOD);a.box(-14,3,9,-5,7,9,WOOD);a.openZ(-10,9,2,1,3);
        a.box(-14,6,-9,-14,6,-4,Blocks.OAK_FENCE.getDefaultState());
        a.box(-15,8,-12,15,8,-12,DARK);a.box(-15,8,10,15,8,10,DARK);
        a.gable(-18,-15,18,13,9);
        a.stairsSouth(-10,10,1,2,2);a.stairsSouth(7,10,1,2,4);
        a.chest(-12,2,-8,"rice_farming");a.chest(-8,2,-8,"rice_farming");
        a.box(-13,3,-3,-12,4,3,Blocks.HAY_BLOCK.getDefaultState());
        a.block(-8,3,1,Blocks.CRAFTING_TABLE.getDefaultState());
        a.block(11,3,-8,Blocks.FURNACE.getDefaultState());a.chest(7,2,-8,"rice_farming");
        a.table(-1,2,-7,5);a.box(0,3,4,3,3,4,Blocks.WOODEN_SLAB.getStateFromMeta(1));
        a.block(10,3,-3,Blocks.CAULDRON.getStateFromMeta(3));a.block(11,7,8,ModBlocks.RED_LANTERN.getDefaultState());
        // Drying frames leave the centre of the yard open for carrying harvests.
        for(int z:new int[]{-11,1,13}) {
            for(int x:new int[]{26,39})a.box(x,2,z,x,5,z,Blocks.SPRUCE_FENCE.getDefaultState());
            a.box(26,5,z,39,5,z,Blocks.LOG.getStateFromMeta(5));
            for(int x=28;x<39;x+=3)a.block(x,4,z,Blocks.HAY_BLOCK.getDefaultState());
        }
        // A roofed well sits away from the grain and the furnace.
        a.box(-34,1,-2,-28,2,4,Blocks.COBBLESTONE.getDefaultState());
        a.box(-33,0,-1,-29,1,3,Blocks.WATER.getDefaultState());
        for(int x:new int[]{-34,-28})a.box(x,3,1,x,6,1,LOG);
        a.gable(-36,-4,-26,6,7);
        a.lamp(-21,2,16);a.room("院门",0,1,23);a.room("农具与饭灶",7,2,2);
        a.room("谷仓",-9,2,-3);a.room("晒架通路",22,1,2);a.room("井边",-25,1,1);
        // Separate threshing mats and bundled harvests give the yard a working centre.
        for(int x=-12;x<=12;x+=8) {
            a.box(x,1,17,x+5,1,22,Blocks.PLANKS.getStateFromMeta(2));
            a.box(x,1,17,x+5,1,17,DARK);a.box(x,1,22,x+5,1,22,DARK);
        }
        a.box(-39,2,15,-32,2,18,Blocks.HAY_BLOCK.getDefaultState());
        a.box(-37,3,16,-34,3,17,Blocks.HAY_BLOCK.getDefaultState());
        GardenScenery.bench(a,-34,1,-10,7,EnumFacing.NORTH);
        a.box(-1,4,-7,3,4,-7,Blocks.WOODEN_SLAB.getStateFromMeta(9));
        a.block(1,5,-7,ModBlocks.LACQUER_BOWL.getDefaultState());
    }

    static final class Sample {
        final int ground,water,age;final boolean crop;
        Sample(int ground,int water,boolean crop,int age) {this.ground=ground;this.water=water;this.crop=crop;this.age=age;}
    }
}
