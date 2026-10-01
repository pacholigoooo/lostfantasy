package dev.lostfantasy.client;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
public final class UiDraw extends Gui {
    private static final double[][] HEX={{-.52,-1},{.52,-1},{1,0},{.52,1},{-.52,1},{-1,0}};
    private UiDraw() {}
    /** Vanilla container-style grey panel and recessed slot framing. */
    public static void panel(int x,int y,int w,int h) {
        drawRect(x,y,x+w,y+h,0xff373737);
        drawRect(x+1,y+1,x+w-1,y+h-1,0xff555555);
        drawRect(x+1,y+1,x+w-2,y+3,0xffffffff);drawRect(x+1,y+1,x+3,y+h-2,0xffffffff);
        drawRect(x+3,y+3,x+w-3,y+h-3,0xffc6c6c6);
    }
    public static void inset(int x,int y,int w,int h) {
        drawRect(x,y,x+w,y+h,0xffffffff);drawRect(x,y,x+w-1,y+h-1,0xff373737);
        drawRect(x+1,y+1,x+w-1,y+h-1,0xff8b8b8b);
    }
    private static void hex(BufferBuilder b,double x,double y,double size,int color) {
        for(int i=0;i<6;i++) {
            double[] a=HEX[i],next=HEX[(i+1)%6];
            vertex(b,x,y,color);vertex(b,x+a[0]*size,y+a[1]*size,color);vertex(b,x+next[0]*size,y+next[1]*size,color);
        }
    }
    private static void vertex(BufferBuilder b,double x,double y,int c) {b.pos(x,y,0).color((c>>16)&255,(c>>8)&255,c&255,(c>>>24)&255).endVertex();}
    public static void spiritBar(double x,double y,float spirit,int capacity) {
        try(RenderState state=new RenderState()) {
            ShaderBridge.screen();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.disableTexture2D();GlStateManager.enableBlend();GlStateManager.tryBlendFuncSeparate(770,771,1,0);
            BufferBuilder b=Tessellator.getInstance().getBuffer();b.begin(GL11.GL_TRIANGLES,DefaultVertexFormats.POSITION_COLOR);
            appendSpiritBar(b,x,y,spirit,capacity);
            Tessellator.getInstance().draw();
        }
    }
    static void appendSpiritBar(BufferBuilder b,double x,double y,float spirit,int capacity) {
        for(int i=0;i<6;i++)spirit(b,x+i*15,y,Math.max(0,Math.min(1,spirit-i)),i<capacity);
    }
    private static void spirit(BufferBuilder b,double x,double y,float fill,boolean unlocked) {
        hex(b,x,y,7.5,0xff2e3040);hex(b,x,y,6.5,unlocked?0xffe9e4cf:0xff5b5763);hex(b,x,y,5.2,0xff171c37);
        if(unlocked&&fill>0) {
            for(int row=0;row<12;row++) {
                double yy=5-row*.85;if((row+1)/12f>fill)continue;
                double half=5.0*(1-.48*Math.abs(yy)/5);
                int color=0xff000000 | ((int)(225-row*15)<<8) | 255;
                vertex(b,x-half,y+yy,color);vertex(b,x+half,y+yy,color);vertex(b,x+half,y+yy-.85,color);
                vertex(b,x-half,y+yy,color);vertex(b,x+half,y+yy-.85,color);vertex(b,x-half,y+yy-.85,color);
            }
        }
    }
    public static void spellWheel(double cx,double cy,int hovered) {
        try(RenderState state=new RenderState()) {
            ShaderBridge.screen();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.disableTexture2D();GlStateManager.enableBlend();GlStateManager.tryBlendFuncSeparate(770,771,1,0);
            BufferBuilder b=Tessellator.getInstance().getBuffer();b.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_COLOR);
            for(int i=0;i<4;i++)sector(b,cx,cy,i*Math.PI/2-Math.PI/4+.025,i*Math.PI/2+Math.PI/4-.025,33,104,i==hovered?0xcf705a9c:0xb51b162b);
            Tessellator.getInstance().draw();
        }
    }
    private static void sector(BufferBuilder b,double cx,double cy,double from,double to,double inner,double outer,int color) {
        for(int j=0;j<24;j++) {
            double a=from+(to-from)*j/24,d=from+(to-from)*(j+1)/24;
            vertex(b,cx+Math.cos(a)*inner,cy+Math.sin(a)*inner,color);vertex(b,cx+Math.cos(a)*outer,cy+Math.sin(a)*outer,color);
            vertex(b,cx+Math.cos(d)*outer,cy+Math.sin(d)*outer,color);vertex(b,cx+Math.cos(d)*inner,cy+Math.sin(d)*inner,color);
        }
    }
}
