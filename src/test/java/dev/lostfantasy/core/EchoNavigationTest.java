package dev.lostfantasy.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EchoNavigationTest {
    @Test
    public void companionsStaggerBlockedSearchesWithoutSearchingMoreOften() {
        int[] calls = new int[63];
        for (int delay = 0; delay < 3; delay++) {
            EchoNavigation navigation = new EchoNavigation(delay);
            for (int tick = 0; tick < 63; tick++) {
                final int now = tick;
                navigation.step(Vec3d.ZERO, new Vec3d(0, -4, 0), .32, 0, tick,
                        desired -> Vec3d.ZERO, () -> {
                            calls[now]++;
                            return Collections.emptyList();
                        });
            }
        }
        int total = 0;
        for (int count : calls) {
            assertTrue("Searches bunched on the same tick", count <= 1);
            total += count;
        }
        assertEquals(12, total);
    }

    @Test
    public void repeatedStepSharesCollisionQueryOnlyWithinTheCurrentTick() {
        EchoNavigation navigation = new EchoNavigation();
        AtomicInteger queries = new AtomicInteger();
        Vec3d goal = new Vec3d(0, -4, 0);
        navigation.step(Vec3d.ZERO, goal, .32, 0, 0,
                desired -> {
                    queries.incrementAndGet();
                    return Vec3d.ZERO;
                },
                () -> Collections.singletonList(goal));
        assertEquals("Identical direct and route steps should share the query", 1, queries.get());
        Vec3d next = navigation.step(Vec3d.ZERO, goal, .32, 0, 1,
                desired -> {
                    queries.incrementAndGet();
                    return desired;
                }, Collections::emptyList);
        assertEquals(2, queries.get());
        assertTrue("Removed obstacles must be noticed next tick", next.y < 0);
    }

    @Test
    public void cachedDetourGoesPastTheLedgeBeforeDescendingInsteadOfTurningBack() {
        EchoNavigation navigation = new EchoNavigation();
        AtomicInteger searches = new AtomicInteger();
        AxisAlignedBB ledge = new AxisAlignedBB(-1, 2, -1, 2, 3, 2);
        Vec3d at = new Vec3d(.5, 3, .5), goal = new Vec3d(.5, 0, .5);
        double furthest = at.x;
        for (int tick = 0; tick < 100 && at.squareDistanceTo(goal) > .04; tick++) {
            AxisAlignedBB body = body(at);
            Vec3d movement = navigation.step(at, goal, .32, 0, tick,
                    desired -> EchoMovement.slide(body, desired, Collections.singletonList(ledge)), () -> {
                        searches.incrementAndGet();
                        return Arrays.asList(new Vec3d(2.5, 3, .5), new Vec3d(2.5, 0, .5), goal);
                    });
            assertTrue(movement.lengthSquared() <= .32 * .32 + 1e-10);
            at = at.add(movement);
            furthest = Math.max(furthest, at.x);
            assertFalse(body(at).intersects(ledge));
        }
        assertTrue(furthest > 2.2);
        assertTrue(at.squareDistanceTo(goal) < .04);
        assertEquals(1, searches.get());
    }

    @Test
    public void openSpaceUsesDirectMovementWithoutSearching() {
        EchoNavigation navigation = new EchoNavigation();
        Vec3d goal = new Vec3d(4, -2, 1), at = Vec3d.ZERO;
        for (int tick = 0; tick < 40; tick++) {
            at = at.add(navigation.step(at, goal, .32, .5, tick, desired -> desired,
                    () -> {
                        throw new AssertionError("Open space must not invoke pathfinding");
                    }));
        }
        assertEquals(.5, at.distanceTo(goal), 1e-8);
    }

    @Test
    public void sealedRoutesAreRetriedOncePerSecondWithoutBlindlyFlyingUp() {
        EchoNavigation navigation = new EchoNavigation();
        AtomicInteger searches = new AtomicInteger();
        for (int tick = 0; tick < 200; tick++) {
            Vec3d movement = navigation.step(Vec3d.ZERO, new Vec3d(0, -4, 0), .32, 0, tick,
                    desired -> Vec3d.ZERO, () -> {
                        searches.incrementAndGet();
                        return Collections.emptyList();
                    });
            assertEquals(Vec3d.ZERO, movement);
        }
        assertEquals(10, searches.get());
    }

    @Test
    public void aMovingTargetInvalidatesTheOldDetour() {
        EchoNavigation navigation = new EchoNavigation();
        Vec3d at = Vec3d.ZERO;
        navigation.step(at, new Vec3d(0, -4, 0), .32, 0, 0,
                desired -> desired.y < 0 ? Vec3d.ZERO : desired,
                () -> Collections.singletonList(new Vec3d(4, 0, 0)));
        Vec3d step = navigation.step(at, new Vec3d(-4, 0, 0), .32, 0, 1, desired -> desired,
                () -> {
                    throw new AssertionError("New target is directly reachable");
                });
        assertTrue(step.x < 0);
    }

    @Test
    public void failureToMakeProgressDiscardsTheCachedRoute() {
        EchoNavigation navigation = new EchoNavigation();
        AtomicInteger searches = new AtomicInteger();
        for (int tick = 0; tick < 50; tick++) {
            navigation.step(Vec3d.ZERO, new Vec3d(0, -4, 0), .32, 0, tick,
                    desired -> desired.y < 0 ? Vec3d.ZERO : desired,
                    () -> {
                        searches.incrementAndGet();
                        return Collections.singletonList(new Vec3d(4, 0, 0));
                    });
        }
        assertEquals(3, searches.get());
    }

    private static AxisAlignedBB body(Vec3d at) {
        return new AxisAlignedBB(at.x - .275, at.y, at.z - .275, at.x + .275, at.y + 1.7, at.z + .275);
    }
}
