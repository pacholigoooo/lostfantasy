package dev.lostfantasy.client;
import java.util.Collections;
import java.lang.reflect.Field;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import org.junit.Test;
import static org.junit.Assert.*;
public class RibbonGeometryTest {
    @Test public void readsSemanticAttributesInsteadOfShaderExpandedPackedStride() throws Exception {
        VertexFormat format=new VertexFormat().addElement(DefaultVertexFormats.POSITION_3F).addElement(DefaultVertexFormats.NORMAL_3B).addElement(DefaultVertexFormats.PADDING_1B);
        float[][][] data=new float[4][3][4];
        for(int i=0;i<4;i++){data[i][0]=new float[]{i*.1f,.7f,.2f,1};data[i][1]=new float[]{0,1,0,0};}
        UnpackedBakedQuad q=new UnpackedBakedQuad(data,-1,EnumFacing.UP,null,true,format) {
            @Override public int[] getVertexData(){throw new AssertionError("Packed shader stride must not be read");}
        };
        RibbonItemRenderer.Geometry geometry=new RibbonItemRenderer.Geometry(Collections.singletonList(q));
        Field field=RibbonItemRenderer.Geometry.class.getDeclaredField("vertices");field.setAccessible(true);float[][] vertices=(float[][])field.get(geometry);
        for(int i=0;i<4;i++)assertArrayEquals(new float[]{i*.1f,.7f,.2f,0,1,0},vertices[i],0);
    }
}
