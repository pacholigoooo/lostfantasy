package dev.lostfantasy.core;

import net.minecraft.util.math.Vec3d;
import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;

public class EchoRoamingTest {
    @Test public void chaseCanLeaveHomeButADeadOrLostTargetRequiresReturnBeforeSearching() {
        EchoRoaming roaming=new EchoRoaming();
        assertEquals(EchoRoaming.Mode.PATROL,roaming.update(false,100*100));
        assertEquals(EchoRoaming.Mode.CHASE,roaming.update(true,140*140));
        assertEquals(EchoRoaming.Mode.CHASE,roaming.update(true,300*300));
        assertEquals(EchoRoaming.Mode.RETURN,roaming.update(false,140*140));
        assertEquals(EchoRoaming.Mode.RETURN,roaming.update(true,99*99));
        assertEquals(EchoRoaming.Mode.PATROL,roaming.update(false,96*96));
        assertEquals(EchoRoaming.Mode.CHASE,roaming.update(true,96*96));
    }
    @Test public void movingOwnerAndThreeDimensionalDistanceDriveTheBoundary() {
        EchoRoaming roaming=new EchoRoaming();Vec3d at=new Vec3d(100,70,0),owner=new Vec3d(0,70,0);
        assertTrue(EchoRoaming.inside(at,owner));assertFalse(EchoRoaming.inside(at.add(0,.1,0),owner));
        assertEquals(EchoRoaming.Mode.RETURN,roaming.update(false,at.squareDistanceTo(owner.add(-20,0,0))));
        Vec3d goal=EchoRoaming.returnPoint(at,owner.add(-20,0,0));
        assertTrue(EchoRoaming.inside(goal,owner.add(-20,0,0)));
        assertEquals(95,goal.distanceTo(owner.add(-20,0,0)),1e-9);
    }
    @Test public void randomWaypointsAndTheirMovementStayBoundedIncludingLargeCoordinates() {
        Random random=new Random(1808);
        for(int trial=0;trial<5000;trial++) {
            Vec3d owner=new Vec3d(random.nextInt(60000000)-30000000,random.nextInt(300),random.nextInt(60000000)-30000000);
            Vec3d at=owner.add(random.nextDouble()*180-90,random.nextDouble()*20-10,random.nextDouble()*180-90);
            if(!EchoRoaming.inside(at,owner))continue;
            Vec3d goal=EchoRoaming.patrolPoint(at,owner,random);
            assertTrue(goal.squareDistanceTo(owner)<=98*98+1e-5);
            Vec3d step=EchoRoaming.step(at,goal,.22,.5);
            assertTrue(step.lengthSquared()<=.22*.22+1e-12);assertTrue(EchoRoaming.inside(at.add(step),owner));
        }
    }
    @Test public void returnStepsConvergeWithoutTeleportingOrOvershooting() {
        Vec3d owner=new Vec3d(4,80,9),at=new Vec3d(304,180,-90);EchoRoaming roaming=new EchoRoaming();
        int ticks=0;
        while(roaming.update(false,at.squareDistanceTo(owner))==EchoRoaming.Mode.RETURN && ticks++<2000) {
            double before=at.squareDistanceTo(owner);
            Vec3d step=EchoRoaming.step(at,EchoRoaming.returnPoint(at,owner),.32,0);
            assertTrue(step.lengthSquared()<=.32*.32+1e-12);
            at=at.add(step);assertTrue(at.squareDistanceTo(owner)<before);
        }
        assertTrue(ticks<2000);assertTrue(at.squareDistanceTo(owner)<=96*96);
        assertEquals(Vec3d.ZERO,EchoRoaming.step(owner,owner,.32,0));
    }
}
