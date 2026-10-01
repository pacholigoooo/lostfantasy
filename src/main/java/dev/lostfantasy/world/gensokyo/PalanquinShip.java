package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A suspended treasure ship, with a working deck and two enclosed, usable decks. */
final class PalanquinShip {
    private static final IBlockState HULL=Blocks.PLANKS.getStateFromMeta(5),DECK=Blocks.PLANKS.getDefaultState(),
            TEAL=Blocks.PRISMARINE.getStateFromMeta(2),TRIM=Blocks.PRISMARINE.getStateFromMeta(1),
            RAIL=Blocks.SPRUCE_FENCE.getDefaultState(),SAIL=Blocks.WOOL.getDefaultState(),
            MARK=Blocks.WOOL.getStateFromMeta(14),MAT=Blocks.WOOL.getStateFromMeta(5);
    private PalanquinShip() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.PALANQUIN);
        hull(a);lowerHold(a);passengerDeck(a);cabin(a);workingDeck(a);rigging(a);
        stairs(a,-6,-5,0);stairs(a,6,-10,-5);
    }
    static int halfWidth(int z) {
        if(z< -50 || z>43)return -1;
        if(z< -27)return Math.max(1,(int)Math.round(19*Math.pow((z+51)/24.0,.68)));
        if(z>25)return 19-(z-25)/3;
        return 19;
    }
    static int bottom(int z) {return -14+Math.max(0,(Math.abs(z)-26)/5);}
    static int widthAt(int z,int y) {
        int keel=bottom(z),rim=halfWidth(z);
        return Math.max(0,(int)Math.round(rim*Math.pow(Math.max(0,(y-keel)/(double)-keel),.55)));
    }
    private static void hull(GensokyoArchitecture a) {
        a.box(-26,-18,-55,26,49,48,AIR);
        for(int z=-50;z<=43;z++) {
            int keel=bottom(z),rim=halfWidth(z);
            for(int y=keel;y<=0;y++) {
                int w=widthAt(z,y);IBlockState skin=y>=-3?TEAL:y==-4?TRIM:HULL;
                if(y==keel || y==-10 || y==-5 || y==0)a.box(-w,y,z,w,y,z,y==keel?HULL:DECK);
                else {a.box(-w,y,z,Math.min(-w+1,w),y,z,skin);a.box(Math.max(w-1,-w),y,z,w,y,z,skin);}
            }
            for(int side:new int[]{-1,1}) {
                a.block(side*rim,0,z,HULL);a.block(side*rim,1,z,RAIL);
                if(z%7==0)a.block(side*rim,2,z,Blocks.WOODEN_SLAB.getStateFromMeta(1));
            }
            if(z> -50)joinRail(a,z,halfWidth(z-1),rim,1);
        }
        for(int z:new int[]{-50,43})for(int y=bottom(z);y<=2;y++) {
            int w=widthAt(z,Math.min(y,0));a.box(-w,y,z,w,y,z,y>=-3?TEAL:HULL);
        }
        a.box(-1,-12,-38,1,-11,37,HULL);
        a.box(0,-12,44,0,-1,45,Blocks.LOG.getDefaultState());
        a.box(0,-1,40,0,1,44,Blocks.LOG.getStateFromMeta(8));
        for(int z:new int[]{-23,-2,5,15,26,34})for(int side:new int[]{-1,1})for(int y=-3;y<=-2;y++) {
            int w=widthAt(z,y);a.box(side<0?-w:w-1,y,z,side<0?-w+1:w,y,z,Blocks.STAINED_GLASS.getStateFromMeta(0));
        }
    }
    private static void lowerHold(GensokyoArchitecture a) {
        for(int z:new int[]{-25,-3,21}) {
            a.box(-8,-9,z,8,-6,z,HULL);a.box(-2,-9,z,2,-7,z,AIR);
        }
        a.box(-2,-10,-33,2,-10,31,WOOD);
        for(int z:new int[]{-31,-27,-21,-17,3,7,13,17,26,30}) {
            int side=z%3==0?-1:1;
            a.chest(side*7,-10,z,z<0?"ship_supplies":"ship_gear");
            a.box(-side*8,-9,z-1,-side*8,-8,z+1,Blocks.HAY_BLOCK.getDefaultState());
        }
        for(int z:new int[]{-21,4,16,28})a.box(z<0?5:-7,-9,z,z<0?7:-5,-9,z+1,Blocks.WOOL.getStateFromMeta(z<0?0:3));
        for(int z:new int[]{-19,9,27})for(int side:new int[]{-1,1}) {
            a.box(side*9,-9,z,side*9,-7,z+3,LOG);
            a.box(side<0?-9:7,-7,z,side<0?-7:9,-7,z+3,Blocks.WOODEN_SLAB.getDefaultState());
        }
        for(int z:new int[]{-29,-18,8,27}) {
            a.block(0,-6,z,ModBlocks.RED_LANTERN.getDefaultState());a.block(0,-5,z,DECK);
        }
        a.block(-7,-9,-7,Blocks.CRAFTING_TABLE.getDefaultState());a.block(-5,-9,-7,Blocks.ANVIL.getDefaultState());
        a.room("船底前物资舱",0,-10,-29);a.room("帆布与缆索舱",0,-10,-20);
        a.room("龙骨检修通路",0,-10,-7);a.room("后部船具舱",0,-10,12);a.room("饮水与干粮",0,-10,28);
    }
    private static void passengerDeck(GensokyoArchitecture a) {
        a.box(-2,-5,-36,2,-5,36,WOOD);
        for(int side:new int[]{-1,1})a.box(side*3,-4,-1,side*3,-1,31,HULL);
        for(int z:new int[]{0,12,24,32}) {
            a.box(-13,-4,z,-4,-1,z,HULL);a.box(4,-4,z,13,-1,z,HULL);
        }
        for(int side:new int[]{-1,1})for(int z:new int[]{5,17,28})a.box(side*3,-4,z,side*3,-2,z+1,AIR);
        for(int side:new int[]{-1,1})for(int z:new int[]{5,17}) {
            a.bed(side*10,-5,z);a.chest(side*7,-5,z+4,"ship_supplies");
            a.box(side<0?-12:6,-4,z-3,side<0?-6:12,-4,z-3,Blocks.WOODEN_SLAB.getDefaultState());
            a.box(side<0?-11:7,-5,z-1,side<0?-7:11,-5,z+2,MAT);
            a.box(side<0?-7:5,-4,z-4,side<0?-5:7,-2,z-4,WOOD);
            a.box(side<0?-11:9,-4,z-2,side<0?-9:11,-3,z-2,HULL);
            a.box(side<0?-6:5,-4,z+3,side<0?-5:6,-4,z+3,Blocks.WOODEN_SLAB.getDefaultState());
            a.block(side*5,-4,z+1,Blocks.CARPET.getStateFromMeta(14));
            a.room((side<0?"左":"右")+"舷寝舱"+(z==5?1:2),side*6,-5,z+1);
        }
        a.box(-11,-4,30,-6,-4,30,WOOD);a.block(-9,-3,30,Blocks.CAULDRON.getStateFromMeta(3));
        a.chest(10,-5,29,"ship_gear");a.block(7,-4,30,Blocks.CRAFTING_TABLE.getDefaultState());
        a.room("盥洗舱",-6,-5,28);a.room("被服修补",6,-5,28);
        a.box(-12,-4,-27,-9,-4,-25,WOOD);a.block(-10,-3,-26,Blocks.CAULDRON.getStateFromMeta(3));
        for(int z:new int[]{-25,-28})a.block(-7,-4,z,Blocks.FURNACE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.EAST));
        a.chest(-10,-5,-21,"ship_supplies");a.chest(-7,-5,-21,"ship_supplies");
        a.box(-12,-4,-17,-8,-4,-17,WOOD);a.box(-12,-2,-17,-8,-2,-17,Blocks.WOODEN_SLAB.getDefaultState());
        a.block(-11,-3,-17,Blocks.FLOWER_POT.getDefaultState());a.block(-9,-3,-17,Blocks.FLOWER_POT.getDefaultState());
        for(int z:new int[]{-27,-21}) {
            a.box(6,-4,z,12,-4,z,Blocks.WOODEN_SLAB.getDefaultState());
            for(int x:new int[]{7,10})a.block(x,-4,z+2,Blocks.CARPET.getStateFromMeta(14));
        }
        a.box(-9,-4,-34,9,-2,-34,Blocks.BOOKSHELF.getDefaultState());
        a.room("前舱备餐",-5,-5,-25);a.room("共用食桌",5,-5,-23);a.room("前舱书架",0,-5,-32);
        a.room("舱内通廊",0,-5,19);a.room("舱内楼梯厅",0,-5,-7);
        for(int z:new int[]{-29,-19,-1,12,27})a.block(0,-1,z,ModBlocks.RED_LANTERN.getDefaultState());
        for(int x:new int[]{-9,9})for(int z:new int[]{7,19,28})a.block(x,-1,z,ModBlocks.RED_LANTERN.getDefaultState());
    }
    private static void cabin(GensokyoArchitecture a) {
        a.box(-12,1,14,12,6,39,WHITE);a.box(-11,1,15,11,6,38,AIR);a.box(-12,7,14,12,7,39,DECK);
        for(int x:new int[]{-12,12})for(int z:new int[]{14,23,30,39})a.box(x,1,z,x,7,z,LOG);
        for(int z:new int[]{14,39}) {a.box(-12,6,z,12,6,z,WOOD);a.box(-12,7,z,12,7,z,DARK);}
        for(int side:new int[]{-1,1})for(int z:new int[]{17,25,33})a.box(side*12,2,z,side*12,4,z+3,PAPER);
        for(int x:new int[]{-9,5})a.box(x,2,14,x+4,4,14,PAPER);
        a.openZ(0,14,0,2,4);
        a.box(-11,1,28,11,5,28,WHITE);a.box(-2,1,28,2,3,28,AIR);
        a.box(0,1,29,0,5,38,WOOD);a.box(0,1,31,0,3,32,AIR);
        for(int x=-9;x<=7;x+=5)for(int z=17;z<=24;z+=4)a.box(x,0,z,x+3,0,z+2,MAT);
        a.box(-3,1,20,3,1,20,Blocks.WOODEN_SLAB.getDefaultState());
        for(int x:new int[]{-2,2})for(int z:new int[]{18,22})a.block(x,1,z,Blocks.CARPET.getStateFromMeta(14));
        a.box(-10,1,26,-5,3,26,Blocks.BOOKSHELF.getDefaultState());a.chest(9,0,26,"ship_records");
        a.block(8,1,18,ModBlocks.WRITING_DESK.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.WEST));
        a.box(-10,1,36,-4,1,36,Blocks.WOODEN_SLAB.getDefaultState());a.chest(-9,0,30,"ship_records");
        a.block(-9,1,33,ModBlocks.WRITING_DESK.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.EAST));
        a.box(-10,1,38,-4,3,38,Blocks.BOOKSHELF.getDefaultState());
        for(int x:new int[]{-8,-5})a.block(x,1,34,Blocks.CARPET.getStateFromMeta(14));
        a.bed(8,0,35);a.chest(5,0,37,"ship_supplies");
        a.box(3,1,30,10,1,30,Blocks.WOODEN_SLAB.getDefaultState());
        a.box(3,1,35,3,3,37,WOOD);a.box(6,0,33,10,0,36,MAT);
        for(int z:new int[]{18,25,33})for(int x:new int[]{-6,6})a.block(x,6,z,ModBlocks.RED_LANTERN.getDefaultState());
        for(int x=-15;x<=15;x++) {
            int y=8+(15-Math.abs(x))/3;a.box(x,y,12,x,y,42,TEAL);
            if(Math.abs(x)>12)a.box(x,y+1,12,x,y+1,42,Blocks.PRISMARINE.getDefaultState());
            for(int z:new int[]{14,39})if(y>8)a.box(x,8,z,x,y-1,z,DARK);
        }
        a.box(0,14,12,0,14,42,TRIM);a.block(0,15,12,Blocks.GOLD_BLOCK.getDefaultState());a.block(0,15,42,Blocks.GOLD_BLOCK.getDefaultState());
        a.box(-2,5,13,2,6,13,DARK);a.sign(0,5,12,EnumFacing.NORTH,"圣辇船","");
        a.room("船尾榻榻米会客舱",0,0,24);a.room("航行记录间",-4,0,33);a.room("船长休息间",4,0,34);
    }
    private static void workingDeck(GensokyoArchitecture a) {
        for(int z=-47;z<=-36;z++) {
            int w=Math.max(1,halfWidth(z)-2);a.box(-w,1,z,w,2,z,DECK);
            for(int side:new int[]{-1,1}) {
                a.box(side*(w+1),1,z,side*(w+1),2,z,DECK);a.block(side*(w+1),3,z,RAIL);
            }
            if(z> -47)joinRail(a,z,Math.max(1,halfWidth(z-1)-2)+1,w+1,3);
        }
        for(int z=-36;z>=-47;z--)a.block(0,2,z,WOOD);
        a.box(-2,1,-35,2,1,-35,Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
        a.box(-2,2,-36,2,2,-36,Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
        a.box(-2,1,-28,2,1,-26,WOOD);a.block(0,2,-27,Blocks.LOG.getDefaultState());
        a.box(-3,2,-27,3,2,-27,Blocks.OAK_FENCE.getDefaultState());
        for(int x:new int[]{-14,14})for(int z:new int[]{-21,3}) {
            a.box(x-1,1,z,x+1,1,z+2,HULL);a.box(x,2,z,x,2,z+2,Blocks.HAY_BLOCK.getDefaultState());
        }
        for(int x:new int[]{-17,17})for(int z:new int[]{-14,8,30}) {
            a.box(x,1,z,x,3,z,LOG);a.block(x,4,z,ModBlocks.RED_LANTERN.getDefaultState());
            a.block(x,5,z,Blocks.WOODEN_SLAB.getStateFromMeta(1));
        }
        a.box(-3,0,2,3,0,9,Blocks.LOG.getStateFromMeta(8));
        for(int x:new int[]{-15,15})a.box(x,1,15,x,1,22,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,x<0?EnumFacing.EAST:EnumFacing.WEST));
        a.room("前甲板落脚处",0,0,-20);a.room("船首瞭望",0,2,-42);a.room("起锚绞盘",5,0,-27);
        a.room("主桅与货舱口",4,0,5);a.room("左舷步廊",-15,0,27);a.room("右舷步廊",15,0,27);a.room("船尾舵座",3,0,41);
        a.box(21,-13,-27,21,1,-27,Blocks.IRON_BARS.getDefaultState());a.box(18,1,-27,21,1,-27,Blocks.LOG.getStateFromMeta(4));
        a.box(20,-16,-28,22,-13,-28,Blocks.STONEBRICK.getDefaultState());a.box(17,-16,-28,25,-16,-28,Blocks.STONEBRICK.getDefaultState());
        for(int x:new int[]{17,25})a.box(x,-16,-28,x,-14,-28,Blocks.STONEBRICK.getDefaultState());
    }
    private static void rigging(GensokyoArchitecture a) {
        a.box(-1,-10,-5,1,47,-3,LOG);a.box(-18,44,-6,18,44,-6,Blocks.LOG.getStateFromMeta(4));
        a.box(-18,13,-6,18,13,-6,Blocks.LOG.getStateFromMeta(4));
        for(int y=14;y<=43;y++)for(int x=-17;x<=17;x++)a.block(x,y,sailZ(x,y),SAIL);
        // The red character is part of the bowed cloth, not a floating plate in front of it.
        int[][] strokes={{-1,39,1,41},{-10,36,10,38},{-10,33,-9,37},{9,33,10,37},
                {-7,31,7,32},{-1,19,1,31},{-6,25,6,26},{-9,18,9,19},{5,21,7,22}};
        for(int[] s:strokes)for(int x=s[0];x<=s[2];x++)for(int y=s[1];y<=s[3];y++)a.block(x,y,sailZ(x,y),MARK);
        for(int side:new int[]{-1,1}) {rope(a,side,46,-4,side*18,1,7);rope(a,side*18,44,-6,side*8,3,-40);}
        a.box(0,47,-4,0,49,-4,RAIL);a.box(1,47,-4,6,48,-4,MARK);
    }
    private static int sailZ(int x,int y) {
        return -7-(int)Math.round(3*Math.max(0,Math.sin((y-13)*Math.PI/31)*Math.cos(x*Math.PI/36)));
    }
    private static void joinRail(GensokyoArchitecture a,int z,int previous,int current,int y) {
        // Turn on the wider row so diagonal edges close without extending beyond the deck.
        if(previous==current)return;
        int row=current>previous?z:z-1;
        for(int side:new int[]{-1,1})a.box(side*Math.min(previous,current),y,row,side*Math.max(previous,current),y,row,RAIL);
    }
    private static void rope(GensokyoArchitecture a,int x1,int y1,int z1,int x2,int y2,int z2) {
        int n=Math.max(Math.abs(y2-y1),Math.max(Math.abs(x2-x1),Math.abs(z2-z1)));
        int x=x1,y=y1,z=z1;a.block(x,y,z,RAIL);
        for(int i=1;i<=n;i++) {
            int nx=(int)Math.round(x1+(x2-x1)*i/(double)n),ny=(int)Math.round(y1+(y2-y1)*i/(double)n),
                    nz=(int)Math.round(z1+(z2-z1)*i/(double)n);
            // Adjacent faces let ordinary fences connect, including each change of direction.
            if(nx!=x) {x=nx;a.block(x,y,z,RAIL);}
            if(nz!=z) {z=nz;a.block(x,y,z,RAIL);}
            y=ny;a.block(x,y,z,RAIL);
        }
    }
    private static void stairs(GensokyoArchitecture a,int x,int bottom,int top) {
        a.box(x-1,bottom+1,-15,x+1,top+3,-6,AIR);
        for(int i=1;i<=5;i++) {
            int z=-8-i,y=bottom+i;if(i>1)a.box(x-1,bottom+1,z,x+1,y-1,z,HULL);
            a.box(x-1,y,z,x+1,y,z,Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
        }
        a.box(x-1,top,-15,x+1,top,-14,DECK);
        for(int side:new int[]{-1,1})a.box(x+side*2,top+1,-13,x+side*2,top+1,-7,RAIL);
    }
}
