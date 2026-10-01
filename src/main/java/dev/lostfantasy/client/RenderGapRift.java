package dev.lostfantasy.client;
import dev.lostfantasy.entity.EntityGapRift;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
public final class RenderGapRift extends Render<EntityGapRift> {
    public RenderGapRift(RenderManager manager) {super(manager);}
    @Override public void doRender(EntityGapRift e,double x,double y,double z,float yaw,float partial) {
        if(ShaderBridge.shadowPass())return;
        try(RenderState state=new RenderState();RenderMatrix matrix=new RenderMatrix()) {
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.disableTexture2D();GlStateManager.disableLighting();GlStateManager.disableCull();GlStateManager.enableBlend();GlStateManager.depthMask(true);
            ShaderBridge.colored();org.lwjgl.opengl.GL11.glNormal3f(0,1,0);
            GlStateManager.tryBlendFuncSeparate(770,771,1,0);
            SpellVisuals.drawGapRift(new Vec3d(x,y,z),new Vec3d(-Math.sin(Math.toRadians(e.rotationYaw)),0,Math.cos(Math.toRadians(e.rotationYaw))),Math.min(1,(e.ticksExisted+partial)/8));
        }
    }
    @Override protected ResourceLocation getEntityTexture(EntityGapRift e) {return null;}
}
