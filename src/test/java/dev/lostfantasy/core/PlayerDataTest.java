package dev.lostfantasy.core;
import dev.lostfantasy.data.PlayerData;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;
import static org.junit.Assert.*;
public class PlayerDataTest {
    @Test public void returningWithAKeyCountsAsEscapeOnlyForTheCurrentCaptivity() {
        PlayerData voluntary=new PlayerData();voluntary.hasReturn=true;
        assertFalse(voluntary.finishGapReturn());
        assertFalse(voluntary.hasReturn);
        PlayerData captive=new PlayerData();captive.trappedInGap=true;captive.hasReturn=true;
        PlayerData rejoined=new PlayerData();rejoined.deserializeNBT(captive.serializeNBT());
        assertTrue(rejoined.finishGapReturn());
        assertFalse(rejoined.trappedInGap);assertFalse(rejoined.hasReturn);
        assertFalse(rejoined.finishGapReturn());
        captive.resetAfterDeath();captive.hasReturn=true;
        assertFalse("Past captivity ended by death must not count on a later voluntary visit",captive.finishGapReturn());
    }
    @Test public void echoCooldownDoesNotBlockOtherSpellsAndSurvivesOnlyUntilDeath() {
        PlayerData d=new PlayerData();d.echoCooldown=100;
        assertEquals(0,d.cooldownFor(Spell.ROYAL_FLARE));assertEquals(0,d.cooldownFor(Spell.GUNGNIR));
        assertEquals(100,d.cooldownFor(Spell.FOUR_OF_A_KIND));
        d.cooldown=150;assertEquals(150,d.cooldownFor(Spell.FOUR_OF_A_KIND));
        PlayerData copy=new PlayerData();copy.deserializeNBT(d.serializeNBT());assertEquals(100,copy.echoCooldown);
        copy.resetAfterDeath();assertEquals(0,copy.echoCooldown);
        copy.deserializeNBT(new NBTTagCompound());assertEquals(0,copy.echoCooldown);
    }
    @Test public void eachFinalStageIndependentlyUnlocksFlightButCaptivityDoesNot() {
        PlayerData d=new PlayerData();d.trappedInGap=true;d.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,3);d.grantStage(dev.lostfantasy.core.Growth.Route.YOUKAI,3);d.grantStage(dev.lostfantasy.core.Growth.Route.VAMPIRE,3);assertFalse(d.hasFinalStage());
        d.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,4);assertTrue(d.hasFinalStage());d.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,3);d.grantStage(dev.lostfantasy.core.Growth.Route.YOUKAI,4);assertTrue(d.hasFinalStage());
        d.grantStage(dev.lostfantasy.core.Growth.Route.YOUKAI,3);d.grantStage(dev.lostfantasy.core.Growth.Route.VAMPIRE,4);assertTrue(d.hasFinalStage());
    }
    @Test public void flightOwnershipAndFinalStageSurviveSaveAndDeath() {
        PlayerData before=new PlayerData();before.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,4);before.flightGranted=true;
        PlayerData after=new PlayerData();after.deserializeNBT(before.serializeNBT());after.resetAfterDeath();
        assertTrue(after.hasFinalStage());assertTrue(after.flightGranted);assertFalse(after.trappedInGap);
    }
    @Test public void saveRoundTripPreservesCaptivityAndLearnedSpells() {
        PlayerData before=new PlayerData();before.setAbsorbedXp(1600);before.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,3);before.setSpirit(4.5f);before.setPower(4);before.trappedInGap=true;
        before.hasReturn=true;before.returnDimension=-1;before.returnX=23.25;before.learn(Spell.ROYAL_FLARE);before.learn(Spell.GUNGNIR);
        PlayerData after=new PlayerData();after.deserializeNBT(before.serializeNBT());
        assertTrue(after.trappedInGap);assertTrue(after.hasReturn);assertEquals(-1,after.returnDimension);assertEquals(23.25,after.returnX,0);
        assertEquals(4.5f,after.spirit(),0);assertEquals(6,after.capacity());assertTrue(after.knows(Spell.GUNGNIR));assertArrayEquals(before.preparedSlots(),after.preparedSlots());
    }
    @Test public void corruptNbtCannotGrantResourcesOrInvalidSpells() {
        NBTTagCompound n=new NBTTagCompound();n.setFloat("spirit",Float.NaN);n.setInteger("power",500);n.setInteger("magic",-5);
        n.setIntArray("slots",new int[]{999,999,999,999});n.setInteger("selected",99);
        PlayerData d=new PlayerData();d.deserializeNBT(n);assertEquals(0,d.spirit(),0);assertEquals(5,d.power());assertEquals(0,d.magic());assertNull(d.selectedSpell());
    }
    @Test public void failedConsumptionLeavesSpiritUntouched() {
        PlayerData d=new PlayerData();d.setSpirit(1);assertFalse(d.useSpirit(2));assertFalse(d.useSpirit(Float.NaN));assertEquals(1,d.spirit(),0);assertTrue(d.useSpirit(.25f));assertEquals(.75f,d.spirit(),0);
    }
    @Test public void deathClearsCaptivityAndTemporaryResourcesButKeepsProgress() {
        PlayerData d=new PlayerData();d.setAbsorbedXp(1600);d.vampireXp(500);d.youkaiProgress(300);
        d.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,4);d.grantStage(dev.lostfantasy.core.Growth.Route.YOUKAI,2);d.grantStage(dev.lostfantasy.core.Growth.Route.VAMPIRE,2);d.learn(Spell.ROYAL_FLARE);
        d.trappedInGap=true;d.hasReturn=true;d.setPower(5);d.setSpirit(6);d.cooldown=80;
        int[] equipment=d.preparedSlots().clone();d.resetAfterDeath();
        assertFalse(d.trappedInGap);assertFalse(d.hasReturn);assertEquals(0,d.power());assertEquals(0,d.spirit(),0);
        assertEquals(0,d.cooldown);assertEquals(6,d.capacity());
        assertEquals(4,d.magic());assertEquals(2,d.youkai());assertEquals(2,d.vampire());
        assertEquals(500,d.vampireXp());assertEquals(300,d.youkaiProgress());assertTrue(d.knows(Spell.ROYAL_FLARE));assertArrayEquals(equipment,d.preparedSlots());
        PlayerData restored=new PlayerData();restored.deserializeNBT(d.serializeNBT());assertFalse(restored.trappedInGap);
    }
    @Test public void serverDisplaySettingsDoNotLeakIntoSavedProgress() {
        NBTTagCompound packet=new NBTTagCompound();packet.setInteger("displayCapacity",5);packet.setFloat("displayShieldMax",200);
        packet.setFloat("spirit",4);packet.setFloat("shield",150);
        NBTTagCompound message=new NBTTagCompound();message.setTag("runtime",packet);
        PlayerData client=new PlayerData();client.applyUpdate(message);
        assertEquals(5,client.capacity());assertEquals(4,client.spirit(),0);assertEquals(200,client.shieldMaximum(),0);assertEquals(150,client.shield,0);
        NBTTagCompound saved=client.serializeNBT();assertFalse(saved.hasKey("displayCapacity"));assertFalse(saved.hasKey("displayShieldMax"));
        PlayerData server=new PlayerData();server.deserializeNBT(saved);assertEquals(2,server.capacity());assertEquals(2,server.spirit(),0);
    }
    @Test public void extremeStoredXpCannotOverflowWhenMoreXpIsAbsorbed() {
        NBTTagCompound n=new NBTTagCompound();n.setLong("xp",Long.MAX_VALUE);Growth growth=new Growth();growth.grant(Growth.Route.VAMPIRE,1);growth.progress(Growth.Route.VAMPIRE,Long.MAX_VALUE);n.setTag("growth",growth.save());
        PlayerData d=new PlayerData();d.deserializeNBT(n);d.absorb(1000);
        assertEquals(Integer.MAX_VALUE,d.absorbedXp());assertEquals(Integer.MAX_VALUE,d.vampireXp());assertEquals(6,d.capacity());
    }
    @Test public void learningTwiceDoesNotDuplicateOrReplaceEquipment() {
        PlayerData d=new PlayerData();assertTrue(d.learn(Spell.GUNGNIR));int[] equipped=d.preparedSlots().clone();
        assertFalse(d.learn(Spell.GUNGNIR));assertArrayEquals(equipped,d.preparedSlots());
        assertTrue(d.learn(Spell.FOUR_OF_A_KIND));assertTrue(d.validLoadout(d.preparedSlots()));
    }
}
