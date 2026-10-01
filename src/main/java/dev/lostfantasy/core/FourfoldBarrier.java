package dev.lostfantasy.core;

/** Shared 20 Hz timeline and horizontal geometry for the barrier, independent of rendering. */
public final class FourfoldBarrier {
    public static final int OPEN = 6, PROTECTION_END = 22, FINISH_HIT = 38, FADE_END = 42, DURATION = 52;
    public static final double NEAR = 1.5, FAR = 4.5, WIDTH = 2.5, PULL_FAR = 7, PULL_WIDTH = 3;
    public static final double CENTER = 3, BOTTOM = -1, TOP = 3, PLANE_HEIGHT = .12;
    private FourfoldBarrier() {}

    public static boolean pulling(int age) { return age >= OPEN && age < FINISH_HIT; }
    public static boolean smallHit(int age) { return pulling(age) && (age - OPEN) % 4 == 0; }
    public static boolean restricted(long age) { return age >= 0 && age < DURATION; }
    public static boolean protectedAt(long age) { return age >= 0 && age < PROTECTION_END; }
    public static double pose(double age) {
        return CastMotion.smooth(age / OPEN) * (1 - CastMotion.smooth((age - FADE_END) / (DURATION - FADE_END)));
    }

    /** Exact horizontal rectangle/AABB SAT. Vertical tolerance is a total of four blocks. */
    public static boolean intersects(double dx, double dz, double minX, double minY, double minZ,
                                     double maxX, double maxY, double maxZ, boolean pull) {
        if (maxY <= BOTTOM || minY >= TOP) return false;
        double far = pull ? PULL_FAR : FAR, width = pull ? PULL_WIDTH : WIDTH;
        double half = (far - NEAR) / 2, center = (far + NEAR) / 2;
        double x = (minX + maxX) / 2 - dx * center, z = (minZ + maxZ) / 2 - dz * center;
        double hx = (maxX - minX) / 2, hz = (maxZ - minZ) / 2;
        return Math.abs(x * dx + z * dz) <= half + hx * Math.abs(dx) + hz * Math.abs(dz)
                && Math.abs(x * dz - z * dx) <= width + hx * Math.abs(dz) + hz * Math.abs(dx)
                && Math.abs(x) <= half * Math.abs(dx) + width * Math.abs(dz) + hx
                && Math.abs(z) <= half * Math.abs(dz) + width * Math.abs(dx) + hz;
    }

    /** Planar, offset squares, scaled to stay within the attack footprint. [side, forward] */
    public static double[] corner(int layer, int corner) {
        double angle = Math.PI / 4 + layer * Math.PI / 8 + corner * Math.PI / 2;
        return new double[]{Math.cos(angle) * WIDTH, Math.sin(angle) * (FAR - CENTER)};
    }
}
