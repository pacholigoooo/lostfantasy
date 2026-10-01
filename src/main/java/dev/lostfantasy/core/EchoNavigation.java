package dev.lostfantasy.core;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.util.math.Vec3d;

/** Cached detours for the companion's existing velocity-based movement. */
public final class EchoNavigation {
    private List<Vec3d> route = Collections.emptyList();
    private int waypoint;
    private Vec3d destination;
    private int nextSearchTick, lastProgressTick;
    private double closestDistance = Double.POSITIVE_INFINITY;
    private final int searchDelay;
    private boolean searchScheduled;
    private Vec3d lastRequest, lastMovement;

    public EchoNavigation() {
        this(0);
    }

    public EchoNavigation(int searchDelay) {
        if (searchDelay < 0 || searchDelay >= 20) throw new IllegalArgumentException("Search delay must be below 20 ticks");
        this.searchDelay = searchDelay;
    }

    public Vec3d step(Vec3d at, Vec3d goal, double speed, double stop, int tick,
                     UnaryOperator<Vec3d> resolve, Supplier<List<Vec3d>> plan) {
        lastRequest = lastMovement = null;
        Vec3d direct = EchoRoaming.step(at, goal, speed, stop);
        if (direct.lengthSquared() < 1e-12) {
            clearRoute();
            return Vec3d.ZERO;
        }
        if (destination != null && destination.squareDistanceTo(goal) > 4) clearRoute();

        Vec3d detour = follow(at, speed, tick, resolve);
        if (detour != null) return detour;
        Vec3d movement = resolve(direct, resolve);
        if (movement.squareDistanceTo(direct) < 1e-10) return movement;

        if (!searchScheduled) {
            nextSearchTick = tick + searchDelay;
            searchScheduled = true;
        }
        if (tick >= nextSearchTick) {
            nextSearchTick = tick + 20;
            route = plan.get();
            waypoint = 0;
            destination = goal;
            closestDistance = Double.POSITIVE_INFINITY;
            lastProgressTick = tick;
            detour = follow(at, speed, tick, resolve);
            if (detour != null) return detour;
        }
        return movement;
    }

    private Vec3d follow(Vec3d at, double speed, int tick, UnaryOperator<Vec3d> resolve) {
        while (waypoint < route.size() && at.squareDistanceTo(route.get(waypoint)) < .04) {
            waypoint++;
            closestDistance = Double.POSITIVE_INFINITY;
            lastProgressTick = tick;
        }
        if (waypoint >= route.size()) {
            clearRoute();
            return null;
        }
        double distance = at.squareDistanceTo(route.get(waypoint));
        if (distance < closestDistance - .01) {
            closestDistance = distance;
            lastProgressTick = tick;
        } else if (tick - lastProgressTick >= 20) {
            clearRoute();
            return null;
        }
        Vec3d movement = resolve(EchoRoaming.step(at, route.get(waypoint), speed, 0), resolve);
        if (movement.lengthSquared() < 1e-10) {
            clearRoute();
            return null;
        }
        return movement;
    }

    private Vec3d resolve(Vec3d request, UnaryOperator<Vec3d> resolver) {
        // Direct movement and a route's first step can be identical. Reuse only within this tick.
        if (!request.equals(lastRequest)) {
            lastRequest = request;
            lastMovement = resolver.apply(request);
        }
        return lastMovement;
    }

    private void clearRoute() {
        route = Collections.emptyList();
        destination = null;
        waypoint = 0;
        closestDistance = Double.POSITIVE_INFINITY;
    }
}
