package dev.lostfantasy.core;
import org.junit.Test;
import java.util.UUID;
import static org.junit.Assert.*;

public class EchoAggroTest {
    private final UUID a=new UUID(0,1),b=new UUID(0,2),c=new UUID(0,3),stranger=new UUID(0,4);
    @Test public void combatAllowsOnlyTheTwoPlayersInvolvedAndWorksBothWays() {
        EchoAggro aggro=new EchoAggro();assertFalse(aggro.active(a,b,0));
        aggro.record(a,b,100);
        assertTrue(aggro.active(a,b,100));assertTrue(aggro.active(b,a,100));
        assertFalse(aggro.active(a,stranger,100));assertFalse(aggro.active(stranger,a,100));
    }
    @Test public void thirtySecondsExpiresExactlyAndOnlyBodyCombatRefreshesIt() {
        EchoAggro aggro=new EchoAggro();aggro.record(a,b,0);
        for(int tick=0;tick<600;tick++)assertTrue(aggro.active(a,b,tick));
        assertFalse(aggro.active(a,b,600));
        aggro.record(a,b,1000);aggro.record(b,a,1400);
        assertTrue(aggro.active(a,b,1999));assertFalse(aggro.active(a,b,2000));
    }
    @Test public void multipleOpponentsHaveIndependentExpiry() {
        EchoAggro aggro=new EchoAggro();aggro.record(a,b,0);aggro.record(a,c,300);
        assertFalse(aggro.active(a,b,600));assertTrue(aggro.active(a,c,600));
        assertFalse(aggro.active(b,c,600));
    }
    @Test public void deathLogoutOrDimensionChangeRemovesBothSidesOfThatPlayerOnly() {
        EchoAggro aggro=new EchoAggro();aggro.record(a,b,0);aggro.record(b,c,0);aggro.forget(a);
        assertFalse(aggro.active(a,b,10));assertFalse(aggro.active(b,a,10));assertTrue(aggro.active(b,c,10));
    }
    @Test public void cleanupAndServerStopDoNotLeaveStaleCombat() {
        EchoAggro aggro=new EchoAggro();aggro.record(a,b,0);aggro.record(a,c,400);aggro.prune(600);
        assertFalse(aggro.active(a,b,600));assertTrue(aggro.active(a,c,600));
        aggro.clear();assertFalse(aggro.active(a,c,600));
        aggro.record(a,a,601);aggro.record(null,a,601);assertFalse(aggro.active(a,a,601));
    }
    @Test public void engagedPlayerTakesPriorityOverNearbyMonstersButStillNeedsRange() {
        EchoTargeting selection=new EchoTargeting();
        selection.select(false,1,false,1);
        assertTrue(selection.canImprove(true,900,true,32*32));
        assertFalse(selection.canImprove(true,1,false,32*32+1));
    }

    @Test public void ownerAttackMemoryExpiresAtTenSecondsWithoutGrantingPvpPermission() {
        EchoAggro aggro=new EchoAggro();
        aggro.recordAttack(a,b,100);
        assertEquals(b,aggro.attackTarget(a,299));
        assertNull(aggro.attackTarget(a,300));
        assertFalse(aggro.active(a,b,150));
        assertNull(aggro.attackTarget(b,150));
    }

    @Test public void latestOwnerAttackReplacesAndRefreshesOnlyThatOwnersTarget() {
        EchoAggro aggro=new EchoAggro();
        aggro.recordAttack(a,b,100);
        aggro.recordAttack(c,b,100);
        aggro.recordAttack(a,stranger,250);
        assertEquals(stranger,aggro.attackTarget(a,449));
        assertNull(aggro.attackTarget(c,300));
        aggro.recordAttack(a,stranger,449);
        assertEquals(stranger,aggro.attackTarget(a,648));
        assertNull(aggro.attackTarget(a,649));
    }

    @Test public void targetAndOwnerCleanupRemovesAttackMemoryWithoutTouchingOtherOwners() {
        EchoAggro aggro=new EchoAggro();
        aggro.recordAttack(a,b,10);
        aggro.recordAttack(c,stranger,10);
        aggro.forget(b);
        assertNull(aggro.attackTarget(a,11));
        assertEquals(stranger,aggro.attackTarget(c,11));
        aggro.forget(c);
        assertNull(aggro.attackTarget(c,11));
        aggro.recordAttack(a,b,20);
        aggro.prune(220);
        assertNull(aggro.attackTarget(a,220));
        aggro.recordAttack(a,b,221);
        aggro.clear();
        assertNull(aggro.attackTarget(a,222));
        aggro.recordAttack(a,a,222);
        aggro.recordAttack(null,b,222);
        aggro.recordAttack(a,null,222);
        assertNull(aggro.attackTarget(a,222));
    }
}
