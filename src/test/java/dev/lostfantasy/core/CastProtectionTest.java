package dev.lostfantasy.core;

import java.util.UUID;
import org.junit.Test;
import static org.junit.Assert.*;

public class CastProtectionTest {
    @Test public void protectionIncludesWindupAndStopsAtTheEndOfTheCast() {
        for (Spell spell : new Spell[]{Spell.ROYAL_FLARE, Spell.ABANDONED_TRAIN, Spell.GUNGNIR}) {
            assertFalse(spell.protectsCaster(-1, 90));
            assertTrue(spell.protectsCaster(0, 90));
            assertTrue(spell.protectsCaster(89, 90));
            assertFalse(spell.protectsCaster(90, 90));
            assertFalse(spell.protectsCaster(91, 90));
            assertFalse(spell.protectsCaster(0, 0));
        }
        assertFalse(Spell.FOUR_OF_A_KIND.protectsCaster(0, 1200));
        assertFalse(Spell.FOUR_OF_A_KIND.protectsCaster(600, 1200));
    }

    @Test public void anEchoGroupCannotKeepProtectionAfterTheOtherCastEnds() {
        SpellSessions<Spell> sessions = new SpellSessions<>();
        UUID player = new UUID(0, 1);
        sessions.start(player, Spell.FOUR_OF_A_KIND, Spell.FOUR_OF_A_KIND);
        assertNull(sessions.foreground(player));
        sessions.start(player, Spell.GUNGNIR, Spell.GUNGNIR);
        assertTrue(sessions.foreground(player).protectsCaster(12, 27));
        sessions.remove(player, Spell.GUNGNIR);
        assertNull(sessions.foreground(player));
        assertSame(Spell.FOUR_OF_A_KIND, sessions.get(player, Spell.FOUR_OF_A_KIND));
        sessions.clear();
        assertNull(sessions.foreground(player));
    }
}
