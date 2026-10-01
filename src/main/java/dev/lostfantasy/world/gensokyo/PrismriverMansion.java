package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** The former Prismriver family house, with rehearsal rooms inside its weathered shell. */
final class PrismriverMansion {
    static final GensokyoAtlas SITE=GensokyoAtlas.RUINED_MANSION;
    private static final IBlockState PLASTER=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(8),
            TRIM=Blocks.SANDSTONE.getStateFromMeta(2),TILE=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(9),
            CRACKED=Blocks.STONEBRICK.getStateFromMeta(2),MOSS=Blocks.STONEBRICK.getStateFromMeta(1);
    private PrismriverMansion() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,SITE);
        a.box(-39,1,-25,39,36,36,AIR);
        shell(a);rooms(a);furnish(a);garden(a);wear(a);
        rehearsalDetails(a);
        a.room("旧门前",0,0,52);a.room("门廊",0,2,24);a.room("楼梯厅",0,2,15);
        a.room("上层回廊",-6,10,-2);a.room("合奏厅",-19,2,8);a.room("合奏台",-19,3,-12);
        a.room("乐谱收存",19,2,-9);a.room("装箱与修护",27,2,10);a.room("会客茶间",16,2,7);
        a.room("弦乐分练",-27,10,-9);a.room("抄谱间",-17,10,-9);a.room("键盘分练",19,10,7);a.room("号管分练",27,10,10);
        a.room("西侧旧寝间一",-27,10,5);a.room("西侧旧寝间二",-16,10,5);
        a.room("东侧旧寝间一",15,10,-10);a.room("东侧旧寝间二",27,10,-10);
        a.room("家书与旧物",0,10,-11);a.room("前廊",0,10,22);a.room("侧门装卸",36,0,9);
    }
    private static void shell(GensokyoArchitecture a) {
        a.box(-32,0,-18,32,1,18,STONE);
        a.box(-32,2,-18,32,18,18,PLASTER);a.box(-31,3,-17,31,17,17,AIR);
        for(int y:new int[]{2,10,18}) {
            a.box(-31,y,-17,31,y,17,WOOD);
            a.box(-33,y,-19,33,y,-19,TRIM);a.box(-33,y,19,33,y,19,TRIM);
            a.box(-33,y,-18,-33,y,18,TRIM);a.box(33,y,-18,33,y,18,TRIM);
        }
        for(int x:new int[]{-32,-9,9,32})for(int z:new int[]{-18,18})a.box(x,3,z,x,17,z,STONE);
        for(int f:new int[]{2,10}) {
            for(int x:new int[]{-26,-17,0,17,26})for(int z:new int[]{-18,18})window(a,x,f,z,false);
            for(int z:new int[]{-11,8})for(int x:new int[]{-32,32})window(a,x,f,z,true);
        }
        hipped(a,-35,-21,35,21,19);
        // Central bay: entrance below, a small gallery above, then a front-facing pediment.
        a.box(-7,0,19,7,1,27,STONE);a.box(-7,2,19,7,18,27,PLASTER);
        a.box(-6,3,18,6,17,26,AIR);
        for(int y:new int[]{2,10,18})a.box(-7,y,18,7,y,27,WOOD);
        for(int f:new int[]{2,10}) {
            window(a,-7,f,23,true);window(a,7,f,23,true);window(a,0,f,27,false);
        }
        a.box(-3,3,27,3,7,27,AIR);a.box(-3,3,18,3,8,19,AIR);a.box(-3,11,18,3,15,19,AIR);
        a.gableZ(-9,17,9,30,19);
        a.box(-8,2,28,8,2,29,TRIM);a.stairsSouth(0,30,0,2,5);
        for(int x:new int[]{-8,8}) {a.box(x,3,28,x,8,28,TRIM);a.block(x,9,28,SLAB);}
        a.box(-8,9,27,8,9,29,TRIM);
        for(int x:new int[]{-24,24}) {
            a.box(x-4,19,13,x+4,24,18,PLASTER);a.box(x-3,19,14,x+3,23,17,AIR);
            window(a,x,18,18,false);a.gableZ(x-5,11,x+5,20,25);
        }
        a.box(32,3,7,33,6,11,AIR);
        for(int x=34;x<=35;x++)a.box(x,36-x,7,x,36-x,11,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.WEST));
        a.box(34,1,7,34,1,11,STONE);
    }
    private static void rehearsalDetails(GensokyoArchitecture a) {
        // Framed wall panels and a continuous stage cornice leave all three instruments accessible.
        for(int x:new int[]{-27,-20,-13}) {
            a.box(x-2,5,-17,x+2,7,-17,DARK);
            a.box(x-1,5,-17,x+1,6,-17,Blocks.WOOL.getStateFromMeta(11));
        }
        a.box(-31,8,-17,-10,8,-17,TRIM);
        a.box(-28,2,-7,-13,2,14,Blocks.PLANKS.getStateFromMeta(5));
        a.box(-24,2,-7,-23,2,14,Blocks.PLANKS.getStateFromMeta(2));
        a.box(-18,2,-7,-17,2,14,Blocks.PLANKS.getStateFromMeta(2));
        GardenScenery.bench(a,-28,0,26,9,EnumFacing.NORTH);
        GardenScenery.bench(a,17,0,26,9,EnumFacing.NORTH);
        a.box(-25,0,22,-22,0,29,Blocks.GRAVEL.getDefaultState());
        a.box(22,0,22,25,0,29,Blocks.GRAVEL.getDefaultState());
    }
    private static void hipped(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int base) {
        for(int y=0;y<=12;y++) {
            int inset=y<4?y:y*2-3;
            int xa=x1+inset,xb=x2-inset,za=z1+inset,zb=z2-inset;
            a.box(xa,base+y,za,xb,base+y,za,TILE);a.box(xa,base+y,zb,xb,base+y,zb,TILE);
            a.box(xa,base+y,za,xa,base+y,zb,TILE);a.box(xb,base+y,za,xb,base+y,zb,TILE);
            if(y>=4) {
                a.box(xa-1,base+y,za-1,xb+1,base+y,za,TILE);a.box(xa-1,base+y,zb,xb+1,base+y,zb+1,TILE);
                a.box(xa-1,base+y,za,xa,base+y,zb,TILE);a.box(xb,base+y,za,xb+1,base+y,zb,TILE);
            }
            if(y==12) {a.box(xa,base+y,za,xb,base+y,zb,TILE);a.box(xa,base+y+1,0,xb,base+y+1,0,SLAB);}
        }
        a.box(x1-1,base,z1-1,x2+1,base,z1-1,SLAB);a.box(x1-1,base,z2+1,x2+1,base,z2+1,SLAB);
    }
    private static void window(GensokyoArchitecture a,int x,int f,int z,boolean side) {
        for(int u=-2;u<=2;u++)for(int y=2;y<=6;y++) {
            IBlockState s=Math.abs(u)==2 || y==2 || y==6 && Math.abs(u)>0?TRIM:Blocks.GLASS_PANE.getDefaultState();
            a.block(x+(side?0:u),f+y,z+(side?u:0),s);
        }
    }
    private static void rooms(GensokyoArchitecture a) {
        for(int f:new int[]{2,10})for(int x:new int[]{-9,9}) {
            a.box(x,f+1,-17,x,f+7,17,PLASTER);a.openX(x,-9,f,1,4);a.openX(x,9,f,1,4);
        }
        a.box(-5,10,2,5,10,15,AIR);a.box(-2,3,2,2,13,9,AIR);a.stairsSouth(0,2,2,10,2);
        for(int x:new int[]{-5,5})a.box(x,11,2,x,11,15,Blocks.DARK_OAK_FENCE.getDefaultState());
        a.box(-5,11,15,5,11,15,Blocks.DARK_OAK_FENCE.getDefaultState());
        a.box(-2,11,1,2,14,1,AIR);
        a.box(10,3,-1,31,9,-1,PLASTER);a.openZ(18,-1,2,1,3);
        a.box(23,3,0,23,9,17,PLASTER);a.openX(23,9,2,1,3);
        // Four former family chambers share galleries with the working music rooms.
        a.box(-31,11,-1,-10,17,-1,PLASTER);a.openZ(-15,-1,10,1,3);
        a.box(-22,11,-17,-22,17,-2,PLASTER);a.openX(-22,-8,10,1,3);
        a.box(-21,11,0,-21,17,17,PLASTER);a.openX(-21,6,10,1,3);
        a.box(10,11,-1,31,17,-1,PLASTER);a.openZ(15,-1,10,1,3);
        a.box(21,11,-17,21,17,-2,PLASTER);a.openX(21,-9,10,1,3);
        a.box(23,11,0,23,17,17,PLASTER);a.openX(23,9,10,1,3);
        a.box(-31,3,-17,-10,3,-11,DARK);
        a.box(-30,3,-10,-11,3,-10,Blocks.DARK_OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
        for(int x:new int[]{-31,-10})a.box(x,4,-17,x,8,-12,Blocks.WOOL.getStateFromMeta(11));
    }
    private static void furnish(GensokyoArchitecture a) {
        instrument(a,-27,4,-14,ModBlocks.VIOLIN_STAND,EnumFacing.SOUTH);
        instrument(a,-20,4,-15,ModBlocks.REHEARSAL_KEYBOARD,EnumFacing.SOUTH);
        instrument(a,-13,4,-14,ModBlocks.TRUMPET_STAND,EnumFacing.SOUTH);
        for(int x:new int[]{-27,-20,-13})scoreStand(a,x,3,-12);
        for(int z:new int[]{-3,4,11})for(int x:new int[]{-28,-18})bench(a,x,x+5,2,z,EnumFacing.SOUTH);
        a.chest(-29,2,16,"prismriver_rehearsal");a.chest(-13,2,16,"concert_supplies");
        for(int x:new int[]{12,19,26}) {
            a.box(x,3,-16,x+3,6,-16,Blocks.BOOKSHELF.getDefaultState());a.chest(x,2,-5,"prismriver_scores");
        }
        a.box(29,3,-14,30,5,-5,Blocks.BOOKSHELF.getDefaultState());
        a.table(14,2,-11,6);a.block(16,5,-11,ModBlocks.RESEARCH_NOTES.getDefaultState());
        bench(a,14,19,2,-8,EnumFacing.NORTH);
        a.box(25,3,15,30,3,16,WOOD);a.block(26,3,14,Blocks.CRAFTING_TABLE.getDefaultState());
        a.chest(30,2,2,"concert_supplies");a.chest(26,2,2,"concert_supplies");
        a.box(29,4,15,30,5,16,Blocks.WOOL.getStateFromMeta(8));
        a.table(13,2,9,6);bench(a,13,18,2,6,EnumFacing.SOUTH);bench(a,13,18,2,12,EnumFacing.NORTH);
        a.box(12,3,16,19,3,16,DARK);a.block(14,4,16,Blocks.FLOWER_POT.getDefaultState());a.chest(20,2,2,"village_pantry");
        bedroom(a,-30,2,11);bedroom(a,-19,2,11);bedroom(a,11,-16,10);bedroom(a,23,-16,9);
        instrument(a,-27,11,-12,ModBlocks.VIOLIN_STAND,EnumFacing.EAST);
        a.table(-19,10,-15,6);a.block(-17,13,-15,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.box(-21,11,-16,-21,14,-12,Blocks.BOOKSHELF.getDefaultState());
        bench(a,-18,-14,10,-11,EnumFacing.NORTH);scoreStand(a,-25,10,-12);
        bench(a,-29,-25,10,-5,EnumFacing.NORTH);a.chest(-11,10,-15,"prismriver_scores");
        a.box(-30,11,-16,-28,14,-16,Blocks.BOOKSHELF.getDefaultState());
        instrument(a,17,11,7,ModBlocks.REHEARSAL_KEYBOARD,EnumFacing.NORTH);
        bench(a,16,18,10,10,EnumFacing.NORTH);a.box(11,11,16,20,13,16,Blocks.BOOKSHELF.getDefaultState());
        a.chest(11,10,2,"prismriver_scores");
        instrument(a,28,11,6,ModBlocks.TRUMPET_STAND,EnumFacing.SOUTH);
        scoreStand(a,26,10,6);
        a.table(26,10,14,4);a.block(27,13,14,ModBlocks.RESEARCH_NOTES.getDefaultState());a.chest(30,10,2,"concert_supplies");
        for(int f:new int[]{2,10}) {
            a.box(-3,f+1,-16,3,f+5,-15,STONE);a.box(-1,f+1,-14,1,f+2,-14,Blocks.IRON_BARS.getDefaultState());
            a.box(-1,f+1,-15,1,f+2,-15,LIGHT);
            bench(a,-6,-4,f,-9,EnumFacing.EAST);bench(a,4,6,f,-9,EnumFacing.WEST);
            a.table(-1,f,-9,3);
        }
        a.chest(-6,10,-15,"prismriver_scores");a.chest(6,10,-15,"forest_books");
        a.box(-2,19,-17,2,31,-15,STONE);a.box(-3,32,-18,3,32,-14,SLAB);
        bench(a,-5,-3,10,24,EnumFacing.EAST);bench(a,3,5,10,24,EnumFacing.WEST);a.block(0,11,25,Blocks.FLOWER_POT.getDefaultState());
        a.box(-1,3,21,1,3,22,Blocks.CARPET.getStateFromMeta(11));
        a.box(-7,3,12,-6,5,16,DARK);a.box(6,3,12,7,5,16,DARK);
        for(int x:new int[]{-7,7})a.block(x,6,14,Blocks.FLOWER_POT.getDefaultState());
        rug(a,-2,12,2,26,2,11);rug(a,-30,-8,-12,14,2,7);
        rug(a,-30,-13,-24,-5,10,11);rug(a,-20,-9,-12,-4,10,7);
        rug(a,12,4,20,13,10,11);rug(a,25,4,30,12,10,7);
        for(int f:new int[]{2,10})for(int[] p:new int[][]{{-20,-5},{-20,12},{20,-10},{16,7},{27,10},{0,-5}})lamp(a,p[0],f+6,p[1]);
        lamp(a,0,14,18);lamp(a,0,7,24);
    }
    private static void bedroom(GensokyoArchitecture a,int x,int z,int width) {
        a.bed(x+2,10,z+3);a.chest(x+width-3,10,z+1,"prismriver_scores");
        a.box(x,11,z,x+3,13,z,DARK);a.box(x+width-2,11,z+6,x+width-2,13,z+9,Blocks.BOOKSHELF.getDefaultState());
        VillageJoinery.lowDesk(a,x+1,10,z+11,4);a.block(x+width-3,11,z+11,Blocks.CAULDRON.getDefaultState());
        a.block(x+4,11,z+3,DARK);a.block(x+4,12,z+3,Blocks.FLOWER_POT.getDefaultState());
        rug(a,x+1,z+5,x+width-3,z+9,10,8);
    }
    private static void scoreStand(GensokyoArchitecture a,int x,int f,int z) {
        a.block(x,f+1,z,WOOD);a.block(x,f+2,z,ModBlocks.RESEARCH_NOTES.getDefaultState());
    }
    private static void rug(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int f,int color) {
        for(int x=x1;x<=x2;x++)for(int z=z1;z<=z2;z++)
            if(a.plan.at(SITE.x+x,SITE.y+f+1,SITE.z+z).getBlock()==Blocks.AIR)
                a.block(x,f+1,z,Blocks.CARPET.getStateFromMeta(color));
    }
    private static void instrument(GensokyoArchitecture a,int x,int y,int z,net.minecraft.block.Block block,EnumFacing facing) {
        a.block(x,y,z,block.getDefaultState().withProperty(BlockHorizontal.FACING,facing));
    }
    private static void bench(GensokyoArchitecture a,int x1,int x2,int f,int z,EnumFacing facing) {
        a.box(x1,f+1,z,x2,f+1,z,Blocks.DARK_OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,facing));
    }
    private static void lamp(GensokyoArchitecture a,int x,int y,int z) {
        a.box(x,y+1,z,x,(y<10?9:17),z,Blocks.IRON_BARS.getDefaultState());
        a.block(x,y,z,LIGHT);a.block(x,y-1,z,SLAB);
    }
    private static void garden(GensokyoArchitecture a) {
        a.box(-4,0,32,4,0,58,Blocks.GRAVEL.getDefaultState());a.box(36,0,7,45,0,11,Blocks.GRAVEL.getDefaultState());
        for(int x:new int[]{-46,46})a.box(x,0,-30,x,1,45,MOSS);
        for(int z:new int[]{-30,45})for(int x=-46;x<=46;x++)if(z<0 || Math.abs(x)>7) {
            a.block(x,0,z,MOSS);if(Math.floorMod(x,9)!=0)a.block(x,1,z,Blocks.IRON_BARS.getDefaultState());
        }
        for(int x:new int[]{-7,7}) {a.box(x,0,45,x,4,45,STONE);a.block(x,5,45,SLAB);}
        for(int hand:new int[]{-1,1}) {
            int cx=hand*22;
            for(int x=-8;x<=8;x++)for(int z=-6;z<=6;z++) {
                double r=x*x/64.0+z*z/36.0;if(r>1)continue;
                a.block(cx+x,0,36+z,r>.70?MOSS:Blocks.GRAVEL.getDefaultState());
                if(r>.70)a.block(cx+x,1,36+z,Blocks.COBBLESTONE_WALL.getDefaultState());
            }
            if(hand<0) {a.box(cx-1,1,35,cx+1,1,37,STONE);a.box(cx,2,36,cx,3,36,STONE);a.block(cx,4,36,SLAB);}
            else for(int x=-5;x<=5;x++)for(int z=-3;z<=3;z++)if(x*x+z*z<25) {
                a.block(cx+x,0,36+z,Blocks.GRASS.getDefaultState());
                if(Math.floorMod(x*7+z*11,5)<2)a.block(cx+x,1,36+z,Blocks.RED_FLOWER.getStateFromMeta(0));
            }
        }
        for(int[] p:new int[][]{{-38,-13},{38,-6},{-39,9},{37,23}})
            a.box(p[0],1,p[1],p[0]+2,2,p[1]+4,Blocks.LEAVES.getStateFromMeta(4));
        for(int[] p:new int[][]{{-43,-23,24,7},{42,-25,27,8},{-43,24,22,6},{43,30,21,6}})ForestDetails.tree(a,p[0],p[1],p[2],p[3]);
    }
    private static void wear(GensokyoArchitecture a) {
        for(int z:new int[]{-18,18})for(int x=-31;x<=31;x++)for(int y=3;y<=17;y++)
            if(a.plan.at(SITE.x+x,SITE.y+y,SITE.z+z)==PLASTER && Math.floorMod(x*17+y*13+z*7,41)<3)
                a.block(x,y,z,y<6?MOSS:CRACKED);
        for(int[] p:new int[][]{{-32,4,-15},{32,12,-8},{-18,11,18},{20,5,-18},{-7,12,25}})a.block(p[0],p[1],p[2],CRACKED);
        for(int[] p:new int[][]{{-25,7,18},{18,15,18},{32,7,-10},{-1,7,-18}})a.block(p[0],p[1],p[2],AIR);
        a.box(-29,6,19,-23,6,19,DARK);a.box(33,14,6,33,14,10,DARK);
        a.box(26,19,-16,28,26,-14,AIR);a.box(25,19,-16,25,19,-14,LOG);a.block(27,20,-17,Blocks.WEB.getDefaultState());
        for(int[] p:new int[][]{{-30,9,-16},{30,17,16},{5,17,25}})a.block(p[0],p[1],p[2],Blocks.WEB.getDefaultState());
        ForestDetails.vinesZ(a,-31,-28,19,3,16,EnumFacing.NORTH);
        ForestDetails.vinesZ(a,24,29,-19,3,16,EnumFacing.SOUTH);
    }
}
