package dev.lostfantasy.client;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.junit.Test;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.List;
import static org.junit.Assert.*;

public class GapItemModelTest {
    private static final class Sprite extends TextureAtlasSprite {
        Sprite() {
            super("lostfantasy:blocks/gohei_white");
            setIconWidth(16);
            setIconHeight(16);
            initSprite(256, 256, 64, 32, false);
        }
    }

    @Test
    public void fragmentBakesOpaqueColouredFacets() throws Exception {
        check("gap_fragment", 88);
    }

    @Test
    public void energyCoreBakesRingsAndThinEdges() throws Exception {
        check("gap_key", 664);
    }

    private void check(String mesh, int triangles) throws Exception {
        List<BakedQuad> quads;
        try (DataInputStream input = new DataInputStream(new FileInputStream(
                "src/main/resources/assets/lostfantasy/meshes/" + mesh + ".lfm"))) {
            quads = MeshItemModel.bake(input, new Sprite(), false);
        }
        assertEquals(triangles, quads.size());
        for (BakedQuad quad : quads) {
            assertFalse(quad.shouldApplyDiffuseLighting());
            assertFalse(quad.hasTintIndex());
            for (int vertex = 0; vertex < 4; vertex++) {
                float[] position = field(quad, vertex, "POSITION");
                for (int axis = 0; axis < 3; axis++) assertTrue(position[axis] >= 0 && position[axis] <= 1);
                assertEquals(1, field(quad, vertex, "COLOR")[3], .00001);
                float[] normal = field(quad, vertex, "NORMAL");
                assertEquals(1, normal[0] * normal[0] + normal[1] * normal[1] + normal[2] * normal[2], .03);
                float[] uv = field(quad, vertex, "UV");
                assertTrue(uv[0] > 64f / 256 && uv[0] < 80f / 256);
                assertTrue(uv[1] > 32f / 256 && uv[1] < 48f / 256);
            }
            for (int[] triangle : new int[][]{{0, 1, 2}, {0, 2, 3}}) {
                float[] a = field(quad, triangle[0], "POSITION");
                float[] b = field(quad, triangle[1], "POSITION");
                float[] c = field(quad, triangle[2], "POSITION");
                double ux = b[0] - a[0], uy = b[1] - a[1], uz = b[2] - a[2];
                double vx = c[0] - a[0], vy = c[1] - a[1], vz = c[2] - a[2];
                assertTrue(Math.pow(uy * vz - uz * vy, 2) + Math.pow(uz * vx - ux * vz, 2)
                        + Math.pow(ux * vy - uy * vx, 2) > 1e-18);
            }
        }
    }

    private float[] field(BakedQuad quad, int vertex, String usage) {
        for (int element = 0; element < quad.getFormat().getElementCount(); element++) {
            if (!quad.getFormat().getElement(element).getUsage().name().equals(usage)) continue;
            float[] value = new float[4];
            LightUtil.unpack(quad.getVertexData(), value, quad.getFormat(), vertex, element);
            return value;
        }
        throw new AssertionError("Missing vertex field: " + usage);
    }
}
