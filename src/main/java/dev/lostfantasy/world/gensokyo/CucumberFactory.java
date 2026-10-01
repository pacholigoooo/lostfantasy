package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Terraced cultivation, packing rooms and a separate stair/service spine. */
final class CucumberFactory {
    private static final net.minecraft.block.state.IBlockState FRAME=Blocks.STONE.getStateFromMeta(5),
            GLASS=Blocks.GLASS.getDefaultState(),BRICK=Blocks.BRICK_BLOCK.getDefaultState();
    private CucumberFactory() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.CUCUMBER_FACTORY);
        a.box(-44,-2,-39,38,0,40,STONE);a.box(-43,1,-38,37,31,39,AIR);
        shell(a,-40,-29,18,19,21);shell(a,18,-29,34,21,21);
        for(int f:new int[]{0,7,14}) {
            a.box(-39,f,-28,33,f,20,STONE);
            a.openX(18,15,f,2,3);a.openX(18,-6,f,2,3);
            crops(a,f);services(a,f);
            a.room((f/7+1)+"层采摘廊",-25,f,-6);
            a.room((f/7+1)+"层灌水巡检",12,f,-19);
        }
        // Two flights occupy the same horizontal strip, seven blocks apart vertically.
        for(int f:new int[]{0,7})for(int rise=1;rise<=7;rise++) {
            int z=20-rise;
            a.box(23,f+rise,z,29,f+rise,z,Blocks.STONE_BRICK_STAIRS.getDefaultState()
                    .withProperty(net.minecraft.block.BlockStairs.FACING,EnumFacing.NORTH));
            a.box(23,f+rise+1,z,29,f+rise+3,z,AIR);
        }
        a.room("二层楼梯平台",26,7,10);a.room("三层楼梯平台",26,14,10);
        // Narrow glazed ridges give the greenhouse a different silhouette from the residences.
        for(int x=-40;x<=18;x++) {
            int bay=Math.floorMod(x+40,20),rise=Math.min(bay,20-bay)/3;
            a.box(x,22,-31,x,22+rise,21,GLASS);
            a.box(x,22+rise,-31,x,22+rise,21,(bay==0 || bay==10)?FRAME:GLASS);
        }
        a.box(18,22,-31,36,22,23,FRAME);
        for(int z=-27;z<=17;z+=11)a.box(-41,21,z,35,21,z,FRAME);
        workrooms(a);loading(a);
        a.box(-6,0,37,6,0,47,Blocks.GRAVEL.getDefaultState());
        a.openZ(0,36,0,3,3);a.openZ(0,23,0,3,3);a.openZ(0,19,0,3,3);
        a.box(-3,3,36,3,4,36,BRICK);a.sign(0,3,37,EnumFacing.SOUTH,"黄瓜栽培所","");
        for(int x:new int[]{-35,29})a.lamp(x,1,39);
        a.room("前庭",0,0,40);
        CucumberRailway.build(plan);
    }
    private static void shell(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int height) {
        a.box(x1,1,z1,x2,height,z2,GLASS);a.box(x1+1,1,z1+1,x2-1,height-1,z2-1,AIR);
        for(int y:new int[]{1,7,14,21}) {
            a.box(x1,y,z1,x2,y,z1,y==1?BRICK:FRAME);a.box(x1,y,z2,x2,y,z2,y==1?BRICK:FRAME);
            a.box(x1,y,z1,x1,y,z2,FRAME);a.box(x2,y,z1,x2,y,z2,FRAME);
        }
        for(int x=x1;x<=x2;x+=10)for(int z:new int[]{z1,z2})a.box(x,1,z,x,height,z,FRAME);
        for(int z=z1;z<=z2;z+=10)for(int x:new int[]{x1,x2})a.box(x,1,z,x,height,z,FRAME);
    }
    private static void crops(GensokyoArchitecture a,int floor) {
        for(int left:new int[]{-34,-22,-10,2})for(int start:new int[]{-23,-4}) {
            int end=start+14;
            a.box(left-1,floor+1,start-1,left+7,floor+1,end+1,STONE);
            a.box(left,floor+1,start,left+6,floor+1,end,
                    Blocks.FARMLAND.getDefaultState().withProperty(BlockFarmland.MOISTURE,7));
            a.box(left+3,floor+1,start,left+3,floor+1,end,Blocks.WATER.getDefaultState());
            for(int x=left;x<=left+6;x++)if(x!=left+3)for(int z=start;z<=end;z++)
                a.block(x,floor+2,z,ModBlocks.CUCUMBER_CROP.getDefaultState().withProperty(BlockCrops.AGE,4+Math.floorMod(x+z+floor,4)));
            // Continuous ordinary block lighting keeps both sides of every bed productive indoors.
            a.box(left+3,floor+4,start,left+3,floor+4,end,Blocks.SEA_LANTERN.getDefaultState());
            a.box(left+3,floor+5,start,left+3,floor+5,end,Blocks.IRON_BARS.getDefaultState());
            for(int z:new int[]{start-1,end+1})a.box(left+3,floor+2,z,left+3,floor+5,z,Blocks.IRON_BARS.getDefaultState());
        }
    }
    private static void services(GensokyoArchitecture a,int floor) {
        a.box(19,floor+1,-1,33,floor+6,-1,WHITE);a.openZ(26,-1,floor,2,3);
        a.box(19,floor+1,-14,33,floor+6,-14,WHITE);a.openZ(26,-14,floor,1,3);
        if(floor==0) {
            VillageJoinery.lowDesk(a,21,0,-10,8);a.chest(31,0,-11,"cucumber_supplies");
            a.block(32,1,-4,Blocks.CRAFTING_TABLE.getDefaultState());
            a.box(20,1,-26,22,1,-20,WOOD);a.box(31,1,-26,32,2,-20,Blocks.BOOKSHELF.getDefaultState());
            a.block(22,2,-25,ModBlocks.RESEARCH_NOTES.getDefaultState());
            a.room("生长记录",26,0,-6);a.room("值守休息",26,0,-20);
        } else if(floor==7) {
            for(int x:new int[]{21,31})for(int z:new int[]{-24,-18,-10,-4})a.chest(x,floor,z,"cucumber_supplies");
            a.block(27,8,-27,Blocks.CRAFTING_TABLE.getDefaultState());
            a.room("种子库",26,7,-7);a.room("育苗用品",26,7,-20);
        } else {
            a.box(20,15,-27,24,17,-17,STONE);a.box(21,17,-26,23,17,-18,Blocks.WATER.getDefaultState());
            a.box(29,15,-27,32,17,-17,STONE);a.box(30,17,-26,31,17,-18,Blocks.WATER.getDefaultState());
            a.chest(31,14,-9,"kourindou_tools");a.block(21,15,-9,Blocks.CAULDRON.getStateFromMeta(3));
            a.block(22,15,-4,Blocks.CRAFTING_TABLE.getDefaultState());
            a.room("供水检修",26,14,-20);a.room("工具间",26,14,-6);
        }
        for(int z:new int[]{-23,-7,7})a.block(26,floor+6,z,Blocks.SEA_LANTERN.getDefaultState());
    }
    private static void workrooms(GensokyoArchitecture a) {
        a.box(-40,1,23,34,6,36,BRICK);a.box(-39,1,24,33,5,35,AIR);
        a.box(-42,7,21,36,7,38,FRAME);
        for(int x:new int[]{-14,12}) {a.box(x,1,24,x,5,35,WHITE);a.openX(x,29,0,1,3);}
        for(int x=-36;x<=30;x+=11)a.box(x,2,36,x+6,4,36,GLASS);
        for(int x=-36;x<=-20;x+=4)a.block(x,1,25,Blocks.CAULDRON.getStateFromMeta(3));
        a.box(-37,1,33,-21,1,34,STONE);a.chest(-17,0,33,"cucumber_supplies");
        a.box(-10,1,25,8,1,26,WOOD);a.block(-8,2,25,Blocks.CRAFTING_TABLE.getDefaultState());
        for(int x:new int[]{-9,7})a.chest(x,0,33,"cucumber_supplies");
        for(int x:new int[]{17,23,29})a.chest(x,0,25,"cucumber_supplies");
        a.box(17,1,33,24,2,34,Blocks.HAY_BLOCK.getDefaultState());a.block(29,1,33,Blocks.CRAFTING_TABLE.getDefaultState());
        for(int x:new int[]{-29,-3,24})a.block(x,6,29,Blocks.SEA_LANTERN.getDefaultState());
        a.room("洗拣间",-28,0,29);a.room("包装间",-4,0,29);a.room("成品库",23,0,29);
    }
    private static void loading(GensokyoArchitecture a) {
        a.box(17,0,-40,35,0,-29,STONE);a.openZ(26,-29,0,2,4);
        for(int x:new int[]{18,34})for(int z:new int[]{-39,-30})a.box(x,1,z,x,5,z,FRAME);
        a.box(16,6,-41,36,6,-28,FRAME);a.block(27,5,-35,Blocks.SEA_LANTERN.getDefaultState());
        a.chest(20,0,-37,"cucumber_supplies");a.chest(20,0,-32,"kourindou_tools");
        a.room("装卸站",30,0,-34);
    }
}
