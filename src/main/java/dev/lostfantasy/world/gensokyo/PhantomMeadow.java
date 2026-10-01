package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;

/** An open flowering plain; waterways and roads are graded after its rolling ground. */
final class PhantomMeadow {
    private static final GensokyoAtlas SITE=GensokyoAtlas.PHANTOM_MEADOW;
    private static final int RX=500,RZ=360;
    private PhantomMeadow() {}
    private static double radius(int x,int z) {
        if(Math.abs((long)x-SITE.x)>RX || Math.abs((long)z-SITE.z)>RZ)return 2;
        return Math.hypot((x-SITE.x)/(double)RX,(z-SITE.z)/(double)RZ);
    }
    static boolean contains(int x,int z) {return radius(x,z)<1;}
    static double shape(int x,int z,double height,GensokyoNoise noise) {
        double d=radius(x,z);if(d>=1)return height;
        double target=SITE.y+noise.value(x,z,175,920)*4+noise.value(x,z,46,921)*1.5;
        return GensokyoNoise.lerp(target,height,GensokyoNoise.smooth((d-.68)/.32));
    }
    static void paint(ChunkPrimer p,int cx,int cz,GensokyoTerrain terrain) {
        int ox=cx<<4,oz=cz<<4;
        if(Math.abs((long)ox+8-SITE.x)>RX+16 || Math.abs((long)oz+8-SITE.z)>RZ+16)return;
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int wx=ox+x,wz=oz+z;double d=radius(wx,wz);if(d>=1)continue;
            GensokyoTerrain.Column c=terrain.column(wx,wz);
            if(c.wet() || c.path() || c.rockSurface || GensokyoAtlas.reserved(wx,wz,8)
                    || p.getBlockState(x,c.ground,z).getBlock()!=Blocks.GRASS)continue;
            IBlockState above=p.getBlockState(x,c.ground+1,z);
            if(above.getBlock()!=Blocks.AIR && above.getBlock()!=Blocks.TALLGRASS)continue;
            long h=terrain.hash(wx,wz,922);int chance=(int)Math.floorMod(h,100);
            double patch=terrain.patchNoise(wx,wz,35,923);
            double fade=1-GensokyoNoise.smooth((d-.8)/.2);
            IBlockState plant=null;
            if(chance<(patch>-.08?37:13)*fade) {
                double color=terrain.patchNoise(wx,wz,67,924)+(Math.floorMod(h>>>12,11)-5)*.025;
                plant=color<-.16?Blocks.RED_FLOWER.getDefaultState():color>.17?
                        Blocks.RED_FLOWER.getStateFromMeta(5):Blocks.YELLOW_FLOWER.getDefaultState();
            } else if(chance<67*fade)plant=ModBlocks.CLOVER.getDefaultState();
            else if(chance<82*fade)plant=Blocks.TALLGRASS.getStateFromMeta(1);
            if(plant!=null)p.setBlockState(x,c.ground+1,z,plant);
        }
    }
}
