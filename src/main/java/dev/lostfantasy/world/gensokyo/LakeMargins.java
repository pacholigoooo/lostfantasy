package dev.lostfantasy.world.gensokyo;

import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.ChunkPrimer;

/** Shallow-water planting and stone beaches, generated as ordinary chunk blocks. */
final class LakeMargins {
    private LakeMargins() {}
    static void paint(ChunkPrimer p,int cx,int cz,GensokyoTerrain terrain) {
        int ox=cx<<4,oz=cz<<4;
        boolean mist=Math.abs(ox+8-GensokyoAtlas.MIST_LAKE.x)<735 && Math.abs(oz+8-GensokyoAtlas.MIST_LAKE.z)<800;
        boolean wind=Math.abs(ox+8-GensokyoAtlas.WIND_LAKE.x)<285 && Math.abs(oz+8-GensokyoAtlas.WIND_LAKE.z)<190;
        if(!mist && !wind)return;
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            int wx=ox+x,wz=oz+z;GensokyoTerrain.Column c=terrain.column(wx,wz);
            if(c.path() || GensokyoAtlas.reserved(wx,wz,5))continue;
            if(Math.hypot((wx-GensokyoAtlas.CIRNO.x)/48.0,(wz-GensokyoAtlas.CIRNO.z+2)/42.0)<1)continue;
            int level=mist?72:173;
            if(c.ground<level-3 || c.ground>level+2)continue;
            int chance=(int)Math.floorMod(terrain.hash(wx,wz,984),100);
            double patch=terrain.patchNoise(wx,wz,32,985);
            if(c.wet()) {
                if(c.water!=level)continue;
                p.setBlockState(x,c.ground,z,patch>.15?Blocks.CLAY.getDefaultState():Blocks.GRAVEL.getDefaultState());
                if(patch>.24 && chance<9 && c.water-c.ground<=2
                        && p.getBlockState(x,level,z).getBlock()==Blocks.WATER
                        && p.getBlockState(x,level+1,z).getBlock()==Blocks.AIR)
                    p.setBlockState(x,level+1,z,Blocks.WATERLILY.getDefaultState());
            } else if(p.getBlockState(x,c.ground+1,z).getBlock()==Blocks.AIR
                    || p.getBlockState(x,c.ground+1,z).getBlock()==Blocks.TALLGRASS) {
                if(c.ground==level && chance<23 && patch>-.15) {
                    boolean water=false;
                    for(EnumFacing side:EnumFacing.HORIZONTALS) {
                        GensokyoTerrain.Column near=terrain.column(wx+side.getXOffset(),wz+side.getZOffset());
                        water|=near.wet() && near.water==level;
                    }
                    if(water) {
                        p.setBlockState(x,c.ground,z,Blocks.DIRT.getDefaultState());
                        p.setBlockState(x,c.ground+1,z,Blocks.REEDS.getDefaultState());
                        if(chance<12)p.setBlockState(x,c.ground+2,z,Blocks.REEDS.getDefaultState());
                        continue;
                    }
                }
                if(patch<-.18 && chance<36) {
                    p.setBlockState(x,c.ground,z,chance<12?Blocks.MOSSY_COBBLESTONE.getDefaultState():Blocks.GRAVEL.getDefaultState());
                    p.setBlockState(x,c.ground+1,z,Blocks.AIR.getDefaultState());
                }
                else if(patch>.18 && chance<21)p.setBlockState(x,c.ground+1,z,Blocks.TALLGRASS.getStateFromMeta(2));
            }
        }
    }
}
