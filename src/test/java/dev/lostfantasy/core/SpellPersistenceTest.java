package dev.lostfantasy.core;

import dev.lostfantasy.data.PlayerData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import org.junit.Test;
import static org.junit.Assert.*;

public class SpellPersistenceTest {
    private static final Spell[] FIRST_FOUR = {Spell.ROYAL_FLARE, Spell.ABANDONED_TRAIN, Spell.GUNGNIR, Spell.FOUR_OF_A_KIND};

    @Test public void everyCurrentSpellRoundTripsWithPreparedSlotsAndSelection() {
        PlayerData source=new PlayerData();for(Spell spell:Spell.catalog())source.learn(spell,false);
        source.equipSpell(0,Spell.EMERALD_CITY.networkId);source.equipSpell(3,Spell.FOURFOLD_BARRIER.networkId);source.selectSlot(3);
        PlayerData restored=new PlayerData();restored.deserializeNBT(source.serializeNBT());
        for(Spell spell:Spell.catalog())assertTrue(restored.knows(spell));
        assertArrayEquals(source.preparedSlots(),restored.preparedSlots());assertEquals(3,restored.selectedSlot());
    }

    @Test public void namesAreOrderIndependentAndUnknownOrDuplicateNamesGrantNothingExtra() {
        NBTTagCompound saved = new NBTTagCompound();
        NBTTagList names = new NBTTagList();
        names.appendTag(new NBTTagString("unknown_future_spell"));
        names.appendTag(new NBTTagString("gungnir"));
        names.appendTag(new NBTTagString("royal_flare"));
        names.appendTag(new NBTTagString("gungnir"));
        saved.setTag("learnedSpells", names);
        saved.setIntArray("slots", new int[]{2, -1, 0, -1});
        PlayerData data = new PlayerData();
        data.deserializeNBT(saved);
        assertTrue(data.knows(Spell.GUNGNIR));
        assertTrue(data.knows(Spell.ROYAL_FLARE));
        assertFalse(data.knows(Spell.ABANDONED_TRAIN));
        assertFalse(data.knows(Spell.FOUR_OF_A_KIND));
        assertEquals(2, data.serializeNBT().getTagList("learnedSpells", 8).tagCount());
        assertArrayEquals(new int[]{2, -1, 0, -1}, data.preparedSlots());
    }

    @Test public void stableNumericIdsMatchSlotsAndNames() {
        for (int id = 0; id < FIRST_FOUR.length; id++) {
            assertEquals(id, FIRST_FOUR[id].networkId);
            assertSame(FIRST_FOUR[id], Spell.byId(id));
            assertSame(FIRST_FOUR[id], Spell.byName(FIRST_FOUR[id].id));
        }
        assertNull(Spell.byId(-1));
        assertNull(Spell.byName("not_a_spell"));
    }

    @Test public void preparingSpellsStillHasExactlyFourSlotsAndRejectsDuplicatesOrUnknownIds() {
        PlayerData data = new PlayerData();
        for (Spell spell : FIRST_FOUR) data.learn(spell);
        int[] equipped = data.preparedSlots().clone();
        assertEquals(4, equipped.length);
        assertFalse(data.equipSpell(4, Spell.ROYAL_FLARE.networkId));
        assertFalse(data.equipSpell(0, Spell.GUNGNIR.networkId));
        assertFalse(data.equipSpell(0, Integer.MAX_VALUE));
        assertArrayEquals(equipped, data.preparedSlots());
        assertTrue(data.equipSpell(2, -1));
        assertTrue(data.equipSpell(0, Spell.GUNGNIR.networkId));
        data.selectSlot(0);
        assertSame(Spell.GUNGNIR, data.selectedSpell());
        assertEquals(0, data.preparedUltimateCount());
    }
}
