package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** The inhabited blue-roofed city, its springs and the two surface approaches. */
final class OldHellCity {
    static final int HALF_WIDTH=382,SOUTH=374,EXIT_Z=388,BATH_Z=318;
    static final int EAST_GATE_X=486,EAST_ARRIVAL_X=475;
    private static final IBlockState LANTERN=ModBlocks.RED_LANTERN.getDefaultState(),FENCE=Blocks.DARK_OAK_FENCE.getDefaultState();
    private OldHellCity() {}
    static GensokyoBlueprint create() {
        GensokyoBlueprint plan=new GensokyoBlueprint();GensokyoArchitecture a=at(plan,0,0,"旧都");
        approaches(a);streets(a);OldCapitalHomes.build(plan);
        bathhouse(at(plan,0,BATH_Z,"旧地狱温泉"));
        guesthouse(at(plan,-246,BATH_Z,"旧都·旅舍"));
        banquet(at(plan,246,BATH_Z,"旧都·宴会所"));
        sharedCourt(at(plan,-124,BATH_Z,"旧都·西街公井"));
        sharedCourt(at(plan,124,BATH_Z,"旧都·东街公井"));
        for(int side:new int[]{-1,1}) {
            GensokyoArchitecture end=at(plan,side*352,BATH_Z,"旧都·街尾歇脚处");
            end.box(-20,0,-28,20,0,28,STONE);
            OldCapitalJoinery.roof(end,-15,-12,15,12,8,false);
            for(int x:new int[]{-15,15})for(int z:new int[]{-12,12})end.box(x,1,z,x,7,z,LOG);
            end.box(-15,7,0,15,7,0,DARK);
            for(int x:new int[]{-8,8})end.block(x,6,0,LANTERN);
            OldCapitalJoinery.bench(end,-11,4,0,8,EnumFacing.NORTH);
            OldCapitalJoinery.bench(end,4,4,0,8,EnumFacing.NORTH);
            end.room("街亭",0,0,0);
        }
        plan.seal();return plan;
    }
    private static GensokyoArchitecture at(GensokyoBlueprint p,int x,int z,String name) {return new GensokyoArchitecture(p,OldHellWorld.ORIGIN,x,z,name);}
    private static void approaches(GensokyoArchitecture a) {
        a.box(-60,-3,-181,-44,13,-110,Blocks.STONE.getDefaultState());
        a.box(414,-3,68,494,14,92,Blocks.STONE.getDefaultState());
        a.box(-56,-2,-181,-48,0,-96,STONE);a.box(-56,1,-181,-48,7,-96,AIR);
        a.box(-56,-2,-103,6,0,-95,STONE);a.box(-56,1,-103,6,7,-95,AIR);
        a.box(-6,-2,-102,6,0,-70,STONE);a.box(-6,1,-102,6,8,-70,AIR);
        a.box(366,-2,76,490,0,84,STONE);a.box(366,1,76,490,9,84,AIR);
        for(int z=-161;z<=-107;z+=18)bracket(a,-48,z,3);
        for(int x=386;x<=476;x+=18)bracket(a,x,85,3);
        a.box(-47,1,-169,-47,5,-169,LOG);a.box(482,1,86,482,5,86,LOG);
        a.sign(-48,4,-169,EnumFacing.WEST,"地狱的深道","幻想风穴 ↑");
        a.sign(482,4,85,EnumFacing.NORTH,"地灵虹洞","魔法森林");
        a.box(-7,-1,-71,7,1,-14,STONE);
        for(int z=-69;z<=-17;z++) {
            int sag=(int)Math.round(4+12*Math.pow(Math.abs(z+43)/27.0,2));
            for(int x:new int[]{-6,6})a.box(x,-sag,z,x,-2,z,STONE);
        }
        for(int x:new int[]{-7,7}) {
            a.box(x,2,-71,x,3,-14,FENCE);
            for(int z=-68;z<=-17;z+=17)bracket(a,x,z,5);
        }
        for(int z:new int[]{-150,-108,-82})a.room("风穴来路"+z,z<-95?-52:0,0,z);
        a.room("跨河桥",0,1,-43);a.room("虹洞来路",436,0,80);
    }
    private static void streets(GensokyoArchitecture a) {
        a.box(-8,-1,-13,8,0,290,STONE);
        a.box(-8,-1,366,8,0,EXIT_Z,STONE);
        for(int z:new int[]{-2,82,168,254,366})a.box(-HALF_WIDTH,-1,z-4,HALF_WIDTH,0,z+4,STONE);
        for(int side:new int[]{-1,1}) {
            for(int lane:new int[]{60,76,124,172,246,310,378}) {
                int x=side*lane,half=lane==60 || lane==124 || lane==246?2:3;
                a.box(x-half,-1,-5,x+half,0,lane==60?254:370,STONE);
            }
            for(int center:new int[]{124,246,352})a.box(side*center-6,-1,254,side*center+6,0,366,STONE);
            // The bathhouse is bypassed on either side on the way to Chireiden.
            int x=side*76;a.box(x-4,-1,254,x+4,0,366,STONE);
        }
        a.box(-62,0,270,62,0,288,STONE);
        for(int z:new int[]{5,47,87,128,174,214,260,280})for(int x:new int[]{-10,10})bracket(a,x,z,5);
        for(int side:new int[]{-1,1}) {
            for(int lane:new int[]{172,310})for(int z:new int[]{7,91,176,263,354})bracket(a,side*(lane+4),z,5);
            for(int x:new int[]{60,124,246,378})for(int z:new int[]{-6,78,164,250,362})bracket(a,side*(x+4),z,4);
        }
        for(int z:new int[]{45,128,260}) {
            a.box(-10,10,z,10,10,z,DARK);
            for(int x:new int[]{-10,10})a.box(x,1,z,x,9,z,LOG);
            for(int x=-6;x<=6;x+=4)a.block(x,9,z,LANTERN);
        }
        a.room("入口广场",0,0,0);a.room("灯笼街",0,0,80);a.room("温泉前庭",0,0,278);
        a.room("地灵殿去路",0,0,380);
    }
    private static void bracket(GensokyoArchitecture a,int x,int z,int h) {
        a.box(x,1,z,x,h,z,LOG);a.block(x+1,h,z,DARK);a.block(x+1,h-1,z,LANTERN);
    }
    private static void sharedCourt(GensokyoArchitecture a) {
        a.box(-26,0,-30,26,0,30,STONE);
        a.box(8,-2,6,14,1,12,STONE);
        a.box(9,-1,7,13,0,11,Blocks.WATER.getDefaultState());
        a.box(9,1,7,13,1,11,AIR);
        for(int x:new int[]{8,14})a.box(x,2,9,x,5,9,LOG);
        a.box(8,6,9,14,6,9,DARK);
        OldCapitalJoinery.roof(a,7,5,15,13,7,false);
        OldCapitalJoinery.roof(a,-22,-12,-4,4,7,false);
        for(int x:new int[]{-22,-4})for(int z:new int[]{-12,4})a.box(x,1,z,x,6,z,LOG);
        a.box(-22,6,4,-4,6,4,DARK);a.block(-13,5,4,LANTERN);
        a.table(-18,0,-3,9);a.chest(-20,0,1,"old_hell_trade");
        a.block(-8,1,1,Blocks.CRAFTING_TABLE.getDefaultState());
        OldCapitalJoinery.bench(a,5,-14,0,9,EnumFacing.SOUTH);
        a.room("公井院场",0,0,-6);a.room("修具棚",-12,0,0);
    }
    private static void civicHall(GensokyoArchitecture a,int x1,int z1,int x2,int z2,int height) {
        a.hall(x1,z1,x2,z2,2,height,LOG);
        int roof=height+3;
        a.box(x1-4,roof,z1-4,x2+4,roof+(z2-z1)/2+6,z2+4,AIR);
        OldCapitalJoinery.roof(a,x1,z1,x2,z2,roof,false);
    }
    private static void bathhouse(GensokyoArchitecture a) {
        civicHall(a,-49,-26,49,28,13);a.openZ(0,-26,2,3,5);
        // Northern entry has a broad pair of steps down to the spring street.
        for(int step=0;step<2;step++)a.box(-3,1+step,-28+step,3,1+step,-28+step,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
        a.box(-48,3,-9,48,10,-9,WHITE);
        for(int x:new int[]{-26,0,26})a.openZ(x,-9,2,2,4);
        for(int x:new int[]{-5,5}) {
            a.box(x,3,-8,x,12,27,WHITE);a.openX(x,1,2,2,4);
        }
        a.table(-29,2,-17,14);a.chest(-40,2,-21,"old_hell_household");a.chest(38,2,-21,"old_hell_household");
        a.table(15,2,-17,14);
        for(int x:new int[]{-32,15}) {
            a.box(x,3,-13,x+17,3,-13,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
            a.box(x,3,-23,x+17,3,-23,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
        }
        for(int x:new int[]{-45,42})a.box(x,3,-18,x+2,6,-13,DARK);
        for(int cx:new int[]{-26,26}) {
            a.box(cx-15,2,-3,cx+15,2,23,STONE);
            a.box(cx-12,-1,0,cx+12,1,20,STONE);
            a.box(cx-11,2,1,cx+11,6,19,AIR);
            a.box(cx-11,0,1,cx+11,1,19,Blocks.WATER.getDefaultState());
            // A shallow entry step and a dry rim connect both basins to the changing area.
            a.box(cx-3,1,1,cx+3,1,2,STONE);
            a.box(cx-2,2,0,cx+2,2,0,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
            for(int x=cx-10;x<=cx+10;x+=5)a.chest(x,2,-7,"old_hell_household");
            for(int x:new int[]{cx-8,cx,cx+8}) {
                a.block(x,3,25,Blocks.CAULDRON.getDefaultState().withProperty(net.minecraft.block.BlockCauldron.LEVEL,3));
                a.block(x+2,3,25,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
            }
            a.room("更衣间"+cx,cx,2,-11);a.room("浴池边"+cx,cx-14,2,10);
        }
        for(int x=-42;x<=42;x+=14)for(int z:new int[]{-17,9})a.block(x,14,z,LANTERN);
        a.block(4,7,-26,LOG);a.sign(4,7,-27,EnumFacing.NORTH,"旧地狱温泉","");
        a.room("番台",0,2,-18);a.room("中廊",0,2,14);
    }
    private static void guesthouse(GensokyoArchitecture a) {
        civicHall(a,-26,-24,26,24,10);a.openZ(0,-24,2,2,4);
        a.box(-2,1,-26,2,1,-26,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
        a.box(-2,2,-25,2,2,-25,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
        for(int x:new int[]{-5,5}) {
            a.box(x,3,-14,x,10,23,WHITE);
            for(int z:new int[]{-8,11})a.openX(x,z,2,1,4);
        }
        for(int side:new int[]{-1,1}) {
            a.box(side<0?-25:6,3,1,side<0?-6:25,10,1,WHITE);
            for(int z:new int[]{-8,11}) {
                a.bed(side*19,2,z);a.bed(side*13,2,z);a.chest(side*23,2,z-4,"old_hell_household");
                a.table(side<0?-21:10,2,z+4,5);a.block(side*15,11,z,LANTERN);a.room("客房"+side+"/"+z,side*9,2,z);
            }
        }
        a.chest(21,2,-20,"old_hell_pantry");a.table(-20,2,-19,7);a.block(0,11,-18,LANTERN);
        a.block(4,6,-24,LOG);a.sign(4,6,-25,EnumFacing.NORTH,"旅舍","");a.room("前厅",0,2,-18);
    }
    private static void banquet(GensokyoArchitecture a) {
        civicHall(a,-26,-24,26,24,11);a.openZ(0,-24,2,3,4);
        a.box(-3,1,-26,3,1,-26,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
        a.box(-3,2,-25,3,2,-25,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
        a.box(-25,3,13,25,9,13,WHITE);a.openZ(0,13,2,2,4);
        for(int x:new int[]{-17,8})for(int z:new int[]{-9,2}) {
            a.table(x,2,z,9);a.box(x,3,z+2,x+8,3,z+2,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
            a.block(x+4,5,z,ModBlocks.LACQUER_BOWL.getDefaultState());
        }
        for(int x=-19;x<=19;x+=6) {
            a.block(x,3,22,Blocks.FURNACE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.NORTH));
            a.chest(x,2,16,"old_hell_pantry");
        }
        a.block(-22,3,20,Blocks.CRAFTING_TABLE.getDefaultState());
        for(int x:new int[]{-16,0,16})for(int z:new int[]{-11,8})a.block(x,12,z,LANTERN);
        a.block(5,6,-24,LOG);a.sign(5,6,-25,EnumFacing.NORTH,"宴会所","");a.room("宴席",0,2,-3);a.room("后厨",0,2,20);
    }
}
