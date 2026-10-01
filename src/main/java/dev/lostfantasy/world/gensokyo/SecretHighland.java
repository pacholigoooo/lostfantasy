package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;

/** Forest below a tall, dry escarpment; an open alpine shelf above it. */
final class SecretHighland {
    private SecretHighland() {}
    private static double distance(int x,int z) {
        return Math.hypot((x-GensokyoAtlas.SECRET_CLIFF.x+65)/255.0,(z-GensokyoAtlas.SECRET_CLIFF.z+100)/275.0);
    }
    private static double edge(int x) {return -45+8*Math.sin((x-GensokyoAtlas.SECRET_CLIFF.x)/48.0);}
    static Sample at(int x,int z,GensokyoNoise noise) {
        int dz=z-GensokyoAtlas.SECRET_CLIFF.z;
        if(Math.abs((long)x-GensokyoAtlas.SECRET_CLIFF.x)>390 || dz<-445 || dz>245)return null;
        double distance=distance(x,z);if(distance>1.25)return null;
        double cliff=(edge(x)-dz)/8.0,blend=1-GensokyoNoise.smooth((distance-.76)/.49);
        double ground=132+49*GensokyoNoise.smooth(cliff)+noise.value(x,z,29,338)*2;
        return new Sample(ground,blend,blend>.9 && cliff>0 && cliff<1);
    }
    static boolean forest(int x,int z) {
        return Math.abs((long)x-GensokyoAtlas.SECRET_CLIFF.x)<310 && Math.abs((long)z-GensokyoAtlas.SECRET_CLIFF.z)<230
                && distance(x,z)<1.05 && z-GensokyoAtlas.SECRET_CLIFF.z>edge(x)+8;
    }
    static boolean meadow(int x,int z) {
        return Math.abs((long)x-GensokyoAtlas.SECRET_CLIFF.x)<310 && Math.abs((long)z-GensokyoAtlas.SECRET_CLIFF.z)<380
                && distance(x,z)<.98 && z-GensokyoAtlas.SECRET_CLIFF.z<edge(x)-12;
    }
    static void paint(ChunkPrimer p,int cx,int cz,GensokyoTerrain terrain) {
        int ox=cx<<4,oz=cz<<4;
        if(Math.abs((long)ox-GensokyoAtlas.FALSE_HEAVEN.x)>430 || Math.abs((long)oz-GensokyoAtlas.FALSE_HEAVEN.z)>400)return;
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int wx=ox+x,wz=oz+z;if(!meadow(wx,wz) || GensokyoAtlas.reserved(wx,wz,3))continue;
            GensokyoTerrain.Column c=terrain.column(wx,wz);
            if(c.wet() || c.path() || c.ground>199 || c.ground<175 || c.road!=null && c.road.distance<c.road.width+4)continue;
            if(p.getBlockState(x,c.ground+1,z).getBlock()!=Blocks.AIR && p.getBlockState(x,c.ground+1,z).getBlock()!=Blocks.TALLGRASS)continue;
            long h=terrain.hash(wx,wz,339);
            // Continuous fields give snowbanks and flower beds irregular, connected edges.
            double patch=terrain.patchNoise(wx,wz,33,340)+terrain.patchNoise(wx,wz,9,341)*.26;
            if(patch>.23) {
                p.setBlockState(x,c.ground+1,z,Blocks.SNOW_LAYER.getStateFromMeta(patch>.48?2:patch>.35?1:0));
            }else if(patch<-.08 && Math.floorMod(h,13)<3) {
                p.setBlockState(x,c.ground,z,Blocks.GRASS.getDefaultState());
                p.setBlockState(x,c.ground+1,z,ModBlocks.KOMAKUSA.getDefaultState());
            }
        }
    }
    static final class Sample {
        final double ground,blend;final boolean cliff;
        Sample(double ground,double blend,boolean cliff) {this.ground=ground;this.blend=blend;this.cliff=cliff;}
    }
}
