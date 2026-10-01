package dev.lostfantasy.core;
import org.junit.Test;
import static org.junit.Assert.*;
public class RulesTest {
    @Test public void royalFlareHitsTheWholeDomainIncludingSpaceBetweenRays() {
        assertTrue(Rules.sphereIntersectsBox(0,4.2,0,50,12,0,17,13,2,18));
        assertTrue(Rules.sphereIntersectsBox(0,0,0,50,-1,-1,-50,1,1,-49));
        assertTrue(Rules.sphereIntersectsBox(0,0,0,50,49.9,-1,-1,51,1,1));
        assertFalse(Rules.sphereIntersectsBox(0,0,0,50,50.1,-1,-1,51,1,1));
        assertFalse(Rules.sphereIntersectsBox(0,0,0,50,40,40,40,41,41,41));
    }
    @Test public void domainMathRejectsInvalidRadiusAndHandlesInterior() {
        assertTrue(Rules.sphereIntersectsBox(0,0,0,0,-1,-1,-1,1,1,1));
        assertFalse(Rules.sphereIntersectsBox(0,0,0,-1,-1,-1,-1,1,1,1));
        assertFalse(Rules.sphereIntersectsBox(0,0,0,Double.NaN,-1,-1,-1,1,1,1));
    }
    @Test public void capacityOnlyChangesAtPermanentThresholds() {
        int[] levels={100,350,800,1500};
        assertEquals(2,Rules.spiritCapacity(0,levels));assertEquals(2,Rules.spiritCapacity(99,levels));
        assertEquals(3,Rules.spiritCapacity(100,levels));assertEquals(5,Rules.spiritCapacity(1499,levels));assertEquals(6,Rules.spiritCapacity(Long.MAX_VALUE,levels));
    }
    @Test public void powerRewardsAreBoundedEvenForBosses() {
        assertEquals(1,Rules.powerDrop(20,40));assertEquals(2,Rules.powerDrop(41,40));assertEquals(5,Rules.powerDrop(100000,40));assertEquals(0,Rules.powerDrop(Float.NaN,40));
    }
    @Test public void shieldAddsPercentagePointsAndCannotHealVictim() {
        assertEquals(5,Rules.remainingDamage(100,20,.15f),.0001);
        assertEquals(0,Rules.remainingDamage(100,10,.15f),.0001);
        assertEquals(0,Rules.remainingDamage(100,100,2),.0001);
        assertEquals(20,Rules.remainingDamage(100,20,0),.0001);
    }
    @Test public void invalidAndDuplicateLoadoutsAreRejected() {
        java.util.function.IntPredicate learned=id -> id>=0 && id<4;
        java.util.function.IntPredicate ultimate=id -> id==1 || id==2;
        assertTrue(Rules.validLoadout(new int[]{0,1,3,-1},learned,ultimate));
        assertFalse(Rules.validLoadout(new int[]{1,2,-1,-1},learned,ultimate));
        assertFalse(Rules.validLoadout(new int[]{0,0,-1,-1},learned,ultimate));
        assertFalse(Rules.validLoadout(new int[]{0,1,-1,-1},id -> id==0,ultimate));
        assertFalse(Rules.validLoadout(new int[]{Integer.MAX_VALUE,-1,-1,-1},learned,ultimate));
        assertFalse(Rules.validLoadout(new int[]{-2,-1,-1,-1},learned,ultimate));
        assertFalse(Rules.validLoadout(new int[5],learned,ultimate));
    }
    @Test public void sparseSpellIdsBeyondTheOldBitMaskStillUseExactlyFourSlots() {
        java.util.function.IntPredicate learned=id -> id==0 || id==31 || id==64 || id==1000;
        assertTrue(Rules.validLoadout(new int[]{0,31,64,1000},learned,id -> id==64));
        assertFalse(Rules.validLoadout(new int[]{31,31,-1,-1},learned,id -> false));
        assertFalse(Rules.validLoadout(new int[]{31,64,-1,-1},learned,id -> id==31 || id==64));
        assertFalse(Rules.validLoadout(new int[]{0,31,64,1000,-1},learned,id -> false));
    }
    @Test public void beamsStopAtSegmentEndpoints() {
        assertEquals(4,Rules.distanceToSegmentSquared(-2,0,0,0,0,0,10,0,0),.0001);
        assertEquals(9,Rules.distanceToSegmentSquared(13,0,0,0,0,0,10,0,0),.0001);
        assertEquals(1,Rules.distanceToSegmentSquared(5,1,0,0,0,0,10,0,0),.0001);
        assertEquals(14,Rules.distanceToSegmentSquared(1,2,3,0,0,0,0,0,0),.0001);
    }
    @Test public void bookProgressionCannotSkipStages() {
        assertTrue(Rules.canLearnBook(0,1,2));assertFalse(Rules.canLearnBook(0,3,6));
        assertFalse(Rules.canLearnBook(1,2,3));assertTrue(Rules.canLearnBook(2,3,6));assertFalse(Rules.canLearnBook(3,3,6));
    }
}
