package dev.lostfantasy.client;

import dev.lostfantasy.entity.EntityHisoutensoku;
import dev.lostfantasy.entity.HisoutensokuShape;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/** Opaque textured geometry on the normal entity shader pass, with ordinary world lighting. */
public final class RenderHisoutensoku extends Render<EntityHisoutensoku> {
    private static final ResourceLocation TEXTURE=new ResourceLocation("lostfantasy:textures/entity/hisoutensoku.png");
    private final Balloon model=new Balloon();
    public RenderHisoutensoku(RenderManager manager) {super(manager);shadowSize=0;}
    @Override protected ResourceLocation getEntityTexture(EntityHisoutensoku e) {return TEXTURE;}
    @Override public void doRender(EntityHisoutensoku e,double x,double y,double z,float yaw,float partial) {
        if(e.isInvisible())return;
        try(RenderState state=new RenderState();RenderMatrix matrix=new RenderMatrix()) {
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);GlStateManager.enableTexture2D();bindEntityTexture(e);
            GlStateManager.enableLighting();GlStateManager.disableBlend();GlStateManager.enableCull();GlStateManager.color(1,1,1,1);
            GlStateManager.translate(x,y,z);model.render(HisoutensokuShape.armAngle(e.world.getTotalWorldTime(),partial));
        }
    }
    private static final class Balloon extends ModelBase {
        private final ModelRenderer[] joints=new ModelRenderer[3];
        Balloon() {
            textureWidth=256;textureHeight=256;
            for(int i=0;i<3;i++)joints[i]=new ModelRenderer(this);
            joints[1].setRotationPoint(-HisoutensokuShape.ARM_X,HisoutensokuShape.ARM_Y,0);
            joints[2].setRotationPoint(HisoutensokuShape.ARM_X,HisoutensokuShape.ARM_Y,0);
            for(HisoutensokuShape.Part p:HisoutensokuShape.PARTS) {
                ModelRenderer part=new ModelRenderer(this,(p.material%2)*128+4,(p.material/2)*64+4);
                part.addBox(p.x,p.y,p.z,p.w,p.h,p.d);joints[p.joint].addChild(part);
            }
        }
        void render(float arm) {joints[1].rotateAngleZ=-arm;joints[2].rotateAngleZ=arm;for(ModelRenderer joint:joints)joint.render(1);}
    }
}
