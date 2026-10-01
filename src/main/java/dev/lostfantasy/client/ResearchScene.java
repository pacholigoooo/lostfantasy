package dev.lostfantasy.client;

import dev.lostfantasy.core.EmeraldCity;
import dev.lostfantasy.core.ResearchDemonstration;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/** Desk choreography and a static comparison specimen, independent of combat timing. */
final class ResearchScene {
    private ResearchScene() {}
    static void draw(double age) {
        boolean writeDepth=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        GlStateManager.tryBlendFuncSeparate(770,771,1,0);
        BufferBuilder buffer=Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_LINES,DefaultVertexFormats.POSITION_COLOR);
        for(int ring=0;ring<4;ring++) {
            double reveal=Math.max(0,Math.min(1,(age-ring*4)/8)),radius=EmeraldCity.ringRadius(ring);
            int alpha=(int)(155*reveal*Math.min(1,Math.max(0,(180-age)/12)));
            for(int i=0;i<96;i++) {
                double a=i*Math.PI/48,b=(i+1)*Math.PI/48;
                buffer.pos(Math.cos(a)*radius,.015,Math.sin(a)*radius).color(183,196,126,alpha).endVertex();
                buffer.pos(Math.cos(b)*radius,.015,Math.sin(b)*radius).color(183,196,126,alpha).endVertex();
            }
        }
        Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        try {
            buffer.begin(GL11.GL_TRIANGLES,DefaultVertexFormats.POSITION_COLOR);
            for(EmeraldCity.Column column:ResearchDemonstration.COLUMNS) {
                double height=column.height*ResearchDemonstration.fraction(age,column.round);
                if(height<=.001)continue;
                BlenderEmeraldModel.INSTANCE.append(buffer,column.height,height,255,column.x,0,column.z);
            }
            Tessellator.getInstance().draw();
        } finally {GlStateManager.depthMask(writeDepth);}
    }
    static void sampleInset(int centerX,int baseY,float size) {
        try(RenderState state=new RenderState();RenderMatrix matrix=new RenderMatrix()) {
        ShaderBridge.screen();
        GlStateManager.setActiveTexture(net.minecraft.client.renderer.OpenGlHelper.defaultTexUnit);
        GlStateManager.translate(centerX,baseY,180);
        GlStateManager.scale(size,-size,size);GlStateManager.rotate(18,1,0,0);GlStateManager.rotate(30,0,1,0);
        GlStateManager.disableTexture2D();GlStateManager.disableLighting();GlStateManager.disableCull();GlStateManager.enableBlend();
        GlStateManager.enableDepth();GlStateManager.depthMask(true);
        BlenderEmeraldModel.INSTANCE.draw(4,4,255);
        }
    }
}
