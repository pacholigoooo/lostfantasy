package dev.lostfantasy.client;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Coloured Blender triangles, shared by the spell, comparison cards and desk demonstration. */
public final class BlenderEmeraldModel implements IResourceManagerReloadListener {
    public static final BlenderEmeraldModel INSTANCE=new BlenderEmeraldModel();
    private float[] positions;
    private int[] colors;
    private boolean failed;
    private final double[][] triangle=new double[3][6],clipped=new double[4][6];
    private BlenderEmeraldModel() {}
    @Override public void onResourceManagerReload(IResourceManager manager) { positions=null;colors=null;failed=false;load(manager); }
    public void draw(double height,double visible,int alpha) {
        BufferBuilder buffer=Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_TRIANGLES,DefaultVertexFormats.POSITION_COLOR);
        append(buffer,height,visible,alpha,0,0,0);
        Tessellator.getInstance().draw();
    }
    /** Clip in model space; shader packs need not implement the fixed-function clip plane. */
    void append(BufferBuilder buffer,double height,double visible,int alpha,double x,double y,double z) {
        if(failed || positions==null || visible<=0 || height<=0)return;
        double tip=Math.min(.8,height),shoulder=height-tip,offset=visible-height;
        for(int first=0;first<colors.length;first+=3) {
            for(int j=0;j<3;j++) {
                int i=first+j,c=colors[i];double py=positions[i*3+1];
                double[] v=triangle[j];v[0]=positions[i*3];v[2]=positions[i*3+2];
                v[1]=(py<=3.2?py/3.2*shoulder:shoulder+(py-3.2)/.8*tip)+offset;
                v[3]=c>>>24&255;v[4]=c>>>16&255;v[5]=c>>>8&255;
            }
            int count=0;double[] previous=triangle[2];boolean wasInside=previous[1]>=0;
            for(double[] next:triangle) {
                boolean inside=next[1]>=0;
                if(inside!=wasInside) {
                    double t=-previous[1]/(next[1]-previous[1]);double[] v=clipped[count++];
                    for(int k=0;k<6;k++)v[k]=previous[k]+(next[k]-previous[k])*t;
                    v[1]=0;
                }
                if(inside)System.arraycopy(next,0,clipped[count++],0,6);
                previous=next;wasInside=inside;
            }
            for(int j=1;j<count-1;j++) {
                vertex(buffer,clipped[0],alpha,x,y,z);vertex(buffer,clipped[j],alpha,x,y,z);vertex(buffer,clipped[j+1],alpha,x,y,z);
            }
        }
    }
    private static void vertex(BufferBuilder buffer,double[] v,int alpha,double x,double y,double z) {
        buffer.pos(x+v[0],y+v[1],z+v[2]).color((int)Math.round(v[3]),(int)Math.round(v[4]),(int)Math.round(v[5]),alpha).endVertex();
    }
    private void load(IResourceManager manager) {
        try(IResource resource=manager.getResource(new ResourceLocation("lostfantasy:meshes/emerald_city.lfm"));
            DataInputStream input=new DataInputStream(new BufferedInputStream(resource.getInputStream()))) {
            if(input.readInt()!=0x4c464d31)throw new IOException("Bad emerald mesh header");
            int count=input.readInt();if(count<=0 || count%3!=0 || count>18000)throw new IOException("Invalid emerald vertex count");
            float[] loaded=new float[count*3];int[] shades=new int[count];
            for(int i=0;i<count;i++) {
                for(int axis=0;axis<3;axis++) {
                    float v=input.readFloat();if(!Float.isFinite(v))throw new IOException("Nonfinite emerald vertex");
                    loaded[i*3+axis]=v;
                }
                shades[i]=input.readInt();
            }
            if(input.read()!=-1)throw new IOException("Trailing emerald mesh data");
            positions=loaded;colors=shades;
        } catch(IOException ex) { failed=true;org.apache.logging.log4j.LogManager.getLogger("LostFantasy").error("Unable to load Blender emerald mesh",ex); }
    }
}
