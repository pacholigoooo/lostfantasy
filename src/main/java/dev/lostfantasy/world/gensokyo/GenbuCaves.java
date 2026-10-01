package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Small dry river workshops connected to natural, moss-covered grottoes. */
final class GenbuCaves {
    private static final IBlockState BASALT=ModBlocks.COLUMNAR_BASALT.getDefaultState(),
            MOSS=ModBlocks.MOSSY_BASALT.getDefaultState(),RAIL=Blocks.SPRUCE_FENCE.getDefaultState();
    private GenbuCaves() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,GensokyoAtlas.GENBU);
        int[][] rooms={{-15,-25,18,13,0,12},{-47,-37,12,9,0,10},
                {-47,-3,13,9,0,9},{-83,-13,22,22,-3,12}};
        // Finish every rock envelope before carving, so adjoining walls cannot refill a room.
        for(boolean carve:new boolean[]{false,true})for(int[] r:rooms)chamber(a,r[0],r[1],r[2],r[3],r[4],r[5],carve);
        bankPath(a);
        passage(a,-48,-27,-31,0);
        a.box(-48,-1,-29,-44,0,-4,BASALT);a.box(-48,1,-29,-44,5,-4,AIR);
        passage(a,-78,-56,-9,0);
        // Three gradual steps enter the lower grotto, without opening its floor to the river.
        for(int x=-69;x>=-78;x--) {
            int floor=-Math.min(3,(-69-x)/3);
            a.box(x,-5,-11,x,floor,-7,BASALT);a.box(x,floor+1,-11,x,6,-7,AIR);
        }
        passage(a,-2,41,-25,0);
        a.box(34,-2,-31,61,0,-19,BASALT);a.box(34,1,-31,61,5,-19,AIR);
        a.box(40,1,-31,49,1,-31,RAIL);a.box(40,1,-19,57,1,-19,RAIL);
        a.box(59,1,-30,59,1,-20,RAIL);
        // The open end meets the upstream bank walk one step above the cave floor.
        for(int x=8;x<=28;x+=16) {
            for(int z:new int[]{-29,-21})a.box(x,1,z,x,6,z,LOG);
            a.box(x,7,-29,x,7,-21,LOG);
            a.block(x,6,-25,Blocks.SEA_LANTERN.getDefaultState());
        }
        workshop(a);stores(a);grotto(a);conduits(a);
        a.room("谷底洞口",43,0,-25);a.room("崖内通道",16,0,-25);
    }
    private static void chamber(GensokyoArchitecture a,int cx,int cz,int rx,int rz,int floor,int height,boolean carve) {
        for(int dx=-rx-11;dx<=rx+11;dx++)for(int dz=-rz-11;dz<=rz+11;dz++) {
            if(!carve) {
                int[] column=GenbuRavine.cellCentre(a.site.x+cx+dx,a.site.z+cz+dz);
                int px=column[0]-a.site.x-cx,pz=column[1]-a.site.z-cz;
                double outer=px*px/((rx+6.0)*(rx+6))+pz*pz/((rz+6.0)*(rz+6));
                if(outer>1)continue;
                int cap=floor+height+6+Math.floorMod(column[0]*71+column[1]*37,7);
                a.box(cx+dx,floor-3,cz+dz,cx+dx,cap,cz+dz,BASALT);
                continue;
            }
            double inner=dx*dx/(double)(rx*rx)+dz*dz/(double)(rz*rz);
            if(inner>=1)continue;
            int ceiling=floor+4+(int)Math.round((height-4)*Math.sqrt(1-inner));
            a.block(cx+dx,floor,cz+dz,BASALT);
            a.box(cx+dx,floor+1,cz+dz,cx+dx,ceiling-1,cz+dz,AIR);
            if(inner>.67 && Math.floorMod(dx*17+dz*7,9)<4) {
                a.block(cx+dx,floor,cz+dz,MOSS);
                if(inner>.86)a.block(cx+dx,ceiling,cz+dz,MOSS);
            }
        }
    }
    private static void passage(GensokyoArchitecture a,int x1,int x2,int z,int floor) {
        a.box(x1,floor-2,z-4,x2,floor+8,z+4,BASALT);
        a.box(x1,floor+1,z-2,x2,floor+5,z+2,AIR);
        a.box(x1,floor+1,z-3,x2,floor+3,z+3,AIR);
    }
    private static void workshop(GensokyoArchitecture a) {
        a.box(-25,1,-35,-10,1,-33,WOOD);a.block(-22,2,-34,Blocks.CRAFTING_TABLE.getDefaultState());
        a.block(-14,2,-34,Blocks.ANVIL.getDefaultState());
        a.box(-24,1,-18,-11,1,-17,WOOD);a.block(-19,2,-18,Blocks.CRAFTING_TABLE.getDefaultState());
        a.chest(-29,0,-25,"kappa_tools");a.chest(-27,0,-31,"kappa_parts");a.chest(-7,0,-18,"kappa_parts");
        for(int z:new int[]{-30,-20})a.block(-2,1,z,Blocks.FURNACE.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.WEST));
        // A retained column and its banded pipe break up the large chamber's silhouette.
        a.box(-12,1,-27,-11,10,-26,BASALT);
        for(int[] lamp:new int[][]{{-26,-20},{-5,-27},{-17,-36}})inspectionLamp(a,lamp[0],0,lamp[1]);
        a.room("河具修理",-21,0,-25);a.room("装配工作台",-16,0,-21);
    }
    private static void stores(GensokyoArchitecture a) {
        a.box(-52,1,-44,-41,3,-44,Blocks.BOOKSHELF.getDefaultState());
        VillageJoinery.lowDesk(a,-52,0,-38,9);
        a.chest(-57,0,-36,"school_supplies");a.chest(-38,0,-38,"kappa_parts");
        inspectionLamp(a,-47,0,-43);
        a.box(-56,1,1,-39,1,1,WOOD);
        a.block(-53,2,1,Blocks.CAULDRON.getStateFromMeta(3));
        a.block(-47,2,1,Blocks.CRAFTING_TABLE.getDefaultState());
        a.block(-40,2,1,ModBlocks.OUTSIDE_TELEVISION.getDefaultState());
        a.chest(-56,0,-7,"kappa_tools");a.chest(-37,0,-7,"village_pantry");
        inspectionLamp(a,-47,0,4);
        a.room("水文图册",-48,0,-33);a.room("清洗与器材收存",-45,0,-4);
    }
    private static void grotto(GensokyoArchitecture a) {
        for(int[] pool:new int[][]{{-91,-23},{-89,1}}) {
            for(int dx=-5;dx<=5;dx++)for(int dz=-3;dz<=3;dz++)if(dx*dx/25.0+dz*dz/9.0<1) {
                a.block(pool[0]+dx,-5,pool[1]+dz,BASALT);
                a.block(pool[0]+dx,-4,pool[1]+dz,Blocks.WATER.getDefaultState());
                a.block(pool[0]+dx,-3,pool[1]+dz,AIR);
            }
        }
        for(int[] rock:new int[][]{{-96,-16,4},{-82,-27,3},{-70,-1,4},{-90,4,2}}) {
            a.box(rock[0]-1,-3,rock[1]-1,rock[0]+1,rock[2]-3,rock[1]+1,MOSS);
        }
        a.box(-91,-2,-7,-87,-2,-7,Blocks.STONE_SLAB.getDefaultState());
        a.box(-96,-2,-10,-94,5,-8,BASALT);
        // Ordinary inspection lighting illuminates a reflective-coloured surface; the moss does not emit light.
        inspectionLamp(a,-73,-3,-20);inspectionLamp(a,-94,-3,-6);
        a.room("光藓岩窟",-83,-3,-13);a.room("浅潭石台",-83,-3,-4);
    }
    private static void conduits(GensokyoArchitecture a) {
        IBlockState pipe=ModBlocks.KAPPA_PIPE.getDefaultState().withProperty(BlockRotatedPillar.AXIS,EnumFacing.Axis.X);
        a.box(-24,5,-28,42,5,-28,pipe);
        a.box(42,5,-28,53,5,-28,pipe);a.box(53,-3,-28,53,5,-28,ModBlocks.KAPPA_PIPE.getDefaultState());
        a.block(53,5,-28,Blocks.IRON_BLOCK.getDefaultState());
        for(int x=-15;x<=33;x+=16)a.box(x,4,-29,x,6,-29,Blocks.IRON_BARS.getDefaultState());
    }
    private static void inspectionLamp(GensokyoArchitecture a,int x,int floor,int z) {
        a.block(x,floor+1,z,STONE);a.block(x,floor+2,z,ModBlocks.LIBRARY_LAMP.getDefaultState());
    }
    private static void bankPath(GensokyoArchitecture a) {
        // Starts on the completed workshop's south deck, following the same river stations.
        for(int z=-2118;z<=-1680;z++) {
            int x=(int)Math.round(KappaWatercourse.centreX(z))-32;
            int y=(int)Math.floor(KappaWatercourse.level(z))+3-a.site.y,localZ=z-a.site.z;
            a.box(x-3,y,localZ,x+3,y,localZ,WOOD);a.box(x-3,y+1,localZ,x+3,y+4,localZ,AIR);
        }
        for(int z=-2110;z<=-1680;z++) {
            int x=(int)Math.round(KappaWatercourse.centreX(z))-32;
            int y=(int)Math.floor(KappaWatercourse.level(z))+3-a.site.y,localZ=z-a.site.z;
            if(z>=-1836 && z<=-1812)continue;
            a.block(x+4,y+1,localZ,RAIL);
            if(Math.floorMod(z,16)==0) {
                for(int dx:new int[]{-3,3})a.box(x+dx,y-12,localZ,x+dx,y-1,localZ,LOG);
                a.block(x+4,y+2,localZ,ModBlocks.LIBRARY_LAMP.getDefaultState());
            }
        }
        int end=(int)Math.round(KappaWatercourse.centreX(-1680))-32;
        a.box(end-7,-1,117,end+7,-1,126,WOOD);a.box(end-7,0,117,end+7,4,126,AIR);
        a.box(end-7,0,126,end+7,0,126,RAIL);
        for(int x:new int[]{end-7,end+7})a.box(x,-8,117,x,-2,126,LOG);
        for(int x:new int[]{end-5,end+5})inspectionLamp(a,x,-1,124);
        a.room("下游歇脚台",end,-1,122);
        a.room("上游沿崖步道",232,4,-312);
    }
}
