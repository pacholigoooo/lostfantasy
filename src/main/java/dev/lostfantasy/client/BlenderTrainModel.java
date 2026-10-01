package dev.lostfantasy.client;

import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import java.io.*;

/** Cached source triangles; the portal cut is independent of fixed-function clip planes. */
public final class BlenderTrainModel implements IResourceManagerReloadListener {
    public static final BlenderTrainModel INSTANCE=new BlenderTrainModel();
    private static final ResourceLocation MESH=new ResourceLocation("lostfantasy","meshes/yukari_train.lfm");
    private ClippedTrainMesh mesh;
    private boolean failed;
    private BlenderTrainModel() {}
    @Override public void onResourceManagerReload(IResourceManager manager) {mesh=null;failed=false;load(manager);}
    public void draw(double travel) {
        if(failed || mesh==null)return;
        BufferBuilder b=Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_TRIANGLES,DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        mesh.draw(-travel,(x,y,z,r,g,blue,a,nx,ny,nz)->b.pos(x,y,z).tex(.5,.5)
                .color(r,g,blue,a).normal(nx,ny,nz).endVertex());
        Tessellator.getInstance().draw();
    }
    private void load(IResourceManager manager) {
        try(IResource resource=manager.getResource(MESH);
            DataInputStream in=new DataInputStream(new BufferedInputStream(resource.getInputStream()))) {
            mesh=new ClippedTrainMesh(in);
        }catch(IOException ex) {failed=true;org.apache.logging.log4j.LogManager.getLogger("LostFantasy").error("Unable to load Blender train mesh",ex);}
    }
}
