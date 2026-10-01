package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;

/** Separate river-maple and green tanuki woods, painted from deterministic tree anchors. */
final class YoukaiWoodlands {
    private static final int[][] DIRECTIONS={{1,0},{0,1},{-1,0},{0,-1}};
    private YoukaiWoodlands() {}
    private static int kind(int x,int z) {
        if(nearCascade(x,z,0) && (Math.abs(x-GensokyoAtlas.WATERFALL.x)>135
                || z<GensokyoAtlas.WATERFALL.z-145 || z>GensokyoAtlas.WATERFALL.z+210))return 1;
        if(x>=-690 && x<=562 && z>=-1606 && z<=-426
                && radius(x,z,-64,-1016,625,590)<1)return 1;
        GensokyoAtlas t=GensokyoAtlas.TANUKI_FOREST;
        if(Math.abs((long)x-t.x)<390 && Math.abs((long)z-t.z)<320
                && radius(x,z,t.x,t.z,390,320)<1)return 2;
        return 0;
    }
    private static boolean nearCascade(int x,int z,int margin) {
        return Math.abs((long)x-GensokyoAtlas.WATERFALL.x)<275+margin
                && Math.abs((long)z-GensokyoAtlas.WATERFALL.z)<285+margin;
    }
    private static double radius(int x,int z,int cx,int cz,int rx,int rz) {
        return Math.hypot((x-cx)/(double)rx,(z-cz)/(double)rz);
    }
    static boolean contains(int x,int z) {return kind(x,z)!=0;}
    static void paint(ChunkPrimer p,int cx,int cz,GensokyoTerrain terrain) {
        int ox=cx<<4,oz=cz<<4;
        GensokyoAtlas t=GensokyoAtlas.TANUKI_FOREST;
        if(!(ox>=-720 && ox<=592 && oz>=-1636 && oz<=-396)
                && !(Math.abs((long)ox+8-t.x)<420 && Math.abs((long)oz+8-t.z)<350)
                && !nearCascade(ox+8,oz+8,25))return;
        // Each crown reaches at most thirteen blocks from its anchor.
        for(int gx=Math.floorDiv(ox-14,16);gx<=Math.floorDiv(ox+29,16);gx++)
            for(int gz=Math.floorDiv(oz-14,16);gz<=Math.floorDiv(oz+29,16);gz++) {
                long h=terrain.hash(gx,gz,930);
                int x=gx*16+(int)Math.floorMod(h,9),z=gz*16+(int)Math.floorMod(h>>>8,9),kind=kind(x,z);
                if(kind==0 || Math.floorMod(h>>>16,100)>=(kind==1?88:84)
                        || TanukiClearing.contains(x,z,15) || GensokyoAtlas.reserved(x,z,20))continue;
                GensokyoTerrain.Column c=terrain.column(x,z);
                if(c.wet() || c.rockSurface || c.ground>200 || c.road!=null && c.road.distance<c.road.width+9)continue;
                tree(p,ox,oz,x,c.ground,z,h,kind,terrain);
            }
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int wx=ox+x,wz=oz+z,kind=kind(wx,wz);
            if(kind==0 || TanukiClearing.contains(wx,wz,3) || GensokyoAtlas.reserved(wx,wz,8))continue;
            GensokyoTerrain.Column c=terrain.column(wx,wz);
            if(c.wet() || c.path() || c.rockSurface || p.getBlockState(x,c.ground,z).getBlock()!=Blocks.GRASS)continue;
            IBlockState above=p.getBlockState(x,c.ground+1,z);
            if(above.getBlock()!=Blocks.AIR && above.getBlock()!=Blocks.TALLGRASS)continue;
            long h=terrain.hash(wx,wz,931);int chance=(int)Math.floorMod(h,100);
            double patch=terrain.patchNoise(wx,wz,22,932);
            if(kind==1 && chance<(patch>0?54:26))
                p.setBlockState(x,c.ground+1,z,ModBlocks.AUTUMN_LEAVES.getDefaultState());
            else if(chance<70 && patch>-.2)p.setBlockState(x,c.ground+1,z,Blocks.TALLGRASS.getStateFromMeta(2));
            else if(chance>94 && patch>.1) {
                p.setBlockState(x,c.ground,z,Blocks.DIRT.getStateFromMeta(2));
                p.setBlockState(x,c.ground+1,z,Blocks.BROWN_MUSHROOM.getDefaultState());
            }
        }
    }
    private static void tree(ChunkPrimer p,int ox,int oz,int x,int y,int z,long hash,int kind,GensokyoTerrain terrain) {
        int height=17+(int)Math.floorMod(hash>>>23,8),lean=(hash&1)==0?1:-1;
        IBlockState bark=Blocks.LOG.getStateFromMeta(12);
        IBlockState leaves=kind==2?Blocks.LEAVES.getStateFromMeta(4):
                (Math.floorMod(hash>>>29,5)<2?ModBlocks.MAPLE_LEAVES:
                        Math.floorMod(hash>>>29,5)<4?ModBlocks.ORANGE_MAPLE_LEAVES:ModBlocks.GOLDEN_MAPLE_LEAVES).getDefaultState();
        for(int dy=1;dy<=height;dy++) {
            int bend=dy>height*2/3?lean:0;
            put(p,ox,oz,x+bend,y+dy,z,bark,false);
            if(dy<height-5)put(p,ox,oz,x+1,y+dy,z,bark,false);
        }
        crown(p,ox,oz,x+lean,y+height,z,6,leaves);
        for(int i=0;i<4;i++) {
            int[] dir=DIRECTIONS[i];int reach=5+(int)Math.floorMod(hash>>>(33+i*4),3),base=y+height-6+(i&1);
            put(p,ox,oz,x,base,z,bark,false);
            for(int d=1;d<=reach;d++)put(p,ox,oz,x+dir[0]*d,base+d/3,z+dir[1]*d,bark,false);
            crown(p,ox,oz,x+dir[0]*reach,base+reach/3+1,z+dir[1]*reach,5,leaves);
            // Roots follow the local bank; they cannot bridge water or bury a path.
            for(int d=1;d<=3;d++) {
                int rx=x+dir[0]*d,rz=z+dir[1]*d;GensokyoTerrain.Column root=terrain.column(rx,rz);
                if(root.wet() || root.path() || Math.abs(root.ground-y)>2)break;
                for(int ry=root.ground;ry<=Math.max(root.ground,y+2-d/2);ry++)put(p,ox,oz,rx,ry,rz,bark,false);
            }
        }
    }
    private static void crown(ChunkPrimer p,int ox,int oz,int x,int y,int z,int r,IBlockState leaves) {
        int minDx=Math.max(-r,ox-x),maxDx=Math.min(r,ox+15-x);
        int minDz=Math.max(-r,oz-z),maxDz=Math.min(r,oz+15-z);
        for(int dx=minDx;dx<=maxDx;dx++)for(int dz=minDz;dz<=maxDz;dz++)for(int dy=-3;dy<=3;dy++)
            if(dx*dx+dz*dz+dy*dy*2<r*r+3 && Math.floorMod(dx*13+dz*7+dy*11,29)!=0)
                put(p,ox,oz,x+dx,y+dy,z+dz,leaves,true);
    }
    private static void put(ChunkPrimer p,int ox,int oz,int x,int y,int z,IBlockState state,boolean airOnly) {
        x-=ox;z-=oz;
        if(x>=0 && x<16 && z>=0 && z<16 && y>0 && y<255
                && (!airOnly || p.getBlockState(x,y,z).getBlock()==Blocks.AIR))p.setBlockState(x,y,z,state);
    }
}
