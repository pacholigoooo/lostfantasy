package dev.lostfantasy.core;

public final class Facing {
    private Facing() {}

    public static boolean moving(double x, double y, double z) {
        return x * x + y * y + z * z > 1e-6;
    }

    public static float yaw(double x, double z, float fallback) {
        return x * x + z * z > 1e-6 ? (float) Math.toDegrees(Math.atan2(-x, z)) : fallback;
    }

    public static float pitch(double x, double y, double z) {
        double angle = -Math.toDegrees(Math.atan2(y, Math.sqrt(x * x + z * z)));
        return (float) Math.max(-60, Math.min(60, angle));
    }

    public static float wrap(float angle) {
        angle %= 360;
        if (angle >= 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }

    public static float interpolate(float from, float to, double progress) {
        return from + wrap(to - from) * (float) progress;
    }
}
