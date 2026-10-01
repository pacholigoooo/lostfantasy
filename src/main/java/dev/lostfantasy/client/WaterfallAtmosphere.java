package dev.lostfantasy.client;

import dev.lostfantasy.Balance;
import dev.lostfantasy.world.gensokyo.GensokyoAtlas;
import dev.lostfantasy.world.gensokyo.MountainCascade;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;

/** A small, distance-limited source of ordinary particles at the waterfall's impact basin. */
final class WaterfallAtmosphere {
    private WaterfallAtmosphere() {}
    static void tick(Minecraft mc) {
        if(mc.world==null || mc.world.provider.getDimension()!=Balance.gensokyoDimensionId
                || mc.gameSettings.particleSetting==2 || (mc.world.getTotalWorldTime()&1)!=0)return;
        Entity eye=mc.getRenderViewEntity();if(eye==null)return;
        GensokyoAtlas site=GensokyoAtlas.WATERFALL;
        double dx=eye.posX-site.x,dz=eye.posZ-site.z;
        if(Math.abs(dx)>250 || Math.abs(dz)>210 || eye.posY>225 || eye.posY<85)return;
        int count=mc.gameSettings.particleSetting==0?3:1;
        for(int i=0;i<count;i++) {
            int across=mc.world.rand.nextInt(51)-32;
            double x=site.x+across+.5,z=site.z+MountainCascade.lip(across)+9+mc.world.rand.nextDouble()*35;
            BlockPos column=new BlockPos(x,0,z);
            if(!mc.world.isBlockLoaded(column))continue;
            BlockPos surface=mc.world.getHeight(column).down();
            double y=surface.getY()+1+mc.world.rand.nextDouble()*5;
            if(eye.getDistanceSq(x,y,z)>192*192)continue;
            if(mc.world.getBlockState(surface).getMaterial()!=Material.WATER
                    || !mc.world.isAirBlock(new BlockPos(x,y,z)))continue;
            Particle cloud=mc.effectRenderer.spawnEffectParticle(EnumParticleTypes.CLOUD.getParticleID(),
                    x,y,z,(mc.world.rand.nextDouble()-.5)*.035,.025,.025);
            if(cloud!=null) {
                cloud.setRBGColorF(.85f,.91f,.94f);cloud.setAlphaF(.32f);cloud.multipleParticleScaleBy(6);
            }
            mc.effectRenderer.spawnEffectParticle(EnumParticleTypes.WATER_SPLASH.getParticleID(),
                    x,surface.getY()+1,z,0,.08,0);
        }
    }
}
