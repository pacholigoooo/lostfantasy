package dev.lostfantasy.client;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.world.HiganWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/** Surface ribbons and petals remain visible with the vanilla particle setting at minimum. */
public final class HiganAtmosphere {
    private static final it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap SURFACES=new it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap(4096);
    private static final BlockPos.MutableBlockPos SURFACE_POS=new BlockPos.MutableBlockPos();
    private static net.minecraft.world.World surfaceWorld;
    private static long surfaceTick;
    @SubscribeEvent public void unload(net.minecraftforge.event.world.WorldEvent.Unload event) {
        if(event.getWorld().isRemote) {
            HiganWaterSurface.INSTANCE.unload(event.getWorld());
            if(surfaceWorld==event.getWorld()) {surfaceWorld=null;SURFACES.clear();}
        }
    }
    @SubscribeEvent public void fog(EntityViewRenderEvent.FogDensity event) {
        if(!HiganWorld.inside(event.getEntity()) || HiganShaderCompatibility.externalAtmosphere())return;
        GlStateManager.setFog(GlStateManager.FogMode.EXP2);event.setDensity(.0105f);event.setCanceled(true);
    }
    @SubscribeEvent public void color(EntityViewRenderEvent.FogColors event) {
        if(!HiganWorld.inside(event.getEntity()) || HiganShaderCompatibility.externalAtmosphere())return;
        event.setRed(.29f);event.setGreen(.39f);event.setBlue(.38f);
    }
    public static void render(float partial) {
        Minecraft mc=Minecraft.getMinecraft();if(!HiganWorld.inside(mc.player) || HiganShaderCompatibility.externalAtmosphere())return;
        net.minecraft.entity.Entity eye=mc.getRenderViewEntity();if(eye==null)return;
        double ex=eye.lastTickPosX+(eye.posX-eye.lastTickPosX)*partial,ey=eye.lastTickPosY+(eye.posY-eye.lastTickPosY)*partial,ez=eye.lastTickPosZ+(eye.posZ-eye.lastTickPosZ)*partial;
        if(!HiganSurfaceDetails.nearRiver(ex,ez,HiganSurfaceDetails.RADIUS))return;
        if(OpenGlHelper.shadersSupported && GL11.glGetInteger(org.lwjgl.opengl.GL20.GL_CURRENT_PROGRAM)!=0)return;
        double time=mc.world.getTotalWorldTime()+partial;
        try(RenderState state=new RenderState();RenderMatrix matrix=new RenderMatrix()) {GlStateManager.translate(-ex,-ey,-ez);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);GlStateManager.disableTexture2D();GlStateManager.disableLighting();
        GlStateManager.enableBlend();GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA,1,0);GlStateManager.disableCull();GlStateManager.disableAlpha();GlStateManager.depthMask(false);
        boolean shaded=HiganWaterSurface.INSTANCE.draw(ex,ey,ez,partial);
        BufferBuilder b=Tessellator.getInstance().getBuffer();b.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_COLOR);
        HiganSurfaceDetails.emit(ex,ez,time,shaded,(x,z)->surface(mc,x,z),
                (x0,z0,x1,z1,x2,z2,x3,z3,y,r,g,blue,alpha)->quad(b,x0,y,z0,x1,y,z1,x2,y,z2,x3,y,z3,r,g,blue,alpha));
        Tessellator.getInstance().draw();
        }
    }
    private static boolean surface(Minecraft mc,int x,int z) {
        long now=mc.world.getTotalWorldTime();
        if(surfaceWorld!=mc.world || surfaceTick!=now || SURFACES.size()>=8192) {
            surfaceWorld=mc.world;surfaceTick=now;SURFACES.clear();
        }
        long key=((long)x<<32)^(z&0xffffffffL);byte known=SURFACES.get(key);
        if(known!=0)return known==1;
        SURFACE_POS.setPos(x,dev.lostfantasy.world.HiganTerrain.WATER,z);
        boolean water=mc.world.isBlockLoaded(SURFACE_POS) && mc.world.getBlockState(SURFACE_POS).getBlock()==ModBlocks.SANZU_WATER;
        if(water) {
            SURFACE_POS.setY(dev.lostfantasy.world.HiganTerrain.WATER+1);
            water=mc.world.isAirBlock(SURFACE_POS);
        }
        SURFACES.put(key,(byte)(water?1:2));return water;
    }
    private static void quad(BufferBuilder b,double x0,double y0,double z0,double x1,double y1,double z1,double x2,double y2,double z2,double x3,double y3,double z3,int r,int g,int blue,int a) {
        b.pos(x0,y0,z0).color(r,g,blue,a).endVertex();b.pos(x1,y1,z1).color(r,g,blue,a).endVertex();b.pos(x2,y2,z2).color(r,g,blue,a).endVertex();b.pos(x3,y3,z3).color(r,g,blue,a).endVertex();
    }
}
