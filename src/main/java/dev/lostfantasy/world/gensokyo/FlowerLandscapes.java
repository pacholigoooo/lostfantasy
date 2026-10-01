package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.ChunkPrimer;
import static dev.lostfantasy.world.gensokyo.GensokyoAtlas.*;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** One southern sunflower bowl and one continuous, cooler hill of white bells. */
final class FlowerLandscapes {
    static final int STAGE_DX=110,STAGE_DZ=80;
    private static final double HILL_DX=SUZURAN.x-NAMELESS_HILL.x,HILL_DZ=SUZURAN.z-NAMELESS_HILL.z;
    private static final double HILL_LENGTH2=HILL_DX*HILL_DX+HILL_DZ*HILL_DZ;
    private FlowerLandscapes() {}

    static boolean flowers(int x,int z) {return sunRadius(x,z)<1.12 || hillDistance(x,z)<290;}
    static boolean treeless(int x,int z) {return sunRadius(x,z)<1.14 || hillDistance(x,z)<305;}
    static double shape(int x,int z,double height,GensokyoNoise noise) {
        double r=sunRadius(x,z);
        if(r<1.32) {
            double south=(z-SUN_GARDEN.z)/315.0;
            double bowl=86+26*Math.min(1.13,r)*Math.min(1.13,r)-3*south+noise.value(x,z,82,241)*1.7;
            height=GensokyoNoise.lerp(height,bowl,1-GensokyoNoise.smooth((r-1.03)/.29));
        }
        double distance=hillDistance(x,z);
        if(distance<410) {
            double t=hillAlong(x,z);
            // Match the descending route profile rather than carving a road through a second crest.
            double shoulder=115-21*Math.max(0,Math.min(1,t));
            double hill=shoulder-14*Math.pow(Math.min(1,distance/410),2)+noise.value(x,z,90,242)*1.9;
            height=GensokyoNoise.lerp(height,hill,1-GensokyoNoise.smooth((distance-255)/155));
        }
        double pad=stageDistance(x,z);
        if(pad<25)height=GensokyoNoise.lerp(height,SUN_GARDEN.y,1-GensokyoNoise.smooth(pad/25));
        return height;
    }

    /** Called once for a dry, unreserved column, before structure painting. */
    static boolean paintColumn(ChunkPrimer p,int x,int z,int wx,int wz,GensokyoTerrain.Column c,GensokyoTerrain terrain) {
        double r=sunRadius(wx,wz),hill=hillDistance(wx,wz);
        if(r>=1.12 && hill>=290)return false;
        if(c.wet() || c.path() || c.rockSurface || GensokyoAtlas.reserved(wx,wz,3))return true;
        if(stageDistance(wx,wz)<=0 || clearing(wx,wz) || path(wx,wz)) {
            p.setBlockState(x,c.ground,z,stageDistance(wx,wz)<=0 || path(wx,wz)?Blocks.GRAVEL.getDefaultState():Blocks.GRASS.getDefaultState());
            return true;
        }
        if(c.ground>=250)return true;
        long hash=terrain.hash(wx,wz,243);double patches=terrain.patchNoise(wx,wz,39,244);
        int chance=(int)Math.floorMod(hash,100);
        if(r<1.12) {
            double edge=1-GensokyoNoise.smooth((r-.88)/.24);
            int density=(int)((48+patches*35)*edge);
            if(chance<density) {
                p.setBlockState(x,c.ground+1,z,Blocks.DOUBLE_PLANT.getStateFromMeta(0));
                p.setBlockState(x,c.ground+2,z,Blocks.DOUBLE_PLANT.getStateFromMeta(8));
            } else if(chance<density+10)p.setBlockState(x,c.ground+1,z,Blocks.TALLGRASS.getStateFromMeta(1));
        } else {
            double edge=1-GensokyoNoise.smooth((hill-215)/75);
            // Pale clusters follow the same hill from its northern shoulder to the southern field.
            int density=(int)((58+patches*33)*edge);
            if(chance<density)p.setBlockState(x,c.ground+1,z,ModBlocks.SUZURAN.getDefaultState());
            else if(chance<density+15)p.setBlockState(x,c.ground+1,z,Blocks.TALLGRASS.getStateFromMeta(1));
        }
        return true;
    }

    static double sunRadius(int x,int z) {
        double dx=((double)x-SUN_GARDEN.x)/410,dz=((double)z-SUN_GARDEN.z)/315;
        return Math.hypot(dx,dz);
    }
    private static double hillAlong(int x,int z) {return ((x-(double)NAMELESS_HILL.x)*HILL_DX+(z-(double)NAMELESS_HILL.z)*HILL_DZ)/HILL_LENGTH2;}
    private static double hillDistance(int x,int z) {
        double t=Math.max(0,Math.min(1,hillAlong(x,z)));
        return Math.hypot(x-NAMELESS_HILL.x-t*HILL_DX,z-NAMELESS_HILL.z-t*HILL_DZ);
    }
    static double stageDistance(int x,int z) {
        double dx=x-(double)SUN_GARDEN.x-STAGE_DX,dz=z-(double)SUN_GARDEN.z-STAGE_DZ;
        return Math.max(Math.max(-23-dx,dx-23),Math.max(-22-dz,dz-48));
    }
    static boolean clearing(int x,int z) {
        double dx=x-(double)SUN_GARDEN.x,dz=z-(double)SUN_GARDEN.z;
        // Low openings reveal the flowers above eye level; no extra houses occupy the meadow.
        return Math.hypot(dx+155,dz-78)<11 || Math.hypot(dx-222,dz+35)<10
                || Math.hypot(x-(double)SUZURAN.x+42,z-(double)SUZURAN.z-21)<8;
    }
    static boolean path(int x,int z) {
        double dx=x-(double)SUN_GARDEN.x,dz=z-(double)SUN_GARDEN.z;
        if(Math.abs(dx)>430 || Math.abs(dz)>340)return false;
        double angle=Math.atan2(dz/206,dx/279),ring=Math.hypot(dx/279,dz/206);
        boolean ringPath=Math.abs(ring-1)<.008 && angle>-.25 && angle<3.5;
        boolean stageAccess=segmentDistance(dx,dz,0,0,40,135)<1.8
                || segmentDistance(dx,dz,40,135,110,135)<1.8
                || segmentDistance(dx,dz,110,135,110,125)<1.8;
        boolean westAccess=segmentDistance(dx,dz,0,0,-155,78)<1.6;
        boolean rimAccess=segmentDistance(dx,dz,-155,78,-255,105)<1.6;
        boolean eastAccess=segmentDistance(dx,dz,110,125,160,125)<1.6
                || segmentDistance(dx,dz,160,125,222,-35)<1.6
                || segmentDistance(dx,dz,222,-35,277,-25)<1.6;
        return ringPath || stageAccess || westAccess || rimAccess || eastAccess;
    }
    private static double segmentDistance(double x,double z,double ax,double az,double bx,double bz) {
        double dx=bx-ax,dz=bz-az,t=Math.max(0,Math.min(1,((x-ax)*dx+(z-az)*dz)/(dx*dx+dz*dz)));
        return Math.hypot(x-ax-t*dx,z-az-t*dz);
    }

    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,SUN_GARDEN,STAGE_DX,STAGE_DZ,"太阳花田演奏舞台");
        a.box(-23,1,-22,23,22,48,AIR);
        a.box(-23,0,-22,23,0,48,Blocks.GRAVEL.getDefaultState());
        // Small open-air timber stage, with the audience in the flower meadow.
        a.box(-15,-3,-15,15,1,11,Blocks.COBBLESTONE.getDefaultState());
        a.box(-15,2,-15,15,2,11,WOOD);
        for(int x:new int[]{-13,13})for(int z:new int[]{-13,9})a.box(x,3,z,x,11,z,LOG);
        a.box(-14,11,-14,14,11,-14,DARK);a.box(-14,11,10,14,11,10,DARK);
        a.box(-14,11,-14,-14,11,10,DARK);a.box(14,11,-14,14,11,10,DARK);
        a.gable(-17,-17,17,13,12);
        a.box(-13,3,-13,13,10,-13,WOOD);
        pineBackdrop(a);
        for(int side:new int[]{-1,1}) {
            int x=side*14;
            for(int z=-12;z<=4;z++)a.box(x,5,z,x,9,z,Blocks.WOOL.getStateFromMeta((z+12)%6<2?14:(z+12)%6<4?15:13));
            a.box(side*15,3,-11,side*15,4,7,Blocks.SPRUCE_FENCE.getDefaultState());
            // Backstage storage is accessible through the open rear corners.
            a.chest(side*11,2,-14,"concert_supplies");
        }
        a.stairsSouth(0,12,0,2,3);
        a.stairsSouth(-12,12,0,2,1);a.stairsSouth(12,12,0,2,1);
        // A playable note block console, percussion and two free microphone positions.
        a.box(-8,3,-5,-4,3,-4,DARK);
        for(int x=-8;x<=-4;x+=2)a.block(x,4,-4,Blocks.NOTEBLOCK.getDefaultState());
        a.box(-7,3,-2,-5,3,-2,Blocks.WOODEN_SLAB.getStateFromMeta(1));
        for(int[] drum:new int[][]{{3,-6},{6,-6},{4,-3}}) {
            a.block(drum[0],3,drum[1],Blocks.OAK_FENCE.getDefaultState());
            a.block(drum[0],4,drum[1],Blocks.NOTEBLOCK.getDefaultState());
        }
        for(int x:new int[]{-5,5}) {
            a.block(x,3,5,Blocks.STONE_SLAB.getDefaultState());
            a.box(x,4,5,x,5,5,Blocks.IRON_BARS.getDefaultState());
        }
        for(int x:new int[]{-10,-5,0,5,10}) {
            a.block(x,10,9,Blocks.GLOWSTONE.getDefaultState());
            a.block(x,9,9,Blocks.STAINED_GLASS.getStateFromMeta(x<0?3:x>0?10:4));
        }
        for(int z=23;z<=38;z+=5)for(int side:new int[]{-1,1}) {
            int x1=side<0?-17:5,x2=side<0?-5:17;
            a.box(x1,1,z,x2,1,z,Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.SOUTH));
            a.box(x1,1,z+1,x2,1,z+1,Blocks.SPRUCE_FENCE.getDefaultState());
        }
        a.box(-22,0,16,-20,3,16,LOG);a.box(20,0,16,22,3,16,LOG);
        a.block(-21,4,16,Blocks.GLOWSTONE.getDefaultState());a.block(21,4,16,Blocks.GLOWSTONE.getDefaultState());
        a.block(-21,5,16,SLAB);a.block(21,5,16,SLAB);
        a.room("前场通路",0,0,43);a.room("观众中道",0,0,29);a.room("舞台中央",0,2,2);
        a.room("键盘席",-6,2,0);a.room("打击乐席",6,2,-1);
        a.room("西侧器材箱",-11,2,-15);a.room("东侧器材箱",11,2,-15);
    }

    private static void pineBackdrop(GensokyoArchitecture a) {
        // Block-scale pine silhouette on a warm wooden backdrop, not a planted tree indoors.
        IBlockState trunk=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(12),needles=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(13);
        a.box(-2,3,-12,-1,6,-12,trunk);a.box(-1,6,-12,2,6,-12,trunk);a.box(1,7,-12,2,8,-12,trunk);
        a.box(-7,6,-12,-2,7,-12,needles);a.box(-9,6,-12,-6,6,-12,needles);
        a.box(2,5,-12,9,6,-12,needles);a.box(5,7,-12,10,7,-12,needles);
        a.box(-3,9,-12,6,9,-12,needles);a.box(-1,10,-12,4,10,-12,needles);
    }
}
