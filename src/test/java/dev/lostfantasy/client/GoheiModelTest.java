package dev.lostfantasy.client;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.junit.Test;
import java.io.*;
import java.util.*;
import static org.junit.Assert.*;

public class GoheiModelTest {
    private static final class Sprite extends TextureAtlasSprite {
        Sprite(){super("lostfantasy:blocks/gohei_white");setIconWidth(16);setIconHeight(16);initSprite(256,256,64,32,false);}
    }
    private List<BakedQuad> baked() throws IOException {
        try(DataInputStream input=new DataInputStream(new FileInputStream("src/main/resources/assets/lostfantasy/meshes/gohei.lfm"))) {return MeshItemModel.bake(input,new Sprite(),true);}
    }
    private float[] field(BakedQuad quad,int vertex,String usage) {
        for(int e=0;e<quad.getFormat().getElementCount();e++)if(quad.getFormat().getElement(e).getUsage().name().equals(usage)) {
            float[] value=new float[4];LightUtil.unpack(quad.getVertexData(),value,quad.getFormat(),vertex,e);return value;
        }
        throw new AssertionError("Missing "+usage);
    }
    @Test public void actualPackedVerticesAreOpaqueFiniteAndHaveNormalsEvenOnThinEdges() throws IOException {
        List<BakedQuad> quads=baked();assertEquals(208,quads.size());
        for(BakedQuad quad:quads)for(int i=0;i<4;i++) {
            float[] pos=field(quad,i,"POSITION"),color=field(quad,i,"COLOR"),normal=field(quad,i,"NORMAL");
            for(float p:pos)assertTrue(Float.isFinite(p));
            assertEquals(1,color[3],.00001);
            double length=normal[0]*normal[0]+normal[1]*normal[1]+normal[2]*normal[2];assertEquals(1,length,.03);
        }
    }
    @Test public void bothHalvesOfEveryQuadHaveNonzeroGeometryAndUvArea() throws IOException {
        for(BakedQuad quad:baked())for(int[] tri:new int[][]{{0,1,2},{0,2,3}}) {
            float[] a=field(quad,tri[0],"POSITION"),b=field(quad,tri[1],"POSITION"),c=field(quad,tri[2],"POSITION");
            double ux=b[0]-a[0],uy=b[1]-a[1],uz=b[2]-a[2],vx=c[0]-a[0],vy=c[1]-a[1],vz=c[2]-a[2];
            assertTrue(Math.pow(uy*vz-uz*vy,2)+Math.pow(uz*vx-ux*vz,2)+Math.pow(ux*vy-uy*vx,2)>1e-16);
            a=field(quad,tri[0],"UV");b=field(quad,tri[1],"UV");c=field(quad,tri[2],"UV");
            assertTrue(Math.abs((b[0]-a[0])*(c[1]-a[1])-(b[1]-a[1])*(c[0]-a[0]))>1e-8);
        }
    }
}
