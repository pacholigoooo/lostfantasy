package dev.lostfantasy.core;

import org.junit.Test;
import static org.junit.Assert.*;
import static dev.lostfantasy.core.Growth.Route.*;

public class ReincarnationTest {
    @Test public void newbornCanUpgradeOneRouteButCannotEnterAnotherCategoryOrRace() {
        for(Growth.Route first:Growth.Route.values()) {
            Growth g=new Growth();assertTrue(g.advance(first,1));assertTrue(g.advance(first,2));
            for(Growth.Route next:Growth.Route.values())if(next!=first)assertFalse(g.advance(next,1));
            assertEquals(1,g.count());assertFalse(g.reincarnated());
        }
    }
    @Test public void magicProfessionNeverAddsASpecies() {
        Growth g=new Growth();g.advance(MAGIC,3);assertEquals(1,g.professions().size());assertTrue(g.races().isEmpty());
    }
    @Test public void oneRebirthAllowsBothSpeciesAndTheProfessionWithoutAnotherJourney() {
        Growth g=new Growth();g.advance(YOUKAI,2);g.reincarnate(false);
        assertEquals(0,g.count());assertTrue(g.reincarnated());
        assertTrue(g.advance(YOUKAI,1));assertTrue(g.advance(VAMPIRE,1));assertTrue(g.advance(MAGIC,1));assertEquals(3,g.count());
    }
}
