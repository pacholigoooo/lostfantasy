package dev.lostfantasy.core;
import org.junit.Test;
import static org.junit.Assert.*;

public class CastMotionTest {
    @Test public void trainHeadLooksSlightlyRightOfThePointingHandAndRecovers() {
        for (int track=-180;track<=180;track+=15) {
            assertEquals(15,Facing.wrap(CastMotion.trainHeadYaw(track,track,25)-track),1e-5);
            assertEquals(-75,Facing.wrap(CastMotion.trainHeadYaw(track,track,25)
                    -CastMotion.trainBodyYaw(track,track,25)),1e-5);
        }
        assertEquals(47,CastMotion.trainHeadYaw(47,10,0),0);
        assertEquals(47,CastMotion.trainHeadYaw(47,10,CastMotion.TRAIN_DURATION),0);
    }
    @Test public void trainTurnsBeforeReleaseThenLowersTheArmAtTheEnd() {
        assertEquals(35,CastMotion.trainBodyYaw(35,35,0),0);
        assertEquals(125,CastMotion.trainBodyYaw(35,35,18),1e-5);
        assertEquals(0,CastMotion.trainPoint(5),0);
        assertEquals(1,CastMotion.trainPoint(CastMotion.TRAIN_CHARGE),0);
        assertEquals(1,CastMotion.trainPoint(CastMotion.TRAIN_DURATION-10),0);
        assertEquals(0,CastMotion.trainPoint(CastMotion.TRAIN_DURATION),0);
        assertEquals(0,CastMotion.trainRecovery(CastMotion.TRAIN_DURATION),0);
    }

    @Test public void trainLeftArmExtendsOutwardsAndPointsDownTheTrackAtEveryHeading() {
        for(int heading=-180;heading<=180;heading+=15) {
            double body=Math.toRadians(CastMotion.trainBodyYaw(heading,heading,25));
            double arm=Math.toRadians(Facing.wrap(heading-(float)Math.toDegrees(body)));
            assertEquals(-Math.PI/2,arm,1e-5);
            // ModelRenderer rotates Y after X; RenderLivingBase then flips X/Y and rotates 180-body.
            double modelX=-Math.sin(arm),modelZ=-Math.cos(arm);
            assertTrue("The left hand must stay outside the left shoulder",modelX>.99);
            double x=-modelX*Math.cos(Math.PI-body)+modelZ*Math.sin(Math.PI-body);
            double z=modelX*Math.sin(Math.PI-body)+modelZ*Math.cos(Math.PI-body);
            assertEquals(-Math.sin(Math.toRadians(heading)),x,1e-5);
            assertEquals(Math.cos(Math.toRadians(heading)),z,1e-5);
        }
    }

    @Test public void flareHandsAndSunRiseTogetherAndHoldAfterCharge() {
        double previousHeight=0,previousRadius=0;
        for(int tick=0;tick<=40;tick++) {
            double height=CastMotion.flareHandHeight(tick,40),radius=CastMotion.flareRadius(tick,40);
            assertTrue(height>=previousHeight);assertTrue(radius>=previousRadius);
            assertTrue(CastMotion.flareSpread(tick,40)<Math.PI);
            assertTrue("The outer halo must clear the player's head",CastMotion.flareSunHeight(tick,40)-1.55*radius>2);
            previousHeight=height;previousRadius=radius;
        }
        assertEquals(CastMotion.flareHandHeight(40,40),CastMotion.flareHandHeight(200,40),0);
        assertEquals(CastMotion.flareRadius(40,40),CastMotion.flareRadius(200,40),0);
        assertEquals(2,CastMotion.flareLift(40,40),0);
        assertTrue(CastMotion.flareSpread(40,40)>Math.PI/2);
        assertTrue(CastMotion.flareHandHeight(40,40)>1.8);
    }
    @Test public void attacksNeverTravelDuringTheirWindup() {
        assertEquals(0,CastMotion.trainTravel(CastMotion.TRAIN_CHARGE-.1),0);
        assertEquals(0,CastMotion.spearTravel(CastMotion.SPEAR_RELEASE-.1),0);
        assertEquals(60,CastMotion.spearTravel(CastMotion.SPEAR_DURATION),.0001);
        assertTrue(CastMotion.trainTravel(CastMotion.TRAIN_DURATION)>48+CastMotion.TRAIN_LENGTH);
    }
    @Test public void spearArmHasWindupReleaseAndRecoveryWithoutDiscontinuities() {
        assertEquals(0,CastMotion.spearArm(0),0);
        assertTrue(CastMotion.spearArm(8)<-2.5);
        assertTrue(CastMotion.spearArm(12)>CastMotion.spearArm(8));
        assertEquals(0,CastMotion.spearArm(26),0);
        assertEquals(CastMotion.spearArm(12-.0001),CastMotion.spearArm(12+.0001),.0001);
    }
    @Test public void trainSweepRejectsBlocksBesideTrackAndBehindGap() {
        assertTrue(CastMotion.trainIntersects(1,1,.5,.5,0,1,0,2));
        assertFalse(CastMotion.trainIntersects(2.1,1,.5,.5,0,1,0,2));
        assertFalse(CastMotion.trainIntersects(0,-1,.5,.5,0,1,0,2));
        assertFalse(CastMotion.trainIntersects(0,3,.5,.5,0,1,0,2));
    }
    @Test public void diagonalTrainUsesAnOrientedSweepInsteadOfItsHugeBoundingBox() {
        double d=Math.sqrt(.5);
        assertTrue(CastMotion.trainIntersects(10*d,10*d,.5,.5,d,d,0,20));
        assertFalse(CastMotion.trainIntersects(0,14,.5,.5,d,d,0,20));
        assertTrue(CastMotion.trainIntersects(-10*d,10*d,.5,.5,-d,d,0,20));
    }
}
