package dev.lostfantasy.world.gensokyo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;

/** Trees are anchored on a seed grid. Every intersecting chunk paints the same tree slice. */
final class GensokyoVegetation {
    private GensokyoVegetation() {}
    static void paint(ChunkPrimer p,int cx,int cz,GensokyoTerrain terrain) {
        int ox=cx<<4,oz=cz<<4;
        for(int gx=Math.floorDiv(ox-10,10);gx<=Math.floorDiv(ox+25,10);gx++)
            for(int gz=Math.floorDiv(oz-10,10);gz<=Math.floorDiv(oz+25,10);gz++) {
                long h=terrain.hash(gx,gz,70);int x=gx*10+(int)Math.floorMod(h,6),z=gz*10+(int)Math.floorMod(h>>>8,6);
                GensokyoTerrain.Column c=terrain.column(x,z);
                if(c.region==GensokyoTerrain.Region.BAMBOO || SanzuCoast.treeless(x,z) || SecretHighland.meadow(x,z) || ToadPond.grove(x,z) || FlowerLandscapes.treeless(x,z) || RicePaddies.treeless(x,z) || SeasonalWays.treeless(x,z)
                        || PhantomMeadow.contains(x,z) || YoukaiWoodlands.contains(x,z))continue;
                int density=c.region==GensokyoTerrain.Region.FOREST?82:
                        c.region==GensokyoTerrain.Region.MOUNTAIN?42:12;
                boolean mountain=c.region==GensokyoTerrain.Region.MOUNTAIN;
                boolean gorge=mountain && Math.abs(x-GensokyoAtlas.WATERFALL.x)<470
                        && Math.abs(z-GensokyoAtlas.WATERFALL.z)<440;
                if(gorge)density=66;
                if(c.region==GensokyoTerrain.Region.FOREST)
                    density+=(int)(terrain.patchNoise(x,z,76,981)*25);
                if(Math.floorMod(h>>>16,100)>=density || c.wet() || c.ground>(mountain?220:202) || GensokyoAtlas.reserved(x,z,8)
                        || c.road!=null && c.road.distance<c.road.width+7)continue;
                if(mountain) {
                    // Trees need a real ledge: leave cliff faces and narrow rock tips exposed.
                    int slope=Math.max(Math.abs(terrain.column(x-3,z).ground-c.ground),Math.abs(terrain.column(x+3,z).ground-c.ground));
                    slope=Math.max(slope,Math.max(Math.abs(terrain.column(x,z-3).ground-c.ground),Math.abs(terrain.column(x,z+3).ground-c.ground)));
                    if(slope>4 || c.rockSurface && (slope>1 || Math.floorMod(h>>>19,4)!=0))continue;
                } else if(c.rockSurface)continue;
                boolean maple=mountain && c.ground<213
                        && Math.floorMod(h>>>20,100)<(gorge?82:44+terrain.patchNoise(x,z,150,959)*30);
                boolean pine=c.region==GensokyoTerrain.Region.MOUNTAIN && !maple;
                boolean forest=c.region==GensokyoTerrain.Region.FOREST;
                int height=(pine?10:forest?13:6)+(int)Math.floorMod(h>>>23,forest?17:6);
                IBlockState log=Blocks.LOG.getStateFromMeta(pine?1:0);
                double autumn=maple?terrain.patchNoise(x,z,57,960):0;
                IBlockState leaves=maple?(autumn<-.18?dev.lostfantasy.ModBlocks.GOLDEN_MAPLE_LEAVES:
                        autumn>.23?dev.lostfantasy.ModBlocks.MAPLE_LEAVES:dev.lostfantasy.ModBlocks.ORANGE_MAPLE_LEAVES).getDefaultState():Blocks.LEAVES.getStateFromMeta(pine?5:4);
                int lean=forest?((h&1)==0?1:-1):0;
                for(int y=1;y<=height;y++) {
                    int tx=x+lean*y/height;
                    put(p,ox,oz,tx,c.ground+y,z,log,false);
                    if(forest && y<height-3)put(p,ox,oz,tx+1,c.ground+y,z,log,false);
                }
                if(forest) {
                    for(int[] dir:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                        for(int distance=1;distance<=4;distance++) {
                            int rx=x+dir[0]*distance,rz=z+dir[1]*distance;
                            GensokyoTerrain.Column root=terrain.column(rx,rz);
                            if(!root.wet())for(int y=root.ground;y<=Math.max(root.ground,c.ground+3-distance/2);y++)
                                put(p,ox,oz,rx,y,rz,Blocks.LOG.getStateFromMeta(12),false);
                            put(p,ox,oz,x+dir[0]*distance,c.ground+height-6+distance/2,z+dir[1]*distance,
                                    Blocks.LOG.getStateFromMeta(12),false);
                        }
                    }
                    // Podzol supports mushrooms even at the occasional break in the canopy.
                    for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++) {
                        if(Math.floorMod(terrain.hash(x+dx,z+dz,83),13)!=0)continue;
                        GensokyoTerrain.Column patch=terrain.column(x+dx,z+dz);
                        if(patch.wet())continue;
                        int ground=patch.ground;
                        put(p,ox,oz,x+dx,ground,z+dz,Blocks.DIRT.getStateFromMeta(2),false);
                        put(p,ox,oz,x+dx,ground+1,z+dz,(dx+dz)%2==0?Blocks.BROWN_MUSHROOM.getDefaultState():Blocks.RED_MUSHROOM.getDefaultState(),true);
                    }
                }
                if(pine)for(int dy=-7;dy<=2;dy++) {
                    int radius=Math.max(0,3-(dy+7)/3+(dy%2==0?1:0));
                    int minDx=Math.max(-radius,ox-x),maxDx=Math.min(radius,ox+15-x);
                    int minDz=Math.max(-radius,oz-z),maxDz=Math.min(radius,oz+15-z);
                    for(int dx=minDx;dx<=maxDx;dx++)for(int dz=minDz;dz<=maxDz;dz++)
                        if(dx*dx+dz*dz<=radius*radius+1)
                            put(p,ox,oz,x+dx,c.ground+height+dy,z+dz,leaves,true);
                } else {
                    crown(p,ox,oz,x+lean,c.ground+height,z,forest?5:4,leaves);
                    if(maple) {
                        int side=(h&2)==0?-1:1;
                        for(int n=1;n<=4;n++)put(p,ox,oz,x+side*n,c.ground+height-3+n/2,z+n/2,Blocks.LOG.getStateFromMeta(12),false);
                        crown(p,ox,oz,x+side*4,c.ground+height-1,z+2,3,leaves);
                        for(int n=1;n<=3;n++)put(p,ox,oz,x-side*n,c.ground+height-4+n/2,z-n,Blocks.LOG.getStateFromMeta(12),false);
                        crown(p,ox,oz,x-side*3,c.ground+height-2,z-3,3,leaves);
                    } else if(forest) {
                        int side=(h&2)==0?-1:1;
                        crown(p,ox,oz,x+side*4,c.ground+height-4,z+2,4,leaves);
                        crown(p,ox,oz,x-side*3,c.ground+height-3,z-3,4,leaves);
                        for(int n=1;n<=4;n++) {
                            put(p,ox,oz,x+side*n,c.ground+height-7+n/2,z+n/2,Blocks.LOG.getStateFromMeta(12),false);
                            put(p,ox,oz,x-side*n,c.ground+height-6+n/2,z-n,Blocks.LOG.getStateFromMeta(12),false);
                        }
                    }
                }
            }
        BambooGrove.paint(p,cx,cz,terrain);
    }
    private static void crown(ChunkPrimer p,int ox,int oz,int x,int y,int z,int radius,IBlockState leaves) {
        int minDx=Math.max(-radius,ox-x),maxDx=Math.min(radius,ox+15-x);
        int minDz=Math.max(-radius,oz-z),maxDz=Math.min(radius,oz+15-z);
        for(int dy=-3;dy<=3;dy++)for(int dx=minDx;dx<=maxDx;dx++)for(int dz=minDz;dz<=maxDz;dz++)
            if(dx*dx+dz*dz+dy*dy*2<=radius*radius && Math.floorMod(x+dx*11+z+dz*7+dy*3,29)!=0)
                put(p,ox,oz,x+dx,y+dy,z+dz,leaves,true);
    }
    private static void put(ChunkPrimer p,int ox,int oz,int x,int y,int z,IBlockState state,boolean airOnly) {
        x-=ox;z-=oz;
        if(x>=0 && x<16 && z>=0 && z<16 && y>0 && y<255
                && (!airOnly || p.getBlockState(x,y,z).getBlock()==Blocks.AIR))p.setBlockState(x,y,z,state);
    }
}
