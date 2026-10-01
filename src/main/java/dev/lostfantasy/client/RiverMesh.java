package dev.lostfantasy.client;

import java.io.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

final class RiverMesh implements IResourceManagerReloadListener {
    static final RiverMesh FERRY=new RiverMesh("river_ferry");
    static final RiverMesh LAMP=new RiverMesh("river_ferry_lamp");
    private final String name;private float[] vertices,normals;private int[] colors;private boolean failed;
    RiverMesh(String name) {this.name=name;}
    @Override public void onResourceManagerReload(IResourceManager manager) {vertices=null;normals=null;colors=null;failed=false;load(manager);}
    void draw(int alpha) {
        if(failed || vertices==null)return;
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();Minecraft.getMinecraft().getTextureManager().bindTexture(ShaderBridge.WHITE);
        BufferBuilder b=Tessellator.getInstance().getBuffer();b.begin(GL11.GL_TRIANGLES,DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        for(int i=0;i<colors.length;i++) {int c=colors[i],n=(i/3)*3;b.pos(vertices[i*3],vertices[i*3+1],vertices[i*3+2]).tex(.5,.5)
                .color(c>>>24&255,c>>>16&255,c>>>8&255,alpha).normal(normals[n],normals[n+1],normals[n+2]).endVertex();}
        Tessellator.getInstance().draw();
    }
    private void load(IResourceManager manager) {
        try(IResource r=manager.getResource(new ResourceLocation("lostfantasy:meshes/"+name+".lfm"));DataInputStream in=new DataInputStream(new BufferedInputStream(r.getInputStream()))) {
            if(in.readInt()!=0x4c464d31)throw new IOException("Bad river mesh header");int count=in.readInt();
            if(count<=0 || count%3!=0 || count>60000)throw new IOException("Invalid river mesh size");
            float[] v=new float[count*3];int[] c=new int[count];
            for(int i=0;i<count;i++) {for(int axis=0;axis<3;axis++) {v[i*3+axis]=in.readFloat();if(!Float.isFinite(v[i*3+axis]))throw new IOException("Nonfinite river vertex");}c[i]=in.readInt();}
            if(in.read()!=-1)throw new IOException("Trailing river data");
            vertices=v;colors=c;normals=faceNormals(v);
        } catch(IOException ex) {failed=true;org.apache.logging.log4j.LogManager.getLogger("LostFantasy").error("Cannot load river mesh "+name,ex);}
    }
    static float[] faceNormals(float[] vertices) {
        float[] result=new float[vertices.length/3];
        for(int k=0;k<vertices.length;k+=9) {
            double ux=(double)vertices[k+3]-vertices[k],uy=(double)vertices[k+4]-vertices[k+1],uz=(double)vertices[k+5]-vertices[k+2];
            double vx=(double)vertices[k+6]-vertices[k],vy=(double)vertices[k+7]-vertices[k+1],vz=(double)vertices[k+8]-vertices[k+2];
            double nx=uy*vz-uz*vy,ny=uz*vx-ux*vz,nz=ux*vy-uy*vx;
            // Vec3d.normalize discards small but valid model faces below its movement epsilon.
            double length=Math.sqrt(nx*nx+ny*ny+nz*nz);
            if(length==0)continue;
            int i=k/3;result[i]=(float)(nx/length);result[i+1]=(float)(ny/length);result[i+2]=(float)(nz/length);
        }
        return result;
    }
}
