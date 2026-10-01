package dev.lostfantasy.client;

import dev.lostfantasy.Balance;
import dev.lostfantasy.world.gensokyo.GensokyoAtlas;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** A bounded cloud bank in the normal atlas particle pass; no sky replacement or GL state changes. */
final class GenunkaiClouds {
    static final ResourceLocation TEXTURE=new ResourceLocation("lostfantasy","particles/genunkai_cloud");
    private static final Map<BlockPos,Cloud> CLOUDS=new HashMap<>();
    private static World currentWorld;
    private static final int STEP=48,RADIUS=7;
    private static final int[] HEIGHTS={194,207,253};
    private GenunkaiClouds() {}

    static void clear() {
        for(Cloud cloud:CLOUDS.values())cloud.setExpired();
        CLOUDS.clear();currentWorld=null;
    }
    static void tick(Minecraft mc) {
        if(currentWorld!=mc.world) {clear();currentWorld=mc.world;}
        Entity eye=mc.getRenderViewEntity();GensokyoAtlas summit=GensokyoAtlas.MOUNTAIN_TOP;
        if(!Balance.genunkaiClouds || mc.world==null || eye==null || mc.world.provider.getDimension()!=Balance.gensokyoDimensionId
                || Math.abs(eye.posX-summit.x)>1150 || Math.abs(eye.posZ-summit.z)>1000
                || eye.posY<135 || eye.posY>310) {
            if(!CLOUDS.isEmpty())clear();return;
        }
        long now=mc.world.getTotalWorldTime();if((now&3)!=0)return;
        Iterator<Cloud> old=CLOUDS.values().iterator();
        while(old.hasNext()) {
            Cloud cloud=old.next();
            // The particle manager can evict particles during a busy scene or resource reload.
            if(!cloud.isAlive() || now-cloud.lastUpdate>20) {cloud.setExpired();old.remove();}
        }
        int limit=mc.gameSettings.particleSetting==0?144:mc.gameSettings.particleSetting==1?96:48;
        if(HiganShaderCompatibility.externalAtmosphere())limit=limit*2/3;
        double viewRadius=Math.min(STEP*RADIUS,Math.max(32,mc.gameSettings.renderDistanceChunks*16));
        int rings=Math.min(RADIUS,(int)Math.ceil(viewRadius/STEP));
        int gx=(int)Math.floor(eye.posX/STEP),gz=(int)Math.floor(eye.posZ/STEP),wanted=0,created=0;
        // Visit the closest cells first. The minimum particle setting still leaves a sparse cloud bank.
        for(int ring=0;ring<=rings;ring++)for(int dx=-ring;dx<=ring;dx++)for(int dz=-ring;dz<=ring;dz++) {
            if(Math.max(Math.abs(dx),Math.abs(dz))!=ring)continue;
            for(int layer=0;layer<HEIGHTS.length;layer++) {
                int x=gx+dx,z=gz+dz;long hash=mix(x,z,layer);
                // Alternate the two low banks instead of stacking both in every cell.
                if(layer<2 && layer!=Math.floorMod(mix(x,z,0),2))continue;
                double wx=x*STEP+Math.floorMod(hash,29),wz=z*STEP+Math.floorMod(hash>>>8,29);
                double radius=Math.hypot((wx-summit.x)/820.0,(wz-summit.z)/680.0);
                double distance=Math.hypot(wx-eye.posX,wz-eye.posZ);
                if(radius>=1 || distance>=viewRadius)continue;
                // Small gaps expose the rocky crest and let visitors see the moving banks from above.
                if(Math.hypot(wx-summit.x,wz-summit.z)<48 || Math.floorMod(hash>>>16,13)==0)continue;
                if(wanted>=limit)continue;
                BlockPos key=new BlockPos(x,layer,z);Cloud cloud=CLOUDS.get(key);
                if(cloud==null && created<12 && CLOUDS.size()<limit+24) {
                    double y=HEIGHTS[layer]+Math.floorMod(hash>>>20,7);
                    float alpha=(float)((layer==2?.66:.58)*Math.min(1,(1-radius)/.18));
                    cloud=new Cloud(mc.world,wx,y,wz,hash,alpha,layer==2,mc);CLOUDS.put(key,cloud);
                    mc.effectRenderer.addEffect(cloud);created++;
                }
                if(cloud!=null) {
                    cloud.wanted=now;
                    cloud.distanceOpacity=(float)Math.min(1,(viewRadius-distance)/(viewRadius*.24));
                    wanted++;
                }
            }
        }
    }
    private static long mix(int x,int z,int layer) {
        long h=x*0x632be59bd9b4e019L ^ z*0x94d049bb133111ebL ^ layer*0x9e3779b97f4a7c15L;
        h=(h^(h>>>30))*0xbf58476d1ce4e5b9L;return h^(h>>>27);
    }
    private static final class Cloud extends Particle {
        final double anchorX,anchorY,anchorZ,phase;final float opacity;
        long wanted,lastUpdate;float fade,distanceOpacity;
        Cloud(World world,double x,double y,double z,long hash,float opacity,boolean overhead,Minecraft mc) {
            super(world,x,y,z);anchorX=x;anchorY=y;anchorZ=z;this.opacity=opacity;
            phase=Math.floorMod(hash>>>28,628)/100.0;wanted=lastUpdate=world.getTotalWorldTime();
            particleScale=(overhead?320:240)+Math.floorMod(hash>>>36,80);canCollide=false;particleAlpha=0;
            particleRed=overhead?.44f:.62f;particleGreen=overhead?.48f:.65f;particleBlue=overhead?.54f:.70f;
            setParticleTexture(mc.getTextureMapBlocks().getAtlasSprite(TEXTURE.toString()));
        }
        @Override public int getFXLayer() {return 1;}
        @Override public int getBrightnessForRender(float partial) {return 0xf00000;}
        @Override public void onUpdate() {
            prevPosX=posX;prevPosY=posY;prevPosZ=posZ;
            long now=world.getTotalWorldTime();lastUpdate=now;
            fade=Math.max(0,Math.min(1,fade+(now-wanted<12?.035f:-.025f)));
            if(fade==0 && now-wanted>=12) {setExpired();return;}
            particleAlpha=opacity*fade*distanceOpacity;
            double t=now*.0015+phase;
            setPosition(anchorX+Math.sin(t)*9,anchorY+Math.sin(t*.7)*1.1,anchorZ+Math.cos(t)*7);
        }
        @Override public void renderParticle(BufferBuilder buffer,Entity eye,float partial,
                float rotationX,float rotationZ,float rotationYZ,float rotationXY,float rotationXZ) {
            // Flatten the vertical billboard axis into rolling layers without changing the render pass.
            super.renderParticle(buffer,eye,partial,rotationX,rotationZ*.24f,rotationYZ,rotationXY*.24f,rotationXZ*.24f);
        }
    }
}
