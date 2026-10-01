package dev.lostfantasy.core;

import java.util.EnumMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class SpellRegistryTest {
    @Test public void missingImplementationFailsBeforeACastCanStart() {
        for(Spell missing:Spell.catalog()) {
            Map<Spell,Runnable> entries=new EnumMap<>(Spell.class);
            for(Spell spell:Spell.catalog())if(spell!=missing)entries.put(spell,()->{});
            try {Spell.completeRegistry(entries);fail("Missing "+missing);}
            catch(IllegalStateException expected) {assertTrue(expected.getMessage().contains(missing.id));}
        }
    }
    @Test public void registryOwnsAnImmutableCopy() {
        Map<Spell,Runnable> entries=new EnumMap<>(Spell.class);
        for(Spell spell:Spell.catalog())entries.put(spell,()->{});
        Map<Spell,Runnable> complete=Spell.completeRegistry(entries);entries.clear();
        assertEquals(Spell.catalog().size(),complete.size());
        try {complete.clear();fail();}catch(UnsupportedOperationException expected) {}
    }
}
