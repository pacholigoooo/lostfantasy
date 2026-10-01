package dev.lostfantasy.client;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.junit.Test;
import static org.junit.Assert.*;

public class BookcaseModelTest {
    @Test public void eachInventoryVariantResolvesToARealThreeDimensionalModel() throws Exception {
        for (String damage : new String[]{"sparse", "empty", "collapsed", "double_sided"}) {
            ModelBlock item = load("item/ruined_bookcase_" + damage);
            assertEquals("lostfantasy:block/ruined_bookcase_" + damage, item.getParentLocation().toString());
            ModelBlock block = load("block/ruined_bookcase_" + damage);
            item.parent = block;
            assertFalse(item.getElements().isEmpty());
            assertNotEquals("missingno", item.resolveTextureName("particle"));
            assertTrue(item.isGui3d());
        }
    }

    @Test public void forgeFaceBakerProducesFiniteInBlockVerticesInAllFourDirections() throws Exception {
        TextureAtlasSprite sprite = new TestSprite();
        FaceBakery bakery = new FaceBakery();
        for (String damage : new String[]{"sparse", "empty", "collapsed", "double_sided"}) {
            ModelBlock model = load("block/ruined_bookcase_" + damage);
            int faces = 0;
            for (BlockPart part : model.getElements()) {
                for (Map.Entry<EnumFacing, BlockPartFace> entry : part.mapFaces.entrySet()) {
                    if (part.partRotation == null) {
                        EnumFacing face = entry.getKey();
                        int axis = face.getAxis() == EnumFacing.Axis.X ? 0 : face.getAxis() == EnumFacing.Axis.Y ? 1 : 2;
                        boolean negative = face.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
                        org.lwjgl.util.vector.Vector3f point = negative ? part.positionFrom : part.positionTo;
                        float plane = axis == 0 ? point.x : axis == 1 ? point.y : point.z;
                        EnumFacing expected = plane == (negative ? 0 : 16) ? face
                                : damage.equals("collapsed") || damage.equals("double_sided") ? null : EnumFacing.NORTH;
                        assertEquals("Recessed faces must not disappear behind a block above or beside the cabinet",
                                expected, entry.getValue().cullFace);
                    }
                    faces++;
                    for (int y : new int[]{0, 90, 180, 270}) {
                        BakedQuad quad = bakery.makeBakedQuad(part.positionFrom, part.positionTo,
                                entry.getValue(), sprite, entry.getKey(), ModelRotation.getModelRotation(0, y),
                                part.partRotation, false, part.shade);
                        int[] data = quad.getVertexData();
                        int stride = data.length / 4;
                        for (int vertex = 0; vertex < 4; vertex++) for (int axis = 0; axis < 3; axis++) {
                            float coordinate = Float.intBitsToFloat(data[vertex * stride + axis]);
                            assertTrue(damage + " escaped its block: " + coordinate,
                                    Float.isFinite(coordinate) && coordinate >= -.0001f && coordinate <= 1.0001f);
                        }
                    }
                }
            }
            assertTrue(faces >= 18);
            if (!damage.equals("collapsed")) assertTrue("Hidden cuboid faces were not removed", faces < model.getElements().size() * 6);
        }
    }

    private static ModelBlock load(String path) throws Exception {
        try (InputStream stream = BookcaseModelTest.class.getResourceAsStream("/assets/lostfantasy/models/" + path + ".json")) {
            assertNotNull("Missing model: " + path, stream);
            return ModelBlock.deserialize(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
    }

    private static final class TestSprite extends TextureAtlasSprite {
        TestSprite() {
            super("test:wood");
            setIconWidth(16); setIconHeight(16); initSprite(16, 16, 0, 0, false);
        }
    }
}
