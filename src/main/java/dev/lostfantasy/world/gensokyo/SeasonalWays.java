package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;
import static dev.lostfantasy.world.gensokyo.GensokyoAtlas.*;

/** Flowering woodland and lingering snow share the atlas roads and normal day/night lighting. */
final class SeasonalWays {
    private enum Theme { SPRING, CHERRY, SNOW }
    private SeasonalWays() {}

    private static Sample at(int x,int z) {
        if(nearFlowers(x,z,0)) {
            double spring=radius(x,z,SPRING_PATH.x,SPRING_PATH.z,270,205);
            double cherry=radius(x,z,CHERRY_PATH.x,CHERRY_PATH.z,285,195);
            if(spring<1 || cherry<1)return spring<cherry?new Sample(Theme.SPRING,spring):new Sample(Theme.CHERRY,cherry);
        }
        if(x>=565 && x<=1850 && z>=-1490 && z<=-740) {
            double snow=radius(x,z,1200,-1110,620,350);
            if(snow<1)return new Sample(Theme.SNOW,snow);
        }
        return null;
    }
    private static double radius(int x,int z,int cx,int cz,int rx,int rz) {
        return Math.hypot((x-cx)/(double)rx,(z-cz)/(double)rz);
    }
    static boolean treeless(int x,int z) {return at(x,z)!=null;}
    private static boolean nearFlowers(int x,int z,int margin) {
        return Math.abs((long)x-SPRING_PATH.x)<=270+margin && Math.abs((long)z-SPRING_PATH.z)<=205+margin
                || Math.abs((long)x-CHERRY_PATH.x)<=285+margin && Math.abs((long)z-CHERRY_PATH.z)<=195+margin;
    }
    private static boolean near(int x,int z) {
        return nearFlowers(x,z,30)
                || x>=535 && x<=1880 && z>=-1520 && z<=-710;
    }

    static void paint(ChunkPrimer p,int cx,int cz,GensokyoTerrain terrain) {
        int ox=cx<<4,oz=cz<<4;if(!near(ox+8,oz+8))return;
        // Anchors and complete branch shapes are shared by all intersecting chunks.
        for(int gx=Math.floorDiv(ox-15,18);gx<=Math.floorDiv(ox+30,18);gx++)
            for(int gz=Math.floorDiv(oz-15,18);gz<=Math.floorDiv(oz+30,18);gz++) {
                long hash=terrain.hash(gx,gz,901);
                int x=gx*18+(int)Math.floorMod(hash,10),z=gz*18+(int)Math.floorMod(hash>>>8,10);
                Sample s=at(x,z);if(s==null)continue;
                int density=s.theme==Theme.SNOW?14:s.theme==Theme.SPRING?49:77;
                if(Math.floorMod(hash>>>16,100)>=density*(1-GensokyoNoise.smooth((s.radius-.82)/.18)))continue;
                GensokyoTerrain.Column c=terrain.column(x,z);
                if(c.wet() || c.rockSurface || c.ground>214 || GensokyoAtlas.reserved(x,z,24)
                        || c.road!=null && c.road.distance<c.road.width+12)continue;
                if(s.theme==Theme.SNOW)pine(p,ox,oz,x,c.ground,z,hash);
                else cherry(p,ox,oz,x,c.ground,z,hash,s.theme);
            }
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int wx=ox+x,wz=oz+z;Sample s=at(wx,wz);if(s==null)continue;
            GensokyoTerrain.Column c=terrain.column(wx,wz);
            if(c.wet() || c.rockSurface || GensokyoAtlas.reserved(wx,wz,9))continue;
            IBlockState floor=p.getBlockState(x,c.ground,z),above=p.getBlockState(x,c.ground+1,z);
            if(c.path()) {
                if(floor.getBlock()==Blocks.GRAVEL)p.setBlockState(x,c.ground,z,
                        s.theme==Theme.SNOW?Blocks.GRASS.getDefaultState():Blocks.GRASS_PATH.getDefaultState());
                continue;
            }
            if(floor.getBlock()!=Blocks.GRASS && floor.getBlock()!=Blocks.DIRT)continue;
            if(above.getBlock()!=Blocks.AIR && above.getBlock()!=Blocks.TALLGRASS)continue;
            long hash=terrain.hash(wx,wz,902);int chance=(int)Math.floorMod(hash,100);
            double fade=1-GensokyoNoise.smooth((s.radius-.8)/.2);
            if(s.theme==Theme.SNOW) {
                double patch=terrain.patchNoise(wx,wz,36,903)*.7+terrain.patchNoise(wx,wz,11,904)*.3;
                double road=c.road==null?30:c.road.distance-c.road.width;
                if(road>2 && patch>.06+(1-fade)*.65) {
                    int layers=1+(int)Math.min(3,Math.max(0,(patch-.12)*8));
                    p.setBlockState(x,c.ground+1,z,Blocks.SNOW_LAYER.getStateFromMeta(layers-1));
                } else if(chance<9*fade && road>1)p.setBlockState(x,c.ground+1,z,Blocks.TALLGRASS.getStateFromMeta(1));
                continue;
            }
            double flowers=terrain.patchNoise(wx,wz,29,905);
            if(chance<fade*(flowers>.05?36:9)) {
                // Low red flowers leave clear views along the path; a few rose bushes mark its edges.
                boolean tall=chance<3 && flowers>.15 && p.getBlockState(x,c.ground+2,z).getBlock()==Blocks.AIR;
                p.setBlockState(x,c.ground+1,z,tall?Blocks.DOUBLE_PLANT.getStateFromMeta(4):Blocks.RED_FLOWER.getDefaultState());
                if(tall)p.setBlockState(x,c.ground+2,z,Blocks.DOUBLE_PLANT.getStateFromMeta(8));
            } else if(chance<fade*63)p.setBlockState(x,c.ground+1,z,ModBlocks.FALLEN_PETALS.getDefaultState());
            else if(chance<fade*78)p.setBlockState(x,c.ground+1,z,Blocks.TALLGRASS.getStateFromMeta(1));
        }
    }

    private static void cherry(ChunkPrimer p,int ox,int oz,int x,int y,int z,long hash,Theme theme) {
        int height=8+(int)Math.floorMod(hash>>>24,5),lean=(hash&1)==0?1:-1;
        IBlockState wood=Blocks.LOG.getStateFromMeta(12);
        IBlockState leaves=(theme==Theme.CHERRY && Math.floorMod(hash>>>29,4)==0?
                ModBlocks.PURPLE_CHERRY_LEAVES:ModBlocks.CHERRY_LEAVES).getDefaultState();
        for(int dy=1;dy<=height;dy++) {
            int tx=x+(dy>height/2?lean:0);
            put(p,ox,oz,tx,y+dy,z,wood,false);
            if(dy<4)put(p,ox,oz,tx+1,y+dy,z,wood,false);
        }
        crown(p,ox,oz,x+lean,y+height+2,z,5,leaves);
        int branch=0;
        for(int[] dir:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
            int reach=4+(int)Math.floorMod(hash>>>(30+branch*3),3);
            int base=y+height-3+(branch++&1);
            for(int d=1;d<=reach;d++)put(p,ox,oz,x+lean+dir[0]*d,base+d/2,z+dir[1]*d,wood,false);
            crown(p,ox,oz,x+lean+dir[0]*reach,base+reach/2+1,z+dir[1]*reach,4,leaves);
        }
    }
    private static void crown(ChunkPrimer p,int ox,int oz,int x,int y,int z,int radius,IBlockState leaves) {
        int minDx=Math.max(-radius,ox-x),maxDx=Math.min(radius,ox+15-x);
        int minDz=Math.max(-radius,oz-z),maxDz=Math.min(radius,oz+15-z);
        for(int dx=minDx;dx<=maxDx;dx++)for(int dz=minDz;dz<=maxDz;dz++)for(int dy=-2;dy<=3;dy++)
            if(dx*dx+dz*dz+dy*dy*2<radius*radius+2 && Math.floorMod(dx*11+dz*17+dy*7,23)!=0)
                put(p,ox,oz,x+dx,y+dy,z+dz,leaves,true);
    }
    private static void pine(ChunkPrimer p,int ox,int oz,int x,int y,int z,long hash) {
        int height=10+(int)Math.floorMod(hash>>>24,5);
        for(int dy=1;dy<=height;dy++)put(p,ox,oz,x,y+dy,z,Blocks.LOG.getStateFromMeta(1),false);
        for(int dy=4;dy<=height+1;dy++) {
            int r=Math.max(1,(height-dy+2)/3);
            int minDx=Math.max(-r,ox-x),maxDx=Math.min(r,ox+15-x);
            int minDz=Math.max(-r,oz-z),maxDz=Math.min(r,oz+15-z);
            for(int dx=minDx;dx<=maxDx;dx++)for(int dz=minDz;dz<=maxDz;dz++)
                if(dx*dx+dz*dz<r*r+1)put(p,ox,oz,x+dx,y+dy,z+dz,Blocks.LEAVES.getStateFromMeta(5),true);
        }
    }
    private static void put(ChunkPrimer p,int ox,int oz,int x,int y,int z,IBlockState state,boolean airOnly) {
        x-=ox;z-=oz;
        if(x>=0 && x<16 && z>=0 && z<16 && y>0 && y<255
                && (!airOnly || p.getBlockState(x,y,z).getBlock()==Blocks.AIR))p.setBlockState(x,y,z,state);
    }
    private static final class Sample {
        final Theme theme;final double radius;
        Sample(Theme theme,double radius) {this.theme=theme;this.radius=radius;}
    }
}
