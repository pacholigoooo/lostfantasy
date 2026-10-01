package dev.lostfantasy.client;

import dev.lostfantasy.entity.EntityCablecar;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;

public final class RenderCablecar extends Render<EntityCablecar> {
    private static final ResourceLocation TEXTURE=new ResourceLocation("lostfantasy:textures/blocks/gensokyo_craft.png");
    public RenderCablecar(RenderManager manager) {super(manager);shadowSize=0;}
    @Override protected ResourceLocation getEntityTexture(EntityCablecar e) {return TEXTURE;}
    @Override public void doRender(EntityCablecar e,double x,double y,double z,float yaw,float partial) {
        try(RenderState state=new RenderState();RenderMatrix matrix=new RenderMatrix()) {
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);GlStateManager.enableTexture2D();bindEntityTexture(e);
            GlStateManager.enableLighting();GlStateManager.disableBlend();GlStateManager.color(1,1,1,1);GlStateManager.enableCull();
            GlStateManager.translate(x,y,z);GlStateManager.rotate(-yaw,0,1,0);TexturedMesh.CABLECAR.draw(1);
        }
    }
}
