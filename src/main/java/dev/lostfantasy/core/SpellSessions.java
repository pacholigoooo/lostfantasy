package dev.lostfantasy.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Server-thread state: one ordinary cast and one echo group per player. */
public final class SpellSessions<T> {
    private final Map<UUID, T> foreground = new HashMap<>();
    private final Map<UUID, T> echoes = new HashMap<>();

    public T foreground(UUID owner) {
        return foreground.get(owner);
    }

    public T get(UUID owner, Spell spell) {
        return sessionsFor(spell).get(owner);
    }

    public boolean start(UUID owner, Spell spell, T session) {
        return sessionsFor(spell).putIfAbsent(owner, session) == null;
    }

    public T remove(UUID owner, Spell spell) {
        return sessionsFor(spell).remove(owner);
    }

    public boolean remove(UUID owner, Spell spell, T expected) {
        return sessionsFor(spell).remove(owner, expected);
    }

    public List<T> snapshot() {
        if (isEmpty()) return Collections.emptyList();

        List<T> result = new ArrayList<>(foreground.size() + echoes.size());
        for (T cast : foreground.values()) result.add(cast);
        for (T group : echoes.values()) result.add(group);
        return result;
    }

    public boolean isEmpty() {
        return foreground.isEmpty() && echoes.isEmpty();
    }

    public void clear() {
        foreground.clear();
        echoes.clear();
    }

    private Map<UUID, T> sessionsFor(Spell spell) {
        return spell == Spell.FOUR_OF_A_KIND ? echoes : foreground;
    }
}
