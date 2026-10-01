package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;
import static dev.lostfantasy.world.gensokyo.GensokyoArchitecture.*;

/** A wooded burial clearing and Nazrin's small, separate salvage shelter. */
final class Muenzuka {
    static final GensokyoAtlas SITE=GensokyoAtlas.MUENZUKA;
    // One centreline grades the walking route and locates its red flower margins.
    static final int[][] PATH={
            {GensokyoAtlas.ALICE.x,GensokyoAtlas.ALICE.approachZ(),95},
            {GensokyoAtlas.ALICE.x+180,GensokyoAtlas.ALICE.z+235,98},
            {GensokyoAtlas.RECONSIDERATION.x-100,GensokyoAtlas.RECONSIDERATION.z-136,100},
            {GensokyoAtlas.RECONSIDERATION.x,GensokyoAtlas.RECONSIDERATION.z,101},
            {SITE.x-80,SITE.z+112,103},{SITE.x,SITE.approachZ(),SITE.y}};
    private Muenzuka() {}
    static void build(GensokyoBlueprint plan) {
        GensokyoArchitecture a=new GensokyoArchitecture(plan,SITE);
        a.box(-39,1,-36,39,33,37,AIR);
        GensokyoNoise planting=new GensokyoNoise(379);
        // Burials lie under the ground; the clearing has no invented rows of named graves.
        for(int z=-34;z<=35;z++)for(int x=-37;x<=38;x++) {
            double d=x*x/1369.0+z*z/1225.0;
            if(d>1 || x>=16 && x<=37 && z>=-13 && z<=14)continue;
            int h=(int)Math.floorMod(planting.hash(x,z,4),1000);
            if(h<90)a.block(x,0,z,Blocks.DIRT.getStateFromMeta(1));
            if(h<560+240*planting.value(x,z,12,5) && Math.abs(x)>3)a.block(x,1,z,ModBlocks.SPIDER_LILY.getDefaultState());
        }
        for(int[] tree:new int[][]{{-25,-20,18,9},{1,-28,22,10},{27,-22,19,9},{-30,12,17,8},{22,28,18,8}})
            cherry(a,tree[0],tree[1],tree[2],tree[3]);
        a.box(-2,0,-16,2,0,43,Blocks.GRASS_PATH.getDefaultState());a.box(-2,1,-16,2,3,43,AIR);
        a.box(0,0,11,27,0,14,Blocks.GRASS_PATH.getDefaultState());a.box(0,1,11,27,3,14,AIR);
        a.box(-21,0,0,0,0,3,Blocks.GRASS_PATH.getDefaultState());a.box(-21,1,0,0,3,3,AIR);
        a.room("林下空地",0,0,-12);a.room("紫樱树下",-18,0,1);a.room("归路",0,0,35);
        shelter(a);
    }
    private static void shelter(GensokyoArchitecture a) {
        a.box(19,0,-10,31,0,7,Blocks.COBBLESTONE.getDefaultState());
        a.box(19,1,-10,31,6,7,WOOD);a.box(20,2,-9,30,5,6,AIR);
        for(int x:new int[]{19,31})for(int z:new int[]{-10,7})a.box(x,1,z,x,6,z,LOG);
        a.box(22,3,-10,25,4,-10,Blocks.GLASS_PANE.getDefaultState());
        a.box(31,3,-6,31,4,-3,Blocks.GLASS_PANE.getDefaultState());
        a.openZ(26,7,1,1,3);a.stairsSouth(26,8,0,1,2);
        for(int x=17;x<=33;x++) {
            int rise=(8-Math.abs(x-25))/3;
            a.box(x,7+rise,-12,x,7+rise,9,ModBlocks.THATCH.getDefaultState());
            if(rise>0)for(int z:new int[]{-10,7})a.box(x,7,z,x,6+rise,z,WOOD);
        }
        a.bed(21,1,-5);a.chest(29,1,-8,"nazrin_salvage");a.chest(29,1,-4,"village_pantry");
        a.block(29,2,2,Blocks.FURNACE.getDefaultState());a.block(29,2,4,Blocks.CRAFTING_TABLE.getDefaultState());
        VillageJoinery.lowDesk(a,21,1,2,3);VillageJoinery.lantern(a,26,1,-2);
        a.box(20,2,-1,24,4,-1,WOOD);a.box(28,2,-1,30,4,-1,WOOD);
        a.box(23,2,-9,25,2,-9,DARK);a.block(24,3,-9,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.box(20,2,-3,20,4,-2,Blocks.BOOKSHELF.getDefaultState());
        a.box(28,4,5,30,4,5,Blocks.WOODEN_SLAB.getStateFromMeta(1));
        a.block(28,5,5,Blocks.FLOWER_POT.getDefaultState());
        a.room("临时小屋",26,1,2);a.room("寝位与收存",24,1,-5);
        a.box(9,0,-10,18,0,7,Blocks.GRAVEL.getDefaultState());
        a.box(9,1,-10,18,5,7,AIR);
        for(int z:new int[]{-10,7})a.box(9,1,z,9,4,z,LOG);
        a.box(9,5,-11,18,5,8,Blocks.WOODEN_SLAB.getStateFromMeta(1));a.box(18,1,-10,18,5,7,WOOD);
        a.box(11,1,-7,15,1,-7,DARK);a.block(11,2,-7,ModBlocks.OUTSIDE_TELEVISION.getDefaultState());
        a.block(14,2,-7,ModBlocks.RESEARCH_NOTES.getDefaultState());
        a.chest(10,0,-3,"nazrin_salvage");a.chest(16,0,-3,"nazrin_salvage");a.block(13,1,4,Blocks.ANVIL.getDefaultState());
        a.room("旧物分拣棚",13,0,0);a.room("小屋前",26,0,12);
    }
    private static void cherry(GensokyoArchitecture a,int x,int z,int height,int radius) {
        IBlockState bark=Blocks.LOG2.getStateFromMeta(13),bloom=ModBlocks.PURPLE_CHERRY_LEAVES.getDefaultState();
        a.box(x,0,z,x+1,height,z+1,bark);
        for(int side:new int[]{-1,1})for(int d=1;d<=3;d++) {
            a.box(x+side*d,0,z,x+side*d,Math.max(0,2-d),z+1,bark);
            a.box(x,0,z+side*d,x+1,Math.max(0,2-d),z+side*d,bark);
        }
        for(int[] direction:new int[][]{{1,0},{-1,1},{0,-1}}) {
            for(int n=1;n<=radius-2;n++)a.block(x+direction[0]*n,height-7+n/2,z+direction[1]*n,bark);
            int cx=x+direction[0]*(radius-3),cz=z+direction[1]*(radius-3),cy=height-3;
            for(int dy=-3;dy<=3;dy++)for(int dx=-6;dx<=6;dx++)for(int dz=-6;dz<=6;dz++)
                if(dx*dx+dz*dz+dy*dy*4<36 && Math.floorMod(dx*19+dz*13+dy,17)!=0)a.block(cx+dx,cy+dy,cz+dz,bloom);
        }
        for(int dy=-3;dy<=3;dy++)for(int dz=-radius;dz<=radius;dz++) {
            double n=radius*radius-dz*dz-dy*dy*5;if(n<0)continue;
            int width=(int)Math.sqrt(n);a.box(x-width,height+dy,z+dz,x+width,height+dy,z+dz,bloom);
        }
    }
    static double pathDistance(int x,int z) {
        if(x<270 || x>1120 || z<1740 || z>2690)return Double.POSITIVE_INFINITY;
        double nearest=Double.POSITIVE_INFINITY;
        for(int i=1;i<PATH.length;i++) {
            int[] a=PATH[i-1],b=PATH[i];double dx=b[0]-a[0],dz=b[1]-a[1];
            double t=Math.max(0,Math.min(1,((x-a[0])*dx+(z-a[1])*dz)/(dx*dx+dz*dz)));
            nearest=Math.min(nearest,Math.hypot(x-a[0]-t*dx,z-a[1]-t*dz));
        }
        return nearest;
    }
    static boolean forest(int x,int z) {
        return SITE.contains(x,z,78) || z>GensokyoAtlas.ALICE.z+130 && pathDistance(x,z)<76;
    }
    static void paint(ChunkPrimer p,int cx,int cz,GensokyoTerrain terrain) {
        int ox=cx<<4,oz=cz<<4;if(ox<240 || ox>1136 || oz<1888 || oz>2704)return;
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int wx=ox+x,wz=oz+z;double distance=pathDistance(wx,wz);
            if(distance<4 || distance>25 || GensokyoAtlas.reserved(wx,wz,2))continue;
            GensokyoTerrain.Column c=terrain.column(wx,wz);
            if(c.wet() || c.path() || c.rockSurface || p.getBlockState(x,c.ground+1,z).getMaterial().isSolid())continue;
            if(!p.getBlockState(x,c.ground,z).getMaterial().isSolid())continue;
            double edge=1-GensokyoNoise.smooth((distance-13)/12),patches=.46+.23*terrain.patchNoise(wx,wz,23,307);
            if(Math.floorMod(terrain.hash(wx,wz,308),1000)<edge*patches*1000)
                p.setBlockState(x,c.ground+1,z,ModBlocks.SPIDER_LILY.getDefaultState());
        }
    }
}
