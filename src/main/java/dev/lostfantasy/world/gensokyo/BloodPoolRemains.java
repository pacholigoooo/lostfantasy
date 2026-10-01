package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Remaining service galleries around an open blood sea; no invented character residence. */
final class BloodPoolRemains {
    static final int FLOOR=-18;
    private static final IBlockState ROCK=ModBlocks.COLUMNAR_BASALT.getDefaultState(),
            TRIM=Blocks.RED_NETHER_BRICK.getDefaultState(),BAR=Blocks.IRON_BARS.getDefaultState();
    private BloodPoolRemains() {}
    static void buildUpper(GensokyoBlueprint p) {
        GensokyoArchitecture a=new GensokyoArchitecture(p,OldHellWorld.ORIGIN,0,0,"血池下行道");
        BlazingHell.bridge(a,0,1690,0,1739,BlazingHell.DECK,4);
        // Enclosed rock casing separates the low tunnel from the surrounding lava.
        for(int z=1740;z<=1819;z++) {
            int f=BlazingHell.DECK-Math.min(24,(z-1740)/2+1);
            a.box(-7,f-3,z,7,f+11,z,ROCK);a.box(-4,f+1,z,4,f+8,z,AIR);
            if(z<1788 && (z-1740)%2==0)a.box(-4,f+1,z,4,f+1,z,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
            if(z%14==0)for(int x:new int[]{-5,5})a.block(x,f+4,z,LIGHT);
            if(z%8==1)a.room("下行石阶",0,f,z);
        }
        a.box(-3,BlazingHell.DECK+1,1688,3,BlazingHell.DECK+3,1696,AIR);
        a.room("深层门槛",0,-59,1813);
    }
    static GensokyoBlueprint create() {
        GensokyoBlueprint p=new GensokyoBlueprint();
        GensokyoArchitecture a=new GensokyoArchitecture(p,OldHellWorld.ORIGIN,0,0,"旧血池地狱");
        a.box(-9,106,-302,9,124,-257,ROCK);a.box(-6,111,-300,6,122,-257,AIR);
        a.room("上行门槛",0,110,-294);a.room("深层到达廊",0,110,-282);
        for(int z=-256;z<0;z++) {
            int drop=(z+256)/2+1,f=110-drop;
            a.box(-8,f-3,z,8,f,z,ROCK);a.box(-6,f+1,z,6,f+9,z,AIR);
            a.block(-7,f+1,z,BAR);a.block(7,f+1,z,BAR);
            if((z+256)%2==0)a.box(-6,f+1,z,6,f+1,z,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
            if(z%16==0)for(int x:new int[]{-7,7})a.block(x,f,z,LIGHT);
            if(z%16==-1)a.room("下行长阶",0,f,z);
        }
        bridge(a,0,0,0,620,4);
        bridge(a,-170,120,170,120,4);bridge(a,-170,620,170,620,4);
        bridge(a,-170,120,-170,620,4);bridge(a,170,120,170,620,4);
        bridge(a,-170,440,170,440,4);
        // A low gate frames the much higher natural cavern beyond it.
        for(int x:new int[]{-15,15}) {
            a.box(x-3,FLOOR-4,50,x+3,FLOOR+25,62,ROCK);
            for(int y:new int[]{6,16,24})a.box(x-4,FLOOR+y,49,x+4,FLOOR+y+1,63,TRIM);
        }
        a.box(-20,FLOOR+24,48,20,FLOOR+27,64,TRIM);
        a.box(-18,FLOOR+28,50,18,FLOOR+29,62,ROCK);a.room("血海入口",0,FLOOR,60);
        // Two small surviving work buildings are set off the main loop.
        bridge(a,-170,240,-224,240,3);chamber(a,-224,240,"旧案库",false);
        bridge(a,170,310,224,310,3);chamber(a,224,310,"器具收存所",true);
        sluice(a);dais(a);
        bridge(a,170,520,260,520,3);lookout(a,260,520);
        for(int[] q:new int[][]{{0,120},{-170,120},{170,120},{-170,620},{0,620},{170,620},{-170,440},{0,440},{170,440},{-170,240},{170,310},{170,520}}) {
            a.box(q[0]-4,FLOOR+1,q[1]-4,q[0]+4,FLOOR+3,q[1]+4,AIR);
            for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++) {
                net.minecraft.util.math.BlockPos pos=BloodPoolWorld.local(q[0]+dx,FLOOR,q[1]+dz);
                if(!p.at(pos.getX(),pos.getY(),pos.getZ()).isFullCube())continue;
                boolean edge=false;
                for(EnumFacing side:EnumFacing.HORIZONTALS) {net.minecraft.util.math.BlockPos n=pos.offset(side);edge|=!p.at(n.getX(),n.getY(),n.getZ()).isFullCube();}
                if(edge)a.block(q[0]+dx,FLOOR+1,q[1]+dz,BAR);
            }
            a.room("巡池通路",q[0],FLOOR,q[1]);
        }
        p.seal();return p;
    }
    private static void bridge(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int half) {
        boolean along=x1==x2;int lo=along?Math.min(z1,z2):Math.min(x1,x2),hi=along?Math.max(z1,z2):Math.max(x1,x2);
        for(int n=lo;n<=hi;n++) {
            int x=along?x1:n,z=along?n:z1;
            a.box(x-(along?half:0),FLOOR-2,z-(along?0:half),x+(along?half:0),FLOOR,z+(along?0:half),ROCK);
            a.box(x-(along?half:0),FLOOR+1,z-(along?0:half),x+(along?half:0),FLOOR+8,z+(along?0:half),AIR);
            for(int side:new int[]{-1,1}) {
                int px=x+(along?side*half:0),pz=z+(along?0:side*half);
                a.block(px,FLOOR+1,pz,BAR);
                if(n%24==0) {
                    a.box(px-1,-82,pz-1,px+1,FLOOR-3,pz+1,ROCK);
                    a.block(px,FLOOR,pz,LIGHT);a.block(px,FLOOR-1,pz,TRIM);
                }
            }
        }
    }
    private static void chamber(GensokyoArchitecture a,int x,int z,String title,boolean tools) {
        a.box(x-22,-80,z-23,x+22,FLOOR,z+23,ROCK);
        a.box(x-22,FLOOR+1,z-23,x+22,FLOOR+17,z+23,STONE);
        a.box(x-20,FLOOR+1,z-21,x+20,FLOOR+15,z+21,AIR);
        for(int split:new int[]{-4,4}) {a.box(x-21,FLOOR+1,z+split,x+21,FLOOR+10,z+split,ROCK);a.openZ(x,z+split,FLOOR,3,6);}
        a.openX(x+(x<0?22:-22),z,FLOOR,3,7);
        for(int px:new int[]{x-22,x+22})for(int pz=z-23;pz<=z+23;pz+=11) {
            a.box(px-1,FLOOR+1,pz-1,px+1,FLOOR+20,pz+1,ROCK);
            a.block(px,FLOOR+12,pz,Blocks.NETHERRACK.getDefaultState());
        }
        for(int y:new int[]{0,11,17})a.box(x-24,FLOOR+y,z-25,x+24,FLOOR+y,z+25,TRIM);
        // Interior carving after cornices leaves the floor and roof solid.
        a.box(x-20,FLOOR+11,z-21,x+20,FLOOR+15,z+21,AIR);
        for(int side:new int[]{-1,1}) {
            int pz=z+side*12;
            a.room(title+(side<0?"·记录间":"·收存间"),x,FLOOR,pz-5);
            for(int px:new int[]{x-16,x-9,x+9,x+16})a.chest(px,FLOOR,pz+side*6,"blood_pool_relics");
            for(int px:new int[]{x-19,x+19}) {
                a.box(px,FLOOR+1,pz-4,px,FLOOR+4,pz+4,tools?Blocks.IRON_BLOCK.getDefaultState():Blocks.BOOKSHELF.getDefaultState());
                a.block(px,FLOOR+10,pz,ModBlocks.LIBRARY_LAMP.getDefaultState());
                a.box(px,FLOOR+11,pz,px,FLOOR+16,pz,BAR);
            }
            a.table(x-4,FLOOR,pz,7);a.box(x-4,FLOOR+1,pz+3,x+3,FLOOR+1,pz+3,Blocks.STONE_BRICK_STAIRS.getDefaultState());
            a.block(x+10,FLOOR+1,pz,tools?Blocks.ANVIL.getDefaultState():ModBlocks.WRITING_DESK.getDefaultState());
            a.block(x-10,FLOOR+1,pz,Blocks.CRAFTING_TABLE.getDefaultState());
            for(int sx:new int[]{-16,9})for(int offset:new int[]{-4,4}) {
                a.box(x+sx,FLOOR+1,pz+offset,x+sx+7,FLOOR+3,pz+offset,tools?ROCK:Blocks.BOOKSHELF.getDefaultState());
                a.box(x+sx,FLOOR+4,pz+offset,x+sx+7,FLOOR+4,pz+offset,Blocks.STONE_SLAB.getStateFromMeta(5));
                if(tools)for(int n:new int[]{1,5})a.block(x+sx+n,FLOOR+5,pz+offset,Blocks.FLOWER_POT.getDefaultState());
            }
            for(int dx:new int[]{-12,12})a.block(x+dx,FLOOR,pz,LIGHT);
            a.block(x,FLOOR,pz-6,LIGHT);
            // Small high openings and burned masonry keep these rooms distinct from homes.
            for(int px:new int[]{x-22,x+22})a.box(px,FLOOR+6,pz-2,px,FLOOR+9,pz+2,BAR);
        }
        int entry=x+(x<0?22:-22);a.box(entry-2,FLOOR+1,z-3,entry+2,FLOOR+7,z+3,AIR);
    }
    private static void sluice(GensokyoArchitecture a) {
        int x=-170,z=365;
        a.box(x-29,-80,z-8,x+29,FLOOR-4,z+8,ROCK);
        a.box(x-25,FLOOR-30,z-9,x+25,FLOOR-5,z+9,AIR);
        a.box(x-25,FLOOR-30,z-9,x+25,-32,z+9,ModBlocks.CURSED_BLOOD.getDefaultState());
        for(int dx:new int[]{-26,-13,13,26}) {
            a.box(x+dx-1,-80,z-8,x+dx+1,FLOOR+22,z+8,ROCK);
            a.box(x+dx-3,FLOOR+20,z-10,x+dx+3,FLOOR+23,z+10,TRIM);
        }
        for(int dx:new int[]{-20,20})a.box(x+dx-4,-31,z-1,x+dx+4,FLOOR+15,z+1,BAR);
        a.box(x-31,FLOOR+18,z-8,x+31,FLOOR+19,z+8,TRIM);
        // The inspection walk crosses the dry middle bay; no moving gate simulation.
        a.box(x-4,FLOOR-2,z-9,x+4,FLOOR,z+9,ROCK);a.box(x-3,FLOOR+1,z-9,x+3,FLOOR+8,z+9,AIR);
        a.room("旧闸廊",x,FLOOR,z);
    }
    private static void dais(GensokyoArchitecture a) {
        for(int x=-35;x<=35;x++)for(int z=-35;z<=35;z++) {
            double d=Math.hypot(x,z);if(d>35)continue;
            a.box(x,-82,440+z,x,FLOOR,440+z,d>32 || d<26 && d>24?TRIM:ROCK);
            a.box(x,FLOOR+1,440+z,x,FLOOR+12,440+z,AIR);
            if(d>33 && Math.abs(x)>4 && Math.abs(z)>4)a.block(x,FLOOR+1,440+z,BAR);
            if(d<23 && Math.abs(x)>4 && Math.abs(z)>4) {
                a.box(x,-30,440+z,x,FLOOR,440+z,AIR);
                a.box(x,-30,440+z,x,-26,440+z,ModBlocks.CURSED_BLOOD.getDefaultState());
            }
        }
        for(int x:new int[]{-28,28})for(int z:new int[]{412,468}) {
            a.box(x-2,-82,z-2,x+2,FLOOR+12,z+2,ROCK);a.block(x,FLOOR+13,z,LIGHT);
        }
        a.room("旧池台",0,FLOOR,440);
    }
    private static void lookout(GensokyoArchitecture a,int x,int z) {
        a.box(x-12,-82,z-14,x+12,FLOOR,z+14,ROCK);a.box(x-11,FLOOR+1,z-13,x+11,FLOOR+10,z+13,AIR);
        for(int px:new int[]{x-12,x+12})a.box(px,FLOOR+1,z-14,px,FLOOR+1,z+14,BAR);
        for(int pz:new int[]{z-14,z+14})a.box(x-12,FLOOR+1,pz,x+12,FLOOR+1,pz,BAR);
        a.openX(x-12,z,FLOOR,2,3);a.block(x+6,FLOOR+1,z+8,ModBlocks.WRITING_DESK.getDefaultState());
        a.chest(x+8,FLOOR,z-10,"blood_pool_relics");a.lamp(x-9,FLOOR,z+11);a.room("深池望台",x,FLOOR,z);
    }
}
