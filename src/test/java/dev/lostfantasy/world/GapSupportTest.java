package dev.lostfantasy.world;

import net.minecraft.util.math.AxisAlignedBB;
import org.junit.Test;
import static org.junit.Assert.*;

public class GapSupportTest {
    @Test public void gravityStopsAtEntryHeightAndHorizontalMovementIsUnchanged() {
        AxisAlignedBB player=new AxisAlignedBB(-.3,65,-.3,.3,66.8,.3);
        AxisAlignedBB floor=GapSupport.surface(player.expand(3,-.08,-4));
        assertNotNull(floor);
        assertEquals(0,floor.calculateYOffset(player,-.08),0);
        assertEquals(3,floor.calculateXOffset(player,3),0);
        assertEquals(-4,floor.calculateZOffset(player,-4),0);
    }
    @Test public void jumpingCanRiseAndLandEvenAtLargeHorizontalCoordinates() {
        AxisAlignedBB air=new AxisAlignedBB(999999,66,2000000,999999.6,67.8,2000000.6);
        assertNull(GapSupport.surface(air.expand(0,.42,0)));
        AxisAlignedBB floor=GapSupport.surface(air.expand(0,-4,0));
        assertEquals(-1,floor.calculateYOffset(air,-4),0);
        assertEquals(.42,floor.calculateYOffset(air,.42),0);
    }
    @Test public void queriesFarAboveOrBelowTheSurfaceDoNotGainColliders() {
        assertNull(GapSupport.surface(new AxisAlignedBB(0,120,0,1,122,1)));
        assertNull(GapSupport.surface(new AxisAlignedBB(0,-200,0,1,-198,1)));
    }
}
