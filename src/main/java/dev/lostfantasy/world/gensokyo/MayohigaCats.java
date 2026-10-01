package dev.lostfantasy.world.gensokyo;

import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

/** Initial inhabitants belong to their generated chunk and use normal entity saving. */
final class MayohigaCats {
    private MayohigaCats() {}
    static void install(Chunk chunk) {
        GensokyoAtlas site=GensokyoAtlas.MAYOHIGA;
        if(Math.abs((chunk.x<<4)-site.x)>site.rx+16 || Math.abs((chunk.z<<4)-site.z)>site.rz+16)return;
        for(int i=0;i<MayohigaVillage.CATS.length;i++) {
            int[] p=MayohigaVillage.CATS[i];BlockPos pos=new BlockPos(site.x+p[0],site.y+p[1],site.z+p[2]);
            if(pos.getX()>>4!=chunk.x || pos.getZ()>>4!=chunk.z)continue;
            if(!chunk.getBlockState(pos.down()).isFullCube() || chunk.getBlockState(pos).getMaterial().blocksMovement())continue;
            EntityOcelot cat=new EntityOcelot(chunk.getWorld());
            cat.setTameSkin(1+i%3);cat.enablePersistence();
            cat.setLocationAndAngles(pos.getX()+.5,pos.getY(),pos.getZ()+.5,(i*71)%360,0);
            chunk.addEntity(cat);
        }
    }
}
