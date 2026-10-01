package dev.lostfantasy.client;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

/** Short-lived luminous motes, using the vanilla particle atlas and no collision queries. */
final class SpellParticle extends Particle {
    SpellParticle(World world,double x,double y,double z,double vx,double vy,double vz,int color,float size,int life) {
        super(world,x,y,z);motionX=vx;motionY=vy;motionZ=vz;
        particleRed=(color>>16&255)/255f;particleGreen=(color>>8&255)/255f;particleBlue=(color&255)/255f;
        particleScale=size;particleMaxAge=life;particleAlpha=0;canCollide=false;setParticleTextureIndex(0);
    }
    @Override public void onUpdate() {
        prevPosX=posX;prevPosY=posY;prevPosZ=posZ;
        if(particleAge++>=particleMaxAge){setExpired();return;}
        setPosition(posX+motionX,posY+motionY,posZ+motionZ);
        motionX*=.96;motionY*=.96;motionZ*=.96;
        particleAlpha=.75f*Math.min(1,particleAge/3f)*Math.max(0,1-particleAge/(float)particleMaxAge);
    }
    @Override public int getBrightnessForRender(float partial) {return 0xf000f0;}
}
