package dev.lostfantasy.client;

import dev.lostfantasy.Balance;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import java.util.Random;

final class GapAtmosphere {
    private static final Random RANDOM=new Random();
    private GapAtmosphere() {}
    static void tick(Minecraft mc) {
        if(mc.world==null || mc.world.provider.getDimension()!=Balance.dimensionId || mc.gameSettings.particleSetting>=2)return;
        Entity viewer=mc.getRenderViewEntity();if(viewer==null)return;
        boolean reduced=mc.gameSettings.particleSetting==1;
        if(mc.world.getTotalWorldTime()%(reduced?4:2)!=0)return;
        for(int i=0;i<(reduced?1:3);i++) {
            double angle=RANDOM.nextDouble()*Math.PI*2,distance=3+RANDOM.nextDouble()*13;
            double x=viewer.posX+Math.cos(angle)*distance,y=viewer.posY-3+RANDOM.nextDouble()*10,z=viewer.posZ+Math.sin(angle)*distance;
            mc.effectRenderer.addEffect(new SpellParticle(mc.world,x,y,z,Math.sin(angle)*.008,.012,-Math.cos(angle)*.008,i%2==0?0xb998ee:0xe4d3ff,.55f+RANDOM.nextFloat()*.35f,40+RANDOM.nextInt(20)));
        }
    }
}
