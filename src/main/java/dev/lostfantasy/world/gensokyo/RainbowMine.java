package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockRail;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Layered workings with a rail incline and a separate western circuit. */
final class RainbowMine {
    private static final IBlockState ROCK=Blocks.STONE.getDefaultState(),GRAVEL=Blocks.GRAVEL.getDefaultState(),
            ORE=ModBlocks.DRAGON_GEM_ORE.getDefaultState(),FENCE=Blocks.SPRUCE_FENCE.getDefaultState();
    private static final GensokyoNoise DETAIL=new GensokyoNoise(180418);
    private static final int[][] CHAMBERS={{0,-69,22,18,-8,13},{-43,-68,13,10,-8,8},
            {-52,-119,24,20,-22,18},{46,-148,27,21,-32,22}};
    private RainbowMine() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.RAINBOW_CAVE);
        // Complete all rock lining before any excavation. Intersections remain open.
        for(boolean carve:new boolean[]{false,true}) {
            for(int[] c:CHAMBERS)chamber(a,c,carve);
            for(int z=-154;z<=-19;z++)section(a,0,z,railFloor(z),true,carve);
            for(int x=-52;x<=-12;x++)section(a,x,-68,-8,false,carve);
            for(int z=-119;z<=-68;z++)section(a,-52,z,westFloor(z),true,carve);
            for(int x=-52;x<=-16;x++)section(a,x,-119,-22,false,carve);
            for(int z=-148;z<=-119;z++)section(a,-16,z,loopFloor(z),true,carve);
            for(int x=-16;x<=47;x++)section(a,x,-148,-32,false,carve);
        }
        yard(a);rails(a);bracing(a);sorting(a);toolBay(a);westWorking(a);deepWorking(a);
        for(int[] p:new int[][]{{0,-24,0},{-2,-44,railFloor(-44)},{-2,-96,railFloor(-96)},
                {-52,-92,westFloor(-92)},{-19,-134,loopFloor(-134)}})
            a.room("矿道"+p[1]+"/"+p[0],p[0],p[2],p[1]);
    }
    static int railFloor(int z) {
        if(z>=-36)return 0;
        if(z>=-60)return -Math.min(8,(-36-z)/3);
        if(z>=-82)return -8;
        return -8-Math.min(24,(-82-z)*24/66);
    }
    static int westFloor(int z) {return -8-Math.min(14,Math.max(0,(-78-z)*14/34));}
    static int loopFloor(int z) {return -22-Math.min(10,Math.max(0,(-119-z)*10/29));}
    private static void chamber(GensokyoArchitecture a,int[] c,boolean carve) {
        int cx=c[0],cz=c[1],rx=c[2],rz=c[3],floor=c[4],height=c[5];
        for(int dx=-rx-4;dx<=rx+4;dx++)for(int dz=-rz-4;dz<=rz+4;dz++) {
            double outer=dx*dx/((rx+4.0)*(rx+4))+dz*dz/((rz+4.0)*(rz+4));
            double inner=dx*dx/(double)(rx*rx)+dz*dz/(double)(rz*rz);
            if(!carve) {
                if(outer<=1)a.box(cx+dx,floor-4,cz+dz,cx+dx,floor+height+4,cz+dz,ROCK);
            }else if(inner<1) {
                int cap=floor+5+(int)Math.round((height-5)*Math.sqrt(1-inner));
                a.block(cx+dx,floor,cz+dz,DETAIL.value(cx+dx,cz+dz,8,4)>.24?GRAVEL:ROCK);
                a.box(cx+dx,floor+1,cz+dz,cx+dx,cap-1,cz+dz,AIR);
                // Narrow mineral seams follow the excavation rather than filling the room with blocks.
                if(inner>.76 && Math.abs(dx+2*dz)%17<3)a.block(cx+dx,cap,cz+dz,ORE);
            }
        }
    }
    private static void section(GensokyoArchitecture a,int x,int z,int floor,boolean alongZ,boolean carve) {
        int dx=alongZ?6:0,dz=alongZ?0:6;
        if(!carve) {a.box(x-dx,floor-3,z-dz,x+dx,floor+10,z+dz,ROCK);return;}
        for(int w=-5;w<=5;w++) {
            int px=x+(alongZ?w:0),pz=z+(alongZ?0:w);
            a.block(px,floor,pz,Math.abs(w)<2?GRAVEL:ROCK);
            a.box(px,floor+1,pz,px,floor+(Math.abs(w)<=2?7:Math.abs(w)<=4?5:3),pz,AIR);
        }
    }
    private static void yard(GensokyoArchitecture a) {
        a.box(-12,-3,-19,12,0,23,STONE);a.box(-12,1,-19,12,10,23,AIR);
        a.box(-9,0,-19,9,0,23,GRAVEL);
        for(int x:new int[]{-7,7})a.box(x,1,-20,x,8,-18,STONE);
        a.box(-7,8,-20,7,9,-18,STONE);a.box(-5,7,-19,5,7,-19,LOG);
        a.box(-5,1,-19,-5,6,-19,LOG);a.box(5,1,-19,5,6,-19,LOG);
        a.box(-5,8,-17,5,8,-16,WOOD);
        a.sign(0,8,-15,EnumFacing.SOUTH,"虹龙洞","");
        for(int x:new int[]{-4,4})a.block(x,6,-19,ModBlocks.RED_LANTERN.getDefaultState());
        // Covered hand-sorting dock, with a clear path between the yard and the mine.
        a.box(12,-1,-8,28,1,10,WOOD);
        for(int x:new int[]{12,28})for(int z:new int[]{-8,10})a.box(x,2,z,x,7,z,LOG);
        a.gableZ(10,-10,30,12,8);
        a.box(14,2,-7,25,2,-6,WOOD);a.block(17,3,-6,Blocks.CRAFTING_TABLE.getDefaultState());
        a.chest(25,1,7,"rainbow_mining");a.chest(15,1,7,"kappa_parts");
        a.box(12,8,3,28,8,3,LOG);
        a.box(16,2,-1,18,3,1,Blocks.IRON_ORE.getDefaultState());
        a.box(24,2,-2,26,2,0,ORE);
        a.block(20,7,3,ModBlocks.RED_LANTERN.getDefaultState());
        for(int z=1;z<=5;z++)a.block(11,1,z,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.EAST));
        // Timber, spare sleepers, and a track-end buffer stay outside the walking lane.
        a.box(-11,1,-6,-9,2,4,Blocks.LOG.getStateFromMeta(9));
        a.box(-9,1,11,-5,1,13,WOOD);a.chest(-7,0,17,"rainbow_mining");
        a.block(0,1,25,LOG);a.block(0,2,25,WOOD);
        a.room("装卸院",3,0,17);a.room("地上拣矿棚",22,1,4);
    }
    private static void rails(GensokyoArchitecture a) {
        // A lower cross-cut may excavate the last few slope floors. Restore walking grades after all cuts.
        for(int z=-119;z<=-68;z++)a.box(-55,westFloor(z)-3,z,-49,westFloor(z),z,ROCK);
        for(int z=-148;z<=-119;z++)a.box(-19,loopFloor(z)-3,z,-13,loopFloor(z),z,ROCK);
        for(int z=-150;z<=24;z++) {
            int f=railFloor(z);boolean uphill=railFloor(z+1)>f;
            a.box(-3,f-3,z,4,f,z,ROCK);
            EnumRailDirection shape=uphill?EnumRailDirection.ASCENDING_SOUTH:EnumRailDirection.NORTH_SOUTH;
            boolean powered=z<-35 && z>-149 || Math.floorMod(z,10)==0;
            a.block(0,f,z,powered?Blocks.REDSTONE_BLOCK.getDefaultState():GRAVEL);
            IBlockState track=powered?Blocks.GOLDEN_RAIL.getDefaultState().withProperty(BlockRailPowered.SHAPE,shape).withProperty(BlockRailPowered.POWERED,true)
                    :Blocks.RAIL.getDefaultState().withProperty(BlockRail.SHAPE,shape);
            a.block(0,f+1,z,track);
            if(uphill)for(int x=2;x<=4;x++)a.block(x,f+1,z,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
        }
        a.block(0,-31,-151,LOG);a.block(0,-30,-151,WOOD);
        for(int z=-118;z>=-147;z--)if(loopFloor(z+1)>loopFloor(z))
            for(int x=-17;x<=-15;x++)a.block(x,loopFloor(z)+1,z,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
        for(int z=-78;z>=-111;z--)if(westFloor(z+1)>westFloor(z))
            for(int x=-53;x<=-51;x++)a.block(x,westFloor(z)+1,z,Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
    }
    private static void frame(GensokyoArchitecture a,int x,int floor,int z) {
        for(int dx:new int[]{-4,4})a.box(x+dx,floor+1,z,x+dx,floor+5,z,LOG);
        a.box(x-4,floor+6,z,x+4,floor+6,z,LOG);
        a.block(x-2,floor+5,z,ModBlocks.RED_LANTERN.getDefaultState());
    }
    private static void bracing(GensokyoArchitecture a) {
        for(int z:new int[]{-29,-43,-56,-84,-96,-108,-123,-137})frame(a,0,railFloor(z),z);
        for(int z:new int[]{-81,-95,-109})frame(a,-52,westFloor(z),z);
        for(int z:new int[]{-128,-142})frame(a,-16,loopFloor(z),z);
        for(int x:new int[]{-32,19,35}) {
            int z=x==-32?-68:-148,f=x==-32?-8:-32;
            for(int dz:new int[]{-4,4})a.box(x,f+1,z+dz,x,f+5,z+dz,LOG);
            a.box(x,f+6,z-4,x,f+6,z+4,LOG);a.block(x,f+5,z+2,ModBlocks.RED_LANTERN.getDefaultState());
        }
    }
    private static void sorting(GensokyoArchitecture a) {
        a.box(8,-7,-75,17,-7,-73,WOOD);a.block(10,-6,-74,Blocks.CRAFTING_TABLE.getDefaultState());
        a.box(8,-7,-63,15,-7,-61,WOOD);a.block(10,-6,-62,ModBlocks.DRAGON_GEM_BLOCK.getDefaultState());
        a.chest(18,-8,-67,"rainbow_mining");a.chest(8,-8,-57,"kappa_parts");a.chest(-11,-8,-60,"rainbow_mining");
        a.box(-15,-7,-80,-10,-6,-77,Blocks.IRON_ORE.getDefaultState());
        a.box(8,-7,-82,16,-7,-79,STONE);a.box(9,-6,-81,15,-6,-80,ORE);
        a.box(8,-6,-82,16,-5,-82,WOOD);a.box(8,-6,-81,8,-5,-79,WOOD);a.box(16,-6,-81,16,-5,-79,WOOD);
        a.box(-16,-7,-59,-14,-5,-57,WOOD);a.box(-15,-6,-56,-10,-6,-56,Blocks.WOODEN_SLAB.getDefaultState());
        for(int[] p:new int[][]{{-12,-72},{14,-68},{-8,-58}})lamp(a,p[0],-8,p[1]);
        a.room("洞内分拣",11,-8,-68);a.room("西线岔口",-12,-8,-68);
    }
    private static void toolBay(GensokyoArchitecture a) {
        a.box(-48,-7,-75,-37,-7,-75,WOOD);a.block(-46,-6,-75,Blocks.CRAFTING_TABLE.getDefaultState());
        a.block(-40,-6,-75,Blocks.ANVIL.getDefaultState());
        a.chest(-35,-8,-70,"rainbow_mining");a.chest(-43,-8,-62,"village_pantry");
        a.box(-45,-7,-60,-38,-7,-60,Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
        lamp(a,-43,-8,-74);a.room("工具修补",-43,-8,-69);
    }
    private static void westWorking(GensokyoArchitecture a) {
        // Retained pillars and a shallow worked-out seam keep this a mine, not an empty hall.
        for(int[] p:new int[][]{{-65,-126},{-40,-130},{-61,-108}}) {
            a.box(p[0]-1,-21,p[1]-1,p[0]+1,-3,p[1]+1,ROCK);
            a.box(p[0]-1,-19,p[1],p[0]+1,-17,p[1],ORE);
        }
        for(int x=-64;x<=-58;x++)for(int z=-133;z<=-128;z++) {
            a.block(x,-27,z,ROCK);a.box(x,-26,z,x,-22,z,AIR);
            a.block(x,-26,z,ORE);
        }
        a.box(-66,-21,-127,-56,-21,-127,FENCE);a.box(-57,-21,-134,-57,-21,-127,FENCE);
        a.box(-67,-21,-131,-67,-21,-127,FENCE);
        a.box(-49,-21,-132,-45,-20,-131,WOOD);a.chest(-46,-22,-128,"rainbow_mining");
        for(int[] p:new int[][]{{-65,-118},{-38,-118},{-51,-134}})lamp(a,p[0],-22,p[1]);
        a.room("西采场",-53,-22,-119);a.room("采空凹地旁",-53,-22,-129);
    }
    private static void deepWorking(GensokyoArchitecture a) {
        for(int[] p:new int[][]{{31,-158,9},{43,-132,10},{60,-151,12}}) {
            int x=p[0],z=p[1],height=p[2];
            a.box(x-1,-31,z-1,x+1,-32+height,z+1,ROCK);
            for(int y=-30;y<=-32+height;y++)a.block(x+(y%2==0?1:0),y,z+1,ORE);
        }
        // Radial faces of the deposit sit against the wall; the centre remains traversable.
        for(int x=34;x<=57;x++)for(int z=-167;z<=-159;z++) {
            double edge=(x-46)*(x-46)/169.0+(z+166)*(z+166)/100.0;
            if(edge>1.1)continue;
            int height=3+(int)Math.round(5*(1-Math.min(1,edge))+DETAIL.value(x,z,6,7)*2);
            a.box(x,-31,z,x,-32+height,z,Blocks.STONE.getStateFromMeta(3));
            for(int y=-30;y<=-32+height;y++)if(Math.abs(y+33+(x-35)/4)%6<2)a.block(x,y,z,ORE);
        }
        a.box(56,-31,-138,61,-31,-137,WOOD);a.block(58,-30,-138,Blocks.CRAFTING_TABLE.getDefaultState());
        a.chest(62,-32,-141,"rainbow_mining");a.chest(36,-32,-140,"rainbow_mining");
        for(int[] p:new int[][]{{46,-155},{61,-145},{33,-138}})lamp(a,p[0],-32,p[1]);
        a.room("深层矿面",46,-32,-148);a.room("深层工具台",57,-32,-141);
        a.room("底部停靠",3,-32,-150);a.room("回环通道",-16,-32,-148);
    }
    private static void lamp(GensokyoArchitecture a,int x,int floor,int z) {
        a.block(x,floor+1,z,STONE);a.block(x,floor+2,z,ModBlocks.LIBRARY_LAMP.getDefaultState());
    }
}
