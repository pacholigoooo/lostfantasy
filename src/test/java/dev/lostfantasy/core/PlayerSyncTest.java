package dev.lostfantasy.core;

import dev.lostfantasy.data.PlayerData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.CompressedStreamTools;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import static org.junit.Assert.*;

public class PlayerSyncTest {
    private static PlayerData prepared() {
        PlayerData data=new PlayerData();data.grantStage(Growth.Route.MAGIC,3);
        data.setAbsorbedXp(2000);data.research.restore(15);
        for(Spell spell:Spell.catalog())data.learn(spell,false);
        data.equipSpell(0,Spell.EMERALD_CITY.networkId);return data;
    }
    @Test public void runtimeUpdatesPreserveProgressAndExcludeServerCoordinates() throws Exception {
        PlayerData server=prepared(),client=new PlayerData();
        server.journey.arrive();server.journey.board(false);
        NBTTagCompound full=server.createUpdate(true);client.applyUpdate(full);
        server.setSpirit(1.25f);server.setPower(3);server.journey.advance(true);server.cooldown=17;
        NBTTagCompound update=server.createUpdate(false);client.applyUpdate(update);
        assertFalse(update.hasKey("progress"));assertFalse(update.toString().contains("origin"));
        assertFalse(update.toString().contains("returnX"));assertFalse(update.toString().contains("dimension"));
        assertEquals(3,client.magic());assertEquals(15,client.research.flags());assertTrue(client.knows(Spell.EMERALD_CITY));
        assertEquals(Spell.EMERALD_CITY,client.selectedSpell());assertEquals(1.25f,client.spirit(),0);
        assertEquals(17,client.cooldown);assertEquals(1,client.journey.elapsed());
        assertTrue(size(update)<size(full)*.65);
        System.out.println("PLAYER_SYNC_BYTES full="+size(full)+" runtime="+size(update));
    }
    @Test public void nestedProgressAndJourneyChangesMarkTheOwningPlayer() {
        PlayerData data=new PlayerData(),other=new PlayerData();data.createUpdate(true);other.createUpdate(true);
        assertTrue(data.advanceStage(Growth.Route.MAGIC,1));assertTrue(data.dirty);
        assertTrue(data.createUpdate(false).hasKey("progress"));assertFalse(other.dirty);
        assertFalse(data.advanceStage(Growth.Route.YOUKAI,1));assertFalse(data.dirty);
        assertTrue(data.research.discover());assertTrue(data.createUpdate(false).hasKey("progress"));
        assertTrue(data.journey.separateFortune());assertTrue(data.dirty);
        assertFalse(data.createUpdate(false).hasKey("progress"));
        data.growth.reincarnate(true);assertTrue(data.advanceStage(Growth.Route.YOUKAI,1));
        assertTrue(data.createUpdate(false).hasKey("progress"));
    }
    @Test public void preparedSlotCopiesAndClampedResourcesCannotBypassMutators() {
        PlayerData data=prepared();data.createUpdate(true);
        int[] slots=data.preparedSlots();slots[0]=Spell.GUNGNIR.networkId;
        assertSame(Spell.EMERALD_CITY,data.selectedSpell());assertFalse(data.dirty);
        data.setPower(500);data.setSpirit(Float.NaN);assertEquals(5,data.power());assertEquals(2,data.spirit(),0);
        data.setSpirit(-1);assertEquals(0,data.spirit(),0);
        assertTrue(data.equipSpell(0,Spell.GUNGNIR.networkId));assertTrue(data.createUpdate(false).hasKey("progress"));
    }
    @Test public void administrativeRouteClearAlsoRemovesUnassignedProgress() {
        PlayerData data=new PlayerData();data.youkaiProgress(40);data.createUpdate(true);
        data.grantStage(Growth.Route.YOUKAI,0);
        assertEquals(0,data.youkaiProgress());assertTrue(data.createUpdate(false).hasKey("progress"));
    }
    @Test public void roundTripSyncPreservesProgressAndSeparatedFortune() {
        PlayerData server=prepared(),client=new PlayerData();
        server.journey.separateFortune();server.journey.arrive();server.journey.board(false);
        client.applyUpdate(server.createUpdate(true));
        for(int i=0;i<RiverJourney.DURATION;i++)server.journey.advance(true);
        assertTrue(server.journey.ready());assertTrue(server.journey.finishCrossing());
        client.applyUpdate(server.createUpdate(false));
        assertEquals(RiverJourney.Phase.SHORE,client.journey.phase());assertEquals(0,client.journey.elapsed());
        assertFalse(client.journey.fromFarBank());assertTrue(client.journey.fortuneSeparated());
        assertTrue(client.knows(Spell.EMERALD_CITY));assertEquals(15,client.research.flags());assertEquals(3,client.magic());
        assertTrue(server.journey.board(true));
        for(int i=0;i<RiverJourney.DURATION;i++)server.journey.advance(true);
        assertTrue(server.journey.finishCrossing());client.applyUpdate(server.createUpdate(false));
        assertEquals(RiverJourney.Phase.SHORE,client.journey.phase());assertEquals(0,client.journey.elapsed());
        assertTrue(client.journey.fromFarBank());assertTrue(client.journey.fortuneSeparated());
        assertTrue(client.knows(Spell.EMERALD_CITY));assertEquals(15,client.research.flags());assertEquals(3,client.magic());
    }
    @Test public void forcedRefreshAndReloadCarryProgressAfterDirtyWasCleared() {
        PlayerData server=prepared();server.createUpdate(true);assertFalse(server.createUpdate(false).hasKey("progress"));
        PlayerData rejoined=new PlayerData();rejoined.applyUpdate(server.createUpdate(true));assertTrue(rejoined.knows(Spell.EMERALD_CITY));
        PlayerData restored=new PlayerData();restored.deserializeNBT(server.serializeNBT());
        assertTrue(restored.createUpdate(false).hasKey("progress"));
        restored.resetAfterDeath();rejoined.applyUpdate(restored.createUpdate(true));
        assertTrue(rejoined.knows(Spell.EMERALD_CITY));assertEquals(0,rejoined.power());assertEquals(0,rejoined.spirit(),0);
    }
    private static int size(NBTTagCompound tag) throws Exception {
        ByteArrayOutputStream out=new ByteArrayOutputStream();CompressedStreamTools.write(tag,new DataOutputStream(out));return out.size();
    }
}
