package dev.lostfantasy.core;

import dev.lostfantasy.data.PlayerData;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;
import static org.junit.Assert.*;

public class ResearchProgressTest {
    @Test public void evidenceMustFollowDiscoveryAndBothRecordsAreNeededInEitherOrder() {
        for(boolean reversed:new boolean[]{false,true}) {
            ResearchProgress p=new ResearchProgress();
            assertFalse(p.compare(ResearchProgress.SAMPLE,2));assertFalse(p.complete());
            assertTrue(p.discover());assertFalse(p.discover());
            assertFalse(p.compare(ResearchProgress.GROWTH,0));assertEquals(1,p.flags());
            assertTrue(p.compare(reversed?ResearchProgress.GROWTH:ResearchProgress.SAMPLE,reversed?1:2));
            assertFalse(p.ready());assertFalse(p.complete());
            assertTrue(p.compare(reversed?ResearchProgress.SAMPLE:ResearchProgress.GROWTH,reversed?2:1));
            assertTrue(p.ready());assertTrue(p.complete());assertFalse(p.complete());
            assertEquals(15,p.flags());assertFalse(p.compare(ResearchProgress.SAMPLE,2));
        }
    }
    @Test public void progressSurvivesSaveDeathAndStateSyncWithoutTeachingOrPromoting() {
        PlayerData data=new PlayerData();data.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,2);data.setPower(3);data.research.discover();data.research.compare(2,2);
        PlayerData after=new PlayerData();after.deserializeNBT(data.serializeNBT());
        assertEquals(3,after.research.flags());after.resetAfterDeath();assertEquals(3,after.research.flags());
        after.research.compare(4,1);assertTrue(after.research.complete());
        assertEquals(2,after.magic());assertEquals(0,after.absorbedXp());assertEquals(0,after.youkaiProgress());
        assertFalse(after.knows(Spell.EMERALD_CITY));assertFalse(after.knows(Spell.ROYAL_FLARE));
        assertArrayEquals(new int[]{-1,-1,-1,-1},after.preparedSlots());
        data.deserializeNBT(after.serializeNBT());assertEquals(15,data.research.flags());
    }
    @Test public void differentPlayersNeverShareEvidenceOrTheCompletionTransition() {
        PlayerData first=new PlayerData(),second=new PlayerData();
        first.research.discover();first.research.compare(2,2);first.research.compare(4,1);assertTrue(first.research.complete());
        assertEquals(0,second.research.flags());assertFalse(second.research.complete());
        second.research.discover();second.research.compare(4,1);second.research.compare(2,2);assertTrue(second.research.complete());
        assertFalse(first.research.complete());
    }
    @Test public void legacyAndMalformedSavesCannotSkipMissingEvidence() {
        PlayerData data=new PlayerData();data.research.discover();data.deserializeNBT(new NBTTagCompound());assertEquals(0,data.research.flags());
        for(int invalid:new int[]{-1,2,4,6,8,14,16,Integer.MAX_VALUE}) {
            ResearchProgress p=new ResearchProgress();p.restore(invalid);assertEquals(0,p.flags());
        }
        ResearchProgress p=new ResearchProgress();p.restore(9);assertEquals(1,p.flags());
        p.restore(11);assertEquals(3,p.flags());p.restore(15);assertTrue(p.has(8));
        assertFalse(p.compare(8,0));assertFalse(p.compare(4,-1));assertFalse(p.compare(4,2));
    }
}
