package dev.lostfantasy.client;

import dev.lostfantasy.core.EmeraldCity;
import dev.lostfantasy.network.EffectMessage;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/** Blender-built crystal emerging through the ground plane. */
final class EmeraldVisual {
    private EmeraldVisual() {}
    static void draw(EffectMessage effect,double age) {
        GlStateManager.tryBlendFuncSeparate(770,771,1,0);
        BufferBuilder b=Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_COLOR);
        for(EmeraldCity.Column column:effect.columns) {
            double local=age-column.start();
            if(local<0 && local>=-8)warning(b,column,1+local/8);
        }
        Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        try {
            b.begin(GL11.GL_TRIANGLES,DefaultVertexFormats.POSITION_COLOR);
            for(EmeraldCity.Column column:effect.columns) {
                double local=age-column.start(),height=column.heightAt(age);
                if(local<0 || height<=0)continue;
                int alpha=(int)(255*Math.min(1,(EmeraldCity.RISE+EmeraldCity.HOLD+EmeraldCity.SINK-local)/3));
                BlenderEmeraldModel.INSTANCE.append(b,column.height,height,Math.max(0,alpha),column.x,column.y,column.z);
            }
            Tessellator.getInstance().draw();
        } finally {GlStateManager.depthMask(false);}
    }
    private static void warning(BufferBuilder b,EmeraldCity.Column column,double progress) {
        int alpha=(int)(40+100*progress);
        for(int i=0;i<8;i++) {
            double a=i*Math.PI/4,c=(i+1)*Math.PI/4;
            vertex(b,column,a,.72,.015,140,230,173,alpha);vertex(b,column,c,.72,.015,140,230,173,alpha);
            vertex(b,column,c,.68,.015,140,230,173,alpha);vertex(b,column,a,.68,.015,140,230,173,alpha);
        }
    }
    private static void vertex(BufferBuilder b,EmeraldCity.Column c,double angle,double radius,double y,int r,int g,int blue,int alpha) {
        b.pos(c.x+Math.cos(angle)*radius,c.y+y,c.z+Math.sin(angle)*radius).color(r,g,blue,Math.max(0,alpha)).endVertex();
    }
}
