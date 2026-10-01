package dev.lostfantasy.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.math.Vec3d;
import java.io.DataInputStream;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

public class KomachiRowingPoseTest {
    @Test public void bothPalmsStayOnTheSameRigidShaftThroughoutTheStroke() {
        ModelBase model = new ModelBase() {};
        ModelRenderer left = new ModelRenderer(model), right = new ModelRenderer(model);
        for (int frame = 0; frame <= 360; frame++) {
            KomachiRowingPose pose = KomachiRowingPose.at((float)(frame * Math.PI / 180 / .09));
            pose.apply(left, right);
            Vec3d leftPalm = palm(left, .5), rightPalm = palm(right, -.5);
            assertEquals(0, leftPalm.distanceTo(pose.upper), .00001);
            assertEquals(0, rightPalm.distanceTo(pose.lower), .00001);
            assertEquals(7, leftPalm.distanceTo(rightPalm), .00001);
            assertTrue(leftPalm.z < -4 && rightPalm.z < -4);
        }
    }

    @Test public void strokeLoopsSmoothlyAndActuallyMovesTheBlade() {
        KomachiRowingPose start = KomachiRowingPose.at(0);
        KomachiRowingPose end = KomachiRowingPose.at((float)(Math.PI * 2 / .09));
        assertEquals(0, start.upper.distanceTo(end.upper), .00001);
        assertEquals(0, start.direction.distanceTo(end.direction), .00001);
        KomachiRowingPose pull = KomachiRowingPose.at((float)(Math.PI / 2 / .09));
        KomachiRowingPose recover = KomachiRowingPose.at((float)(Math.PI * 1.5 / .09));
        assertTrue(Math.abs(pull.direction.z - recover.direction.z) > .5);
        assertTrue(pull.upper.distanceTo(recover.upper) > 1);
        double lowest = 10, highest = -10;
        for (int degree = 0; degree < 360; degree += 5) {
            KomachiRowingPose pose = KomachiRowingPose.at((float)(Math.toRadians(degree) / .09));
            double bladeY = 1.691 - pose.upper.y / 16
                    - pose.direction.y * (1.5 + .7875) * KomachiRowingPose.OAR_SCALE;
            lowest = Math.min(lowest, bladeY);
            highest = Math.max(highest, bladeY);
        }
        // River surface and ferry origin are nearly level; dip the blade then lift it clear.
        assertTrue(lowest < -.04);
        assertTrue(highest > .03);
    }

    @Test public void shaftClearsTheActualBoatMeshDuringPullAndRecovery() throws IOException {
        Vec3d[] vertices;
        try (DataInputStream in = new DataInputStream(getClass().getResourceAsStream(
                "/assets/lostfantasy/meshes/river_ferry.lfm"))) {
            assertEquals(0x4c464d31, in.readInt());
            vertices = new Vec3d[in.readInt()];
            for (int i = 0; i < vertices.length; i++) {
                vertices[i] = new Vec3d(in.readFloat(), in.readFloat(), in.readFloat());
                in.readInt();
            }
        }
        Vec3d[] margins = {Vec3d.ZERO, new Vec3d(.035, -.02, 0), new Vec3d(-.035, 0, 0),
                new Vec3d(0, 0, .035), new Vec3d(0, 0, -.035)};
        for (int degree = 0; degree < 360; degree += 5) {
            KomachiRowingPose pose = KomachiRowingPose.at((float)(Math.toRadians(degree) / .09));
            Vec3d origin = new Vec3d(pose.upper.x / 16, 1.691 - pose.upper.y / 16, .90 - pose.upper.z / 16);
            Vec3d ray = new Vec3d(pose.direction.x, -pose.direction.y, -pose.direction.z)
                    .scale((1.5 + .7875) * KomachiRowingPose.OAR_SCALE);
            for (Vec3d margin : margins) {
                Vec3d start = origin.add(margin);
                for (int i = 0; i < vertices.length; i += 3) {
                    assertFalse("Oar intersects ferry at phase " + degree,
                            intersects(start, ray, vertices[i], vertices[i + 1], vertices[i + 2]));
                }
            }
        }
    }

    private static boolean intersects(Vec3d start, Vec3d ray, Vec3d a, Vec3d b, Vec3d c) {
        Vec3d edge = b.subtract(a), second = c.subtract(a), h = ray.crossProduct(second);
        double determinant = edge.dotProduct(h);
        if (Math.abs(determinant) < 1e-8) return false;
        Vec3d s = start.subtract(a);
        double u = s.dotProduct(h) / determinant;
        if (u < 0 || u > 1) return false;
        Vec3d q = s.crossProduct(edge);
        double v = ray.dotProduct(q) / determinant, distance = second.dotProduct(q) / determinant;
        return v >= 0 && u + v <= 1 && distance > 0 && distance < 1;
    }

    private static Vec3d palm(ModelRenderer arm, double x) {
        // Apply the model's X then Z rotations to the center of the last hand pixels.
        double y = KomachiRowingPose.PALM_Y * Math.cos(arm.rotateAngleX);
        double z = KomachiRowingPose.PALM_Y * Math.sin(arm.rotateAngleX);
        return new Vec3d(x * Math.cos(arm.rotateAngleZ) - y * Math.sin(arm.rotateAngleZ) + arm.rotationPointX,
                x * Math.sin(arm.rotateAngleZ) + y * Math.cos(arm.rotateAngleZ) + arm.rotationPointY,
                z + arm.rotationPointZ);
    }
}
