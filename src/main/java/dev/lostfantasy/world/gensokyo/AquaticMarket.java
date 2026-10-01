package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Kappa exhibition fair: working stalls around two contained demonstration pools. */
final class AquaticMarket {
    static final GensokyoAtlas SITE=GensokyoAtlas.AQUATIC_MARKET;
    private static final IBlockState FRAME=Blocks.DARK_OAK_FENCE.getDefaultState(),
            BLUE=Blocks.WOOL.getStateFromMeta(11),TEAL=Blocks.WOOL.getStateFromMeta(9),
            CREAM=Blocks.WOOL.getStateFromMeta(0),METAL=Blocks.IRON_BLOCK.getDefaultState();
    private AquaticMarket() {}
    static void installBalloon(net.minecraft.world.chunk.Chunk chunk) {
        BlockPos p=local(0,19,-27);
        if(chunk.x!=p.getX()>>4 || chunk.z!=p.getZ()>>4)return;
        dev.lostfantasy.entity.EntityHisoutensoku e=new dev.lostfantasy.entity.EntityHisoutensoku(chunk.getWorld());
        e.setPosition(p.getX()+.5,p.getY(),p.getZ()+.5);chunk.addEntity(e);
    }
    static BlockPos local(int x,int y,int z) {return new BlockPos(SITE.x+x,SITE.y+y,SITE.z+z);}
    static void build(GensokyoBlueprint p) {
        GensokyoArchitecture a=new GensokyoArchitecture(p,SITE);
        a.box(-59,-3,-43,59,0,47,STONE);a.box(-59,1,-43,59,90,47,AIR);
        a.box(-58,1,-42,58,2,41,STONE);
        a.box(-56,2,-40,56,2,39,Blocks.STONE.getStateFromMeta(6));
        a.box(-6,2,-17,6,2,42,Blocks.QUARTZ_BLOCK.getDefaultState());
        for(int x:new int[]{-58,58})a.box(x,3,-42,x,4,41,STONE);
        a.box(-58,3,-42,58,4,-42,STONE);a.stairsSouth(0,42,0,2,8);
        for(int x:new int[]{-43,43}) {
            for(int i=0;i<3;i++)stall(a,x,-10+i*16,i);
            refreshment(a,x,35);workshop(a,x<0?-55:18,x<0?-18:55,x<0);
        }
        for(int side:new int[]{-1,1})pool(a,side);
        steamYard(a);entry(a);
        for(int x:new int[]{-29,29})for(int z:new int[]{-15,1,17,33})a.lamp(x,2,z);
        for(int x:new int[]{-18,18}) {
            a.table(x-3,2,29,7);a.box(x-3,3,31,x+3,3,31,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
        }
        a.room("山脚入口",0,0,47);a.room("展销主街",0,2,25);a.room("试水场中庭",0,2,5);
    }
    private static void stall(GensokyoArchitecture a,int x,int z,int type) {
        boolean west=x<0;int face=x+(west?8:-8);
        a.box(x-10,2,z-6,x+10,2,z+6,WOOD);
        for(int px:new int[]{x-10,x+10})for(int pz:new int[]{z-6,z+6})a.box(px,3,pz,px,8,pz,LOG);
        a.box(west?x-10:x+10,3,z-6,west?x-10:x+10,5,z+6,WOOD);
        a.box(x-10,3,z-6,x+10,4,z-6,WOOD);
        // Counter ends leave a full-height side entrance for using storage and benches.
        a.box(face,3,z-3,face,3,z+3,WOOD);a.box(face,4,z-3,face,4,z+3,Blocks.WOODEN_SLAB.getDefaultState());
        for(int pz=z-7;pz<=z+7;pz++) {
            int rise=(7-Math.abs(pz-z))/3;
            a.box(x-11,8+rise,pz,x+11,8+rise,pz,Math.floorMod(pz-z,4)<2?CREAM:type==1?TEAL:BLUE);
        }
        a.box(x-11,8,z-7,x+11,8,z-7,BLUE);a.box(x-11,8,z+7,x+11,8,z+7,BLUE);
        a.chest(x-4,2,z-4,type==0?"kappa_parts":type==1?"kappa_tools":"cucumber_supplies");
        a.chest(x+4,2,z-4,type==0?"kappa_tools":type==1?"kappa_parts":"village_pantry");
        a.block(x,3,z-4,type==2?Blocks.FURNACE.getDefaultState():Blocks.CRAFTING_TABLE.getDefaultState());
        a.table(x-3,2,z+3,7);
        a.block(x-2,5,z+3,type==0?Blocks.PISTON.getDefaultState():type==1?Blocks.CAULDRON.getDefaultState():Blocks.MELON_BLOCK.getDefaultState());
        a.block(x+2,5,z+3,type==0?Blocks.DISPENSER.getDefaultState():type==1?Blocks.ANVIL.getDefaultState():Blocks.PUMPKIN.getDefaultState());
        a.block(x,7,z,ModBlocks.LIBRARY_LAMP.getDefaultState());a.box(x,8,z,x,9,z,FRAME);
        a.room((west?"西":"东")+new String[]{"机关零件摊","水具与工具摊","种苗与食材摊"}[type],x,2,z);
    }
    private static void refreshment(GensokyoArchitecture a,int x,int z) {
        a.box(x-10,2,z-4,x+10,2,z+4,WOOD);
        for(int px:new int[]{x-10,x+10})a.box(px,3,z-4,px,7,z-4,LOG);
        a.box(x-11,8,z-5,x+11,8,z+5,TEAL);a.box(x-9,9,z-3,x+9,9,z+3,CREAM);
        for(int px:new int[]{x-10,x+10})a.box(px,3,z+4,px,7,z+4,LOG);
        a.table(x-7,2,z+2,11);a.chest(x-7,2,z-3,"night_stall");
        a.block(x+6,3,z-3,Blocks.FURNACE.getDefaultState());a.block(x+8,3,z-3,Blocks.CAULDRON.getDefaultState());
        a.room((x<0?"西":"东")+"茶饮摊",x,2,z);
    }
    private static void workshop(GensokyoArchitecture a,int left,int right,boolean west) {
        int mid=(left+right)/2;
        a.box(left,2,-40,right,17,-20,WHITE);a.box(left+1,3,-39,right-1,16,-21,AIR);
        a.box(left,2,-40,right,2,-20,WOOD);a.box(left,10,-40,right,10,-20,WOOD);
        for(int x=left;x<=right;x+=6)for(int z:new int[]{-40,-20}) {
            a.box(x,3,z,x,17,z,LOG);
            if(x+4<right)for(int f:new int[]{2,10})a.box(x+1,f+3,z,x+4,f+5,z,PAPER);
        }
        a.gable(left-2,-42,right+2,-18,18);
        a.openZ(mid+8,-20,2,3,5);
        a.box(mid-2,10,-32,mid+2,10,-23,AIR);a.stairsSouth(mid,-32,2,10,2);
        for(int x:new int[]{left+3,right-3}) {
            for(int z:new int[]{-37,-30})a.chest(x,2,z,west?"kappa_parts":"kappa_tools");
            a.block(x,3,-24,west?Blocks.CRAFTING_TABLE.getDefaultState():Blocks.FURNACE.getDefaultState());
        }
        a.table(left+7,2,-37,8);a.table(right-13,2,-37,8);
        a.table(left+7,2,-27,7);a.table(right-13,2,-27,7);
        a.block(left+9,5,-27,Blocks.ANVIL.getDefaultState());a.block(right-9,5,-27,Blocks.PISTON.getDefaultState());
        // Upper sleeping and stock areas leave the stair landing and bed sides clear.
        a.bed(left+4,10,-35);a.bed(left+8,10,-35);
        a.chest(left+3,10,-25,"village_pantry");a.chest(left+8,10,-25,"kappa_tools");
        a.table(left+4,10,-30,6);
        for(int x:new int[]{left+14,right-15}) {
            a.box(x,11,-39,x,16,-21,WOOD);a.openX(x,x==left+14?-28:-35,10,2,4);
        }
        a.box(left+4,11,-28,left+9,11,-28,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
        for(int z:new int[]{-39,-23})a.box(right-10,11,z,right-10,14,z,LOG);
        a.box(right-10,14,-39,right-6,14,-23,WOOD);
        a.box(right-2,11,-37,right-2,13,-25,WOOD);
        a.box(right-4,11,-37,right-4,13,-32,Blocks.BOOKSHELF.getDefaultState());
        for(int z:new int[]{-37,-32,-26})a.chest(right-8,10,z,west?"kappa_parts":"kappa_tools");
        for(int f:new int[]{2,10})for(int x:new int[]{left+8,right-8}) {
            a.block(x,f+6,-29,ModBlocks.LIBRARY_LAMP.getDefaultState());a.block(x,f+7,-29,FRAME);
        }
        a.room(west?"发明修配间":"展品整备间",mid+8,2,-24);
        a.room(west?"商贩休息与零件库":"值守起居与器具库",mid+8,10,-34);
        a.room(west?"西楼寝居":"东楼寝居",left+6,10,-33);
    }
    private static void pool(GensokyoArchitecture a,int side) {
        int left=side<0?-24:9,right=side<0?-9:24;
        a.box(left,1,-6,right,3,19,STONE);
        a.box(left+1,2,-5,right-1,2,18,Blocks.WATER.getDefaultState());
        a.box(left+1,3,-5,right-1,4,18,AIR);
        // Opposing entry steps, with the outside paths kept four blocks wide.
        a.box(left+4,3,19,right-4,3,19,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
        a.block(side<0?left:right,4,4,Blocks.DISPENSER.getDefaultState().withProperty(BlockDispenser.FACING,side<0?EnumFacing.EAST:EnumFacing.WEST));
        a.chest(side<0?left-2:right+2,2,8,"kappa_tools");
        a.room(side<0?"舟具试水池":"机关试水池",side<0?left-2:right+2,2,13);
    }
    private static void steamYard(GensokyoArchitecture a) {
        a.box(-12,2,-39,12,2,-19,METAL);
        for(int x=-7;x<=7;x++)for(int z=-33;z<=-21;z++) {
            double d=x*x/49.0+(z+27)*(z+27)/36.0;
            if(d<=1)a.box(x,3,z,x,9,z,d>.60?Blocks.HARDENED_CLAY.getDefaultState():AIR);
            if(d<=1)a.block(x,10,z,METAL);
        }
        for(int x:new int[]{-9,9}) {a.box(x,3,-27,x,14,-27,METAL);a.box(x,14,-30,x,14,-24,METAL);}
        a.box(-2,11,-29,2,17,-25,Blocks.HARDENED_CLAY.getDefaultState());
        a.box(-3,18,-30,3,18,-24,METAL);
        a.chest(-10,2,-22,"kappa_parts");a.chest(10,2,-22,"kappa_tools");
        a.block(-10,3,-34,Blocks.FURNACE.getDefaultState());a.block(10,3,-34,Blocks.CRAFTING_TABLE.getDefaultState());
        a.room("蒸汽总管与检修台",0,2,-17);
    }
    private static void entry(GensokyoArchitecture a) {
        for(int x:new int[]{-11,11}) {
            a.box(x-1,2,39,x+1,12,41,LOG);a.block(x,13,40,SLAB);
            a.block(x<0?x+2:x-2,9,40,ModBlocks.LIBRARY_LAMP.getDefaultState());
        }
        a.box(-13,11,39,13,12,41,TEAL);a.box(-6,10,41,6,12,41,WOOD);
        a.sign(0,11,42,EnumFacing.SOUTH,"未来水妖集市","河童发明展销");
        for(int x:new int[]{-21,21}) {
            a.box(x,2,38,x,12,38,FRAME);
            a.box(x+1,10,38,x+5,12,38,x<0?TEAL:BLUE);
            a.block(x+4,9,38,CREAM);
        }
    }
}
