package dev.lostfantasy.client;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.junit.Test;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.List;
import static org.junit.Assert.*;

public class RibbonModelTest {
    private static final Path MESH=Paths.get("src/main/resources/assets/lostfantasy/meshes/misfortune_ribbon.lfm");
    private static final class Sprite extends TextureAtlasSprite {
        Sprite(){super("lostfantasy:items/misfortune_ribbon_satin");setIconWidth(1024);setIconHeight(1024);initSprite(4096,4096,1024,1024,false);}
    }
    private static List<BakedQuad> bake(byte[] bytes) throws IOException {
        return MeshItemModel.bake(new DataInputStream(new ByteArrayInputStream(bytes)),new Sprite(),true);
    }
    private static float[] field(BakedQuad q,int vertex,String usage) {
        for(int i=0;i<q.getFormat().getElementCount();i++)if(q.getFormat().getElement(i).getUsage().name().equals(usage)) {
            float[] value=new float[4];LightUtil.unpack(q.getVertexData(),value,q.getFormat(),vertex,i);return value;
        }
        throw new AssertionError(usage);
    }
    @Test public void satinAtlasStaysInsideItsSpriteAndTriangleSplitPreservesUvAndSmoothNormals() throws Exception {
        List<BakedQuad> quads=bake(Files.readAllBytes(MESH));assertTrue(quads.size()>3000);assertTrue(quads.size()<=4000);
        for(BakedQuad q:quads) {
            assertTrue(q.shouldApplyDiffuseLighting());
            for(int v=0;v<4;v++) {
                float[] n=field(q,v,"NORMAL"),uv=field(q,v,"UV");
                assertEquals(1,n[0]*n[0]+n[1]*n[1]+n[2]*n[2],.03);
                for(int axis=0;axis<2;axis++)assertTrue(uv[axis]>.25 && uv[axis]<.5);
                for(float value:field(q,v,"POSITION"))assertTrue(Float.isFinite(value));
            }
            for(int axis=0;axis<2;axis++)assertEquals((field(q,1,"UV")[axis]+field(q,3,"UV")[axis])*.5f,field(q,2,"UV")[axis],.00001);
        }
    }
    @Test public void corruptUvAndTruncatedTexturedVerticesCannotBake() throws Exception {
        byte[] original=Files.readAllBytes(MESH),bad=original.clone();ByteBuffer.wrap(bad).putFloat(24,Float.NaN);
        try{bake(bad);fail("Non-finite UV accepted");}catch(IOException expected){}
        try{bake(java.util.Arrays.copyOf(original,original.length-1));fail("Truncated vertex accepted");}catch(IOException expected){}
    }
}
