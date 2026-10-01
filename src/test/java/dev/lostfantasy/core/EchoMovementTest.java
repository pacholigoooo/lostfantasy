package dev.lostfantasy.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.junit.Test;
import static org.junit.Assert.*;

public class EchoMovementTest {
    @Test public void descendingClearsAnOverhangEvenWhenTheSweptEnvelopeTouchesIt() {
        AxisAlignedBB body = new AxisAlignedBB(-.275, .4, -.275, .275, 2.1, .275);
        AxisAlignedBB overhang = new AxisAlignedBB(.4, 2, -.5, 1.4, 3, .5);
        Vec3d desired = new Vec3d(.25, -.3, 0);
        assertTrue(body.expand(desired.x, desired.y, desired.z).intersects(overhang));
        Vec3d step = EchoMovement.slide(body, desired, Collections.singletonList(overhang));
        assertEquals(desired, step);
        assertFalse(body.offset(step).intersects(overhang));
    }

    @Test public void aWallStopsTheBlockedAxisButLeavesDescentAndSidewaysMotion() {
        AxisAlignedBB body = new AxisAlignedBB(-.275, 1, -.275, .275, 2.7, .275);
        AxisAlignedBB wall = new AxisAlignedBB(.275, -5, -5, 1.275, 5, 5);
        Vec3d step = EchoMovement.slide(body, new Vec3d(.18, -.18, .18), Collections.singletonList(wall));
        assertEquals(0, step.x, 1e-8);
        assertEquals(-.18, step.y, 1e-8);
        assertEquals(.18, step.z, 1e-8);
        assertFalse(body.offset(step).intersects(wall));
    }

    @Test public void collisionClippingDoesNotSpeedUpOrPassThroughFloorsAndCeilings() {
        AxisAlignedBB body = new AxisAlignedBB(-.275, .1, -.275, .275, 1.8, .275);
        List<AxisAlignedBB> blocks = Arrays.asList(new AxisAlignedBB(-2, -1, -2, 2, 0, 2),
                new AxisAlignedBB(-2, 2, -2, 2, 3, 2));
        for (Vec3d desired : Arrays.asList(new Vec3d(.2, -.2, .1), new Vec3d(.1, .3, 0), Vec3d.ZERO)) {
            Vec3d step = EchoMovement.slide(body, desired, blocks);
            assertTrue(step.lengthSquared() <= desired.lengthSquared() + 1e-10);
            for (AxisAlignedBB block : blocks) assertFalse(body.offset(step).intersects(block));
        }
    }
}
