package dev.lostfantasy.client;

import java.io.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** A cached Blender mesh submitted once through the ordinary textured entity pass. */
final class TexturedMesh implements IResourceManagerReloadListener {
    static final TexturedMesh CABLECAR = new TexturedMesh("cablecar");
    private final ResourceLocation resource;
    private Part part;
    private boolean failed;

    TexturedMesh(String path) { resource = new ResourceLocation("lostfantasy", "meshes/" + path + ".lfm"); }
    @Override public void onResourceManagerReload(IResourceManager manager) { part = null; failed = false; load(manager); }
    void draw(float opacity) {
        if (failed || part == null) return;
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        for (int i = 0; i < part.colors.length; i++) {
            int o = i * 8, color = part.colors[i];
            float[] v = part.values;
            buffer.pos(v[o], v[o+1], v[o+2]).tex(v[o+3], v[o+4])
                    .color(color >>> 24 & 255, color >>> 16 & 255, color >>> 8 & 255, (int)((color & 255) * opacity))
                    .normal(v[o+5], v[o+6], v[o+7]).endVertex();
        }
        Tessellator.getInstance().draw();
    }
    private void load(IResourceManager manager) {
        try (IResource file = manager.getResource(resource);
             DataInputStream input = new DataInputStream(new BufferedInputStream(file.getInputStream()))) {
            part = read(input);
        } catch (IOException ex) {
            failed = true;
            org.apache.logging.log4j.LogManager.getLogger("LostFantasy").error("Cannot load mesh " + resource, ex);
        }
    }
    static Part read(DataInputStream input) throws IOException {
        if (input.readInt() != 0x4c464d32) throw new IOException("Invalid textured mesh header");
        int count = input.readInt();
        if (count <= 0 || count % 3 != 0 || count > 18000) throw new IOException("Invalid textured mesh size");
        Part result = new Part(count);
        for (int i = 0; i < count; i++) {
            int o = i * 8;
            for (int a = 0; a < 3; a++) result.values[o+a] = input.readFloat();
            result.colors[i] = input.readInt();
            for (int a = 3; a < 8; a++) result.values[o+a] = input.readFloat();
            for (int a = 0; a < 8; a++) if (!Float.isFinite(result.values[o+a])) throw new IOException("Nonfinite mesh vertex");
            if (result.values[o+3] < 0 || result.values[o+3] > 1 || result.values[o+4] < 0 || result.values[o+4] > 1)
                throw new IOException("Mesh UV outside atlas");
            double nx = result.values[o+5], ny = result.values[o+6], nz = result.values[o+7];
            double length = Math.sqrt(nx*nx + ny*ny + nz*nz);
            if (length < 1e-6) throw new IOException("Zero mesh normal");
            for (int a = 5; a < 8; a++) result.values[o+a] /= length;
        }
        if (input.read() != -1) throw new IOException("Trailing mesh data");
        return result;
    }
    static final class Part {
        final float[] values;
        final int[] colors;
        Part(int count) { values = new float[count*8]; colors = new int[count]; }
    }
}
