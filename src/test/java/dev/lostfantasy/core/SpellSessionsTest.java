package dev.lostfantasy.core;

import org.junit.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.Assert.*;

public class SpellSessionsTest {
    private final UUID owner = new UUID(0, 1);
    private final UUID other = new UUID(0, 2);

    @Test
    public void otherSpellsCanRunWhileEchoesRemainAvailable() {
        SpellSessions<Object> sessions = new SpellSessions<>();
        Object echoes = new Object();
        assertTrue(sessions.start(owner, Spell.FOUR_OF_A_KIND, echoes));
        for (Spell spell : new Spell[]{Spell.ROYAL_FLARE, Spell.ABANDONED_TRAIN, Spell.GUNGNIR}) {
            Object cast = new Object();
            assertTrue(sessions.start(owner, spell, cast));
            assertSame(echoes, sessions.get(owner, Spell.FOUR_OF_A_KIND));
            assertSame(cast, sessions.foreground(owner));
            assertTrue(sessions.remove(owner, spell, cast));
            assertNull(sessions.foreground(owner));
            assertSame(echoes, sessions.get(owner, Spell.FOUR_OF_A_KIND));
        }
    }

    @Test
    public void duplicateCastsCannotReplaceEitherSlot() {
        SpellSessions<Object> sessions = new SpellSessions<>();
        Object echoes = new Object();
        Object foreground = new Object();
        assertTrue(sessions.start(owner, Spell.FOUR_OF_A_KIND, echoes));
        assertFalse(sessions.start(owner, Spell.FOUR_OF_A_KIND, new Object()));
        assertSame(echoes, sessions.get(owner, Spell.FOUR_OF_A_KIND));
        assertTrue(sessions.start(owner, Spell.ROYAL_FLARE, foreground));
        assertFalse(sessions.start(owner, Spell.GUNGNIR, new Object()));
        assertSame(foreground, sessions.foreground(owner));
    }

    @Test
    public void echoExpiryCannotRemoveOtherCastsOrAReplacementGroup() {
        SpellSessions<Object> sessions = new SpellSessions<>();
        Object echo = new Object();
        Object cast = new Object();
        Object otherEcho = new Object();
        sessions.start(owner, Spell.FOUR_OF_A_KIND, echo);
        sessions.start(owner, Spell.ROYAL_FLARE, cast);
        sessions.start(other, Spell.FOUR_OF_A_KIND, otherEcho);
        assertTrue(sessions.remove(owner, Spell.FOUR_OF_A_KIND, echo));
        assertSame(cast, sessions.foreground(owner));
        assertSame(otherEcho, sessions.get(other, Spell.FOUR_OF_A_KIND));

        Object replacement = new Object();
        assertTrue(sessions.start(owner, Spell.FOUR_OF_A_KIND, replacement));
        assertFalse(sessions.remove(owner, Spell.FOUR_OF_A_KIND, echo));
        assertSame(replacement, sessions.get(owner, Spell.FOUR_OF_A_KIND));
    }

    @Test
    public void snapshotsKeepCastOrderAndSurviveCancellationAndReplacement() {
        SpellSessions<Object> sessions = new SpellSessions<>();
        Object echoes = new Object();
        Object foreground = new Object();
        sessions.start(owner, Spell.FOUR_OF_A_KIND, echoes);
        sessions.start(owner, Spell.GUNGNIR, foreground);
        List<Object> snapshot = sessions.snapshot();
        assertEquals(2, snapshot.size());
        assertSame(foreground, snapshot.get(0));
        assertSame(echoes, snapshot.get(1));

        sessions.remove(owner, Spell.FOUR_OF_A_KIND);
        Object replacement = new Object();
        sessions.start(owner, Spell.FOUR_OF_A_KIND, replacement);
        assertSame(echoes, snapshot.get(1));
        assertFalse(snapshot.contains(replacement));
        assertFalse(sessions.isEmpty());
        sessions.clear();
        assertTrue(sessions.isEmpty());
        assertTrue(sessions.snapshot().isEmpty());
        assertNull(sessions.foreground(owner));
        assertNull(sessions.get(owner, Spell.FOUR_OF_A_KIND));
        assertEquals(2, snapshot.size());
    }
}
