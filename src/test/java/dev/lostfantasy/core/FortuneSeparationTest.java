package dev.lostfantasy.core;

import dev.lostfantasy.data.PlayerData;
import org.junit.Test;
import static org.junit.Assert.*;

public class FortuneSeparationTest {
    @Test public void repeatedUseAndReloadCannotRestartTheSameSeparation() {
        PlayerData data=new PlayerData();data.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,3);data.setPower(4);data.learn(Spell.ROYAL_FLARE);
        assertTrue(data.journey.separateFortune());assertFalse(data.journey.separateFortune());
        PlayerData loaded=new PlayerData();loaded.deserializeNBT(data.serializeNBT());
        assertTrue(loaded.journey.fortuneSeparated());assertFalse(loaded.journey.separateFortune());
        assertEquals(3,loaded.magic());assertEquals(4,loaded.power());assertTrue(loaded.knows(Spell.ROYAL_FLARE));
        loaded.journey.arrive();assertFalse(loaded.journey.separateFortune());
        assertTrue(loaded.journey.board(false));
        for(int i=0;i<RiverJourney.DURATION;i++)loaded.journey.advance(true);
        assertTrue(loaded.journey.ready());assertTrue(loaded.journey.finishCrossing());
        assertEquals(RiverJourney.Phase.SHORE,loaded.journey.phase());assertTrue(loaded.journey.fortuneSeparated());
        loaded.journey.leave();assertTrue(loaded.journey.fortuneSeparated());
    }
    @Test public void activeVoyageCannotStartSeparation() {
        RiverJourney journey=new RiverJourney();journey.arrive();
        assertFalse(journey.separateFortune());assertFalse(journey.fortuneSeparated());
        assertTrue(journey.board(false));assertFalse(journey.separateFortune());
    }
}
