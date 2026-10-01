package dev.lostfantasy.world.gensokyo;

import net.minecraft.init.Blocks;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** The main shrine house, an open worship court and shaded side gardens. */
final class HakureiShrine {
    static void build(GensokyoBlueprint blueprint) {
        GensokyoArchitecture a=new GensokyoArchitecture(blueprint,GensokyoAtlas.HAKUREI);
        // Raised perimeter, with the front gate and its approach left open.
        a.box(-78,0,-79,78,0,79,Blocks.GRASS.getDefaultState());
        a.box(-77,1,-78,77,2,-78,STONE);
        a.box(-78,1,-78,-78,2,78,STONE);a.box(78,1,-78,78,2,78,STONE);
        a.box(-78,1,78,-14,2,78,STONE);a.box(14,1,78,78,2,78,STONE);
        a.box(-9,0,-19,9,0,86,Blocks.GRAVEL.getDefaultState());
        a.box(-42,0,18,56,0,49,Blocks.GRAVEL.getDefaultState());
        for(int x=-6;x<=6;x++)for(int z=19;z<=86;z++)
            a.block(x,0,z,Math.floorMod(x+z/3,7)==0?Blocks.STONEBRICK.getStateFromMeta(2):STONE);
        HakureiMainHall.build(a);
        // Storehouse and working yard.
        a.hall(-65,-47,-43,-25,1,6,LOG);a.openZ(-54,-25,1,1,3);a.stairsSouth(-54,-24,0,1,2);
        ShrineRoofs.replaceGable(a,-68,-50,-40,-22,8,false,false);
        a.room("储藏室",-54,1,-31);
        for(int x=-61;x<=-46;x+=3)a.chest(x,1,-43,"school_supplies");
        a.box(-62,2,-36,-62,4,-29,Blocks.HAY_BLOCK.getDefaultState());
        a.block(-47,2,-29,Blocks.CRAFTING_TABLE.getDefaultState());
        a.box(-60,0,-24,-49,0,20,Blocks.GRAVEL.getDefaultState());
        // Purification basin under an open pavilion.
        for(int x:new int[]{-57,-43})for(int z:new int[]{20,30})a.box(x,1,z,x,7,z,LOG);
        ShrineRoofs.gable(a,-60,18,-40,32,8,false,false);
        a.box(-52,1,23,-47,2,27,STONE);a.box(-51,3,24,-48,3,26,Blocks.WATER.getDefaultState());
        a.box(-52,3,23,-47,3,23,STONE);a.box(-52,3,27,-47,3,27,STONE);
        a.box(-52,3,24,-52,3,26,STONE);a.box(-47,3,24,-47,3,26,STONE);
        // Two torii frame the climb without filling the court with oversized gates.
        torii(a,71);torii(a,45);
        a.sign(11,2,73,net.minecraft.util.EnumFacing.SOUTH,"归途","空手潜行右键石座");
        for(int z:new int[]{29,55,79})for(int x:new int[]{-16,16})a.lamp(x,1,z);
        for(int x:new int[]{-70,69})for(int z:new int[]{-65,45})cherry(a,x,z);
        garden(a);
        for(int z:new int[]{-68,9,36})a.lamp(72,1,z);
        branchShrine(a);
        GardenScenery.arbour(a,-52,57,7,4);
        a.box(-55,0,33,-51,0,52,Blocks.GRAVEL.getDefaultState());
        GardenScenery.plantedBed(a,-67,8,6,8,0);
        GardenScenery.plantedBed(a,50,60,9,5,3);
        a.box(-66,1,-13,-62,2,-11,Blocks.LOG.getStateFromMeta(8));
        a.box(-66,3,-13,-62,3,-11,Blocks.WOODEN_SLAB.getStateFromMeta(1));
        a.room("树荫歇脚处",-52,0,57);
    }
    private static void branchShrine(GensokyoArchitecture a) {
        // A small wayside shrine beside the main buildings, reached from the west court.
        a.box(-36,0,-57,-32,0,19,Blocks.GRAVEL.getDefaultState());
        a.box(-42,0,-68,-26,0,-51,Blocks.GRAVEL.getDefaultState());
        a.box(-39,1,-65,-29,1,-58,STONE);
        a.box(-38,2,-64,-30,2,-59,WOOD);
        a.box(-37,3,-63,-31,5,-61,WOOD);
        a.box(-36,3,-62,-32,4,-61,AIR);
        for(int x:new int[]{-38,-30})a.box(x,3,-59,x,5,-59,LOG);
        a.box(-38,5,-59,-30,5,-59,DARK);
        // Closed inner doors, small offering shelf and carved roof edge.
        a.box(-35,3,-62,-33,4,-62,Blocks.OAK_FENCE.getDefaultState());
        a.box(-35,3,-60,-33,3,-60,Blocks.WOODEN_SLAB.getDefaultState());
        ShrineRoofs.gable(a,-40,-66,-28,-57,6,false,false);
        for(int x:new int[]{-38,-30})a.block(x,5,-58,LOG);
        a.box(-37,5,-58,-31,5,-58,dev.lostfantasy.ModBlocks.SHRINE_ROPE.getDefaultState());
        for(int x:new int[]{-36,-32})a.block(x,4,-58,dev.lostfantasy.ModBlocks.SHIDE.getDefaultState());
        a.stairsSouth(-34,-57,0,1,2);
        a.box(-35,1,-54,-33,1,-54,DARK);
        a.block(-34,2,-54,Blocks.IRON_TRAPDOOR.getDefaultState());
        a.sign(-34,1,-53,net.minecraft.util.EnumFacing.SOUTH,"守矢分社","");
        for(int x:new int[]{-42,-26})a.lamp(x,1,-53);
        a.room("守矢分社参拜位",-34,0,-51);
    }
    private static void torii(GensokyoArchitecture a,int z) {
        for(int x:new int[]{-11,11}) {a.box(x-1,1,z-1,x+1,2,z+1,STONE);a.box(x,3,z,x,12,z,RED);}
        a.box(-15,10,z,15,11,z,RED);a.box(-16,13,z-1,16,13,z+1,DARK);
        a.box(-17,14,z,17,14,z,SLAB);a.box(-2,11,z,2,12,z,DARK);
    }
    private static void cherry(GensokyoArchitecture a,int x,int z) {
        a.box(x,1,z,x,10,z,Blocks.LOG.getDefaultState());
        for(int[] arm:new int[][]{{-5,-2,8,5},{4,3,10,5},{0,-4,13,4}}) {
            for(int n=1;n<=5;n++)a.block(x+arm[0]*n/5,5+n,z+arm[1]*n/5,Blocks.LOG.getStateFromMeta(12));
            crown(a,x+arm[0],arm[2],z+arm[1],arm[3]);
        }
        crown(a,x,11,z,5);
    }
    private static void crown(GensokyoArchitecture a,int x,int y,int z,int radius) {
        for(int dy=-2;dy<=2;dy++)for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)
            if(dx*dx+dz*dz+dy*dy*4<=radius*radius && Math.floorMod(dx*13+dz*7+dy,19)!=0)
                a.block(x+dx,y+dy,z+dz,dev.lostfantasy.ModBlocks.CHERRY_LEAVES.getDefaultState());
    }
    private static void garden(GensokyoArchitecture a) {
        // The rear path reaches the fairies' tree without passing through the shrine house.
        a.box(9,0,-86,15,0,-54,Blocks.GRAVEL.getDefaultState());a.box(9,1,-86,15,3,-54,AIR);
        a.box(9,1,-79,15,2,-78,AIR);
        for(int rise=1;rise<=3;rise++) {
            int z=-54+rise;
            if(rise>1)a.box(10,1,z,14,rise-1,z,STONE);
            a.box(10,rise,z,14,rise,z,Blocks.STONE_BRICK_STAIRS.getDefaultState()
                    .withProperty(net.minecraft.block.BlockStairs.FACING,net.minecraft.util.EnumFacing.SOUTH));
        }
        // A low pond and stepping walk sit beside the living room's veranda.
        for(int x=35;x<=62;x++)for(int z=-43;z<=-19;z++) {
            double d=Math.pow((x-48)/13.0,2)+Math.pow((z+31)/11.0,2)+.12*Math.sin((x-35)*.43+(z+31)*.31);
            if(d<1) {a.box(x,-2,z,x,-1,z,STONE);a.block(x,0,z,Blocks.WATER.getDefaultState());}
            else if(d<1.22)a.block(x,0,z,Blocks.MOSSY_COBBLESTONE.getDefaultState());
        }
        a.box(29,0,-10,62,0,-6,Blocks.GRAVEL.getDefaultState());
        for(int z=-20;z<=-8;z+=3)a.box(59,0,z,61,0,z+1,STONE);
        for(int x:new int[]{37,56})a.box(x,1,-8,x+4,1,-8,Blocks.WOODEN_SLAB.getDefaultState());
        cherry(a,49,-57);cherry(a,43,5);cherry(a,-8,-66);
        GardenScenery.rocks(a,63,-34,-1);
        GardenScenery.pine(a,68,-19,-1);
        a.block(42,1,-33,Blocks.WATERLILY.getDefaultState());
        a.block(47,1,-38,Blocks.WATERLILY.getDefaultState());
        for(int[] patch:new int[][]{{-76,-54},{75,-46},{73,12},{-73,63}}) {
            a.box(patch[0]-1,0,patch[1]-1,patch[0]+1,0,patch[1]+1,Blocks.DIRT.getStateFromMeta(1));
            a.block(patch[0],1,patch[1],Blocks.TALLGRASS.getStateFromMeta(2));
        }
        a.room("池边庭园",58,0,-11);
    }
}
