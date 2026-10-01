package dev.lostfantasy.core;

import net.minecraft.util.math.Vec3d;
import java.util.Random;

public final class EchoRoaming {
    public static final double RADIUS = 100;
    public static final double RETURN_RADIUS = 96;

    public enum Mode { PATROL, CHASE, RETURN }

    private Mode mode = Mode.PATROL;

    public Mode mode() {
        return mode;
    }

    public Mode update(boolean validTarget, double ownerDistanceSquared) {
        // Return a little past the boundary to avoid switching modes at its edge.
        if (mode == Mode.RETURN && ownerDistanceSquared > RETURN_RADIUS * RETURN_RADIUS) {
            return mode;
        }
        if (validTarget) {
            mode = Mode.CHASE;
        } else {
            mode = ownerDistanceSquared > RADIUS * RADIUS ? Mode.RETURN : Mode.PATROL;
        }
        return mode;
    }

    public static boolean inside(Vec3d at, Vec3d owner) {
        return at.squareDistanceTo(owner) <= RADIUS * RADIUS;
    }

    public static Vec3d patrolPoint(Vec3d at, Vec3d owner, Random random) {
        double angle = random.nextDouble() * Math.PI * 2;
        double distance = 8 + random.nextDouble() * 12;
        Vec3d point = at.add(Math.cos(angle) * distance,
                (random.nextDouble() - .5) * 4, Math.sin(angle) * distance);
        return clampToOwner(point, owner, RADIUS - 2);
    }

    public static Vec3d returnPoint(Vec3d at, Vec3d owner) {
        return clampToOwner(at, owner, RETURN_RADIUS - 1);
    }

    private static Vec3d clampToOwner(Vec3d at, Vec3d owner, double radius) {
        Vec3d offset = at.subtract(owner);
        double lengthSquared = offset.lengthSquared();
        return lengthSquared > radius * radius
                ? owner.add(offset.scale(radius / Math.sqrt(lengthSquared))) : at;
    }

    public static Vec3d step(Vec3d at, Vec3d goal, double speed, double stop) {
        Vec3d delta = goal.subtract(at);
        double lengthSquared = delta.lengthSquared();
        if (lengthSquared <= stop * stop || lengthSquared < 1e-12) return Vec3d.ZERO;

        double length = Math.sqrt(lengthSquared);
        return delta.scale(Math.min(speed, length - stop) / length);
    }

}
