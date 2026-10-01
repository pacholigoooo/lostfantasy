package dev.lostfantasy.client;

import java.io.DataInputStream;
import java.io.InputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class RiverMeshTest {
    @Test public void smallFacesKeepUnitNormalsAndWinding() {
        assertArrayEquals(new float[]{0, 0, 1}, RiverMesh.faceNormals(new float[]{0,0,0, .001f,0,0, 0,.001f,0}), 1e-6f);
        assertArrayEquals(new float[]{0, 0, -1}, RiverMesh.faceNormals(new float[]{0,0,0, 0,.001f,0, .001f,0,0}), 1e-6f);
        assertArrayEquals(new float[3], RiverMesh.faceNormals(new float[9]), 0);
    }

    @Test public void everyShippedFerryAndLampFaceHasAUnitNormal() throws Exception {
        for (String name : new String[]{"river_ferry", "river_ferry_lamp"}) {
            InputStream resource = getClass().getResourceAsStream("/assets/lostfantasy/meshes/" + name + ".lfm");
            assertNotNull(name, resource);
            try (DataInputStream in = new DataInputStream(resource)) {
                assertEquals(0x4c464d31, in.readInt());
                int count = in.readInt();
                float[] vertices = new float[count * 3];
                for (int i = 0; i < count; i++) {
                    for (int axis = 0; axis < 3; axis++) vertices[i * 3 + axis] = in.readFloat();
                    in.readInt();
                }
                float[] normals = RiverMesh.faceNormals(vertices);
                for (int i = 0; i < normals.length; i += 3) {
                    double length = Math.sqrt(normals[i]*normals[i]+normals[i+1]*normals[i+1]+normals[i+2]*normals[i+2]);
                    assertEquals(name + " face " + i/3, 1, length, 1e-6);
                }
            }
        }
    }
}
