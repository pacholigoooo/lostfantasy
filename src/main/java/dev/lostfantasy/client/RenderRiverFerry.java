package dev.lostfantasy.client;

import dev.lostfantasy.entity.EntityRiverFerry;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class RenderRiverFerry extends Render<EntityRiverFerry> {
    public RenderRiverFerry(RenderManager manager) {super(manager);shadowSize=0;}
    @Override public void doRender(EntityRiverFerry e,double x,double y,double z,float yaw,float partial) {
        try(RenderState state=new RenderState();RenderMatrix matrix=new RenderMatrix()) {
            GlStateManager.translate(x,y,z);GlStateManager.rotate(-yaw,0,1,0);
            GlStateManager.rotate((float)Math.sin((e.ticksExisted+partial)*.035)*.6f,0,0,1);
            GlStateManager.disableLighting();GlStateManager.disableCull();
            GlStateManager.enableBlend();GlStateManager.tryBlendFuncSeparate(770,771,1,0);
            RiverMesh.FERRY.draw(255);
            if(!ShaderBridge.shadowPass())lantern(yaw);
        }
    }
    private void lantern(float yaw) {
        ShaderBridge.emissive();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,240,240);
        GlStateManager.disableAlpha();GlStateManager.depthMask(false);
        GlStateManager.enableBlend();GlStateManager.tryBlendFuncSeparate(770,771,1,0);
        boolean offset=GL11.glIsEnabled(GL11.GL_POLYGON_OFFSET_FILL);
        float factor=GL11.glGetFloat(GL11.GL_POLYGON_OFFSET_FACTOR),units=GL11.glGetFloat(GL11.GL_POLYGON_OFFSET_UNITS);
        GlStateManager.enablePolygonOffset();GlStateManager.doPolygonOffset(-1,-1);
        try {RiverMesh.LAMP.draw(255);}
        finally {
            GlStateManager.doPolygonOffset(factor,units);
            if(!offset)GlStateManager.disablePolygonOffset();
        }
        try(RenderMatrix lantern=new RenderMatrix()) {
            GlStateManager.translate(.93,1.26,1.48);
            GlStateManager.rotate(yaw-renderManager.playerViewY,0,1,0);GlStateManager.rotate(renderManager.playerViewX,1,0,0);
            GlStateManager.shadeModel(GL11.GL_SMOOTH);GlStateManager.tryBlendFuncSeparate(770,1,1,0);
            BufferBuilder b=Tessellator.getInstance().getBuffer();
            b.begin(GL11.GL_TRIANGLES,DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            for(int i=0;i<24;i++) {
                double a=i*Math.PI/12,c=(i+1)*Math.PI/12;
                b.pos(0,0,0).tex(.5,.5).color(255,178,75,42).normal(0,0,1).endVertex();
                b.pos(Math.cos(a)*.5,Math.sin(a)*.5,0).tex(.5,.5).color(255,145,52,0).normal(0,0,1).endVertex();
                b.pos(Math.cos(c)*.5,Math.sin(c)*.5,0).tex(.5,.5).color(255,145,52,0).normal(0,0,1).endVertex();
            }
            Tessellator.getInstance().draw();
        }
    }
    @Override protected ResourceLocation getEntityTexture(EntityRiverFerry e) {return null;}
}
