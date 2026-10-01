package dev.lostfantasy.client;

import dev.lostfantasy.core.RibbonFortune;
import java.nio.FloatBuffer;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

public final class RibbonItemRenderer extends TileEntityItemStackRenderer {
    public static final RibbonItemRenderer INSTANCE=new RibbonItemRenderer();
    /** Actual wrapped centre of the existing Blender mesh, in its exported coordinates. */
    static final Vec3d KNOT=new Vec3d(.50,.62,.46);
    private static final net.minecraft.util.ResourceLocation GLOW=new net.minecraft.util.ResourceLocation("lostfantasy","textures/misc/ribbon_glow.png");
    private static final FloatBuffer MATRIX=BufferUtils.createFloatBuffer(16);
    private static final Map<UUID,Vec3d[]> bodies=new HashMap<>();
    RibbonItemModel context;
    public static void beginFrame(){bodies.clear();INSTANCE.context=null;}
    RibbonItemModel takeContext(){RibbonItemModel model=context;context=null;return model;}
    public static void beforePlayer(RenderPlayerEvent.Pre event) {
        if(!FortuneVisual.isPlaying(event.getEntityPlayer().getUniqueID()))return;
        Matrix4f matrix=matrix();Vec3d[] points=new Vec3d[14];
        for(int i=0;i<points.length;i++) {
            double a=i*Math.PI*2/points.length;
            points[i]=transform(matrix,new Vec3d(event.getX()+Math.cos(a)*.38,event.getY()+1.1+Math.sin(a*2)*.38,event.getZ()+Math.sin(a)*.28));
        }
        bodies.put(event.getEntityPlayer().getUniqueID(),points);
    }
    static Matrix4f matrix(){MATRIX.clear();GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,MATRIX);Matrix4f result=new Matrix4f();result.load(MATRIX);return result;}
    static Vec3d transform(Matrix4f matrix,Vec3d p) {
        Vector4f v=Matrix4f.transform(matrix,new Vector4f((float)p.x,(float)p.y,(float)p.z,1),null);
        return new Vec3d(v.x/v.w,v.y/v.w,v.z/v.w);
    }
    @Override public void renderByItem(ItemStack stack) {
        RibbonItemModel model=takeContext();if(model==null)return;
        try (RenderState state=new RenderState()) {
        Minecraft mc=Minecraft.getMinecraft();float partial=mc.getRenderPartialTicks();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        BufferBuilder b=Tessellator.getInstance().getBuffer();b.begin(GL11.GL_QUADS,DefaultVertexFormats.ITEM);
        mc.getRenderItem().renderQuads(b,model.mesh.getQuads(null,null,0),-1,stack);Tessellator.getInstance().draw();
        double now=mc.world==null?0:mc.world.getTotalWorldTime()+partial;
        float glow=RibbonFortune.glow(stack,mc.world==null?Integer.MIN_VALUE:mc.world.provider.getDimension(),now);
        boolean held=model.transform==ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND || model.transform==ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
                || model.transform==ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND || model.transform==ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
        boolean drawing=held && model.holder!=null && FortuneVisual.matches(stack,model.holder.getUniqueID());
        if((glow<=0 && !drawing) || ShaderBridge.shadowPass())return;
        Matrix4f inverse=Matrix4f.invert(matrix(),null);if(inverse==null)return;
        Vec3d eye=transform(inverse,Vec3d.ZERO);
        GlStateManager.disableLighting();GlStateManager.enableTexture2D();GlStateManager.enableBlend();GlStateManager.disableAlpha();GlStateManager.depthMask(false);
        mc.getTextureManager().bindTexture(ShaderBridge.WHITE);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,240,240);
            if(drawing) {
                boolean first=model.transform==ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND || model.transform==ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND;
                Vec3d[] source=first?firstPersonSources(inverse):bodySources(model.holder.getUniqueID(),inverse);
                if(source!=null) {
                    GlStateManager.disableCull();GlStateManager.tryBlendFuncSeparate(770,771,1,0);
                    FortuneVisual.drawAttached(stack,model.holder.getUniqueID(),source,eye,now);
                }
            }
            if(glow>0) {
                mc.getTextureManager().bindTexture(GLOW);
                ShaderBridge.glint();
                GlStateManager.disableCull();GlStateManager.tryBlendFuncSeparate(1,1,1,0);
                model.geometry.glow(eye,now,glow);
            }
        }
    }
    private static Vec3d[] firstPersonSources(Matrix4f inverse) {
        Vec3d[] points=new Vec3d[14];
        for(int i=0;i<points.length;i++) {
            double a=i*Math.PI*2/points.length;
            points[i]=transform(inverse,new Vec3d(Math.cos(a)*.18,-.28+Math.sin(a*2)*.13,-.72+Math.sin(a)*.10));
        }
        return points;
    }
    private static Vec3d[] bodySources(UUID player,Matrix4f inverse) {
        Vec3d[] body=bodies.get(player);if(body==null)return null;
        Vec3d[] points=new Vec3d[body.length];for(int i=0;i<body.length;i++)points[i]=transform(inverse,body[i]);return points;
    }
    static final class Geometry {
        private final float[][] vertices;
        Geometry(MeshItemModel model) {
            this(model.getQuads(null,null,0));
        }
        Geometry(List<BakedQuad> quads) {
            vertices=new float[quads.size()*4][6];
            // OptiFine expands packed quad strides. Read the source attributes through Forge's
            // consumer API so the glow never interprets shader padding as positions or normals.
            final VertexFormat format=new VertexFormat().addElement(DefaultVertexFormats.POSITION_3F).addElement(DefaultVertexFormats.NORMAL_3B).addElement(DefaultVertexFormats.PADDING_1B);
            IVertexConsumer consumer=new IVertexConsumer() {
                private int vertex;
                public VertexFormat getVertexFormat(){return format;}
                public void setQuadTint(int tint){}
                public void setQuadOrientation(EnumFacing facing){}
                public void setApplyDiffuseLighting(boolean diffuse){}
                public void setTexture(TextureAtlasSprite sprite){}
                public void put(int element,float... data) {
                    if(element<2 && data.length>=3)System.arraycopy(data,0,vertices[vertex],element==0?0:3,3);
                    if(element==format.getElementCount()-1)vertex++;
                }
            };
            for(BakedQuad q:quads)q.pipe(consumer);
        }
        void glow(Vec3d eye,double time,float strength) {
            boolean offset=GL11.glIsEnabled(GL11.GL_POLYGON_OFFSET_FILL);
            float factor=GL11.glGetFloat(GL11.GL_POLYGON_OFFSET_FACTOR),units=GL11.glGetFloat(GL11.GL_POLYGON_OFFSET_UNITS);
            GlStateManager.enablePolygonOffset();GlStateManager.doPolygonOffset(-1,-1);
            try {
            BufferBuilder b=Tessellator.getInstance().getBuffer();b.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            for(float[] v:vertices) {
                double dx=eye.x-v[0],dy=eye.y-v[1],dz=eye.z-v[2],length=Math.sqrt(dx*dx+dy*dy+dz*dz);
                double facing=length<1e-6?1:Math.abs((dx*v[3]+dy*v[4]+dz*v[5])/length);
                // Put the gradient in UVs: several packs declare glint vertex colours flat.
                // The texture contains premultiplied light, with opaque alpha for both pipelines.
                int light=(int)(strength*255);
                b.pos(v[0],v[1],v[2]).tex(Math.max(.002,Math.min(.998,facing)),v[1]*.9+v[0]*.32-time*.009)
                        .color(light,light,light,255).normal(v[3],v[4],v[5]).endVertex();
            }
            Tessellator.getInstance().draw();
            } finally {
                GlStateManager.doPolygonOffset(factor,units);if(!offset)GlStateManager.disablePolygonOffset();
            }
        }
    }
}
