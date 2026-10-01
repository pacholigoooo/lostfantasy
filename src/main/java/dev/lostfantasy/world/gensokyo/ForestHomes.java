package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Two forest households: a cluttered working shop and a carefully furnished doll-maker's home. */
final class ForestHomes {
    private static final IBlockState CREAM=Blocks.SANDSTONE.getStateFromMeta(2),WINDOW=Blocks.GLASS_PANE.getDefaultState();
    private ForestHomes() {}
    static void marisa(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.MARISA);
        ForestDetails.tree(a,26,-21,24,8);ForestDetails.tree(a,-25,-25,22,7);ForestDetails.tree(a,29,24,19,7);
        shell(a,-15,-13,15,14,2,16,WHITE,DARK);
        a.box(-14,9,-12,14,9,13,WOOD);
        roof(a,-18,-16,18,17,17,DARK,WHITE,.5);
        // Front room doubles as the shop. Furniture piles leave a winding but clear aisle.
        a.openZ(0,14,2,1,4);a.box(-5,2,15,5,2,18,WOOD);a.stairsSouth(0,19,0,2,3);
        a.box(-2,0,21,2,0,42,Blocks.GRAVEL.getDefaultState());
        for(int x:new int[]{-5,5})a.box(x,3,17,x,7,17,LOG);
        a.box(-6,8,14,6,8,19,DARK);
        a.box(-14,3,-3,8,8,-3,DARK);a.openZ(-2,-3,2,1,3);
        a.box(9,3,-10,13,13,-2,AIR);a.stairsSouth(11,-9,2,9,2);
        a.box(9,10,-2,13,10,-2,Blocks.OAK_FENCE.getDefaultState());
        a.box(-13,3,7,-5,3,7,WOOD);a.box(-13,4,7,-5,4,7,Blocks.WOODEN_SLAB.getStateFromMeta(8));
        a.block(-10,5,7,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.box(-14,3,-1,-14,7,5,Blocks.BOOKSHELF.getDefaultState());
        for(int[] p:new int[][]{{-11,11},{-8,10},{6,10},{11,10},{7,5}}) {
            a.chest(p[0],2,p[1]);a.box(p[0],5,p[1],p[0]+1,5,p[1],WOOD);
            a.box(p[0]+1,3,p[1],p[0]+1,5,p[1],LOG);
        }
        a.table(1,2,3,4);a.block(3,5,3,ModBlocks.ARMILLARY.getDefaultState());
        a.box(1,4,3,4,4,3,Blocks.WOODEN_SLAB.getStateFromMeta(8));
        a.block(2,3,5,Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
        a.room("雾雨魔法店",0,2,10);lamp(a,-4,7,5);lamp(a,7,7,9);
        a.box(-13,3,-12,-6,3,-12,WOOD);
        a.block(-13,4,-12,Blocks.FLOWER_POT.getDefaultState());
        a.block(-10,3,-11,Blocks.FURNACE.getDefaultState());a.block(-7,3,-11,Blocks.CAULDRON.getStateFromMeta(3));
        a.table(-3,2,-9,3);a.chest(4,2,-11);a.room("起居与厨房",-3,2,-6);lamp(a,-3,7,-7);
        // Upstairs: bedroom behind the partition, notes and stored books beside the landing.
        a.box(-14,10,-3,4,15,-3,WHITE);a.openZ(-5,-3,9,1,3);
        a.box(4,10,-12,4,15,-3,WHITE);a.openX(4,-7,9,1,3);
        a.bed(-10,9,-7);a.chest(-12,9,-4);a.room("寝室",-5,9,-7);
        a.box(-14,10,0,-14,14,11,Blocks.BOOKSHELF.getDefaultState());
        a.box(-13,10,12,-4,12,12,Blocks.BOOKSHELF.getDefaultState());
        a.table(-7,9,5,5);a.box(-7,11,5,-3,11,5,Blocks.WOODEN_SLAB.getStateFromMeta(8));
        a.block(-5,12,5,ModBlocks.RESEARCH_NOTES.getDefaultState());a.chest(6,9,10);
        a.room("实验记录与藏书",-3,9,2);lamp(a,-3,15,5);lamp(a,-6,15,-7);
        // Low side laboratory and a masonry flue, not a second large mansion.
        shell(a,-29,-8,-15,10,2,8,WHITE,DARK);roof(a,-31,-10,-15,12,9,DARK,WHITE,.45);
        a.box(-16,3,0,-14,6,3,AIR);
        a.box(-28,3,-7,-20,3,-7,WOOD);
        for(int x:new int[]{-26,-22})a.block(x,4,-7,Blocks.BREWING_STAND.getDefaultState());
        a.block(-26,3,-3,Blocks.CAULDRON.getStateFromMeta(3));a.block(-22,3,-3,Blocks.CAULDRON.getStateFromMeta(2));
        a.box(-28,3,-6,-27,21,-5,Blocks.BRICK_BLOCK.getDefaultState());
        a.block(-26,3,-5,Blocks.FURNACE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.EAST));
        a.chest(-27,2,6,"forest_alchemy");a.chest(-19,2,6,"forest_books");a.room("蘑菇调配室",-22,2,4);lamp(a,-22,7,2);
        for(int x=-30;x<=-17;x++)for(int z=16;z<=27;z++) {
            double d=Math.pow((x+24)/7.0,2)+Math.pow((z-21)/6.0,2);
            if(d>1+.15*Math.sin(x+z*.7))continue;
            a.block(x,0,z,Blocks.DIRT.getStateFromMeta(2));
            if(Math.floorMod(x*11+z*7,9)<2)a.block(x,1,z,
                    (x+z)%2==0?Blocks.RED_MUSHROOM.getDefaultState():Blocks.BROWN_MUSHROOM.getDefaultState());
        }
        GardenScenery.rocks(a,22,30,-1);
        a.box(-27,1,29,-20,1,30,Blocks.LOG.getStateFromMeta(4));
        a.box(-26,2,29,-21,2,29,Blocks.LOG.getStateFromMeta(4));
        for(int x=-12;x<=-5;x+=3) {
            a.chest(x,0,25);a.block(x,1,28,Blocks.HAY_BLOCK.getDefaultState());
        }
        // Creepers are confined to solid exterior wall faces and spare the entrance.
        ForestDetails.vinesZ(a,-14,10,-14,3,15,EnumFacing.SOUTH);
        ForestDetails.vinesZ(a,6,13,15,3,14,EnumFacing.NORTH);
        // Narrow reagent shelves are fixed to the laboratory's rear wall.
        for(int x:new int[]{-24,-20}) {
            a.block(x,6,-7,Blocks.WOODEN_SLAB.getStateFromMeta(9));
            a.block(x,7,-7,Blocks.FLOWER_POT.getDefaultState());
        }
        a.box(2,10,12,9,11,12,Blocks.BOOKSHELF.getDefaultState());
        a.block(7,12,12,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.box(-12,0,19,-9,0,29,Blocks.GRAVEL.getDefaultState());
        GardenScenery.bench(a,7,0,23,5,EnumFacing.NORTH);
        GardenScenery.plantedBed(a,15,-22,4,5,8);
    }
    static void alice(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.ALICE);
        ForestDetails.tree(a,-33,-25,26,8);ForestDetails.tree(a,34,-27,24,8);ForestDetails.tree(a,-34,25,24,7);
        shell(a,-16,-15,16,15,2,17,CREAM,WHITE);
        a.box(-15,10,-14,15,10,14,Blocks.PLANKS.getStateFromMeta(2));
        roof(a,-19,-18,19,18,18,ROOF,CREAM,.7);
        a.openZ(0,15,2,2,5);a.box(-6,2,16,6,2,21,CREAM);a.stairsSouth(0,22,0,2,3);
        for(int x:new int[]{-6,6})a.box(x,3,19,x,8,19,CREAM);
        a.box(-7,9,15,7,9,22,SLAB);
        a.box(-2,0,24,2,0,47,Blocks.GRAVEL.getDefaultState());
        a.box(10,3,5,14,15,12,AIR);a.stairsSouth(12,5,2,10,2);
        a.box(10,11,12,14,11,12,Blocks.SPRUCE_FENCE.getDefaultState());
        // Sitting room, fireplace and a table reached from both the door and the workshop.
        a.box(-14,3,-5,-11,6,-2,STONE);a.box(-13,4,-1,-12,5,-1,Blocks.IRON_BARS.getDefaultState());
        a.box(-13,4,-2,-12,5,-2,LIGHT);a.box(-14,7,-5,-12,29,-3,STONE);
        for(int z=5;z<=10;z++)a.block(-12,3,z,Blocks.QUARTZ_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.WEST));
        a.table(-7,2,6,4);a.box(-7,4,6,-4,4,6,Blocks.WOODEN_SLAB.getStateFromMeta(8));
        a.block(-6,5,6,Blocks.FLOWER_POT.getDefaultState());
        dollShelf(a,-12,2,-13,8);dollShelf(a,5,2,-13,8);
        a.box(-13,2,1,-3,2,12,Blocks.WOOL.getStateFromMeta(3));
        a.box(-2,3,0,-2,8,13,CREAM);a.openX(-2,7,2,1,4);
        a.box(-15,3,-2,-3,8,-2,CREAM);a.openZ(-7,-2,2,1,4);
        a.box(3,3,-14,3,8,-3,CREAM);a.box(3,3,-3,15,8,-3,CREAM);a.openZ(8,-3,2,1,4);
        a.box(5,3,-11,14,3,-11,CREAM);a.block(6,3,-10,Blocks.FURNACE.getDefaultState());
        a.block(13,3,-10,Blocks.CAULDRON.getStateFromMeta(3));a.chest(13,2,-5);
        a.table(-10,2,-8,4);a.box(-10,4,-8,-7,4,-8,Blocks.WOODEN_SLAB.getStateFromMeta(8));
        a.block(-9,5,-8,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.room("玄关与楼梯厅",0,2,7);a.room("茶点起居室",-6,2,10);
        a.room("藏书与人偶陈列",-6,2,-7);a.room("厨房",8,2,-7);lamp(a,0,8,3);
        lamp(a,-7,8,6);lamp(a,-5,8,-8);lamp(a,8,8,-7);
        a.box(-16,3,-15,16,8,-15,CREAM);a.openZ(0,-15,2,2,4);
        // Rear workshop is connected under its own lower roof.
        shell(a,-20,-29,20,-15,2,9,CREAM,WHITE);roof(a,-22,-31,22,-15,10,ROOF,CREAM,.3);
        a.box(-2,3,-16,2,7,-14,AIR);
        for(int x:new int[]{-15,6}) {
            a.table(x,2,-22,7);a.box(x,4,-22,x+6,4,-22,Blocks.WOODEN_SLAB.getStateFromMeta(8));
            for(int dx:new int[]{1,4})a.block(x+dx,5,-22,ModBlocks.DOLL_DISPLAY.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
            a.chest(x,2,-27,"doll_materials");a.block(x+3,3,-27,Blocks.CRAFTING_TABLE.getDefaultState());
        }
        a.box(-19,3,-26,-19,6,-18,Blocks.BOOKSHELF.getDefaultState());
        a.box(19,3,-26,19,6,-18,Blocks.WOOL.getStateFromMeta(0));
        a.box(-5,3,-27,-2,4,-26,WOOD);a.box(2,3,-19,5,4,-18,WOOD);
        a.block(-4,5,-27,ModBlocks.DOLL_DISPLAY.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
        a.block(3,5,-19,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.room("人偶制作室",0,2,-23);lamp(a,-9,8,-23);lamp(a,9,8,-23);
        // Upper bedrooms and a work table share a short landing.
        a.box(-15,11,-1,15,16,-1,CREAM);a.openZ(-8,-1,10,1,3);a.openZ(8,-1,10,1,3);
        a.box(0,11,-14,0,16,-2,CREAM);
        a.bed(-11,10,-9);a.bed(9,10,-9);a.chest(-5,10,-3);a.chest(13,10,-3);
        a.room("客寝室",-7,10,-7);a.room("寝室",7,10,-7);
        a.table(-12,10,8,5);dollShelf(a,-12,10,13,7);a.room("裁缝与书信室",-6,10,6);
        lamp(a,-5,16,6);lamp(a,-7,16,-7);lamp(a,7,16,-7);
        // Glazed eastern bay supports a small balcony on the second floor.
        shell(a,16,-8,27,8,2,9,CREAM,WHITE);
        a.box(17,10,-8,27,10,8,CREAM);a.box(27,11,-8,27,11,8,Blocks.IRON_BARS.getDefaultState());
        for(int z:new int[]{-8,8})a.box(17,11,z,26,11,z,Blocks.IRON_BARS.getDefaultState());
        a.box(15,3,-2,17,7,2,AIR);a.box(15,11,-2,17,15,2,AIR);
        a.table(21,2,1,3);dollShelf(a,19,2,-6,6);a.room("窗边陈列室",21,2,4);
        a.room("林间阳台",22,10,0);lamp(a,22,8,-1);
        for(int side:new int[]{-1,1})for(int x=-7;x<=7;x++)for(int z=-5;z<=5;z++) {
            double d=x*x/49.0+z*z/25.0;
            if(d>1)continue;
            if(d>.69)a.block(side*20+x,1,30+z,Blocks.LEAVES.getStateFromMeta(4));
            else if(Math.floorMod(x+z,3)!=0)a.block(side*20+x,1,30+z,Blocks.RED_FLOWER.getStateFromMeta(z<0?6:3));
        }
        for(int x:new int[]{-7,7})a.lamp(x,1,32);
        aliceDetails(a);
    }
    private static void aliceDetails(GensokyoArchitecture a) {
        // Sewing materials occupy the existing rear workshop; the through aisle stays in the middle.
        for(int z=-26;z<=-18;z+=3) {
            a.block(18,3,z,Blocks.WOOL.getStateFromMeta(z==-26?11:z==-23?0:14));
            a.block(18,4,z,Blocks.WOODEN_SLAB.getStateFromMeta(10));
        }
        a.box(-12,12,8,-8,12,8,Blocks.WOODEN_SLAB.getStateFromMeta(10));
        a.block(-10,13,8,ModBlocks.RESEARCH_NOTES.getDefaultState());
        InteriorFinishes.rug(a,-13,2,-4,11,2,3);
        InteriorFinishes.rug(a,-13,-12,-3,-4,10,3);
        InteriorFinishes.rug(a,3,-12,13,-4,10,0);
        // A sheltered tea seat looks towards the forest rather than into the front doorway.
        GardenScenery.arbour(a,21,41,6,3);
        a.box(3,0,40,16,0,42,Blocks.GRAVEL.getDefaultState());
        GardenScenery.bench(a,19,10,5,5,EnumFacing.SOUTH);
        a.room("林下茶庭",21,0,41);
    }
    private static void shell(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int floor,int ceiling,IBlockState wall,IBlockState frame) {
        a.box(x1,0,z1,x2,floor-1,z2,STONE);a.box(x1,floor,z1,x2,floor,z2,WOOD);
        a.box(x1,floor+1,z1,x2,ceiling,z2,wall);a.box(x1+1,floor+1,z1+1,x2-1,ceiling-1,z2-1,AIR);
        for(int y:new int[]{floor+1,ceiling-1}) {
            a.box(x1,y,z1,x2,y,z1,frame);a.box(x1,y,z2,x2,y,z2,frame);
        }
        for(int x=x1;x<=x2;x+=6)for(int z:new int[]{z1,z2}) {
            a.box(x,floor+1,z,x,ceiling,z,frame);
            if(x+4<x2)for(int base=floor;base+5<ceiling;base+=8)a.box(x+1,base+2,z,x+4,base+5,z,WINDOW);
        }
        for(int z=z1;z<=z2;z+=6)for(int x:new int[]{x1,x2}) {
            a.box(x,floor+1,z,x,ceiling,z,frame);
            if(z+4<z2)for(int base=floor;base+5<ceiling;base+=8)a.box(x,base+2,z+1,x,base+5,z+4,WINDOW);
        }
    }
    private static void roof(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int base,IBlockState tile,IBlockState infill,double slope) {
        int middle=(x1+x2)/2,half=(x2-x1)/2;
        for(int x=x1;x<=x2;x++) {
            int rise=(int)((half-Math.abs(x-middle))*slope);
            a.box(x,base+rise,z1,x,base+rise,z2,tile);
            if(rise>0)for(int z:new int[]{z1+3,z2-3})a.box(x,base,z,x,base+rise-1,z,infill);
        }
        a.box(middle,base+(int)(half*slope)+1,z1,middle,base+(int)(half*slope)+1,z2,SLAB);
    }
    private static void lamp(GensokyoArchitecture a,int x,int y,int z) {
        a.block(x,y,z,LIGHT);a.block(x,y-1,z,Blocks.WOODEN_SLAB.getStateFromMeta(5));
    }
    private static void dollShelf(GensokyoArchitecture a,int x,int floor,int z,int length) {
        a.box(x,floor+1,z,x+length-1,floor+2,z,DARK);
        for(int dx=0;dx<length;dx+=3)a.block(x+dx,floor+3,z,
                ModBlocks.DOLL_DISPLAY.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.SOUTH));
    }
}
