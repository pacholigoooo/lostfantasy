package dev.lostfantasy.core;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class SpellPageTest {
    @Test public void everyEntryCanBeBrowsedBeyondFourAndThirtyOneSpells() {
        for (int count : new int[]{0, 1, 4, 5, 8, 37, 70}) {
            int[] ids = new int[count];
            for (int index = 0; index < count; index++) ids[index] = 100 + index * 3;
            SpellPage page = new SpellPage(ids);
            List<Integer> seen = new ArrayList<>();
            for (int index = 0; index < page.pageCount(); index++) {
                assertEquals(index, page.page());
                for (int row = 0; row < page.visibleCount(); row++) seen.add(page.spellIdAt(row));
                assertEquals(-1, page.spellIdAt(-1));
                assertEquals(-1, page.spellIdAt(page.visibleCount()));
                page.move(1);
            }
            assertEquals(count, seen.size());
            for (int index = 0; index < count; index++) assertEquals(ids[index], (int) seen.get(index));
            assertEquals(page.pageCount() - 1, page.page());
            for (int index = 0; index <= page.pageCount(); index++) page.move(-1);
            assertEquals(0, page.page());
        }
    }

    @Test public void fifthSpellUsesSecondPageWithoutChangingPreparedSlotCount() {
        int[] ids = {3, 1, 2, 0, 64};
        SpellPage page = new SpellPage(ids);
        ids[4] = 99;
        assertEquals(4, Rules.LOADOUT_SIZE);
        assertEquals(2, page.pageCount());
        page.move(1);
        assertEquals(1, page.visibleCount());
        assertEquals(64, page.spellIdAt(0));
        assertEquals(-1, page.spellIdAt(1));
    }
}
