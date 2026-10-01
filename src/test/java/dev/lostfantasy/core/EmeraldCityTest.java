package dev.lostfantasy.core;

import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.network.EffectMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import org.junit.Test;
import java.util.ArrayList;
import java.util.UUID;
import static org.junit.Assert.*;

public class EmeraldCityTest {
    @Test public void fourRoundsHaveFourRisingTicksTwentyHoldingTicksAndEightSinkingTicks() {
        for(int round=0;round<4;round++) {
            EmeraldCity.Column c=new EmeraldCity.Column(0,64,2+2*round,4,round);
            int start=8+8*round, rises=0, shields=0;
            for(int age=0;age<=64;age++) { if(c.rising(age)) rises++; if(c.shielding(age)) shields++; }
            assertEquals(4,rises); assertEquals(28,shields);
            assertEquals(0,c.heightAt(start),0); assertEquals(1,c.heightAt(start+1),0);
            assertEquals(4,c.heightAt(start+4),0); assertEquals(4,c.heightAt(start+24),0);
            assertEquals(2,c.heightAt(start+28),0); assertEquals(0,c.heightAt(start+32),0);
            assertFalse(c.rising(start+4)); assertTrue(c.shielding(start+4)); assertFalse(c.shielding(start+32));
        }
    }
    @Test public void controlsReleaseAtTwelveWhileEffectAndForegroundContinueWithoutProtection() {
        EffectMessage effect=new EffectMessage(5,1,0,100,64,0,Vec3d.ZERO,new Vec3d(0,0,1),8);
        for(int age=0;age<64;age++) {
            assertFalse(Spell.EMERALD_CITY.protectsCaster(age,64));
            assertEquals(age<12,Spell.EMERALD_CITY.restrictsMovement(age));
            assertEquals(age<12,effect.poseActive(age)); assertTrue(effect.retainedAt(0,100+age));
        }
        assertFalse(effect.retainedAt(0,164)); assertFalse(effect.poseActive(-1));
        SpellSessions<String> sessions=new SpellSessions<>(); UUID owner=UUID.randomUUID();
        assertTrue(sessions.start(owner,Spell.EMERALD_CITY,"columns"));
        assertFalse(sessions.start(owner,Spell.ROYAL_FLARE,"blocked"));
        assertTrue(sessions.remove(owner,Spell.EMERALD_CITY,"columns"));
        assertTrue(sessions.start(owner,Spell.ROYAL_FLARE,"next"));
    }
    @Test public void contactMatchesOctagonPointedTopGapsAndVisibleSinkingHeight() {
        EmeraldCity.Column c=new EmeraldCity.Column(0,64,2,4,0);
        Vec3d hit=c.contact(new Vec3d(-2,65,2),new Vec3d(2,65,2),12);
        assertNotNull(hit); assertEquals(-.7,hit.x,1e-9);
        assertNull(c.contact(new Vec3d(-2,65,3),new Vec3d(2,65,3),12));
        assertNull(c.contact(new Vec3d(-2,68.1,2),new Vec3d(2,68.1,2),12));
        assertNull(c.contact(new Vec3d(-2,67.8,2.5),new Vec3d(2,67.8,2.5),12));
        assertNotNull(c.contact(new Vec3d(-2,67.8,2),new Vec3d(2,67.8,2),12));
        assertNotNull(c.contact(new Vec3d(-2,65,2),new Vec3d(2,65,2),36));
        assertNull(c.contact(new Vec3d(-2,67,2),new Vec3d(2,67,2),36));
        assertNull(c.contact(new Vec3d(-2,65,2),new Vec3d(2,65,2),40));
        assertNull(c.contact(new Vec3d(-2,64.05,2.4),new Vec3d(2,64.05,2.4),39.5));
        assertNotNull(c.contact(new Vec3d(-2,64.05,2),new Vec3d(2,64.05,2),39.5));
    }
    @Test public void canceledAttemptsDoNotConsumeTheSharedSuccessfulHitCap() {
        EmeraldCity.Hits hits=new EmeraldCity.Hits(); UUID target=UUID.randomUUID(),other=UUID.randomUUID();
        assertTrue(hits.attempt(0,target)); assertFalse(hits.attempt(0,target)); assertEquals(0,hits.count(target));
        for(int column=1;column<=3;column++) { assertTrue(hits.attempt(column,target)); hits.landed(target); }
        assertEquals(3,hits.count(target)); assertFalse(hits.attempt(4,target)); assertTrue(hits.attempt(4,other));
    }
    @Test public void learningRequiresMagicThreeAndPersistsWithoutAutoPreparationOrPromotion() {
        PlayerData data=new PlayerData(); data.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,2); assertFalse(data.qualifies(Spell.EMERALD_CITY));
        data.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,3); assertTrue(data.qualifies(Spell.EMERALD_CITY)); assertTrue(data.learn(Spell.EMERALD_CITY,false));
        assertArrayEquals(new int[]{-1,-1,-1,-1},data.preparedSlots()); assertEquals(3,data.magic());
        assertFalse(data.knows(Spell.ROYAL_FLARE)); assertTrue(data.equipSpell(3,5));
        PlayerData read=new PlayerData(); read.deserializeNBT(data.serializeNBT());
        assertTrue(read.knows(Spell.EMERALD_CITY)); assertEquals(5,read.spellInSlot(3)); assertEquals(4,read.preparedSlots().length);
        SpellPage page=new SpellPage(Spell.catalog().stream().mapToInt(spell->spell.networkId).toArray());
        page.move(1); assertEquals(5,page.spellIdAt(1));
    }
    @Test public void packetPreservesEveryTerrainLimitedColumnAndRound() {
        EffectMessage sent=new EffectMessage(5,1,0,100,64,0,Vec3d.ZERO,new Vec3d(0,0,1),8);
        sent.columns=new ArrayList<>();
        for(EmeraldCity.Column c:ResearchDemonstration.COLUMNS) sent.columns.add(new EmeraldCity.Column(c.x,64+c.round*.25,c.z,4-c.round*.5,c.round));
        ByteBuf buffer=Unpooled.buffer();
        try {
            sent.toBytes(buffer); EffectMessage read=new EffectMessage(); read.fromBytes(buffer);
            assertEquals(80,read.columns.size()); assertEquals(0,buffer.readableBytes());
            for(int i=0;i<80;i++) { EmeraldCity.Column a=sent.columns.get(i), b=read.columns.get(i);
                assertEquals(a.x,b.x,0);assertEquals(a.y,b.y,0);assertEquals(a.z,b.z,0);assertEquals(a.height,b.height,0);assertEquals(a.round,b.round); }
            buffer.clear(); sent.columns.clear(); sent.toBytes(buffer); buffer.setByte(buffer.writerIndex()-1,81);
            try { read.fromBytes(buffer); fail("Unbounded payload accepted"); } catch(IllegalArgumentException expected) {}
        } finally { buffer.release(); }
    }
}
