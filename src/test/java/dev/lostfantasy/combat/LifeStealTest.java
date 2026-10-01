package dev.lostfantasy.combat;

import com.mojang.authlib.GameProfile;
import dev.lostfantasy.Balance;
import dev.lostfantasy.GameEvents;
import dev.lostfantasy.TestWorld;
import dev.lostfantasy.core.Growth;
import dev.lostfantasy.data.PlayerData;
import java.util.UUID;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class LifeStealTest {
    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Test public void damageCancelledAfterOurListenerDoesNotHeal() {
        for (boolean cancel : new boolean[]{true, false}) {
            TestWorld world = new TestWorld();
            Player player = new Player(world);
            EntityCreature victim = new EntityCreature(world) {};
            DamageEventFixture events = new DamageEventFixture();
            events.damage = event -> {
                new GameEvents().damage(event);
                assertEquals(0, player.healed, 0);
                if (cancel) event.setCanceled(true); else event.setAmount(0);
            };
            assertEquals(0, events.apply(victim, source(player), 10), 0);
            assertEquals(0, player.healed, 0);
        }
    }

    @Test public void healingUsesFinalAmountAndCannotExceedRemainingHealth() {
        TestWorld world = new TestWorld();
        Player player = new Player(world);
        EntityCreature victim = new EntityCreature(world) {};
        DamageEventFixture events = new DamageEventFixture();
        events.damage = event -> event.setAmount(4);
        assertEquals(4, events.apply(victim, source(player), 10), 0);
        assertEquals(4 * 2 * Balance.lifeStealPerTier, player.healed, 1e-6);
        player.healed = 0;
        victim.setHealth(2);
        events.apply(victim, source(player), 10);
        assertEquals(2 * 2 * Balance.lifeStealPerTier, player.healed, 1e-6);
    }

    @Test public void spellsSelfDamageHurtCancellationAndFailedHooksDoNotHeal() {
        TestWorld world = new TestWorld();
        Player player = new Player(world);
        EntityCreature victim = new EntityCreature(world) {};
        DamageEventFixture events = new DamageEventFixture();
        events.apply(victim, new EntityDamageSource("lostfantasy.spell", player), 10);
        events.apply(player, source(player), 10);
        events.hurt = event -> event.setCanceled(true);
        events.apply(victim, source(player), 10);
        try {
            DamageTransactions.finish(victim, source(player), 10, (v, s, amount) -> {
                throw new IllegalStateException("listener failed");
            });
            fail();
        } catch (IllegalStateException expected) {}
        assertEquals(0, player.healed, 0);
        events.hurt = event -> {};
        events.apply(victim, source(player), 10);
        assertEquals(10 * 2 * Balance.lifeStealPerTier, player.healed, 1e-6);
    }

    private static DamageSource source(Player player) { return new EntityDamageSource("player", player); }
    private static final class Player extends EntityPlayer {
        final PlayerData data = new PlayerData();
        float healed;
        Player(TestWorld world) {
            super(world, new GameProfile(UUID.randomUUID(), "VampireTester"));
            data.grantStage(Growth.Route.VAMPIRE, 2);
        }
        @Override public boolean isSpectator() { return false; }
        @Override public boolean isCreative() { return false; }
        @Override public void heal(float amount) { healed += amount; }
        @SuppressWarnings("unchecked")
        @Override public <T> T getCapability(Capability<T> capability, EnumFacing face) { return (T) data; }
    }
}
