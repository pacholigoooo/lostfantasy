package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** Separate old and new trees, with the dwelling cut into the living trunk rather than a hut on top. */
final class FairyTreeHomes {
    static void build(GensokyoBlueprint plan,GensokyoAtlas site,boolean old) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,site);
        int height=old?36:41;
        // Rounded, gently leaning bole. Horizontal runs keep the plan compact.
        for(int y=0;y<=height;y++) {
            double radius=y<16?8.1-y*.055:7.2-(y-16)*.15;
            int shift=y>20?(y-20)/8:0;
            disk(a,shift,y,0,radius,Blocks.LOG.getStateFromMeta(12));
        }
        // Buttress roots taper into the surrounding earth.
        for(int arm=0;arm<7;arm++) {
            double angle=arm*Math.PI*2/7+.2;
            for(int d=6;d<=18;d++) {
                int x=(int)Math.round(Math.cos(angle)*d),z=(int)Math.round(Math.sin(angle)*d);
                int top=Math.max(1,7-(d-6)/2);
                a.box(x-1,1,z-1,x+1,top,z+1,Blocks.LOG.getStateFromMeta(12));
            }
        }
        for(int arm=0;arm<6;arm++) {
            double angle=arm*Math.PI*2/6+(old?.35:.7);
            int by=height-17+arm%3*3;
            for(int d=3;d<=15;d++) {
                int x=(int)Math.round(Math.cos(angle)*d),z=(int)Math.round(Math.sin(angle)*d),y=by+d/2;
                a.box(x-1,y-1,z-1,x+1,y+1,z+1,Blocks.LOG.getStateFromMeta(12));
            }
            crown(a,(int)(Math.cos(angle)*14),by+9,(int)(Math.sin(angle)*14),old?8:9);
        }
        crown(a,3,height,0,10);
        // Carve the shared living room and a loft, retaining a thick bark wall.
        for(int y=2;y<=13;y++)disk(a,0,y,0,5.9,AIR);
        disk(a,0,1,0,6.4,WOOD);disk(a,0,8,0,6.4,WOOD);
        a.box(-1,2,4,1,4,10,AIR);a.box(-1,1,5,1,1,10,WOOD);
        a.room("树心客厅",0,1,3);
        a.table(-1,1,0,3);
        IBlockState chair=Blocks.OAK_STAIRS.getDefaultState();
        a.block(-2,2,0,chair.withProperty(BlockStairs.FACING,EnumFacing.EAST));
        a.block(2,2,0,chair.withProperty(BlockStairs.FACING,EnumFacing.WEST));
        a.block(0,2,2,chair.withProperty(BlockStairs.FACING,EnumFacing.NORTH));
        a.block(-2,2,-4,Blocks.FURNACE.getDefaultState());a.block(0,2,-5,Blocks.CAULDRON.getStateFromMeta(3));
        a.box(1,2,-5,3,2,-5,WOOD);a.chest(-3,1,-5,"village_pantry");
        a.box(-6,3,-3,-6,5,-2,Blocks.BOOKSHELF.getDefaultState());
        a.block(3,5,-3,Blocks.GLOWSTONE.getDefaultState());
        // A staircase follows the eastern trunk, avoiding the table and the three seats.
        for(int i=0;i<7;i++) {
            int z=3-i;
            a.box(3,2+i,z,4,6+i,z,AIR);
            a.box(3,1,z,4,1+i,z,WOOD);
            a.box(3,2+i,z,4,2+i,z,Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH));
        }
        // Three differentiated sleeping alcoves around the upper landing.
        a.box(-5,9,-1,-2,11,-1,WOOD);a.openX(-2,-1,8,0,2);
        a.box(0,9,-5,0,11,-2,WOOD);
        a.bed(-4,8,3);
        if(old) {a.bed(-4,8,-3);a.bed(2,8,-3);}
        else {
            a.bed(-4,1,-3);a.box(-5,2,-2,-3,5,-2,WOOD);
            a.room("星光寝位",-3,1,-4);
        }
        a.box(-5,9,2,-5,11,4,Blocks.WOOL.getStateFromMeta(15));
        a.box(-5,12,1,-2,12,4,Blocks.WOOL.getStateFromMeta(0));
        a.box(-5,9,4,-2,9,4,WOOD);
        for(int x:new int[]{-4,-2})a.block(x,10,4,ModBlocks.DOLL_DISPLAY.getDefaultState().withProperty(BlockHorizontal.FACING,EnumFacing.NORTH));
        a.block(-5,10,2,Blocks.WOOL.getStateFromMeta(0));
        a.chest(-1,8,4);a.chest(1,8,-5);
        a.room("月光寝位",-3,8,2);if(old)a.room("星光寝位",-3,8,-3);
        if(old)a.room("日光寝位",2,8,-1);
        a.block(-3,12,-3,Blocks.GLOWSTONE.getDefaultState());
        // Small glazed windows sunk into the trunk, with wooden lintels and ledges.
        a.box(-1,10,-6,1,12,-5,AIR);a.box(-1,10,-7,1,12,-7,Blocks.GLASS.getDefaultState());
        a.box(-6,3,-1,-5,5,1,AIR);a.box(-7,3,-1,-7,5,1,Blocks.GLASS.getDefaultState());
        a.box(5,10,0,6,12,2,AIR);a.box(7,10,0,7,12,2,Blocks.GLASS.getDefaultState());
        ForestDetails.vinesZ(a,-4,4,-8,3,16,EnumFacing.SOUTH);
        for(int x:new int[]{-12,-10,10,12})for(int z:new int[]{-13,-9,10})
            if(plan.at(site.x+x,site.y+1,site.z+z).getBlock()==Blocks.AIR) {
                a.block(x,0,z,Blocks.DIRT.getStateFromMeta(2));a.block(x,1,z,Blocks.BROWN_MUSHROOM.getDefaultState());
            }
        // The newer tree has a high, sun-facing room in a branch, as shown in OSP.
        if(!old) {
            for(int y=18;y<=25;y++)disk(a,3,y,4,5,Blocks.LOG.getStateFromMeta(12));
            for(int y=19;y<=24;y++)disk(a,3,y,4,4,AIR);
            disk(a,3,18,4,4.5,WOOD);a.bed(5,18,5);a.table(1,18,4,2);
            a.box(2,20,8,4,22,9,Blocks.GLASS.getDefaultState());
            // Ladder is inside the bole and has solid bark backing.
            a.box(4,9,3,4,20,3,Blocks.LOG.getDefaultState());
            a.box(3,9,3,3,20,3,Blocks.LADDER.getDefaultState().withProperty(net.minecraft.block.BlockLadder.FACING,EnumFacing.WEST));
            a.box(3,19,4,3,21,4,AIR);
            a.room("日光寝位",2,18,6);
        }
    }
    private static void disk(GensokyoArchitecture a,int cx,int y,int cz,double radius,IBlockState state) {
        int r=(int)Math.ceil(radius);
        for(int z=-r;z<=r;z++) {
            if(z*z>radius*radius)continue;
            int half=(int)Math.floor(Math.sqrt(radius*radius-z*z));
            a.box(cx-half,y,cz+z,cx+half,y,cz+z,state);
        }
    }
    private static void crown(GensokyoArchitecture a,int x,int y,int z,int radius) {
        for(int dy=-4;dy<=6;dy++) {
            double r=radius*Math.sqrt(Math.max(0,1-dy*dy/49.0));
            disk(a,x+dy/3,y+dy,z,r,Blocks.LEAVES.getStateFromMeta(4));
        }
    }
}
