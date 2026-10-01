package dev.lostfantasy.core;

import dev.lostfantasy.data.PlayerData;
import org.junit.Test;
import static org.junit.Assert.*;

public class ResearchLearningTest {
    @Test public void everyIncompleteInvestigationStageRejectsStudyEvenForAnArchmage() {
        for(int flags:new int[]{0,1,3,5,7}) {
            PlayerData data=new PlayerData();data.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,4);data.research.restore(flags);data.dirty=false;
            assertFalse("flags "+flags,data.canStudy(Spell.EMERALD_CITY));
            assertFalse(data.learnFromStudy(Spell.EMERALD_CITY));
            assertFalse(data.knows(Spell.EMERALD_CITY));assertFalse(data.dirty);
            assertEquals(flags,data.research.flags());assertArrayEquals(new int[]{-1,-1,-1,-1},data.preparedSlots());
        }
    }
    @Test public void bothComparisonsStillNeedTableCompletionAndMagicThree() {
        PlayerData data=new PlayerData();data.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,3);data.research.discover();
        assertTrue(data.research.compare(ResearchProgress.SAMPLE,ResearchEvidence.SAMPLE_MATCH));
        assertFalse(data.learnFromStudy(Spell.EMERALD_CITY));
        assertTrue(data.research.compare(ResearchProgress.GROWTH,ResearchEvidence.GROWTH_MATCH));
        assertTrue(data.research.ready());assertFalse(data.learnFromStudy(Spell.EMERALD_CITY));
        assertTrue(data.research.complete());data.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,2);
        assertFalse(data.learnFromStudy(Spell.EMERALD_CITY));data.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,3);
        assertTrue(data.canStudy(Spell.EMERALD_CITY));assertTrue(data.learnFromStudy(Spell.EMERALD_CITY));
        assertTrue(data.knows(Spell.EMERALD_CITY));assertFalse(data.learnFromStudy(Spell.EMERALD_CITY));
        assertEquals(3,data.magic());assertFalse(data.knows(Spell.ROYAL_FLARE));
        assertArrayEquals(new int[]{-1,-1,-1,-1},data.preparedSlots());
    }
    @Test public void completedInvestigationPersistsButDoesNotGrantAnotherPlayersStudy() {
        PlayerData first=new PlayerData();first.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,3);first.research.restore(15);
        PlayerData reloaded=new PlayerData();reloaded.deserializeNBT(first.serializeNBT());reloaded.resetAfterDeath();
        assertTrue(reloaded.learnFromStudy(Spell.EMERALD_CITY));
        PlayerData second=new PlayerData();second.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,3);assertFalse(second.learnFromStudy(Spell.EMERALD_CITY));
    }
    @Test public void administrativeGrantsAndOtherSpellStudyKeepTheirOwnRules() {
        PlayerData data=new PlayerData();data.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,4);assertTrue(data.learn(Spell.EMERALD_CITY));
        assertTrue(data.knows(Spell.EMERALD_CITY));assertTrue(data.qualifies(Spell.EMERALD_CITY));assertEquals(0,data.research.flags());
        data.grantStage(dev.lostfantasy.core.Growth.Route.YOUKAI,3);assertTrue(data.learnFromStudy(Spell.FOURFOLD_BARRIER));
    }
}
