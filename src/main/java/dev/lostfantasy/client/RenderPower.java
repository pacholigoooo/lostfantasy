package dev.lostfantasy.client;
import dev.lostfantasy.entity.EntityPowerOrb;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
public final class RenderPower extends Render<EntityPowerOrb> {
    public RenderPower(RenderManager manager) {super(manager);}
    @Override public void doRender(EntityPowerOrb e,double x,double y,double z,float yaw,float partial) {
        try(RenderState state=new RenderState();RenderMatrix matrix=new RenderMatrix()) {
        GlStateManager.translate(x,y+.2+Math.sin((e.ticksExisted+partial)*.1)*.05,z);GlStateManager.rotate(-renderManager.playerViewY,0,1,0);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.disableLighting();
        try(RenderState colorState=new RenderState()) {
            GlStateManager.disableTexture2D();ShaderBridge.colored();org.lwjgl.opengl.GL11.glNormal3f(0,0,1);
            NativeModels.begin();NativeModels.box(-.16,-.16,-.03,.32,.32,.06,0xffff3f65);NativeModels.end();
        }
        GlStateManager.enableTexture2D();
        GlStateManager.scale(.026,-.026,.026);getFontRendererFromRenderManager().drawString("P",-3,-4,0xffffff);
        }
    }
    @Override protected ResourceLocation getEntityTexture(EntityPowerOrb e) {return null;}
}
