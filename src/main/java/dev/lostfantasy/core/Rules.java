package dev.lostfantasy.core;

import java.util.HashSet;
import java.util.Set;
import java.util.function.IntPredicate;

/** Deterministic gameplay rules shared by the server and unit tests. */
public final class Rules {
    private Rules() {}
    public static final int MAX_POWER = 5;
    public static final int LOADOUT_SIZE = 4;
    public static boolean sphereIntersectsBox(double x,double y,double z,double radius,double minX,double minY,double minZ,double maxX,double maxY,double maxZ) {
        double dx=x-Math.max(minX,Math.min(maxX,x)),dy=y-Math.max(minY,Math.min(maxY,y)),dz=z-Math.max(minZ,Math.min(maxZ,z));
        return radius>=0&&dx*dx+dy*dy+dz*dz<=radius*radius;
    }
    public static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    public static float clamp(float value, float min, float max) {
        return Float.isFinite(value) ? Math.max(min, Math.min(max, value)) : min;
    }
    public static int spiritCapacity(long experience, int[] thresholds) {
        int capacity = 2;
        for (int threshold : thresholds) if (experience >= threshold) capacity++;
        return Math.min(6, capacity);
    }
    public static int powerDrop(float maximumHealth, float healthPerPower) {
        if (!Float.isFinite(maximumHealth) || maximumHealth <= 0) return 0;
        return clamp((int) Math.ceil(maximumHealth / Math.max(1, healthPerPower)), 1, MAX_POWER);
    }
    /** Add percentage points to other mitigation, bounded at complete prevention. */
    public static float remainingDamage(float original, float afterOtherMitigation, float extraReduction) {
        if (!Float.isFinite(original) || !Float.isFinite(afterOtherMitigation)) return 0;
        return Math.max(0, afterOtherMitigation - Math.max(0, original) * clamp(extraReduction, 0, 1));
    }
    public static boolean validLoadout(int[] slots, IntPredicate learned, IntPredicate ultimate) {
        if (slots == null || slots.length != LOADOUT_SIZE) return false;
        Set<Integer> seen = new HashSet<>();
        int finals = 0;
        for (int id : slots) {
            if (id == -1) continue;
            if (id < 0 || !learned.test(id) || !seen.add(id)) return false;
            if (ultimate.test(id) && ++finals > 1) return false;
        }
        return true;
    }
    public static boolean canLearnBook(int stage, int bookTier, int capacity) {
        return bookTier >= 1 && bookTier <= 3 && stage == bookTier - 1 && capacity >= bookTier * 2;
    }
    /** Distance to a finite beam segment, avoiding infinite-line hits behind the emitter. */
    public static double distanceToSegmentSquared(double px, double py, double pz,
                                                  double ax, double ay, double az,
                                                  double bx, double by, double bz) {
        double dx=bx-ax, dy=by-ay, dz=bz-az, denom=dx*dx+dy*dy+dz*dz;
        double t=denom < 1.0e-9 ? 0 : Math.max(0,Math.min(1,((px-ax)*dx+(py-ay)*dy+(pz-az)*dz)/denom));
        double x=px-ax-dx*t, y=py-ay-dy*t, z=pz-az-dz*t;
        return x*x+y*y+z*z;
    }
}
