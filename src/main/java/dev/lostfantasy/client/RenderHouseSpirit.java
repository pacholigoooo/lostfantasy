package dev.lostfantasy.client;

import dev.lostfantasy.entity.EntityHouseSpirit;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/** Ordinary textured entity geometry, lit by the world and the selected shader program. */
public final class RenderHouseSpirit extends Render<EntityHouseSpirit> {
    private static final ResourceLocation TEXTURE=new ResourceLocation("lostfantasy:textures/entity/house_spirit.png");
    private final Spirit model=new Spirit();
    public RenderHouseSpirit(RenderManager manager) {super(manager);shadowSize=0;}
    @Override protected ResourceLocation getEntityTexture(EntityHouseSpirit entity) {return TEXTURE;}
    @Override public void doRender(EntityHouseSpirit e,double x,double y,double z,float yaw,float partial) {
        if(e.isInvisible())return;
        try(RenderState state=new RenderState();RenderMatrix matrix=new RenderMatrix()) {
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);GlStateManager.enableTexture2D();bindEntityTexture(e);
            GlStateManager.enableLighting();GlStateManager.enableBlend();GlStateManager.tryBlendFuncSeparate(770,771,1,0);
            GlStateManager.alphaFunc(516,.05f);GlStateManager.color(1,1,1,.82f);GlStateManager.disableCull();
            GlStateManager.translate(x,y+.43,z);GlStateManager.rotate(-renderManager.playerViewY,0,1,0);GlStateManager.scale(1,-1,-1);
            model.render((e.ticksExisted+partial)*.045f);
        }
    }
    private static final class Spirit extends ModelBase {
        private final ModelRenderer crown,body,belly,tail,tip;
        Spirit() {
            textureWidth=64;textureHeight=64;
            crown=part(0,0,-4,-15,-3,8,3,6);
            body=part(0,10,-6,-12,-4,12,7,8);
            belly=part(0,26,-4,-5,-3,8,4,6);
            tail=part(32,26,-2,0,-2,4,4,4);tail.setRotationPoint(0,-1,0);
            tip=part(48,26,-1,0,-1,2,4,2);tip.setRotationPoint(-1,2,0);
        }
        private ModelRenderer part(int u,int v,float x,float y,float z,int w,int h,int d) {
            ModelRenderer p=new ModelRenderer(this,u,v);p.addBox(x,y,z,w,h,d);return p;
        }
        void render(float time) {
            tail.rotateAngleZ=.26f+(float)Math.sin(time)*.08f;tip.rotateAngleZ=.55f+(float)Math.sin(time+.6f)*.14f;
            crown.render(.0625f);body.render(.0625f);belly.render(.0625f);tail.render(.0625f);tip.render(.0625f);
        }
    }
}
