package dev.lostfantasy.core;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import static org.junit.Assert.*;

public class EchoTargetingTest {
    @Test
    public void nearerOwnerWinsOverNearerCloneAndTargetSharing() {
        EchoTargeting selection = new EchoTargeting();
        selection.select(false, 100, false, 1);
        assertTrue(selection.canImprove(false, 4, true, 32 * 32));
        assertFalse(selection.canImprove(false, 121, false, .1));
    }

    @Test
    public void engagedPlayersRemainAheadOfOtherHostiles() {
        EchoTargeting selection = new EchoTargeting();
        selection.select(false, 1, false, 1);
        assertTrue(selection.canImprove(true, 10000, true, 32 * 32));
        selection.select(true, 10000, true, 32 * 32);
        assertFalse(selection.canImprove(false, 0, false, 0));
        assertTrue(selection.canImprove(true, 9, true, 32 * 32));
    }

    @Test
    public void equalOwnerDistancePrefersUnclaimedThenNearerCloneAndKeepsTies() {
        EchoTargeting selection = new EchoTargeting();
        selection.select(false, 25, true, 1);
        assertTrue(selection.canImprove(false, 25, false, 900));
        selection.select(false, 25, false, 900);
        assertTrue(selection.canImprove(false, 25, false, 16));
        assertFalse(selection.canImprove(false, 25, true, 1));
        assertFalse(selection.canImprove(false, 25, false, 900));
    }

    @Test
    public void everyTierStillRequiresFiniteDistancesAndCloneRange() {
        EchoTargeting selection = new EchoTargeting();
        assertTrue(selection.canImprove(true, 40000, true, 32 * 32));
        for (double distance : new double[]{-1, 1024.01, Double.POSITIVE_INFINITY, Double.NaN}) {
            assertFalse(selection.canImprove(true, 1, false, distance));
        }
        for (double distance : new double[]{-1, Double.POSITIVE_INFINITY, Double.NaN}) {
            assertFalse(selection.canImprove(false, distance, false, 1));
        }
    }

    @Test
    public void priorityPruningMatchesFullSortingForRandomVisibleCandidates() {
        Random random = new Random(731);
        Comparator<Candidate> order = Comparator.comparingInt((Candidate candidate) -> candidate.player ? 0 : 1)
                .thenComparingDouble(candidate -> candidate.ownerDistance)
                .thenComparingInt(candidate -> candidate.claimed ? 1 : 0)
                .thenComparingDouble(candidate -> candidate.echoDistance);
        for (int trial = 0; trial < 2000; trial++) {
            List<Candidate> candidates = new ArrayList<>();
            for (int i = 0; i < 64; i++) {
                candidates.add(new Candidate(random.nextBoolean(), random.nextInt(20000),
                        random.nextBoolean(), random.nextInt(1800), random.nextBoolean()));
            }
            Candidate expected = candidates.stream()
                    .filter(candidate -> candidate.visible && candidate.echoDistance <= EchoTargeting.RANGE * EchoTargeting.RANGE)
                    .min(order).orElse(null);
            assertSame(expected, pruned(candidates));
        }
    }

    @Test
    public void blockedTopCandidateLeavesVisibleFallbackAvailable() {
        List<Candidate> candidates = new ArrayList<>();
        candidates.add(new Candidate(true, 1, false, 1, false));
        candidates.add(new Candidate(false, 4, true, 25, true));
        candidates.add(new Candidate(false, 9, false, 1, true));
        assertSame(candidates.get(1), pruned(candidates));
    }

    @Test
    public void onlyHomeRangeChasesAndPatrolsMayAssistOwner() {
        assertTrue(EchoTargeting.mayAssist(EchoRoaming.Mode.PATROL, 100 * 100));
        assertTrue(EchoTargeting.mayAssist(EchoRoaming.Mode.CHASE, 100 * 100));
        assertFalse(EchoTargeting.mayAssist(EchoRoaming.Mode.CHASE, 100 * 100 + .01));
        assertFalse(EchoTargeting.mayAssist(EchoRoaming.Mode.RETURN, 99 * 99));
        assertFalse(EchoTargeting.mayAssist(EchoRoaming.Mode.RETURN, 95 * 95));
    }

    private Candidate pruned(List<Candidate> candidates) {
        EchoTargeting selection = new EchoTargeting();
        Candidate best = null;
        for (Candidate candidate : candidates) {
            if (selection.canImprove(candidate.player, candidate.ownerDistance, candidate.claimed, candidate.echoDistance)
                    && candidate.visible) {
                selection.select(candidate.player, candidate.ownerDistance, candidate.claimed, candidate.echoDistance);
                best = candidate;
            }
        }
        return best;
    }

    private static final class Candidate {
        final boolean player;
        final double ownerDistance;
        final boolean claimed;
        final double echoDistance;
        final boolean visible;

        Candidate(boolean player, double ownerDistance, boolean claimed, double echoDistance, boolean visible) {
            this.player = player;
            this.ownerDistance = ownerDistance;
            this.claimed = claimed;
            this.echoDistance = echoDistance;
            this.visible = visible;
        }
    }
}
