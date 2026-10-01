package dev.lostfantasy.combat;

import dev.lostfantasy.GameEvents;
import dev.lostfantasy.TestWorld;
import dev.lostfantasy.data.PlayerData;
import java.lang.reflect.Field;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import sun.misc.Unsafe;

import static org.junit.Assert.*;

/** Exercises binding and the real GameEvents mitigation without a server, connection or game save. */
public class DamageTransactionsTest {
    private Player player;
    private final DamageSource source = new DamageSource("binding_test");

    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Before public void createPlayer() throws Exception {
        // Only world/capability/identity are used. Avoid constructing a networked EntityPlayerMP.
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        player = (Player) ((Unsafe) field.get(null)).allocateInstance(Player.class);
        player.world = new TestWorld();
        player.data = new PlayerData();
        player.data.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,4);
    }

    @Test public void prematureFinalEventCannotClaimBaseline() {
        DamageTransactions.Scope scope = DamageTransactions.enter(player, source);
        try {
            DamageTransactions.capture(100, player, source);
            LivingDamageEvent premature = event(20);
            DamageTransactions.bind(premature);
            assertTrue(Float.isNaN(DamageTransactions.consume(premature)));
        } finally {
            DamageTransactions.exit(scope);
        }
    }

    @Test public void prematureSameSourceHookDoesNotSpendShieldOrStealRealMitigation() {
        GameEvents events = new GameEvents();
        DamageTransactions.Scope scope = DamageTransactions.enter(player, source);
        try {
            DamageTransactions.capture(100, player, source);
            LivingDamageEvent premature = event(20);
            DamageTransactions.bind(premature); // Same bind hook as an extra ForgeHooks.onLivingDamage call.
            events.damage(premature);
            assertEquals(20, premature.getAmount(), .0001);
            assertEquals(80, player.data.shield, .0001);

            float remaining = DamageTransactions.finish(player, source, 40, (victim, damageSource, amount) -> {
                LivingDamageEvent actual = event(amount);
                DamageTransactions.bind(actual);
                events.damage(actual);
                assertTrue(Float.isNaN(DamageTransactions.consume(actual)));
                return actual.getAmount();
            });
            // 40 post-armour - 100 * 10% passive defence - 100 * 15% shield.
            assertEquals(15, remaining, .0001);
            assertEquals(65, player.data.shield, .0001);
        } finally {
            DamageTransactions.exit(scope);
        }
    }

    @Test public void nestedSameSourceDamageRestoresOuterEventAndBaseline() {
        DamageTransactions.Scope outer = DamageTransactions.enter(player, source);
        try {
            DamageTransactions.capture(100, player, source);
            DamageTransactions.finish(player, source, 40, (victim, damageSource, amount) -> {
                LivingDamageEvent actual = event(amount);
                DamageTransactions.bind(actual);
                LivingDamageEvent manual = event(9);
                DamageTransactions.bind(manual);
                assertTrue(Float.isNaN(DamageTransactions.consume(manual)));

                DamageTransactions.Scope inner = DamageTransactions.enter(player, source);
                try {
                    DamageTransactions.capture(30, player, source);
                    DamageTransactions.finish(player, source, 10, (nestedVictim, nestedSource, nestedAmount) -> {
                        LivingDamageEvent nested = event(nestedAmount);
                        DamageTransactions.bind(nested);
                        assertEquals(30, DamageTransactions.consume(nested), 0);
                        return nestedAmount;
                    });
                } finally {
                    DamageTransactions.exit(inner);
                }
                assertEquals(100, DamageTransactions.consume(actual), 0);
                assertTrue(Float.isNaN(DamageTransactions.consume(actual)));
                return amount;
            });
        } finally {
            DamageTransactions.exit(outer);
        }
    }

    @Test public void exceptionsCloseTheFinalBindingWindow() {
        DamageTransactions.Scope scope = DamageTransactions.enter(player, source);
        LivingDamageEvent actual = event(40);
        RuntimeException failure = new RuntimeException("listener failure");
        try {
            DamageTransactions.capture(100, player, source);
            try {
                DamageTransactions.finish(player, source, 40, (victim, damageSource, amount) -> {
                    DamageTransactions.bind(actual);
                    throw failure;
                });
                fail("Expected listener failure");
            } catch (RuntimeException caught) {
                assertSame(failure, caught);
            }
            assertTrue(Float.isNaN(DamageTransactions.consume(actual)));
            LivingDamageEvent later = event(40);
            DamageTransactions.bind(later);
            assertTrue(Float.isNaN(DamageTransactions.consume(later)));
        } finally {
            DamageTransactions.exit(scope);
        }
        DamageTransactions.bind(actual);
        assertTrue(Float.isNaN(DamageTransactions.consume(actual)));
    }

    @Test public void cancellationAlsoClosesTheBindingWindow() {
        DamageTransactions.Scope scope = DamageTransactions.enter(player, source);
        LivingDamageEvent cancelled = event(40);
        try {
            DamageTransactions.capture(100, player, source);
            float result = DamageTransactions.finish(player, source, 40, (victim, damageSource, amount) -> {
                DamageTransactions.bind(cancelled);
                cancelled.setCanceled(true);
                return 0;
            });
            assertEquals(0, result, 0);
            assertTrue(Float.isNaN(DamageTransactions.consume(cancelled)));
        } finally {
            DamageTransactions.exit(scope);
        }
    }

    @Test public void unknownBaselineAndMismatchedSourceCannotBind() {
        DamageTransactions.Scope scope = DamageTransactions.enter(player, source);
        try {
            DamageTransactions.finish(player, source, 40, (victim, damageSource, amount) -> {
                LivingDamageEvent unknown = event(amount);
                DamageTransactions.bind(unknown);
                assertTrue(Float.isNaN(DamageTransactions.consume(unknown)));
                return amount;
            });
            DamageTransactions.capture(100, player, source);
            DamageTransactions.finish(player, new DamageSource("binding_test"), 40, (victim, damageSource, amount) -> {
                LivingDamageEvent wrongCall = event(amount);
                DamageTransactions.bind(wrongCall);
                assertTrue(Float.isNaN(DamageTransactions.consume(wrongCall)));
                return amount;
            });
        } finally {
            DamageTransactions.exit(scope);
        }
    }

    private LivingDamageEvent event(float amount) {
        // Forge normally generates this override from @Cancelable during class loading.
        return new LivingDamageEvent(player, source, amount) {
            @Override public boolean isCancelable() { return true; }
        };
    }

    private static final class Player extends EntityPlayerMP {
        PlayerData data;
        private Player() { super((MinecraftServer) null, (WorldServer) null, null, (PlayerInteractionManager) null); }
        @Override public UUID getUniqueID() { return new UUID(0, 22); }
        @Override @SuppressWarnings("unchecked")
        public <T> T getCapability(Capability<T> capability, EnumFacing facing) { return (T) data; }
    }
}
