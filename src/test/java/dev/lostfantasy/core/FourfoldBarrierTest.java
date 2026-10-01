package dev.lostfantasy.core;

import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.network.EffectMessage;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;
import static org.junit.Assert.*;

public class FourfoldBarrierTest {
    @Test public void protectionAttackAndRecoveryHaveDistinctEndpoints() {
        int hits=0;
        for(int age=0;age<52;age++) if(FourfoldBarrier.smallHit(age)) hits++;
        assertEquals(8,hits);
        assertTrue(FourfoldBarrier.smallHit(6)); assertTrue(FourfoldBarrier.smallHit(34));
        assertFalse(FourfoldBarrier.smallHit(38)); assertFalse(FourfoldBarrier.pulling(38));
        assertTrue(Spell.FOURFOLD_BARRIER.protectsCaster(21,52));
        assertFalse(Spell.FOURFOLD_BARRIER.protectsCaster(22,52));
        assertTrue(FourfoldBarrier.restricted(42)); assertTrue(FourfoldBarrier.restricted(51));
        assertFalse(FourfoldBarrier.restricted(52)); assertFalse(FourfoldBarrier.restricted(-1));
        assertEquals(1,FourfoldBarrier.pose(41),0); assertEquals(0,FourfoldBarrier.pose(52),0);
    }
    @Test public void deadZoneRearFlanksAndHeightAreExcludedAtEveryYaw() {
        for(int angle=0;angle<360;angle+=15) {
            double dx=Math.sin(Math.toRadians(angle)), dz=Math.cos(Math.toRadians(angle));
            assertTrue(point(dx,dz,dx*3,0,dz*3,false));
            assertFalse(point(dx,dz,dx*.8,0,dz*.8,false));
            assertFalse(point(dx,dz,-dx*3,0,-dz*3,false));
            assertFalse(point(dx,dz,dx*3+dz*3,0,dz*3-dx*3,false));
            assertFalse(point(dx,dz,dx*3,3.5,dz*3,false));
            assertTrue(point(dx,dz,dx*6,0,dz*6,true));
            assertFalse(point(dx,dz,dx*6,0,dz*6,false));
        }
    }
    private boolean point(double dx,double dz,double x,double y,double z,boolean pull) {
        return FourfoldBarrier.intersects(dx,dz,x-.1,y,z-.1,x+.1,y+.2,z+.1,pull);
    }
    @Test public void allFourPatternsStayWithinTheAttackFootprint() {
        for(int layer=0;layer<4;layer++)for(int corner=0;corner<4;corner++) {
            double[] p=FourfoldBarrier.corner(layer,corner);
            assertTrue(Math.abs(p[0])<=2.5); assertTrue(Math.abs(p[1])<=1.5);
        }
    }
    @Test public void fifthSpellIsOptInAndPersistsWithoutIncreasingSlotsOrChangingOtherLearning() {
        PlayerData data=new PlayerData(); data.grantStage(dev.lostfantasy.core.Growth.Route.YOUKAI,2);
        assertFalse(data.qualifies(Spell.FOURFOLD_BARRIER)); data.grantStage(dev.lostfantasy.core.Growth.Route.YOUKAI,3);
        assertTrue(data.qualifies(Spell.FOURFOLD_BARRIER));
        for(Spell spell:new Spell[]{Spell.ROYAL_FLARE,Spell.ABANDONED_TRAIN,Spell.GUNGNIR,Spell.FOUR_OF_A_KIND}) data.learn(spell);
        int[] oldSlots=data.preparedSlots().clone(); assertTrue(data.learn(Spell.FOURFOLD_BARRIER,false));
        assertArrayEquals(oldSlots,data.preparedSlots()); assertFalse(data.learn(Spell.FOURFOLD_BARRIER,false));
        assertTrue(data.equipSpell(1,4));
        PlayerData restored=new PlayerData(); restored.deserializeNBT(data.serializeNBT());
        assertTrue(restored.knows(Spell.FOURFOLD_BARRIER)); assertEquals(4,restored.preparedSlots().length);
        assertEquals(4,restored.spellInSlot(1)); assertEquals(5,restored.serializeNBT().getTagList("learnedSpells",8).tagCount());
        EffectMessage effect=new EffectMessage(); effect.kind=4;
        assertSame(Spell.FOURFOLD_BARRIER,effect.spell()); assertTrue(effect.hasCastingPose());
        SpellPage pages=new SpellPage(Spell.catalog().stream().mapToInt(spell->spell.networkId).toArray());
        assertEquals(2,pages.pageCount()); pages.move(1); assertEquals(4,pages.spellIdAt(0));
    }
    @Test public void studyingDoesNotAutomaticallyPrepareEvenWhenASlotIsEmpty() {
        PlayerData data=new PlayerData(); data.learn(Spell.FOURFOLD_BARRIER,false);
        assertArrayEquals(new int[]{-1,-1,-1,-1},data.preparedSlots());
    }
}
