package dev.lostfantasy.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.UUID;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Exact synchronous scopes and opt-in tickets for mods that separate damage settlement from the hit. */
public final class DamageTransactions {
    private static final ThreadLocal<Scope> CURRENT = new ThreadLocal<>();
    private static final ThreadLocal<FinalCall> FINAL_CALL = new ThreadLocal<>();
    private static final ThreadLocal<HitResult> OBSERVED = new ThreadLocal<>();
    private static final Map<UUID, Pending> DEFERRED = new HashMap<>();
    private static final Logger LOGGER = LogManager.getLogger("LostFantasy");
    private static long lastWarning;
    private static boolean warned;

    private DamageTransactions() {}

    public static Scope enter(EntityLivingBase victim, DamageSource source) {
        HitResult hit = OBSERVED.get();
        boolean observed = hit != null && hit.victim == victim && hit.source == source;
        if (victim.world.isRemote || (!(victim instanceof EntityPlayerMP) && !observed)) return null;
        Scope scope = new Scope(victim, source, CURRENT.get());
        // Nested settlements cannot claim the result. A later top-level body supersedes a hit
        // fired by a LivingAttack listener before the original damageEntity call began.
        if (observed && CURRENT.get() == hit.parentScope) {
            hit.amount = Float.NaN;
            hit.exited = false;
            scope.hit = hit;
        }
        CURRENT.set(scope);
        return scope;
    }

    /** Observe one synchronous attack; the source must be unique to that attack. */
    public static HitResult observe(EntityLivingBase victim, DamageSource source) {
        HitResult hit = new HitResult(victim, source, OBSERVED.get());
        OBSERVED.set(hit);
        return hit;
    }

    /** Called after Forge has finished all Hurt listeners, including cancellation. */
    public static float capture(float amount, EntityLivingBase victim, DamageSource source) {
        Scope scope = CURRENT.get();
        if (scope != null && scope.victim == victim && scope.source == source) scope.original = amount;
        return amount;
    }

    /** Only the original damageEntity call site (or an explicit ticket) opens this binding window. */
    public static float finish(EntityLivingBase victim, DamageSource source, float amount) {
        return finish(victim, source, amount, ForgeHooks::onLivingDamage);
    }

    static float finish(EntityLivingBase victim, DamageSource source, float amount, FinalDamage hook) {
        Scope scope = CURRENT.get();
        if (scope != null && (scope.victim != victim || scope.source != source)) scope = null;
        FinalCall previous = FINAL_CALL.get();
        // Even an untracked call shadows an outer binding window until it returns.
        FINAL_CALL.set(new FinalCall(scope));
        float settled;
        try {
            settled = hook.apply(victim, source, amount);
            // Read only after every Forge Damage listener and the cancellation check have returned.
            if (scope != null && scope.hit != null) scope.hit.amount = settled;
        } finally {
            if (previous == null) FINAL_CALL.remove();
            else FINAL_CALL.set(previous);
        }
        Combat.settledLifeSteal(victim, source, settled);
        return settled;
    }

    /** Bind the exact event before EventBus.post, only inside the authorized final call. */
    public static void bind(Event event) {
        FinalCall call = FINAL_CALL.get();
        if (call == null || call.scope == null || call.event != null || !(event instanceof LivingDamageEvent)) return;
        Scope scope = call.scope;
        if (CURRENT.get() != scope || scope.consumed) return;
        LivingDamageEvent damage = (LivingDamageEvent) event;
        if (scope.victim == damage.getEntityLiving() && scope.source == damage.getSource()
                && Float.isFinite(scope.original) && scope.original >= 0) call.event = damage;
    }

    /** NaN means unknown, never an inferred pre-armour value. Each event can consume its record once. */
    public static float consume(LivingDamageEvent event) {
        FinalCall call = FINAL_CALL.get();
        if (call == null || call.scope == null || call.event != event) return Float.NaN;
        Scope scope = call.scope;
        if (CURRENT.get() != scope || scope.consumed) return Float.NaN;
        scope.consumed = true;
        return scope.original;
    }

    public static void exit(Scope scope) {
        if (scope == null) return;
        if (CURRENT.get() != scope) throw new IllegalStateException("Lost Fantasy damage scopes closed out of order");
        if (scope.hit != null) scope.hit.exited = true;
        if (scope.parent == null) CURRENT.remove();
        else CURRENT.set(scope.parent);
    }

    /**
     * Adapter API, server thread only. Supply the final pre-armour amount and an explicit lifetime.
     * Cancelling the original hit to defer it does not cancel this separate ticket.
     */
    public static Ticket defer(EntityPlayerMP player, DamageSource source, float original, int lifetimeTicks) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(source, "source");
        MinecraftServer server = player.getServer();
        requireServerThread(server);
        if (!Float.isFinite(original) || original < 0 || lifetimeTicks <= 0)
            throw new IllegalArgumentException("A deferred hit needs a finite nonnegative baseline and a positive lifetime");
        if (!player.isEntityAlive() || server.getPlayerList().getPlayerByUUID(player.getUniqueID()) != player)
            throw new IllegalArgumentException("Cannot defer damage for an inactive player");
        Ticket ticket = new Ticket(UUID.randomUUID());
        DEFERRED.put(ticket.id, new Pending(player, source, original, lifetimeTicks, server));
        return ticket;
    }

    /**
     * Runs only Forge's final Damage phase, not armour or health application. The adapter applies the returned
     * value through its own settlement path. Empty means an explicitly cancelled/expired/consumed ticket;
     * zero is a valid blocked hit, including a cancelled Forge event.
     */
    public static OptionalDouble settle(MinecraftServer server, Ticket ticket, float afterArmor) {
        requireServerThread(server);
        Objects.requireNonNull(ticket, "ticket");
        if (!Float.isFinite(afterArmor) || afterArmor < 0) throw new IllegalArgumentException("Invalid post-armour damage");
        Pending pending = DEFERRED.get(ticket.id);
        if (pending == null || pending.server != server) return OptionalDouble.empty();
        DEFERRED.remove(ticket.id);
        if (!pending.valid()) return OptionalDouble.empty();
        Scope scope = enter(pending.player, pending.source);
        scope.original = pending.original;
        try {
            return OptionalDouble.of(finish(pending.player, pending.source, afterArmor));
        } finally {
            exit(scope);
        }
    }

    public static void cancel(MinecraftServer server, Ticket ticket) {
        requireServerThread(server);
        Pending pending = DEFERRED.get(Objects.requireNonNull(ticket, "ticket").id);
        if (pending != null && pending.server == server) DEFERRED.remove(ticket.id);
    }

    public static void forget(EntityPlayer player) {
        DEFERRED.values().removeIf(pending -> pending.player == player);
    }

    public static void forgetWorld(World world) {
        DEFERRED.values().removeIf(pending -> pending.world == world);
    }

    public static void expireDeferred() {
        DEFERRED.values().removeIf(pending -> !pending.valid());
    }

    public static void clearDeferred() {
        DEFERRED.clear();
    }

    public static void reportMissingBaseline(EntityPlayerMP player, DamageSource source) {
        long now = System.nanoTime();
        if (warned && now - lastWarning < 30_000_000_000L) return;
        warned = true;
        lastWarning = now;
        LOGGER.warn("No paired pre-armour damage for player {} from {} ({}). Keeping post-armour damage; "
                + "baseline-dependent defence/shield were skipped. A delayed/custom damage path needs a DamageTransactions adapter.",
                player.getUniqueID(), source.damageType, source.getClass().getName());
    }

    private static void requireServerThread(MinecraftServer server) {
        if (server == null || !server.isCallingFromMinecraftThread())
            throw new IllegalStateException("Deferred damage must be managed on the server thread");
    }

    public static final class Scope {
        private final EntityLivingBase victim;
        private final DamageSource source;
        private final Scope parent;
        private float original = Float.NaN;
        private boolean consumed;
        private HitResult hit;

        private Scope(EntityLivingBase victim, DamageSource source, Scope parent) {
            this.victim = victim;
            this.source = source;
            this.parent = parent;
        }
    }

    /** A positive formal settlement, not attackEntityFrom's acceptance flag or a health-delta guess. */
    public static final class HitResult implements AutoCloseable {
        private final EntityLivingBase victim;
        private final DamageSource source;
        private final HitResult parent;
        private final Scope parentScope;
        private float amount = Float.NaN;
        private boolean exited;

        private HitResult(EntityLivingBase victim, DamageSource source, HitResult parent) {
            this.victim = Objects.requireNonNull(victim, "victim");
            this.source = Objects.requireNonNull(source, "source");
            this.parent = parent;
            this.parentScope = CURRENT.get();
        }

        public boolean confirmed() { return exited && Float.isFinite(amount) && amount > 0; }

        @Override public void close() {
            if (OBSERVED.get() != this) throw new IllegalStateException("Lost Fantasy hit observations closed out of order");
            if (parent == null) OBSERVED.remove();
            else OBSERVED.set(parent);
        }
    }

    @FunctionalInterface
    interface FinalDamage {
        float apply(EntityLivingBase victim, DamageSource source, float amount);
    }

    private static final class FinalCall {
        final Scope scope;
        LivingDamageEvent event;

        FinalCall(Scope scope) { this.scope = scope; }
    }

    public static final class Ticket {
        private final UUID id;
        private Ticket(UUID id) { this.id = id; }
        public UUID attackId() { return id; }
    }

    private static final class Pending {
        final EntityPlayerMP player;
        final DamageSource source;
        final float original;
        final int lifetime, created;
        final MinecraftServer server;
        final World world;

        Pending(EntityPlayerMP player, DamageSource source, float original, int lifetime, MinecraftServer server) {
            this.player = player;
            this.source = source;
            this.original = original;
            this.lifetime = lifetime;
            this.server = server;
            this.world = player.world;
            this.created = server.getTickCounter();
        }

        boolean valid() {
            return Integer.toUnsignedLong(server.getTickCounter() - created) < lifetime
                    && player.isEntityAlive() && player.world == world
                    && server.getPlayerList().getPlayerByUUID(player.getUniqueID()) == player;
        }
    }
}
