package dev.lostfantasy.world;

import dev.lostfantasy.Balance;
import dev.lostfantasy.core.Facing;
import dev.lostfantasy.core.RiverJourney;
import net.minecraft.util.math.BlockPos;
import org.junit.Test;
import static org.junit.Assert.*;

public class HiganTerrainTest {
    @Test public void bothDirectionsFollowTheRiverAndReachTheirLanding() {
        BlockPos near=HiganTerrain.landing(false),far=HiganTerrain.landing(true);
        assertEquals(new BlockPos(-3270,73,-2160),near);
        assertEquals(new BlockPos(-4450,73,-2160),far);
        assertFalse(HiganTerrain.farSide(near.getX(),near.getZ()));
        assertTrue(HiganTerrain.farSide(far.getX(),far.getZ()));

        for(boolean fromFarBank:new boolean[]{false,true}) {
            float previous=HiganTerrain.boatYaw(0,fromFarBank);
            double lastX=HiganTerrain.boatX(0,fromFarBank),lastZ=HiganTerrain.boatZ(0,fromFarBank);
            for(int tick=0;tick<=RiverJourney.DURATION;tick++) {
                double x=HiganTerrain.boatX(tick,fromFarBank),z=HiganTerrain.boatZ(tick,fromFarBank);
                float yaw=HiganTerrain.boatYaw(tick,fromFarBank);
                assertTrue(Float.isFinite(yaw));
                assertTrue(Math.abs(Facing.wrap(yaw-previous))<1);
                assertTrue(HiganTerrain.bankDistance(x,z)<0);
                assertTrue(HiganWorld.inside(Balance.gensokyoDimensionId,x,z));
                if(tick<RiverJourney.DURATION) {
                    double dx=HiganTerrain.boatX(tick+1,fromFarBank)-x;
                    double dz=HiganTerrain.boatZ(tick+1,fromFarBank)-z;
                    assertEquals(Math.toDegrees(Math.atan2(-dx,dz)),yaw,.2);
                    assertTrue(Math.hypot(x-lastX,z-lastZ)<1);
                }
                previous=yaw;lastX=x;lastZ=z;
            }
            BlockPos source=fromFarBank?far:near,target=fromFarBank?near:far;
            assertTrue(HiganTerrain.boatX(0,fromFarBank)-source.getX()>=0);
            assertTrue(Math.abs(HiganTerrain.boatX(RiverJourney.DURATION,fromFarBank)-target.getX())<=4);
            assertEquals(target.getZ(),HiganTerrain.boatZ(RiverJourney.DURATION,fromFarBank),0);
        }
    }

    @Test public void regionalBoundaryExcludesTheMarketAndShrine() {
        for(dev.lostfantasy.world.gensokyo.GensokyoAtlas site:new dev.lostfantasy.world.gensokyo.GensokyoAtlas[]{
                dev.lostfantasy.world.gensokyo.GensokyoAtlas.LIMINAL_ROAD,
                dev.lostfantasy.world.gensokyo.GensokyoAtlas.HAKUREI})
            assertFalse(HiganWorld.inside(Balance.gensokyoDimensionId,site.x,site.z));
        assertFalse(HiganWorld.inside(0,HiganTerrain.boatX(0),HiganTerrain.boatZ(0)));
        assertTrue(HiganTerrain.halfWidth(HiganTerrain.boatZ(0))*2>1000);
    }
}
