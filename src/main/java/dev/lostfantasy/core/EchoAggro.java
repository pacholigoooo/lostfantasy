package dev.lostfantasy.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Short-lived combat records; cleared on logout, death and dimension changes. */
public final class EchoAggro {
    public static final int MEMORY_TICKS = 30 * 20;
    public static final int ATTACK_MEMORY_TICKS = 10 * 20;
    private final Map<UUID, Map<UUID, Long>> opponents = new HashMap<>();
    private final Map<UUID, Attack> attacks = new HashMap<>();

    public void recordAttack(UUID owner, UUID target, long tick) {
        if (owner == null || target == null || owner.equals(target)) return;
        Attack attack = attacks.computeIfAbsent(owner, id -> new Attack());
        attack.target = target;
        attack.until = tick + ATTACK_MEMORY_TICKS;
    }

    public UUID attackTarget(UUID owner, long tick) {
        Attack attack = attacks.get(owner);
        if (attack == null) return null;
        if (tick < attack.until) return attack.target;
        attacks.remove(owner);
        return null;
    }

    public void record(UUID attacker, UUID victim, long tick) {
        if (attacker == null || victim == null || attacker.equals(victim)) return;
        remember(attacker, victim, tick);
        remember(victim, attacker, tick);
    }

    private void remember(UUID owner, UUID target, long tick) {
        Map<UUID, Long> entries = opponents.computeIfAbsent(owner, id -> new HashMap<>());
        entries.values().removeIf(until -> until <= tick);
        entries.put(target, tick + MEMORY_TICKS);
    }

    public boolean active(UUID owner, UUID target, long tick) {
        Map<UUID, Long> entries = opponents.get(owner);
        if (entries == null) return false;
        Long until = entries.get(target);
        if (until == null) return false;
        if (until > tick) return true;

        entries.remove(target);
        if (entries.isEmpty()) opponents.remove(owner);
        return false;
    }

    public void forget(UUID player) {
        attacks.remove(player);
        attacks.values().removeIf(attack -> player.equals(attack.target));
        opponents.remove(player);
        opponents.values().removeIf(entries -> {
            entries.remove(player);
            return entries.isEmpty();
        });
    }

    public void prune(long tick) {
        attacks.values().removeIf(attack -> attack.until <= tick);
        opponents.values().removeIf(entries -> {
            entries.values().removeIf(until -> until <= tick);
            return entries.isEmpty();
        });
    }

    public void clear() {
        opponents.clear();
        attacks.clear();
    }

    private static final class Attack {
        UUID target;
        long until;
    }
}
