package dev.lostfantasy.core;

/** Ranks visible candidates after the owner's current attack target has been checked. */
public final class EchoTargeting {
    public static final double RANGE = 32;

    private boolean found;
    private boolean bestPlayer;
    private double bestOwnerDistance;
    private boolean bestClaimed;
    private double bestEchoDistance;

    public boolean canImprove(boolean engagedPlayer, double ownerDistanceSquared, boolean claimed, double echoDistanceSquared) {
        if (!Double.isFinite(ownerDistanceSquared) || ownerDistanceSquared < 0
                || !Double.isFinite(echoDistanceSquared) || echoDistanceSquared < 0
                || echoDistanceSquared > RANGE * RANGE) return false;
        if (!found) return true;
        if (engagedPlayer != bestPlayer) return engagedPlayer;
        if (ownerDistanceSquared != bestOwnerDistance) return ownerDistanceSquared < bestOwnerDistance;
        if (claimed != bestClaimed) return !claimed;
        return echoDistanceSquared < bestEchoDistance;
    }

    public void select(boolean engagedPlayer, double ownerDistanceSquared, boolean claimed, double echoDistanceSquared) {
        found = true;
        bestPlayer = engagedPlayer;
        bestOwnerDistance = ownerDistanceSquared;
        bestClaimed = claimed;
        bestEchoDistance = echoDistanceSquared;
    }

    public static boolean mayAssist(EchoRoaming.Mode mode, double ownerDistanceSquared) {
        return mode != EchoRoaming.Mode.RETURN && ownerDistanceSquared <= EchoRoaming.RADIUS * EchoRoaming.RADIUS;
    }
}
