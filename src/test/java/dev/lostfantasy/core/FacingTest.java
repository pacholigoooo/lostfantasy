package dev.lostfantasy.core;

import net.minecraft.util.math.Vec3d;
import org.junit.Test;
import static org.junit.Assert.*;

public class FacingTest {
    @Test public void horizontalTravelFacesEachCardinalDirection() {
        assertEquals(0, Facing.yaw(0, 1, 12), 0);
        assertEquals(-90, Facing.yaw(1, 0, 12), 0);
        assertEquals(90, Facing.yaw(-1, 0, 12), 0);
        assertEquals(180, Math.abs(Facing.yaw(0, -1, 12)), 0);
    }

    @Test public void tinyStepsAndVerticalTravelDoNotResetTheBodyYaw() {
        assertFalse(Facing.moving(0.0001, 0, 0));
        assertTrue(Facing.moving(0, .22, 0));
        assertEquals(123, Facing.yaw(0, 0, 123), 0);
        assertEquals(123, Facing.yaw(.0001, .0001, 123), 0);
        assertEquals(-60, Facing.pitch(0, .22, 0), 0);
        assertEquals(60, Facing.pitch(0, -.22, 0), 0);
        assertEquals(-45, Facing.pitch(1, 1, 0), 1e-5);
    }

    @Test public void obstacleDetoursFaceTheirStepInsteadOfTheOriginalGoal() {
        Vec3d at = new Vec3d(.5, 70, .5);
        for (Vec3d waypoint : new Vec3d[]{new Vec3d(3.5, 70, .5), new Vec3d(3.5, 66, .5), new Vec3d(.5, 66, 4.5)}) {
                Vec3d step = EchoRoaming.step(at, waypoint, .32, 0);
                double length = Math.hypot(step.x, step.z);
                if (length < .001) continue;
                double yaw = Math.toRadians(Facing.yaw(step.x, step.z, 0));
                assertEquals(step.x / length, -Math.sin(yaw), 1e-6);
                assertEquals(step.z / length, Math.cos(yaw), 1e-6);
        }
    }

    @Test public void interpolationTakesTheShortTurnAcrossTheWrap() {
        assertEquals(180, Facing.interpolate(179, -179, .5), 1e-5);
        assertEquals(-180, Facing.interpolate(-179, 179, .5), 1e-5);
    }
}
