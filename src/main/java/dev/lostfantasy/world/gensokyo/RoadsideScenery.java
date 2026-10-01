package dev.lostfantasy.world.gensokyo;

import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;

/** Small verge patches, sampled alongside the existing terrain column without extra world reads. */
final class RoadsideScenery {
    private RoadsideScenery() {}
    static void paint(ChunkPrimer p,int x,int z,int wx,int wz,GensokyoTerrain.Column c,GensokyoTerrain terrain) {
        if(c.road==null || c.wet() || c.path() || c.canalBank || c.basalt || c.ground>220)return;
        double margin=c.road.distance-c.road.width;
        if(margin<1.25 || margin>5.5 || c.region==GensokyoTerrain.Region.BAMBOO
                || c.region==GensokyoTerrain.Region.FLOWERS || SanzuCoast.region(wx,wz)
                || SeasonalWays.treeless(wx,wz) || RicePaddies.treeless(wx,wz)
                || FlowerLandscapes.treeless(wx,wz))return;
        int gx=Math.floorDiv(wx,20),gz=Math.floorDiv(wz,20);
        long hash=terrain.hash(gx,gz,1031);
        int dx=wx-(gx*20+4+(int)Math.floorMod(hash,12));
        int dz=wz-(gz*20+4+(int)Math.floorMod(hash>>>8,12));
        if(dx*dx/25.0+dz*dz/16.0>1)return;
        int pattern=Math.floorMod(wx*13+wz*7,17),y=c.ground;
        if(c.region==GensokyoTerrain.Region.MOUNTAIN) {
            p.setBlockState(x,y,z,pattern==5 || pattern==0 && margin>3
                    ?Blocks.MOSSY_COBBLESTONE.getDefaultState():Blocks.DIRT.getStateFromMeta(1));
            if(pattern==0 && margin>3)p.setBlockState(x,y+1,z,Blocks.STONE_SLAB.getStateFromMeta(3));
            else if(pattern<5)p.setBlockState(x,y+1,z,Blocks.TALLGRASS.getStateFromMeta(2));
        } else if(c.region==GensokyoTerrain.Region.FOREST) {
            p.setBlockState(x,y,z,Blocks.DIRT.getStateFromMeta(2));
            if(pattern<5)p.setBlockState(x,y+1,z,Blocks.TALLGRASS.getStateFromMeta(2));
            else if(pattern==8)p.setBlockState(x,y+1,z,Blocks.BROWN_MUSHROOM.getDefaultState());
        } else if(c.region==GensokyoTerrain.Region.MEADOW && pattern<5) {
            p.setBlockState(x,y+1,z,Blocks.RED_FLOWER.getStateFromMeta((hash&1)==0?3:8));
        }
    }
}
