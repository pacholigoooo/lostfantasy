package dev.lostfantasy.entity;

import dev.lostfantasy.world.HiganTerrain;
import dev.lostfantasy.core.RiverJourney;
import net.minecraft.util.math.MathHelper;
import org.junit.Test;
import static org.junit.Assert.*;

public class FerryHeadingTest {
    @Test public void departureAcceleratesAndSettlesWithoutSnappingOrOvershooting() {
        FerryHeading steering=new FerryHeading();float yaw=0,oldSpeed=0;
        for(int tick=0;tick<160;tick++) {
            float next=steering.turn(yaw,-70),speed=next-yaw;
            assertTrue(Math.abs(speed)<=1.8001f);
            assertTrue(Math.abs(speed-oldSpeed)<=.1801f);
            assertTrue(next<=yaw && next>=-70);
            oldSpeed=speed;yaw=next;
        }
        assertEquals(-70,yaw,.001);
    }
    @Test public void angleWrapUsesShortTurnInBothDirections() {
        for(int sign:new int[]{-1,1}) {
            FerryHeading steering=new FerryHeading();float yaw=179*sign;
            for(int tick=0;tick<80;tick++) {
                float next=steering.turn(yaw,-179*sign);
                assertTrue((next-yaw)*sign>=0);assertTrue(Math.abs(next-yaw)<.5);
                yaw=next;
            }
            assertEquals(0,MathHelper.wrapDegrees(yaw+179*sign),.001);
        }
    }
    @Test public void quantizedNetworkAnglesStaySmoothAndFollowTheWholeRoute() {
        FerryHeading steering=new FerryHeading();float yaw=HiganTerrain.boatYaw(0),packet=yaw;
        for(int tick=0;tick<=RiverJourney.DURATION;tick++) {
            if(tick%3==0)packet=(float)(Math.floor(HiganTerrain.boatYaw(tick)*256/360)*360/256);
            float next=steering.turn(yaw,packet);
            assertTrue("Packet angle caused a snap at "+tick,Math.abs(next-yaw)<.4);
            assertTrue(Math.abs(next-HiganTerrain.boatYaw(tick))<2);
            yaw=next;
        }
    }
}
