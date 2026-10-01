package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** The forgotten signal tower adopted by the fairies in Oriental Sacred Place, chapter 10. */
final class FairySignalTower {
    private static final IBlockState STEEL=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(9),
            RUST=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(12),BARS=Blocks.IRON_BARS.getDefaultState(),
            BARK=Blocks.LOG.getDefaultState().withProperty(BlockLog.LOG_AXIS,BlockLog.EnumAxis.NONE),
            LEAVES=Blocks.LEAVES.getStateFromMeta(4);
    private FairySignalTower() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.FAIRY_SHRINE);
        surroundings(a);frame(a);livingBranches(a);
        for(int y:new int[]{14,28,42})platform(a,y);
        // The old central cable channel backs an uninterrupted ladder. Openings stay clear of foliage.
        a.box(-1,1,0,1,45,1,AIR);a.box(0,0,-1,0,45,-1,STEEL);
        a.box(0,1,0,0,43,0,Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING,EnumFacing.SOUTH));
        for(int y:new int[]{14,28,42}) {
            a.box(-1,y,1,1,y,1,STEEL);a.block(-1,y,0,STEEL);a.block(1,y,0,STEEL);
            a.room("检修平台"+y,2,y,0);
        }
        a.chest(3,14,1,"signal_salvage");a.chest(-3,28,1,"signal_salvage");
        littleShrine(a);oldCabinets(a);vines(a);
        a.room("塔下根系",0,0,4);a.room("小鸟居",0,0,25);
        a.room("花与供台",-7,0,18);a.room("旧机柜",15,0,-1);
    }
    private static int radius(int y) {return Math.max(2,9-y/7);}
    private static void frame(GensokyoArchitecture a) {
        for(int x:new int[]{-9,9})for(int z:new int[]{-9,9})a.box(x-1,-3,z-1,x+1,1,z+1,STONE);
        for(int y=1;y<=49;y++) {
            int r=radius(y),previous=radius(y-1);
            for(int x:new int[]{-1,1})for(int z:new int[]{-1,1})
                a.box(x*r,y,z*r,x*previous,y,z*previous,(y+x*3+z*5)%11<2?RUST:STEEL);
            if(y%7==0)for(int side:new int[]{-1,1}) {
                a.box(-r,y,side*r,r,y,side*r,STEEL);a.box(side*r,y,-r,side*r,y,r,STEEL);
            }
            if(y<49) {
                double t=(y%14)/14.0;
                for(int side:new int[]{-1,1})for(int cross:new int[]{-1,1}) {
                    int x=(int)Math.round(cross*r*(2*t-1));
                    a.box(x-1,y,side*r,x+1,y,side*r,BARS);
                    a.box(side*r,y,x-1,side*r,y,x+1,BARS);
                }
            }
        }
        // A modest aerial belongs to the old tower; the manga's imagined loudspeaker is not hardware.
        a.box(0,46,0,0,61,0,BARS);a.box(-6,50,0,6,50,0,STEEL);
        a.box(-4,55,0,4,55,0,BARS);a.box(0,53,-4,0,53,4,BARS);
        for(int x:new int[]{-6,6})a.box(x,49,-1,x,52,1,Blocks.IRON_TRAPDOOR.getDefaultState());
    }
    private static void platform(GensokyoArchitecture a,int y) {
        int r=radius(y)+1;
        a.box(-r,y,-r,r,y,r,STEEL);
        a.box(-r+2,y,-r+2,r-2,y,r-2,AIR);
        a.box(-r+1,y+1,-r+1,r-1,y+3,r-1,AIR);
        a.box(-r,y,0,r,y,1,STEEL);
        for(int side:new int[]{-1,1}) {
            a.box(-r,y+1,side*r,r,y+1,side*r,BARS);
            a.box(side*r,y+1,-r,side*r,y+1,r,BARS);
        }
        // Rail the inner well too, leaving the middle crosswalk accessible.
        for(int side:new int[]{-1,1})for(int z=-r+2;z<=r-2;z++)if(z<0 || z>1)a.block(side*(r-2),y+1,z,BARS);
        for(int side:new int[]{-1,1})a.box(-r+2,y+1,side*(r-2),r-2,y+1,side*(r-2),BARS);
    }
    private static void livingBranches(GensokyoArchitecture a) {
        // Two thick growths wind around the ironwork rather than replacing it with a solid tree trunk.
        for(int hand:new int[]{-1,1}) {
            int px=hand*12,pz=-10;
            for(int y=0;y<=47;y++) {
                double angle=y*.105+(hand<0?Math.PI:0),r=radius(y)+1.6;
                int x=(int)Math.round(Math.cos(angle)*r),z=(int)Math.round(Math.sin(angle)*r);
                a.box(Math.min(px,x),Math.max(0,y-1),Math.min(pz,z),Math.max(px,x)+1,y,Math.max(pz,z)+1,BARK);
                px=x;pz=z;
                if(y>5 && y%12==hand+6) {
                    int bx=x+(x<0?-5:5),bz=z+(z<0?-3:3);
                    for(int d=0;d<=5;d++)a.box(x+(x<0?-d:d),y+d/3,z,x+(x<0?-d:d)+1,y+d/3+1,z+1,BARK);
                    leaves(a,bx,y+4,bz,5);
                }
            }
            leaves(a,px,48,pz,7);
        }
    }
    private static void vines(GensokyoArchitecture a) {
        // Short hanging vines attach to bark or ironwork with a full supporting face.
        for(int y=4;y<48;y+=2)for(int x=-13;x<=13;x++)for(int z=-13;z<=13;z++) {
            if(Math.floorMod(x*11+z*7+y,9)!=0 || a.plan.at(a.site.x+x,a.site.y+y,a.site.z+z).getBlock()!=Blocks.AIR)continue;
            for(EnumFacing side:EnumFacing.HORIZONTALS)if(a.plan.at(a.site.x+x+side.getXOffset(),a.site.y+y,a.site.z+z+side.getZOffset()).isFullCube()) {
                a.block(x,y,z,Blocks.VINE.getDefaultState().withProperty(BlockVine.getPropertyFor(side),true));break;
            }
        }
    }
    private static void leaves(GensokyoArchitecture a,int x,int y,int z,int r) {
        for(int dy=-3;dy<=3;dy++)for(int dz=-r;dz<=r;dz++)for(int dx=-r;dx<=r;dx++) {
            if(dx*dx+dz*dz+dy*dy*3>r*r || Math.floorMod(dx*7+dz*11+dy,13)==0)continue;
            if(a.plan.at(a.site.x+x+dx,a.site.y+y+dy,a.site.z+z+dz).getBlock()==Blocks.AIR)a.block(x+dx,y+dy,z+dz,LEAVES);
        }
    }
    private static void surroundings(GensokyoArchitecture a) {
        a.box(-13,1,-13,13,63,13,AIR);
        ForestDetails.tree(a,-22,-18,27,8);ForestDetails.tree(a,24,17,25,7);ForestDetails.tree(a,-23,23,22,7);
        ForestDetails.tree(a,-26,-2,26,6);ForestDetails.tree(a,-7,-27,30,7);
        ForestDetails.tree(a,20,-23,29,7);ForestDetails.tree(a,28,-7,27,6);
        a.box(-2,0,2,2,0,39,Blocks.GRAVEL.getDefaultState());
        for(int x=-17;x<=18;x++)for(int z=-17;z<=18;z++)if(Math.floorMod(x*13+z*7,19)<3 && x*x+z*z<300)
            a.block(x,0,z,Blocks.DIRT.getStateFromMeta(2));
        for(int[] p:new int[][]{{-17,-7},{-15,9},{16,16},{10,-16}}) {
            a.block(p[0],0,p[1],Blocks.MYCELIUM.getDefaultState());a.block(p[0],1,p[1],Blocks.BROWN_MUSHROOM.getDefaultState());
        }
    }
    private static void littleShrine(GensokyoArchitecture a) {
        // Small handmade gate, rope and a straw figure follow the fairies' preparations in the comic.
        for(int x:new int[]{-4,4})a.box(x,0,23,x,4,23,LOG);
        a.box(-5,4,23,5,4,23,WOOD);a.box(-6,5,23,6,5,23,DARK);
        a.box(-3,3,23,3,3,23,ModBlocks.SHRINE_ROPE.getDefaultState());
        a.box(-8,1,17,-5,1,17,WOOD);a.block(-7,2,17,Blocks.FLOWER_POT.getDefaultState());
        a.block(-5,2,17,ModBlocks.MEDICINE_TRAY.getDefaultState());
        a.box(-11,1,20,-11,4,20,Blocks.SPRUCE_FENCE.getDefaultState());a.box(-13,3,20,-9,3,20,WOOD);
        a.block(-11,4,20,Blocks.HAY_BLOCK.getDefaultState());
        for(int x:new int[]{-3,3})a.block(x,4,24,ModBlocks.SHIDE.getDefaultState());
        a.box(-9,0,14,-4,0,19,Blocks.GRASS.getDefaultState());
        for(int x=-9;x<=-4;x++)for(int z:new int[]{14,19})a.block(x,1,z,Blocks.RED_FLOWER.getStateFromMeta(Math.floorMod(x,8)));
    }
    private static void oldCabinets(GensokyoArchitecture a) {
        a.box(12,0,-7,18,0,-2,STONE);a.box(12,1,-7,18,4,-6,STEEL);
        a.box(12,1,-5,12,4,-3,RUST);a.box(18,1,-5,18,4,-3,RUST);
        a.box(12,4,-5,18,4,-3,STEEL);
        a.chest(13,0,-4,"signal_salvage");a.chest(17,0,-4,"signal_salvage");
        a.block(15,1,-6,Blocks.REDSTONE_LAMP.getDefaultState());
        a.room("机柜内侧",15,0,-4);
    }
}
