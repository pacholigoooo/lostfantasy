package dev.lostfantasy.world.gensokyo;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

/** Ordinary animals saved with their initial chunk, not repeatedly spawned by a tick handler. */
final class ChireidenPets {
    static final int[][] POSITIONS={{-70,4,-36},{-70,4,0},{70,16,0},{70,16,2},{-80,4,-36},{-80,4,0}};
    private ChireidenPets() {}
    static void install(Chunk chunk) {
        for(int i=0;i<POSITIONS.length;i++) {
            int[] xyz=POSITIONS[i];BlockPos p=Chireiden.local(xyz[0],xyz[1],xyz[2]);
            if((p.getX()>>4)!=chunk.x || (p.getZ()>>4)!=chunk.z)continue;
            if(!chunk.getBlockState(p.down()).isFullCube() || chunk.getBlockState(p).getMaterial().blocksMovement() || chunk.getBlockState(p.up()).getMaterial().blocksMovement())continue;
            EntityLiving animal;
            if(i<4) {EntityOcelot cat=new EntityOcelot(chunk.getWorld());cat.setTameSkin(1+i%3);animal=cat;}
            else {EntityRabbit rabbit=new EntityRabbit(chunk.getWorld());rabbit.setRabbitType(i-4);animal=rabbit;}
            animal.enablePersistence();animal.setLocationAndAngles(p.getX()+.5,p.getY(),p.getZ()+.5,i*57,0);chunk.addEntity(animal);
        }
    }
}
